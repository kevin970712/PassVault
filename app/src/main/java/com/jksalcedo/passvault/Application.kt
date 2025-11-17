package com.jksalcedo.passvault

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.work.Configuration
import com.jksalcedo.passvault.repositories.PasswordRepository
import com.jksalcedo.passvault.repositories.PreferenceRepository
import com.jksalcedo.passvault.ui.auth.UnlockActivity
import com.jksalcedo.passvault.workers.BackupWorkerFactory

class Application() : Application(),
    Configuration.Provider {

    private lateinit var passwordRepository: PasswordRepository
    private lateinit var preferenceRepository: PreferenceRepository
    private lateinit var workerFactory: BackupWorkerFactory
    private var lastInteractionTime: Long = 0

    companion object {
        const val IDLE_TIMEOUT_MS: Long = 60 * 1000 // 1 minute
    }

    override fun onCreate() {
        super.onCreate()

        passwordRepository = PasswordRepository(this.applicationContext)
        preferenceRepository = PreferenceRepository(this.applicationContext)
        workerFactory = BackupWorkerFactory(passwordRepository, preferenceRepository)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                // Check if need to lock when an activity resumes
                if (lastInteractionTime > 0 &&
                    System.currentTimeMillis() - lastInteractionTime >= IDLE_TIMEOUT_MS &&
                    activity !is UnlockActivity
                ) {

                    val intent = Intent(activity, UnlockActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    activity.startActivity(intent)
                    activity.finish()
                }

                // Update time
                lastInteractionTime = System.currentTimeMillis()
            }

            override fun onActivityPaused(activity: Activity) {
                // Record the time when user leaves
                lastInteractionTime = System.currentTimeMillis()
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
