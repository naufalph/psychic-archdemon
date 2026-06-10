# Chat System API - cURL Commands for Postman/Bruno

## Prerequisites
- Replace `{{JWT_TOKEN}}` with your actual JWT token (architect or client)
- Replace `{{CONVERSATION_ID}}` with the conversation ID from response
- Replace `{{MESSAGE_ID}}` with the message ID from response
- Replace file paths with actual files on your system

---

## Conversation Types

There are two distinct conversation types. They share the same endpoints but have different participant structures:

| Type | How Created | Participants | `requesterUserId` |
|------|-------------|--------------|-------------------|
| **PROJECT** | Auto-created when a bid is accepted | Architect + Client | `null` |
| **SUPPORT** | Manually via `POST /rmtr/support/conversations` | Requester (arch/client) + SUPERUSER | set to requester's userId |

PROJECT conversations have `architectId` and `clientId` populated.
SUPPORT conversations have `requesterUserId` set; `architectId` and `clientId` are `null`.

---

## Access Control

All `/rmtr/chat/**` endpoints require an authenticated user. Access is then enforced at the service layer:

| Role | What they can access |
|------|----------------------|
| **SUPERUSER** | Bypasses all ownership checks — can access any conversation |
| **PROJECT conversation** | Only the architect or client participant |
| **SUPPORT conversation** | Only the `requesterUserId` who created it |

---

## General Chat Flow for Frontend Implementation

### PROJECT Chat Flow (Bid-Based)

```
1. CLIENT ACCEPTS BID (POST /rmtr/bids/{bidId}/accept)
   ↓
2. BACKEND CREATES PROJECT CONVERSATION AUTOMATICALLY
   - BidAcceptedEvent published → ChatEventListener creates conversation
   - Status: ACTIVE, itSupportRequested: false
   ↓
3. ARCHITECT/CLIENT CONNECTS TO WEBSOCKET
   - Connect to ws://localhost:8080/ws
   - Authenticate with JWT token
   - Subscribe to /topic/conversation.{conversationId}
   ↓
4. FETCH CONVERSATIONS (GET /rmtr/chat/conversations)
   - Role-filtered: ARCHITECT sees their project chats, CLIENT sees theirs
   - Sorted by lastMessageAt DESC
   ↓
5. OPEN CONVERSATION (GET /rmtr/chat/conversations/{id})
   - Load conversation details
   - Fetch message history (API returns newest-first; reverse for display)
   - Subscribe to real-time updates
   ↓
6. SEND MESSAGE
   - Option A: WebSocket (Real-time) → /app/chat.send
   - Option B: REST API (Fallback) → POST /rmtr/chat/messages
   ↓
7. UPLOAD FILE (POST /rmtr/chat/conversations/{id}/upload)
   - Max 20MB, any file type
   ↓
8. ESCALATE TO SUPPORT (POST /rmtr/support/conversations)
   - Flags existing PROJECT conversation with itSupportRequested = true
   - Publishes SupportRequestedEvent
   - SUPERUSER can now join the conversation
   ↓
9. MARK AS READ
   - WebSocket → /app/chat.read
   - REST API → PUT /rmtr/chat/messages/{id}/read
```

### SUPPORT Chat Flow (Superuser)

```
1. SUPERUSER LISTS ESCALATED CONVERSATIONS (GET /rmtr/support/conversations)
   - Returns all PROJECT conversations where itSupportRequested = true
   ↓
2. SUPERUSER OPENS CONVERSATION (GET /rmtr/chat/conversations/{id})
   - SUPERUSER role bypasses all ownership checks
   ↓
3. SUPERUSER REPLIES VIA NORMAL CHAT ENDPOINTS
   - senderType = SUPERUSER in messages
```

### Business Rules

1. **Conversation Creation**: Automatic when bid is accepted (no manual creation for PROJECT)
2. **Support Escalation**: `POST /rmtr/support/conversations` is idempotent — safe to call multiple times
3. **Participants**: PROJECT = 1 Architect + 1 Client; SUPPORT = 1 Requester + SUPERUSER(s)
4. **Message Types**: TEXT or FILE (IMAGE enum exists but is not yet used)
5. **File Uploads**: Any file type up to 20MB
6. **Read Receipts**: Senders cannot mark their own messages as read
7. **Message Order**: API returns messages newest-first (`DESC`); frontend should reverse for chronological display
8. **Archive**: Either party can archive conversation (changes status to ARCHIVED)

---

## API Endpoints - REST

---

### 1. Get All Conversations

**GET** `/rmtr/chat/conversations`

Role-filtered:
- **SUPERUSER**: Returns all PROJECT conversations with `itSupportRequested = true`
- **ARCHITECT + CLIENT** (dual role): Returns conversations where user is architect OR client
- **ARCHITECT**: Conversations where user is architect participant
- **CLIENT**: Conversations where user is client participant

All results sorted by `lastMessageAt DESC`.

```bash
curl --location 'http://localhost:8080/rmtr/chat/conversations' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "projectId": 456,
      "bidId": 123,
      "architectId": 789,
      "architectName": "John Doe",
      "clientId": 101,
      "clientName": "Jane Smith",
      "requesterUserId": null,
      "requesterName": null,
      "itSupportRequested": false,
      "status": "ACTIVE",
      "unreadCount": 3,
      "lastMessage": {
        "id": 25,
        "conversationId": 1,
        "senderUserId": 101,
        "senderName": "Jane Smith",
        "senderType": "CLIENT",
        "content": "When can we schedule the site visit?",
        "messageType": "TEXT",
        "isRead": false,
        "readAt": null,
        "file": null,
        "createdAt": "2026-01-04T14:30:00"
      },
      "lastMessageAt": "2026-01-04T14:30:00",
      "createdAt": "2026-01-04T10:00:00"
    }
  ],
  "timestamp": "2026-01-04T15:00:00"
}
```

**Frontend Implementation Tip:**
- Show unread badge if `unreadCount > 0`
- Show participant name based on user role:
  - If user is architect → show `clientName`
  - If user is client → show `architectName`
  - If SUPPORT conversation → show `requesterName` (for superuser) or show "Support" label

---

### 2. Get Conversation by ID

**GET** `/rmtr/chat/conversations/{{CONVERSATION_ID}}`

```bash
curl --location 'http://localhost:8080/rmtr/chat/conversations/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "projectId": 456,
    "bidId": 123,
    "architectId": 789,
    "architectName": "John Doe",
    "clientId": 101,
    "clientName": "Jane Smith",
    "requesterUserId": null,
    "requesterName": null,
    "itSupportRequested": false,
    "status": "ACTIVE",
    "unreadCount": 3,
    "lastMessage": { "...": "..." },
    "lastMessageAt": "2026-01-04T14:30:00",
    "createdAt": "2026-01-04T10:00:00"
  },
  "timestamp": "2026-01-04T15:00:00"
}
```

**Authorization:** Returns 403 if current user is not a participant (see Access Control section above).

---

### 3. Get Message History

**GET** `/rmtr/chat/conversations/{{CONVERSATION_ID}}/messages?page=0&size=50`

```bash
curl --location 'http://localhost:8080/rmtr/chat/conversations/1/messages?page=0&size=50' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Query Parameters:**
- `page` (optional): Page number, defaults to 0
- `size` (optional): Messages per page, defaults to 50

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "messages": [
      {
        "id": 3,
        "conversationId": 1,
        "senderUserId": 789,
        "senderName": "John Doe",
        "senderType": "ARCHITECT",
        "content": "Here's the project timeline document.",
        "messageType": "FILE",
        "isRead": true,
        "readAt": "2026-01-04T10:25:00",
        "file": {
          "id": 1,
          "fileName": "Project_Timeline_2026.pdf",
          "fileUrl": "https://storage.example.com/chat/1/files/a1b2c3d4-timeline.pdf",
          "fileType": "application/pdf",
          "fileSize": 524288
        },
        "createdAt": "2026-01-04T10:20:00"
      },
      {
        "id": 1,
        "conversationId": 1,
        "senderUserId": 789,
        "senderName": "John Doe",
        "senderType": "ARCHITECT",
        "content": "Thank you for accepting my bid!",
        "messageType": "TEXT",
        "isRead": true,
        "readAt": "2026-01-04T10:05:00",
        "file": null,
        "createdAt": "2026-01-04T10:01:00"
      }
    ],
    "currentPage": 0,
    "totalPages": 1,
    "totalMessages": 3,
    "hasMore": false
  },
  "timestamp": "2026-01-04T15:00:00"
}
```

**Important:** Messages are returned **newest-first** (`ORDER BY createdAt DESC`). Reverse the array before rendering for chronological display.

**Frontend Implementation Tip:**
- Infinite scroll: Load previous messages with `page + 1`
- Group messages by date
- Different bubble style for own vs other's messages

---

### 4. Send Message (REST Fallback)

**POST** `/rmtr/chat/messages`

```bash
curl --location 'http://localhost:8080/rmtr/chat/messages' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "conversationId": 1,
  "content": "I will review the timeline and get back to you by tomorrow."
}'
```

**Request Body:**
- `conversationId` (Long, required)
- `content` (String, required, cannot be blank)

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 26,
    "conversationId": 1,
    "senderUserId": 101,
    "senderName": "Jane Smith",
    "senderType": "CLIENT",
    "content": "I will review the timeline and get back to you by tomorrow.",
    "messageType": "TEXT",
    "isRead": false,
    "readAt": null,
    "file": null,
    "createdAt": "2026-01-04T15:05:00"
  },
  "timestamp": "2026-01-04T15:05:00"
}
```

**Frontend Implementation Tip:**
- Prefer WebSocket for real-time messaging; use REST as fallback when WebSocket disconnected
- Optimistic UI: Show message immediately, update when confirmed

---

### 5. Upload File

**POST** `/rmtr/chat/conversations/{{CONVERSATION_ID}}/upload`

```bash
curl --location 'http://localhost:8080/rmtr/chat/conversations/1/upload' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--form 'file=@"/home/user/Documents/Contract_Draft.pdf"'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 27,
    "conversationId": 1,
    "senderUserId": 789,
    "senderName": "John Doe",
    "senderType": "ARCHITECT",
    "content": "Contract_Draft.pdf",
    "messageType": "FILE",
    "isRead": false,
    "readAt": null,
    "file": {
      "id": 2,
      "fileName": "Contract_Draft.pdf",
      "fileUrl": "https://storage.example.com/chat/1/files/x9y8z7w6-contract.pdf",
      "fileType": "application/pdf",
      "fileSize": 1048576
    },
    "createdAt": "2026-01-04T15:10:00"
  },
  "timestamp": "2026-01-04T15:10:00"
}
```

**Supported File Types:** Any (validated by size only)
**File Size Limit:** 20MB
**Storage Path:** `chat/{conversationId}/files/`

---

### 6. Mark Message as Read

**PUT** `/rmtr/chat/messages/{{MESSAGE_ID}}/read`

```bash
curl --location --request PUT 'http://localhost:8080/rmtr/chat/messages/25/read' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": null,
  "timestamp": "2026-01-04T15:15:00"
}
```

**Business Logic:**
- Guard: Senders cannot mark their own messages as read
- Sets `isRead = true` and `readAt` timestamp
- Broadcasts messageId to `/topic/conversation.{id}.read`
- Idempotent: safe to call multiple times

---

### 7. Mark All Messages as Read

**PUT** `/rmtr/chat/conversations/{{CONVERSATION_ID}}/read-all`

```bash
curl --location --request PUT 'http://localhost:8080/rmtr/chat/conversations/1/read-all' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": null,
  "timestamp": "2026-01-04T15:20:00"
}
```

**Business Logic:**
- Marks all messages where `senderUserId != currentUserId` as read
- Broadcasts `userId` to `/topic/conversation.{id}.read-all`

---

### 8. Archive Conversation

**PUT** `/rmtr/chat/conversations/{{CONVERSATION_ID}}/archive`

```bash
curl --location --request PUT 'http://localhost:8080/rmtr/chat/conversations/1/archive' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": null,
  "timestamp": "2026-01-04T15:25:00"
}
```

**Business Logic:**
- Changes conversation `status` → `ARCHIVED`

---

### 9. Request Support (Escalate Conversation)

**POST** `/rmtr/support/conversations`

Flags an existing PROJECT conversation for SUPERUSER support. Idempotent.

**Required Role:** `ARCHITECT` or `CLIENT`

```bash
curl --location 'http://localhost:8080/rmtr/support/conversations' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "projectId": 456,
  "bidId": 123
}'
```

**Request Body:**
- `projectId` (Long, required)
- `bidId` (Long, required)

**Expected Response (200 OK):** Returns the `ConversationResponse` for the flagged conversation.

**Business Logic:**
- Finds the PROJECT conversation by `bidId`
- If `itSupportRequested` is already `true`: returns existing conversation (idempotent)
- If `false`: sets `itSupportRequested = true`, records `itSupportRequestedAt`, publishes `SupportRequestedEvent`

---

### 10. List All Support Conversations (Superuser)

**GET** `/rmtr/support/conversations`

Returns all PROJECT conversations where `itSupportRequested = true`, sorted by `lastMessageAt DESC`.

**Required Role:** `SUPERUSER`

```bash
curl --location 'http://localhost:8080/rmtr/support/conversations' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):** `ApiResponse<List<ConversationResponse>>`

---

## WebSocket Implementation

---

### 1. Connect to WebSocket

**Endpoint:** `ws://localhost:8080/ws` (or `wss://` for production)

**JavaScript Example (using @stomp/stompjs):**

```javascript
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const socket = new SockJS('http://localhost:8080/ws')

const stompClient = new Client({
  webSocketFactory: () => socket,
  connectHeaders: {
    Authorization: `Bearer ${jwtToken}`
  },
  debug: (str) => {
    console.log('[WebSocket]', str)
  },
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000,
  onConnect: (frame) => {
    console.log('Connected to WebSocket:', frame)

    stompClient.subscribe('/topic/conversation.1', (message) => {
      const newMessage = JSON.parse(message.body)
      // Update UI with new message
    })

    stompClient.subscribe('/topic/conversation.1.read', (message) => {
      const messageId = JSON.parse(message.body)
      // Update UI to show message as read
    })

    stompClient.subscribe('/topic/conversation.1.read-all', (message) => {
      const userId = JSON.parse(message.body)
      // Mark all messages from this user as read in UI
    })
  },
  onStompError: (frame) => {
    console.error('WebSocket error:', frame)
  }
})

stompClient.activate()
```

**Allowed Origins:** `http://localhost:3000`, `http://localhost:3001`, `http://*:3001`

**Authentication:** JWT token sent in `Authorization` header during CONNECT frame. Connection rejected if token invalid/expired.

---

### 2. Send Message via WebSocket

**Destination:** `/app/chat.send`

```javascript
stompClient.publish({
  destination: '/app/chat.send',
  body: JSON.stringify({
    conversationId: 1,
    content: 'Hello! This is a real-time message.'
  })
})
```

**Message Flow:**
1. Client sends to `/app/chat.send`
2. Backend saves to database
3. Backend broadcasts to `/topic/conversation.{conversationId}`
4. All subscribers (including sender) receive it

---

### 3. Mark as Read via WebSocket

**Destination:** `/app/chat.read`

```javascript
stompClient.publish({
  destination: '/app/chat.read',
  body: JSON.stringify(25) // messageId (Long)
})
```

---

### 4. Disconnect WebSocket

```javascript
stompClient.deactivate()
```

---

## DTO Reference

### ConversationResponse

```
id:               Long
projectId:        Long
bidId:            Long                    — null for support conversations
architectId:      Long                    — null for support conversations
architectName:    String                  — null for support conversations
clientId:         Long                    — null for support conversations
clientName:       String                  — null for support conversations
requesterUserId:  Long                    — null for PROJECT conversations; set for SUPPORT
requesterName:    String                  — null for PROJECT conversations; set for SUPPORT
itSupportRequested: Boolean               — true when escalated to superuser support
status:           ConversationStatus      — ACTIVE | ARCHIVED | CLOSED
unreadCount:      Integer
lastMessage:      MessageResponse
lastMessageAt:    LocalDateTime
createdAt:        LocalDateTime
```

### MessageResponse

```
id:           Long
conversationId: Long
senderUserId: Long
senderName:   String
senderType:   SenderType     — ARCHITECT | CLIENT | SUPERUSER
content:      String         — message text, or filename for FILE messages
messageType:  MessageType    — TEXT | FILE | IMAGE (IMAGE defined but unused)
isRead:       Boolean
readAt:       LocalDateTime
file:         MessageFileResponse   — null for TEXT messages
createdAt:    LocalDateTime
```

### MessageFileResponse

```
id:        Long
fileName:  String
fileUrl:   String
fileType:  String    — MIME type (e.g. "application/pdf", "image/png")
fileSize:  Long      — size in bytes
```

### MessageHistoryResponse

```
messages:      List<MessageResponse>   — ordered newest-first (DESC); reverse for display
currentPage:   Integer
totalPages:    Integer
totalMessages: Long
hasMore:       Boolean
```

### SendMessageRequest

```
conversationId: Long    (required)
content:        String  (required, non-blank)
```

### SupportConversationRequest

```
projectId: Long  (required)
bidId:     Long  (required)
```

---

## Enums

| Enum | Values |
|------|--------|
| `ConversationStatus` | `ACTIVE`, `ARCHIVED`, `CLOSED` |
| `MessageType` | `TEXT`, `FILE`, `IMAGE` *(IMAGE defined but not used)* |
| `SenderType` | `ARCHITECT`, `CLIENT`, `SUPERUSER` |

---

## WebSocket Topics Reference

| Topic | Payload | Description |
|-------|---------|-------------|
| `/topic/conversation.{id}` | `MessageResponse` | New message broadcast |
| `/topic/conversation.{id}.read` | `Long` (messageId) | Single message marked as read |
| `/topic/conversation.{id}.read-all` | `Long` (userId) | All messages marked as read by user |

---

## Testing Sequence for Postman/Bruno

1. **Login as Client** → Get JWT token
2. **Accept Bid** → `POST /rmtr/bids/{bidId}/accept`
3. **Verify Conversation Created** → `GET /rmtr/chat/conversations`
4. **Open Conversation** → `GET /rmtr/chat/conversations/{id}`
5. **Get Message History** → `GET /rmtr/chat/conversations/{id}/messages`
6. **Send Message (REST)** → `POST /rmtr/chat/messages`
7. **Upload File** → `POST /rmtr/chat/conversations/{id}/upload`
8. **Mark as Read** → `PUT /rmtr/chat/messages/{id}/read`
9. **Mark All as Read** → `PUT /rmtr/chat/conversations/{id}/read-all`
10. **Request Support** → `POST /rmtr/support/conversations`
11. **Login as Superuser** → Get superuser JWT token
12. **List Support Conversations** → `GET /rmtr/support/conversations`
13. **Archive Conversation** → `PUT /rmtr/chat/conversations/{id}/archive`
14. **Connect WebSocket** → Use Postman WebSocket or browser console
15. **Send Message (WebSocket)** → `/app/chat.send`
16. **Mark as Read (WebSocket)** → `/app/chat.read`

---

## Error Responses

```json
{
  "success": false,
  "message": "Conversation not found",
  "timestamp": "2026-01-04T15:30:00"
}
```

| Status | Reason |
|--------|--------|
| `401 Unauthorized` | Invalid or expired JWT token |
| `403 Forbidden` | User is not a participant in this conversation |
| `404 Not Found` | Conversation or message not found |
| `400 Bad Request` | Validation error (empty content, file too large) |

---

## Database Schema Reference

| Table | Description |
|-------|-------------|
| `rmtr_conversation` | Conversations (PROJECT and SUPPORT types) |
| `rmtr_message` | Chat messages (TEXT or FILE) |
| `rmtr_message_file` | File metadata for FILE messages |

**Key columns on `rmtr_conversation`:**
- `architect_id`, `client_id` — populated for PROJECT conversations, null for SUPPORT
- `requester_user_id` — populated for SUPPORT conversations, null for PROJECT
- `it_support_requested` — boolean flag set when `POST /rmtr/support/conversations` is called
- `it_support_requested_at` — timestamp of escalation

**Relationships:**
- 1 Bid → 1 Conversation (PROJECT, created on bid acceptance)
- 1 Conversation → Many Messages
- 1 Message → 0-1 MessageFile (if `messageType = FILE`)
