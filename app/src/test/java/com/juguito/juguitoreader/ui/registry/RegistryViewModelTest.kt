package com.juguito.juguitoreader.ui.registry

import android.app.Application
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.BookCriteria
import com.juguito.juguitoreader.domain.usecase.book.GetBooksUseCase
import com.juguito.juguitoreader.domain.usecase.book.UpdateBookUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: RegistryViewModel
    private val application = mockk<Application>(relaxed = true)
    private val getBooksUseCase = mockk<GetBooksUseCase>()
    private val updateBookUseCase = mockk<UpdateBookUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every {
            application.getString(R.string.something_went_wrong, *emptyArray())
        } returns "Algo ha salido mal"
        every { getBooksUseCase() } returns flowOf(emptyList())
        viewModel = RegistryViewModel(application, getBooksUseCase, updateBookUseCase)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onEvent update field events call updateBookUseCase`() = runTest {
        val book = Book(id = 1, title = "T", author = "A", isPhysical = false)
        every { getBooksUseCase() } returns flowOf(listOf(book))
        viewModel = RegistryViewModel(application, getBooksUseCase, updateBookUseCase)
        coEvery { updateBookUseCase(any()) } returns Result.success(Unit)

        viewModel.onEvent(RegistryEvent.OnRatingChanged(1, 5f))
        coVerify { updateBookUseCase(match { it.id == 1 && it.rating == 5f }) }

        viewModel.onEvent(RegistryEvent.OnStatusChanged(1, BookStatus.FINISHED))
        coVerify { updateBookUseCase(match { it.id == 1 && it.status == BookStatus.FINISHED }) }

        viewModel.onEvent(RegistryEvent.OnStartDateChanged(1, 100L))
        coVerify { updateBookUseCase(match { it.id == 1 && it.startDate == 100L }) }

        viewModel.onEvent(RegistryEvent.OnEndDateChanged(1, 200L))
        coVerify { updateBookUseCase(match { it.id == 1 && it.endDate == 200L }) }

        viewModel.onEvent(RegistryEvent.OnCommentChanged(1, "Comm"))
        coVerify { updateBookUseCase(match { it.id == 1 && it.comment == "Comm" }) }
    }

    @Test
    fun `onEvent filtering events update criteria and state`() = runTest {
        viewModel.onEvent(RegistryEvent.OnSearchTextChanged("Search"))
        assertThat(viewModel.uiState.value.criteria.searchText).isEqualTo("Search")

        viewModel.onEvent(RegistryEvent.OnToggleSearch(true))
        assertThat(viewModel.uiState.value.isSearchExpanded).isTrue()

        viewModel.onEvent(RegistryEvent.OnShowFilterSheet(true))
        assertThat(viewModel.uiState.value.showFilterSheet).isTrue()

        val newCriteria = BookCriteria(series = "Harry Potter")
        viewModel.onEvent(RegistryEvent.OnCriteriaChanged(newCriteria))
        assertThat(viewModel.uiState.value.criteria.series).isEqualTo("Harry Potter")

        viewModel.onEvent(RegistryEvent.OnClearFilters)
        assertThat(viewModel.uiState.value.criteria.series).isNull()
    }

    @Test
    fun `loadData catch sets errorMessage and stops loading`() = runTest {
        every { getBooksUseCase() } returns flow { throw RuntimeException("db down") }
        viewModel = RegistryViewModel(application, getBooksUseCase, updateBookUseCase)

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.errorMessage).isEqualTo("Algo ha salido mal")
        assertThat(state.books).isEmpty()
    }

    @Test
    fun `onEvent OnDismissError reloads books and clears error`() = runTest {
        every { getBooksUseCase() } returns flow { throw RuntimeException("db down") }
        viewModel = RegistryViewModel(application, getBooksUseCase, updateBookUseCase)
        assertThat(viewModel.uiState.value.errorMessage).isEqualTo("Algo ha salido mal")

        val book = Book(id = 1, title = "1984", author = "Orwell", isPhysical = true)
        every { getBooksUseCase() } returns flowOf(listOf(book))

        viewModel.onEvent(RegistryEvent.OnDismissError)

        val state = viewModel.uiState.value
        assertThat(state.errorMessage).isNull()
        assertThat(state.isLoading).isFalse()
        assertThat(state.books).containsExactly(book)
    }
}
