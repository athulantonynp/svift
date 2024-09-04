package athul.svift.android.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppUpdateData(
    @Json(name = "latest_version")
    var latestVersion:Int=0,
    var link:String=""
)