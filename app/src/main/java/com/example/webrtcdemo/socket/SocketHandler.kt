package com.example.webrtcdemo.socket

import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketHandler {

    lateinit var mSocket: Socket

    @Synchronized
    fun setSocket() {
        try {
            val opts = IO.Options()
            opts.query = "token=ASSESSMENT_2024_SIG"
            mSocket = IO.socket("https://eval.signalling.nimbuzz.com",opts)
        } catch (e: URISyntaxException) {

        }
    }

    @Synchronized
    fun getSocket(): Socket {
        return mSocket
    }

    @Synchronized
    fun establishConnection() {
        mSocket.connect()
    }

    @Synchronized
    fun closeConnection() {
        mSocket.disconnect()
    }

    @Synchronized
    fun sendMessageToSocket(message: String){
        try {
            mSocket.send(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}