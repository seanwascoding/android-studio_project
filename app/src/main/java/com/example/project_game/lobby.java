package com.example.project_game;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class lobby extends AppCompatActivity {

    private WebSocketClient webSocketClient = null;
    private boolean doubleBackToExitPressedOnce = false;
    private ArrayList<Item> items = new ArrayList<>();
    private Adapter adapter = new Adapter(items, R.layout.player);
    private RecyclerView recyclerView;
    private TextView key_create;
    private Button create_room;
    private Button join;
    private EditText key_enter;
    private String name;
    private Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        connectWebSocket();

        ImageView gifImageView = findViewById(R.id.gif_lobby);
        Glide.with(this).asGif().load(R.drawable.sus).into(gifImageView);

        /** Declare variable */
        create_room = findViewById(R.id.create_room);
        join = findViewById(R.id.join);
        key_create = findViewById(R.id.key_create);
        key_enter = findViewById(R.id.enter_key);
        recyclerView = findViewById(R.id.recyclerView);
        Button ready=findViewById(R.id.ready);
        start=findViewById(R.id.start);

        /** user message */
        Intent intent = getIntent();
        name = intent.getStringExtra("account");
        Log.d("intent", intent.getStringExtra("account"));

        /** 登入系統 */
        create_room.setOnClickListener(v -> {
            String random_create=(generateRandomValue(10));
            key_create.setText(random_create);
            try {
                JSONObject json_create = new JSONObject();
                json_create.put("0", random_create);
                json_create.put("name", name);
                webSocketClient.send(json_create.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            join.setEnabled(false);
            key_enter.setEnabled(false);
            ready.setEnabled(false);
        });
        join.setOnClickListener(v -> {
            if (key_enter.length() > 0) {
                try {
                    JSONObject json_join = new JSONObject();
                    json_join.put("1", key_enter.getText().toString());
                    json_join.put("name", name);
                    json_join.put("state", "not ready");
                    webSocketClient.send(json_join.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "pls enter key", Toast.LENGTH_SHORT).show();
            }

        });

        /** 準備中 */
        ready.setOnClickListener(v ->{
            try {
                for(Item item: items){
                    if(item.name.equals(name)){
                        item.state="ready";
                        recyclerView.setAdapter(adapter);
                        break;
                    }
                }
                JSONObject json_ready=new JSONObject();
                json_ready.put("2", "ready");
                json_ready.put("name", name);
                webSocketClient.send(json_ready.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        /** 開始遊戲 */
        start.setOnClickListener(v->{
            try {
                JSONObject json_start=new JSONObject();
                json_start.put("3", "start");
                webSocketClient.send(json_start.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Bundle data=new Bundle();
            data.putSerializable("player", items);
            data.putString("name", name);
            data.putString("key", key_create.getText().toString());
            Intent intent_start=new Intent(this, start_game.class);
            intent_start.putExtras(data);
            startActivity(intent_start);
            stopService(new Intent(this, music.class));
            finish();
        });


        /** player_list */
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    @NonNull
    private String generateRandomValue(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            sb.append(randomChar);
        }
        return sb.toString();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "再按一次返回鍵退出", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
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

            private boolean host_exist=false;

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                // 連接成功
                Log.d("onOpen", "working to connect");
            }

            @Override
            public void onMessage(String message) {
                String message_temp;
                String client_name;
                String client_state;
                String host;
                // 收到訊息
                Log.d("onMessage", message);
                try {
                    message_temp = new JSONObject(message).getString("signal");
                    if (message_temp.equals("successful create")) {
                        runOnUiThread(() -> {
                            Toast.makeText(lobby.this, "Create party successfully", Toast.LENGTH_SHORT).show();
                            items.clear();
                            recyclerView.setAdapter(adapter);
                            items.add(new Item(name, "host"));
                            recyclerView.setAdapter(adapter);
                        });
                    } else if (message_temp.equals("working to join party")) {
                        client_name = new JSONObject(message).getString("user");
                        client_state = new JSONObject(message).getString("state");
                        host = new JSONObject(message).getString("host");
                        runOnUiThread(() -> {
                            Toast.makeText(lobby.this, client_name, Toast.LENGTH_SHORT).show();
                            if(!host_exist){
                                items.add(new Item(host, "host"));
                                host_exist=true;
                            }
                            items.add(new Item(client_name, client_state));
                            recyclerView.setAdapter(adapter);
                            //turn off
                            create_room.setEnabled(false);
                            join.setEnabled(false);
                            key_enter.setEnabled(false);
                            start.setEnabled(false);
                        });
                    } else if (message_temp.equals("name from client")) {
                        client_name = new JSONObject(message).getString("user");
                        client_state = new JSONObject(message).getString("state");
                        runOnUiThread(() -> {
                            items.add(new Item(client_name, client_state));
                            recyclerView.setAdapter(adapter);
                        });
                    }else if(message_temp.equals("ready")) {
                        client_name = new JSONObject(message).getString("user");
                        client_state = new JSONObject(message).getString("state");
                        runOnUiThread(() -> {
                            for (Item item : items) {
                                if (item.name.equals(client_name)) {
                                    item.state = client_state;
                                    recyclerView.setAdapter(adapter);
                                    break;
                                }
                            }
                        });
                    }else if(message_temp.equals("start")){
                        Bundle data=new Bundle();
                        data.putSerializable("player", items);
                        data.putString("name", name);
                        data.putString("key", key_enter.getText().toString());
                        Intent intent=new Intent(lobby.this, start_game.class);
                        intent.putExtras(data);
                        startActivity(intent);
                        stopService(new Intent(lobby.this, music.class));
                        finish();
                    } else if (message_temp.equals("connecting")) {
                        Log.d("connecting", message_temp);
                    } else {
                        runOnUiThread(() -> Toast.makeText(lobby.this, "Not suitable condition", Toast.LENGTH_SHORT).show());
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
    }
}