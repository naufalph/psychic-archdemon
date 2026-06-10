# Notification API - cURL Commands for Postman/Bruno

## Prerequisites
- Replace `{{JWT_TOKEN}}` with your actual JWT token (client or architect)
- Replace `{{NOTIFICATION_ID}}` with the notification ID from the response
- All endpoints require an authenticated user

**Base URL:** `http://localhost:8080/rmtr/notifications`

---

## API Endpoints

---

### 1. Get All Notifications

**GET** `/rmtr/notifications`

Returns all notifications for the authenticated user, ordered by `createdAt` DESC.

```bash
curl --location 'http://localhost:8080/rmtr/notifications' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "type": "BID_RECEIVED",
      "title": "New Bid Received",
      "message": "PT Karya Desain has submitted a bid on your project Modern Family Home",
      "messageCode": "notification.bid.received",
      "messageData": "{\"bidderName\":\"PT Karya Desain\",\"projectTitle\":\"Modern Family Home\"}",
      "referenceType": "PROJECT",
      "referenceId": 5,
      "isRead": false,
      "readAt": null,
      "createdAt": "2026-06-01T10:00:00"
    },
    {
      "id": 2,
      "type": "PROJECT_VALIDATED",
      "title": "Project Approved",
      "message": "Your project Modern Family Home has been approved and is now open for bidding.",
      "messageCode": "notification.project.validated",
      "messageData": null,
      "referenceType": "PROJECT",
      "referenceId": 5,
      "isRead": true,
      "readAt": "2026-06-01T09:30:00",
      "createdAt": "2026-05-31T15:00:00"
    }
  ],
  "timestamp": "2026-06-01T12:00:00"
}
```

---

### 2. Get Unread Notifications

**GET** `/rmtr/notifications/unread`

Returns only unread notifications for the authenticated user.

```bash
curl --location 'http://localhost:8080/rmtr/notifications/unread' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):** `ApiResponse<List<NotificationResponse>>` (same structure as above, only unread items)

---

### 3. Get Unread Count

**GET** `/rmtr/notifications/unread-count`

Returns the number of unread notifications. Use this for badge counts in the UI.

```bash
curl --location 'http://localhost:8080/rmtr/notifications/unread-count' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "unreadCount": 3
  },
  "timestamp": "2026-06-01T12:00:00"
}
```

---

### 4. Mark Notification as Read

**PUT** `/rmtr/notifications/{{NOTIFICATION_ID}}/read`

Marks a single notification as read.

```bash
curl --location --request PUT 'http://localhost:8080/rmtr/notifications/1/read' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):** `ApiResponse<NotificationResponse>` — the updated notification with `isRead: true` and `readAt` set.

---

### 5. Mark All as Read

**PUT** `/rmtr/notifications/read-all`

Marks all unread notifications as read for the authenticated user.

```bash
curl --location --request PUT 'http://localhost:8080/rmtr/notifications/read-all' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": 3,
  "timestamp": "2026-06-01T12:00:00"
}
```

The `data` field is an integer indicating how many notifications were marked as read.

---

## DTO Reference

### NotificationResponse

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long | |
| `type` | NotificationType | See enum below |
| `title` | String | Human-readable title |
| `message` | String | Human-readable message body |
| `messageCode` | String | i18n key for frontend translation |
| `messageData` | String | JSON string with template variables for message |
| `referenceType` | String | e.g. `"PROJECT"`, `"BID"`, `"PHASE"` |
| `referenceId` | Long | ID of the referenced entity |
| `isRead` | Boolean | |
| `readAt` | LocalDateTime | null if unread |
| `createdAt` | LocalDateTime | |

### UnreadCountResponse

| Field | Type |
|-------|------|
| `unreadCount` | Long |

---

## Notification Types

| Type | Triggered by |
|------|-------------|
| `PROJECT_VALIDATED` | Superuser approves or rejects a project |
| `PROJECT_UPDATED` | Project details are updated |
| `BID_RECEIVED` | An architect submits a bid on client's project |
| `BID_ACCEPTED` | Client accepts architect's bid |
| `PAYMENT_RECEIVED` | Phase payment confirmed by Xendit |
| `SUPPORT_REQUESTED` | A user escalates a conversation to support |
| `BIDDING_DEADLINE_REMINDER` | Reminder that a project's bidding deadline is approaching |
| `PROJECT_CLOSED_NO_WINNER` | A project closed without any accepted bid |
| `REVISION_REQUESTED` | Client requests a revision on delivered phase work |

---

## Frontend Implementation Tips

- Call `GET /rmtr/notifications/unread-count` on app load and after authentication to initialize the notification badge
- Poll `GET /rmtr/notifications/unread-count` periodically, or use WebSocket push (if available) to update badge in real-time
- When user opens the notification panel: call `GET /rmtr/notifications` to load all, then `PUT /rmtr/notifications/read-all`
- Use `referenceType` + `referenceId` to route user to the relevant page when tapping a notification
- Use `messageCode` + `messageData` for i18n if the frontend supports it; fall back to `message` for simple display

---

## Testing Sequence

1. **Login as Client** → Get JWT token
2. *(Trigger notifications by performing actions: create project, accept bid, etc.)*
3. **Get Unread Count** → `GET /rmtr/notifications/unread-count`
4. **Get All Notifications** → `GET /rmtr/notifications`
5. **Get Unread Only** → `GET /rmtr/notifications/unread`
6. **Mark One as Read** → `PUT /rmtr/notifications/{id}/read`
7. **Mark All as Read** → `PUT /rmtr/notifications/read-all`
8. **Verify Count** → `GET /rmtr/notifications/unread-count` — should be 0

---

## Error Responses

| Status | Reason |
|--------|--------|
| `401 Unauthorized` | Invalid or expired JWT token |
| `404 Not Found` | Notification not found or not owned by current user |
