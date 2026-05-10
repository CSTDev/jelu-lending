package io.github.bayang.jelu.dao

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object LoanTable : UUIDTable("loan") {
    val creationDate = timestamp("creation_date")
    val modificationDate = timestamp("modification_date")
    val userBook = reference("user_book_id", UserBookTable, onDelete = ReferenceOption.CASCADE)
    val borrower = reference("borrower_id", BorrowerTable, onDelete = ReferenceOption.CASCADE)
    val loanDate = timestamp("loan_date")
    val dueDate = timestamp("due_date").nullable()
    val returnedDate = timestamp("returned_date").nullable()
}

class Loan(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Loan>(LoanTable)

    var creationDate by LoanTable.creationDate
    var modificationDate by LoanTable.modificationDate
    var userBook by UserBook referencedOn LoanTable.userBook
    var borrower by Borrower referencedOn LoanTable.borrower
    var loanDate by LoanTable.loanDate
    var dueDate by LoanTable.dueDate
    var returnedDate by LoanTable.returnedDate
}
