package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.example.musicplayer.interfaces.IPlayControl;
import com.example.musicplayer.interfaces.IPlayViewControl;
import com.example.musicplayer.services.PlayerService;

import static com.example.musicplayer.interfaces.IPlayControl.PLAY_STATE_PAUSE;
import static com.example.musicplayer.interfaces.IPlayControl.PLAY_STATE_PLAY;
import static com.example.musicplayer.interfaces.IPlayControl.PLAY_STATE_STOP;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private SeekBar seekBar;
    private Button startOrPause;
    private Button close;
    private myServiceConnection connection;
    private boolean isBind;
    private IPlayControl playControl;

    private static final String TAG = "MainActivity";
    private static boolean SEEK_CHANGED_STATE = false;  //进度条是否在移动

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        //绑定音乐服务
        doBindPlayerService();

        initEvent();

        //动态请求访问内存权限
        int permission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

        if(permission != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
        }
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
    private void startService(){
        Log.d(TAG, "startService...");
        startService(new Intent(this,PlayerService.class));
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
     * 监听事件
     */
    private void initEvent() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //进度变化
                Log.d(TAG, "onProgressChanged...");
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
                SEEK_CHANGED_STATE = false;
                int progress = seekBar.getProgress();
                Log.d(TAG, "onStopTrackingTouch --> progress --> "+progress);
                if (playControl != null) {
                    playControl.seekTo(progress);
                }
            }
        });

        startOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击开始播放或暂停
                if (playControl != null) {
                    Log.d(TAG, "playOrPause...");
                    playControl.playOrPause();
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
    }


    /*
    * 初始化控件
    * */
    private void initView() {
        seekBar = findViewById(R.id.seekBar);
        startOrPause = findViewById(R.id.start_or_pause_btn);
        close = findViewById(R.id.close_btn);
    }

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

    private IPlayViewControl mIPlayViewControl = new IPlayViewControl() {
        @Override
        public void onPlayerStateChange(int state) {
            //播放状态改变
            switch(state){
                case PLAY_STATE_PLAY:
                    startOrPause.setText("暂停");
                    break;
                case PLAY_STATE_PAUSE:
                case PLAY_STATE_STOP:
                    startOrPause.setText("播放");
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
                    if (!SEEK_CHANGED_STATE) {
                        seekBar.setProgress(seek);
                    }
                }
            });
        }
    };
}
