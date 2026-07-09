<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import type { Borrower, Loan } from '../model/Lending'
import lendingService from '../services/LendingService'

const props = defineProps<{
  userBookId: string
  bookTitle: string
  open: boolean
}>()

const emit = defineEmits<{
  (e: 'update:open', val: boolean): void
  (e: 'loaned'): void
  (e: 'returned'): void
}>()

const borrowerSearch = ref('')
const borrowerResults = ref<Borrower[]>([])
const selectedBorrower = ref<Borrower | null>(null)
const newBorrowerName = ref('')
const dueDate = ref('')
const currentLoan = ref<Loan | null>(null)
const error = ref<string | null>(null)
const loading = ref(false)

async function fetchCurrentLoan() {
  try {
    const loans = await lendingService.listLoansByUserBook(props.userBookId)
    currentLoan.value = loans.find(l => !l.returnedDate) ?? null
  } catch {
    currentLoan.value = null
  }
}

async function searchBorrowers() {
  if (!borrowerSearch.value.trim()) {
    borrowerResults.value = []
    selectedBorrower.value = null
    return
  }
  try {
    borrowerResults.value = await lendingService.listBorrowers(borrowerSearch.value)
  } catch {
    borrowerResults.value = []
  }
}

function selectBorrower(b: Borrower) {
  selectedBorrower.value = b
  borrowerSearch.value = b.name
  borrowerResults.value = []
}

async function lendBook() {
  error.value = null
  loading.value = true
  try {
    let borrower = selectedBorrower.value
    if (!borrower && newBorrowerName.value.trim()) {
      borrower = await lendingService.createBorrower({ name: newBorrowerName.value.trim() })
    }
    if (!borrower) {
      error.value = 'Please select or enter a borrower name'
      loading.value = false
      return
    }
    await lendingService.createLoan({
      userBookId: props.userBookId,
      borrowerId: borrower.id,
      dueDate: dueDate.value ? new Date(dueDate.value).toISOString() : null,
    })
    close()
    emit('loaned')
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to create loan'
  } finally {
    loading.value = false
  }
}

async function returnBook() {
  if (!currentLoan.value) return
  error.value = null
  loading.value = true
  try {
    await lendingService.returnLoan(currentLoan.value.id)
    close()
    emit('returned')
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to return book'
  } finally {
    loading.value = false
  }
}

function close() {
  emit('update:open', false)
  borrowerSearch.value = ''
  borrowerResults.value = []
  selectedBorrower.value = null
  newBorrowerName.value = ''
  dueDate.value = ''
  error.value = null
}

watch(() => props.open, (val) => {
  if (val) fetchCurrentLoan()
})

onMounted(fetchCurrentLoan)
</script>

<template>
  <dialog
    class="modal"
    :class="{ 'modal-open': open }"
  >
    <div class="modal-box">
      <button
        class="btn btn-sm btn-circle btn-ghost absolute right-2 top-2"
        @click="close"
      >
        &#x2715;
      </button>
      <h3 class="font-bold text-lg mb-4">
        {{ currentLoan ? 'Book is on Loan' : 'Lend Book' }}
      </h3>
      <p class="text-sm text-base-content/70 mb-4">
        {{ bookTitle }}
      </p>

      <div v-if="currentLoan" class="space-y-3">
        <p>
          Currently lent to <strong>{{ currentLoan.borrower.name }}</strong>
          since {{ new Date(currentLoan.loanDate).toLocaleDateString() }}.
        </p>
        <p v-if="currentLoan.dueDate" class="text-sm">
          Due: {{ new Date(currentLoan.dueDate).toLocaleDateString() }}
        </p>
        <div v-if="error" class="alert alert-error text-sm py-2">
          {{ error }}
        </div>
        <div class="modal-action">
          <button
            class="btn btn-success"
            :disabled="loading"
            @click="returnBook"
          >
            Mark as Returned
          </button>
          <button class="btn btn-ghost" @click="close">
            Close
          </button>
        </div>
      </div>

      <div v-else class="space-y-3">
        <div class="form-control">
          <label class="label"><span class="label-text">Search Borrower</span></label>
          <input
            v-model="borrowerSearch"
            type="text"
            placeholder="Type a name…"
            class="input input-bordered"
            @input="searchBorrowers"
          >
          <ul v-if="borrowerResults.length > 0" class="menu bg-base-200 rounded-box mt-1 z-50 shadow">
            <li
              v-for="b in borrowerResults"
              :key="b.id"
            >
              <a @click="selectBorrower(b)">{{ b.name }}</a>
            </li>
          </ul>
        </div>

        <div v-if="!selectedBorrower" class="form-control">
          <label class="label"><span class="label-text">Or create new borrower</span></label>
          <input
            v-model="newBorrowerName"
            type="text"
            placeholder="New borrower name"
            class="input input-bordered"
          >
        </div>

        <div class="form-control">
          <label class="label"><span class="label-text">Due Date (optional)</span></label>
          <input
            v-model="dueDate"
            type="date"
            class="input input-bordered"
          >
        </div>

        <div v-if="error" class="alert alert-error text-sm py-2">
          {{ error }}
        </div>

        <div class="modal-action">
          <button
            class="btn btn-primary"
            :disabled="loading"
            @click="lendBook"
          >
            Lend
          </button>
          <button class="btn btn-ghost" @click="close">
            Cancel
          </button>
        </div>
      </div>
    </div>
    <form method="dialog" class="modal-backdrop" @click="close">
      <button>close</button>
    </form>
  </dialog>
</template>
