package com.example.musicplayer.interfaces;

/**
 * 界面状态变化控制接口
 */
public interface IPlayViewControl {

    /**
     * 播放按钮状态转变(播放/暂停)的通知
     * @param state 状态
     */
    void onPlayerStateChange(int state);

    /**
     * 进度条变化的通知
     * @param seek 进度
     */
    void onSeekChange(int seek);

    /**
     * 修改播放暂停键的状态,设置为播放
     */
    void onNextMusic();
}
