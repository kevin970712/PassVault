package com.jksalcedo.passvault

import android.app.Application
import androidx.work.Configuration
import com.jksalcedo.passvault.repositories.PasswordRepository
import com.jksalcedo.passvault.repositories.PreferenceRepository
import com.jksalcedo.passvault.workers.BackupWorkerFactory

class Application() : Application(),
    Configuration.Provider {

    val passwordRepository = PasswordRepository(this)
    val preferenceRepository = PreferenceRepository(this)
    val workerFactory = BackupWorkerFactory(passwordRepository, preferenceRepository)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
