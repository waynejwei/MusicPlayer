package com.example.musicplayer.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.musicplayer.presenter.PlayerPresenter;

/**
 * 音乐控制服务
 */
public class PlayerService extends Service {


    private PlayerPresenter playerPresenter;
    private static final String TAG = "PlayerService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate...");
        if (playerPresenter == null) {
            playerPresenter = new PlayerPresenter();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind...");
        return playerPresenter;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy...");
        if (playerPresenter != null) {
            playerPresenter = null;
        }
    }
}
