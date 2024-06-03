package com.example.webrtcdemo.webrtc

import android.util.Log
import org.webrtc.*

class PeerConnectionObserver(
    val onRenegotiationNeeded: (peerConnection: PeerConnection?) -> Unit,
    val onIceCandidateImpl: (iceCandidate: IceCandidate) -> Unit
) : PeerConnection.Observer {
    private val TAG: String? = PeerConnectionObserver::class.qualifiedName

    var peerConnection: PeerConnection? = null

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        p0?.let {
            Log.d( "signaling state updated to %s", it.toString())
        }
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        p0?.let {
            Log.d( "ice connection state updated to %s", it.toString())
        }
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        Log.d( "ice connection receiving change %s", p0.toString())
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        p0?.let {
            Log.d( "ice gathering state updated to %s", it.toString())
        }
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        p0?.let {
            Log.d( "ice candidate received %s", it.toString())
            onIceCandidateImpl(it)
        }
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        p0?.let {
            it.iterator().forEach { iceCandidate ->
                Log.d("ice candidate removed %s", iceCandidate.toString())
            }
        }
    }

    override fun onAddStream(p0: MediaStream?) {}

    override fun onRemoveStream(p0: MediaStream?) {}

    override fun onDataChannel(p0: DataChannel?) {
        p0?.let {
            Log.d( "data channel received %s", it.id().toString())
        }
    }

    override fun onRenegotiationNeeded() {
        Log.d( "connection observer: onRenegotiationNeeded", "")
        this.onRenegotiationNeeded(this.peerConnection)
    }

}
