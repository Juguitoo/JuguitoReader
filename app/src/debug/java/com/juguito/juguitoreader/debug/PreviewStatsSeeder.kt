package com.juguito.juguitoreader.debug

import android.util.Log
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.repository.BookRepository
import com.juguito.juguitoreader.domain.repository.DailyReadingRepository
import com.juguito.juguitoreader.domain.repository.GenreRepository
import com.juguito.juguitoreader.utils.utcMidnightMillis
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class PreviewStatsSeeder @Inject constructor(
    private val bookRepository: BookRepository,
    private val genreRepository: GenreRepository,
    private val dailyReadingRepository: DailyReadingRepository,
) : AppInitializer {

    override suspend fun onAppStart() {
        val books = bookRepository.getAllBooks().first()
        if (books.any { it.comment == MARKER }) return

        val today = LocalDate.now()
        val fantasy = genreId("Fantasía")
        val historical = genreId("Ficción histórica")
        val science = genreId("Ciencia ficción")

        val readingId = addBook(
            title = "El nombre del viento",
            author = "Patrick Rothfuss",
            status = BookStatus.READING,
            genreId = fantasy,
            endDate = null,
        )
        addBook(
            title = "El hobbit",
            author = "J. R. R. Tolkien",
            status = BookStatus.FINISHED,
            rating = 5f,
            genreId = fantasy,
            endDate = today.minusMonths(1).withDayOfMonth(8),
        )
        addBook(
            title = "Aprendiz de asesino",
            author = "Robin Hobb",
            status = BookStatus.FINISHED,
            rating = 4.5f,
            genreId = fantasy,
            endDate = today.minusMonths(1).withDayOfMonth(22),
        )
        addBook(
            title = "Los pilares de la Tierra",
            author = "Ken Follett",
            status = BookStatus.FINISHED,
            rating = 4.8f,
            genreId = historical,
            endDate = dateInCurrentMonth(today, 4),
        )
        addBook(
            title = "Sidi",
            author = "Arturo Pérez-Reverte",
            status = BookStatus.FINISHED,
            rating = 4.2f,
            genreId = historical,
            endDate = dateInCurrentMonth(today, 12),
        )
        addBook(
            title = "El archivo de las tormentas",
            author = "Brandon Sanderson",
            status = BookStatus.FINISHED,
            rating = 4.9f,
            genreId = fantasy,
            endDate = dateInCurrentMonth(today, today.dayOfMonth),
        )
        addBook(
            title = "Fundación",
            author = "Isaac Asimov",
            status = BookStatus.FINISHED,
            rating = 3.5f,
            genreId = science,
            endDate = dateInCurrentMonth(today, (today.dayOfMonth - 1).coerceAtLeast(1)),
            physical = true,
        )
        seedSessions(readingId, today)
        Log.i(TAG, "Biblioteca de prueba cargada para estadísticas")
    }

    private suspend fun seedSessions(bookId: Int, today: LocalDate) {
        for (offset in 0..13) {
            val minutes = if (offset == 0) 105 else 30 + (offset * 5 % 45)
            saveSession(
                bookId = bookId,
                day = today.minusDays(offset.toLong()),
                minutes = minutes,
                speed = 250 + (offset % 6) * 12,
            )
        }
        for (offset in 15..42) {
            saveSession(
                bookId = bookId,
                day = today.minusDays(offset.toLong()),
                minutes = 20 + (offset % 5) * 8,
                speed = 230 + (offset % 4) * 10,
            )
        }
    }

    private suspend fun saveSession(bookId: Int, day: LocalDate, minutes: Int, speed: Int) {
        dailyReadingRepository.saveDailyReading(
            DailyReading(
                bookId = bookId,
                date = day.toString(),
                timeSpentMillis = minutes * 60_000,
                reachedPercentage = 35f,
                readingSpeed = speed,
            )
        )
    }

    private suspend fun addBook(
        title: String,
        author: String,
        status: BookStatus,
        genreId: Int,
        endDate: LocalDate?,
        rating: Float = 0f,
        physical: Boolean = false,
    ): Int {
        val endMillis = endDate?.let { utcMidnightMillis(it) }
        val startMillis = endDate?.minusDays(20)?.let { utcMidnightMillis(it) }
        return bookRepository.insertBookWithCrossRefs(
            Book(
                title = title,
                author = author,
                isPhysical = physical,
                status = status,
                rating = rating,
                comment = MARKER,
                startDate = startMillis,
                endDate = endMillis,
            ),
            folderIds = emptyList(),
            genreIds = listOf(genreId),
        ).toInt()
    }

    private suspend fun genreId(name: String): Int {
        genreRepository.getGenreByName(name)?.let { return it.id }
        return genreRepository.insertGenre(Genre(name = name)).toInt()
    }

    private fun dateInCurrentMonth(today: LocalDate, day: Int): LocalDate {
        return today.withDayOfMonth(day.coerceIn(1, today.dayOfMonth))
    }

    private companion object {
        const val TAG = "PreviewStats"
        const val MARKER = "preview-stats"
    }
}
