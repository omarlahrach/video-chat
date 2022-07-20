package com.ailyan.lotto;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.webrtc.AudioTrack;
import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.util.List;

public class RemoteCamerasAdapter extends RecyclerView.Adapter<RemoteCamerasAdapter.ViewHolder> {
    private static final String TAG = "###";
    private final List<MediaStream> mediaStreams;
    private final EglBase rootEglBase;
    private final LayoutInflater mInflater;
    private RemoteCamerasAdapter.ItemClickListener mClickListener;

    public RemoteCamerasAdapter(AppCompatActivity context, List<MediaStream> mediaStreams, EglBase rootEglBase) {
        this.mInflater = LayoutInflater.from(context);
        this.mediaStreams = mediaStreams;
        this.rootEglBase = rootEglBase;
    }

    @NonNull
    @Override
    public RemoteCamerasAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_camera, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RemoteCamerasAdapter.ViewHolder holder, int position) {
        MediaStream mediaStream = mediaStreams.get(position);
        VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
        AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);
        remoteAudioTrack.setEnabled(true);
        remoteVideoTrack.setEnabled(true);
        remoteVideoTrack.addRenderer(new VideoRenderer(holder.remote_camera));
        Log.d(TAG, "The new stream gotten from player(" + (position + 1) + ") should be visible now");
    }

    @Override
    public int getItemCount() {
        return mediaStreams.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        SurfaceViewRenderer remote_camera;

        ViewHolder(View itemView) {
            super(itemView);
            remote_camera = itemView.findViewById(R.id.remote_camera);
            remote_camera.init(rootEglBase.getEglBaseContext(), null);
            remote_camera.setEnableHardwareScaler(true);
            remote_camera.setMirror(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    /*@SuppressLint("NotifyDataSetChanged")
    public void updateMediaStreamList(List<MediaStream> newMediaStreams) {
        this.mediaStreams.clear();
        this.mediaStreams.addAll(newMediaStreams);
        this.notifyDataSetChanged();
    }*/
}