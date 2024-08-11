package athul.svift.android.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "userName")
    val userName:String,
    @Json(name = "password")
    val password:String
)
