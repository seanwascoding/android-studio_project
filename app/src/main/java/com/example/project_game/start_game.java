package com.example.project_game;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class start_game extends AppCompatActivity {

    private ArrayList<Item> items = new ArrayList<>();
    private Adapter adapter = new Adapter(items, R.layout.player, 15);
    private WebSocketClient webSocketClient;
    private ProgressBar blood;
    private JSONObject json_open = new JSONObject();
    private String name;
    private MediaPlayer music;
    private AtomicInteger user_blood = new AtomicInteger(5);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_game);

        /** declare */
        music = MediaPlayer.create(this, R.raw.abstractanxiety);
        music.setLooping(true);
        music.start();
        RecyclerView recyclerView = findViewById(R.id.recyclerView2);
        SeekBar random_value = findViewById(R.id.random_value);
        Button attack = findViewById(R.id.attack);
        Button defend = findViewById(R.id.defend);
        Button click = findViewById(R.id.click);
        blood = findViewById(R.id.blood);
        blood.setProgress(1000);
        random_value.setEnabled(false);
        click.setEnabled(false);

        /** data */
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        String key = intent.getStringExtra("key");
        ArrayList<Item> items_temp = (ArrayList<Item>) intent.getSerializableExtra("player");
        for (Item item : items_temp) {
            items.add(new Item(item.name, item.state));
        }

        //
        try {
            json_open = new JSONObject();
            json_open.put("5", key);
            json_open.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /** attack */
        AtomicInteger i = new AtomicInteger();
        JSONObject json_attack = new JSONObject();
        AtomicBoolean hit = new AtomicBoolean(false);
        Random random = new Random();
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (hit.get()) {
                    click.setEnabled(false);
                    attack.setEnabled(true);
                    defend.setEnabled(true);
                    random_value.setProgress(0);
                    if (i.get() == 1) {
                        try {
                            json_attack.put("4", "attack");
                            json_attack.put("value", random.nextInt(11) * 10);
                            json_attack.put("name", name);
                            webSocketClient.send(json_attack.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (i.get() == 2) {
                        try {
                            json_attack.put("4", "defend");
                            json_attack.put("value", random.nextInt(11) * 10);
                            json_attack.put("name", name);
                            webSocketClient.send(json_attack.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }
                random_value.setProgress(random.nextInt(6) * 20);
                handler.postDelayed(this, 1000);
            }
        };
        attack.setOnClickListener(v -> {
            Toast.makeText(this, "你選擇了攻擊模式", Toast.LENGTH_SHORT).show();
            i.set(1);
            attack.setEnabled(false);
            defend.setEnabled(false);
            click.setEnabled(true);
            hit.set(false);
            handler.postDelayed(runnable, 2000);
        });

        /** defend */
        defend.setOnClickListener(v -> {
            Toast.makeText(this, "你選擇了防禦模式", Toast.LENGTH_SHORT).show();
            i.set(2);
            defend.setEnabled(false);
            attack.setEnabled(false);
            click.setEnabled(true);
            hit.set(false);
            handler.postDelayed(runnable, 2000);
        });

        /** hit */
        click.setOnClickListener(v -> {
            click.setEnabled(false);
            if (random_value.getProgress() == 20) {
                hit.set(true);
                handler.removeCallbacks(runnable);
                runnable.run();
            } else {
                click.setEnabled(false);
                attack.setEnabled(true);
                defend.setEnabled(true);
                random_value.setProgress(0);
                Toast.makeText(this, "Wrong position", Toast.LENGTH_SHORT).show();
                handler.removeCallbacks(runnable);
                user_blood.addAndGet(-1);
                Toast.makeText(start_game.this, "玩家生命剩下:" + user_blood, Toast.LENGTH_SHORT).show();
                if (user_blood.get() == 0) {
                    Toast.makeText(this, "失敗，請再挑戰", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, lobby.class).putExtra("account", name));
                    finish();
                }
            }
        });

        /** player_list */
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        connectWebSocket();

    }

    private void connectWebSocket() {
        URI uri_2;
        try {
            uri_2 = new URI("ws://192.168.1.108:8080/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        webSocketClient = new WebSocketClient(uri_2) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                // 連接成功
                Log.d("onOpen", "working to connect");
                webSocketClient.send(json_open.toString());
            }

            @Override
            public void onMessage(String message) {
                // 收到訊息
                Log.d("onMessage", message);

                try {
                    String message_temp = new JSONObject(message).getString("signal");
                    Log.d("message:", message_temp);

                    if (message_temp.equals("damage")) {
                        String moster_blood = new JSONObject(message).getString("value");
                        if (Integer.parseInt(moster_blood) > 0) {
                            blood.setProgress(Integer.parseInt(moster_blood));
                            runOnUiThread(()-> Toast.makeText(start_game.this, "Boss:"+Integer.parseInt(moster_blood), Toast.LENGTH_SHORT).show());
                        } else {
                            Log.d("kill the monster:", "victory");
                            runOnUiThread(() -> Toast.makeText(start_game.this, "Victory", Toast.LENGTH_SHORT).show());
                            startActivity(new Intent(start_game.this, lobby.class).putExtra("account", name));
                            finish();
                        }
                    } else if (message_temp.equals("loading")) {
                        Toast.makeText(start_game.this, "Working to loading", Toast.LENGTH_SHORT).show();
                    } else if (message_temp.equals("defend")) {
                        String result = new JSONObject(message).getString("value");
                        if (result.equals("防禦失敗")) {
                            user_blood.addAndGet(-1);
                        }
                        runOnUiThread(() -> {
                            Toast.makeText(start_game.this, result, Toast.LENGTH_SHORT).show();
                            Toast.makeText(start_game.this, "玩家生命剩下:" + user_blood, Toast.LENGTH_SHORT).show();
                            if (user_blood.get() == 0) {
                                Toast.makeText(start_game.this, "失敗，請再挑戰", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(start_game.this, lobby.class).putExtra("account", name));
                                startService(new Intent(start_game.this, music.class));
                            }
                        });
                    } else {
                        Log.d("error message:", "search fail");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                // 連線關閉
                Log.d("close", "close connect");
            }

            @Override
            public void onError(Exception ex) {
                // 連線錯誤
                Log.d("error", "error connect");
            }
        };
        webSocketClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocketClient.close();
        // 停止播放音乐并释放资源
        if (music != null) {
            music.stop();
            music.release();
            music = null;
        }
        startService(new Intent(start_game.this, music.class));
    }
}