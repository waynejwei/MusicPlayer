# MusicPlayer
混合开启服务案例——音乐播放器
**Android案例：`MusicPlayer`**

> 参考视频：<https://www.bilibili.com/video/BV1it411j7yN?p=16>

- **项目界面：**一个进度条(seekBar)，一个按钮(播放/暂停)，一个停止按钮(进度从头开始)

- **服务接口：**

  - **UI界面变化通知接口——`IPlayViewControl`**

  ```java
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
  }
  ```

  

  - **音乐播放控制接口——`IPlayControl`**

  主要对音乐的播放/暂停、停止、进度条调节还有UI界面的逻辑管理

  ```java
  public interface IPlayControl {
      /**
       * 播放状态
       */
      int PLAY_STATE_PLAY = 1;   //播放
      int PLAY_STATE_PAUSE = 2;  //暂停
      int PLAY_STATE_STOP = 3;   //停止
  
      /**
       * 把UI设置传递给逻辑层
       * @param playViewControl
       */
      void registerViewController(IPlayViewControl playViewControl);
  
      /**
       * 取消设置状态通知
       */
      void unRegisterViewController();
  
      /**
       * 播放/暂停音乐
       */
      void playOrPause();
  
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
  ```

- **服务层——PlayerService**

打开服务、绑定服务、返回绑定类的实体、解绑等

**注意注册**

- **逻辑层——PlayerPresenter**

继承Binder，并且实现IPlayControl

音乐的播放使用的是`MediaPlayer`

进度条的管理使用的是时间管理起`Timer`

- **主活动层——`MainActivity`**

初始界面、绑定服务(这里使用的是显示绑定的方式)、设置监听事件
