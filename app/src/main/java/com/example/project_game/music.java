package com.example.project_game;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class music extends Service {

    private MediaPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化MediaPlayer等操作
        player = MediaPlayer.create(this, R.raw.awaltzfornaseem);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        playmusic();
        return START_STICKY;
    }

    private void playmusic() {
        player = MediaPlayer.create(this, R.raw.awaltzfornaseem);
        player.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 停止播放音乐并释放资源
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }
}