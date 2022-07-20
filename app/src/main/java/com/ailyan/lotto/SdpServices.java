package com.ailyan.lotto;

import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ailyan.lotto.databinding.ActivityChatBinding;

import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SdpServices {
    private static final String TAG = "###";
    private final AppCompatActivity context;
    private final SignallingServer signallingServer;
    private final PeerConnectionFactory peerConnectionFactory;
    private final Map<Integer, PeerConnection> peerConnections;
    private final MediaServices mediaServices;
    private final int currentPlayer;
    private int newPlayer;

    public SdpServices(AppCompatActivity context, int currentPlayer, ActivityChatBinding binding) {
        this.context = context;
        this.currentPlayer = currentPlayer;
        EglBase eglBase = EglBase.create();
        PeerConnectionFactory.initializeAndroidGlobals(context, true, true, true);
        peerConnectionFactory = new PeerConnectionFactory(null);
        peerConnectionFactory.setVideoHwAccelerationOptions(eglBase.getEglBaseContext(), eglBase.getEglBaseContext());
        peerConnections = new HashMap<>();
        signallingServer = new ViewModelProvider(context).get(SignallingServer.class);
        mediaServices = new MediaServices(context, binding, eglBase, peerConnectionFactory, currentPlayer);
        start();
    }

    private void start() {
        signallingServer.getPlayersCount().observe(context, playersCount -> {
            for (int player = 1; player <= playersCount; player++) {
                if (player != currentPlayer && !peerConnections.containsKey(player)) {
                    Log.d(TAG, "----------- Player(" + player + ") joined the room -----------");
                    PeerConnection peerConnection = createPeerConnection(player);
                    peerConnection.addStream(mediaServices.getLocalStreamingVideo());
                    peerConnections.put(player, peerConnection);
                    if (playersCount == currentPlayer)
                        sendOffer(player);
                }
            }
        });
        signallingServer.getSdp().observe(context, sdp -> {
            switch (sdp.type) {
                case OFFER:
                    if (sdp.toPlayer == currentPlayer) {
                        Log.d(TAG, "Player(" + sdp.toPlayer + ") got an offer from player(" + sdp.fromPlayer + ")");
                        getOffer(sdp);
                        sendAnswer(sdp.fromPlayer);
                    }
                    break;
                case ANSWER:
                    if (sdp.toPlayer == currentPlayer) {
                        Log.d(TAG, "Player(" + sdp.toPlayer + ") got an answer from player(" + sdp.fromPlayer + ")");
                        getAnswer(sdp);
                    }
                    break;
            }
        });
        signallingServer.getIceCandidate().observe(context, customIceCandidate -> {
            if (customIceCandidate.toPlayer == currentPlayer) {
                Log.d(TAG, "Player(" + currentPlayer + ") got a candidate from player(" + customIceCandidate.fromPlayer + ")");
                PeerConnection peerConnection = peerConnections.get(customIceCandidate.fromPlayer);
                if (peerConnection != null) {
                    peerConnection.addIceCandidate(customIceCandidate);
                    Log.d(TAG, "Player(" + currentPlayer + ") added player(" + customIceCandidate.fromPlayer + ") candidate");
                }
            }
        });
    }

    private PeerConnection createPeerConnection(int player) {
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        String URL = "stun:stun.l.google.com:19302";
        iceServers.add(new PeerConnection.IceServer(URL));
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        MediaConstraints pcConstraints = new MediaConstraints();
        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {

            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                if (player != newPlayer) {
                    CustomIceCandidate customIceCandidate = new CustomIceCandidate(
                            iceCandidate.sdpMid,
                            iceCandidate.sdpMLineIndex,
                            iceCandidate.sdp,
                            currentPlayer,
                            player
                    );
                    signallingServer.sendIceCandidate(customIceCandidate);
                    newPlayer = player;
                }
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                mediaServices.addRemoteStreamingVideo(mediaStream, player);
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {

            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
            }

            @Override
            public void onRenegotiationNeeded() {
            }
        };
        Log.d(TAG, "Player(" + currentPlayer + ") created a peer connection with player(" + player + ")");
        return peerConnectionFactory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
    }

    private void sendOffer(int toPlayer) {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        PeerConnection peerConnection = peerConnections.get(toPlayer);
        if (peerConnection != null) {
            peerConnection.createOffer(new SimpleSdpObserver() {
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {
                    CustomSessionDescription customSessionDescription = new CustomSessionDescription(
                            sessionDescription.type,
                            sessionDescription.description,
                            currentPlayer,
                            toPlayer
                    );
                    signallingServer.sendSdp(customSessionDescription);
                    peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                }
            }, sdpMediaConstraints);
        }
    }

    private void sendAnswer(int toPlayer) {
        PeerConnection peerConnection = peerConnections.get(toPlayer);
        if (peerConnection != null) {
            peerConnection.createAnswer(new SimpleSdpObserver() {
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {
                    CustomSessionDescription customSessionDescription = new CustomSessionDescription(
                            sessionDescription.type,
                            sessionDescription.description,
                            currentPlayer,
                            toPlayer
                    );
                    signallingServer.sendSdp(customSessionDescription);
                    peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                }
            }, new MediaConstraints());
        }
    }

    private void getOffer(CustomSessionDescription sdp) {
        PeerConnection peerConnection = peerConnections.get(sdp.fromPlayer);
        if (peerConnection != null) {
            peerConnection.setRemoteDescription(
                    new SimpleSdpObserver(),
                    new SessionDescription(SessionDescription.Type.OFFER, sdp.description)
            );
            Log.d(TAG, "Player(" + sdp.toPlayer + ") accepted player(" + sdp.fromPlayer + ") " + sdp.type.canonicalForm());
        }
    }

    private void getAnswer(CustomSessionDescription sdp) {
        PeerConnection peerConnection = peerConnections.get(sdp.fromPlayer);
        if (peerConnection != null) {
            peerConnection.setRemoteDescription(
                    new SimpleSdpObserver(),
                    new SessionDescription(SessionDescription.Type.ANSWER, sdp.description)
            );
            Log.d(TAG, "Player(" + sdp.toPlayer + ") accepted player(" + sdp.fromPlayer + ") " + sdp.type.canonicalForm());
        }
    }

    public void end() {
        for (int player = 1; player <= peerConnections.size(); player++) {
            PeerConnection peerConnection = peerConnections.get(player);
            if (peerConnection != null) {
                peerConnection.close();
                peerConnection.dispose();
                peerConnections.remove(player);
            }
        }

        if (peerConnections.size() == 1)
            signallingServer.endRoom();

        Intent intent = new Intent(context, MainActivity.class);
        context.finishAffinity();
        context.startActivity(intent);
    }
}