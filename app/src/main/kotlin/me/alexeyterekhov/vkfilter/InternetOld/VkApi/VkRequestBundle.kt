package me.alexeyterekhov.vkfilter.InternetOld.VkApi

import android.os.Bundle
import com.vk.sdk.api.VKParameters

class VkRequestBundle (
        val vkFun: VkFun,
        val vkParams: VKParameters,
        val additionalParams: Bundle = Bundle()
)