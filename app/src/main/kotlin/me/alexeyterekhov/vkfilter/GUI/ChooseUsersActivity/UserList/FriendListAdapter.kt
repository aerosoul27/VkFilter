package me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity.UserList

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
import me.alexeyterekhov.vkfilter.DataCache.FriendsListCache
import me.alexeyterekhov.vkfilter.DataClasses.User
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import me.alexeyterekhov.vkfilter.Util.TextFormat
import java.util.*


public class FriendListAdapter(
        val selected: MutableSet<Long>,
        val onSelectionChange: () -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val friends = Vector<User>()
    private var update = -1L
    private val imageLoader = ImageLoader.getInstance()

    val TYPE_ITEM = 1
    val TYPE_FOOTER = 2

    fun checkFriendCache() {
        if (FriendsListCache.lastUpdate() != update) {
            update = FriendsListCache.lastUpdate()
            friends.clear()
            friends.addAll(FriendsListCache.list)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = friends.size + 1
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(AppContext.instance)
        return when (viewType) {
            TYPE_ITEM -> {
                val view = inflater.inflate(R.layout.item_user_checked, parent, false)
                UserItemHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_floatbutton_footer, parent, false)
                object : RecyclerView.ViewHolder(view) {}
            }
        }
    }
    override fun getItemViewType(p: Int) = if (p == friends.size) TYPE_FOOTER else TYPE_ITEM
    override fun onBindViewHolder(h: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) != TYPE_ITEM)
            return

        val user = friends[position]

        with (h as UserItemHolder) {
            singlePic.visibility = View.VISIBLE
            doubleLayout.visibility = View.GONE
            tripleLayout.visibility = View.GONE
            quadLayout.visibility = View.GONE
            imageLoader.displayImage(user.photoUrl, singlePic)

            name.text = TextFormat.userTitle(user, false)

            val id = user.id.toLong()
            with (checkBox) {
                setOnCheckedChangeListener(null)
                isChecked = selected.contains(id)
                setOnCheckedChangeListener {
                    view, checked ->
                    if (checked) {
                        selected.add(id)
                    } else {
                        selected.remove(id)
                    }
                    onSelectionChange()
                }
            }
        }
    }
}