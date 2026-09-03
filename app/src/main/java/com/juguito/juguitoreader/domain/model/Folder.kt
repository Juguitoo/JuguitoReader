package com.juguito.juguitoreader.domain.model

class Folder(
    val id: Int = 0,
    val name: String,
    val colorHex: String,
    val description: String? = null,
    val bookCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

fun Folder.copy(
    name: String = this.name,
    colorHex: String = this.colorHex,
    description: String? = this.description,
    bookCount: Int = this.bookCount,
    createdAt: Long = this.createdAt
): Folder {
    return Folder(
        id = this.id,
        name = name,
        colorHex = colorHex,
        description = description,
        bookCount = bookCount,
        createdAt = createdAt
    )
}
