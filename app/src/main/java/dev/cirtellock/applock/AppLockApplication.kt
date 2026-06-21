package dev.cirtellock.applock

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import dev.cirtellock.applock.core.utils.LogUtils
import dev.cirtellock.applock.data.repository.AppLockRepository
import org.lsposed.hiddenapibypass.HiddenApiBypass
import kotlin.concurrent.thread

class AppLockApplication : Application() {

    lateinit var appLockRepository: AppLockRepository
        private set

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        initializeHiddenApiBypass()
    }

    override fun onCreate() {
        super.onCreate()
        initializeComponents()

        LogUtils.initialize(this)
        LogUtils.setLoggingEnabled(appLockRepository.isLoggingEnabled())
        // Purge logs older than 3 days on every app start (run in background to avoid ANR)
        thread(start = true, name = "LogPurge") {
            LogUtils.purgeOldLogs()
        }
    }

    private fun initializeComponents() {
        appLockRepository = AppLockRepository(this)
    }

    private fun initializeHiddenApiBypass() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                HiddenApiBypass.addHiddenApiExemptions("L")
                Log.d(TAG, "Hidden API bypass initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize hidden API bypass", e)
            }
        }
    }
    companion object {
        private const val TAG = "AppLockApplication"
    }
}
