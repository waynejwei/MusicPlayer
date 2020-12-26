package com.example.musicplayer.interfaces;

import com.example.musicplayer.model.Music;

/**
 * 音乐详情界面接口
 */
public interface IMusicViewControl {

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
     * 当前进度时间变化通知
     * @param duration 时间
     */
    void onCurrentTimeChange(int duration);

    /**
     * 修改播放暂停键的状态,设置为播放
     */
    void onNextMusic();

    /**
     * 修改音乐信息
     * @param music 音乐
     */
    void onMusicInfoChanged(Music music);

}
