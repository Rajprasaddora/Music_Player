package com.example.music_player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
    int playStatus;
    int curr_playing_pos;

    TextView songName,totalDuration,currDuration;
    ImageView play,pause,skip_next,skip_previouse;
    SeekBar seekBar;
    Runnable runnable;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allSongs=new ArrayList<AudioModel>();
        playStatus=0;
        handler=new Handler();
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            allSongs=getAllAudioFromDevice(this);
        }
        seekBar=findViewById(R.id.seekBar);
        songName=findViewById(R.id.IdSongName);
        songName.setSelected(true);
        totalDuration=findViewById(R.id.IdTotalDuration);
        currDuration=findViewById(R.id.IdCurrDuration);
        play=findViewById(R.id.IdPlayButton);
        skip_next=findViewById(R.id.IdSkipNext);
        skip_previouse=findViewById(R.id.IdSkipPreviouse);
        play.setOnClickListener(this);
        skip_previouse.setOnClickListener(this);
        skip_next.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(myMediaPlayer==null){
                    Toast.makeText(MainActivity.this, "No song selected ", Toast.LENGTH_SHORT).show();
                    seekBar.setProgress(0);
                    return ;
                }
                if(fromUser){
                    myMediaPlayer.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mySongAdapter=new AllSongAdapter(this, allSongs);
        RecyclerView recyclerView=findViewById(R.id.RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mySongAdapter);






    }

    public ArrayList<AudioModel> getAllAudioFromDevice(Context context) {
        ArrayList<AudioModel> tempAudioList = new ArrayList<>();
        ContentResolver contentResolver=getContentResolver();
        Uri musicUri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor c=contentResolver.query(musicUri,null,null,null,null);



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
        if (requestCode == 1)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getAllAudioFromDevice(this);
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void songSelectedListener(int position) {
      play(position);
    }
    public void play(int postion){
        Uri uri=Uri.parse("file:///"+allSongs.get(postion).getaName());
        if(myMediaPlayer==null){
            myMediaPlayer=MediaPlayer.create(this,uri);
        }
        else{
            stop();
            myMediaPlayer=MediaPlayer.create(this,uri);
        }
        myMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int milils=myMediaPlayer.getDuration();
                seekBar.setMax(milils);
                int min=(milils/1000)/60;
                int sec=(milils/1000)%60;
                String miniPart="";

                if(min<10){
                    miniPart+="0"+String.valueOf(min);
                }
                else{
                    miniPart+=String.valueOf(min);
                }
                String secPart="";
                if(sec<10){
                    secPart+="0"+String.valueOf(sec);
                }
                else{
                    secPart+=String.valueOf(sec);
                }
                totalDuration.setText(miniPart+":"+secPart);
//                Log.d("raj", String.valueOf(myMediaPlayer.getDuration()));
                changeSeekBar();
            }
        });
        songName.setText(allSongs.get(postion).getaAlbum());
        playStatus=1;
        play.setImageResource(R.drawable.pause);
        curr_playing_pos=postion;
        myMediaPlayer.start();
    }
    public void stop(){
        if(myMediaPlayer!=null){
            myMediaPlayer.release();
            myMediaPlayer=null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.IdPlayButton:{
                if(playStatus==0){
                    if(myMediaPlayer==null){
                        Toast.makeText(this,"No songs selected ",Toast.LENGTH_SHORT).show();
                    }
                    else{

                        play.setImageResource(R.drawable.pause);
                        myMediaPlayer.start();
                        playStatus=1;
                    }
                }
                else{
                    play.setImageResource(play_arrow);
                    myMediaPlayer.pause();
                    playStatus=0;
                }
                break;
            }
            case R.id.IdSkipNext:{
                Log.d("raj","skip next called");
                if(playStatus==0){
                    if(myMediaPlayer==null){
                        curr_playing_pos=0;
                        play(0);
                    }
                    else{
                        curr_playing_pos++;
                        curr_playing_pos%=allSongs.size();
                        play(curr_playing_pos);
                    }
                }
                else{
                    curr_playing_pos++;
                    curr_playing_pos%=allSongs.size();
                    play(curr_playing_pos);
                }
                break;
            }
            case R.id.IdSkipPreviouse:{
                if(playStatus==0){
                    if(myMediaPlayer==null){
                        curr_playing_pos=0;
                        play(0);
                    }
                    else{
                        curr_playing_pos--;
                        curr_playing_pos+=allSongs.size();
                        curr_playing_pos%=allSongs.size();
                        play(curr_playing_pos);
                    }
                }
                else{
                    curr_playing_pos++;
                    curr_playing_pos%=allSongs.size();
                    play(curr_playing_pos);
                }
                break;
            }


        }
    }
    public void changeSeekBar(){
        int milis=myMediaPlayer.getCurrentPosition();
        int min=(milis/1000)/60;
        int sec=(milis/1000)%60;
        String miniPart="";

        if(min<10){
            miniPart+="0"+String.valueOf(min);
        }
        else{
            miniPart+=String.valueOf(min);
        }
        String secPart="";
        if(sec<10){
            secPart+="0"+String.valueOf(sec);
        }
        else{
            secPart+=String.valueOf(sec);
        }
        currDuration.setText(miniPart+":"+secPart);
        seekBar.setProgress(milis);
        if(myMediaPlayer.isPlaying()){
            runnable=new Runnable() {
                @Override
                public void run() {
                    changeSeekBar();
                }
            };
            handler.postDelayed(runnable,1000);

        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        stop();
    }
}