package com.example.musicplayer.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.musicplayer.R;
import com.example.musicplayer.interfaces.IMusicViewControl;
import com.example.musicplayer.interfaces.IPlayControl;
import com.example.musicplayer.model.Music;
import com.example.musicplayer.services.PlayerService;

import static com.example.musicplayer.activity.MainActivity.CURRENT_PLAYING_TYPE;
import static com.example.musicplayer.custom.RoundRectImageView.getRoundBitmapByShader;
import static com.example.musicplayer.interfaces.IPlayControl.PLAY_STATE_PAUSE;
import static com.example.musicplayer.interfaces.IPlayControl.PLAY_STATE_PLAY;
import static com.example.musicplayer.interfaces.IPlayControl.PLAY_STATE_STOP;

/**
 * 音乐详情
 */
public class MusicInfoActivity extends AppCompatActivity {

    private static final String TAG = "MusicInfoActivity";
    private ImageView musicCover;
    private TextView musicName;
    private TextView musicAuthor;
    private TextView nowTime;
    private TextView totalTime;
    private SeekBar seekBar;
    private ImageView lastButton;
    private ImageView startOrPauseButton;
    private ImageView nextButton;
    private ImageView loopButton;
    private Music music;
    private IPlayControl playControl;
    private static boolean SEEK_CHANGED_STATE = false;  //进度条是否在移动
    private int musicSeek;
    private int totalDuration;
    private MainActivity mMainActivity;
    private myServiceConnection connection;
    private boolean isBind;
    private int currentDuration;
    private int currentPlayingState;
    private int currentPlayingType;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_info);
        //获取MainActivity的实例，调用它的方法
        mMainActivity = MainActivity.getInstance();
        //获取当前播放音乐信息、进度
        Intent intent = getIntent();
        music = (Music) intent.getSerializableExtra("music");
        musicSeek = intent.getIntExtra("musicSeek", 0);
        totalDuration = intent.getIntExtra("totalDuration", 0);
        currentDuration = intent.getIntExtra("currentDuration", 0);
        currentPlayingState = intent.getIntExtra("currentPlayingState", PLAY_STATE_PAUSE);
        currentPlayingType = intent.getIntExtra("currentPlayingType", 0);
        Log.d(TAG, "currentPlayingState is ——> " + currentPlayingState);
        //绑定音乐服务
        doBindPlayerService();

        initView();

        //设置初始化布局
        setView(music);

        initEvent();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        musicCover = findViewById(R.id.music_cover);
        musicName = findViewById(R.id.music_name);
        musicAuthor = findViewById(R.id.music_author);
        nowTime = findViewById(R.id.now_time);
        totalTime = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.music_seek);
        seekBar.setProgress(musicSeek);
        lastButton = findViewById(R.id.last_music_btn);
        startOrPauseButton = findViewById(R.id.start_or_pause_btn);
        nextButton = findViewById(R.id.next_music_btn);
        loopButton = findViewById(R.id.loop_btn);
    }

    /**
     * 设置布局中的音乐
     * @param music
     */
    private void setView(Music music){
        //设置圆角图片
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), music.getImageId());
        Bitmap outBitmap =getRoundBitmapByShader(bitmap, 500,300,20, 3);
        musicCover.setImageBitmap(outBitmap);
        musicName.setText(music.getName());
        musicAuthor.setText(music.getAuthor());
        nowTime.setText(timeParse(currentDuration));
        if (playControl != null) {
            totalDuration = playControl.getTotalDuration();
        }
        totalTime.setText(timeParse(totalDuration));
        seekBar.setProgress(musicSeek);
        //开始和结束按键
        if (currentPlayingState == PLAY_STATE_PAUSE || currentPlayingState == PLAY_STATE_STOP){
            startOrPauseButton.setBackgroundResource(R.drawable.start_btn_white);
        } else {
            startOrPauseButton.setBackgroundResource(R.drawable.stop_btn_white);
        }
        setLoopButton();
    }

    /**
     * 设置播放模式按钮
     */
    private void setLoopButton(){
        switch (currentPlayingType){
            case 0:
                loopButton.setBackgroundResource(R.drawable.loop);
                break;
            case 1:
                loopButton.setBackgroundResource(R.drawable.single_loop);
                break;
            case 2:
                loopButton.setBackgroundResource(R.drawable.random);
                break;
            default:
                break;
        }
    }

    /**
     * 设置事件
     */
    private void initEvent() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                SEEK_CHANGED_STATE = true;
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //松开进度条
                mMainActivity.onStopSeekBar(seekBar);  //调节音乐进度
                if (playControl != null) {  //修改当前时间
                    currentDuration = playControl.getCurrentDuration();
                    nowTime.setText(timeParse(currentDuration));
                }
                SEEK_CHANGED_STATE = false;
            }
        });
        startOrPauseButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                mMainActivity.onStartOrPauseClicked();  //调用MainActivity中的方法
            }
        });
        lastButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                //下一首
                Music music = mMainActivity.playLastMusic();
                setView(music);
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                Music music = mMainActivity.playNextMusic();
                setView(music);
            }
        });
        loopButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                mMainActivity.onLoopButtonClicked();
                currentPlayingType = (currentPlayingType + 1) % 3;
                setLoopButton();
            }
        });
    }

    /**
     * 绑定服务
     */
    private void doBindPlayerService() {
        Intent intent = new Intent(this, PlayerService.class);
        if (connection == null) {
            connection = new myServiceConnection();
        }
        isBind = bindService(intent, connection, BIND_AUTO_CREATE);
        Log.d(TAG, "doBindPlayerService --> isBind --> "+ isBind);
    }

    /**
     * 连接服务
     */
    private class myServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected --> name --> "+name);
            playControl = (IPlayControl) service;
            playControl.registerMusicViewController(mIMusicViewControl);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected --> name --> "+name);
            playControl = null;
        }
    }

    /**
     * 通过接口实现界面UI的控制
     */
    private IMusicViewControl mIMusicViewControl = new IMusicViewControl() {

        @Override
        public void onPlayerStateChange(int state) {
            //播放状态改变
            Log.d(TAG, "current state is ——> " + state);
            switch(state){
                case PLAY_STATE_PLAY:
                    startOrPauseButton.setBackgroundResource(R.drawable.stop_btn_white);
                    break;
                case PLAY_STATE_PAUSE:
                case PLAY_STATE_STOP:
                    startOrPauseButton.setBackgroundResource(R.drawable.start_btn_white);
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
                    }
                }
            });
        }

        @Override
        public void onCurrentTimeChange(final int duration) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!SEEK_CHANGED_STATE) {
                        //当没有移动进度条的时候修改当前时间
                        nowTime.setText(timeParse(duration));
                    }
                }
            });
        }

        @Override
        public void onNextMusic() {

        }

        @Override
        public void onMusicInfoChanged(Music music) {
            setView(music);
            seekBar.setProgress(0);   //重新开始
        }

    };


    /**
     * 将duration转为分秒
     * @param duration 时间(毫秒)
     * @return 时间(分:秒)
     */
    public static String timeParse(int duration) {
        String time = "";
        int minute = duration / 60000;
        int seconds = duration % 60000;
        int second = Math.round((float)seconds/1000);
        if (minute < 10) {
            time += "0";
        }
        time += minute+":";
        if (second < 10) {
            time += "0";
        }
        time += second;
        return time;
    }

    /**
     * 结束的时候解绑服务，并释放资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBind && connection != null) {
            //释放资源
            playControl.unRegisterMusicViewController();
            Log.d(TAG, "onDestroy...");
            unbindService(connection);
        }
    }
}