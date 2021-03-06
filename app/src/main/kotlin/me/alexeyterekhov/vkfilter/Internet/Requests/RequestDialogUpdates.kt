package me.alexeyterekhov.vkfilter.Internet.Requests

import me.alexeyterekhov.vkfilter.DataCache.MessageCache.MessageCaches
import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.Internet.DialogRefresher
import me.alexeyterekhov.vkfilter.Internet.JSONParser
import me.alexeyterekhov.vkfilter.Internet.MissingDataAnalyzer
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import org.json.JSONObject
import java.util.*

class RequestDialogUpdates(val dialogId: String, val isChat: Boolean) : Request("execute.dialogUpdates") {
    init {
        params[if (isChat) "chat_id" else "user_id"] = dialogId
        params["last_id"] = MessageCaches.getCache(dialogId, isChat).lastMessageIdFromServer
        val lastOutcomeRead = MessageCaches
                .getCache(dialogId, isChat)
                .getMessages()
                .lastOrNull { it.sentState == Message.STATE_SENT && it.isOut && it.isRead }
        params["read_id"] = lastOutcomeRead?.sentId ?: 0L
    }

    override fun handleResponse(json: JSONObject) {
        if (json.isNull("response"))
            DialogRefresher.messageCacheListener.onAddNewMessages(LinkedList<Message>())
        else {
            val response = json.getJSONObject("response")

            if (response.has("read")) {
                val lastReadId = response.getLong("read")
                MessageCaches.getCache(dialogId, isChat).onReadMessages(out = true, lastId = lastReadId)
            }

            if (response.has("new_messages")) {
                val jsonMessages = response.getJSONArray("new_messages")
                val messages = JSONParser parseMessages jsonMessages

                if (messages.isEmpty())
                    DialogRefresher.messageCacheListener.onAddNewMessages(LinkedList<Message>())
                else {
                    MessageCaches.getCache(dialogId, isChat).putMessages(messages = messages, allHistoryLoaded = false)
                    loadMissingUsers(messages)
                    loadMissingVideos(messages)
                }
            }
        }
    }

    private fun loadMissingUsers(messages: Collection<Message>) {
        val missingIds = MissingDataAnalyzer.missingUsersIds(messages)
        if (missingIds.isNotEmpty())
            RequestControl addBackground RequestUsers(missingIds)
    }

    private fun loadMissingVideos(messages: Collection<Message>) {
        val missingIds = MissingDataAnalyzer.missingVideoIds(messages)
        if (missingIds.isNotEmpty())
            RequestControl addBackground RequestVideoUrls(dialogId, isChat, missingIds)
    }
}