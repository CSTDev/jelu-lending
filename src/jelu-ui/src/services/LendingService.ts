import axios from 'axios'
import type { Borrower, CreateBorrower, CreateLoan, Loan, UpdateBorrower } from '../model/Lending'
import urls from '../urls'

const API = urls.API_URL

export default {
  async createBorrower(dto: CreateBorrower): Promise<Borrower> {
    const { data } = await axios.post<Borrower>(`${API}/borrowers`, dto, { withCredentials: true })
    return data
  },

  async getBorrower(id: string): Promise<Borrower> {
    const { data } = await axios.get<Borrower>(`${API}/borrowers/${id}`, { withCredentials: true })
    return data
  },

  async listBorrowers(name?: string): Promise<Borrower[]> {
    const { data } = await axios.get<Borrower[]>(`${API}/borrowers`, {
      params: name ? { name } : undefined,
      withCredentials: true,
    })
    return data
  },

  async updateBorrower(id: string, dto: UpdateBorrower): Promise<Borrower> {
    const { data } = await axios.put<Borrower>(`${API}/borrowers/${id}`, dto, { withCredentials: true })
    return data
  },

  async deleteBorrower(id: string): Promise<void> {
    await axios.delete(`${API}/borrowers/${id}`, { withCredentials: true })
  },

  async createLoan(dto: CreateLoan): Promise<Loan> {
    const { data } = await axios.post<Loan>(`${API}/loans`, dto, { withCredentials: true })
    return data
  },

  async listOpenLoans(): Promise<Loan[]> {
    const { data } = await axios.get<Loan[]>(`${API}/loans`, { withCredentials: true })
    return data
  },

  async listLoansByUserBook(userBookId: string): Promise<Loan[]> {
    const { data } = await axios.get<Loan[]>(`${API}/loans`, {
      params: { userBookId },
      withCredentials: true,
    })
    return data
  },

  async returnLoan(id: string): Promise<Loan> {
    const { data } = await axios.post<Loan>(`${API}/loans/${id}/return`, null, { withCredentials: true })
    return data
  },

  async deleteLoan(id: string): Promise<void> {
    await axios.delete(`${API}/loans/${id}`, { withCredentials: true })
  },
}
