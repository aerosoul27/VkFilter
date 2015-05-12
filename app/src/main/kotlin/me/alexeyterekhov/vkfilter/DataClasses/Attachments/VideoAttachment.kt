package me.alexeyterekhov.vkfilter.DataClasses.Attachments

class VideoAttachment(
        val id: Long,
        val title: String,
        val durationSec: Int,
        val previewUrl: String,
        var playerUrl: String
) {
    var requestKey = ""
}