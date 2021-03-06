package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.TypedValue
import android.view.View
import com.rockerhieu.emojicon.EmojiconGridFragment
import com.rockerhieu.emojicon.EmojiconsFragment
import com.rockerhieu.emojicon.emoji.Emojicon
import me.alexeyterekhov.vkfilter.R

class EmojiconModule(val activity: ChatActivity) :
        EmojiconsFragment.OnEmojiconBackspaceClickedListener,
        EmojiconGridFragment.OnEmojiconClickedListener
{
    val PORTRAIT_HEIGHT = 250f
    val LANDSCAPE_HEIGHT = 130f
    val DELAY_BEFORE_OPEN = 300L
    val KEY_MODULE_OPENED = "emoji_open"
    private val closeEmojiPanelAction: (() -> Unit) = {
        hidePanel()
        Handler().postDelayed({
            activity.editPanelModule.getEditText().allowKeyboardAndShow(true)
            activity.editPanelModule.unbindSendButton(animate = true)
        }, DELAY_BEFORE_OPEN)
    }

    fun onCreate(saved: Bundle?) {
        if (saved != null && saved.containsKey(KEY_MODULE_OPENED)) {
            activity.editPanelModule.getEditText().denyKeyboard()
            bindForClosingSmiles(animate = false)
            showPanel(animate = false)
        }
    }
    fun onSaveState(bundle: Bundle?) {
        if (bundle != null) {
            if (isEmojiconPanelShown())
                bundle.putBoolean(KEY_MODULE_OPENED, true)
        }
    }

    fun openEmojiconPanel() {
        if (!isEmojiconPanelShown()) {
            activity.editPanelModule.getEditText().denyKeyboard()
            Handler().postDelayed({
                showPanel(animate = true)
                bindForClosingSmiles(animate = true)
            }, DELAY_BEFORE_OPEN)
        } else {
            bindForClosingSmiles(animate = true)
        }
    }
    fun bindForClosingSmiles(animate: Boolean = false) {
        activity.editPanelModule.bindSendButton(
                iconRes = R.drawable.button_close,
                action = closeEmojiPanelAction,
                animate = animate
        )
    }

    private fun hidePanel() {
        val panel = activity.findViewById(R.id.emoji_container)
        val fragment = activity.findViewById(R.id.emoji_fragment)

        val animator = ValueAnimator.ofFloat(getHeight(), 0f)
        animator.addUpdateListener { animator ->
            val curHeight = animator.animatedValue as Float
            val params = panel.layoutParams
            params.height = curHeight.toInt()
            panel.layoutParams = params
        }
        animator.interpolator = FastOutSlowInInterpolator()
        animator.setDuration(150)
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator) {
                panel.visibility = View.GONE
            }
        })
        fragment.visibility = View.GONE
        animator.start()
    }
    private fun showPanel(animate: Boolean = false) {
        val panel = activity.findViewById(R.id.emoji_container)
        val fragment = activity.findViewById(R.id.emoji_fragment)

        if (animate) {
            fragment.visibility = View.GONE
            val animator = ValueAnimator.ofFloat(0f, getHeight())
            animator.addUpdateListener { animator ->
                val curHeight = animator.animatedValue as Float
                val params = panel.layoutParams
                params.height = curHeight.toInt()
                panel.layoutParams = params
            }
            animator.setInterpolator(FastOutSlowInInterpolator())
            animator.setDuration(150)
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator) {
                    fragment.setVisibility(View.VISIBLE)
                }
            })
            panel.visibility = View.VISIBLE
            animator.start()
        } else {
            val params = panel.layoutParams
            params.height = getHeight().toInt()
            panel.layoutParams = params
            panel.visibility = View.VISIBLE
            fragment.visibility = View.VISIBLE
        }
    }
    fun isEmojiconPanelShown() = activity.findViewById(R.id.emoji_container).visibility == View.VISIBLE

    fun getHeight(): Float {
        val defaultDisplay = activity.windowManager.defaultDisplay
        val isLandscape = defaultDisplay.width > defaultDisplay.height
        val displayMetrics = activity.resources.displayMetrics
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                if (isLandscape) LANDSCAPE_HEIGHT else PORTRAIT_HEIGHT,
                displayMetrics
        )
    }

    override fun onEmojiconBackspaceClicked(p0: View?) = EmojiconsFragment.backspace(activity.editPanelModule.getEditText())
    override fun onEmojiconClicked(emoji: Emojicon?) = EmojiconsFragment.input(activity.editPanelModule.getEditText(), emoji)
}