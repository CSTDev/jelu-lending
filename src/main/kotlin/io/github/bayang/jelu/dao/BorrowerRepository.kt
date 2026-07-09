package io.github.bayang.jelu.dao

import io.github.bayang.jelu.dto.BorrowerDto
import io.github.bayang.jelu.dto.CreateBorrowerDto
import io.github.bayang.jelu.dto.UpdateBorrowerDto
import io.github.bayang.jelu.utils.nowInstant
import org.jetbrains.exposed.sql.and
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class BorrowerRepository {
    fun save(dto: CreateBorrowerDto): Borrower {
        val now = nowInstant()
        return Borrower.new {
            creationDate = now
            modificationDate = now
            name = dto.name
            email = dto.email
            phone = dto.phone
        }
    }

    fun findById(id: UUID): Borrower? = Borrower.findById(id)

    fun findAll(nameFilter: String? = null): List<Borrower> =
        if (nameFilter.isNullOrBlank()) {
            Borrower.all().toList()
        } else {
            Borrower.find { BorrowerTable.name like "%$nameFilter%" }.toList()
        }

    fun update(
        entity: Borrower,
        dto: UpdateBorrowerDto,
    ): Borrower {
        dto.name?.let { entity.name = it }
        dto.email?.let { entity.email = it }
        dto.phone?.let { entity.phone = it }
        entity.modificationDate = nowInstant()
        return entity
    }

    fun delete(entity: Borrower) = entity.delete()

    fun hasOpenLoans(borrowerId: UUID): Boolean =
        Loan.find { (LoanTable.borrower eq borrowerId) and LoanTable.returnedDate.isNull() }.count() > 0

    fun toBorrowerDto(entity: Borrower) =
        BorrowerDto(
            id = entity.id.value,
            creationDate = entity.creationDate,
            modificationDate = entity.modificationDate,
            name = entity.name,
            email = entity.email,
            phone = entity.phone,
        )
}
