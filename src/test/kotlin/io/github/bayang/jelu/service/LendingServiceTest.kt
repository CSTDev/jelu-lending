package io.github.bayang.jelu.service

import io.github.bayang.jelu.bookDto
import io.github.bayang.jelu.createUserBookDto
import io.github.bayang.jelu.dto.CreateBorrowerDto
import io.github.bayang.jelu.dto.CreateLoanDto
import io.github.bayang.jelu.dto.CreateUserDto
import io.github.bayang.jelu.dto.JeluUser
import io.github.bayang.jelu.dto.UpdateBorrowerDto
import io.github.bayang.jelu.dto.UserDto
import io.github.bayang.jelu.errors.JeluNotFoundException
import io.github.bayang.jelu.errors.JeluValidationException
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import java.util.UUID

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LendingServiceTest(
    @Autowired private val lendingService: LendingService,
    @Autowired private val bookService: BookService,
    @Autowired private val userService: UserService,
) {
    @BeforeAll
    fun setup() {
        userService.save(CreateUserDto(login = "lendtest", password = "1234", isAdmin = true))
    }

    @AfterAll
    fun tearDown() {
        runCatching {
            lendingService.listOpenLoans().forEach { runCatching { lendingService.returnLoan(it.id) } }
        }
        runCatching {
            lendingService.listBorrowers().forEach { runCatching { lendingService.deleteBorrower(it.id) } }
        }
        runCatching {
            val userId =
                (userService.loadUserByUsername("lendtest") as? JeluUser)?.user?.id
                    ?: return@runCatching
            bookService
                .findUserBookByCriteria(userId, null, null, null, null, null, Pageable.ofSize(50))
                .forEach { runCatching { bookService.deleteUserBookById(it.id!!) } }
        }
        userService
            .findAll(null)
            .filter { it.login == "lendtest" }
            .forEach { runCatching { userService.deleteUser(it.id!!) } }
    }

    private fun user(): UserDto = (userService.loadUserByUsername("lendtest") as JeluUser).user

    @Test
    fun testBorrowerLifeCycle() {
        val created = lendingService.createBorrower(CreateBorrowerDto(name = "Alice", email = "alice@example.com"))
        assertNotNull(created.id)
        assertEquals("Alice", created.name)
        assertEquals("alice@example.com", created.email)

        val fetched = lendingService.getBorrower(created.id)
        assertEquals(created.id, fetched.id)

        val updated = lendingService.updateBorrower(created.id, UpdateBorrowerDto(name = "Alice B"))
        assertEquals("Alice B", updated.name)

        val list = lendingService.listBorrowers("Alice")
        assert(list.any { it.id == created.id })

        lendingService.deleteBorrower(created.id)
        assertThrows<JeluNotFoundException> { lendingService.getBorrower(created.id) }
    }

    @Test
    fun testLoanLifeCycle() {
        val borrower = lendingService.createBorrower(CreateBorrowerDto(name = "Bob"))
        val userBookDto = bookService.save(createUserBookDto(bookDto("Loan Test Book")), user(), null)
        val userBookId = userBookDto.id!!

        val loan = lendingService.createLoan(CreateLoanDto(userBookId = userBookId, borrowerId = borrower.id))
        assertNotNull(loan.id)
        assertNull(loan.returnedDate)
        assertEquals(borrower.id, loan.borrower.id)
        assertEquals("Loan Test Book", loan.bookTitle)

        val openLoans = lendingService.listOpenLoans()
        assert(openLoans.any { it.id == loan.id })

        val returned = lendingService.returnLoan(loan.id)
        assertNotNull(returned.returnedDate)

        val byUserBook = lendingService.listLoansByUserBook(userBookId)
        assertEquals(1, byUserBook.size)
        assertNotNull(byUserBook[0].returnedDate)
    }

    @Test
    fun testCannotLoanAlreadyLentBook() {
        val borrower1 = lendingService.createBorrower(CreateBorrowerDto(name = "Charlie"))
        val borrower2 = lendingService.createBorrower(CreateBorrowerDto(name = "Dave"))
        val userBookDto = bookService.save(createUserBookDto(bookDto("Double Loan Book")), user(), null)

        lendingService.createLoan(CreateLoanDto(userBookId = userBookDto.id!!, borrowerId = borrower1.id))
        assertThrows<JeluValidationException> {
            lendingService.createLoan(CreateLoanDto(userBookId = userBookDto.id!!, borrowerId = borrower2.id))
        }
    }

    @Test
    fun testCannotReturnAlreadyReturnedLoan() {
        val borrower = lendingService.createBorrower(CreateBorrowerDto(name = "Eve"))
        val userBookDto = bookService.save(createUserBookDto(bookDto("Return Test Book")), user(), null)
        val loan =
            lendingService.createLoan(
                CreateLoanDto(userBookId = userBookDto.id!!, borrowerId = borrower.id),
            )
        lendingService.returnLoan(loan.id)
        assertThrows<JeluValidationException> { lendingService.returnLoan(loan.id) }
    }

    @Test
    fun testCannotDeleteBorrowerWithOpenLoan() {
        val borrower = lendingService.createBorrower(CreateBorrowerDto(name = "Frank"))
        val userBookDto = bookService.save(createUserBookDto(bookDto("Delete Borrower Book")), user(), null)
        lendingService.createLoan(CreateLoanDto(userBookId = userBookDto.id!!, borrowerId = borrower.id))
        assertThrows<JeluValidationException> { lendingService.deleteBorrower(borrower.id) }
    }

    @Test
    fun testListLoansByBorrower() {
        val borrower = lendingService.createBorrower(CreateBorrowerDto(name = "Grace"))
        val userBook1 = bookService.save(createUserBookDto(bookDto("Book By Borrower 1")), user(), null)
        val userBook2 = bookService.save(createUserBookDto(bookDto("Book By Borrower 2")), user(), null)
        lendingService.createLoan(CreateLoanDto(userBookId = userBook1.id!!, borrowerId = borrower.id))
        val loan2 = lendingService.createLoan(CreateLoanDto(userBookId = userBook2.id!!, borrowerId = borrower.id))
        lendingService.returnLoan(loan2.id)

        val loans = lendingService.listLoansByBorrower(borrower.id)
        assertEquals(2, loans.size)
    }

    @Test
    fun testGetNonExistentLoan() {
        assertThrows<JeluNotFoundException> { lendingService.getLoan(UUID.randomUUID()) }
    }
}
