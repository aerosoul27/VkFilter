package me.alexeyterekhov.vkfilter.Internet.VkApi

import android.util.Log
import me.alexeyterekhov.vkfilter.Internet.VkSdkInitializer
import java.util.Collections

public object VkRequestControl {
    private fun checkSdkInitialized() {
        if (VkSdkInitializer.isNull())
            VkSdkInitializer.init()
    }

    public fun addRequest(request: VkRequestBundle) {
        checkSdkInitialized()
        Log.d(VkTask.LOG_TAG, ">>> Add request [${VkFunNames.name(request.vkFun)}]")
        VkTask.instance.handle(Collections.singleton(request))
    }

    public fun addUnstoppableRequest(request: VkRequestBundle) {
        checkSdkInitialized()
        Log.d(VkTask.LOG_TAG, ">>> Add unstoppable request [${VkFunNames.name(request.vkFun)}]")
        VkTask.unstoppableInstance.handle(Collections.singleton(request))
    }

    public fun pause() {
        val task = VkTask.instance
        if (task.isRunning() && !task.willPause())
            task.pauseAfterCurrentSource()
    }

    public fun resume() {
        checkSdkInitialized()
        val task = VkTask.instance
        if (!task.isRunning() || task.willPause())
            task.resumeHandling()
    }
}