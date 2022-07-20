package com.ailyan.lotto;

import androidx.annotation.NonNull;

import org.webrtc.SessionDescription;

public class CustomSessionDescription extends SessionDescription {
    public int fromPlayer;
    public int toPlayer;

    public CustomSessionDescription(Type type, String description, int fromPlayer, int toPlayer) {
        super(type, description);
        this.fromPlayer = fromPlayer;
        this.toPlayer = toPlayer;
    }

    @NonNull
    @Override
    public String toString() {
        return "CustomSessionDescription{" +
                "fromPlayer=" + fromPlayer +
                ", toPlayer=" + toPlayer +
                ", type=" + type +
                ", description='" + description + '\'' +
                '}';
    }
}
