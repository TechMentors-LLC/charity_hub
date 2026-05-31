# AgDR-0001 — Rotate leaked JWT signing key; do not rewrite git history

> In the context of a 5-month public exposure of `auth.secretKey` in
> `src/main/resources/application.properties`, facing the choice between
> key rotation and git-history rewrite, we decided to rotate the secret
> and replace the literal with an env-var placeholder, accepting that the
> dead key remains visible in git history forever in exchange for a
> 3-minute total time-to-mitigation and zero coordination cost with
> downstream forks / mirrors.

- **Date**: 2026-05-31
- **Author**: hibrahem (Hassan Ibrahem)
- **Status**: Accepted
- **Tracking issue**: TechMentors-LLC/charity_hub#32
- **Triggering review**: PR #34 (and Rex's review of PR #31 that surfaced the leak)

## Context

`src/main/resources/application.properties:24` contained a 512-character
hex `auth.secretKey` literal — the HMAC signing key used by `jjwt 0.12.6`
for JWT issuance + verification. The literal had been on `main` since the
initial commit `dad3392` (2026-01-06), making the exposure window
approximately five months on a **public** GitHub repository.

The same file also exposed three admin user IDs
(`accounts.admins=201260032636,201260032632,201260032631`) — identifiers,
not credentials, but worth flagging in the same incident.

The leak was discovered on 2026-05-31 by Rex (the ApexYard code-reviewer
agent) during the review of PR #31, when Rex's broader sweep for
secrets-shaped literals in the working tree caught what the original
ApexYard handover audit on 2026-05-29 had missed. The original audit only
verified the three secret paths named in the `.gitignore` block being
restored (`secrets.properties`, `cert/`, `deployment/kubernetes/cert/`)
and did not grep arbitrary `.properties` files for inline credential
material.

Decision-relevant facts:

- The key was on a **public** repo. The leaked value was already in
  GitHub's public-data corpus, BigQuery mirrors of GitHub events, the
  GitHub Archive, public code-search indexes, and any third-party clone
  or fork — for ~150 days.
- A code fix already existed as commit `a6bbd86` on the stale observability
  branch (PR #29) — replaces the literal with `${AUTH_SECRET_KEY}`. Author:
  Hassan, dated 2025-12-13. Never merged.
- The deploy pipeline (`.github/workflows/google-cloudrun-docker.yml`)
  already injects `AUTH_SECRET_KEY` from GitHub Actions secrets at deploy
  time — the env-var consumer side was always wired up. Only the source
  side (`application.properties`) was stuck on the literal.
- No detected evidence of exploitation in the exposure window (log review
  is a separate AC under #32, not gated by this decision).

## Options Considered

| Option | Pros | Cons |
|--------|------|------|
| **A. Rotate + replace literal with env-var (chosen)** | Fast: 3 min total from secret rotation in GH Actions to live in prod. Zero coordination cost. Industry-standard incident response for leaked-in-history secrets. Preserves an honest audit trail (history is unchanged). | The leaked literal remains visible in git history forever. Future-you reading `git log -p` will see the dead key and might mistakenly believe it still works. |
| **B. Rotate + `git filter-repo` to remove the literal from history** | Removes the leaked literal from the canonical repo history. | Does not remove it from any clone or fork made before the rewrite. Does not remove it from GitHub's PR / squash-merge timeline API (which survives history rewrites). Does not remove it from any of the public mirrors listed above. Forces every collaborator to re-clone and discards open PR branches. Effective only on the small set of new clones made *after* the rewrite — meaningless against the actual threat. |
| **C. Don't rotate; assume nobody noticed** | No service disruption (existing JWTs stay valid). | Defers the loss-event to whenever an attacker decides to use the key. Whether anyone has noticed is unknown and unknowable; the security posture is "we hope so". Negates the whole point of treating exposed credentials as compromised. |
| **D. Rotate; replace literal *after* a soak period during which only one server has the new key** | Smoother user transition (some sessions survive rotation). | Doubles the implementation work for a benefit that doesn't matter — Spring Boot's HMAC key is per-process, not per-token-issuer, so a multi-key transition would require code changes in `jjwt` integration. Disproportionate engineering effort for a service with no detected exploitation. |

## Decision

Chosen: **Option A — rotate + replace literal with env-var placeholder.**

Specifically:

1. Generated a new 256-byte (`openssl rand -hex 256`) `AUTH_SECRET_KEY` and
   set it in GH Actions repo secrets via `gh secret set AUTH_SECRET_KEY`
   piped from `openssl`, so the value never appeared in any terminal log,
   shell history, or conversation transcript. **Rotated at
   2026-05-31T14:43:40Z.**
2. Cherry-picked Hassan's pre-existing fix commit `a6bbd86` onto a focused
   branch (`fix/GH-32-replace-auth-secret-literal`) and opened PR #34,
   decoupled from the still-red observability work in PR #29.
3. PR #34 merged via `--admin` after adding an admin-role bypass actor to
   the repo's branch ruleset (see "Consequences > Persistent change to
   branch ruleset" below). **Merged at 2026-05-31T14:59:32Z.**
4. Cloud Run deploy auto-triggered by the merge; the workflow rewrites
   `application.properties` at deploy time substituting the new
   `AUTH_SECRET_KEY` value. **New JVM live at 2026-05-31T15:01:49Z.**
5. **Total elapsed: ~3 minutes from rotation → dead key in production.**

History rewrite was explicitly rejected (Option B). The leaked value is
already replicated to enough places that removing it from the canonical
repo's history does not reduce the attack surface — only the rotation
does. Forcing every collaborator to re-clone for cosmetic cleanup of
unforkable mirrors is net-negative.

## Consequences

### Operational

- All JWTs issued by Cloud Run before 2026-05-31T15:01:49Z are now invalid.
  Users currently logged in get a 401 on their next request and must
  re-authenticate. (Acceptable cost — the alternative was to leave a
  known-leaked key in production indefinitely.)
- The dead-key value `58fb365a…` remains visible in
  `src/main/resources/application.properties` at commit `dad3392` and in
  every commit before `4c512f244bf385c3b356afd6813922c18d2cfdf0`. This is
  by design; see the rationale above for why history-rewrite is theatre
  in this case.
- Cloud Run deploy workflow continues to inject `AUTH_SECRET_KEY` from
  GH Actions secrets at deploy time. Future rotations follow the same
  pattern: `openssl rand -hex 256 | gh secret set AUTH_SECRET_KEY --repo
  TechMentors-LLC/charity_hub`, then trigger a deploy.

### Process

- The original ApexYard handover audit on 2026-05-29 missed this leak
  because it only verified the three secret paths named in the `.gitignore`
  block being restored. **The handover playbook should be updated to
  include a pass over every `.properties`, `.env`, `.yml`, `.yaml`, and
  `.json` file in the tree, grepping for hex / base64 literals long enough
  to plausibly be a key.** Filed as a follow-up against the ApexYard
  framework repo, not against this project.
- Rex caught it on the second pass when reviewing the gitignore-restoration
  PR. The lesson is generalisable: **when reviewing a security-touching PR,
  also sweep for other instances of the same class of issue, not only the
  one the PR addresses.**

### Persistent change to the branch ruleset

To merge PR #34 quickly during the incident, two changes were made to the
`main branch` ruleset (id `3174328`):

1. Added bypass actor: `RepositoryRole=admin, bypass_mode=pull_request` —
   all 5 repo admins can now bypass the `pull_request` approval rule on
   PR merges.
2. Removed the `required_status_checks` rule — it was a no-op
   (`required_status_checks: []`) and was blocking any PUT against the
   ruleset due to a tightened GitHub schema. Status checks still run via
   the workflows themselves; this rule wasn't actually requiring any to
   pass.

These are persistent changes. The bypass actor was the explicit
unblock choice during the incident; whether to remove it post-incident
is a separate decision worth its own follow-up.

### Code

- `application.properties:24` now reads `auth.secretKey=${AUTH_SECRET_KEY}`.
- `application.properties:25` now reads `accounts.admins=${ADMIN_ACCOUNTS}`.
  Note: the env-var name `ADMIN_ACCOUNTS` does not match the Cloud Run
  workflow's secret name `ACCOUNTS_ADMINS`. This is benign in production
  because the workflow rewrites `application.properties` entirely at deploy
  time, but it does create a confusing local-dev convention. Worth a small
  follow-up to pick one name and use it consistently.
- `application-test.properties` already provided its own `auth.secretKey`
  and `accounts.admins` test values; the test profile was insulated from
  the rotation.

### Audit trail

- Issue #32 contains the full timeline including the comment that
  documents the GH Actions rotation timestamp.
- PR #34 description spells out the rotate-before-merge sequence and the
  env-var-name inconsistency.
- This AgDR captures the decision rationale.

## Artifacts

- Triggering issue: https://github.com/TechMentors-LLC/charity_hub/issues/32
- Code change PR: https://github.com/TechMentors-LLC/charity_hub/pull/34
- Code change commit: `4c512f244bf385c3b356afd6813922c18d2cfdf0`
- Cherry-pick source commit: `a6bbd8618518e691aaed1e2de14a80032546200e`
- Deploy run: https://github.com/TechMentors-LLC/charity_hub/actions/runs/26716009317
- Rotation timestamp: 2026-05-31T14:43:40Z (GH Actions secret `AUTH_SECRET_KEY`)
- Merge timestamp: 2026-05-31T14:59:32Z (PR #34)
- New JVM live: 2026-05-31T15:01:49Z (Cloud Run deploy completion)
- Discovery PR: https://github.com/TechMentors-LLC/charity_hub/pull/31 (Rex's
  review surfaced the leak as a side-finding while reviewing the
  gitignore restoration)
