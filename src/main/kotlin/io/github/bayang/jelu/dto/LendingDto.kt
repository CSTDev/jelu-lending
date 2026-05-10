package io.github.bayang.jelu.dto

import java.time.Instant
import java.util.UUID

data class BorrowerDto(
    val id: UUID,
    val creationDate: Instant,
    val modificationDate: Instant,
    val name: String,
    val email: String?,
    val phone: String?,
)

data class CreateBorrowerDto(
    val name: String,
    val email: String? = null,
    val phone: String? = null,
)

data class UpdateBorrowerDto(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
)

data class LoanDto(
    val id: UUID,
    val creationDate: Instant,
    val modificationDate: Instant,
    val userBookId: UUID,
    val bookTitle: String,
    val borrower: BorrowerDto,
    val loanDate: Instant,
    val dueDate: Instant?,
    val returnedDate: Instant?,
)

data class CreateLoanDto(
    val userBookId: UUID,
    val borrowerId: UUID,
    val loanDate: Instant? = null,
    val dueDate: Instant? = null,
)
