package dev.internetapp.presentation

import android.content.ContentResolver
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.abbasian.protocol.LocationCommand
import dev.abbasian.protocol.LocationResponse
import dev.internetapp.data.CommandRepositoryImpl
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
    fun `sendStartServiceCommandAndReceiveSuccessResponse`() = runTest {
        val response = repository.sendCommand(LocationCommand.StartService)

        Assert.assertTrue(response is LocationResponse.Success || response is LocationResponse.Error)
    }

    @Test
    fun `sendGetLocationsCommandReturnsLocationList`() = runTest {
        val response = repository.sendCommand(LocationCommand.GetAllLocations)

        Assert.assertTrue(
            response is LocationResponse.LocationList ||
                    response is LocationResponse.Error
        )
    }
}