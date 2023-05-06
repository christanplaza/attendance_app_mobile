package com.example.attendanceapp;

import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClassListAdapter extends RecyclerView.Adapter<ClassListAdapter.ClassViewHolder> {

    private List<ClassItem> classList;
    private SparseBooleanArray itemStateArray = new SparseBooleanArray();
    private OnItemClickListener listener;

    public ClassListAdapter(List<ClassItem> classList, OnItemClickListener listener) {
        this.classList = classList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_item, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassItem currentItem = classList.get(position);
        holder.bind(currentItem, position, listener, itemStateArray);
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    public static class ClassViewHolder extends RecyclerView.ViewHolder {

        private TextView classTitleTextView;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            classTitleTextView = itemView.findViewById(R.id.classNameTextView);
        }

        public void bind(final ClassItem item, final int position, final OnItemClickListener listener, final SparseBooleanArray itemStateArray) {
            classTitleTextView.setText(item.getTitle());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (itemStateArray.get(position, false)) {
                        itemStateArray.put(position, false);
                        classTitleTextView.setTextColor(Color.BLACK);
                    } else {
                        itemStateArray.put(position, true);
                        classTitleTextView.setTextColor(Color.GREEN);
                    }
                    listener.onItemClick(itemStateArray);
                }
            });
        }
    }

    public ArrayList<Integer> getSelectedItems() {
        ArrayList<Integer> selectedItems = new ArrayList<>();
        for (int i = 0; i < itemStateArray.size(); i++) {
            if (itemStateArray.valueAt(i)) {
                selectedItems.add(Integer.valueOf(classList.get(itemStateArray.keyAt(i)).getId()));
            }
        }
        return selectedItems;
    }

    public interface OnItemClickListener {
        void onItemClick(SparseBooleanArray itemStateArray);
    }
}
