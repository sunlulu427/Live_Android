package com.tencent.mlvb.apiexample

import android.app.Application
import androidx.multidex.MultiDex
import com.tencent.live2.V2TXLiveDef.V2TXLiveLogConfig
import com.tencent.live2.V2TXLivePremier
import com.tencent.mlvb.debug.GenerateTestUserSig

class MLVBApplication : Application() {

    companion object {
        private var instance: MLVBApplication? = null

        fun getInstance(): MLVBApplication? = instance
    }

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        instance = this

        val liveLogConfig = V2TXLiveLogConfig().apply {
            enableConsole = true
        }

        V2TXLivePremier.setLogConfig(liveLogConfig)
        V2TXLivePremier.setLicence(instance, GenerateTestUserSig.LICENSEURL, GenerateTestUserSig.LICENSEURLKEY)
    }
}