export interface Borrower {
  id: string
  creationDate: string
  modificationDate: string
  name: string
  email: string | null
  phone: string | null
}

export interface Loan {
  id: string
  creationDate: string
  modificationDate: string
  userBookId: string
  bookTitle: string
  borrower: Borrower
  loanDate: string
  dueDate: string | null
  returnedDate: string | null
}

export interface CreateBorrower {
  name: string
  email?: string | null
  phone?: string | null
}

export interface CreateLoan {
  userBookId: string
  borrowerId: string
  loanDate?: string | null
  dueDate?: string | null
}

export interface UpdateBorrower {
  name?: string | null
  email?: string | null
  phone?: string | null
}
