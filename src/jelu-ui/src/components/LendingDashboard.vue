<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { Loan } from '../model/Lending'
import lendingService from '../services/LendingService'

const loans = ref<Loan[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

async function fetchLoans() {
  loading.value = true
  error.value = null
  try {
    loans.value = await lendingService.listOpenLoans()
  } catch {
    error.value = 'Failed to load loans'
  } finally {
    loading.value = false
  }
}

async function markReturned(loanId: string) {
  try {
    await lendingService.returnLoan(loanId)
    await fetchLoans()
  } catch {
    error.value = 'Failed to mark as returned'
  }
}

function isOverdue(loan: Loan): boolean {
  if (!loan.dueDate) return false
  return new Date(loan.dueDate) < new Date()
}

function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString()
}

onMounted(fetchLoans)
</script>

<template>
  <div class="container mx-auto px-4 py-6">
    <h1 class="text-2xl font-bold mb-6 capitalize">
      Currently Lent Out
    </h1>

    <div v-if="loading" class="flex justify-center py-8">
      <span class="loading loading-spinner loading-lg" />
    </div>

    <div v-else-if="error" class="alert alert-error">
      {{ error }}
    </div>

    <div v-else-if="loans.length === 0" class="text-center text-base-content/60 py-12">
      No books are currently lent out.
    </div>

    <div v-else class="overflow-x-auto">
      <table class="table table-zebra w-full">
        <thead>
          <tr>
            <th>Book</th>
            <th>Borrower</th>
            <th>Lent On</th>
            <th>Due Date</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="loan in loans"
            :key="loan.id"
            :class="{ 'bg-error/10': isOverdue(loan) }"
          >
            <td>
              <router-link
                :to="{ name: 'book-detail', params: { bookId: loan.userBookId } }"
                class="link link-hover font-medium"
              >
                {{ loan.bookTitle }}
              </router-link>
            </td>
            <td>{{ loan.borrower.name }}</td>
            <td>{{ formatDate(loan.loanDate) }}</td>
            <td :class="{ 'text-error font-semibold': isOverdue(loan) }">
              {{ formatDate(loan.dueDate) }}
              <span v-if="isOverdue(loan)" class="badge badge-error badge-sm ml-1">Overdue</span>
            </td>
            <td>
              <button
                class="btn btn-sm btn-outline btn-success"
                @click="markReturned(loan.id)"
              >
                Return
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
