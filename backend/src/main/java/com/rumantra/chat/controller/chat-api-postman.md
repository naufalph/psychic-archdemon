# Chat System API - cURL Commands for Postman/Bruno

## Prerequisites
- Replace `{{JWT_TOKEN}}` with your actual JWT token (architect or client)
- Replace `{{CONVERSATION_ID}}` with the conversation ID from response
- Replace `{{MESSAGE_ID}}` with the message ID from response
- Replace file paths with actual files on your system

---

## General Chat Flow for Frontend Implementation

### Complete Chat Flow

```
1. CLIENT ACCEPTS BID (POST /api/bids/{bidId}/accept)
   ↓
2. BACKEND CREATES CONVERSATION AUTOMATICALLY
   - BidAcceptedEvent published
   - ChatEventListener creates conversation
   - Status: ACTIVE
   ↓
3. ARCHITECT/CLIENT CONNECTS TO WEBSOCKET
   - Connect to ws://localhost:8080/ws
   - Authenticate with JWT token
   - Subscribe to /topic/conversation.{conversationId}
   ↓
4. FETCH CONVERSATIONS (GET /api/chat/conversations)
   - List all conversations for current user
   - Shows last message, unread count
   ↓
5. OPEN CONVERSATION (GET /api/chat/conversations/{id})
   - Load conversation details
   - Fetch message history
   - Subscribe to real-time updates
   ↓
6. SEND MESSAGE
   - Option A: WebSocket (Real-time)
     → Send to /app/chat.send
     → Receive on /topic/conversation.{id}
   - Option B: REST API (Fallback)
     → POST /api/chat/messages
   ↓
7. UPLOAD FILE (POST /api/chat/conversations/{id}/upload)
   - Supports any file type (PDF, DOCX, images, etc.)
   - Max 20MB
   - File stored in Cloudinary/local storage
   ↓
8. MARK AS READ
   - Option A: WebSocket → Send to /app/chat.read
   - Option B: REST API → PUT /api/chat/messages/{id}/read
   ↓
9. REAL-TIME UPDATES
   - New messages appear instantly via WebSocket
   - Read receipts broadcast to all participants
   - Last message timestamp updated
```

### Business Rules

1. **Conversation Creation**: Automatic when bid is accepted (no manual creation)
2. **Participants**: 1 Architect + 1 Client per conversation
3. **Message Types**: TEXT (string) or FILE (uploaded document/image)
4. **File Uploads**: Any file type up to 20MB
5. **Read Receipts**: Messages marked as read when opened by recipient
6. **Ownership**: Both architect and client can access their shared conversation
7. **Archive**: Either party can archive conversation (soft delete)

---

## API Endpoints - REST

---

### 1. Get All Conversations

**GET** `/api/chat/conversations`

Retrieves all conversations for the authenticated user (architect or client).

```bash
curl --location 'http://localhost:8080/api/chat/conversations' \
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
    },
    {
      "id": 2,
      "projectId": 789,
      "bidId": 234,
      "architectId": 789,
      "architectName": "John Doe",
      "clientId": 202,
      "clientName": "Bob Johnson",
      "status": "ACTIVE",
      "unreadCount": 0,
      "lastMessage": {
        "id": 42,
        "conversationId": 2,
        "senderUserId": 1050,
        "senderName": "John Doe",
        "senderType": "ARCHITECT",
        "content": "I've uploaded the revised floor plans.",
        "messageType": "TEXT",
        "isRead": true,
        "readAt": "2026-01-04T11:15:00",
        "file": null,
        "createdAt": "2026-01-04T11:00:00"
      },
      "lastMessageAt": "2026-01-04T11:00:00",
      "createdAt": "2026-01-03T09:00:00"
    }
  ],
  "timestamp": "2026-01-04T15:00:00"
}
```

**Frontend Implementation Tip:**
- Display conversations ordered by `lastMessageAt` (most recent first)
- Show unread badge if `unreadCount > 0`
- Preview last message content
- Show participant name based on user role:
  - If user is architect → show `clientName`
  - If user is client → show `architectName`

---

### 2. Get Conversation by ID

**GET** `/api/chat/conversations/{{CONVERSATION_ID}}`

Retrieves a specific conversation with details.

```bash
curl --location 'http://localhost:8080/api/chat/conversations/1' \
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
  },
  "timestamp": "2026-01-04T15:00:00"
}
```

**Authorization:**
- Only accessible by conversation participants (architect or client)
- Returns 403 if unauthorized

---

### 3. Get Message History

**GET** `/api/chat/conversations/{{CONVERSATION_ID}}/messages?page=0&size=50`

Retrieves paginated message history for a conversation.

```bash
curl --location 'http://localhost:8080/api/chat/conversations/1/messages?page=0&size=50' \
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
        "id": 1,
        "conversationId": 1,
        "senderUserId": 789,
        "senderName": "John Doe",
        "senderType": "ARCHITECT",
        "content": "Thank you for accepting my bid! I'm excited to work on this project.",
        "messageType": "TEXT",
        "isRead": true,
        "readAt": "2026-01-04T10:05:00",
        "file": null,
        "createdAt": "2026-01-04T10:01:00"
      },
      {
        "id": 2,
        "conversationId": 1,
        "senderUserId": 101,
        "senderName": "Jane Smith",
        "senderType": "CLIENT",
        "content": "Great! Can you share the preliminary timeline?",
        "messageType": "TEXT",
        "isRead": true,
        "readAt": "2026-01-04T10:12:00",
        "file": null,
        "createdAt": "2026-01-04T10:10:00"
      },
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
          "fileUrl": "https://res.cloudinary.com/yourcloud/chat/1/files/a1b2c3d4-timeline.pdf",
          "fileSize": 524288
        },
        "createdAt": "2026-01-04T10:20:00"
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

**Frontend Implementation Tip:**
- Messages ordered by `createdAt` (oldest first for display)
- Infinite scroll: Load previous messages with `page + 1`
- Show typing indicator while loading
- Group messages by date
- Different bubble style for own vs other's messages

---

### 4. Send Message (REST Fallback)

**POST** `/api/chat/messages`

Sends a text message via REST API (use WebSocket for real-time).

```bash
curl --location 'http://localhost:8080/api/chat/messages' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "conversationId": 1,
  "content": "I will review the timeline and get back to you by tomorrow."
}'
```

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

**Validation:**
- `conversationId` is required
- `content` cannot be empty
- User must be participant in conversation

**Frontend Implementation Tip:**
- Prefer WebSocket for real-time messaging
- Use REST API as fallback when WebSocket disconnected
- Show "Sending..." indicator
- Optimistic UI: Show message immediately, update when confirmed

---

### 5. Upload File

**POST** `/api/chat/conversations/{{CONVERSATION_ID}}/upload`

Uploads a file (any type up to 20MB) and sends as FILE message.

```bash
curl --location 'http://localhost:8080/api/chat/conversations/1/upload' \
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
      "fileUrl": "https://res.cloudinary.com/yourcloud/chat/1/files/x9y8z7w6-contract.pdf",
      "fileSize": 1048576
    },
    "createdAt": "2026-01-04T15:10:00"
  },
  "timestamp": "2026-01-04T15:10:00"
}
```

**Supported File Types:**
- Documents: PDF, DOCX, XLSX, PPTX, TXT
- Images: JPG, PNG, GIF, WebP
- Archives: ZIP, RAR
- Any file type (validated by size only)

**File Size Limit:** 20MB

**Frontend Implementation Tip:**
- Show file upload progress bar
- Preview images inline in chat
- Show file icon + name for documents
- Download button for all file types
- Format file size (e.g., "1.5 MB")

---

### 6. Mark Message as Read

**PUT** `/api/chat/messages/{{MESSAGE_ID}}/read`

Marks a single message as read.

```bash
curl --location --request PUT 'http://localhost:8080/api/chat/messages/25/read' \
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
- Only marks as read if current user is NOT the sender
- Updates `isRead` to `true` and sets `readAt` timestamp
- Broadcasts read receipt via WebSocket to `/topic/conversation.{id}.read`
- Idempotent: Safe to call multiple times

**Frontend Implementation Tip:**
- Call when message enters viewport (Intersection Observer)
- Batch read receipts (mark multiple at once)
- Show double checkmark (✓✓) for read messages

---

### 7. Mark All Messages as Read

**PUT** `/api/chat/conversations/{{CONVERSATION_ID}}/read-all`

Marks all unread messages in a conversation as read.

```bash
curl --location --request PUT 'http://localhost:8080/api/chat/conversations/1/read-all' \
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
- Updates conversation's `unreadCount` to 0
- Broadcasts to `/topic/conversation.{id}.read-all`

**Frontend Implementation Tip:**
- Call when user opens conversation
- Call when app regains focus
- Update unread badge to 0

---

### 8. Archive Conversation

**PUT** `/api/chat/conversations/{{CONVERSATION_ID}}/archive`

Archives a conversation (soft delete).

```bash
curl --location --request PUT 'http://localhost:8080/api/chat/conversations/1/archive' \
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
- Changes conversation `status` from `ACTIVE` to `ARCHIVED`
- Conversation still accessible but hidden from main list
- Can be unarchived (future feature)

**Frontend Implementation Tip:**
- Show "Archive" in conversation menu
- Move to "Archived" tab
- Confirmation dialog before archiving

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

    // Subscribe to conversation
    stompClient.subscribe('/topic/conversation.1', (message) => {
      const newMessage = JSON.parse(message.body)
      console.log('New message:', newMessage)
      // Update UI with new message
    })

    // Subscribe to read receipts
    stompClient.subscribe('/topic/conversation.1.read', (message) => {
      const messageId = JSON.parse(message.body)
      console.log('Message read:', messageId)
      // Update UI to show message as read
    })

    // Subscribe to read-all receipts
    stompClient.subscribe('/topic/conversation.1.read-all', (message) => {
      const userId = JSON.parse(message.body)
      console.log('All messages read by user:', userId)
      // Mark all messages from this user as read in UI
    })
  },
  onStompError: (frame) => {
    console.error('WebSocket error:', frame)
  }
})

stompClient.activate()
```

**Authentication:**
- JWT token sent in `Authorization` header during CONNECT frame
- Backend validates token via `JwtAuthenticationFilter`
- Connection rejected if token invalid/expired

---

### 2. Send Message via WebSocket

**Destination:** `/app/chat.send`

**JavaScript Example:**

```javascript
// Send text message
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
2. Backend receives in `WebSocketChatController.sendMessage()`
3. Backend saves to database via `MessageService`
4. Backend broadcasts to `/topic/conversation.1`
5. All subscribers receive message instantly

**Response:**
- No direct response to sender
- Sender receives their own message via `/topic/conversation.1` broadcast
- Confirms message was sent successfully

---

### 3. Mark as Read via WebSocket

**Destination:** `/app/chat.read`

**JavaScript Example:**

```javascript
// Mark message as read
stompClient.publish({
  destination: '/app/chat.read',
  body: JSON.stringify(25) // messageId
})
```

**Message Flow:**
1. Client sends messageId to `/app/chat.read`
2. Backend marks message as read
3. Backend broadcasts messageId to `/topic/conversation.{id}.read`
4. Other participant sees double checkmark

---

### 4. Disconnect WebSocket

**JavaScript Example:**

```javascript
stompClient.deactivate()
console.log('Disconnected from WebSocket')
```

**Frontend Implementation Tip:**
- Disconnect when user logs out
- Disconnect when navigating away from chat
- Auto-reconnect on connection loss (built into STOMP client)

---

## Frontend Implementation Guidelines

---

### 1. Chat Page Layout

**Components:**
- `ConversationList` (left sidebar)
- `MessageList` (center panel)
- `MessageInput` (bottom of center panel)
- `ChatHeader` (top of center panel)

**ConversationList Component:**
```vue
<template>
  <div class="conversation-list">
    <div
      v-for="conv in conversations"
      :key="conv.id"
      @click="openConversation(conv.id)"
      :class="{ active: activeConversation?.id === conv.id }"
      class="conversation-item"
    >
      <div class="avatar">{{ conv.otherPartyName[0] }}</div>
      <div class="details">
        <h4>{{ conv.otherPartyName }}</h4>
        <p class="last-message">{{ conv.lastMessage?.content }}</p>
        <span class="time">{{ formatTime(conv.lastMessageAt) }}</span>
      </div>
      <span v-if="conv.unreadCount > 0" class="badge">
        {{ conv.unreadCount }}
      </span>
    </div>
  </div>
</template>
```

**MessageList Component:**
```vue
<template>
  <div class="message-list" ref="messageContainer">
    <div
      v-for="msg in messages"
      :key="msg.id"
      :class="['message', msg.senderUserId === currentUserId ? 'sent' : 'received']"
    >
      <div class="bubble">
        <p v-if="msg.messageType === 'TEXT'">{{ msg.content }}</p>
        <div v-else-if="msg.messageType === 'FILE'" class="file-message">
          <a :href="msg.file.fileUrl" target="_blank" download>
            <FileIcon />
            <span>{{ msg.file.fileName }}</span>
            <span class="size">{{ formatFileSize(msg.file.fileSize) }}</span>
          </a>
        </div>
      </div>
      <span class="time">{{ formatTime(msg.createdAt) }}</span>
      <span v-if="msg.isRead" class="read-receipt">✓✓</span>
    </div>
  </div>
</template>
```

**MessageInput Component:**
```vue
<template>
  <div class="message-input">
    <input
      type="file"
      ref="fileInput"
      @change="handleFileUpload"
      style="display: none"
    />
    <button @click="$refs.fileInput.click()">
      <AttachIcon />
    </button>
    <input
      v-model="newMessage"
      @keyup.enter="sendMessage"
      placeholder="Type a message..."
    />
    <button @click="sendMessage" :disabled="!newMessage.trim()">
      <SendIcon />
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useChatStore } from '@/stores/chat'

const chatStore = useChatStore()
const newMessage = ref('')
const fileInput = ref(null)

const sendMessage = () => {
  if (!newMessage.value.trim()) return
  chatStore.sendMessage(newMessage.value)
  newMessage.value = ''
}

const handleFileUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    chatStore.uploadFile(file)
  }
}
</script>
```

---

### 2. Pinia Store (State Management)

**File:** `frontend/src/stores/chat.js`

```javascript
import { defineStore } from 'pinia'
import { chatAPI } from '@/services/api'
import websocketService from '@/services/websocket'
import { useAuthStore } from './auth'

export const useChatStore = defineStore('chat', {
  state: () => ({
    conversations: [],
    activeConversation: null,
    messages: [],
    loading: false,
    wsConnected: false
  }),

  actions: {
    async connectWebSocket() {
      const authStore = useAuthStore()
      try {
        await websocketService.connect(authStore.token)
        this.wsConnected = true
      } catch (error) {
        console.error('Failed to connect WebSocket:', error)
      }
    },

    async fetchConversations() {
      this.loading = true
      try {
        const response = await chatAPI.getConversations()
        this.conversations = response.data.data
      } catch (error) {
        console.error('Failed to fetch conversations:', error)
      } finally {
        this.loading = false
      }
    },

    async openConversation(conversationId) {
      this.loading = true
      try {
        const convResponse = await chatAPI.getConversation(conversationId)
        this.activeConversation = convResponse.data.data

        const msgResponse = await chatAPI.getMessages(conversationId)
        this.messages = msgResponse.data.data.messages

        if (this.wsConnected) {
          websocketService.subscribeToConversation(conversationId, (message) => {
            this.messages.push(message)
            this.updateConversationLastMessage(conversationId, message)
          })
        }

        await chatAPI.markAllAsRead(conversationId)
      } catch (error) {
        console.error('Failed to open conversation:', error)
      } finally {
        this.loading = false
      }
    },

    async sendMessage(content) {
      if (!this.activeConversation) return

      try {
        if (this.wsConnected) {
          websocketService.sendMessage(this.activeConversation.id, content)
        } else {
          const response = await chatAPI.sendMessage(
            this.activeConversation.id,
            content
          )
          this.messages.push(response.data.data)
        }
      } catch (error) {
        console.error('Failed to send message:', error)
      }
    },

    async uploadFile(file) {
      if (!this.activeConversation) return

      try {
        const response = await chatAPI.uploadFile(this.activeConversation.id, file)
        this.messages.push(response.data.data)
      } catch (error) {
        console.error('Failed to upload file:', error)
      }
    },

    updateConversationLastMessage(conversationId, message) {
      const conv = this.conversations.find(c => c.id === conversationId)
      if (conv) {
        conv.lastMessage = message
        conv.lastMessageAt = message.createdAt
      }
    }
  }
})
```

---

### 3. API Service Configuration

**File:** `frontend/src/services/api.js`

```javascript
export const chatAPI = {
  getConversations: () => api.get('/api/chat/conversations'),

  getConversation: (conversationId) =>
    api.get(`/api/chat/conversations/${conversationId}`),

  getMessages: (conversationId, page = 0, size = 50) =>
    api.get(`/api/chat/conversations/${conversationId}/messages`, {
      params: { page, size }
    }),

  sendMessage: (conversationId, content) =>
    api.post('/api/chat/messages', { conversationId, content }),

  uploadFile: (conversationId, file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post(`/api/chat/conversations/${conversationId}/upload`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  markAsRead: (messageId) =>
    api.put(`/api/chat/messages/${messageId}/read`),

  markAllAsRead: (conversationId) =>
    api.put(`/api/chat/conversations/${conversationId}/read-all`),

  archiveConversation: (conversationId) =>
    api.put(`/api/chat/conversations/${conversationId}/archive`)
}
```

---

### 4. Real-Time Features

**Auto-Mark as Read (Intersection Observer):**

```javascript
const observer = new IntersectionObserver((entries) => {
  entries.forEach(entry => {
    if (entry.isIntersecting) {
      const messageId = entry.target.dataset.messageId
      chatStore.markAsRead(messageId)
    }
  })
}, { threshold: 0.5 })

// Observe each message element
messages.forEach(msgEl => observer.observe(msgEl))
```

**Auto-Scroll to Bottom:**

```javascript
import { nextTick, watch } from 'vue'

const messageContainer = ref(null)

watch(messages, async () => {
  await nextTick()
  messageContainer.value.scrollTop = messageContainer.value.scrollHeight
})
```

**Typing Indicator (Optional):**

```javascript
// Send typing event via WebSocket
websocketService.sendTyping(conversationId)

// Subscribe to typing events
websocketService.subscribeToTyping(conversationId, (userId) => {
  // Show "User is typing..." indicator
})
```

---

## Testing Sequence for Postman/Bruno

1. **Login as Client** → Get JWT token
2. **Accept Bid** → `POST /api/bids/{bidId}/accept`
3. **Verify Conversation Created** → `GET /api/chat/conversations`
4. **Open Conversation** → `GET /api/chat/conversations/{id}`
5. **Get Message History** → `GET /api/chat/conversations/{id}/messages`
6. **Send Message (REST)** → `POST /api/chat/messages`
7. **Upload File** → `POST /api/chat/conversations/{id}/upload`
8. **Mark as Read** → `PUT /api/chat/messages/{id}/read`
9. **Mark All as Read** → `PUT /api/chat/conversations/{id}/read-all`
10. **Archive Conversation** → `PUT /api/chat/conversations/{id}/archive`
11. **Connect WebSocket** → Use Postman WebSocket or browser console
12. **Send Message (WebSocket)** → `/app/chat.send`
13. **Mark as Read (WebSocket)** → `/app/chat.read`

---

## WebSocket Topics Reference

| Topic | Description | Payload |
|-------|-------------|---------|
| `/topic/conversation.{id}` | New messages in conversation | `MessageResponse` |
| `/topic/conversation.{id}.read` | Single message read receipt | `messageId` (Long) |
| `/topic/conversation.{id}.read-all` | All messages read by user | `userId` (Long) |

**Subscription Pattern:**
```javascript
// Subscribe to conversation messages
stompClient.subscribe('/topic/conversation.1', callback)

// Subscribe to read receipts
stompClient.subscribe('/topic/conversation.1.read', callback)

// Subscribe to read-all receipts
stompClient.subscribe('/topic/conversation.1.read-all', callback)
```

---

## Error Responses

All errors follow the standardized format:

```json
{
  "success": false,
  "message": "Conversation not found",
  "timestamp": "2026-01-04T15:30:00"
}
```

**Common Errors:**
- `403 Forbidden`: User not participant in conversation
- `404 Not Found`: Conversation/message not found
- `400 Bad Request`: Validation error (empty content, file too large)
- `401 Unauthorized`: Invalid/expired JWT token

---

## Database Schema Reference

- **rmtr_conversation**: Conversations between architect and client
- **rmtr_message**: Chat messages (TEXT or FILE type)
- **rmtr_message_file**: File metadata for FILE messages

**Relationships:**
- 1 Bid → 1 Conversation (created on bid acceptance)
- 1 Conversation → Many Messages
- 1 Message → 0-1 File (if messageType = FILE)
