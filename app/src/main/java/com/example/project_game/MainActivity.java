package com.example.project_game;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private boolean doubleBackToExitPressedOnce = false;
    private EditText account;
    private EditText password;
    String address=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent=new Intent(this, music.class);
        startService(intent);

        account = findViewById(R.id.account);
        password = findViewById(R.id.password);
        Button login = findViewById(R.id.login);
        Button registe = findViewById(R.id.registe);

        login.setOnClickListener(v -> {
            try {
                if(account.length()>0 && password.length()>0){
                    address="http://26.164.96.164:8080/login";
                    new PostDate().execute(new JSONObject().put(account.getText().toString(), password.getText().toString()).toString());
                }
                else{
                    Toast.makeText(this, "enter account & password", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        registe.setOnClickListener(v -> {
            try {
                address="http://26.164.96.164:8080/registe";
                new PostDate().execute(new JSONObject().put(account.getText().toString(), password.getText().toString()).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            //預設行為為退回前一頁
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "再按一次返回鍵退出", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    class PostDate extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                // on below line creating a url to post the data.
                URL url = new URL(address);

                // on below line opening the connection.
                HttpURLConnection client = (HttpURLConnection) url.openConnection();

                // on below line setting method as post.
                client.setRequestMethod("POST");

                // on below line setting content type and accept type.
                //告訴服務器所發送的資料格式
                client.setRequestProperty("Content-Type", "application/json");
                //告訴服務器客戶端所能夠接受的回應格式
                //client.setRequestProperty("Accept", "image/png");

                // on below line setting client.
                //用於指示此連接是否允許輸出數據
                client.setDoOutput(true);

                // on below line we are creating an output stream and posting the data.
                try (OutputStream os = client.getOutputStream()) {
                    byte[] input = strings[0].getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                //讀到資料才會進行
                try (InputStream inputStream = client.getInputStream()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    if (stringBuilder.toString().equals("login successfully")) {
                        // 顯示 Toast 訊息
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, stringBuilder.toString(), Toast.LENGTH_SHORT).show());

                        //put data
                        Bundle data=new Bundle();
                        data.putString("account", account.getText().toString());
                        data.putString("password", password.getText().toString());
                        Intent intent=new Intent(MainActivity.this, lobby.class);
                        intent.putExtras(data);
                        startActivity(intent);
                        finish();
                    }
                    else if(stringBuilder.toString().equals("successful create")){
                        runOnUiThread(()-> Toast.makeText(MainActivity.this, stringBuilder.toString(), Toast.LENGTH_SHORT).show());
                    }
                    else {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, stringBuilder.toString(), Toast.LENGTH_SHORT).show());
                    }

                } catch (IOException e) {
                    // 處理錯誤
                    e.printStackTrace();
                }
            } catch (Exception e) {
                // on below line handling the exception.
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show());
            }
            return null;
        }
    }
}

