package com.jun3120.acremote

import android.app.Application
import android.util.Log
import net.irext.webapi.WebAPIs
import net.irext.webapi.WebAPICallbacks
import net.irext.webapi.model.UserApp

class App : Application() {

    val webAPIs: WebAPIs = WebAPIs.getInstance(BASE_URL, APP_PATH)

    private var userApp: UserApp? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        signIn()
    }

    private fun signIn() {
        Thread {
            webAPIs.signIn(this, object : WebAPICallbacks.SignInCallback {
                override fun onSignInSuccess(userApp: UserApp) {
                    this@App.userApp = userApp
                    Log.d(TAG, "IRext signIn success: id=${userApp.id}, token=${userApp.token}")
                }

                override fun onSignInFailed() {
                    Log.w(TAG, "IRext signIn failed")
                }

                override fun onSignInError() {
                    Log.e(TAG, "IRext signIn error")
                }
            })
        }.start()
    }

    companion object {
        private const val TAG = "App"
        private const val BASE_URL = "https://srv.irext.net"
        private const val APP_PATH = "/irext-server"

        lateinit var instance: App
            private set
    }
}
