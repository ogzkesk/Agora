package com.ogzkesk.agora.audio

import android.os.Handler
import android.os.Looper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.Objects

object TokenUtils {

    private val interceptor = HttpLoggingInterceptor()
    private var client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    const val APP_ID = "dd455c171eed4a1f8be5722b76b58b94"
    const val APP_CERTIFICATE = "f42317253faa42b48d7083c659a35c2e"
    const val TEST_CHANNEL_NAME = "test-channel"
    const val TEMPORARY_TOKEN =
        "007eJxTYFBYLmKaF3Foqq+ixsxZxw9yGHF88eeJtz4vkJl612U/p4MCQ0qKialpsqG5YWpqikmiYZpFUqqpuZFRkrlZkqlFkqWJRfSh9IZARgYmHnNGRgYIBPF5GEpSi0t0kzMS8/JScxgYACDCHjQ="

    fun generate(
        channelName: String,
        uid: Int,
        onGetToken: OnTokenGenCallback<String?>?
    ) {
        generate(
            channelName = channelName,
            uid = uid,
            onGetToken = { ret: String? ->
                if (onGetToken != null) {
                    runOnUiThread {
                        onGetToken.onTokenGen(ret)
                    }
                }
            },
            onError = { ret: Exception? ->
                println("Exception on token request: ${ret?.localizedMessage}")
                ret?.printStackTrace()
                if (onGetToken != null) {
                    runOnUiThread {
                        onGetToken.onTokenGen(null)
                    }
                }
            }
        )
    }

    private fun runOnUiThread(runnable: Runnable) {
        if (Thread.currentThread() === Looper.getMainLooper().thread) {
            runnable.run()
        } else {
            Handler(Looper.getMainLooper()).post(runnable)
        }
    }

    private fun generate(
        channelName: String,
        uid: Int,
        onGetToken: OnTokenGenCallback<String>?,
        onError: OnTokenGenCallback<Exception>?
    ) {
        if (channelName.isEmpty()) {
            onError?.onTokenGen(IllegalArgumentException("appId=$APP_ID, certificate=$APP_CERTIFICATE, channelName=$channelName"))
            return
        }
        val postBody = JSONObject()
        try {
            postBody.put("appId", APP_ID)
            postBody.put("appCertificate", APP_CERTIFICATE)
            postBody.put("channelName", channelName)
            postBody.put("expire", 900) // s
            postBody.put("src", "Android")
            postBody.put("ts", System.currentTimeMillis().toString() + "")
            postBody.put("type", 1) // 1: RTC Token ; 2: RTM Token
            postBody.put("uid", uid.toString() + "")
        } catch (e: JSONException) {
            onError?.onTokenGen(e)
        }

        println("requestBody -->" + postBody.toString())

        val request: Request = Request.Builder()
            .url("https://service.agora.io/toolbox-global/v1/token/generate")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(null, postBody.toString()))
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError?.onTokenGen(e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onError?.onTokenGen(IOException("Request failed with code: " + response.code + " " + response.message))
                    return
                }
                val body = response.body
                if (body != null) {
                    try {
                        body.use {
                            val jsonObject = JSONObject(it.string())
                            val data = jsonObject.optJSONObject("data")
                            val token = Objects.requireNonNull(data).optString("token")
                            onGetToken?.onTokenGen(token)
                        }
                    } catch (e: Exception) {
                        onError?.onTokenGen(e)
                    }
                }
            }
        })
    }

    fun interface OnTokenGenCallback<T> {
        fun onTokenGen(ret: T)
    }
}