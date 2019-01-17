package com.joy.player.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.joy.player.R;
import com.joy.player.json.SearchSongInfo2;
import com.joy.player.provider.PlayOnlineFavoriteManager;

import java.util.ArrayList;

public class PlaylistLikedActivity extends Activity {

    private ArrayList<SearchSongInfo2> adapterList = new ArrayList<>();
    private ListView listview;
    private SongAdapter mSongAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlistliked);
        listview = findViewById(R.id.listview_fav);
        adapterList = PlayOnlineFavoriteManager.getInstance(this).getMusicInfos();
        mSongAdapter = new SongAdapter(this, adapterList);
        listview.setAdapter(mSongAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(PlaylistLikedActivity.this, PlayingOnlineActivity.class);
                intent.putExtra("musicinfo",adapterList.get(i));
                intent.putExtra("isFav",true);
                startActivity(intent);
            }
        });

    }

    private class SongAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<SearchSongInfo2> list;
        private ViewHolder viewHolder;

        public SongAdapter(Context context, ArrayList<SearchSongInfo2> list) {
            this.mContext = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {//第一个item的convertView为空
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_favlist,null);//在这里设置了复用，以后的item将会复用第一个item，节省内存资源
                viewHolder = new ViewHolder();
                viewHolder.songname = (TextView) convertView.findViewById(R.id.fav_songname);
                viewHolder.songartist = (TextView) convertView.findViewById(R.id.fav_songartist);
                convertView.setTag(viewHolder);
            } else {
                viewHolder= (ViewHolder) convertView.getTag();
            }
            viewHolder.songname.setText(list.get(position).getName());
            viewHolder.songartist.setText(list.get(position).getArtist());
            return convertView;
        }
        class ViewHolder {
            private TextView songname;
            private TextView songartist;
        }
    }
}
