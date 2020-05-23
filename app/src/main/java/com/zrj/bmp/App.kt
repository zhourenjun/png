package com.zrj.bmp

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.clj.fastble.BleManager
import com.tencent.bugly.Bugly
import com.zrj.bmp.utils.locale.LocaleAwareApplication
import me.jessyan.autosize.AutoSizeConfig
import kotlin.properties.Delegates

/**
 *  zrj  2019/5/17
 */
class App : LocaleAwareApplication() {

    companion object {
        var context: Context by Delegates.notNull()
            private set
    }

    override fun onCreate() {
        super.onCreate()
        AutoSizeConfig.getInstance().designWidthInDp = 360
        context = this
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }
        })
        Bugly.init(applicationContext, "0104607301", BuildConfig.DEBUG)
        BleManager.getInstance().init(this)
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(1, 5000)
            .setConnectOverTime(20000).operateTimeout = 5000
    }
}