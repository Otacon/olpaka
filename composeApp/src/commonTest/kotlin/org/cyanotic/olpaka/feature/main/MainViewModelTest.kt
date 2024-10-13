package org.cyanotic.olpaka.feature.main

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.*
import org.cyanotic.olpaka.core.Preferences
import org.cyanotic.olpaka.core.domain.Model
import org.cyanotic.olpaka.repository.ModelsRepository
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val stateModels = MutableStateFlow<List<Model>>(emptyList())
    private val modelsRepository = mock<ModelsRepository> {
        every { models } returns stateModels.asStateFlow()
    }
    private val preferences = mock<Preferences>(mode = MockMode.autoUnit) {
        every { hasSeenOnboarding } returns false
    }

    @Test
    fun given_notSeenOnboarding_when_startingTheApp_then_onboardingIsShown() = runTestOn { viewModel ->
        // GIVEN
        every { preferences.hasSeenOnboarding } returns false

        viewModel.event.test {
            // WHEN
            viewModel.onCreate()

            // THEN
            val actual = awaitItem()
            assertEquals(MainEvent.OpenOnboarding, actual)
        }
    }

    @Test
    fun given_seenOnboarding_when_startingTheApp_then_onboardingIsNotShown() = runTestOn { viewModel ->
        // GIVEN
        every { preferences.hasSeenOnboarding } returns true

        viewModel.event.test {
            // WHEN
            viewModel.onCreate()
            advanceUntilIdle()

            // THEN
            expectNoEvents()
        }
    }

    @Test
    fun when_tabChanges_then_tabIsShown() = runTestOn { viewModel ->
        turbineScope {
            val events = viewModel.event.testIn(backgroundScope)
            val states = viewModel.state.testIn(backgroundScope)

            suspend fun tabChangesTest(index: Int, event: MainEvent, selectedIndex: Int) {
                viewModel.onTabChanged(index)

                advanceUntilIdle()

                assertEquals(event, events.awaitItem(), "Event failed for tab index $index")
                assertEquals(selectedIndex, states.awaitItem().selectedTabIndex, "State failed for tab index $index")
            }

            tabChangesTest(
                index = -10,
                event = MainEvent.OpenChat,
                selectedIndex = 0,
            )
            tabChangesTest(
                index = 10,
                event = MainEvent.OpenSettings,
                selectedIndex = 2,
            )

            tabChangesTest(
                index = 0,
                event = MainEvent.OpenChat,
                selectedIndex = 0,
            )
            tabChangesTest(
                index = 1,
                event = MainEvent.OpenModels,
                selectedIndex = 1,
            )
            tabChangesTest(
                index = 2,
                event = MainEvent.OpenSettings,
                selectedIndex = 2,
            )
        }
    }

    @Test
    fun given_modelsAreDownloading_when_notInModelsTab_then_badgeIsShown() = runTestOn { viewModel ->
        // GIVEN
        viewModel.onTabChanged(1)
        advanceUntilIdle()
        val downloadingModel = Model.Downloading(
            id = "id",
            name = "name",
            downloadedBytes = 0,
            sizeBytes = 0,
            speedBytesSecond = 0,
            timeLeftSeconds = 0
        )
        stateModels.value = listOf(downloadingModel)

        // WHEN
        viewModel.onTabChanged(index = 0)
        advanceUntilIdle()

        // THEN
        val chatSelectedBadge = viewModel.state.value.activityBadge
        assertEquals(Badge.DOWNLOADING, chatSelectedBadge)

        // WHEN
        viewModel.onTabChanged(index = 1)
        advanceUntilIdle()

        // THEN
        val modelsSelectedBadge = viewModel.state.value.activityBadge
        assertEquals(Badge.NONE, modelsSelectedBadge)

        // WHEN
        viewModel.onTabChanged(index = 2)
        advanceUntilIdle()

        // THEN
        val settingsSelectedBadge = viewModel.state.value.activityBadge
        assertEquals(Badge.DOWNLOADING, settingsSelectedBadge)
    }

    @Test
    fun given_modelsAreDownloaded_when_openingModels_then_badgeIsCleared() = runTestOn { viewModel ->
        viewModel.onTabChanged(index = 1)
        advanceUntilIdle()

        val downloadingModel = Model.Downloading(
            id = "id",
            name = "name",
            downloadedBytes = 0,
            sizeBytes = 0,
            speedBytesSecond = 0,
            timeLeftSeconds = 0
        )

        stateModels.value = listOf(downloadingModel)
        viewModel.onTabChanged(index = 0)
        advanceUntilIdle()

        val cachedModel = Model.Cached(
            id = "id",
            name = "name",
            size = 0,
            quantization = "",
            parameters = "",
        )
        stateModels.value = listOf(cachedModel)
        advanceUntilIdle()

        assertEquals(Badge.COMPLETED, viewModel.state.value.activityBadge)

        viewModel.onTabChanged(index = 2)
        advanceUntilIdle()

        assertEquals(Badge.COMPLETED, viewModel.state.value.activityBadge)

        viewModel.onTabChanged(index = 1)
        advanceUntilIdle()

        assertEquals(Badge.NONE, viewModel.state.value.activityBadge)

        viewModel.onTabChanged(index = 0)
        advanceUntilIdle()

        assertEquals(Badge.NONE, viewModel.state.value.activityBadge)

        viewModel.onTabChanged(index = 2)
        advanceUntilIdle()

        assertEquals(Badge.NONE, viewModel.state.value.activityBadge)
    }

    private fun runTestOn(body: suspend TestScope.(viewModel: MainViewModel) -> Unit) = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        val viewModel = MainViewModel(
            modelsRepository = modelsRepository,
            preferences = preferences,
            backgroundDispatcher = testDispatcher,
        )
        advanceUntilIdle()
        try {
            body(viewModel)
        } finally {
            Dispatchers.resetMain()
        }
    }
}