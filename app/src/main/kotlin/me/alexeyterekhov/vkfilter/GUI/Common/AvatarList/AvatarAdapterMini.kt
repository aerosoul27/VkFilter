package me.alexeyterekhov.vkfilter.GUI.Common.AvatarList

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.DataCache.ChatInfoCache
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.Database.VkIdentifier
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import java.util.*


class AvatarAdapterMini(val layoutRes: Int):
        RecyclerView.Adapter<AvatarHolder>()
{
    private val imageLoader = ImageLoader.getInstance()
    private val vkIds = Vector<VkIdentifier>()

    private val userIdsForLoading = HashSet<String>()
    private val chatIdsForLoading = HashSet<String>()

    infix fun setIds(ids: List<VkIdentifier>) {
        vkIds.clear()
        userIdsForLoading.clear()
        chatIdsForLoading.clear()

        vkIds.addAll(ids)
        notifyDataSetChanged()

        userIdsForLoading.addAll(vkIds
                .filter { it.type == VkIdentifier.TYPE_USER }
                .map { it.id.toString() }
                .filter { !UserCache.contains(it) })
        chatIdsForLoading.addAll(vkIds
                .filter { it.type == VkIdentifier.TYPE_CHAT }
                .map { it.id.toString() }
                .filter { !ChatInfoCache.contains(it) })
    }

    fun checkForNewAvatars() {
        var added = false
        val it1 = userIdsForLoading.iterator()
        while (it1.hasNext()) {
            val id = it1.next()
            if (UserCache contains id) {
                added = true
                it1.remove()
            }
        }
        val it2 = chatIdsForLoading.iterator()
        while (it2.hasNext()) {
            val id = it2.next()
            if (ChatInfoCache contains id) {
                added = true
                it2.remove()
            }
        }
        if (added)
            notifyDataSetChanged()
    }

    override fun getItemCount() = vkIds.size
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AvatarHolder? {
        val inflater = LayoutInflater.from(AppContext.instance)
        val view = inflater.inflate(layoutRes, parent, false)
        return AvatarHolder(view)
    }
    override fun onBindViewHolder(h: AvatarHolder, position: Int) {
        val vkId = vkIds.get(position)
        when (vkId.type) {
            VkIdentifier.TYPE_USER -> {
                h.setVisibilityForCount(1)
                if (UserCache contains vkId.id.toString())
                    imageLoader.displayImage(
                            (UserCache getUser vkId.id.toString())!!.photoUrl,
                            h.singleImage
                    )
                else
                    h.singleImage.setImageResource(R.drawable.icon_user_stub)
            }
            VkIdentifier.TYPE_CHAT -> {
                if (ChatInfoCache contains vkId.id.toString()) {
                    val chat = (ChatInfoCache getChat vkId.id.toString())!!
                    if (chat.photoUrl != "") {
                        h.setVisibilityForCount(1)
                        imageLoader.displayImage(chat.photoUrl, h.singleImage)
                    } else {
                        when (chat.chatPartners.size) {
                            0 -> {
                                h.setVisibilityForCount(1)
                                h.singleImage.setImageResource(R.drawable.icon_user_stub)
                            }
                            1 -> {
                                h.setVisibilityForCount(1)
                                imageLoader.displayImage(chat.chatPartners[0].photoUrl, h.singleImage)
                            }
                            2 -> {
                                h.setVisibilityForCount(2)
                                imageLoader.displayImage(chat.chatPartners[0].photoUrl, h.doubleImage1)
                                imageLoader.displayImage(chat.chatPartners[1].photoUrl, h.doubleImage2)
                            }
                            3 -> {
                                h.setVisibilityForCount(3)
                                imageLoader.displayImage(chat.chatPartners[0].photoUrl, h.tripleImage1)
                                imageLoader.displayImage(chat.chatPartners[1].photoUrl, h.tripleImage2)
                                imageLoader.displayImage(chat.chatPartners[2].photoUrl, h.tripleImage3)
                            }
                            else -> {
                                h.setVisibilityForCount(4)
                                imageLoader.displayImage(chat.chatPartners[0].photoUrl, h.quadImage1)
                                imageLoader.displayImage(chat.chatPartners[1].photoUrl, h.quadImage2)
                                imageLoader.displayImage(chat.chatPartners[2].photoUrl, h.quadImage3)
                                imageLoader.displayImage(chat.chatPartners[3].photoUrl, h.quadImage4)
                            }
                        }
                    }
                } else {
                    h.setVisibilityForCount(1)
                    h.singleImage.setImageResource(R.drawable.icon_user_stub)
                }
            }
        }
    }
}