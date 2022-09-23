package com.example.music;

import static androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
import android.os.IBinder;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import java.util.Objects;

public class PlayerService extends Service {

    private final IBinder serviceBinder=new ServiceBinder();

    ExoPlayer player;
    PlayerNotificationManager notificationManager;

    public class ServiceBinder extends Binder {
        public PlayerService getPlayerService(){
            return PlayerService.this;
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
      return serviceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player=new ExoPlayer.Builder(getApplicationContext()).build();

        AudioAttributes audioAttributes = new com.google.android.exoplayer2.audio.AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build();
        player.setAudioAttributes(audioAttributes,true);


          final String channelId=getResources().getString(R.string.app_name)+"Music Channel";
          final int notificationId=1111111;
          notificationManager=new PlayerNotificationManager.Builder(this,notificationId,channelId)
                  .setNotificationListener(notificationListener)
                  .setMediaDescriptionAdapter(descriptionAdapter)
                  .setChannelImportance(IMPORTANCE_HIGH)
                  .setSmallIconResourceId(R.drawable.small_icon)
                  .setChannelDescriptionResourceId(R.string.app_name)
                  .setNextActionIconResourceId(R.drawable.ic_baseline_skip_next)
                  .setPreviousActionIconResourceId(R.drawable.ic_baseline_skip_previous)
                  .setPauseActionIconResourceId(R.drawable.ic_pause)
                  .setPlayActionIconResourceId(R.drawable.ic_play)
                  .setChannelNameResourceId(R.string.app_name)
                  .build();


          notificationManager.setPlayer(player);
          notificationManager.setPriority(NotificationCompat.PRIORITY_MAX);
          notificationManager.setUseRewindAction(false);
          notificationManager.setUseFastForwardAction(false);


    }

    @Override
    public void onDestroy() {

        if (player.isPlaying())player.stop();
        notificationManager.setPlayer(null);
        player.release();
        player=null;
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }

    PlayerNotificationManager.NotificationListener notificationListener=new PlayerNotificationManager.NotificationListener() {
        @Override
        public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
            PlayerNotificationManager.NotificationListener.super.onNotificationCancelled(notificationId, dismissedByUser);
            stopForeground(true);
            if (player.isPlaying())
                player.pause();
        }

        @Override
        public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
            PlayerNotificationManager.NotificationListener.super.onNotificationPosted(notificationId, notification, ongoing);
            startForeground(notificationId,notification);
        }
    };

    PlayerNotificationManager.MediaDescriptionAdapter descriptionAdapter=new PlayerNotificationManager.MediaDescriptionAdapter() {
        @Override
        public CharSequence getCurrentContentTitle(Player player) {
            return Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title;
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {

            Intent openAppIntent=new Intent(getApplicationContext(),MainActivity.class);

            return PendingIntent.getActivity(getApplicationContext(),0,openAppIntent,PendingIntent.FLAG_IMMUTABLE| PendingIntent.FLAG_UPDATE_CURRENT);
        }

        @Nullable
        @Override
        public CharSequence getCurrentContentText(Player player) {
            return null;
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            ImageView view=new ImageView(getApplicationContext());
            view.setImageURI(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri);


            BitmapDrawable bitmapDrawable= (BitmapDrawable) view.getDrawable();
            if (bitmapDrawable==null){
                bitmapDrawable= (BitmapDrawable) ContextCompat.getDrawable(getApplicationContext(),R.drawable.images);
            }
            assert bitmapDrawable != null;
            return bitmapDrawable.getBitmap();

        }
    };
}