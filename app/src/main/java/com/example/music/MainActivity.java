package com.example.music;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chibde.visualizer.BarVisualizer;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.jgabrielfreitas.core.BlurImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class MainActivity extends AppCompatActivity{

    RecyclerView recyclerView;
    SongAdapter songAdapter;
    List<Track>allSongs=new ArrayList<>();
    ActivityResultLauncher<String>storagePermissionLauncher;
    final String permission=Manifest.permission.READ_EXTERNAL_STORAGE;

    ExoPlayer player;
    ActivityResultLauncher<String>recordAudioPermissionLauncher;
    final String recordAudioPermission=Manifest.permission.RECORD_AUDIO;
    ConstraintLayout playerview;
    TextView PlayerCloseBtn;

    TextView songNameView,skipPreviousBtn,skipNextBtn,playPauseBtn,repeatModeBtn,playListBtn;
    TextView homeSongNameView,homeSkipPreviousBtn,homePlayPauseBtn,homeSkipNextBtn;

    ConstraintLayout homeControlWrapper,headWrapper,artWorkWrapper,seekbarWrapper,controlWrapper,audioVisualizerWrapper;

    CircleImageView artWorkView;

    SeekBar seekbar;

    TextView progressView,durationView;

    BarVisualizer audioVisualizer;


    BlurImageView blurImageView;

    int defaultStatusColor;

    int repeatMode=1;

    SearchView searchView;

    boolean isBond=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        defaultStatusColor=getWindow().getStatusBarColor();

        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor,199));

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.app_name));

        recyclerView=findViewById(R.id.recyclerview);


       storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted->{
           if (granted){

               fetchSongs();
           }
           else {
               userResponses();
           }
       });

       //storagePermissionLauncher.launch(permission);

       recordAudioPermissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(),granted->{
           if (granted && player.isPlaying()){
               activateAudiovisualizer();
           }else {
               userResponsesOnRecordAudioPerm();
           }

       });

       //player=new ExoPlayer.Builder(this).build();
       playerview=findViewById(R.id.PlayerView);
       PlayerCloseBtn=findViewById(R.id.PlayerCloseBtn);
       songNameView=findViewById(R.id.SongNameView);
       skipPreviousBtn=findViewById(R.id.skipPreviousBtn);
       skipNextBtn=findViewById(R.id.skipNextBtn);
       playPauseBtn=findViewById(R.id.playPauseBtn);
       repeatModeBtn=findViewById(R.id.repeatModeBtn);
       playListBtn=findViewById(R.id.playListBtn);


       homeSongNameView=findViewById(R.id.HomeSongNameView);
       homeSkipPreviousBtn=findViewById(R.id.HomeSkipPreviousBtn);
       homeSkipNextBtn=findViewById(R.id.HomeSkipNextBtn);
       homePlayPauseBtn=findViewById(R.id.HomePlayPauseBtn);



         homeControlWrapper=findViewById(R.id.homeControlWrapper);
         headWrapper=findViewById(R.id.headWrapper);
         artWorkWrapper=findViewById(R.id.artWorkWrapper);
         seekbarWrapper=findViewById(R.id.seekbarWrapper);
         controlWrapper=findViewById(R.id.controlWrapper);
         audioVisualizerWrapper=findViewById(R.id.audioVisualizerWrapper);


           artWorkView=findViewById(R.id.artworkView);
           seekbar=findViewById(R.id.seekbar);
           progressView=findViewById(R.id.progressView);
           durationView=findViewById(R.id.durationView);

             audioVisualizer=findViewById(R.id.visualizer);

             blurImageView=findViewById(R.id.BlurImageView);


             //playerControls();

        doBindService();




    }

    private void doBindService() {

        Intent playerServiceIntent = new Intent(this,PlayerService.class);
        bindService(playerServiceIntent,playerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    ServiceConnection playerServiceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.ServiceBinder binder=(PlayerService.ServiceBinder)iBinder;
            player=binder.getPlayerService().player;
            isBond=true;

            storagePermissionLauncher.launch(permission);
            playerControls();
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    private void playerControls() {

        songNameView.setSelected(true);
        homeSongNameView.setSelected(true);

        PlayerCloseBtn.setOnClickListener(view -> exitPlayerview());

        playListBtn.setOnClickListener(view -> exitPlayerview());

        homeControlWrapper.setOnClickListener(view -> showplayerview());

        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);

                assert mediaItem != null;
                songNameView.setText(mediaItem.mediaMetadata.title);
                homeSongNameView.setText(mediaItem.mediaMetadata.title);

                progressView.setText(getReadableTime((int)player.getCurrentPosition()));
                seekbar.setProgress((int) player.getCurrentPosition());
                seekbar.setMax((int)player.getDuration());
                durationView.setText(getReadableTime((int) player.getDuration()));
                playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_pause_circle_outline_24,0,0,0);
                homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause,0,0,0);

                showCurrentArtwork();
                updatePlayerPositionProgress();

                artWorkView.setAnimation(loadRotation());
                activateAudiovisualizer();

                updatePlayerColors();

                if (!player.isPlaying()){
                    player.play();
                }


            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);

                if (playbackState==ExoPlayer.STATE_READY){

                    songNameView.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    homeSongNameView.setText(player.getCurrentMediaItem().mediaMetadata.title);
                    progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                    durationView.setText(getReadableTime((int) player.getDuration()));
                    seekbar.setMax((int) player.getDuration());
                    seekbar.setProgress((int) player.getCurrentPosition());
                    playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_pause_circle_outline_24,0,0,0);
                    homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause,0,0,0);

                    showCurrentArtwork();
                    updatePlayerPositionProgress();

                    artWorkView.setAnimation(loadRotation());

                    activateAudiovisualizer();

                    updatePlayerColors();
                }
                else {
                    playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_play_circle_outline_24,0,0,0);
                    homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play,0,0,0);
                }
            }
        });
        skipNextBtn.setOnClickListener(view -> skipToNextsong());
        homeSkipNextBtn.setOnClickListener(view -> skipToNextsong());

        skipPreviousBtn.setOnClickListener(view -> skipToPrevioussong());
        homeSkipPreviousBtn.setOnClickListener(view -> skipToPrevioussong());

        playPauseBtn.setOnClickListener(view -> playorpauseplayer());
        homePlayPauseBtn.setOnClickListener(view -> playorpauseplayer());


        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressvalue=0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressvalue= seekBar.getProgress();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if (player.getPlaybackState()==ExoPlayer.STATE_READY){
                    seekbar.setProgress(progressvalue);
                    progressView.setText(getReadableTime(progressvalue));
                    player.seekTo(progressvalue);
                }

            }
        });

        repeatModeBtn.setOnClickListener(view -> {
            if (repeatMode==1){
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
                repeatMode=2;
                    repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_one,0,0,0);
            }
            else if (repeatMode==2){
                player.setShuffleModeEnabled(true);
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
                repeatMode=3;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_shuffle_24,0,0,0);
            }
            else if (repeatMode==3){

                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
                player.setShuffleModeEnabled(false);
                repeatMode=1;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat,0,0,0);

            }
            updatePlayerColors();
        });


    }

    private void playorpauseplayer() {
        if (player.isPlaying()){
            player.pause();
            playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_play_circle_outline_24,0,0,0);
            homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play,0,0,0);
            artWorkView.clearAnimation();
        }else{
            player.play();
            playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_pause_circle_outline_24,0,0,0);
            homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause,0,0,0);
            artWorkView.startAnimation(loadRotation());
        }

        updatePlayerColors();
    }

    private void skipToPrevioussong() {
        if (player.hasPreviousMediaItem()){
            player.seekToPrevious();
        }
    }

    private void skipToNextsong() {
        if (player.hasNextMediaItem()){
            player.seekToNext();
        }
    }

    private void updatePlayerPositionProgress() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player.isPlaying()){
                    progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                    seekbar.setProgress((int) player.getCurrentPosition());
                }
                updatePlayerPositionProgress();

            }
        },1000);
    }

    private Animation loadRotation() {
        RotateAnimation rotateAnimation=new RotateAnimation(0,360,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(1000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        return rotateAnimation;
    }

    private void showCurrentArtwork() {
        artWorkView.setImageURI(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri);

        if (artWorkView.getDrawable()==null){
            artWorkView.setImageResource(R.drawable.images);
        }
    }

    String getReadableTime(int duration) {
        String time;
        int hrs = duration / (1000 * 60 * 60);
        int min = (duration % (1000 * 60 * 60)) / (1000 * 60);
        int sec = (((duration % (1000 * 60 * 60)) % (1000 * 60 * 60)) % (1000 * 600)) / 1000;


        if (hrs < 1) {
            time = min + ":" + sec;
        } else {
            time = hrs + ":" + min + ":" + sec;
        }
        return time;
    }

    private void updatePlayerColors() {
        if (playerview.getVisibility()== View.GONE)
            return;


        BitmapDrawable bitmapDrawable= (BitmapDrawable) artWorkView.getDrawable();
        if (bitmapDrawable==null){
            bitmapDrawable= (BitmapDrawable) ContextCompat.getDrawable(this,R.drawable.images);
        }

        assert bitmapDrawable != null;
        Bitmap bmp=bitmapDrawable.getBitmap();

        blurImageView.setImageBitmap(bmp);
        blurImageView.setBlur(4);

        Palette.from(bmp).generate(palette -> {
            if (palette!=null){
                Palette.Swatch swatch = palette.getDarkVibrantSwatch();
                if (swatch==null){
                    swatch=palette.getMutedSwatch();
                    if (swatch==null){
                        swatch=palette.getDominantSwatch();
                    }
                }

                int titleTextColor=swatch.getTitleTextColor();
                int bodyTextColor=swatch.getBodyTextColor();
                int rgbColor=swatch.getRgb();


                getWindow().setStatusBarColor(rgbColor);
                getWindow().setNavigationBarColor(rgbColor);

                songNameView.setTextColor(titleTextColor);
                PlayerCloseBtn.getCompoundDrawables()[0].setTint(titleTextColor);
                progressView.setTextColor(bodyTextColor);
                durationView.setTextColor(bodyTextColor);

                repeatModeBtn.getCompoundDrawables()[0].setTint(bodyTextColor);
                skipPreviousBtn.getCompoundDrawables()[0].setTint(bodyTextColor);
                skipNextBtn.getCompoundDrawables()[0].setTint(bodyTextColor);
                playPauseBtn.getCompoundDrawables()[0].setTint(titleTextColor);
                playListBtn.getCompoundDrawables()[0].setTint(bodyTextColor);
            }

        });
    }


    private void showplayerview() {
        playerview.setVisibility(View.VISIBLE);
        updatePlayerColors();
    }

    private void exitPlayerview() {
        playerview.setVisibility(View.GONE);
        getWindow().setStatusBarColor(defaultStatusColor);
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor,199));
    }

    private void userResponsesOnRecordAudioPerm() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (shouldShowRequestPermissionRationale(recordAudioPermission)){

                new AlertDialog.Builder(this)
                        .setTitle("Requesting to show Audio Visualizer")
                        .setMessage("Allow this App to display audio visualizer when music is playing")
                        .setPositiveButton("allow", (dialogInterface, i) -> recordAudioPermissionLauncher.launch(recordAudioPermission))
                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getApplicationContext(), "you denied to show the audio visualizer", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
            else {
                Toast.makeText(getApplicationContext(), "you denied to show the audio visualizer", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void activateAudiovisualizer() {
        if (ContextCompat.checkSelfPermission(this,recordAudioPermission)!=PackageManager.PERMISSION_GRANTED){
            return;
        }

        audioVisualizer.setColor(ContextCompat.getColor(this,R.color.secondary_color));
        audioVisualizer.setDensity(10);
        audioVisualizer.setPlayer(player.getAudioSessionId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //if (player.isPlaying()){
          //  player.stop();
        doUnbindService();
        }

    private void doUnbindService() {
        if (isBond){
            unbindService(playerServiceConnection);
            isBond=false;
        }
    }
    // player.release();
    //}

    private void userResponses() {
        if (ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_GRANTED){

            fetchSongs();

        }
        else if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (shouldShowRequestPermissionRationale(permission)){

                new AlertDialog.Builder(this)
                        .setTitle("Requesting Permission")
                        .setMessage("Allow us to fetch songs on your Device")
                        .setPositiveButton("allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                storagePermissionLauncher.launch(permission);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getApplicationContext(),"ÿou denied us to show songs",Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        }
        else {
            Toast.makeText(this, "you canceled to dhow songs", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchSongs() {

        List<Track>songs=new ArrayList<>();
        Uri mediaStoreUri;

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            mediaStoreUri=MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);

        }else {
            mediaStoreUri=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        String[] projection=new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM_ID,
        };


//        String sortOrder=MediaStore.Audio.Media.DATE_ADDED + "DESC";

        String sortOrder = MediaStore.Audio.Media.DATE_ADDED;


           try(Cursor cursor = getContentResolver().query(mediaStoreUri,projection,null,null, sortOrder))
           {

            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

            while (cursor.moveToNext()){
                long id=cursor.getLong(idColumn);
                String name =cursor.getString(nameColumn);
                int duration=cursor.getInt(durationColumn);
                int size=cursor.getInt(sizeColumn);
                long albumId=cursor.getLong(albumColumn);


                Uri uri= ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id);

                Uri albumArtworkUri=ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),albumId);

                name=name.substring(0,name.lastIndexOf("."));

                Track track=new Track(name,uri,albumArtworkUri,size,duration);

                songs.add(track);
            }
            showSongs(songs);

        }
    }

    private void showSongs(List<Track> songs) {
        if (songs.size()==0){
            Toast.makeText(this,"No songs",Toast.LENGTH_SHORT).show();
            return;
        }
        allSongs.clear();
        allSongs.addAll(songs);


        String title=getResources().getString(R.string.app_name)+"-"+songs.size();
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        songAdapter=new SongAdapter(this,songs,player,playerview);
        //recyclerView.setAdapter(songAdapter);

        ScaleInAnimationAdapter scaleInAnimationAdapter=new ScaleInAnimationAdapter(songAdapter);
        scaleInAnimationAdapter.setDuration(1000);
        scaleInAnimationAdapter.setInterpolator(new OvershootInterpolator());
        scaleInAnimationAdapter.setFirstOnly(false);
        recyclerView.setAdapter(scaleInAnimationAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_btn,menu);


        MenuItem menuItem=menu.findItem(R.id.searchBtn);
        SearchView searchView= (SearchView) menuItem.getActionView();

        SearchSong(searchView);
        return super.onCreateOptionsMenu(menu);
    }

    private void SearchSong(SearchView searchView) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSongs(newText.toLowerCase());
                return true;
            }
        });
    }

    private void filterSongs(String query) {
        List<Track>filteredList= new ArrayList<>();

        if (allSongs.size()>0){
            for (Track track:allSongs){
                if (track.getTitle().toLowerCase().contains(query)){
                    filteredList.add(track);
                }
            }

            if (songAdapter!=null){
                 songAdapter.filterSongs(filteredList);
            }
        }
    }
}