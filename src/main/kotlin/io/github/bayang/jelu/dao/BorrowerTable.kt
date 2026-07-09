package io.github.bayang.jelu.dao

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object BorrowerTable : UUIDTable("borrower") {
    val creationDate = timestamp("creation_date")
    val modificationDate = timestamp("modification_date")
    val name = varchar("name", 500)
    val email = varchar("email", 500).nullable()
    val phone = varchar("phone", 100).nullable()
}

class Borrower(
    id: EntityID<UUID>,
) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Borrower>(BorrowerTable)

    var creationDate by BorrowerTable.creationDate
    var modificationDate by BorrowerTable.modificationDate
    var name by BorrowerTable.name
    var email by BorrowerTable.email
    var phone by BorrowerTable.phone
}
