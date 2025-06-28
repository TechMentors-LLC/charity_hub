# Cases APIs

## 1. Case Management

### Create Case
- **Method & Path:** `POST /v1/cases`
- **Description:** Creates a new case.
- **Request Fields:**
    - Body: `CreateCaseRequest`
        - `title` (String)
        - `description` (String)
        - `goal` (Amount/Number)
        - `publish` (Boolean)
        - `acceptZakat` (Boolean)
        - `documents` (List)
- **Response Fields:**
    - Success: HTTP 201 Created, created case details

### Update Case
- **Method & Path:** `PUT /v1/cases/{caseCode}`
- **Description:** Updates an existing case.
- **Request Fields:**
    - Path: `caseCode` (int)
    - Body: `UpdateCaseRequest`
        - `title` (String)
        - `description` (String)
        - `goal` (Amount/Number)
        - `acceptZakat` (Boolean)
        - `documents` (List)
- **Response Fields:**
    - Success: HTTP 200 OK, updated case details

### Delete Draft Case
- **Method & Path:** `DELETE /v1/cases/{caseCode}`
- **Description:** Deletes a draft case.
- **Request Fields:**
    - Path: `caseCode` (int)
- **Response Fields:**
    - Success: HTTP 200 OK, empty or standard response

---

## 2. Case Status Management

### Open Case
- **Method & Path:** `POST /v1/cases/{caseCode}/open`
- **Description:** Opens a case (makes it active).
- **Request Fields:**
    - Path: `caseCode` (int)
- **Response Fields:**
    - Success: HTTP 200 OK, empty or standard response

### Close Case
- **Method & Path:** `POST /v1/cases/{caseCode}/close`
- **Description:** Closes a case (makes it inactive).
- **Request Fields:**
    - Path: `caseCode` (int)
- **Response Fields:**
    - Success: HTTP 200 OK, empty or standard response

---

## 3. Case Retrieval

### Get All Cases
- **Method & Path:** `GET /v1/cases`
- **Description:** Retrieves all cases, with optional filters.
- **Request Fields:**
    - Query: `GetCasesRequest`
        - `code` (optional)
        - `tag` (optional)
        - `content` (optional)
        - `offset` (optional, int)
        - `limit` (optional, int)
- **Response Fields:**
    - Success: HTTP 200 OK, list of cases

### Get Case
- **Method & Path:** `GET /v1/cases/{caseCode}`
- **Description:** Retrieves a specific case by code.
- **Request Fields:**
    - Path: `caseCode` (int)
- **Response Fields:**
    - Success: HTTP 201 Created, case details

### Get Draft Cases
- **Method & Path:** `GET /v1/draft-cases`
- **Description:** Retrieves all draft cases.
- **Request Fields:** None
- **Response Fields:**
    - Success: HTTP 200 OK, list of draft cases

---

## 4. Contributions

### Contribute to Case
- **Method & Path:** `POST /v1/cases/{caseCode}/contributions`
- **Description:** Contributes to a case.
- **Request Fields:**
    - Path: `caseCode` (int)
    - Body: `ContributeRequest`
        - `amount` (Number)
    - Authenticated user (from token)
- **Response Fields:**
    - Success: HTTP 200 OK, contribution result

### Pay Contribution
- **Method & Path:** `POST /v1/contributions/{contributionId}/pay`
- **Description:** Marks a contribution as paid.
- **Request Fields:**
    - Path: `contributionId` (UUID)
- **Response Fields:**
    - Success: HTTP 200 OK, empty or standard response

### Confirm Contribution
- **Method & Path:** `POST /v1/contributions/{contributionId}/confirm`
- **Description:** Confirms a contribution.
- **Request Fields:**
    - Path: `contributionId` (UUID)
- **Response Fields:**
    - Success: HTTP 200 OK, empty or standard response
