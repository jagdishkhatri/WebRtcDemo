package com.example.webrtcdemo.data.remote

import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.http.BuildConfig
import java.util.concurrent.TimeUnit

object RetrofitWebSocketBuilder {
    fun getWebSocket(sfuEndpoint: String, listener: WebSocketListener) : WebSocket{
        val loggingInterceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
        val client = builder.build()
        val request = okhttp3.Request
            .Builder()
            .url(sfuEndpoint)
            .build()
        return client.newWebSocket(request, listener)
    }

}