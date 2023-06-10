package com.example.project_game;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class start_game extends AppCompatActivity {

    private ArrayList<Item> items = new ArrayList<>();
    private Adapter adapter = new Adapter(items, R.layout.player, 15);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_game);

        /** declare */
        RecyclerView recyclerView = findViewById(R.id.recyclerView2);
        SeekBar random_value = findViewById(R.id.random_value);
        Button attack = findViewById(R.id.attack);
        Button defend = findViewById(R.id.defend);
        Button click = findViewById(R.id.click);
        random_value.setEnabled(false);
        click.setEnabled(false);

        /** attack */
        AtomicBoolean hit = new AtomicBoolean(false);
        Random random = new Random();
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (hit.get()) {
                    click.setEnabled(false);
                    random_value.setProgress(0);
                    return;
                }
                random_value.setProgress(random.nextInt(6) * 20);
                handler.postDelayed(this, 1000);
            }
        };
        attack.setOnClickListener(v -> {
            Toast.makeText(this, "你選擇了攻擊模式", Toast.LENGTH_SHORT).show();
            click.setEnabled(true);
            hit.set(false);
            handler.postDelayed(runnable, 2000);
        });

        /** defend */
        defend.setOnClickListener(v -> {
            Toast.makeText(this, "你選擇了防禦模式", Toast.LENGTH_SHORT).show();
        });

        /** hit */
        click.setOnClickListener(v -> {
            hit.set(false);
            if (random_value.getProgress() == 20) {
                Toast.makeText(this, "Working to hit", Toast.LENGTH_SHORT).show();
                handler.removeCallbacks(runnable);
                runnable.run();

            } else {
                Toast.makeText(this, "Wrong position", Toast.LENGTH_SHORT).show();
                handler.removeCallbacks(runnable);
            }
        });

        /** data */
        Intent intent = getIntent();
        ArrayList<Item> items_temp = (ArrayList<Item>) intent.getSerializableExtra("player");
        for (Item item : items_temp) {
            items.add(new Item(item.name, item.state));
        }

        /** player_list */
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

}