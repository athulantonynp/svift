package athul.svift.android

import android.app.Application
import athul.svift.android.injection.Injection

class SviftApp : Application(){

    override fun onCreate() {
        super.onCreate()
        Injection.init(this)
    }
}