package com.example.music_player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

public class MyMusicPlayerService extends Service {
    MediaPlayer myMediaPlayer;
    int curPositionOfSong;
    ArrayList<AudioModel> allsongs;
    MyServiceBinder myServiceBinder = new MyServiceBinder();
    boolean play_status;
    public static final String MUSIC_COMPLETE = "MusicCompleted";

    public class MyServiceBinder extends Binder {
        public MyMusicPlayerService getService() {
            return MyMusicPlayerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        Log.d("raj", "onBind called");
        return myServiceBinder;
    }

    @Override
    public void onCreate() {
//        Log.d("raj", "onCreate myMusicService called");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d("raj", "onStartCommand is called");
        int songPos = intent.getIntExtra("song_pos", curPositionOfSong);

        curPositionOfSong = songPos;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createMyNotification(songPos);
        } else {
            startForeground(123, new Notification());
        }


        return START_NOT_STICKY;
    }

    public void createMyNotification(int songPos) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        String NOTIFICATION_CHANNEL_ID = "PLAY_SONG_IN_FOREGROUND";
        String channelName = "Plays song in foreground";
        NotificationChannel myChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        myChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(myChannel);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder
                .setSmallIcon(R.drawable.myicons)
                .setContentTitle(allsongs.get(songPos).getaAlbum())
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .build();
        startForeground(123, notification);
    }


    public void play() {
        myMediaPlayer.start();
        play_status = true;

    }

    public void pause() {
        myMediaPlayer.pause();
        play_status = false;
    }

    public void stop() {
        myMediaPlayer.release();
    }

    public void start(int postion) {
        Uri uri = Uri.parse("file:///" + allsongs.get(postion).getaName());
        if (myMediaPlayer == null) {
            myMediaPlayer = MediaPlayer.create(this, uri);
        } else {
            stop();
            myMediaPlayer = MediaPlayer.create(this, uri);
        }
        myMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Intent intent =new Intent(MUSIC_COMPLETE);
                stopForeground(true);
                stopSelf();
                // LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//                Log.d("raj", "song completed");
            }
        });
        curPositionOfSong = postion;
        play();
//        if(builder==null)
//        {
//            Log.d("raj","builder is empty");
//        }
//        else{
//            Log.d("raj","builder is not empty");
//        }

    }

    public boolean isPlaying() {
        return myMediaPlayer.isPlaying();
    }

    public boolean isNull() {
        return myMediaPlayer == null;
    }

    public boolean myPlay_status() {
        return play_status;
    }
    public int getCurrentPlayingSong(){
        if(myMediaPlayer==null)
            return 0;
        else
            return curPositionOfSong;
    }
    public int getCurPositionOfSong(){
        if(myMediaPlayer!=null)
            return myMediaPlayer.getCurrentPosition();
        return 0;
    }
    public int getTotalDurationOfSong(){
        if(myMediaPlayer!=null)
            return myMediaPlayer.getDuration();
        return 0;
    }
    public void seekToPosition(int position){
        if(myMediaPlayer!=null){
            myMediaPlayer.seekTo(position);
        }
    }


    public void getAllSongs(ArrayList<AudioModel> mySongs) {
        allsongs = mySongs;
    }

    @Override
    public void onDestroy() {
        stop();
        super.onDestroy();
    }
}
