package dev.cirtellock.applock.core.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import dev.cirtellock.applock.core.utils.LogUtils
import dev.cirtellock.applock.core.utils.appLockRepository
import dev.cirtellock.applock.data.repository.BackendImplementation
import dev.cirtellock.applock.services.AppLockAccessibilityService
import dev.cirtellock.applock.services.UsageLockService
import dev.cirtellock.applock.services.isServiceRunning

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val repository = context.appLockRepository()
        
        when (intent.action) {
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // Clear all old logs on app update
                LogUtils.clearAllLogs()
                try {
                    AppLockServiceStarter.startAppropriateServices(context, repository)
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting services on package replace", e)
                }
            }

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_USER_UNLOCKED,
            Intent.ACTION_USER_PRESENT -> {
                try {
                    AppLockServiceStarter.startAppropriateServices(context, repository)
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting services on boot or unlock", e)
                }
            }
            else -> {
                Log.w(TAG, "Invalid intent action: ${intent.action}")
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
