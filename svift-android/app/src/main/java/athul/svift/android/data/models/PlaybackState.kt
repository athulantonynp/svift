package athul.svift.android.data.models

data class PlaybackState(
    val status: PlaybackStatus=PlaybackStatus.NONE,
    val song:Song?=null
)

enum class PlaybackStatus{
    NONE,
    PLAYING,
    PAUSED
}