package com.example.music_player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AllSongAdapter extends RecyclerView.Adapter<AllSongAdapter.MyViewHolder > {
    ArrayList<AudioModel> allsongs;
    Context context;
    public AllSongAdapter(Context context, ArrayList<AudioModel> allsongs) {
        this.allsongs=allsongs;
        this.context=context;
    }

    @NonNull
    @Override
    public AllSongAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.layout,parent,false);
        return new AllSongAdapter.MyViewHolder(view, (OnSongSelectListener) context);
    }

    @Override
    public void onBindViewHolder(@NonNull AllSongAdapter.MyViewHolder holder, int position) {
        holder.songName.setText(allsongs.get(position).getaAlbum());
        holder.singerName.setText(allsongs.get(position).getaName());
    }

    @Override
    public int getItemCount() {
        return allsongs.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView songName,singerName;
        OnSongSelectListener mySongSelcted;

        public MyViewHolder(@NonNull View itemView,OnSongSelectListener onSongSelectListener) {
            super(itemView);
            mySongSelcted=onSongSelectListener;
            songName=itemView.findViewById(R.id.IdSongName);
            singerName=itemView.findViewById(R.id.IdSingerName);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mySongSelcted.songSelectedListener(getAdapterPosition());
                }
            });
        }
    }


}
