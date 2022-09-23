package com.example.music;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<Track>songs;
    ExoPlayer player;
    ConstraintLayout playerView;

    public SongAdapter(Context context, List<Track> songs,ExoPlayer player,ConstraintLayout playerView) {
        this.context = context;
        this.songs = songs;
        this.player=player;
        this.playerView=playerView;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item,parent,false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Track track=songs.get(position);
        SongViewHolder viewHolder= (SongViewHolder) holder;

        viewHolder.titleHolder.setText(track.getTitle());
        viewHolder.durationHolder.setText(getDuration(track.getDuration()));
        viewHolder.sizeHolder.setText(getSize(track.getSize()));


        Uri artworkUri=track.getArtworkUri();

        if (artworkUri!=null){
            viewHolder.artworkHolder.setImageURI(artworkUri);


            if (viewHolder.artworkHolder.getDrawable()==null){
                viewHolder.artworkHolder.setImageResource(R.drawable.images);
            }
        }
        viewHolder.itemView.setOnClickListener(view ->{

            context.startService(new Intent(context.getApplicationContext(),PlayerService.class));

            if (!player.isPlaying()){
                player.setMediaItems(getMediaItems(),position,0);
            }else {
                player.pause();
                player.seekTo(position,0);

            }
            player.prepare();
            player.play();
            Toast.makeText(context,track.getTitle(), Toast.LENGTH_SHORT).show();

            playerView.setVisibility(View.VISIBLE);

            if (ContextCompat.checkSelfPermission(context , Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
                ((MainActivity)context).recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

    }

    private List<MediaItem> getMediaItems() {
        List<MediaItem>mediaItems=new ArrayList<>();
        for (Track track:songs){
            MediaItem mediaItem=new MediaItem.Builder()
                    .setUri(track.getUri())
                    .setMediaMetadata(getMetadata(track))
                    .build();

            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }

    private MediaMetadata getMetadata(Track track) {
        return new MediaMetadata.Builder()
                .setTitle(track.getTitle())
                .setArtworkUri(track.getArtworkUri())
                .build();
    }


    public static class SongViewHolder extends RecyclerView.ViewHolder{
        ImageView artworkHolder;
        TextView titleHolder,durationHolder,sizeHolder;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            artworkHolder=itemView.findViewById(R.id.artworkView);
            titleHolder=itemView.findViewById(R.id.titleView);
            durationHolder=itemView.findViewById(R.id.durationView);
            sizeHolder=itemView.findViewById(R.id.sizeView);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void filterSongs(List<Track>filteredList){
        songs=filteredList;
        notifyDataSetChanged();
    }
    @SuppressLint("DefaultLocale")
    private String getDuration(int totalDuration){

        String totalDurationText;

        int hrs= totalDuration/(1000*60*60);
        int min=(totalDuration%(1000*60*60))/(1000*60);
        int sec=(((totalDuration%(1000*60*60))%(1000*60*60))%(1000*600))/1000;

        if (hrs<1){
            totalDurationText=String.format("%02d:%02d",min,sec);

        }else {
            totalDurationText=String.format("%1d:%02d:%02d",hrs,min,sec);

        }
        return totalDurationText;
    }

    private String getSize(long bytes){
        String hrsize;

        double k=bytes/1024.0;
        double m=((bytes/1024.0)/1024.0);
        double g=(((bytes/1024.0)/1024.0)/1024.0);
        double t=((((bytes/1024.0)/1024.0)/1024.0)/1024.0);

        DecimalFormat dec=new DecimalFormat("0.00");
        if (t > 1) {

            hrsize=dec.format(t).concat("TB");
        }else if (g>1){
            hrsize=dec.format(g).concat("GB");

        }else if (m>1){
            hrsize=dec.format(m).concat("MB");

        }else  if (k>1){
            hrsize=dec.format(k).concat("KB");

        }else{
            hrsize=dec.format(g).concat("Bytes");

        }
        return hrsize;
    }

}
