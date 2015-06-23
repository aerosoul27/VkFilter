package me.alexeyterekhov.vkfilter.GUI.ChatActivity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import io.codetail.animation.SupportAnimator
import me.alexeyterekhov.vkfilter.GUI.Common.SwipeOpener
import me.alexeyterekhov.vkfilter.GUI.SettingsActivity.Settings
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.FileUtils

class SwipePanelModule(val activity: ChatActivity) {
    private var isOpened = false
    val KEY_PANEL_OPENED = "swipe_panel_opened"

    private var demonstratePanel = false
    private val OPENINGS_COUNT = 5

    val closeSwipePanelAction: (() -> Unit) = {
        if (activity.emojiconModule.isEmojiconPanelShown())
            activity.emojiconModule.bindForClosingSmiles(animate = true)
        else
            activity.editPanelModule.unbindSendButton(animate = true)
        hidePanel()
    }

    fun onCreate(saved: Bundle?) {
        if (saved == null) {
            demonstratePanel = true
        }

        if (saved != null && saved containsKey KEY_PANEL_OPENED) {
            bindForClosingSwipePanel(animate = false)
            showPanel(animate = false)
        }
        activity.findViewById(R.id.swipeOpener) as SwipeOpener setListener object : SwipeOpener.OpenerListener {
            override fun onOpen() {
                if (!isPanelShown()) {
                    showPanel(animate = true)
                    Handler().postDelayed({ bindForClosingSwipePanel(animate = true) }, 100)
                    val cur = Settings.getAttachmentsOpenings()
                    if (cur < OPENINGS_COUNT)
                        Settings.setAttachmentsOpenings(cur + 1)
                }
            }
        }
        activity.findViewById(R.id.smileButton) setOnClickListener {
            activity.emojiconModule.openEmojiconPanel()
            hidePanel()
        }
        activity.findViewById(R.id.photoButton) setOnClickListener {
            MaterialDialog.Builder(activity)
                    .title(R.string.a_chat_add_photo)
                    .items(R.array.a_chat_photo_dialog)
                    .itemsCallback(MaterialDialog.ListCallback { dialog, view, which, text ->
                        when (which) {
                            0 -> callCamera()
                            1 -> callGallery()
                        }
                    })
                    .titleColorRes(R.color.my_primary)
                    .contentColorRes(R.color.my_black)
                    .backgroundColorRes(R.color.my_white)
                    .show();
        }
    }
    fun onResume() {
        if (demonstratePanel) {
            demonstratePanel = false
            if (Settings.getAttachmentsOpenings() < OPENINGS_COUNT)
                Handler().postDelayed({ demonstratePanel() }, 500)
        }
    }
    fun onSaveState(bundle: Bundle?) {
        if (bundle != null) {
            if (isPanelShown())
                bundle.putBoolean(KEY_PANEL_OPENED, true)
        }
    }

    fun isPanelShown() = isOpened
    fun hidePanel() {
        val panel = activity.findViewById(R.id.attachmentsLayout)
        val bar = activity.findViewById(R.id.bar)
        isOpened = false

        val animator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(
                panel,
                0,
                bar.getHeight() / 2,
                bar.getWidth() * 1.05f,
                0f
        )
        animator setDuration 200
        animator addListener object : SupportAnimator.AnimatorListener {
            override fun onAnimationEnd() {
                panel.setVisibility(View.INVISIBLE)
            }
            override fun onAnimationStart() {}
            override fun onAnimationCancel() {}
            override fun onAnimationRepeat() {}
        }
        animator.start()
    }
    fun showPanel(animate: Boolean = false) {
        val panel = activity.findViewById(R.id.attachmentsLayout)

        if (!animate) {
            panel setVisibility View.VISIBLE
            isOpened = true
        } else {
            val bar = activity.findViewById(R.id.bar)
            val animator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(
                    panel,
                    0,
                    bar.getHeight() / 2,
                    0f,
                    bar.getWidth() * 1.05f
            )
            animator setDuration 300
            Handler().postDelayed({ isOpened = true }, 300)
            panel setVisibility View.VISIBLE
            animator.start()
        }
    }
    fun demonstratePanel() {
        val panel = activity.findViewById(R.id.attachmentsLayout)
        val smileButton = panel findViewById R.id.smileButton
        val bar = activity.findViewById(R.id.bar)

        val animator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(
                panel,
                0,
                bar.getHeight() / 2,
                0f,
                (smileButton.getLeft() + smileButton.getWidth()).toFloat()
        )
        animator setDuration 250
        panel setVisibility View.VISIBLE
        animator.addListener(object : SupportAnimator.AnimatorListener {
            override fun onAnimationEnd() {
                val secondAnimator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(
                        panel,
                        0,
                        bar.getHeight() / 2,
                        (smileButton.getLeft() + smileButton.getWidth()).toFloat(),
                        0f
                )
                secondAnimator setDuration 250
                secondAnimator addListener object : SupportAnimator.AnimatorListener {
                    override fun onAnimationEnd() {
                        panel setVisibility View.INVISIBLE
                    }
                    override fun onAnimationRepeat() {}
                    override fun onAnimationCancel() {}
                    override fun onAnimationStart() {}
                }
                secondAnimator.start()
            }
            override fun onAnimationRepeat() {}
            override fun onAnimationCancel() {}
            override fun onAnimationStart() {}
        })
        animator.start()
    }

    fun bindForClosingSwipePanel(animate: Boolean = false) {
        activity.editPanelModule.bindSendButton(
                iconRes = R.drawable.button_close_white,
                action = closeSwipePanelAction,
                animate = animate
        )
    }

    private fun callGallery() {
        val pickPhotoIntent = Intent()
        pickPhotoIntent setType "image/*"
        pickPhotoIntent setAction Intent.ACTION_GET_CONTENT
        pickPhotoIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        activity.startActivityForResult(Intent.createChooser(pickPhotoIntent, activity.getString(R.string.a_chat_choose_photo)), UploadModule.CODE_CHOOSE_IMAGES)
    }

    private fun callCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Check is intent safe for invoking
        if (cameraIntent.resolveActivity(activity.getPackageManager()) != null) {
            val file = FileUtils.createFileForPhoto()
            if (file != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file))
                activity.uploadModule.cameraFile = file
                activity.startActivityForResult(cameraIntent, UploadModule.CODE_CAMERA)
            }
        }
    }
}