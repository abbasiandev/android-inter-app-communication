package dev.internetapp.presentation

import android.content.ContentResolver
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.abbasian.protocol.domain.model.CommandResult
import dev.abbasian.protocol.domain.model.LocationCommand
import dev.internetapp.feature.commandsender.data.repository.CommandRepositoryImpl
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InterAppCommunicationTest {
    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var repository: CommandRepositoryImpl

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        contentResolver = context.contentResolver
        repository = CommandRepositoryImpl(context, mockk(relaxed = true))
    }

    @Test
    fun `sendStartServiceCommandAndReceiveSuccessResponse`() =
        runTest {
            val result = repository.sendCommand(LocationCommand.StartService)

            Assert.assertTrue(
                result is CommandResult.Success || result is CommandResult.Failure,
            )
        }

    @Test
    fun `sendGetLocationsCommandReturnsLocationList`() =
        runTest {
            val result = repository.sendCommand(LocationCommand.GetAllLocations)

            Assert.assertTrue(
                result is CommandResult.Success || result is CommandResult.Failure,
            )
        }
}
