package com.joy.player.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.joy.player.info.MusicInfo;
import com.joy.player.json.SearchSongInfo2;
import com.joy.player.service.MusicTrack;
import com.joy.player.util.IConstants;

import java.util.ArrayList;

public class PlayOnlineFavoriteManager {
    private static PlayOnlineFavoriteManager sInstance = null;

    private MusicDB mMusicDatabase = null;
    private long favPlaylistId = IConstants.FAV_PLAYLIST;

    public PlayOnlineFavoriteManager(final Context context) {
        mMusicDatabase = MusicDB.getInstance(context);
    }

    public static final synchronized PlayOnlineFavoriteManager getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PlayOnlineFavoriteManager(context.getApplicationContext());
        }
        return sInstance;
    }

    //建立播放列表表设置播放列表id和歌曲id为复合主键
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PlaylistsColumns.NAME + " ("
                + PlaylistsColumns.PLAYLIST_ID + " CHAR NOT NULL," + PlaylistsColumns.PLAYLIST_NAME + " CHAR NOT NULL,"
                + PlaylistsColumns.ARTIST_NAME + " CHAR NOT NULL," + PlaylistsColumns.URL + " CHAR NOT NULL,"
                + PlaylistsColumns.DURATION + " CHAR NOT NULL,primary key ( " + PlaylistsColumns.PLAYLIST_ID+"));");
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PlaylistsColumns.NAME);
        onCreate(db);
    }

    public synchronized void insertMusic(Context context, SearchSongInfo2 musicinfo) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(11);
            values.put(PlaylistsColumns.PLAYLIST_ID, musicinfo.getId());
            values.put(PlaylistsColumns.PLAYLIST_NAME ,musicinfo.getName());
            values.put(PlaylistsColumns.ARTIST_NAME, musicinfo.getArtist());
            values.put(PlaylistsColumns.URL, musicinfo.getUrl());
            values.put(PlaylistsColumns.DURATION, musicinfo.getDuration());
            database.insertWithOnConflict(PlaylistsColumns.NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

    }

//    public synchronized void insertLists(Context context, long playlistid, ArrayList<MusicInfo> musicInfos) {
//        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
//        database.beginTransaction();
//        int len = musicInfos.size();
//        try {
//            for (int i = 0; i < len; i++) {
//                MusicInfo info = musicInfos.get(i);
//                ContentValues values = new ContentValues(11);
//                values.put(PlaylistsColumns.PLAYLIST_ID, playlistid);
//                values.put(PlaylistsColumns.TRACK_ID, info.songId);
//                values.put(PlaylistsColumns.TRACK_ORDER, getPlaylist(playlistid).size());
//                values.put(PlaylistsColumns.TRACK_NAME, info.musicName);
//                values.put(PlaylistsColumns.ALBUM_ID, info.albumId);
//                values.put(PlaylistsColumns.ALBUM_NAME, info.albumName);
//                values.put(PlaylistsColumns.ALBUM_ART, info.albumData);
//                values.put(PlaylistsColumns.ARTIST_NAME, info.artist);
//                values.put(PlaylistsColumns.ARTIST_ID, info.artistId);
//                values.put(PlaylistsColumns.PATH, info.data);
//                values.put(PlaylistsColumns.IS_LOCAL, info.islocal);
//                database.insertWithOnConflict(PlaylistsColumns.NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
//            }
//
//            database.setTransactionSuccessful();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            database.endTransaction();
//        }
//
//        PlaylistInfo playlistInfo = PlaylistInfo.getInstance(context);
//        String albumart = null;
//        for (int i = len - 1; i >= 0; i--) {
//            MusicInfo info = musicInfos.get(i);
//            albumart = info.albumData;
//            if (info.islocal) {
//                String art = MusicUtils.getAlbumdata(MainApplication.context, info.songId);
//                if (art != null) {
//                    break;
//                } else {
//                    albumart = null;
//                }
//            } else if (!albumart.isEmpty()) {
//                break;
//            }
//
//        }
//        if (albumart != null) {
//            playlistInfo.update(playlistid, getPlaylist(playlistid).size(), albumart);
//        } else {
//            playlistInfo.update(playlistid, getPlaylist(playlistid).size());
//        }
//
//
//    }


//    public synchronized void update(long playlistid, long id, int order) {
//
//        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
//        database.beginTransaction();
//        try {
//            ContentValues values = new ContentValues(1);
//            values.put(PlaylistsColumns.TRACK_ORDER, order);
//            database.update(PlaylistsColumns.NAME, values, PlaylistsColumns.PLAYLIST_ID + " = ?" + " AND " +
//                    PlaylistsColumns.TRACK_ID + " = ?", new String[]{playlistid + "", id + ""});
//            database.setTransactionSuccessful();
//        } finally {
//            database.endTransaction();
//        }
//
//    }

    public synchronized boolean getFav(long id) {

        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistsColumns.NAME, null,null,null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return true;
            }
            return false;

        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

    }


//    public synchronized void update(long playlistid, long[] ids, int[] order) {
//
//        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
//        database.beginTransaction();
//        try {
//            for (int i = 0; i < order.length; i++) {
//                ContentValues values = new ContentValues(1);
//                values.put(PlaylistsColumns.TRACK_ORDER, order[i]);
//                database.update(PlaylistsColumns.NAME, values, PlaylistsColumns.PLAYLIST_ID + " = ?" + " AND " +
//                        PlaylistsColumns.TRACK_ID + " = ?", new String[]{playlistid + "", ids[i] + ""});
//            }
//
//            database.setTransactionSuccessful();
//        } finally {
//            database.endTransaction();
//        }
//
//    }

    public void removeItem(Context context, final String playlistId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistsColumns.NAME, PlaylistsColumns.PLAYLIST_ID + " = ?" , new String[]{
                String.valueOf(playlistId)});

//        PlaylistInfo playlistInfo = PlaylistInfo.getInstance(context);
//        playlistInfo.update(playlistId, getPlaylist(playlistId).size());

    }

    public void delete(final long PlaylistId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistsColumns.NAME, PlaylistsColumns.PLAYLIST_ID + " = ?", new String[]
                {String.valueOf(PlaylistId)});
    }

//    public synchronized void deleteMusicInfo(Context context, final long playlistid, final long musicid) {
//        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
//
//        Cursor cursor = null;
//        try {
//            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistsColumns.NAME, null,
//                    PlaylistsColumns.PLAYLIST_ID + " = ? and" + PlaylistsColumns.TRACK_ID + " = ?", new String[]{
//                            String.valueOf(playlistid), String.valueOf(musicid)
//                    }, null, null, null, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                long[] deletedPlaylistIds = new long[cursor.getCount()];
//                int i = 0;
//
//                do {
//                    deletedPlaylistIds[i] = cursor.getLong(0);
//                    i++;
//                } while (cursor.moveToNext());
//
//                PlaylistInfo.getInstance(context).updatePlaylistMusicCount(deletedPlaylistIds);
//            }
//
//
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//                cursor = null;
//            }
//        }
//
//    }

    //删除播放列表中的记录的音乐 ，删除本地文件时调用
//    public synchronized void deleteMusic(Context context, final long musicId) {
//        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
//
//        Cursor cursor = null;
//        try {
//            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistsColumns.NAME, null,
//                    PlaylistsColumns.TRACK_ID + " = " + String.valueOf(musicId), null, null, null, null, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                long[] deletedPlaylistIds = new long[cursor.getCount()];
//                int i = 0;
//
//                do {
//                    deletedPlaylistIds[i] = cursor.getLong(0);
//                    i++;
//                } while (cursor.moveToNext());
//
//                PlaylistInfo.getInstance(context).updatePlaylistMusicCount(deletedPlaylistIds);
//            }
//
//
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//                cursor = null;
//            }
//        }
//
//        database.delete(PlaylistsColumns.NAME, PlaylistsColumns.TRACK_ID + " = ?", new String[]
//                {String.valueOf(musicId)});
//    }


    public void deleteAll() {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistsColumns.NAME, null, null);
    }

//    public long[] getPlaylistIds(final long playlistid) {
//        long[] results = null;
//
//        Cursor cursor = null;
//        try {
//            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistsColumns.NAME, null,
//                    PlaylistsColumns.PLAYLIST_ID + " = " + String.valueOf(playlistid), null, null, null, PlaylistsColumns.TRACK_ORDER + " ASC ", null);
//            if (cursor != null) {
//                int len = cursor.getCount();
//                results = new long[len];
//                if (cursor.moveToFirst()) {
//                    for (int i = 0; i < len; i++) {
//                        results[i] = cursor.getLong(1);
//                        cursor.moveToNext();
//                    }
//
//                }
//            }
//
//            return results;
//
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//                cursor = null;
//            }
//        }
//    }

    public ArrayList<MusicTrack> getPlaylist() {
        ArrayList<MusicTrack> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistsColumns.NAME, null,
                    null, null, null, null, null , null);
            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {
                    results.add(new MusicTrack(cursor.getLong(1), cursor.getInt(0)));
                } while (cursor.moveToNext());
            }

            return results;

        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public ArrayList<MusicInfo> getMusicInfos(final long playlistid) {
        ArrayList<MusicInfo> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistsColumns.NAME, null,
                    PlaylistsColumns.PLAYLIST_ID + " = " + String.valueOf(playlistid), null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {
                    MusicInfo info = new MusicInfo();
                    info.songId = cursor.getLong(1);
                    info.musicName = cursor.getString(2);
                    info.albumId = cursor.getInt(3);
                    info.albumName = cursor.getString(4);
                    info.albumData = cursor.getString(5);
                    info.artistId = cursor.getLong(6);
                    info.artist = cursor.getString(7);
                    info.islocal = cursor.getInt(8) > 0;
                    results.add(info);
                } while (cursor.moveToNext());
            }

            return results;

        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public interface PlaylistsColumns {
        /* Table name */
        String NAME = "playfavlists";

        /* Album IDs column */
        String PLAYLIST_ID = "id";

        String PLAYLIST_NAME = "name";

        String ARTIST_NAME = "artist";

        String URL = "url";

        String DURATION = "duration";
    }
}
