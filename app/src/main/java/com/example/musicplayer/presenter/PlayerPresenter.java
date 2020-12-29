package com.example.musicplayer.presenter;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.util.Log;


import com.example.musicplayer.R;
import com.example.musicplayer.interfaces.IMusicViewControl;
import com.example.musicplayer.interfaces.IPlayControl;
import com.example.musicplayer.interfaces.IPlayViewControl;
import com.example.musicplayer.model.Music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 音乐播放服务的逻辑层
 */
public class PlayerPresenter extends Binder implements IPlayControl, MediaPlayer.OnCompletionListener {

    private IPlayViewControl playViewControl1;   //控制UI
    private IMusicViewControl musicViewControl1;
    private static final String TAG = "PlayerPresenter";
    private static int mCurrentPlayerState = PLAY_STATE_STOP;  //停止状态
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private SeekTimerTask timerTask;
    private ArrayList<Music> musicList = new ArrayList<Music>(){{
            add(new Music("世间美好与你环环相扣", Music.BASIC_LOCATION+"song.mp3", R.drawable.song, "尹初七"));
            add(new Music("打上花火", Music.BASIC_LOCATION+"song2.mp3", R.drawable.song2, "米津玄师"));
            add(new Music("耗尽", Music.BASIC_LOCATION+"song3.mp3", R.drawable.song3, "薛之谦&郭聪明"));
            add(new Music("踏山河", Music.BASIC_LOCATION+"song4.mp3", R.drawable.song4, "是七叔呢"));
            add(new Music("夏天的风", Music.BASIC_LOCATION+"song5.mp3", R.drawable.song5,"温岚"));
            add(new Music("冬眠", Music.BASIC_LOCATION+"song6.mp3", R.drawable.song6,"司南"));
            add(new Music("大天蓬", Music.BASIC_LOCATION+"song7.mp3", R.drawable.song7,"李袁杰"));
            add(new Music("吹梦到西洲", Music.BASIC_LOCATION+"song8.mp3", R.drawable.song8,"久书"));
    }};
    public static int CURRENT_MUSIC = 0;   //当前播放音乐的位置
    public static int CURRENT_PLAYING_TYPE = 0;  //当前播放模式(0:循环，1:单曲循环，2:随机)
    public static int CURRENT_SEEK = 0;  //当前播放的进度
    private int currentPlayType;
    private int currentMusic;


    /**
     * 获取界面UI管理
     * @param playViewControl 界面控制对象
     */
    @Override
    public void registerViewController(IPlayViewControl playViewControl) {
        Log.d(TAG, "registerViewController...");
        if (playViewControl1 == null) {
            playViewControl1 = playViewControl;
        }
    }

    @Override
    public void unRegisterViewController() {
        Log.d(TAG, "unRegisterViewController...");
        playViewControl1 = null;
    }

    @Override
    public void registerMusicViewController(IMusicViewControl musicViewControl) {
        Log.d(TAG, "registerMusicViewController...");
        if (musicViewControl1 == null) {
            musicViewControl1 = musicViewControl;
        }
    }

    @Override
    public void unRegisterMusicViewController() {
        Log.d(TAG, "unRegisterMusicViewController...");
        musicViewControl1 = null;
    }

    /**
     * 播放按键设置
     */
    @Override
    public void playOrPause(String location) {
        Log.d(TAG, "playOrPause...");
        if (mCurrentPlayerState == PLAY_STATE_STOP) {
            //创建播放器
            initPlayer();
            //设置数据源
            try {
                mediaPlayer.setDataSource(location);
                mediaPlayer.prepare();
                mediaPlayer.start();
                startTimer();
                mCurrentPlayerState = PLAY_STATE_PLAY;  //设置为播放状态
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (mCurrentPlayerState == PLAY_STATE_PLAY) {
            //如果当前的状态为播放，则点击为暂停
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                stopTimer();
                mCurrentPlayerState = PLAY_STATE_PAUSE;
            }
        }else if (mCurrentPlayerState == PLAY_STATE_PAUSE){
            //如果当前的状态为暂停的话，就继续播放
            if (mediaPlayer != null) {
                mediaPlayer.start();
                startTimer();
                mCurrentPlayerState = PLAY_STATE_PLAY;
            }
        }
        //通知修改UI播放/暂停按钮的状态
        if (playViewControl1 != null) {
            playViewControl1.onPlayerStateChange(mCurrentPlayerState);
        }
        if (musicViewControl1 != null) {
            musicViewControl1.onPlayerStateChange(mCurrentPlayerState);
        }
    }


    @Override
    public void changeMusic(String location) {
        //当前状态为静止的时候，则创建音乐播放器，然后播放
        if (mCurrentPlayerState == PLAY_STATE_STOP) {
            //创建播放器
            initPlayer();
            //设置数据源
            try {
                mediaPlayer.setDataSource(location);
                mediaPlayer.prepare();
                mediaPlayer.start();
                startTimer();
                mCurrentPlayerState = PLAY_STATE_PLAY;  //设置为播放状态
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //当前正在播放其它音乐的时候，则释放音乐资源，然后播放
            mediaPlayer.reset();
            try{
                //TODO:切换太频繁会出问题，卡顿(或直接闪退)
                mediaPlayer.setDataSource(location);
                mediaPlayer.prepare();
                mediaPlayer.start();
                timerTask = null;  //设置时间计数器为空，重新设置计时
                startTimer();
                mCurrentPlayerState = PLAY_STATE_PLAY;  //设置为播放状态
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建播放器
     */
    private void initPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(this);   //设置结束的监听事件
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);  //设置播放类型(mp3)
        }
    }

    /**
     * 停止播放
     */
    @Override
    public void stopPlay() {
        Log.d(TAG, "stopPlay...");
        if (mediaPlayer != null){
            mediaPlayer.stop();
            stopTimer();  //释放计时器资源
            //更新UI播放界面
            mCurrentPlayerState = PLAY_STATE_STOP;
            if (playViewControl1 != null) {
                Log.d(TAG, "stopPlay --> mCurrentPlayState --> "+mCurrentPlayerState);
                playViewControl1.onSeekChange(0);   //进度条归0
                playViewControl1.onPlayerStateChange(mCurrentPlayerState);
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void seekTo(int seek) {
        Log.d(TAG, "seekTo --> seek --> "+seek);
        if (mediaPlayer != null) {
            int tarSeek = (int) (seek * 1.0f/100 * mediaPlayer.getDuration());//进度位置*所播放音乐的进度条精度
            mediaPlayer.seekTo(tarSeek);
        }
    }

    @Override
    public int getTotalDuration() {
        if (mediaPlayer != null){
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public int getCurrentDuration() {
        if (mediaPlayer != null){
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }


    /**
     * 打开时间管理器
     */
    private void startTimer(){
        if (timer == null) {
            timer = new Timer();
        }
        if (timerTask == null) {
            timerTask = new SeekTimerTask();
        }
        timer.schedule(timerTask,0,500); //5s更新一次
    }

    /**
     * 关闭时间管理
     */
    private void stopTimer(){
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //下一首歌
        if (playViewControl1 != null) {
            playViewControl1.onNextMusic();
        }
        if (musicViewControl1 != null){
            Music music = getNextMusic();
            Log.d(TAG, "next music is ——> "+music.toString());
            musicViewControl1.onMusicInfoChanged(music);
        }
    }

    /**
     * 获取下一首音乐
     * @return
     */
    private Music getNextMusic(){
        if (playViewControl1 != null) {
            currentPlayType = playViewControl1.getCurrentPlayType();
            currentMusic = playViewControl1.getCurrentMusic();
        }
        Log.d(TAG, "music ——> "+currentMusic);
        return musicList.get(currentMusic);
    }

    /**
     * 进度条随着音乐播放自动移动进度
     */
    private class SeekTimerTask extends TimerTask{

        @Override
        public void run() {
            if (mediaPlayer != null && playViewControl1 != null) {
                //获取当前进度
                int currentPosition = mediaPlayer.getCurrentPosition();
//                Log.d(TAG, "seekTimerTask --> currentSeek --> "+currentPosition);
                int curPosition = (int) (currentPosition*1.0f/mediaPlayer.getDuration()*100);
                playViewControl1.onSeekChange(curPosition);   //修改进度条
                if (musicViewControl1 != null){
                    musicViewControl1.onSeekChange(curPosition);
                    musicViewControl1.onCurrentTimeChange(currentPosition);
                }
            }
        }
    }


}
