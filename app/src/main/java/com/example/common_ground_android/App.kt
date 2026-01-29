package com.example.common_ground_android

import android.app.Application
import com.example.common_ground_android.network.client.KtorClientFactory
import de.hdodenhof.circleimageview.BuildConfig
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        KtorClientFactory.create(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        KtorClientFactory.close()
    }
}