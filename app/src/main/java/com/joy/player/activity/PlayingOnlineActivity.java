package com.joy.player.activity;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.*;
import com.joy.player.R;
import com.joy.player.downmusic.DownloadTask;
import com.joy.player.fragment.PlayQueueFragment;
import com.joy.player.fragment.RoundFragment;
import com.joy.player.fragment.SimpleMoreFragment;
import com.joy.player.handler.HandlerUtil;
import com.joy.player.info.MusicInfo;
import com.joy.player.json.SearchSongInfo2;
import com.joy.player.provider.PlayOnlineFavoriteManager;
import com.joy.player.proxy.utils.Constants;
import com.joy.player.service.MediaService;
import com.joy.player.service.MusicPlayer;
import com.joy.player.util.*;
import com.joy.player.widget.AlbumViewPager;
import com.joy.player.widget.LrcView;
import com.joy.player.widget.PlayerSeekBar;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.URLEncoder;


/**
 *
 */
public class PlayingOnlineActivity extends BaseActivity implements IConstants {
    private ImageView mBackAlbum, mPlayingmode, mControl, mNext, mPre, mPlaylist, mCmt, mFav, mDown, mMore, mNeedle;
    private TextView mTimePlayed, mDuration;
    private PlayerSeekBar mProgress;

    private ActionBar ab;
    private ObjectAnimator mNeedleAnim, mRotateAnim;
    private AnimatorSet mAnimatorSet;
    private AlbumViewPager mViewPager;
    private FragmentAdapter mAdapter;
    private BitmapFactory.Options mNewOpts;
    private View mActiveView;
    private PlayOnlineFavoriteManager mPlaylistsManager;
    private WeakReference<ObjectAnimator> animatorWeakReference;
    private WeakReference<View> mViewWeakReference = new WeakReference<View>(null);
    private boolean isFav = false;
    private boolean isNextOrPreSetPage = false; //判断viewpager由手动滑动 还是setcruuentitem换页
    private Toolbar toolbar;
    private FrameLayout mAlbumLayout;
    private RelativeLayout mLrcViewContainer;
    private LrcView mLrcView;
    private TextView mTryGetLrc;
    private LinearLayout mMusicTool;
    private SeekBar mVolumeSeek;
    private Handler mHandler;
    private Handler mPlayHandler;
    private static final int VIEWPAGER_SCROLL_TIME = 390;
    private static final int TIME_DELAY = 500;
    private static final int NEXT_MUSIC = 0;
    private static final int PRE_MUSIC = 1;
    private Bitmap mBitmap;
    private long lastAlbum;
    private PlayMusic mPlayThread;
    private boolean print = true;
    private String TAG = PlayingOnlineActivity.class.getSimpleName();

    private SearchSongInfo2 model;
    private Player player;

    private static final int PROCESSING = 1;
    private static final int FAILURE = -1;

    private ProgressBar downloadProgressBar;

    private Handler handler = new UIHandler();

    private final class UIHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROCESSING:
                    downloadProgressBar.setProgress(msg.getData().getInt("size"));
                    float num = (float) downloadProgressBar.getProgress()
                            / (float) downloadProgressBar.getMax();
                    if (downloadProgressBar.getProgress() == downloadProgressBar.getMax()) { // �������
                        Toast.makeText(getApplicationContext(), "下载成功",
                                Toast.LENGTH_LONG).show();

                        mDown.setImageResource(R.drawable.play_icn_dlded_dis);
                        String savePath = Constants.DOWNLOAD_PATH  + "/" + model.getName();
                        Uri contentUri = Uri.fromFile(new File(savePath));
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,contentUri);
                        sendBroadcast(mediaScanIntent);
                    }
                    break;
                case FAILURE:
                    Toast.makeText(getApplicationContext(), "下载失败",
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }


    @Override
    protected void showQuickControl(boolean show) {
        //super.showOrHideQuickControl(show);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playingonline);
        //播放管理界面
        mPlaylistsManager = mPlaylistsManager.getInstance(this);
        model = getIntent().getParcelableExtra("musicinfo");
        isFav = getIntent().getBooleanExtra("isFav", false);

        //放歌曲名字和作者界面
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        downloadProgressBar = findViewById(R.id.downloadprogressBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ab = getSupportActionBar();
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.actionbar_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        mAlbumLayout = (FrameLayout) findViewById(R.id.headerView);
        mLrcViewContainer = (RelativeLayout) findViewById(R.id.lrcviewContainer);
        mLrcView = (LrcView) findViewById(R.id.lrcview);
        mTryGetLrc = (TextView) findViewById(R.id.tragetlrc);
        mMusicTool = (LinearLayout) findViewById(R.id.music_tool);

        mBackAlbum = (ImageView) findViewById(R.id.albumArt);
        mPlayingmode = (ImageView) findViewById(R.id.playing_mode);
        //播放按钮
        mControl = (ImageView) findViewById(R.id.playing_play);
        mNext = (ImageView) findViewById(R.id.playing_next);
        mPre = (ImageView) findViewById(R.id.playing_pre);
        mPlaylist = (ImageView) findViewById(R.id.playing_playlist);
        mMore = (ImageView) findViewById(R.id.playing_more);
        mCmt = (ImageView) findViewById(R.id.playing_cmt);
        mFav = (ImageView) findViewById(R.id.playing_fav);
        mDown = (ImageView) findViewById(R.id.playing_down);
        mTimePlayed = (TextView) findViewById(R.id.music_duration_played);
        mDuration = (TextView) findViewById(R.id.music_duration);
        mProgress = (PlayerSeekBar) findViewById(R.id.play_seek);
        mNeedle = (ImageView) findViewById(R.id.needle);
        mViewPager = (AlbumViewPager) findViewById(R.id.view_pager);

        mNeedleAnim = ObjectAnimator.ofFloat(mNeedle, "rotation", -25, 0);
        mNeedleAnim.setDuration(200);
        mNeedleAnim.setRepeatMode(0);
        mNeedleAnim.setInterpolator(new LinearInterpolator());

        mVolumeSeek = (SeekBar) findViewById(R.id.volume_seek);
        mProgress.setIndeterminate(false);
        mProgress.setProgress(1);
        mProgress.setMax(1000);
        player = new Player(mProgress);
        new Thread(new Runnable() {

            @Override
            public void run() {
                player.playUrl(model.getUrl());
            }
        }).start();

        ab.setTitle(model.getName());
        ab.setSubtitle(model.getArtist());
        mDuration.setText(MusicUtils.makeShortTimeString(PlayingOnlineActivity.this.getApplication(), Long.parseLong(model.getDuration())));


        loadOther();
        setViewPager();
        initLrcView();
        mHandler = HandlerUtil.getInstance(this);

        mPlayThread = new PlayMusic();
        mPlayThread.start();

        initView();
        if (isFav) {
            mFav.setImageResource(R.drawable.play_icn_loved);
        } else {
            mFav.setImageResource(R.drawable.play_icn_love);
        }
    }

    private void initView() {


        if (player.mediaPlayer.isPlaying()) {
            mControl.setImageResource(R.drawable.play_rdi_btn_pause);
        }
    }

    private void initLrcView() {
        mLrcView.setOnSeekToListener(onSeekToListener);
        mLrcView.setOnLrcClickListener(onLrcClickListener);
        mViewPager.setOnSingleTouchListener(new AlbumViewPager.OnSingleTouchListener() {
            @Override
            public void onSingleTouch(View v) {
                if (mAlbumLayout.getVisibility() == View.VISIBLE) {
                    mAlbumLayout.setVisibility(View.INVISIBLE);
                    mLrcViewContainer.setVisibility(View.VISIBLE);
                    mMusicTool.setVisibility(View.INVISIBLE);
                }
            }
        });
        mLrcViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLrcViewContainer.getVisibility() == View.VISIBLE) {
                    mLrcViewContainer.setVisibility(View.INVISIBLE);
                    mAlbumLayout.setVisibility(View.VISIBLE);
                    mMusicTool.setVisibility(View.VISIBLE);
                }
            }
        });

        //下载功能
        mDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path = model.getUrl();
                String filename = path.substring(path.lastIndexOf('/') + 1);

                try {
                    filename = URLEncoder.encode(filename, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                path = path.substring(0, path.lastIndexOf("/") + 1) + filename;
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
//                    File savDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
//                    File savDir1 = Environment.getExternalStorageDirectory();
                    /**
                     * 03-11 18:20:27.620 9635-9635/com.joy.player.musicplayer I/System.out: /storage/emulated/0
                     *     保存的路径为：/storage/emulated/0/Movies
                     */
                    File savDir1 = new File(Constants.DOWNLOAD_PATH);
//                    System.out.println(savDir1);
                    System.out.println("url:" + model.getUrl());
                    //android 媒体文件通知媒体数据库更新 不行 报错
//                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
//                            Uri.parse("file://"
//                                    + Environment
//                                    .getExternalStorageDirectory())));

                    download(path, savDir1);

                } else {
                    Toast.makeText(getApplicationContext(),
                            "rrr", Toast.LENGTH_LONG).show();
                }
            }
        });

        mTryGetLrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(MediaService.TRY_GET_TRACKINFO);
                sendBroadcast(intent);
                Toast.makeText(getApplicationContext(), "正在获取信息", Toast.LENGTH_SHORT).show();
            }
        });

        final AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int v = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int mMaxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolumeSeek.setMax(mMaxVol);
        mVolumeSeek.setProgress(v);
        mVolumeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.ADJUST_SAME);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void download(String path, File savDir) {
        DownloadTask task = new DownloadTask(path, savDir);
        new Thread(task).start();
    }

    private final class DownloadTask implements Runnable {
        private String path;
        private File saveDir;
        private FileDownloader loader;

        public DownloadTask(String path, File saveDir) {
            this.path = path;
            this.saveDir = saveDir;
        }

        public void exit() {
            if (loader != null)
                loader.exit();
        }

        DownloadProgressListener downloadProgressListener = new DownloadProgressListener() {
            @Override
            public void onDownloadSize(int size) {
                Message msg = new Message();
                msg.what = PROCESSING;
                msg.getData().putInt("size", size);
                handler.sendMessage(msg);
            }
        };

        public void run() {
            try {
                loader = new FileDownloader(getApplicationContext(), path,
                        saveDir, 3);
                downloadProgressBar.setMax(loader.getFileSize());
                loader.download(downloadProgressListener);
            } catch (Exception e) {
                e.printStackTrace();
                handler.sendMessage(handler.obtainMessage(FAILURE));
            }
        }
    }


    LrcView.OnLrcClickListener onLrcClickListener = new LrcView.OnLrcClickListener() {

        @Override
        public void onClick() {

            if (mLrcViewContainer.getVisibility() == View.VISIBLE) {
                mLrcViewContainer.setVisibility(View.INVISIBLE);
                mAlbumLayout.setVisibility(View.VISIBLE);
                mMusicTool.setVisibility(View.VISIBLE);
            }
        }
    };
    LrcView.OnSeekToListener onSeekToListener = new LrcView.OnSeekToListener() {

        @Override
        public void onSeekTo(int progress) {
        }
    };


    private void loadOther() {

        setSeekBarListener();
        setTools();

    }

    private void setViewPager() {
        mViewPager.setOffscreenPageLimit(2);
        PlaybarPagerTransformer transformer = new PlaybarPagerTransformer();
        mAdapter = new FragmentAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageTransformer(true, transformer);

        // 改变viewpager动画时间
        try {
            Field mField = ViewPager.class.getDeclaredField("mScroller");
            mField.setAccessible(true);
            MyScroller mScroller = new MyScroller(mViewPager.getContext().getApplicationContext(), new LinearInterpolator());
            mField.set(mViewPager, mScroller);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(final int pPosition) {
                if (pPosition < 1) { //首位之前，跳转到末尾（N）
                    MusicPlayer.setQueuePosition(MusicPlayer.getQueue().length);
                    mViewPager.setCurrentItem(MusicPlayer.getQueue().length, false);
                    isNextOrPreSetPage = false;
                    return;

                } else if (pPosition > MusicPlayer.getQueue().length) { //末位之后，跳转到首位（1）
                    MusicPlayer.setQueuePosition(0);
                    mViewPager.setCurrentItem(1, false); //false:不显示跳转过程的动画
                    isNextOrPreSetPage = false;
                    return;
                } else {

                    if (!isNextOrPreSetPage) {
                        if (pPosition < MusicPlayer.getQueuePosition() + 1) {

                            Message msg = new Message();
                            msg.what = PRE_MUSIC;
                            mPlayHandler.sendMessageDelayed(msg, TIME_DELAY);


                        } else if (pPosition > MusicPlayer.getQueuePosition() + 1) {

                            Message msg = new Message();
                            msg.what = NEXT_MUSIC;
                            mPlayHandler.sendMessageDelayed(msg, TIME_DELAY);

                        }
                    }

                }
                isNextOrPreSetPage = false;

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int pState) {
            }
        });
    }

    private void setTools() {
        mPlayingmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayer.cycleRepeat();
                updatePlaymode();
            }
        });

        mPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = PRE_MUSIC;
                mPlayHandler.sendMessage(msg);
            }
        });

        mControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (player.mediaPlayer.isPlaying()) {
                    mControl.setImageResource(R.drawable.play_rdi_btn_play);
                    player.mediaPlayer.pause();
                    mProgress.removeCallbacks(mUpdateProgress);
                    mControl.setImageResource(R.drawable.play_rdi_btn_play);
                    if (mNeedleAnim != null) {
                        mNeedleAnim.reverse();
                        mNeedleAnim.end();
                    }

                    if (mRotateAnim != null && mRotateAnim.isRunning()) {
                        mRotateAnim.cancel();
                        float valueAvatar = (float) mRotateAnim.getAnimatedValue();
                        mRotateAnim.setFloatValues(valueAvatar, 360f + valueAvatar);
                    }
                } else {
                    mControl.setImageResource(R.drawable.play_rdi_btn_pause);
                    player.mediaPlayer.start();
                    if (mAnimatorSet != null) {
                        mAnimatorSet.start();
                    }
                }
            }
        });

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRotateAnim != null) {
                    mRotateAnim.end();
                    mRotateAnim = null;
                }
                Message msg = new Message();
                msg.what = NEXT_MUSIC;
                mPlayHandler.sendMessage(msg);

            }
        });

        mPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayQueueFragment playQueueFragment = new PlayQueueFragment();
                playQueueFragment.show(getSupportFragmentManager(), "playlistframent");
            }
        });

        mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SimpleMoreFragment moreFragment = SimpleMoreFragment.newInstance(MusicPlayer.getCurrentAudioId());
                moreFragment.show(getSupportFragmentManager(), "music");
            }
        });

        mFav.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (isFav) {
                    mPlaylistsManager.removeItem(PlayingOnlineActivity.this, model.getId());
                    mFav.setImageResource(R.drawable.play_rdi_icn_love);
                    isFav = false;
                } else {
                    try {
                        mPlaylistsManager.insertMusic(PlayingOnlineActivity.this, model);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mFav.setImageResource(R.drawable.play_icn_loved);
                    isFav = true;
                }

            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        if (item.getItemId() == R.id.menu_share) {
            MusicInfo musicInfo = MusicUtils.getMusicInfo(PlayingOnlineActivity.this, MusicPlayer.getCurrentAudioId());
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + musicInfo.data));
            shareIntent.setType("audio/*");
            this.startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.shared_to)));

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //分享功能屏蔽掉
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.playing_menu, menu);
        return true;

    }

    private void updatePlaymode() {
        if (MusicPlayer.getShuffleMode() == MediaService.SHUFFLE_NORMAL) {
            mPlayingmode.setImageResource(R.drawable.play_icn_shuffle);
            Toast.makeText(PlayingOnlineActivity.this.getApplication(), getResources().getString(R.string.random_play),
                    Toast.LENGTH_SHORT).show();
        } else {
            switch (MusicPlayer.getRepeatMode()) {
                case MediaService.REPEAT_ALL:
                    mPlayingmode.setImageResource(R.drawable.play_icn_loop);
                    Toast.makeText(PlayingOnlineActivity.this.getApplication(), getResources().getString(R.string.loop_play),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MediaService.REPEAT_CURRENT:
                    mPlayingmode.setImageResource(R.drawable.play_icn_one);
                    Toast.makeText(PlayingOnlineActivity.this.getApplication(), getResources().getString(R.string.play_one),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        //设置ViewPager的默认项
        mViewPager.setCurrentItem(MusicPlayer.getQueuePosition() + 1);

    }

    @Override
    public void onResume() {
        super.onResume();
        lastAlbum = -1;
        if (MusicPlayer.isTrackLocal())
            updateBuffer(100);
        else {
            updateBuffer(MusicPlayer.secondPosition());
        }
        mHandler.postDelayed(mUpdateProgress, 0);
    }


    public void updateQueue() {
        if (MusicPlayer.getQueueSize() == 0) {
            MusicPlayer.stop();
            finish();
            return;
        }
        mAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(MusicPlayer.getQueuePosition() + 1, false);
    }

    private void updateFav(boolean b) {
        if (b) {
            mFav.setImageResource(R.drawable.play_icn_loved);
        } else {
            mFav.setImageResource(R.drawable.play_rdi_icn_love);
        }
    }

    public void updateLrc() {
    }

    public void updateTrack() {
    }

    public void updateTrackInfo() {

//        if (MusicPlayer.getQueueSize() == 0) {
//            return;
//        }

        Fragment fragment = (RoundFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem());
        if (fragment != null) {
            View v = fragment.getView();
            if (mViewWeakReference.get() != v && v != null) {
                ((ViewGroup) v).setAnimationCacheEnabled(false);
                if (mViewWeakReference != null)
                    mViewWeakReference.clear();
                mViewWeakReference = new WeakReference<View>(v);
                mActiveView = mViewWeakReference.get();
            }
        }

        if (mActiveView != null) {
            mRotateAnim = (ObjectAnimator) mActiveView.getTag(R.id.tag_animator);
        }

        mAnimatorSet = new AnimatorSet();
//        if (player.mediaPlayer.isPlaying()) {
        mProgress.removeCallbacks(mUpdateProgress);
        mProgress.postDelayed(mUpdateProgress, 200);
        mControl.setImageResource(R.drawable.play_rdi_btn_pause);
        if (mAnimatorSet != null && mRotateAnim != null && !mRotateAnim.isRunning()) {
            //修复从playactivity回到Main界面null
            if (mNeedleAnim == null) {
                mNeedleAnim = ObjectAnimator.ofFloat(mNeedle, "rotation", -30, 0);
                mNeedleAnim.setDuration(200);
                mNeedleAnim.setRepeatMode(0);
                mNeedleAnim.setInterpolator(new LinearInterpolator());
            }
            mAnimatorSet.play(mNeedleAnim).before(mRotateAnim);
            mAnimatorSet.start();
        }

        isNextOrPreSetPage = false;
        if (MusicPlayer.getQueuePosition() + 1 != mViewPager.getCurrentItem()) {
            mViewPager.setCurrentItem(MusicPlayer.getQueuePosition() + 1);
            isNextOrPreSetPage = true;
        }

    }

    @Override
    public void updateBuffer(int p) {
        mProgress.setSecondaryProgress(p * 10);
    }

    @Override
    public void loading(boolean l) {
        mProgress.setLoading(l);
    }

    private Runnable mUpdateProgress = new Runnable() {

        @Override
        public void run() {

            if (mProgress != null) {
                long position = player.mediaPlayer.getCurrentPosition();
                long duration = Long.parseLong(model.getDuration()) * 1000;
                if (duration > 0 && duration < 627080716) {
                    mProgress.setProgress((int) (1000 * position / duration));
                    mTimePlayed.setText(MusicUtils.makeTimeString(position));
                }

                if (MusicPlayer.isPlaying()) {
                    mProgress.postDelayed(mUpdateProgress, 200);
                } else {
                    mProgress.removeCallbacks(mUpdateProgress);
                }
            }
        }
    };

    private void setSeekBarListener() {

        if (mProgress != null)
            mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int progress = 0;

                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    this.progress = progress * player.mediaPlayer.getDuration()
                            / seekBar.getMax();
                    //设置流动时间 这个地方每一次运行就开始有反应
                    mTimePlayed.setText(MusicUtils.makeTimeString(player.mediaPlayer.getCurrentPosition()));
//                    mTimePlayed.setText(MusicUtils.makeTimeString( System.currentTimeMillis() ));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    player.mediaPlayer.seekTo(progress);
                }
            });
    }

    private void stopAnim() {
        mActiveView = null;

        if (mRotateAnim != null) {
            mRotateAnim.end();
            mRotateAnim = null;
        }
        if (mNeedleAnim != null) {
            mNeedleAnim.end();
            mNeedleAnim = null;
        }
        if (mAnimatorSet != null) {
            mAnimatorSet.end();
            mAnimatorSet = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayHandler.removeCallbacksAndMessages(null);
        mPlayHandler.getLooper().quit();
        mPlayHandler = null;

        mProgress.removeCallbacks(mUpdateProgress);
        stopAnim();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopAnim();
        mProgress.removeCallbacks(mUpdateProgress);
        finish();
        player.mediaPlayer.release();
        player.mediaPlayer.release();
        player.mediaPlayer = null;
    }


    public class PlaybarPagerTransformer implements ViewPager.PageTransformer {


        @Override
        public void transformPage(View view, float position) {

            if (position == 0) {
                if (MusicPlayer.isPlaying()) {
                    mRotateAnim = (ObjectAnimator) view.getTag(R.id.tag_animator);
                    if (mRotateAnim != null && !mRotateAnim.isRunning() && mNeedleAnim != null) {
                        mAnimatorSet = new AnimatorSet();
                        mAnimatorSet.play(mNeedleAnim).before(mRotateAnim);
                        mAnimatorSet.start();
                    }
                }

            } else if (position == -1 || position == -2 || position == 1) {

                mRotateAnim = (ObjectAnimator) view.getTag(R.id.tag_animator);
                if (mRotateAnim != null) {
                    mRotateAnim.setFloatValues(0);
                    mRotateAnim.end();
                    mRotateAnim = null;
                }
            } else {

                if (mNeedleAnim != null) {
                    mNeedleAnim.reverse();
                    mNeedleAnim.end();
                }

                mRotateAnim = (ObjectAnimator) view.getTag(R.id.tag_animator);
                if (mRotateAnim != null) {
                    mRotateAnim.cancel();
                    float valueAvatar = (float) mRotateAnim.getAnimatedValue();
                    mRotateAnim.setFloatValues(valueAvatar, 360f + valueAvatar);

                }
            }
        }

    }


    private void setDrawable(Drawable result) {
        if (result != null) {
            if (mBackAlbum.getDrawable() != null) {
                final TransitionDrawable td =
                        new TransitionDrawable(new Drawable[]{mBackAlbum.getDrawable(), result});


                mBackAlbum.setImageDrawable(td);
                //去除过度绘制
                td.setCrossFadeEnabled(true);
                td.startTransition(200);

            } else {
                mBackAlbum.setImageDrawable(result);
            }
        }
    }

    class FragmentAdapter extends FragmentStatePagerAdapter {

        private int mChildCount = 0;

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            //默认的圆封面
            return RoundFragment.newInstance("");
        }

        @Override
        public int getCount() {
            //左右各加一个
            return 1;
        }


        @Override
        public void notifyDataSetChanged() {
            mChildCount = getCount();
            super.notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            if (mChildCount > 0) {
                mChildCount--;
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }

    }

    public class MyScroller extends Scroller {
        private int animTime = VIEWPAGER_SCROLL_TIME;

        public MyScroller(Context context) {
            super(context);
        }

        public MyScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, animTime);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, animTime);
        }

        public void setmDuration(int animTime) {
            this.animTime = animTime;
        }
    }

    public class PlayMusic extends Thread {
        public void run() {
            if (Looper.myLooper() == null)
                Looper.prepare();
            mPlayHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case PRE_MUSIC:
//                            MusicPlayer.previous(PlayingOnlineActivity.this,true);
                            break;
                        case NEXT_MUSIC:
//                            MusicPlayer.next();
                            break;
                        case 3:
//                            MusicPlayer.setQueuePosition(msg.arg1);
                            break;
                    }
                }
            };

            Looper.loop();

        }
    }


}