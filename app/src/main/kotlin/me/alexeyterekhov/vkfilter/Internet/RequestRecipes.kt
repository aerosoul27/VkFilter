package me.alexeyterekhov.vkfilter.Internet

import android.util.Log
import com.vk.sdk.api.VKError
import com.vk.sdk.api.VKRequest
import com.vk.sdk.api.VKResponse
import me.alexeyterekhov.vkfilter.Internet.Requests.Request
import me.alexeyterekhov.vkfilter.Util.Chef
import me.alexeyterekhov.vkfilter.Util.Recipe
import org.json.JSONObject

public object RequestRecipes {
    private val MAX_WAIT_FOR_RESPONSE = 5000
    val LOG_TAG = "VkRequest"

    public val foregroundRecipe: Recipe<Request, JSONObject> = requestBase()
            .maxCookAttempts(Chef.UNLIMITED_ATTEMPTS)
            .ifCookingFail(Chef.COOK_AGAIN_AFTER_OTHERS)
            .waitAfterCookingFail(500)
            .create()

    public val backgroundRecipe: Recipe<Request, JSONObject> = requestBase()
            .maxCookAttempts(Chef.UNLIMITED_ATTEMPTS)
            .ifCookingFail(Chef.COOK_AGAIN_AFTER_OTHERS)
            .waitAfterCookingFail(500)
            .create()

    public val backgroundOrderedRecipe: Recipe<Request, JSONObject> = requestBase()
            .maxCookAttempts(Chef.UNLIMITED_ATTEMPTS)
            .ifCookingFail(Chef.COOK_AGAIN_RIGHT_NOW)
            .waitAfterCookingFail(500)
            .create()

    private fun requestBase() = Chef.createRecipe<Request, JSONObject>()
            .cookThisWay(cook())
            .serveThisWay(serve())
            .cleanUpThisWay(cleanUp())

    private fun cook(): (Request) -> JSONObject = { request: Request ->
        if (!request.allowExecuteRequest()) {
            JSONObject()
        } else {
            var gotResponse = false
            var gotError = false
            var response: VKResponse? = null

            val listener = object : VKRequest.VKRequestListener() {
                var canceled = false
                override fun onComplete(resp: VKResponse?) {
                    if (!canceled) {
                        response = resp
                        gotResponse = true
                    }
                }

                override fun onError(err: VKError?) {
                    if (!canceled) {
                        super<VKRequest.VKRequestListener>.onError(err)
                        val message = when {
                            err != null && err.apiError != null -> "Vk api error: ${err.apiError.toString()}"
                            err != null && err.errorMessage != null -> "Vk error: ${err.errorMessage}"
                            else -> "Vk error"
                        }
                        Log.d(LOG_TAG, message)
                        gotError = true
                    }
                }

                fun cancel() {
                    canceled = true
                }
            }

            // Wait to follow request-per-second restrictions
            RequestFrequencyControl.waitNext()

            // Start request
            val vkRequest = VKRequest(request.getServerFunName(), request.getParameters())
            vkRequest.attempts = 1
            vkRequest.executeWithListener(listener)
            Log.d(LOG_TAG, "Start [${request.getServerFunName()}]")

            // Waiting while complete
            val sleepTime = 50L
            val maxCount = MAX_WAIT_FOR_RESPONSE / sleepTime
            var count = 0
            while (!gotResponse && !gotError && count < maxCount) {
                Thread.sleep(sleepTime)
                ++count
            }

            // If isn't done in 5 sec - try again
            if (!gotResponse && !gotError) {
                vkRequest.cancel()
                listener.cancel()
                throw Exception()
            }

            if (gotError || response == null || response!!.json == null) {
                throw Exception()
            } else response!!.json
        }
    }

    private fun serve(): (Request, JSONObject) -> Unit = { request, json ->
        Log.d(LOG_TAG, "Done [${request.getServerFunName()}]")
        if (request.allowExecuteRequest())
            request.handleResponse(json)
    }

    private fun cleanUp(): (Request, Exception) -> Unit = { request, exception ->
        Log.d(LOG_TAG, "Error at [${request.getServerFunName()}]")
    }
}