package dev.locationapp.feature.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dev.abbasian.protocol.AppLogger

class BootReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootReceiverEntryPoint {
        fun logger(): AppLogger
    }

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            BootReceiverEntryPoint::class.java
        )
        val logger = entryPoint.logger()

        logger.i(TAG, "Boot receiver triggered with action: ${intent.action}")

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                restartLocationService(context, logger)
            }
        }
    }

    private fun restartLocationService(context: Context, logger: AppLogger) {
        logger.i(TAG, "Restarting location service after boot/update")

        try {
            val serviceIntent = Intent(context, LocationCollectionService::class.java).apply {
                action = LocationCollectionService.ACTION_START_COLLECTION
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            logger.i(TAG, "Location service restart command sent")
        } catch (e: Exception) {
            logger.e(TAG, "Failed to restart location service", e)
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}