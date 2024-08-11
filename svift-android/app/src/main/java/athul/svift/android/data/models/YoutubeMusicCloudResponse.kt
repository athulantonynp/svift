package athul.svift.android.data.models

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class YoutubeMusicItem(var videoId:String="",var time:Long=0L)

@JsonClass(generateAdapter = true)
data class YoutubeMusicCloudResponse(var ym:List<YoutubeMusicItem> = emptyList(),var userName:String="")