package com.jksalcedo.passvault

import android.app.Application
import androidx.work.Configuration
import com.jksalcedo.passvault.repositories.PasswordRepository
import com.jksalcedo.passvault.repositories.PreferenceRepository
import com.jksalcedo.passvault.workers.BackupWorkerFactory

class Application() : Application(),
    Configuration.Provider {

    private lateinit var passwordRepository: PasswordRepository
    private lateinit var preferenceRepository: PreferenceRepository
    private lateinit var workerFactory: BackupWorkerFactory

    override fun onCreate() {
        super.onCreate()

        passwordRepository = PasswordRepository(this.applicationContext)
        preferenceRepository = PreferenceRepository(this.applicationContext)
        workerFactory = BackupWorkerFactory(passwordRepository, preferenceRepository)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
