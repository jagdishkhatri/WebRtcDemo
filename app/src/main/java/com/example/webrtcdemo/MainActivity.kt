package com.example.webrtcdemo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.webrtcdemo.data.remote.RetrofitWebSocketBuilder
import com.example.webrtcdemo.socket.SocketHandler
import com.example.webrtcdemo.ui.theme.WebRtcDemoTheme
import com.example.webrtcdemo.webrtc.IceServerData
import com.example.webrtcdemo.webrtc.PeerConnectionObserver
import com.example.webrtcdemo.webrtc.RTCConfigurationData
import com.example.webrtcdemo.webrtc.RTP
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.socket.client.Socket
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

lateinit var peerConnectionObserver: PeerConnectionObserver
private lateinit var peerConnectionFactory: PeerConnectionFactory
lateinit var webSocket: WebSocket

const val sfuEndpoint = "wss://https://eval.signalling.nimbuzz.com"
const val turnEndpoint = "turn:numb.viagenie.ca"
const val turnUser = "webrtc@live.com"
const val turnPassword = "muazkh"
const val token = "ASSESSMENT_2024_SIG"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WebRtcDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Sample()
                }
            }
        }
    }
}

@Preview
@Composable
fun Sample(modifier: Modifier = Modifier) {

    val inputValue = remember {
        mutableStateOf(TextFieldValue())
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(modifier = Modifier.padding(10.dp), onClick = {
            connect()
            connectSocket()
        }) {
            Text(text = "Connect")
        }

        TextField(
            modifier = Modifier.padding(10.dp),
            value = inputValue.value,
            onValueChange = { inputValue.value = it },
            label = { Text(text = stringResource(id = R.string.app_name)) }
        )


        Button(modifier = Modifier.padding(10.dp), onClick = {
            val requestJson = JsonObject()
            requestJson.addProperty("from", token)
            requestJson.addProperty("target", "all")
            requestJson.addProperty("payload", inputValue.value.toString())

            webSocket.send(Gson().toJson(requestJson))

            SocketHandler.sendMessageToSocket(inputValue.value.toString())
        }) {
            Text(text = "Share Message")
        }
    }
}

fun connectSocket(){
    SocketHandler.setSocket()
    SocketHandler.establishConnection()
    val auth = JSONObject()
    auth.put("token", token)
    val mSocket = SocketHandler.getSocket()
    mSocket.emit("authentication",auth)
    mSocket.on(Socket.EVENT_CONNECT) { args ->
        Log.e("TAG", "connectSocket: $args" )
        if (args[0] != null) {
            val counter = args[0] as Int
            Log.e("I",counter.toString())
        }
    }
    mSocket.on(Socket.EVENT_DISCONNECT) { args ->
        Log.e("TAG", "disconnectSocket: $args" )
    }
    mSocket.on(Socket.EVENT_CONNECT_ERROR){ args ->
        Log.e("TAG", "connect Error: $args" )
        if (args[0] != null) {
            val counter = args[0] as JSONObject
            Log.e("I",counter.toString())
        }
    }
}

fun connect() {

        webSocket = RetrofitWebSocketBuilder.getWebSocket(sfuEndpoint, listener = object : WebSocketListener(){

            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)

                val peerConnection = setupPeerConnection()
                createOffer(peerConnection)
            }


            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                Log.e("TAG", "onClosing: " )
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                Log.e("TAG", "onClosed: " )
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                Log.e("TAG", "onMessage: " )
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Log.e("TAG", "onFailure: ${response.toString()}" )
            }

        })
}

fun setupPeerConnection(): PeerConnection {
    val conf: PeerConnection.RTCConfiguration = RTP.parseRTCConfiguration(
        RTCConfigurationData(
            iceServerData = listOf(
                IceServerData(url = sfuEndpoint),
                IceServerData(
                    url = turnEndpoint,
                    username = turnUser,
                    credential = turnPassword
                )
            ),
            sdpSemantics = "unified-plan"
        )
    )
    peerConnectionObserver = PeerConnectionObserver(
        onRenegotiationNeeded = { peerConnection ->
            peerConnection?.let { createOffer(it) }
        },
        onIceCandidateImpl = { candidate ->
            val internalJson = JsonObject()
            internalJson.addProperty("sdpMid", candidate.sdpMid)
            internalJson.addProperty("sdpMLineIndex", candidate.sdpMLineIndex)
            val paramJson = JsonObject()
            paramJson.add("candidate", internalJson)
            send("trickle", paramJson)
        })
    val peerConnection: PeerConnection? = peerConnectionFactory.createPeerConnection(
        conf,
        peerConnectionObserver
    )
    peerConnectionObserver.peerConnection = peerConnection
    return peerConnection!!
}

private fun createOffer(peerConnection: PeerConnection?) {
    val sdpMediaConstraints = mediaConstraints()
    peerConnection?.createOffer(
        object : SdpObserver {
            override fun onCreateFailure(s: String) {
                Log.e("peerConnectionCreateOffer: %s", s)
            }

            override fun onCreateSuccess(sdp: SessionDescription) {
                Log.e("TAG", "onCreateSuccess: " )
                peerConnection.setLocalDescription(this, sdp)
                val offerJson = JsonObject()
                offerJson.addProperty("type", sdp.type.canonicalForm())
                offerJson.addProperty("sdp", sdp.description)
                val paramJson = JsonObject()
                paramJson.addProperty("token", token)
                paramJson.add("offer", offerJson)
                send("join", paramJson)
            }

            override fun onSetFailure(s: String) {
                Log.e("TAG", "onSetFailure: " )
            }
            override fun onSetSuccess() {
                Log.e("TAG", "onSetSuccess: " )
            }
        }, sdpMediaConstraints
    )
}

private fun mediaConstraints(): MediaConstraints {
    val sdpMediaConstraints = MediaConstraints()
    sdpMediaConstraints.mandatory.add(
        MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true")
    )
    sdpMediaConstraints.mandatory.add(
        MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false")
    )
    return sdpMediaConstraints
}

fun send(event: String, params: JsonObject) {
    val requestJson = JsonObject()
    requestJson.addProperty("method", event)
    requestJson.add("params", params)
    requestJson.addProperty("id", token)

    webSocket.send(Gson().toJson(requestJson))
}

