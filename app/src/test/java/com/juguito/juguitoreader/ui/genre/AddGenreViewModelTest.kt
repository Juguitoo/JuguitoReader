package com.juguito.juguitoreader.ui.genre

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.usecase.genre.AddGenreUseCase
import com.juguito.juguitoreader.ui.common.UiText
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddGenreViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: AddGenreViewModel
    private val addGenreUseCase = mockk<AddGenreUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AddGenreViewModel(addGenreUseCase)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveGenre calls use case and triggers onSuccess`() = runTest {
        coEvery { addGenreUseCase(any()) } returns Result.success(Unit)
        var successCalled = false
        
        viewModel.saveGenre("Fantasy") { successCalled = true }
        
        assertThat(successCalled).isTrue()
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `saveGenre sets error on failure`() = runTest {
        coEvery { addGenreUseCase(any()) } returns Result.failure(Exception("Error message"))
        
        viewModel.saveGenre("Fantasy") {}

        val uiText = viewModel.error.value as UiText.DynamicString
        assertThat(uiText.value).isEqualTo("Error message")
    }
}
