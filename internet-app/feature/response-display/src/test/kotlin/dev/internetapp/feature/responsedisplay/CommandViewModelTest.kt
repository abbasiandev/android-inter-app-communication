package dev.internetapp.feature.responsedisplay

import app.cash.turbine.test
import dev.abbasian.protocol.domain.logger.AppLogger
import dev.abbasian.protocol.domain.model.CommandError
import dev.abbasian.protocol.domain.model.CommandResult
import dev.abbasian.protocol.domain.model.LocationData
import dev.abbasian.protocol.domain.model.LocationResponse
import dev.internetapp.core.common.analytics.InternetAppAnalytics
import dev.internetapp.feature.commandsender.domain.usecase.GetAllLocationsUseCase
import dev.internetapp.feature.commandsender.domain.usecase.GetLatestLocationUseCase
import dev.internetapp.feature.commandsender.domain.usecase.StartServiceUseCase
import dev.internetapp.feature.commandsender.domain.usecase.StopServiceUseCase
import dev.internetapp.feature.responsedisplay.domain.model.CommandEffect
import dev.internetapp.feature.responsedisplay.domain.model.CommandIntent
import dev.internetapp.feature.responsedisplay.domain.model.ServiceStatus
import dev.internetapp.feature.responsedisplay.presentation.viewmodel.CommandViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CommandViewModelTest {
    private lateinit var viewModel: CommandViewModel
    private lateinit var startServiceUseCase: StartServiceUseCase
    private lateinit var stopServiceUseCase: StopServiceUseCase
    private lateinit var getAllLocationsUseCase: GetAllLocationsUseCase
    private lateinit var getLatestLocationUseCase: GetLatestLocationUseCase
    private lateinit var logger: AppLogger
    private lateinit var analytics: InternetAppAnalytics

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        startServiceUseCase = mockk()
        stopServiceUseCase = mockk()
        getAllLocationsUseCase = mockk()
        getLatestLocationUseCase = mockk()
        logger = mockk(relaxed = true)
        analytics = mockk(relaxed = true)

        viewModel =
            CommandViewModel(
                startServiceUseCase,
                stopServiceUseCase,
                getAllLocationsUseCase,
                getLatestLocationUseCase,
                logger,
                analytics,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `start service intent updates state to running`() =
        runTest {
            coEvery { startServiceUseCase(any()) } returns
                CommandResult.Success(LocationResponse.Success("Service started"))

            viewModel.handleIntent(CommandIntent.StartService)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertEquals(ServiceStatus.RUNNING, state.serviceStatus)
            Assert.assertFalse(state.isLoading)
            Assert.assertNull(state.error)
        }

    @Test
    fun `stop service intent updates state to stopped`() =
        runTest {
            coEvery { stopServiceUseCase(any()) } returns
                CommandResult.Success(LocationResponse.Success("Service stopped"))

            viewModel.handleIntent(CommandIntent.StopService)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertEquals(ServiceStatus.STOPPED, state.serviceStatus)
            Assert.assertFalse(state.isLoading)
        }

    @Test
    fun `get all locations intent updates locations list`() =
        runTest {
            val locations =
                listOf(
                    LocationData(
                        latitude = 48.8566,
                        longitude = 2.3522,
                        accuracy = 10f,
                        timestamp = System.currentTimeMillis(),
                        provider = "gps",
                    ),
                )
            coEvery { getAllLocationsUseCase(any()) } returns
                CommandResult.Success(LocationResponse.LocationList(locations))

            viewModel.handleIntent(CommandIntent.GetAllLocations)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertEquals(1, state.locations.size)
            Assert.assertEquals(48.8566, state.locations[0].latitude, 0.0001)
        }

    @Test
    fun `error response updates error state`() =
        runTest {
            val errorMessage = "Communication failed"
            val error = CommandError.Unknown(errorMessage)

            coEvery { startServiceUseCase(any()) } returns
                CommandResult.Failure(error)

            viewModel.handleIntent(CommandIntent.StartService)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertNotNull(state.error)
            Assert.assertFalse(state.isLoading)
        }

    @Test
    fun `effect emits success on start service`() =
        runTest {
            coEvery { startServiceUseCase(any()) } returns
                CommandResult.Success(LocationResponse.Success("Service started"))

            viewModel.effect.test {
                viewModel.handleIntent(CommandIntent.StartService)
                testDispatcher.scheduler.advanceUntilIdle()

                val effect = awaitItem()
                Assert.assertTrue(effect is CommandEffect.ShowSuccess)
                Assert.assertEquals(
                    "Service started",
                    (effect as CommandEffect.ShowSuccess).message,
                )
            }
        }
}
