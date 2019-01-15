package com.joy.player.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
            database.insert(PlaylistsColumns.NAME, null, values);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            database.close();
        }

    }

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

    public void removeItem(Context context, final String playlistId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistsColumns.NAME, PlaylistsColumns.PLAYLIST_ID + " = ?" , new String[]{
                String.valueOf(playlistId)});
    }

    public void delete(final long PlaylistId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistsColumns.NAME, PlaylistsColumns.PLAYLIST_ID + " = ?", new String[]
                {String.valueOf(PlaylistId)});
    }


    public void deleteAll() {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistsColumns.NAME, null, null);
    }

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

    public ArrayList<SearchSongInfo2> getMusicInfos() {
        ArrayList<SearchSongInfo2> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistsColumns.NAME, null,
                    null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    SearchSongInfo2 info = new SearchSongInfo2();
                    info.id = cursor.getString(cursor.getColumnIndex("id"));
                    info.name = cursor.getString(cursor.getColumnIndex("name"));
                    info.artist = cursor.getString(cursor.getColumnIndex("artist"));
                    info.url = cursor.getString(cursor.getColumnIndex("url"));
                    info.duration = cursor.getString(cursor.getColumnIndex("duration"));
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
