package athul.svift.android.data.models

import sviftytd.DownloadCallback

interface FetchCallback {
    var downloadCallback:DownloadCallback
    fun onFetchTypeDecided(fetchType: FetchType)

    fun onFetchStarted()

    fun onFetchEnd()
}

enum class FetchType{
    FULL,
    PARTIAL,
    NO_FETCH
}