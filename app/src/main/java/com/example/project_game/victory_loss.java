package com.example.project_game;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class victory_loss extends AppCompatActivity {

    private MediaPlayer music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_victory_loss);
        ImageView gifImageView = findViewById(R.id.result);
        Button next=findViewById(R.id.next);
        Button leave=findViewById(R.id.leave);

        Intent intent=getIntent();
        if (intent.getStringExtra("result").equals("victory")){
            music = MediaPlayer.create(this, R.raw.victory);
            music.setLooping(true);
            music.start();
            Glide.with(this).asGif().load(R.drawable.victory).into(gifImageView);
        }
        else if(intent.getStringExtra("result").equals("loss")){
            music = MediaPlayer.create(this, R.raw.loss);
            music.setLooping(true);
            music.start();
            Glide.with(this).asGif().load(R.drawable.loss).into(gifImageView);
        }

        next.setOnClickListener(v->{
            startActivity(new Intent(this, lobby.class).putExtra("account", intent.getStringExtra("account")));
            finish();
        });

        leave.setOnClickListener(v->{
            Toast.makeText(this, "end game", Toast.LENGTH_SHORT).show();
            finish();
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止播放音乐并释放资源
        if (music != null) {
            music.stop();
            music.release();
            music = null;
        }
        startService(new Intent(victory_loss.this, music.class));
    }
}