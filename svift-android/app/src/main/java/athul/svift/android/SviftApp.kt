package athul.svift.android

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import athul.svift.android.injection.Injection

class SviftApp : Application(),Application.ActivityLifecycleCallbacks{

    var isInBackground = false
    override fun onCreate() {
        super.onCreate()
        Injection.init(this)
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {
        isInBackground = true
    }

    override fun onActivityStopped(activity: Activity) {
        isInBackground = true
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

}