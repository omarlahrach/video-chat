package com.ailyan.lotto;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignallingServer extends AndroidViewModel {
    private static final String TAG = "###";
    private final FirebaseFirestore db;
    private final MutableLiveData<Integer> currentPlayer = new MutableLiveData<>();
    private final MutableLiveData<Integer> playersCount = new MutableLiveData<>();
    private final MutableLiveData<CustomSessionDescription> sdp = new MutableLiveData<>();
    private final MutableLiveData<CustomIceCandidate> iceCandidate = new MutableLiveData<>();

    public SignallingServer(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        db.enableNetwork();
    }

    public LiveData<Integer> addPlayer() {
        db.collection("room")
                .document("players")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            db.collection("room")
                                    .document("players")
                                    .update("count", FieldValue.increment(1))
                                    .addOnSuccessListener(unused ->
                                            db.collection("room")
                                                    .document("players")
                                                    .get().addOnSuccessListener(snapshot -> {
                                                        if (snapshot != null && snapshot.exists()) {
                                                            Long currentPlayer = snapshot.getLong("count");
                                                            if (currentPlayer != null)
                                                                this.currentPlayer.postValue(Math.toIntExact(currentPlayer));
                                                        }
                                                    }));
                        }
                        else {
                            Map<String, Integer> players = new HashMap<>();
                            players.put("count", 1);
                            db.collection("room")
                                    .document("players")
                                    .set(players)
                                    .addOnSuccessListener(unused -> this.currentPlayer.postValue(1));
                        }
                    }
                });
        return currentPlayer;
    }

    public LiveData<Integer> getPlayersCount() {
        db.collection("room").document("players")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null)
                        return;
                    if (snapshot != null && snapshot.exists()) {
                        Long longPlayersCount = snapshot.getLong("count");
                        if (longPlayersCount != null) {
                            int playersCount = longPlayersCount.intValue();
                            this.playersCount.postValue(playersCount);
                        }
                    }
                });
        return playersCount;
    }

    public void sendSdp(CustomSessionDescription sdp) {
        Map<String, Object> sdpMap = new HashMap<>();
        sdpMap.put("sdpType", sdp.type.canonicalForm());
        sdpMap.put("sdpDesc", sdp.description);
        sdpMap.put("fromPlayer", sdp.fromPlayer);
        sdpMap.put("toPlayer", sdp.toPlayer);
        db.collection("room").document("sdp").set(sdpMap)
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "Player(" + sdp.fromPlayer + ") sent an " + sdp.type.canonicalForm() + " to player(" + sdp.toPlayer + ")")
                );
    }

    public LiveData<CustomSessionDescription> getSdp() {
        db.collection("room").document("sdp")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null)
                        return;
                    if (snapshot != null && snapshot.exists()) {
                        Map<String, Object> data = snapshot.getData();
                        if (data != null) {
                            String stringSdpType = String.valueOf(data.get("sdpType"));
                            String sdpDesc = String.valueOf(data.get("sdpDesc"));
                            Long longFromPlayer = snapshot.getLong("fromPlayer");
                            Long longToPlayer = snapshot.getLong("toPlayer");
                            int fromPlayer, toPlayer;
                            if (longFromPlayer != null && longToPlayer != null) {
                                fromPlayer = Math.toIntExact(longFromPlayer);
                                toPlayer = Math.toIntExact(longToPlayer);
                                CustomSessionDescription.Type sdpType;
                                if (stringSdpType.equals("offer"))
                                    sdpType = CustomSessionDescription.Type.OFFER;
                                else
                                    sdpType = CustomSessionDescription.Type.ANSWER;
                                CustomSessionDescription sdp = new CustomSessionDescription(sdpType, sdpDesc, fromPlayer, toPlayer);
                                this.sdp.postValue(sdp);
                            }
                        }
                    }
                });
        return sdp;
    }

    public void sendIceCandidate(CustomIceCandidate customIceCandidate) {
        Map<String, Object> candidate = new HashMap<>();
        candidate.put("sdpMLineIndex", customIceCandidate.sdpMLineIndex);
        candidate.put("sdpMid", customIceCandidate.sdpMid);
        candidate.put("sdp", customIceCandidate.sdp);
        candidate.put("fromPlayer", customIceCandidate.fromPlayer);
        candidate.put("toPlayer", customIceCandidate.toPlayer);
        db.collection("room")
                .document("candidates")
                .set(candidate)
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "Player(" + customIceCandidate.fromPlayer + ") sent the candidate to player(" + customIceCandidate.toPlayer + ")")
                );
    }

    public LiveData<CustomIceCandidate> getIceCandidate() {
        db.collection("room").document("candidates")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null)
                        return;
                    if (snapshot != null && snapshot.exists()) {
                        Map<String, Object> data = snapshot.getData();
                        if (data != null) {
                            String sdpMid = String.valueOf(data.get("sdpMid"));
                            String sdp = String.valueOf(data.get("sdp"));
                            Long sdpMLineIndex = snapshot.getLong("sdpMLineIndex");
                            Long longFromPlayer = snapshot.getLong("fromPlayer");
                            Long longToPlayer = snapshot.getLong("toPlayer");
                            int fromPlayer, toPlayer;
                            if (sdpMLineIndex != null && longFromPlayer != null && longToPlayer != null) {
                                fromPlayer = Math.toIntExact(longFromPlayer);
                                toPlayer = Math.toIntExact(longToPlayer);
                                CustomIceCandidate customIceCandidate = new CustomIceCandidate(
                                        sdpMid,
                                        Math.toIntExact(sdpMLineIndex),
                                        sdp,
                                        fromPlayer,
                                        toPlayer);
                                this.iceCandidate.postValue(customIceCandidate);
                            }
                        }
                    }
                });
        return iceCandidate;
    }

    public void endRoom() {
        Map<String, Integer> playersMap = new HashMap<>();
        playersMap.put("count", 0);

        Map<String, Object> sdpMap = new HashMap<>();
        sdpMap.put("sdpType", "");
        sdpMap.put("sdpDesc", "");
        sdpMap.put("fromPlayer", 0);
        sdpMap.put("toPlayer", 0);

        Map<String, Object> candidate = new HashMap<>();
        candidate.put("sdpMLineIndex", 0);
        candidate.put("sdpMid", "");
        candidate.put("sdp", "");
        candidate.put("fromPlayer", 0);
        candidate.put("toPlayer", 0);

        db.collection("room")
                .document("players")
                .set(playersMap);
        db.collection("room")
                .document("candidates")
                .set(candidate);
        db.collection("room")
                .document("sdp")
                .set(sdpMap);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
