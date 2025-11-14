# Dashboard Notification API - cURL Commands for Postman Import

## Prerequisites
- Replace `{{JWT_TOKEN}}` with your actual JWT token (client or architect)
- Replace `{{NOTIFICATION_ID}}` with the notification ID from the response
- All endpoints require authentication
- Available to any authenticated user (both CLIENT and ARCHITECT roles)

---

## 1. Get All Notifications

**GET** `/api/notifications`

Get all dashboard notifications for the authenticated user, ordered by most recent first.

```bash
curl --location 'http://localhost:8080/api/notifications' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Notifications retrieved successfully",
  "data": [
    {
      "id": 2,
      "userId": 1,
      "type": "PROJECT_VALIDATED",
      "title": "Project Approved",
      "message": "Your project 'Single Family Home (Project #1)' has been approved and is now visible to architects!",
      "referenceType": "PROJECT",
      "referenceId": 1,
      "isRead": false,
      "readAt": null,
      "createdAt": "2025-11-13T10:30:00.123456"
    },
    {
      "id": 1,
      "userId": 1,
      "type": "PROJECT_VALIDATED",
      "title": "Project Needs Changes",
      "message": "Your project 'Office Building (Project #2)' requires changes. Please review and update your project.",
      "referenceType": "PROJECT",
      "referenceId": 2,
      "isRead": true,
      "readAt": "2025-11-13T11:00:00.123456",
      "createdAt": "2025-11-13T09:15:00.123456"
    }
  ],
  "timestamp": "2025-11-13T12:00:00.123456"
}
```

---

## 2. Get Unread Notifications

**GET** `/api/notifications/unread`

Get only unread dashboard notifications for the authenticated user.

```bash
curl --location 'http://localhost:8080/api/notifications/unread' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Unread notifications retrieved successfully",
  "data": [
    {
      "id": 2,
      "userId": 1,
      "type": "PROJECT_VALIDATED",
      "title": "Project Approved",
      "message": "Your project 'Single Family Home (Project #1)' has been approved and is now visible to architects!",
      "referenceType": "PROJECT",
      "referenceId": 1,
      "isRead": false,
      "readAt": null,
      "createdAt": "2025-11-13T10:30:00.123456"
    }
  ],
  "timestamp": "2025-11-13T12:05:00.123456"
}
```

---

## 3. Get Unread Notification Count

**GET** `/api/notifications/unread-count`

Get the count of unread notifications for the authenticated user (useful for notification badges).

```bash
curl --location 'http://localhost:8080/api/notifications/unread-count' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Unread count retrieved successfully",
  "data": {
    "unreadCount": 3
  },
  "timestamp": "2025-11-13T12:10:00.123456"
}
```

---

## 4. Mark Notification as Read

**PUT** `/api/notifications/{{NOTIFICATION_ID}}/read`

Mark a specific notification as read. The `readAt` timestamp will be automatically set.

```bash
curl --location --request PUT 'http://localhost:8080/api/notifications/2/read' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Notification marked as read",
  "data": {
    "id": 2,
    "userId": 1,
    "type": "PROJECT_VALIDATED",
    "title": "Project Approved",
    "message": "Your project 'Single Family Home (Project #1)' has been approved and is now visible to architects!",
    "referenceType": "PROJECT",
    "referenceId": 1,
    "isRead": true,
    "readAt": "2025-11-13T12:15:00.123456",
    "createdAt": "2025-11-13T10:30:00.123456"
  },
  "timestamp": "2025-11-13T12:15:00.234567"
}
```

**Error Response - Not Found (404):**
```json
{
  "success": false,
  "message": "Notification not found with id: 999",
  "data": null,
  "timestamp": "2025-11-13T12:16:00.123456"
}
```

**Error Response - Forbidden (403):**
```json
{
  "success": false,
  "message": "You do not have permission to access this notification",
  "data": null,
  "timestamp": "2025-11-13T12:17:00.123456"
}
```

---

## 5. Mark All Notifications as Read

**PUT** `/api/notifications/read-all`

Mark all unread notifications as read for the authenticated user.

```bash
curl --location --request PUT 'http://localhost:8080/api/notifications/read-all' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "3 notifications marked as read",
  "data": 3,
  "timestamp": "2025-11-13T12:20:00.123456"
}
```

**When No Unread Notifications:**
```json
{
  "success": true,
  "message": "0 notifications marked as read",
  "data": 0,
  "timestamp": "2025-11-13T12:21:00.123456"
}
```

---

## Notification Types

The system currently supports the following notification types:

| Type | Description | Triggered By |
|------|-------------|--------------|
| `PROJECT_VALIDATED` | Project approval/rejection by superuser | Superuser validates a project |
| `PROJECT_UPDATED` | Project details updated | Client updates project (future) |
| `BID_RECEIVED` | New bid submitted on project | Architect submits bid (future) |
| `BID_ACCEPTED` | Bid accepted by client | Client accepts architect bid (future) |
| `PAYMENT_RECEIVED` | Payment received confirmation | Payment processed (future) |

---

## Event-Driven Architecture

### How Notifications Are Created

1. **Event Publishing**: When a superuser validates a project, `ProjectService` publishes a `ProjectValidatedEvent`
2. **Event Listeners**:
   - **NotificationEventListener**: Creates dashboard notification (synchronous, transactional)
   - **EmailEventListener**: Sends email notification (asynchronous, runs in background)
3. **Transactional Safety**: Notifications are only created AFTER the database transaction commits successfully
4. **Async Email**: Email sending doesn't block the main request flow

### Example Flow: Project Validation

```
Superuser validates project
    ↓
ProjectService.updateProjectValidation()
    ↓
Publishes ProjectValidatedEvent
    ↓
    ├→ NotificationEventListener
    │   └→ Creates dashboard notification in DB
    │
    └→ EmailEventListener (async)
        └→ Sends validation email to client
```

---

## Integration with Frontend

### Typical Frontend Usage

```javascript
// Get unread count for notification badge
const getUnreadCount = async () => {
  const response = await axios.get('/api/notifications/unread-count', {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data.data.unreadCount;
};

// Get all notifications for notification dropdown
const getNotifications = async () => {
  const response = await axios.get('/api/notifications', {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data.data;
};

// Mark notification as read when clicked
const markAsRead = async (notificationId) => {
  await axios.put(`/api/notifications/${notificationId}/read`, {}, {
    headers: { Authorization: `Bearer ${token}` }
  });
};

// Mark all as read
const markAllAsRead = async () => {
  await axios.put('/api/notifications/read-all', {}, {
    headers: { Authorization: `Bearer ${token}` }
  });
};
```

### Polling Strategy (Optional)

Since this is dashboard notification (not real-time WebSocket), you can poll for new notifications:

```javascript
// Poll every 30 seconds
setInterval(async () => {
  const count = await getUnreadCount();
  updateNotificationBadge(count);
}, 30000);
```

---

## Security

### Authentication & Authorization

- **Authentication Required**: All endpoints require valid JWT token
- **Ownership Verification**: Users can only access their own notifications
- **Role Agnostic**: Works for both CLIENT and ARCHITECT roles
- **User ID from JWT**: User ID is extracted from JWT, NOT from request parameters
- **No Cross-User Access**: Attempting to access another user's notification returns 403 Forbidden

### Security Implementation

```java
// User ID always comes from JWT (SecurityUtils)
Long userId = SecurityUtils.getCurrentUserId();

// Ownership verification in service layer
if (!notification.getUser().getId().equals(userId)) {
    throw new RuntimeException("You do not have permission...");
}
```

---

## Testing Tips

1. **Test with Different Roles**: Try with both CLIENT and ARCHITECT tokens
2. **Test Validation Flow**:
   - Create a project as client
   - Login as superuser
   - Validate the project
   - Check client's notifications
3. **Test Ownership**: Try to mark another user's notification as read (should fail)
4. **Test Email**: Check console logs for email sending confirmation
5. **Test Mark All**: Create multiple unread notifications, then mark all as read

---

## Testing the Complete Flow

### Step 1: Create a Project (as Client)
```bash
# Create project with CLIENT token
curl --location 'http://localhost:8080/api/v1/projects' \
--header 'Authorization: Bearer {{CLIENT_JWT_TOKEN}}' \
--form 'budgetMin="50000000"' \
--form 'budgetMax="100000000"' \
--form 'buildingFunction="Residential House"' \
# ... other fields
```

### Step 2: Validate Project (as Superuser)
```bash
# Validate project with SUPERUSER token
curl --location --request PUT 'http://localhost:8080/api/v1/projects/1/validate' \
--header 'Authorization: Bearer {{SUPERUSER_JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{"isValid": true}'
```

### Step 3: Check Notifications (as Client)
```bash
# Get notifications with CLIENT token
curl --location 'http://localhost:8080/api/notifications' \
--header 'Authorization: Bearer {{CLIENT_JWT_TOKEN}}'

# You should see a "Project Approved" notification!
```

### Step 4: Check Email Logs
Look for email logs in the console:
```
INFO  c.r.u.service.EmailService - Project validation email sent successfully to: client@example.com
```

---

## Import to Postman

1. Copy any of the cURL commands above
2. In Postman, click "Import" → "Raw text"
3. Paste the cURL command
4. Postman will automatically create the request
5. Set up environment variables:
   - `{{JWT_TOKEN}}` - Your JWT token
   - `{{NOTIFICATION_ID}}` - Notification ID from response

---

## Notes

- **No Delete Endpoint**: Notifications are kept for historical purposes
- **Automatic Timestamps**: `createdAt` and `readAt` are automatically managed
- **Email Failures Don't Block**: If email fails, dashboard notification is still created
- **Reference System**: Notifications link to related entities via `referenceType` and `referenceId`
- **Future Extensions**: More notification types can be easily added (BID_RECEIVED, etc.)
