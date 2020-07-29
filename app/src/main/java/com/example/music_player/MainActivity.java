package com.example.music_player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.example.music_player.R.drawable.play_arrow;

public class MainActivity extends AppCompatActivity implements OnSongSelectListener, View.OnClickListener {
    ArrayList<AudioModel> allSongs;
    MediaPlayer myMediaPlayer;
    AllSongAdapter mySongAdapter;
    int  curr_playing_pos;
    TextView songName, totalDuration, currDuration;
    ImageView play, skip_next, skip_previouse;
    SeekBar seekBar;
    boolean mbound;
    Handler handler;
    MyMusicPlayerService myMusicPlayerService;
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
//            Log.d("raj", "service Connected");
            myMusicPlayerService = ((MyMusicPlayerService.MyServiceBinder) service).getService();
            mbound = true;
            myMusicPlayerService.getAllSongs(allSongs);
//            Log.d("raj","Activity Thread id is "+Thread.currentThread().getId());
            if(!myMusicPlayerService.isNull()){
                curr_playing_pos=myMusicPlayerService.getCurrentPlayingSong();
                songName.setText(allSongs.get(curr_playing_pos).getaAlbum());
                if(myMusicPlayerService.myPlay_status()==true){
                    play.setImageResource(R.drawable.pause);    Runnable runnable;

                }
                int milis=myMusicPlayerService.getTotalDurationOfSong();
                int min = (milis / 1000) / 60;
                int sec = (milis / 1000) % 60;
                String miniPart = "";

                if (min < 10) {
                    miniPart += "0" + String.valueOf(min);
                } else {
                    miniPart += String.valueOf(min);
                }
                String secPart = "";
                if (sec < 10) {
                    secPart += "0" + String.valueOf(sec);
                } else {
                    secPart += String.valueOf(sec);
                }
                totalDuration.setText(miniPart+":"+secPart);
                seekBar.setMax(milis);
                changeSeekBar();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
//            Log.d("raj", "service Disconnected ");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Log.d("raj", "onCreate of Activity is called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allSongs = new ArrayList<AudioModel>();
        handler = new Handler();


        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            allSongs = getAllAudioFromDevice(this);
        }


        seekBar = findViewById(R.id.seekBar);
        songName = findViewById(R.id.IdSongName);
        songName.setSelected(true);
        totalDuration = findViewById(R.id.IdTotalDuration);
        currDuration = findViewById(R.id.IdCurrDuration);
        play = findViewById(R.id.IdPlayButton);
        skip_next = findViewById(R.id.IdSkipNext);
        skip_previouse = findViewById(R.id.IdSkipPreviouse);
        play.setOnClickListener(this);
        skip_previouse.setOnClickListener(this);
        skip_next.setOnClickListener(this);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (myMusicPlayerService.isNull() == true) {
                    Toast.makeText(MainActivity.this, "No song selected ", Toast.LENGTH_SHORT).show();
                    seekBar.setProgress(0);
                    return;
                }
                if (fromUser) {
                    myMusicPlayerService.seekToPosition(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mySongAdapter = new AllSongAdapter(this, allSongs);
        RecyclerView recyclerView = findViewById(R.id.RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mySongAdapter);


    }
    BroadcastReceiver mySongCompleteReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playNext();
        }
    };

    @Override
    protected void onStart() {
//        Log.d("raj", "onStart of Activity is called");
        super.onStart();
        Intent intent = new Intent(MainActivity.this, MyMusicPlayerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mySongCompleteReceiver,new IntentFilter(MyMusicPlayerService.MUSIC_COMPLETE));

    }

    @Override
    public void songSelectedListener(int position) {
        if (mbound) {
            if (myMusicPlayerService.isNull()) {

            } else if (myMusicPlayerService.isPlaying()) {
                myMusicPlayerService.stop();
            }
            songName.setText(allSongs.get(position).getaAlbum());

            Intent intent =new Intent(this,MyMusicPlayerService.class);
            intent.putExtra("song_pos",position);
//            intent.setAction(Constants.START_MUSIC_SERVICE);
//            intent.setAction(Constants.PLAY_MUSIC);
            startService(intent);
            Uri uri=Uri.parse("file:///"+allSongs.get(position).getaName());
            myMediaPlayer=MediaPlayer.create(this,uri);
            int milis=myMediaPlayer.getDuration();
            myMediaPlayer.release();
            int min = (milis / 1000) / 60;
            int sec = (milis / 1000) % 60;
            String miniPart = "";

            if (min < 10) {
                miniPart += "0" + String.valueOf(min);
            } else {
                miniPart += String.valueOf(min);
            }
            String secPart = "";
            if (sec < 10) {
                secPart += "0" + String.valueOf(sec);
            } else {
                secPart += String.valueOf(sec);
            }
            totalDuration.setText(miniPart+":"+secPart);


            play.setImageResource(R.drawable.pause);
//            Log.d("raj","songSelected is called");
//            Log.d("raj","startService is called");
            myMusicPlayerService.start(position);
//            Log.d("raj","start of myMusicPlayerService is called");

            curr_playing_pos = position;
            seekBar.setMax(milis);
            changeSeekBar();
        }
        //play(position);
    }


    public ArrayList<AudioModel> getAllAudioFromDevice(Context context) {
        ArrayList<AudioModel> tempAudioList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor c = contentResolver.query(musicUri, null, null, null, null);


        if (c != null) {
            while (c.moveToNext()) {
                // Create a model object.
                AudioModel audioModel = new AudioModel();

                String path = c.getString(0);   // Retrieve path.
                String name = c.getString(1);   // Retrieve name.
                String album = c.getString(2);  // Retrieve album name.
                String artist = c.getString(3); // Retrieve artist name.

                // Set data to the model object.
                audioModel.setaName(name);
                audioModel.setaAlbum(album);
                audioModel.setaArtist(artist);
                audioModel.setaPath(path);

//                 Log.d("raj","Name :" + name + " Album :" + album);
//                 Log.d("raj","Path :" + path+ " Artist :" + artist);

                // Add the model object to the list .
                tempAudioList.add(audioModel);
            }
            c.close();
        }
        return tempAudioList;
        // Return the list.
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getAllAudioFromDevice(this);
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

//    public void play(int postion){
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//        Uri uri=Uri.parse("file:///"+allSongs.get(postion).getaName());
//        if(myMediaPlayer==null){
//            myMediaPlayer=MediaPlayer.create(this,uri);
//        }
//        else{
//            stop();
//            myMediaPlayer=MediaPlayer.create(this,uri);
//        }
//        myMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                int milils=myMediaPlayer.getDuration();
//                seekBar.setMax(milils);
//                int min=(milils/1000)/60;
//                int sec=(milils/1000)%60;
//                String miniPart="";
//
//                if(min<10){
//                    miniPart+="0"+String.valueOf(min);
//                }
//                else{
//                    miniPart+=String.valueOf(min);
//                }
//                String secPart="";
//                if(sec<10){
//                    secPart+="0"+String.valueOf(sec);
//                }
//                else{
//                    secPart+=String.valueOf(sec);
//                }
//                totalDuration.setText(miniPart+":"+secPart);
////                Log.d("raj", String.valueOf(myMediaPlayer.getDuration()));
//                changeSeekBar();
//            }
//        });
//        songName.setText(allSongs.get(postion).getaAlbum());
//        playStatus=1;
//        play.setImageResource(R.drawable.pause);
//        curr_playing_pos=postion;
//        myMediaPlayer.start();
//    }
//    public void stop(){
//        if(myMediaPlayer!=null){
//            myMediaPlayer.release();
//            myMediaPlayer=null;
//        }
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.IdPlayButton: {
//                if(myMusicPlayerService==null){
//                    Log.d("raj","yes myMusicService is Null");
//                }
                if (myMusicPlayerService.isNull()) {
                    Toast.makeText(this, "No songs selected ", Toast.LENGTH_SHORT).show();
                } else {
                    if (myMusicPlayerService.myPlay_status()) {
                        myMusicPlayerService.pause();
                        play.setImageResource(play_arrow);

                    } else {
                        myMusicPlayerService.play();
                        play.setImageResource(R.drawable.pause);
                    }
                }
                break;
            }
            case R.id.IdSkipNext: {
//                Log.d("raj", "skip next called");
                playNext();
                break;
            }
            case R.id.IdSkipPreviouse: {
//                Log.d("raj", "skip previose called");
                if (myMusicPlayerService.isNull()) {
                    curr_playing_pos = 0;
                } else {
                    curr_playing_pos--;
                    curr_playing_pos += allSongs.size();
                    curr_playing_pos %= allSongs.size();
                }
                songName.setText(allSongs.get(curr_playing_pos).getaAlbum());
                myMusicPlayerService.start(curr_playing_pos);
                break;
            }


        }
    }
    public void playNext(){
        if (myMusicPlayerService.isNull()) {
            curr_playing_pos = 0;
        } else {
            curr_playing_pos++;
            curr_playing_pos %= allSongs.size();
        }
        songName.setText(allSongs.get(curr_playing_pos).getaAlbum());
        myMusicPlayerService.start(curr_playing_pos);
    }

    public void changeSeekBar() {
        int milis = myMusicPlayerService.getCurPositionOfSong();
        seekBar.setProgress(milis);
        int min = (milis / 1000) / 60;
        int sec = (milis / 1000) % 60;
        String miniPart = "";

        if (min < 10) {
            miniPart += "0" + String.valueOf(min);
        } else {
            miniPart += String.valueOf(min);
        }
        String secPart = "";
        if (sec < 10) {
            secPart += "0" + String.valueOf(sec);
        } else {
            secPart += String.valueOf(sec);
        }
        currDuration.setText(miniPart + ":" + secPart);
        //Log.d("raj1",milis+"");
        if (!myMusicPlayerService.isNull() ) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
//                    Log.d("raj","seekbar thread id is "+Thread.currentThread().getId()+"");

                    changeSeekBar();
                }
            };
            handler.postDelayed(runnable, 1000);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mbound) {
            unbindService(serviceConnection);
            mbound = false;
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mySongCompleteReceiver);
//        stop();
    }
}