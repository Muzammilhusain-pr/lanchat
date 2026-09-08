package com.example.lanchat.call

import android.content.Context
import com.example.lanchat.network.SignalingClient
import com.example.lanchat.network.WireMessage
import org.webrtc.*

/**
 * Thin wrapper around Google's WebRTC Android library for the audio/video
 * call feature. Because caller and callee are always on the same local
 * network here, ICE candidates resolve to LAN addresses directly -- there
 * is no need for a STUN server (to discover a public IP) or a TURN server
 * (to relay when direct connection fails). The only external dependency
 * this class has is [SignalingClient], which carries the offer/answer/ICE
 * exchange over the same local WebSocket used for chat.
 *
 * This is a skeleton: PeerConnectionFactory setup, SDP creation and the
 * actual local/remote video renderers still need to be wired up. Left as
 * TODOs with the shape of what goes where, since this is the single most
 * involved piece of the whole app.
 */
class WebRtcClient(
    private val context: Context,
    private val signalingClient: SignalingClient,
    private val myPeerId: String,
    private val remotePeerId: String
) {

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null

    // No STUN/TURN servers -- empty ICE server list is correct for a pure LAN call.
    private val iceServers: List<PeerConnection.IceServer> = emptyList()

    fun initialize() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()

        // TODO: create local audio/video tracks from camera + mic capturers,
        // create the PeerConnection with `iceServers`, and register an
        // Observer that forwards local ICE candidates to signalingClient.send(
        //   WireMessage(type = MessageType.CALL_ICE_CANDIDATE, ...)
        // )
    }

    /** Start an outgoing call: create an SDP offer and send it over signaling. */
    fun startCall(isVideoCall: Boolean) {
        // TODO: peerConnection.createOffer(...), setLocalDescription(...), then
        // signalingClient.send(WireMessage(type = MessageType.CALL_OFFER, sdp = ..., toPeerId = remotePeerId, isVideoCall = isVideoCall))
    }

    /** Handle an incoming CALL_OFFER: set remote description, create answer, send it back. */
    fun handleOffer(message: WireMessage) {
        // TODO: peerConnection.setRemoteDescription(...) then createAnswer(...),
        // send back WireMessage(type = MessageType.CALL_ANSWER, sdp = ..., toPeerId = message.fromPeerId)
    }

    /** Handle an incoming CALL_ANSWER after we sent an offer. */
    fun handleAnswer(message: WireMessage) {
        // TODO: peerConnection.setRemoteDescription(...)
    }

    /** Handle an incoming CALL_ICE_CANDIDATE from the remote peer. */
    fun handleRemoteIceCandidate(message: WireMessage) {
        // TODO: peerConnection.addIceCandidate(...)
    }

    fun hangUp() {
        signalingClient.send(
            WireMessage(
                type = com.example.lanchat.network.MessageType.CALL_HANGUP,
                fromPeerId = myPeerId,
                toPeerId = remotePeerId
            )
        )
        peerConnection?.close()
        peerConnection = null
    }

    fun release() {
        peerConnection?.close()
        peerConnectionFactory?.dispose()
    }
}
