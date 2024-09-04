package athul.svift.android.activities

import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import athul.svift.android.R
import athul.svift.android.data.models.AppUpdateData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AppUpdate : AppCompatActivity() {
    val webView :WebView by lazy {
        findViewById(R.id.wv_update)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_update)
        checkForUpdate()
    }

    private fun checkForUpdate(){
        lifecycleScope.launch {
            val data = getLatestVersionData()
            if (data!=null){
                val currentVersion = getCurrentVersionCode()
                Log.e("DATA",data.toString())
                Log.e("DATAC",currentVersion.toString())
                if (data.latestVersion>currentVersion){
                    Toast.makeText(this@AppUpdate,"Need update.Loading page",Toast.LENGTH_SHORT).show()
                    loadDownloadPage(data.link,data.latestVersion)
                }else{
                    Toast.makeText(this@AppUpdate,"No need of update. Already latest version",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this@AppUpdate,"Firebase db returned null",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadDownloadPage(link:String,version:Int){
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val request = DownloadManager.Request(Uri.parse(url))

            // Setting up the file destination and title
            request.setTitle("Downloading File...")
            request.setDescription("Downloading from $url")
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "svift-${version}")

            // Get the DownloadManager and enqueue the download request
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        }

        // Load the website
        webView.loadUrl(link)

    }

    private fun getCurrentVersionCode():Int{

        try {
            // Get package manager to retrieve app information
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)

            // Get the version name and version code
            val versionName = packageInfo.versionName
            val versionCode: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt() // API 28+
            } else {
                packageInfo.versionCode // API 27 and below
            }

            return versionCode

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }

    private suspend fun getLatestVersionData():AppUpdateData?{
        try {
            val data = FirebaseFirestore.getInstance().collection("app_update_data").document("data").get().await()
            return data.toObject(AppUpdateData::class.java)

        }catch (e:Exception){
            e.printStackTrace()
            return null
        }
    }
}