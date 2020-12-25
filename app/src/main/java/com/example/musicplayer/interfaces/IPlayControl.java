package com.example.musicplayer.interfaces;

/**
 * 音乐播放控制接口
 */
public interface IPlayControl {


    /**
     * 播放状态
     */
    int PLAY_STATE_PLAY = 1;   //播放
    int PLAY_STATE_PAUSE = 2;  //暂停
    int PLAY_STATE_STOP = 3;   //停止


    /**
     * 把UI设置传递给逻辑层
     * @param playViewControl 界面管理对象
     */
    void registerViewController(IPlayViewControl playViewControl);

    /**
     * 取消设置状态通知
     */
    void unRegisterViewController();

    /**
     * 播放/暂停音乐
     */
    void playOrPause(String location);

    /**
     * 切换音乐
     * @param location 切换音乐的地址
     */
    void changeMusic(String location);

    /**
     * 停止播放
     */
    void stopPlay();

    /**
     * 调节播放进度条
     * @param seek 进度条位置
     */
    void seekTo(int seek);


}
