package org.cyanotic.olpaka.feature.models

import androidx.lifecycle.ViewModel
import app.cash.turbine.test
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import olpaka.composeapp.generated.resources.Res
import olpaka.composeapp.generated.resources.error_missing_ollama_message
import olpaka.composeapp.generated.resources.error_missing_ollama_title
import olpaka.composeapp.generated.resources.models_error_no_models_message
import olpaka.composeapp.generated.resources.models_error_no_models_title
import org.cyanotic.olpaka.core.Analytics
import org.cyanotic.olpaka.core.StringResources
import org.cyanotic.olpaka.core.domain.Model
import org.cyanotic.olpaka.repository.ModelsRepository
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ModelsViewModelTest {

    private val modelsState = MutableStateFlow<List<Model>>(emptyList())
    private val repository = mock<ModelsRepository> {
        every { models } returns modelsState
        everySuspend { refreshModels() } returns Result.success(emptyList())
    }
    private val analytics = mock<Analytics>(mode = MockMode.autoUnit)
    private val strings = mock<StringResources> {
        everySuspend { get(Res.string.models_error_no_models_title) } returns "no models title"
        everySuspend { get(Res.string.models_error_no_models_message) } returns "no models message"
        everySuspend { get(Res.string.error_missing_ollama_title) } returns "missing ollama title"
        everySuspend { get(Res.string.error_missing_ollama_message) } returns "missing ollama message"
    }

    @Test
    fun given_thereAreModels_when_started_then_modelsAreShown() =
        viewModelTest(::viewModel) { viewModel ->
            // WHEN
            viewModel.onCreate()
            advanceUntilIdle()

            modelsState.value = listOf(
                Model.Cached(
                    id = "id",
                    name = "name",
                    size = 0,
                    quantization = "quantization",
                    parameters = "parameters"
                )
            )
            advanceUntilIdle()

            // THEN
            val actual = viewModel.state.value
            val expected = ModelsState.Content(
                models = listOf(
                    ModelUI.Available(
                        key = "id",
                        title = "name",
                        subtitle = "0.0 B • quantization • parameters",
                    )
                ),
                controlsEnabled = true,
            )
            assertEquals(expected, actual)
        }

    @Test
    fun given_ollamaIsNotReachable_when_started_then_errorIsShown() =
        viewModelTest(::viewModel) { viewModel ->
            // GIVEN
            everySuspend { repository.refreshModels() } returns Result.failure(Exception("Whoopsie!"))

            // WHEN
            viewModel.onCreate()
            advanceUntilIdle()

            // THEN
            val actual = viewModel.state.value
            val expected = ModelsState.Error(
                title = "missing ollama title",
                message = "missing ollama message",
            )
            assertEquals(expected, actual)
        }

    @Test
    fun when_started_then_screenViewIsTracked() = viewModelTest(::viewModel) { viewModel ->
        // WHEN
        viewModel.onCreate()
        advanceUntilIdle()

        // THEN
        verify { analytics.screenView("models") }
    }

    @Test
    fun given_thereAreModels_when_refreshing_then_modelsAreShown() =
        viewModelTest(::viewModel) { viewModel ->
            // GIVEN
            everySuspend { repository.refreshModels() } returns Result.success(emptyList())

            // WHEN
            viewModel.onRefreshClicked()
            advanceUntilIdle()

            modelsState.value = listOf(
                Model.Cached(
                    id = "id",
                    name = "name",
                    size = 0,
                    quantization = "quantization",
                    parameters = "parameters"
                )
            )
            advanceUntilIdle()

            // THEN
            val actual = viewModel.state.value
            val expected = ModelsState.Content(
                models = listOf(
                    ModelUI.Available(
                        key = "id",
                        title = "name",
                        subtitle = "0.0 B • quantization • parameters",
                    )
                ),
                controlsEnabled = true,
            )
            assertEquals(expected, actual)
        }

    @Test
    fun when_refreshClicked_then_eventIsTracked() = viewModelTest(::viewModel) { viewModel ->
        // WHEN
        viewModel.onRefreshClicked()
        advanceUntilIdle()

        // THEN
        verify { analytics.event("refresh_models") }
    }

    @Test
    fun when_addModelIsClicked_then_addModelDialogIsShown() =
        viewModelTest(::viewModel) { viewModel ->
            viewModel.event.test {
                // WHEN
                viewModel.onAddModelClicked()
                advanceUntilIdle()

                // THEN
                assertEquals(ModelsEvent.OpenAddModelDialog, awaitItem())
            }
        }

    @Test
    fun when_modelIsAdded_then_modelShouldBeDownloaded() = viewModelTest(::viewModel) { viewModel ->
        everySuspend { repository.downloadModel("model") } returns Unit

        // WHEN
        viewModel.onAddModel("model")
        advanceUntilIdle()

        // THEN
        verifySuspend { repository.downloadModel("model") }
    }

    @Test
    fun when_removeModelClicked_then_removeModelDialogIsShown() =
        viewModelTest(::viewModel) { viewModel ->
            // GIVEN
            viewModel.event.test {
                // WHEN
                viewModel.onRemoveModelClicked(ModelUI.Available("key", "title", "subtitle"))
                advanceUntilIdle()

                // THEN
                assertEquals(ModelsEvent.OpenRemoveModelDialog("key"), awaitItem())
            }
        }

    @Test
    fun when_confirmRemoveModel_then_modelIsRemoved() = viewModelTest(::viewModel) { viewModel ->
        // GIVEN
        everySuspend { repository.removeModel("model") } returns Result.success(Unit)

        // WHEN
        viewModel.onConfirmRemoveModel("model")
        advanceUntilIdle()

        // THEN
        verifySuspend { repository.removeModel("model") }
    }

    @Test
    fun when_confirmRemoveModel_then_eventIsTracked() = viewModelTest(::viewModel) { viewModel ->
        // GIVEN
        everySuspend { repository.removeModel("model") } returns Result.success(Unit)

        // WHEN
        viewModel.onConfirmRemoveModel("model")
        advanceUntilIdle()

        // THEN
        verify {
            analytics.event(
                eventName = "remove_model",
                properties = mapOf("model" to "model")
            )
        }
    }

    @Test
    fun when_cancelDownload_then_downloadIsCanceled() = viewModelTest(::viewModel) { viewModel ->
        // GIVEN
        everySuspend { repository.cancelDownload() } returns Unit

        // WHEN
        viewModel.onCancelDownload()
        advanceUntilIdle()

        // THEN
        verify { repository.cancelDownload() }
    }

    @Test
    fun when_modelsDownloading_then_progressIsShown() = viewModelTest(::viewModel) { viewModel ->
        // GIVEN
        val coreModels = MutableStateFlow<List<Model>>(
            listOf(
                Model.Downloading(
                    id = "id",
                    name = "name",
                    downloadedBytes = 20L,
                    sizeBytes = 100L,
                    speedBytesSecond = 10L,
                    timeLeftSeconds = 8L,
                )
            )
        )
        every { repository.models } returns coreModels

        // WHEN
        advanceUntilIdle()

        // THEN
        val expectedState = ModelsState.Content(
            models = listOf(
                ModelUI.Downloading(
                    key = "id",
                    title = "name",
                    subtitle = "10.0 B/s - 20.0 B of 100.0 B, 8s left",
                    progress = 0.2f,
                ),
            ),
            controlsEnabled = false
        )
        assertEquals(expectedState, viewModel.state.value,)
    }


    private fun viewModel(testDispatcher: TestDispatcher) = ModelsViewModel(
        repository = repository,
        analytics = analytics,
        statsFormatter = DownloadStatsFormatterDefault(),
        dispatcher = testDispatcher,
        strings = strings,
    )

}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T : ViewModel> viewModelTest(
    createViewModel: (TestDispatcher) -> T,
    body: suspend TestScope.(viewModel: T) -> Unit
) = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    Dispatchers.setMain(testDispatcher)
    val viewModel = createViewModel(testDispatcher)
    try {
        body(viewModel)
    } finally {
        Dispatchers.resetMain()
    }
}