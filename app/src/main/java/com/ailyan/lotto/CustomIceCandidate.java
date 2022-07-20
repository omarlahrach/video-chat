package com.ailyan.lotto;

import androidx.annotation.NonNull;

import org.webrtc.IceCandidate;

public class CustomIceCandidate extends IceCandidate {
    public int fromPlayer;
    public int toPlayer;

    public CustomIceCandidate(String sdpMid, int sdpMLineIndex, String sdp, int fromPlayer, int toPlayer) {
        super(sdpMid, sdpMLineIndex, sdp);
        this.fromPlayer = fromPlayer;
        this.toPlayer = toPlayer;
    }

    @NonNull
    @Override
    public String toString() {
        return "CustomIceCandidate{" +
                "fromPlayer=" + fromPlayer +
                ", toPlayer=" + toPlayer +
                ", sdpMid='" + sdpMid + '\'' +
                ", sdpMLineIndex=" + sdpMLineIndex +
                ", sdp='" + sdp + '\'' +
                '}';
    }
}
