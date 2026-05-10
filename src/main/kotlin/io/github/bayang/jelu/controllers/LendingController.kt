package io.github.bayang.jelu.controllers

import io.github.bayang.jelu.dto.BorrowerDto
import io.github.bayang.jelu.dto.CreateBorrowerDto
import io.github.bayang.jelu.dto.CreateLoanDto
import io.github.bayang.jelu.dto.LoanDto
import io.github.bayang.jelu.dto.UpdateBorrowerDto
import io.github.bayang.jelu.service.LendingService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class LendingController(private val lendingService: LendingService) {

    @PostMapping("/borrowers")
    @ResponseStatus(HttpStatus.CREATED)
    fun createBorrower(@RequestBody dto: CreateBorrowerDto): BorrowerDto =
        lendingService.createBorrower(dto)

    @GetMapping("/borrowers/{id}")
    fun getBorrower(@PathVariable id: UUID): BorrowerDto =
        lendingService.getBorrower(id)

    @GetMapping("/borrowers")
    fun listBorrowers(@RequestParam(required = false) name: String?): List<BorrowerDto> =
        lendingService.listBorrowers(name)

    @PutMapping("/borrowers/{id}")
    fun updateBorrower(@PathVariable id: UUID, @RequestBody dto: UpdateBorrowerDto): BorrowerDto =
        lendingService.updateBorrower(id, dto)

    @DeleteMapping("/borrowers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteBorrower(@PathVariable id: UUID) =
        lendingService.deleteBorrower(id)

    @PostMapping("/loans")
    @ResponseStatus(HttpStatus.CREATED)
    fun createLoan(@RequestBody dto: CreateLoanDto): LoanDto =
        lendingService.createLoan(dto)

    @GetMapping("/loans/{id}")
    fun getLoan(@PathVariable id: UUID): LoanDto =
        lendingService.getLoan(id)

    @GetMapping("/loans")
    fun listLoans(
        @RequestParam(required = false) userBookId: UUID?,
        @RequestParam(required = false) borrowerId: UUID?,
    ): List<LoanDto> = when {
        userBookId != null -> lendingService.listLoansByUserBook(userBookId)
        borrowerId != null -> lendingService.listLoansByBorrower(borrowerId)
        else -> lendingService.listOpenLoans()
    }

    @PostMapping("/loans/{id}/return")
    fun returnLoan(@PathVariable id: UUID): LoanDto =
        lendingService.returnLoan(id)

    @DeleteMapping("/loans/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteLoan(@PathVariable id: UUID) =
        lendingService.deleteLoan(id)
}
