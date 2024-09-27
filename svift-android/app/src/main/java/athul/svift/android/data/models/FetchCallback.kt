package athul.svift.android.data.models

import sviftytd.DownloadCallback

interface FetchCallback {
    var downloadCallback:DownloadCallback
    fun onFetchTypeDecided(fetchType: FetchType)

    fun onFetchStarted()

    fun onFetchEnd()

    fun onFetchProgress(name:String)
}

enum class FetchType{
    FULL,
    PARTIAL,
    NO_FETCH
}