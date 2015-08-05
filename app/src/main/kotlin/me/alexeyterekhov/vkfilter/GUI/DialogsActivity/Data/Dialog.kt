package me.alexeyterekhov.vkfilter.GUI.DialogsActivity.Data

import me.alexeyterekhov.vkfilter.DataClasses.Message
import me.alexeyterekhov.vkfilter.DataClasses.User
import me.alexeyterekhov.vkfilter.Util.TextFormat
import java.util.LinkedList

class Dialog {
    var id = 0L
    val partners = LinkedList<User>()
    var lastMessage: Message? = null
    var chatPhotoUrl = ""
    var chatTitle = ""

    fun getTitle(): String {
        if (chatTitle != "")
            return chatTitle
        if (partners.count() == 1)
            return TextFormat.userTitle(partners.first()!!, compact = false)
        var title = partners
            .take(if (partners.count() > 4) 4 else partners.size())
            .map { TextFormat.userTitle(it, compact = true) }
            .joinToString(separator = ", ")
        if (partners.count() > 4)
            title += "..."
        return title
    }

    fun getImageCount() = when {
        chatPhotoUrl != "" -> 1
        partners.count() < 4 -> partners.count()
        else -> 4
    }

    fun getImageUrl(position: Int) = when {
        chatPhotoUrl != "" -> chatPhotoUrl
        else -> partners[position].photoUrl
    }

    fun isChat() = partners.count() > 1
    fun isOnline() = partners.count() == 1 && partners.first()!!.isOnline
    fun isSameDialog(other: Dialog) = other.isChat() == isChat() && other.id == id
    fun isNotSameDialog(other: Dialog) = !isSameDialog(other)
    fun isSameDialogAndContent(other: Dialog): Boolean {
        return isSameDialog(other)
                && other.chatPhotoUrl == chatPhotoUrl
                && other.chatTitle == chatTitle
                && sameMessage(other.lastMessage)
                && samePartners(other)
    }

    private fun sameMessage(other: Message?): Boolean {
        return (other == null && lastMessage == null)
                || (other != null && lastMessage != null
                && other.sentState == lastMessage!!.sentState
                && other.sentId == lastMessage!!.sentId
                && other.text == lastMessage!!.text
                && other.isRead == lastMessage!!.isRead)
    }
    private fun samePartners(other: Dialog): Boolean {
        if (other.partners.count() != partners.count())
            return false
        partners forEach { user ->
            val userWithEqualId = other.partners firstOrNull { it.id == user.id }
            if (userWithEqualId == null
                    || userWithEqualId.isOnline != user.isOnline
                    || userWithEqualId.photoUrl != user.photoUrl)
                return false
        }
        return true
    }
}