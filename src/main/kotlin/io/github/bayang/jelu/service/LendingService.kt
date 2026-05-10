package io.github.bayang.jelu.service

import io.github.bayang.jelu.dao.BorrowerRepository
import io.github.bayang.jelu.dao.LoanRepository
import io.github.bayang.jelu.dao.UserBook
import io.github.bayang.jelu.dto.BorrowerDto
import io.github.bayang.jelu.dto.CreateBorrowerDto
import io.github.bayang.jelu.dto.CreateLoanDto
import io.github.bayang.jelu.dto.LoanDto
import io.github.bayang.jelu.dto.UpdateBorrowerDto
import io.github.bayang.jelu.errors.JeluNotFoundException
import io.github.bayang.jelu.errors.JeluValidationException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class LendingService(
    private val borrowerRepository: BorrowerRepository,
    private val loanRepository: LoanRepository,
) {

    @Transactional
    fun createBorrower(dto: CreateBorrowerDto): BorrowerDto =
        borrowerRepository.toBorrowerDto(borrowerRepository.save(dto))

    @Transactional(readOnly = true)
    fun getBorrower(id: UUID): BorrowerDto =
        borrowerRepository.findById(id)?.let { borrowerRepository.toBorrowerDto(it) }
            ?: throw JeluNotFoundException("Borrower $id not found")

    @Transactional(readOnly = true)
    fun listBorrowers(nameFilter: String? = null): List<BorrowerDto> =
        borrowerRepository.findAll(nameFilter).map { borrowerRepository.toBorrowerDto(it) }

    @Transactional
    fun updateBorrower(id: UUID, dto: UpdateBorrowerDto): BorrowerDto {
        val entity = borrowerRepository.findById(id)
            ?: throw JeluNotFoundException("Borrower $id not found")
        return borrowerRepository.toBorrowerDto(borrowerRepository.update(entity, dto))
    }

    @Transactional
    fun deleteBorrower(id: UUID) {
        val entity = borrowerRepository.findById(id)
            ?: throw JeluNotFoundException("Borrower $id not found")
        if (borrowerRepository.hasOpenLoans(id)) {
            throw JeluValidationException("Cannot delete borrower with open loans")
        }
        borrowerRepository.delete(entity)
    }

    @Transactional
    fun createLoan(dto: CreateLoanDto): LoanDto {
        val userBook = UserBook.findById(dto.userBookId)
            ?: throw JeluNotFoundException("UserBook ${dto.userBookId} not found")
        val borrower = borrowerRepository.findById(dto.borrowerId)
            ?: throw JeluNotFoundException("Borrower ${dto.borrowerId} not found")
        if (loanRepository.hasOpenLoan(dto.userBookId)) {
            throw JeluValidationException("Book already has an open loan")
        }
        return loanRepository.toLoanDto(loanRepository.save(dto, userBook, borrower))
    }

    @Transactional(readOnly = true)
    fun getLoan(id: UUID): LoanDto =
        loanRepository.findById(id)?.let { loanRepository.toLoanDto(it) }
            ?: throw JeluNotFoundException("Loan $id not found")

    @Transactional(readOnly = true)
    fun listOpenLoans(): List<LoanDto> =
        loanRepository.findOpen().map { loanRepository.toLoanDto(it) }

    @Transactional(readOnly = true)
    fun listLoansByUserBook(userBookId: UUID): List<LoanDto> =
        loanRepository.findByUserBook(userBookId).map { loanRepository.toLoanDto(it) }

    @Transactional(readOnly = true)
    fun listLoansByBorrower(borrowerId: UUID): List<LoanDto> =
        loanRepository.findByBorrower(borrowerId).map { loanRepository.toLoanDto(it) }

    @Transactional
    fun returnLoan(id: UUID): LoanDto {
        val entity = loanRepository.findById(id)
            ?: throw JeluNotFoundException("Loan $id not found")
        if (entity.returnedDate != null) {
            throw JeluValidationException("Loan $id has already been returned")
        }
        return loanRepository.toLoanDto(loanRepository.markReturned(entity))
    }

    @Transactional
    fun deleteLoan(id: UUID) {
        val entity = loanRepository.findById(id)
            ?: throw JeluNotFoundException("Loan $id not found")
        loanRepository.delete(entity)
    }
}
