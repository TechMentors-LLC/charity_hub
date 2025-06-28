# Accounts APIs

## 1. Authentication & Session Management

### Authenticate (Login)
- **Method & Path:** `POST /v1/accounts/authenticate`
- **Description:** Authenticates a user and returns a token/session.
- **Request Fields:**
    - Body: `Authenticate`
        - (Fields depend on the `Authenticate` class, likely username/password or similar)
- **Response Fields:**
    - Success: HTTP 200 OK, authentication result (token, user info, etc.)

### Refresh Token
- **Method & Path:** `POST /v1/accounts/refresh-token`
- **Description:** Refreshes the authentication token for the current user/device.
- **Request Fields:**
    - Authenticated user (from token)
- **Response Fields:**
    - Success: HTTP 200 OK, `BasicResponse`
        - (Contains new access token)

---

## 2. User Profile Management

### Update Basic Info
- **Method & Path:** `POST /v1/accounts/update-basic-info`
- **Description:** Updates the basic info (name, photo) of the authenticated user.
- **Request Fields:**
    - Body: `UpdateBasicInfoRequest`
        - `fullName` (String)
        - `photoUrl` (String)
    - Authenticated user (from token)
- **Response Fields:**
    - Success: HTTP 200 OK, `BasicResponse`
        - (Contains new access token)

---

## 3. Permissions & Access Control

### Add User Permission
- **Method & Path:** `POST /v1/accounts/{userId}/add-permission`
- **Description:** Adds a permission to a user account.
- **Request Fields:**
    - Path: `userId` (UUID)
    - Body: `ChangePermissionRequest`
        - `permission` (String)
- **Response Fields:**
    - Success: HTTP 200 OK, empty or standard response

### Remove User Permission
- **Method & Path:** `POST /v1/accounts/{userId}/remove-permission`
- **Description:** Removes a permission from a user account.
- **Request Fields:**
    - Path: `userId` (UUID)
    - Body: `ChangePermissionRequest`
        - `permission` (String)
- **Response Fields:**
    - Success: HTTP 200 OK, empty or standard response

---

## 4. Account Status Management

### Block Account
- **Method & Path:** `POST /v1/accounts/{userId}/block`
- **Description:** Blocks a user account.
- **Request Fields:**
    - Path: `userId` (String)
- **Response Fields:**
    - Success: HTTP 200 OK, empty or standard response

### Unblock User
- **Method & Path:** `POST /v1/accounts/{userId}/un-block`
- **Description:** Unblocks a user account.
- **Request Fields:**
    - Path: `userId` (String)
- **Response Fields:**
    - Success: HTTP 200 OK, empty or standard response

---

## 5. User Connections

### Get Connections (Current User)
- **Method & Path:** `GET /v1/accounts/connections`
- **Description:** Gets the connections for the authenticated user.
- **Request Fields:**
    - Authenticated user (from token)
- **Response Fields:**
    - Success: HTTP 200 OK, list of connections

### Get Connections (Admin)
- **Method & Path:** `GET /v1/accounts/{userId}/connections`
- **Description:** Gets the connections for a user (admin access).
- **Request Fields:**
    - Path: `userId` (UUID)
- **Response Fields:**
    - Success: HTTP 200 OK, list of connections

---

## 6. User Onboarding

### Invite User
- **Method & Path:** `POST /v1/accounts/invite`
- **Description:** Invites a new user by mobile number.
- **Request Fields:**
    - Body: `InviteUserRequest`
        - `mobileNumber` (String)
- **Response Fields:**
    - Success: HTTP 200 OK, invitation result

---

## 7. Device & Notification Management

### Register FCM Token
- **Method & Path:** `POST /v1/accounts/register-fcm-token`
- **Description:** Registers a Firebase Cloud Messaging (FCM) token for push notifications.
- **Request Fields:**
    - Body: `RegisterFCMTokenRequest`
        - `fcmToken` (String)
    - Authenticated user (from token)
- **Response Fields:**
    - Success: HTTP 201 Created, empty
