package com.juguito.juguitoreader.domain.model

class Genre(
    val id: Int = 0,
    val name: String,
    val bookCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

fun Genre.copy(
    name: String = this.name,
    bookCount: Int = this.bookCount,
    createdAt: Long = this.createdAt
): Genre {
    return Genre(
        id = this.id,
        name = name,
        bookCount = bookCount,
        createdAt = createdAt
    )
}
