package com.ailyan.lotto;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.ailyan.lotto.databinding.ActivityChatBinding;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

public class MediaServices {
    private static final String TAG = "###";
    private static final String VIDEO_TRACK_ID = "ARDAMSv0";
    private static final int VIDEO_RESOLUTION_WIDTH = 1280;
    private static final int VIDEO_RESOLUTION_HEIGHT = 720;
    private static final int FPS = 30;

    private final AppCompatActivity context;
    private final ActivityChatBinding binding;
    private final EglBase eglBase;
    private final PeerConnectionFactory peerConnectionFactory;

    private CameraVideoCapturer localVideoCapturer;
    private VideoTrack localVideoTrack;
    private AudioTrack localAudioTrack;

    private final List<MediaStream> mediaStreams = new ArrayList<>();
    private final MutableLiveData<List<MediaStream>> mediaStreamsLiveData = new MutableLiveData<>();

    private int fromPlayer;

    public MediaServices(AppCompatActivity context, ActivityChatBinding binding, EglBase eglBase, PeerConnectionFactory peerConnectionFactory, int currentPlayer) {
        this.context = context;
        this.binding = binding;
        this.eglBase = eglBase;
        this.peerConnectionFactory = peerConnectionFactory;
        initLocalCamera();
        createLocalVideoCapturer();
        createLocalVideoTrack();
        createLocalAudioTrack();
        mediaStreamsLiveData.observe(context, mediaStreams -> {
            Log.d(TAG, "Player(" + currentPlayer + ") got player(" + fromPlayer + ") media stream");
            Log.d(TAG, "Total streams received until now are : " + mediaStreams.size());
            RemoteCamerasAdapter remoteCamerasAdapter = new RemoteCamerasAdapter(context, mediaStreams, eglBase);
            binding.remoteCameras.setAdapter(remoteCamerasAdapter);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2);
            binding.remoteCameras.setLayoutManager(gridLayoutManager);
        });
        observeOptions();
    }

    private void observeOptions() {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        OptionsViewModel optionsViewModel = new ViewModelProvider(context).get(OptionsViewModel.class);
        optionsViewModel.isVideoEnabled.observe(context, enabled -> localVideoTrack.setEnabled(enabled));
        optionsViewModel.isMicEnabled.observe(context, enabled -> localAudioTrack.setEnabled(enabled));
        optionsViewModel.isCameraSwitched.observe(context, unused -> localVideoCapturer.switchCamera(null));
        optionsViewModel.isAudioEnabled.observe(context, audioManager::setSpeakerphoneOn);
    }

    private void initLocalCamera() {
        binding.localCamera.init(eglBase.getEglBaseContext(), null);
        binding.localCamera.setEnableHardwareScaler(true);
        binding.localCamera.setMirror(true);
    }

    private CameraVideoCapturer createLocalCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                CameraVideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                CameraVideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }

    private void createLocalVideoCapturer() {
        if (Camera2Enumerator.isSupported(context)) {
            localVideoCapturer = createLocalCameraCapturer(new Camera2Enumerator(context));
        } else {
            localVideoCapturer = createLocalCameraCapturer(new Camera1Enumerator(true));
        }
    }

    private void createLocalVideoTrack() {
        VideoSource videoSource = peerConnectionFactory.createVideoSource(localVideoCapturer);
        localVideoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);
        localVideoTrack = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        localVideoTrack.setEnabled(true);
        localVideoTrack.addRenderer(new VideoRenderer(binding.localCamera));
    }

    private void createLocalAudioTrack() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        AudioSource audioSource = peerConnectionFactory.createAudioSource(mediaConstraints);
        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);
    }

    public MediaStream getLocalStreamingVideo() {
        MediaStream mediaStream = peerConnectionFactory.createLocalMediaStream("ARDAMS");
        mediaStream.addTrack(localVideoTrack);
        mediaStream.addTrack(localAudioTrack);
        return mediaStream;
    }

    public void addRemoteStreamingVideo(MediaStream mediaStream, int fromPlayer) {
        this.fromPlayer = fromPlayer;
        mediaStreams.add(mediaStream);
        mediaStreamsLiveData.postValue(mediaStreams);
    }
}