package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.os.Handler
import me.alexeyterekhov.vkfilter.DataCache.Common.DataDependAdapter
import me.alexeyterekhov.vkfilter.DataCache.MessageCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.GUI.SettingsActivity.Settings
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.*

class RequestModule(val activity: ChatActivity) {
    companion object {
        val LOAD_PORTION = 40
        val LOAD_THRESHOLD = 20
        val MAX_SYMBOLS_IN_MESSAGE = 3000
        val TYPING_REQUEST_PERIOD_MILLIS = 5000

        fun sendMessage(message: Message, id: String, isChat: Boolean, restOfSending: Boolean = false) {
            var restText = ""
            if (message.text.count() > MAX_SYMBOLS_IN_MESSAGE) {
                restText = message.text.substring(MAX_SYMBOLS_IN_MESSAGE)
                message.text = message.text.substring(0, MAX_SYMBOLS_IN_MESSAGE)
            }

            val request = RequestMessageSend(message, id, isChat)
            val guid = request.getSendingGuid()
            RequestControl addBackgroundOrdered request
            if (!restOfSending)
                MessageCaches.getCache(id, isChat).onWillSendMessage(guid)
            else
                MessageCaches.getCache(id, isChat).onWillSendMessage(guid, message)

            if (restText != "") {
                val restMessage = Message(message.senderId)
                restMessage.isOut = true
                restMessage.isRead = false
                restMessage.text = restText
                Handler().postDelayed({ sendMessage(restMessage, id, isChat, restOfSending = true) }, 1000)
            }
        }
    }

    private var lastTypingTime = 0L
    var messageLoading = false
    val messageListener = createCacheListener()

    fun loadMessagesOlderThan(oldestMessageId: String) {
        if (!messageLoading && !getMessageCache().historyLoaded) {
            messageLoading = true
            getMessageCache().listeners.add(messageListener)
            RequestControl addForeground RequestMessageHistory(
                    dialogId = activity.launchParameters.dialogId(),
                    isChat = activity.launchParameters.isChat(),
                    count = LOAD_PORTION,
                    olderThanId = oldestMessageId
            )
            activity.refreshIndicatorModule.showDelayed()
        }
    }

    fun loadLastMessages() {
        if (!messageLoading && !getMessageCache().historyLoaded) {
            messageLoading = true
            getMessageCache().listeners.add(messageListener)
            RequestControl addForeground RequestMessageHistory(
                    dialogId = activity.launchParameters.dialogId(),
                    isChat = activity.launchParameters.isChat(),
                    count = LOAD_PORTION
            )
            activity.refreshIndicatorModule.showDelayed()
        }
    }

    fun loadDialogPartners() {
        if (UserCache.getMe() == null)
            RequestControl addBackground RequestDialogPartners(
                    activity.launchParameters.dialogId().toLong(),
                    activity.launchParameters.isChat()
            )
    }

    fun sendMessage(editMessage: Message) {
        Companion.sendMessage(
                editMessage,
                activity.launchParameters.dialogId(),
                activity.launchParameters.isChat()
        )
    }

    fun showTypingIfNecessary() {
        if (Settings.getGhostModeEnabled())
            return

        val time = System.currentTimeMillis()
        if (time - lastTypingTime > TYPING_REQUEST_PERIOD_MILLIS) {
            lastTypingTime = time
            RequestControl.addBackground(RequestMeTyping(
                    activity.launchParameters.dialogId(),
                    activity.launchParameters.isChat()))
        }
    }

    fun readIncomeMessages(forced: Boolean) {
        if (!Settings.getGhostModeEnabled() || forced) {
            RequestControl.addBackground(RequestReadMessages(
                    activity.launchParameters.dialogId(),
                    activity.launchParameters.isChat()))
        }
    }

    private fun getMessageCache() = MessageCaches.getCache(
            activity.launchParameters.dialogId(),
            activity.launchParameters.isChat()
    )

    private fun createCacheListener() = object : DataDependAdapter() {
        override fun onDataUpdate() {
            getMessageCache().listeners.remove(this)
            messageLoading = false
            activity.refreshIndicatorModule.hide()
        }
    }
}