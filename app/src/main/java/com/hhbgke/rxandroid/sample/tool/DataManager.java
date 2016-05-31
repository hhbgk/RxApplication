package com.hhbgke.rxandroid.sample.tool;

import com.hhbgke.rxandroid.sample.bean.Singer;
import com.hhbgke.rxandroid.sample.bean.Song;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
   private static DataManager instance = null;

    public static DataManager getInstance() {
        if (null == instance) {
            instance = new DataManager();
        }
        return instance;
    }

    public List<Singer> getData(int number, int songs) {
        List<Singer> singerList = new ArrayList<>();

        for (int i =0; i < number; i++) {
            Singer singer = new Singer();
            singer.setId(i + 1);
            singer.setName("Singer" + (i + 1));
            List<Song> songList = new ArrayList<>();
            for (int j = 0; j < songs; j++) {
                Song song = new Song();
                song.setId(j + 1);
                song.setTitle("Song" + (j + 1));
                songList.add(song);
            }
            singer.setSongs(songList);
            singerList.add(singer);
        }
        return singerList;
    }
}
