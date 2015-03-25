package me.alexeyterekhov.vkfilter.GUI.DialogListActivity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.*
import com.getbase.floatingactionbutton.FloatingActionButton
import me.alexeyterekhov.vkfilter.Common.AppContext
import me.alexeyterekhov.vkfilter.Common.DataSaver
import me.alexeyterekhov.vkfilter.DataCache.ChatInfoCache
import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend
import me.alexeyterekhov.vkfilter.DataCache.UserCache
import me.alexeyterekhov.vkfilter.Database.DAOFilters
import me.alexeyterekhov.vkfilter.Database.VkFilter
import me.alexeyterekhov.vkfilter.Database.VkIdentifier
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.FilterGlass.FilterGlassAdapter
import me.alexeyterekhov.vkfilter.GUI.ManageFiltersActivity.ManageFiltersActivity
import me.alexeyterekhov.vkfilter.Internet.VkApi.RunFun
import me.alexeyterekhov.vkfilter.R
import java.util.LinkedList


class ActivityGlassModule(val activity: DialogListActivity) {
    companion object {
        val SAVED_KEY = "GlassModuleSaved"
        val VISIBLE_KEY = "GlassModuleVisible"
    }

    private val handler = Handler()
    private var blocked = false

    private var manageButtonPressed = false

    fun onCreate() {
        if (DataSaver removeObject SAVED_KEY != null) {
            if (DataSaver removeObject VISIBLE_KEY != null) {
                with (findMainBtn()) {
                    setVisibility(View.VISIBLE)
                    setIcon(R.drawable.icon_close)
                }
                findResetBtn() setVisibility View.VISIBLE
                findManageBtn() setVisibility View.VISIBLE
                findLayout() setVisibility View.VISIBLE
                subscribe()
            }
        }

        findMainBtn() setOnClickListener {
            when (findLayout().getVisibility()) {
                View.VISIBLE -> hide()
                else -> show()
            }
        }

        findManageBtn() setOnClickListener {
            manageButtonPressed = true
            val intent = Intent(AppContext.instance, javaClass<ManageFiltersActivity>())
            activity startActivity intent
        }

        findResetBtn() setOnClickListener {
            val list = findList()
            (list.getAdapter() as FilterGlassAdapter).resetFilters()
        }

        val filterFromDatabase = DAOFilters.loadVkFilters()

        with (findList()) {
            if (getAdapter() == null) setAdapter(
                    FilterGlassAdapter(
                        this,
                        object : DataDepend {
                            override fun onDataUpdate() {
                                activity.getDialogAdapter()!!.filterDataAgain()
                            }
                        }
                    )
            )
            if (getLayoutManager() == null) setLayoutManager(LinearLayoutManager(
                    AppContext.instance, LinearLayoutManager.VERTICAL, true
            ))
            val adapter = getAdapter() as FilterGlassAdapter
            adapter.filters.clear()
            adapter.filters addAll filterFromDatabase
            adapter.notifyDataSetChanged()
        }

        infoRequest(filterFromDatabase)
    }
    fun onDestroy() {
        unsubscribe()
    }
    fun saveState() {
        if (findLayout().getVisibility() == View.VISIBLE) {
            if (manageButtonPressed)
                manageButtonPressed = false
            else {
                DataSaver.putObject(SAVED_KEY, true)
                DataSaver.putObject(VISIBLE_KEY, true)
            }
        }
    }

    private fun subscribe() {
        val adapter = getAdapter()
        if (adapter != null) {
            UserCache.listeners add adapter
            ChatInfoCache.listeners add adapter
        }
    }
    private fun unsubscribe() {
        val adapter = getAdapter()
        if (adapter != null) {
            UserCache.listeners remove adapter
            ChatInfoCache.listeners remove adapter
        }
    }
    private fun infoRequest(filters: List<VkFilter>) {
        val userIds = filters
                .map {
                    it.identifiers()
                        .filter { it.type == VkIdentifier.TYPE_USER }
                        .map { it.id.toString() }
                        .filter { !(UserCache contains it) }}
                .fold(LinkedList<String>(), {
                    res, ids ->
                    res addAll ids
                    res})
                .distinct()
        if (userIds.isNotEmpty())
            RunFun userInfo userIds
        val chatIds = filters
                .map {
                    it.identifiers()
                        .filter { it.type == VkIdentifier.TYPE_CHAT }
                        .map { it.id.toString() }
                        .filter { !(ChatInfoCache contains it) }}
                .fold(LinkedList<String>(), {
                    res, ids ->
                    res addAll ids
                    res})
                .distinct()
        if (chatIds.isNotEmpty())
            RunFun chatInfo chatIds
    }

    fun getAdapter() = findList().getAdapter() as FilterGlassAdapter?
    private fun findMainBtn() = (activity findViewById R.id.showFilterGlass) as FloatingActionButton
    private fun findResetBtn() = (activity findViewById R.id.filterReset) as FloatingActionButton
    private fun findManageBtn() = (activity findViewById R.id.manageFiltersBtn) as FloatingActionButton
    private fun findLayout() = activity findViewById R.id.glassLayout
    private fun findList() = (activity findViewById R.id.filterList) as RecyclerView

    fun isShown() = findLayout().getVisibility() == View.VISIBLE

    fun show() {
        if (blocked)
            return
        subscribe()
        getAdapter()!!.updateVisibleAvatarLists()
        blocked = true
        val glassLayout = findLayout()
        val btn = findMainBtn()
        var revealDuration = 100L
        findList().scrollToPosition(0)
        with (glassLayout) {
            val animator = ViewAnimationUtils.createCircularReveal(
                    this,
                    btn.getLeft() + btn.getWidth() / 2,
                    btn.getTop() + btn.getHeight() / 2,
                    0f,
                    Math.max(getWidth(), getHeight()).toFloat()
            )
            revealDuration = animator.getDuration()
            setVisibility(View.VISIBLE)
            animator.start()
        }
        with (findManageBtn()) {
            val animator = ObjectAnimator.ofFloat(
                    this,
                    "x",
                    btn.getX(),
                    getX()
            )
            animator setDuration revealDuration
            animator setStartDelay revealDuration * 3 / 4
            animator setInterpolator DecelerateInterpolator(1.5f)
            setX(btn.getX())
            setVisibility(View.VISIBLE)
            animator.start()
        }
        with (findResetBtn()) {
            val animator = ObjectAnimator.ofFloat(
                    this,
                    "x",
                    btn.getX(),
                    getX()
            )
            animator setDuration revealDuration
            animator setStartDelay revealDuration
            animator setInterpolator DecelerateInterpolator(1.5f)
            setX(btn.getX())
            setVisibility(View.VISIBLE)
            animator.start()
        }
        btn.setIcon(R.drawable.icon_close)
        val to = 1.3f
        val animation = ScaleAnimation(
                1.0f, to,
                1.0f, to,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        )
        animation.setRepeatMode(Animation.REVERSE)
        animation.setRepeatCount(1)
        animation.setDuration(revealDuration / 3)
        animation.setInterpolator(AccelerateDecelerateInterpolator())
        btn.startAnimation(animation)
        handler.postDelayed({ blocked = false }, (revealDuration * 2.5).toLong())
    }

    fun hide() {
        if (blocked)
            return
        unsubscribe()
        blocked = true
        val glassLayout = findLayout()
        val btn = findMainBtn()
        var revealDuration = 100L
        with (glassLayout) {
            val animator = ViewAnimationUtils.createCircularReveal(
                    this,
                    btn.getLeft() + btn.getWidth() / 2,
                    btn.getTop() + btn.getHeight() / 2,
                    Math.max(getWidth(), getHeight()).toFloat(),
                    0f
            )
            revealDuration = animator.getDuration()
            animator addListener object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    setVisibility(View.INVISIBLE)
                }
            }
            animator.start()
        }
        with (findResetBtn()) {
            val oldX = getX()
            val animator = ObjectAnimator.ofFloat(
                    this,
                    "x",
                    getX(),
                    btn.getX()
            )
            animator setDuration revealDuration / 2
            animator setInterpolator AccelerateInterpolator()
            animator addListener object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    setVisibility(View.INVISIBLE)
                    setX(oldX)
                }
            }
            animator.start()
        }
        with (findManageBtn()) {
            val oldX = getX()
            val animator = ObjectAnimator.ofFloat(
                    this,
                    "x",
                    getX(),
                    btn.getX()
            )
            animator setDuration revealDuration / 2
            animator setStartDelay revealDuration / 4
            animator setInterpolator AccelerateInterpolator()
            animator addListener object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    setVisibility(View.INVISIBLE)
                    setX(oldX)
                }
            }
            animator.start()
        }
        btn.setIcon(R.drawable.icon_people)
        handler.postDelayed({ blocked = false }, (revealDuration * 1.5).toLong())
    }
}