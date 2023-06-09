package com.example.project_game;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class start_game extends AppCompatActivity {

    private ArrayList<Item> items = new ArrayList<>();
    private Adapter adapter = new Adapter(items, R.layout.player, 15);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_game);

        /** declare */
        RecyclerView recyclerView = findViewById(R.id.recyclerView2);

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