<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { Loan } from '../model/Lending'
import lendingService from '../services/LendingService'

const props = defineProps<{
  userBookId: string
}>()

const loans = ref<Loan[]>([])
const open = ref(false)
const loading = ref(false)

async function fetchHistory() {
  loading.value = true
  try {
    loans.value = await lendingService.listLoansByUserBook(props.userBookId)
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString()
}

onMounted(fetchHistory)
</script>

<template>
  <div v-if="loans.length > 0" class="mt-4 w-full sm:w-10/12">
    <div
      class="collapse collapse-arrow border border-base-300 bg-base-100"
      :class="{ 'collapse-open': open }"
    >
      <div
        class="collapse-title text-lg font-semibold capitalize cursor-pointer"
        @click="open = !open"
      >
        Loan History ({{ loans.length }})
      </div>
      <div class="collapse-content">
        <table class="table table-sm w-full mt-2">
          <thead>
            <tr>
              <th>Borrower</th>
              <th>Lent On</th>
              <th>Due Date</th>
              <th>Returned</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="loan in loans" :key="loan.id">
              <td>{{ loan.borrower.name }}</td>
              <td>{{ formatDate(loan.loanDate) }}</td>
              <td>{{ formatDate(loan.dueDate) }}</td>
              <td>
                <span v-if="loan.returnedDate" class="badge badge-success badge-sm">
                  {{ formatDate(loan.returnedDate) }}
                </span>
                <span v-else class="badge badge-warning badge-sm">On loan</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
