package com.example.musicplayer.model;

/**
 * 音乐实体类
 */
public class Music {

    private String name;
    private String location;
    private int imageId;
    private String author;

    public static final String BASIC_LOCATION = "/mnt/sdcard/Music/";

    public Music(){}

    public Music(String name, String location, int imageId, String author) {
        this.name = name;
        this.location = location;
        this.imageId = imageId;
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "Music{" +
                "name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", imageId=" + imageId +
                ", author='" + author + '\'' +
                '}';
    }
}
