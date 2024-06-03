package com.example.webrtcdemo.webrtc

import  org.webrtc.PeerConnection
import org.webrtc.PeerConnection.IceServer
import org.webrtc.PeerConnection.RTCConfiguration

data class IceServerData(val url: String, val username: String? = null, val credential: String? = null)

data class RTCConfigurationData(
    val iceServerData: List<IceServerData> = emptyList(),
    val iceTransportPolicy: String? = null,
    val bundlePolicy: String? = null,
    val rtcpMuxPolicy: String? = null,
    val iceCandidatePoolSize: Int? = null,
    val sdpSemantics: String? = null,
    val tcpCandidatePolicy: String? = null,
    val candidateNetworkPolicy: String? = null,
    val keyType: String? = null,
    val continualGatheringPolicy: String? = null,
)

object RTP {

    fun parseRTCConfiguration(rtpConfigData: RTCConfigurationData): RTCConfiguration {
        val iceServers = createIceServers(rtpConfigData.iceServerData)
        val conf = RTCConfiguration(iceServers)
        rtpConfigData.iceTransportPolicy?.let {
            when (it) {
                "all" -> conf.iceTransportsType = PeerConnection.IceTransportsType.ALL
                "relay" -> conf.iceTransportsType = PeerConnection.IceTransportsType.RELAY
                "nohost" -> conf.iceTransportsType = PeerConnection.IceTransportsType.NOHOST
                "none" -> conf.iceTransportsType = PeerConnection.IceTransportsType.NONE
            }
        }
        rtpConfigData.bundlePolicy?.let {
            when (it) {
                "balanced" -> conf.bundlePolicy = PeerConnection.BundlePolicy.BALANCED
                "max-compat" -> conf.bundlePolicy = PeerConnection.BundlePolicy.MAXCOMPAT
                "max-bundle" -> conf.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            }
        }
        rtpConfigData.rtcpMuxPolicy?.let {
            when (it) {
                "negotiate" -> conf.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.NEGOTIATE
                "require" -> conf.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            }
        }
        // FIXME: peerIdentity of type DOMString (public api)
        // FIXME: certificates of type sequence<RTCCertificate> (public api)
        rtpConfigData.iceCandidatePoolSize?.let {
            if (it > 0) {
                conf.iceCandidatePoolSize = it
            }
        }
        rtpConfigData.sdpSemantics?.let {
            when (it) {
                "plan-b" -> conf.sdpSemantics = PeerConnection.SdpSemantics.PLAN_B
                "unified-plan" -> conf.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            }
        }
        // === below is private api in webrtc ===
        rtpConfigData.tcpCandidatePolicy?.let {
            when (it) {
                "enabled" -> conf.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
                "disabled" -> conf.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
            }
        }
        rtpConfigData.candidateNetworkPolicy?.let {
            when (it) {
                "all" -> conf.candidateNetworkPolicy = PeerConnection.CandidateNetworkPolicy.ALL
                "low_cost" -> conf.candidateNetworkPolicy =
                    PeerConnection.CandidateNetworkPolicy.LOW_COST
            }
        }
        rtpConfigData.keyType?.let {
            when (it) {
                "RSA" -> conf.keyType = PeerConnection.KeyType.RSA
                "ECDSA" -> conf.keyType = PeerConnection.KeyType.ECDSA
            }
        }
        rtpConfigData.continualGatheringPolicy?.let {
            when (it) {
                "gather_once" -> conf.continualGatheringPolicy =
                    PeerConnection.ContinualGatheringPolicy.GATHER_ONCE
                "gather_continually" -> conf.continualGatheringPolicy =
                    PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            }
        }
        return conf
    }

    private fun createIceServers(iceServerData: List<IceServerData>): List<IceServer> {
        return iceServerData.map {
            if (it.username != null && it.credential != null) {
                IceServer.builder(it.url)
                    .setUsername(it.username)
                    .setPassword(it.credential).createIceServer()
            } else {
                IceServer.builder(it.url).createIceServer()
            }
        }
    }
}