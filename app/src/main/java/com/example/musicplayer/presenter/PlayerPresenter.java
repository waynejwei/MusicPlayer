package com.example.musicplayer.presenter;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.util.Log;


import com.example.musicplayer.interfaces.IPlayControl;
import com.example.musicplayer.interfaces.IPlayViewControl;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerPresenter extends Binder implements IPlayControl {

    private IPlayViewControl playViewControl1;
    private static final String TAG = "PlayerPresenter";
    private static int mCurrentPlayerState = PLAY_STATE_STOP;
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private SeekTimerTask timerTask;

    /**
     * 获取界面UI管理
     * @param playViewControl
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

    /**
     * 播放按键设置
     */
    @Override
    public void playOrPause() {
        Log.d(TAG, "playOrPause...");
        if (mCurrentPlayerState == PLAY_STATE_STOP) {
            //创建播放器
            initPlayer();
            //设置数据源
            try {
                mediaPlayer.setDataSource("/mnt/sdcard/Music/DAOKO,米津玄師 - 打上花火.mp3");
                mediaPlayer.prepare();
                mediaPlayer.start();
                startTimer();
                mCurrentPlayerState = PLAY_STATE_PLAY;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (mCurrentPlayerState == PLAY_STATE_PLAY){
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
    }

    /**
     * 创建播放器
     */
    private void initPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    /**
     * 停止播放
     */
    @Override
    public void stopPlay() {
        Log.d(TAG, "stopPlay...");
        mediaPlayer.stop();
        stopTimer();
        //更新UI播放界面
        mCurrentPlayerState = PLAY_STATE_STOP;
        if (playViewControl1 != null) {
            Log.d(TAG, "stopPlay --> mCurrentPlayState --> "+mCurrentPlayerState);
            playViewControl1.onPlayerStateChange(mCurrentPlayerState);
        }
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public void seekTo(int seek) {
        Log.d(TAG, "seekTo --> seek --> "+seek);
        if (mediaPlayer != null) {
            int tarSeek = (int) (seek * 1.0f/100 * mediaPlayer.getDuration());//进度位置*所播放音乐的进度条精度
            mediaPlayer.seekTo(tarSeek);
        }
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
        timer.schedule(timerTask,0,500);
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

    private class SeekTimerTask extends TimerTask{

        @Override
        public void run() {
            if (mediaPlayer != null && playViewControl1 != null) {
                //获取当前进度
                int currentPosition = mediaPlayer.getCurrentPosition();
//                Log.d(TAG, "seekTimerTask --> currentSeek --> "+currentPosition);
                int curPosition = (int) (currentPosition*1.0f/mediaPlayer.getDuration()*100);
                playViewControl1.onSeekChange(curPosition);   //修改进度条
            }
        }
    }
}
