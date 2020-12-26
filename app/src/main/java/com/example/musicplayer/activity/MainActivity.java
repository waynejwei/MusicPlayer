package com.example.musicplayer.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.MusicAdapter;
import com.example.musicplayer.custom.ScrollingTextView;
import com.example.musicplayer.interfaces.IPlayControl;
import com.example.musicplayer.interfaces.IPlayViewControl;
import com.example.musicplayer.model.Music;
import com.example.musicplayer.services.PlayerService;

import java.util.ArrayList;
import java.util.List;

import static com.example.musicplayer.interfaces.IPlayControl.PLAY_STATE_PAUSE;
import static com.example.musicplayer.interfaces.IPlayControl.PLAY_STATE_PLAY;
import static com.example.musicplayer.interfaces.IPlayControl.PLAY_STATE_STOP;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity implements MusicAdapter.onListenItemClickListen {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private SeekBar seekBar;
    private Button close;
    private Button nextButton;
    private Button lastButton;
    private ImageView startOrPause;
    private ImageView playingType;   //播放的模式
    private ScrollingTextView musicName;
    private myServiceConnection connection;
    private boolean isBind;
    private IPlayControl playControl;
    private MusicAdapter musicAdapter;
    private ConstraintLayout musicInfo;
    private SharedPreferences mySharedPreferences;  //记录上一次播放的音乐
    public static int CURRENT_MUSIC = 0;   //当前播放音乐的位置
    public static int CURRENT_PLAYING_TYPE = 0;  //当前播放模式(0:循环，1:单曲循环，2:随机)
    public static int CURRENT_SEEK = 0;  //当前播放的进度
    private static final String TAG = "MainActivity";
    public static boolean SEEK_CHANGED_STATE = false;  //进度条是否在移动
    private List<Music> musicList;
    private static MainActivity mMainActivity = null;
    private static int CURRENT_PLAYING_STATE = PLAY_STATE_PAUSE;   //当前播放状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainActivity = this;

        //加载数据
        loadData();

        //绑定音乐服务
        doBindPlayerService();

        initView();

        initEvent();


        //动态请求访问内存权限
        int permission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

        if(permission != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * 外界获取MainActivity实例的方法
     * @return MainActivity
     */
    public static MainActivity getInstance(){
        if (mMainActivity != null) {
            return mMainActivity;
        }
        return null;
    }

    /**
     * 加载数据
     */
    private void loadData() {
        musicList = new ArrayList<>();
        Music music1 = new Music("世间美好与你环环相扣", Music.BASIC_LOCATION+"song.mp3", R.drawable.song, "尹初七");
        musicList.add(music1);
        Music music2 = new Music("打上花火", Music.BASIC_LOCATION+"song2.mp3", R.drawable.song2, "米津玄师");
        musicList.add(music2);
        Music music3 = new Music("耗尽", Music.BASIC_LOCATION+"song3.mp3", R.drawable.song3, "薛之谦&郭聪明");
        musicList.add(music3);
        Music music4 = new Music("踏山河", Music.BASIC_LOCATION+"song4.mp3", R.drawable.song4, "是七叔呢");
        musicList.add(music4);
        Music music5 = new Music("夏天的风", Music.BASIC_LOCATION+"song5.mp3", R.drawable.song5,"温岚");
        musicList.add(music5);
        Music music6 = new Music("冬眠", Music.BASIC_LOCATION+"song6.mp3", R.drawable.song6,"司南");
        musicList.add(music6);
        Music music7 = new Music("大天蓬", Music.BASIC_LOCATION+"song7.mp3", R.drawable.song7,"李袁杰");
        musicList.add(music7);
        Music music8 = new Music("吹梦到西洲", Music.BASIC_LOCATION+"song8.mp3", R.drawable.song8,"久书");
        musicList.add(music8);
    }

    /*
     * 初始化控件
     * */
    private void initView() {
        Log.d(TAG, "initView ...");
        mySharedPreferences = getSharedPreferences("last_music_player",MODE_PRIVATE);
        seekBar = findViewById(R.id.seekBar);
        //设置初始化的播放的位置
        if (playControl != null) {
            //TODO:如何设置进入回显，此处playControl为空
            CURRENT_SEEK = mySharedPreferences.getInt("lastMusicSeek", 0);
            seekBar.setProgress(CURRENT_SEEK);
            playControl.seekTo(CURRENT_SEEK);
        }
        startOrPause = findViewById(R.id.start_or_pause);
        musicName = findViewById(R.id.music_name_text);
        //设置初始化的音乐名称
        if (mySharedPreferences != null) {
            String lastName = mySharedPreferences.getString("lastMusicName", musicList.get(1).getName());
            musicName.setText(lastName);
            CURRENT_MUSIC = mySharedPreferences.getInt("lastMusicPosition", 0);
        } else {
            musicName.setText(musicList.get(1).getName());
        }
        //初始化选择状态
        playingType = findViewById(R.id.play_type);
        CURRENT_PLAYING_TYPE = mySharedPreferences.getInt("currentPlayingType", 0);
        setLoopButton();
        close = findViewById(R.id.close_btn);
        nextButton = findViewById(R.id.next_btn);
        lastButton = findViewById(R.id.last_btn);
        musicInfo = findViewById(R.id.music_info);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        musicAdapter = new MusicAdapter();
        musicAdapter.setData(musicList);
        recyclerView.setAdapter(musicAdapter);
        //设置分割线
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = 8;
                outRect.bottom = 8;
            }
        });
    }

    /**
     * 监听事件
     */
    private void initEvent() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //进度变化
//                Log.d(TAG, "onProgressChanged...");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //触碰进度条
                Log.d(TAG, "onStartTrackingTouch...");
                SEEK_CHANGED_STATE = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //松开进度条
                onStopSeekBar(seekBar);
            }
        });

        startOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击开始播放或暂停
                if (playControl != null) {
                    Log.d(TAG, "playOrPause...");
//                    String lastMusicLocation = mySharedPreferences.getString("lastMusicLocation", Music.BASIC_LOCATION + "song.mp3");
//                    String lastName = mySharedPreferences.getString("lastMusicName", musicList.get(0).getName());
//                    int lastMusicPosition = mySharedPreferences.getInt("lastMusicPosition", 0);
//                    if (CURRENT_MUSIC != 0){
//                        playControl.playOrPause(musicList.get(CURRENT_MUSIC).getLocation());
//                    }else{
//                        playControl.playOrPause(lastMusicLocation);
//                        CURRENT_MUSIC = lastMusicPosition;
//                    }
//                    saveInSharedPreferences(musicList.get(CURRENT_MUSIC));
//                    musicName.setText(lastName);
                    onStartOrPauseClicked();
                }
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭播放
                if (playControl != null) {
                    Log.d(TAG, "stopPlay...");
                    playControl.stopPlay();
                }
            }
        });
        musicAdapter.setOnListenItemClickListen(this);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getNextMusic();
//                Music music = musicList.get(CURRENT_MUSIC); //(CURRENT_MUSIC+1)%(musicList.size())
//                Log.d(TAG, "playing type is ——> "+CURRENT_PLAYING_TYPE);
//                Log.d(TAG, "next music is ——> "+music.toString());
//                playControl.changeMusic(music.getLocation());
//                startOrPause.setBackgroundResource(R.drawable.stop_btn_white);
////                CURRENT_MUSIC = (CURRENT_MUSIC+1)%(musicList.size());
//                saveInSharedPreferences(music);
//                setMusicName();
                playNextMusic();
            }
        });
        lastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getLastMusic();
//                Music music = musicList.get(CURRENT_MUSIC);
//                Log.d(TAG, "last music is ——> "+music.toString());
//                playControl.changeMusic(music.getLocation());
//                startOrPause.setBackgroundResource(R.drawable.stop_btn_white);
////                CURRENT_MUSIC = (CURRENT_MUSIC-1+musicList.size())%(musicList.size());
//                saveInSharedPreferences(music);
//                setMusicName();
                playLastMusic();
            }
        });

        playingType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoopButtonClicked();
            }
        });
        musicInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击跳转到音乐详情
                Intent intent = new Intent(MainActivity.this, MusicInfoActivity.class);
                intent.putExtra("music", musicList.get(CURRENT_MUSIC));
                intent.putExtra("musicSeek",CURRENT_SEEK);
                int totalDuration = mySharedPreferences.getInt("totalDuration", 0);
                intent.putExtra("totalDuration", totalDuration);
                int currentDuration = mySharedPreferences.getInt("currentDuration", 0);
                intent.putExtra("currentDuration", currentDuration);
                intent.putExtra("currentPlayingState", CURRENT_PLAYING_STATE);
                intent.putExtra("currentPlayingType", CURRENT_PLAYING_TYPE);
                startActivity(intent);
            }
        });
    }

    /**
     * 点击播放模式的按钮时
     */
    public void onLoopButtonClicked() {
        //点击一下，CURRENT_PLAYING_TYPE完后+1(循环)
        CURRENT_PLAYING_TYPE = (CURRENT_PLAYING_TYPE + 1) % 3;
        setLoopButton();
    }

    /**
     * 设置播放模式按钮
     */
    private void setLoopButton(){
        switch (CURRENT_PLAYING_TYPE){
            case 0:
                playingType.setBackgroundResource(R.drawable.loop);
                break;
            case 1:
                playingType.setBackgroundResource(R.drawable.single_loop);
                break;
            case 2:
                playingType.setBackgroundResource(R.drawable.random);
                break;
            default:
                break;
        }
    }

    /**
     * 播放上一首音乐
     */
    public Music playLastMusic() {
        getLastMusic();
        Music music = musicList.get(CURRENT_MUSIC);
        Log.d(TAG, "last music is ——> "+music.toString());
        playControl.changeMusic(music.getLocation());
        startOrPause.setBackgroundResource(R.drawable.stop_btn_white);
//                CURRENT_MUSIC = (CURRENT_MUSIC-1+musicList.size())%(musicList.size());
        saveInSharedPreferences(music);
        setMusicName();
        return music;
    }

    /**
     * 播放下一首音乐
     */
    public Music playNextMusic() {
        getNextMusic();
        Music music = musicList.get(CURRENT_MUSIC); //(CURRENT_MUSIC+1)%(musicList.size())
        Log.d(TAG, "playing type is ——> "+CURRENT_PLAYING_TYPE);
        Log.d(TAG, "next music is ——> "+music.toString());
        playControl.changeMusic(music.getLocation());
        startOrPause.setBackgroundResource(R.drawable.stop_btn_white);
//                CURRENT_MUSIC = (CURRENT_MUSIC+1)%(musicList.size());
        saveInSharedPreferences(music);
        setMusicName();
        return music;
    }

    /**
     * 点击开始或结束按钮后改变view、控制音乐播放、记录音乐
     */
    public void onStartOrPauseClicked() {
        String lastMusicLocation = mySharedPreferences.getString("lastMusicLocation", Music.BASIC_LOCATION + "song.mp3");
        String lastName = mySharedPreferences.getString("lastMusicName", musicList.get(0).getName());
        int lastMusicPosition = mySharedPreferences.getInt("lastMusicPosition", 0);
        if (CURRENT_MUSIC != 0){
            playControl.playOrPause(musicList.get(CURRENT_MUSIC).getLocation());
        }else{
            playControl.playOrPause(lastMusicLocation);
            CURRENT_MUSIC = lastMusicPosition;
        }
        saveInSharedPreferences(musicList.get(CURRENT_MUSIC));
        musicName.setText(lastName);
    }

    /**
     * 松开seekBar之后调整音乐进度
     * @param seekBar
     */
    public void onStopSeekBar(SeekBar seekBar) {
        SEEK_CHANGED_STATE = false;
        int progress = seekBar.getProgress();
        CURRENT_SEEK = progress;
        Log.d(TAG, "onStopTrackingTouch --> progress --> "+progress);
        if (playControl != null) {
            playControl.seekTo(progress);
        }
    }


    /**
     * 绑定音乐服务
     */
    private void doBindPlayerService() {
        Intent intent = new Intent(this, PlayerService.class);
        if (connection == null) {
            connection = new myServiceConnection();
        }
        isBind = bindService(intent, connection, BIND_AUTO_CREATE);
        Log.d(TAG, "doBindPlayerService --> isBind --> "+isBind);
    }


    /*
     * 处理申请用户权限的操作（访问内存）
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_REQUEST_CODE) {
            //判断结果
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG,"has permissions..");
                //有权限
            } else {
                Log.d(TAG,"no permissionS...");
                //没权限
                if(!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_CALENDAR)&&!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CALENDAR)) {
                    //走到这里，说明用户之前用户禁止权限的同时，勾选了不再询问
                    //那么，你需要弹出一个dialog，提示用户需要权限，然后跳转到设置里头去打开。
                    Log.d(TAG,"用户之前勾选了不再询问...");
                    //TODO:弹出一个框框，然后提示用户说需要开启权限。
                    //TODO:用户点击确定的时候，跳转到设置里去
                    //Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    //Uri uri = Uri.fromParts("package", getPackageName(), null);
                    //intent.setData(uri);
                    ////在activity结果范围的地方，再次检查是否有权限
                    //startActivityForResult(intent, PERMISSION_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_CALENDAR,Manifest.permission.READ_CALENDAR}, PERMISSION_REQUEST_CODE);
                    //请求权限
                    Log.d(TAG,"请求权限...");
                }
            }
        }
    }

    /**
     * 开始音乐服务
     */
//    private void startService(){
//        Log.d(TAG, "startService...");
//        startService(new Intent(this,PlayerService.class));
//    }


    /**
     * 获取下一首音乐
     */
    private void getNextMusic(){
        switch (CURRENT_PLAYING_TYPE){
            case 0:
            case 1:
                //循环或者单曲循环的上一首
                CURRENT_MUSIC = (CURRENT_MUSIC + 1) % musicList.size();
                break;
            case 2:
                //随机播放
                int randomMusic = (int) (Math.random() * musicList.size());
                CURRENT_MUSIC = (randomMusic != CURRENT_MUSIC) ? randomMusic : (CURRENT_MUSIC + 1) % musicList.size();
                break;
            default:
                break;
        }
    }

    /**
     * 获取上一首音乐
     */
    private void getLastMusic(){
        switch (CURRENT_PLAYING_TYPE){
            case 0:
            case 1:
                //循环或者单曲循环的下一首
                CURRENT_MUSIC = (CURRENT_MUSIC + musicList.size() - 1) % musicList.size();
                break;
            case 2:
                //随机播放
                int randomMusic = (int) (Math.random() * musicList.size());
                CURRENT_MUSIC = (randomMusic != CURRENT_MUSIC) ? randomMusic : (CURRENT_MUSIC + 1) % musicList.size();
                break;
            default:
                break;
        }
    }

    /**
     * 获取播放完后自动下一首的音乐
     */
    private void getCompletingNextMusic(){
        switch (CURRENT_PLAYING_TYPE){
            case 0:
                //循环或者单曲循环的上一首
                CURRENT_MUSIC = (CURRENT_MUSIC + 1) % musicList.size();
                break;
            case 2:
                //随机播放
                int randomMusic = (int) (Math.random() * musicList.size());
                CURRENT_MUSIC = (randomMusic != CURRENT_MUSIC) ? randomMusic : (CURRENT_MUSIC + 1) % musicList.size();
                break;
            default:
                break;
        }
    }


    /**
     * 覆写item点击事件接口
     * @param music 音乐
     * @param position 当前点击的位置
     */
    @Override
    public void onClickItem(Music music, int position) {
        //当点击正在播放的音乐的时候，不应该从头播放
        if (CURRENT_MUSIC != position){
            if (playControl != null){
                Log.d(TAG, "进入item的点击事件...");
                CURRENT_MUSIC = position;
                Log.d(TAG, "当前播放的音乐为 ——> "+CURRENT_MUSIC);
                playControl.changeMusic(music.getLocation());
                startOrPause.setBackgroundResource(R.drawable.stop_btn_white);
                saveInSharedPreferences(music);
                setMusicName();
            }
        }
    }

    /**
     * 将播放的音乐暂存
     * @param music 音乐
     */
    private void saveInSharedPreferences(Music music){
        //记录到音乐播放器中
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putString("lastMusicLocation", music.getLocation());
        editor.putString("lastMusicName", music.getName());
        editor.putInt("lastMusicPosition", musicList.indexOf(music));  //上一次的播放音乐的位置
        editor.putInt("lastMusicSeek", CURRENT_SEEK);  //上一次的播放进度
        if (playControl != null) {
            editor.putInt("totalDuration", playControl.getTotalDuration());  //获取音乐总时长
            editor.putInt("currentDuration", playControl.getCurrentDuration()); //当前播放时间
        }
        editor.putInt("currentPlayingType", CURRENT_PLAYING_TYPE);
        editor.apply();
    }

    /**
     * 连接服务
     */
    private class myServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected --> name --> "+name);
            playControl = (IPlayControl) service;
            playControl.registerViewController(mIPlayViewControl);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected --> name --> "+name);
            playControl = null;
        }
    }


    /**
     * 设置当前播放音乐的名字
     */
    private void setMusicName(){
        musicName.setText(musicList.get(CURRENT_MUSIC).getName());
    }



    /**
     * 结束的时候解绑服务，并释放资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBind && connection != null) {
            //释放资源
            playControl.unRegisterViewController();
            Log.d(TAG, "onDestroy...");
            unbindService(connection);
        }
    }

    /**
     * 通过接口实现界面UI的控制
     */
    private IPlayViewControl mIPlayViewControl = new IPlayViewControl() {
        @Override
        public void onPlayerStateChange(int state) {
            CURRENT_PLAYING_STATE = state;
            //播放状态改变
            switch(state){
                case PLAY_STATE_PLAY:
                    startOrPause.setBackgroundResource(R.drawable.stop_btn_white);
                    break;
                case PLAY_STATE_PAUSE:
                case PLAY_STATE_STOP:
                    startOrPause.setBackgroundResource(R.drawable.start_btn_white);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onSeekChange(final int seek) {
            //进度变化
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //当进度条没有在移动的时候，设置进度条的值
                    if (!SEEK_CHANGED_STATE) {
                        seekBar.setProgress(seek);
                        CURRENT_SEEK = seek;
                    }
                }
            });
        }

        @Override
        public void onNextMusic() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getCompletingNextMusic();
                    Music music = musicList.get(CURRENT_MUSIC);
                    Log.d(TAG, "next music is ——> "+music.toString());
                    playControl.changeMusic(music.getLocation());
                    startOrPause.setBackgroundResource(R.drawable.stop_btn_white);
//                    CURRENT_MUSIC = (CURRENT_MUSIC+1)%(musicList.size());
                    saveInSharedPreferences(music);
                    setMusicName();
                }
            });
        }

        @Override
        public int getCurrentMusic() {
            return CURRENT_MUSIC;
        }

        @Override
        public int getCurrentPlayType() {
            return CURRENT_PLAYING_TYPE;
        }
    };
}
