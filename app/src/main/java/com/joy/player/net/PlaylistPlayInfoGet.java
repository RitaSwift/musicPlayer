package com.joy.player.net;

import com.joy.player.json.MusicDetailInfo;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PlaylistPlayInfoGet {
    static ArrayList<MusicDetailInfo> arrayList;

    public PlaylistPlayInfoGet(ArrayList<MusicDetailInfo> arrayList) {
        this.arrayList = arrayList;
    }

    static ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void get(MusicDetailInfoGet musicDetailInfoGet) {
        pool.execute(musicDetailInfoGet);
    }
}
