# Ledger APIs

## 1. Ledger Retrieval

### Get Ledger
- **Method & Path:** `GET /v1/ledger/{userId}`
- **Description:** Retrieves the full ledger for a specific user.
- **Request Fields:**
    - Path: `userId` (UUID)
- **Response Fields:**
    - Success: HTTP 200 OK, ledger details (structure depends on handler/query)

### Get Ledger Summary
- **Method & Path:** `GET /v1/ledger/summary`
- **Description:** Retrieves a summary of the ledger for the authenticated user.
- **Request Fields:**
    - Authenticated user (from token)
- **Response Fields:**
    - Success: HTTP 200 OK, ledger summary (structure depends on handler/query) 