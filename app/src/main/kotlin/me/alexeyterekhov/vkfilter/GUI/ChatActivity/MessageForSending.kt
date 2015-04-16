package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import me.alexeyterekhov.vkfilter.Common.DateFormat
import me.alexeyterekhov.vkfilter.DataClasses.Message

class MessageForSending {
    var dialogId = ""
    var isChat = false

    // message content
    var text = ""

    fun transformToMessage(messageId: Long): Message {
        val msg = Message("me")
        with (msg) {
            id = messageId
            text = this@MessageForSending.text
            formattedDate = DateFormat.time(System.currentTimeMillis() / 1000L)
            dateMSC = System.currentTimeMillis()
            isOut = true
            isRead = false
        }
        return msg
    }
}