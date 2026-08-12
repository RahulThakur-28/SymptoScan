package com.rahul.symptoscan

import android.app.Application
import com.rahul.symptoscan.core.di.Injection

class SymptoScanApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Injection.init(this)
    }
}
