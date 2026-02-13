<template>
  <div
    v-if="show"
    class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50"
    @click.self="$emit('cancel')"
  >
    <div class="bg-white rounded-lg shadow-xl max-w-lg w-full mx-4">
      <!-- Header -->
      <div class="px-6 py-4 border-b">
        <h3 class="text-lg font-semibold text-gray-900">
          {{ action === 'approve' ? 'Approve Project' : 'Reject Project' }}
        </h3>
      </div>

      <!-- Content -->
      <div class="px-6 py-4">
        <p class="text-sm text-gray-600 mb-4">
          Project: <span class="font-medium text-gray-900">{{ projectTitle }}</span>
        </p>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">
            {{ action === 'approve' ? 'Approval Notes (Optional)' : 'Rejection Notes (Required)' }}
          </label>
          <textarea
            :value="notes"
            @input="$emit('update:notes', $event.target.value)"
            rows="4"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            :placeholder="
              action === 'approve'
                ? 'Add any notes about this approval...'
                : 'Provide detailed feedback on why this project is being rejected...'
            "
          ></textarea>

          <!-- Character counter for reject -->
          <div v-if="action === 'reject'" class="mt-1 text-sm">
            <span :class="notes.trim().length >= 10 ? 'text-green-600' : 'text-red-600'">
              {{ notes.trim().length }} / 10 minimum characters
            </span>
          </div>
        </div>

        <!-- Warning for reject -->
        <div v-if="action === 'reject'" class="bg-yellow-50 border border-yellow-200 rounded-md p-3 mb-4">
          <p class="text-sm text-yellow-800">
            <strong>Warning:</strong> Rejecting this project will prevent it from being shown to architects.
            Please provide clear feedback for the client.
          </p>
        </div>

        <!-- Info for approve -->
        <div v-else class="bg-green-50 border border-green-200 rounded-md p-3 mb-4">
          <p class="text-sm text-green-800">
            Approving this project will set its status to OPEN and make it available for architects to bid.
            Bidding deadline will be set to 2 weeks from now.
          </p>
        </div>
      </div>

      <!-- Footer -->
      <div class="px-6 py-4 border-t flex justify-end space-x-3">
        <button
          @click="$emit('cancel')"
          class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
        >
          Cancel
        </button>
        <button
          @click="$emit('confirm')"
          :class="[
            'px-4 py-2 text-sm font-medium text-white rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2',
            action === 'approve'
              ? 'bg-green-600 hover:bg-green-700 focus:ring-green-500'
              : 'bg-red-600 hover:bg-red-700 focus:ring-red-500'
          ]"
        >
          {{ action === 'approve' ? 'Approve Project' : 'Reject Project' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ValidationModal',
  props: {
    show: {
      type: Boolean,
      required: true
    },
    projectTitle: {
      type: String,
      required: true
    },
    action: {
      type: String,
      required: true,
      validator: value => ['approve', 'reject'].includes(value)
    },
    notes: {
      type: String,
      default: ''
    }
  },
  emits: ['confirm', 'cancel', 'update:notes']
}
</script>
