package com.example.music_player;

public class AudioModel {
    String aPath;
    String aName;
    String aAlbum;
    public AudioModel(){

    }
    public AudioModel(String aPath, String aName, String aAlbum, String aArtist) {
        this.aPath = aPath;
        this.aName = aName;
        this.aAlbum = aAlbum;
        this.aArtist = aArtist;
    }

    String aArtist;

    public String getaPath() {
        return aPath;
    }
    public void setaPath(String aPath) {
        this.aPath = aPath;
    }
    public String getaName() {
        return aName;
    }
    public void setaName(String aName) {
        this.aName = aName;
    }
    public String getaAlbum() {
        return aAlbum;
    }
    public void setaAlbum(String aAlbum) {
        this.aAlbum = aAlbum;
    }
    public String getaArtist() {
        return aArtist;
    }
    public void setaArtist(String aArtist) {
        this.aArtist = aArtist;
    }
}
