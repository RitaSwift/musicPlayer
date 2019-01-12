package com.joy.player.fragmentnet;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.joy.player.R;
import com.joy.player.activity.PlayingOnlineActivity;
import com.joy.player.fragment.AttachFragment;
import com.joy.player.json.SearchSongInfo2;
import com.joy.player.widget.DividerItemDecoration;

import java.util.ArrayList;

public class SearchMusicFragment extends AttachFragment {

    private MusicAdapter mAdapter;
    //    private ArrayList<SearchSongInfo> songInfos;
    private ArrayList<SearchSongInfo2> songInfos;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;

    public static SearchMusicFragment newInstance(ArrayList<SearchSongInfo2> list) {
        SearchMusicFragment fragment = new SearchMusicFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("searchMusic", list);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recylerview, container, false);
        if (getArguments() != null) {
            songInfos = getArguments().getParcelableArrayList("searchMusic");
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MusicAdapter(songInfos);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));

        return view;
    }


    public class MusicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        final static int FIRST_ITEM = 0;
        final static int ITEM = 1;
        private ArrayList<SearchSongInfo2> mList;

        //        public MusicAdapter(ArrayList<SearchSongInfo> list) {
//////            if (list == null) {
//////                throw new IllegalArgumentException("model Data must not be null");
//////            }
////            mList = list;
////        }
        public MusicAdapter(ArrayList<SearchSongInfo2> list) {
//            if (list == null) {
//                throw new IllegalArgumentException("model Data must not be null");
//            }
            mList = list;
        }

        //更新adpter的数据
        public void updateDataSet(ArrayList<SearchSongInfo2> list) {
            this.mList = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
//            if (viewType == FIRST_ITEM)
//                return new CommonItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.common_item, viewGroup, false));

            return new ListItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_musci_common_item, viewGroup, false));
        }

//        //判断布局类型
//        @Override
//        public int getItemViewType(int position) {
//            return position == FIRST_ITEM ? FIRST_ITEM : ITEM;
//
//        }

        //将数据与界面进行绑定
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            SearchSongInfo2 model = mList.get(position);
            if (holder instanceof ListItemViewHolder) {

                ((ListItemViewHolder) holder).mainTitle.setText(model.getName());
                ((ListItemViewHolder) holder).title.setText(model.getArtist());

            }
        }

        @Override
        public int getItemCount() {
            return (null != mList ? mList.size() : 0);
        }


//        public class CommonItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//            TextView textView;
//            ImageView select;
//
//            CommonItemViewHolder(View view) {
//                super(view);
//                this.textView = (TextView) view.findViewById(R.id.play_all_number);
//                this.select = (ImageView) view.findViewById(R.id.select);
//                view.setOnClickListener(this);
//            }
//
//            public void onClick(View v) {
//
//
//            }
//
//        }


        public class ListItemViewHolder extends RecyclerView.ViewHolder {
            //ViewHolder
            ImageView moreOverflow, playState;
            TextView mainTitle, title;

            ListItemViewHolder(View view) {
                super(view);
                this.mainTitle = (TextView) view.findViewById(R.id.viewpager_list_toptext);
                this.title = (TextView) view.findViewById(R.id.viewpager_list_bottom_text);
                this.playState = (ImageView) view.findViewById(R.id.play_state);
                this.moreOverflow = (ImageView) view.findViewById(R.id.viewpager_list_button);

                moreOverflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //0107 屏蔽下载音乐的功能
//                        final SearchSongInfo2 model = mList.get(getAdapterPosition());
//                        new AlertDialog.Builder(mContext).setTitle("要下载音乐吗").
//                                setPositiveButton(mContext.getString(R.string.sure), new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        Down.downMusic(MainApplication.context, model.getSongid() + "", model.getSongname(), model.getArtistname());
//                                        dialog.dismiss();
//                                    }
//                                }).
//                                setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.dismiss();
//                                    }
//                                }).show();
                    }
                });
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final SearchSongInfo2 model = mList.get(getAdapterPosition());
                        Intent intent = new Intent(getActivity(), PlayingOnlineActivity.class);
                        intent.putExtra("musicinfo",model);
                        startActivity(intent);

//                        new AsyncTask<Void, Void, Void>() {
//
//                            @Override
//                            protected Void doInBackground(Void... params) {
//                                MusicInfo musicInfo = new MusicInfo();
//                                MusicDetailInfo info = null;
//                                try {
//                                    JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.Song.songBaseInfo(model.getId()))
//                                            .get("result").getAsJsonObject().get("items").getAsJsonArray().get(0).getAsJsonObject();
//                                    info = MainApplication.gsonInstance().fromJson(jsonObject, MusicDetailInfo.class);
//                                    musicInfo.albumData = info.getPic_small();
//                                } catch (NullPointerException e) {
//                                    e.printStackTrace();
//                                }
//
//
//                                musicInfo.songId = Integer.parseInt(info.getSong_id());
//                                musicInfo.musicName = info.getSong_title();
//                                musicInfo.artist = info.getArtist_name();
//                                musicInfo.islocal = false;
//                                musicInfo.albumName = info.getAlbum_title();
//                                musicInfo.albumId = Integer.parseInt(info.getAlbum_id());
//                                musicInfo.artistId = Integer.parseInt(info.getArtist_id());
//                                musicInfo.lrc = info.getLrclink();
//
//                                HashMap<Long, MusicInfo> infos = new HashMap<Long, MusicInfo>();
//                                long[] list = new long[1];
//                                list[0] = musicInfo.songId;
//                                infos.put(list[0], musicInfo);
//                                MusicPlayer.playAll(infos, list, 0, false);
//                                return null;
//                            }
//                        }.execute();
                    }
                });

            }


        }
    }


}
