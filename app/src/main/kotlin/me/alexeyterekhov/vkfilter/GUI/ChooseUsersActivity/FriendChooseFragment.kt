package me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.FriendsListCache
import me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity.UserList.FriendListAdapter
import me.alexeyterekhov.vkfilter.GUI.Common.CustomSwipeRefreshLayout
import me.alexeyterekhov.vkfilter.Internet.RequestControl
import me.alexeyterekhov.vkfilter.Internet.Requests.RequestFriendList
import me.alexeyterekhov.vkfilter.LibClasses.EndlessScrollListener
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext
import me.alexeyterekhov.vkfilter.Util.DataSaver
import kotlin.properties.Delegates


public class FriendChooseFragment: Fragment(), DataDepend {
    companion object {
        val KEY_SAVED = "FriendFragmentSaved"
        val KEY_ADAPTER = "FriendFragmentAdapter"
    }

    var adapter: FriendListAdapter by Delegates.notNull()
    val LOAD_PORTION = 50
    val LOAD_THRESHOLD = 15


    fun setSelectedUsers(selected: MutableSet<Long>, notifyAction: () -> Unit) {
        adapter = FriendListAdapter(selected, notifyAction)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_choose, container, false)

        val recycler = view.findViewById(R.id.recyclerList) as RecyclerView
        recycler.layoutManager = LinearLayoutManager(AppContext.instance)
        recycler.adapter = adapter

        val endless = object: EndlessScrollListener(recycler, LOAD_THRESHOLD) {
            override fun onReachThreshold(currentItemCount: Int) {
                RequestControl addForeground RequestFriendList(
                        offset = FriendsListCache.list.size,
                        count = LOAD_PORTION
                )
            }
        }
        recycler.setOnScrollListener(endless)

        val refreshLayout = view.findViewById(R.id.refreshLayout) as CustomSwipeRefreshLayout
        refreshLayout.setRecyclerView(recycler)
        refreshLayout.setOnRefreshListener {
            RequestControl addForeground RequestFriendList(0, LOAD_PORTION)
        }
        refreshLayout.setColorSchemeResources(
                R.color.ui_refresh1,
                R.color.ui_refresh2,
                R.color.ui_refresh3,
                R.color.ui_refresh4
        )

        refreshLayout.isRefreshing = true
        RequestControl addForeground RequestFriendList(0, LOAD_PORTION)

        return view
    }

    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)
        if ((DataSaver removeObject KEY_SAVED) != null) {
            adapter = (DataSaver removeObject KEY_ADAPTER) as FriendListAdapter
        }
        FriendsListCache.listeners.add(this)
        adapter.checkFriendCache()
    }
    override fun onDestroy() {
        FriendsListCache.listeners.remove(this)
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        DataSaver.putObject(KEY_SAVED, true)
        DataSaver.putObject(KEY_ADAPTER, adapter)
    }

    override fun onDataUpdate() {
        (view.findViewById(R.id.refreshLayout) as CustomSwipeRefreshLayout).isRefreshing = false
        adapter.checkFriendCache()
    }
}