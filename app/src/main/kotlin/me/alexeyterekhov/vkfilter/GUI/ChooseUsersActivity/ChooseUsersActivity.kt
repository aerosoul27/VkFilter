package me.alexeyterekhov.vkfilter.GUI.ChooseUsersActivity

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.astuetz.PagerSlidingTabStrip
import me.alexeyterekhov.vkfilter.Database.DAOFilters
import me.alexeyterekhov.vkfilter.Database.VkIdentifier
import me.alexeyterekhov.vkfilter.GUI.Common.VkActivity
import me.alexeyterekhov.vkfilter.R
import java.util.*


public class ChooseUsersActivity: VkActivity() {
    companion object {
        val KEY_FILTER_ID = "ChooseUsersActivityFilterId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_users)
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val filterId = intent.getLongExtra(KEY_FILTER_ID, -1)
        val filter = DAOFilters loadVkFilterById filterId
        filter.invalidateCache()
        val ids = filter.identifiers()
        val selectedUsers = HashSet<Long>()
        val selectedChats = HashSet<Long>()
        for (vkId in ids)
            when (vkId.type) {
                VkIdentifier.TYPE_USER -> selectedUsers.add(vkId.id)
                VkIdentifier.TYPE_CHAT -> selectedChats.add(vkId.id)
            }

        val tabs = findViewById(R.id.tabs) as PagerSlidingTabStrip
        tabs.setTextColorResource(R.color.font_light)
        with (findViewById(R.id.pager) as ViewPager) {
            adapter = PagerAdapter(
                    fm = supportFragmentManager,
                    selectedUsers = selectedUsers,
                    selectedChats = selectedChats
            )
            if (ids.isEmpty())
                currentItem = 1
            tabs.setViewPager(this)
        }

        findViewById(R.id.saveFilterButton).setOnClickListener {
            for (vkId in ids)
                vkId.delete()
            filter.invalidateCache()
            for (u in selectedUsers) {
                val vkId = VkIdentifier()
                with (vkId) {
                    id = u
                    type = VkIdentifier.TYPE_USER
                    ownerFilter = filter
                    save()
                }
            }
            for (c in selectedChats) {
                val vkId = VkIdentifier()
                with (vkId) {
                    id = c
                    type = VkIdentifier.TYPE_CHAT
                    ownerFilter = filter
                    save()
                }
            }
            onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}