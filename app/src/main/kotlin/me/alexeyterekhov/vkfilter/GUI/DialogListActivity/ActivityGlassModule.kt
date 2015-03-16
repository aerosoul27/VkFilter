package me.alexeyterekhov.vkfilter.GUI.DialogListActivity

import me.alexeyterekhov.vkfilter.Common.DataSaver
import me.alexeyterekhov.vkfilter.R
import com.getbase.floatingactionbutton.FloatingActionButton
import android.view.View
import android.content.Intent
import me.alexeyterekhov.vkfilter.Common.AppContext
import me.alexeyterekhov.vkfilter.GUI.ManageFiltersActivity.ManageFiltersActivity
import android.support.v7.widget.RecyclerView
import me.alexeyterekhov.vkfilter.GUI.DialogListActivity.FilterGlass.FilterGlassAdapter
import me.alexeyterekhov.vkfilter.Database.DAOFilters
import android.view.ViewAnimationUtils
import android.animation.ObjectAnimator
import android.view.animation.DecelerateInterpolator
import android.animation.AnimatorListenerAdapter
import android.animation.Animator
import android.view.animation.AccelerateInterpolator
import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend
import android.support.v7.widget.LinearLayoutManager
import android.os.Handler
import android.view.animation.ScaleAnimation
import android.view.animation.Animation
import android.view.animation.AccelerateDecelerateInterpolator


class ActivityGlassModule(val activity: DialogListActivity) {
    class object {
        val SAVED_KEY = "GlassModuleSaved"
        val VISIBLE_KEY = "GlassModuleVisible"
    }

    private val handler = Handler()
    private var blocked = false

    private var manageButtonPressed = false

    fun onCreate() {
        var enableRefresh = false
        if (DataSaver removeObject SAVED_KEY != null) {
            if (DataSaver removeObject VISIBLE_KEY != null) {
                enableRefresh = true
                with (findMainBtn()) {
                    setVisibility(View.VISIBLE)
                    setIcon(R.drawable.icon_close)
                }
                findResetBtn() setVisibility View.VISIBLE
                findManageBtn() setVisibility View.VISIBLE
                findLayout() setVisibility View.VISIBLE
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
            (list.getAdapter() as FilterGlassAdapter).resetFilters(list)
        }

        with (findList()) {
            if (getAdapter() == null) setAdapter(FilterGlassAdapter(object : DataDepend {
                override fun onDataUpdate() {
                    activity.getDialogAdapter()!!.filterDataAgain()
                }
            }))
            if (getLayoutManager() == null) setLayoutManager(LinearLayoutManager(
                    AppContext.instance, LinearLayoutManager.VERTICAL, true
            ))
            val adapter = getAdapter() as FilterGlassAdapter
            adapter.filters.clear()
            adapter.filters addAll DAOFilters.loadVkFilters()
            adapter.notifyDataSetChanged()
            adapter.enableRefreshing(enableRefresh)
        }
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
        blocked = true
        val glassLayout = findLayout()
        val btn = findMainBtn()
        var revealDuration = 100L
        findList().scrollToPosition(0)
        with (getAdapter()!!) {
            enableRefreshing(true)
            notifyDataSetChanged()
        }
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
        animation.setDuration(revealDuration / 2)
        animation.setInterpolator(AccelerateDecelerateInterpolator())
        btn.startAnimation(animation)
        handler.postDelayed({ blocked = false }, (revealDuration * 2.5).toLong())
    }

    fun hide() {
        if (blocked)
            return
        blocked = true
        val glassLayout = findLayout()
        val btn = findMainBtn()
        var revealDuration = 100L
        getAdapter()!!.enableRefreshing(false)
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