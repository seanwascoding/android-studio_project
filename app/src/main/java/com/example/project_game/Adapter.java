package com.example.project_game;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private ArrayList<Item> data;
    private int layout;
    private float text_size;

    public Adapter(ArrayList<Item> data, int layout, float text) {
        this.data = data;
        this.layout = layout;
        this.text_size=text;
    }

    public Adapter(ArrayList<Item> data, int layout) {
        this.data = data;
        this.layout = layout;
        text_size=20;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView state;

        public ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            state = v.findViewById(R.id.state);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(layout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        /** 綁定並設定大小 */
        holder.name.setText(data.get(position).name);
        holder.state.setText(data.get(position).state);
        holder.name.setTextSize(text_size);
        holder.state.setTextSize(text_size);
    }
}
