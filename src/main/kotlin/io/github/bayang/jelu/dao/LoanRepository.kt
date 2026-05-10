package io.github.bayang.jelu.dao

import io.github.bayang.jelu.dto.CreateLoanDto
import io.github.bayang.jelu.dto.LoanDto
import io.github.bayang.jelu.utils.nowInstant
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class LoanRepository(
    private val borrowerRepository: BorrowerRepository,
) {

    fun save(dto: CreateLoanDto, userBook: UserBook, borrower: Borrower): Loan {
        val now = nowInstant()
        return Loan.new {
            creationDate = now
            modificationDate = now
            this.userBook = userBook
            this.borrower = borrower
            loanDate = dto.loanDate ?: now
            dueDate = dto.dueDate
            returnedDate = null
        }
    }

    fun findById(id: UUID): Loan? = Loan.findById(id)

    fun hasOpenLoan(userBookId: UUID): Boolean =
        Loan.find { (LoanTable.userBook eq userBookId) and LoanTable.returnedDate.isNull() }.count() > 0

    fun findOpen(): List<Loan> =
        Loan.find { LoanTable.returnedDate.isNull() }.toList()

    fun findByUserBook(userBookId: UUID): List<Loan> =
        Loan.find { LoanTable.userBook eq userBookId }.toList()

    fun findByBorrower(borrowerId: UUID): List<Loan> =
        Loan.find { LoanTable.borrower eq borrowerId }.toList()

    fun markReturned(entity: Loan): Loan {
        val now = nowInstant()
        entity.returnedDate = now
        entity.modificationDate = now
        return entity
    }

    fun delete(entity: Loan) = entity.delete()

    fun toLoanDto(entity: Loan) = LoanDto(
        id = entity.id.value,
        creationDate = entity.creationDate,
        modificationDate = entity.modificationDate,
        userBookId = entity.userBook.id.value,
        bookTitle = entity.userBook.book.title,
        borrower = borrowerRepository.toBorrowerDto(entity.borrower),
        loanDate = entity.loanDate,
        dueDate = entity.dueDate,
        returnedDate = entity.returnedDate,
    )
}
