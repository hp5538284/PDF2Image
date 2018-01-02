package com.yinghuanhang.pdf.parser;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cao-Human on 2018/1/2
 */

public class MuPDFDemoAdapter extends RecyclerView.Adapter {
    private List<String> mImages = new ArrayList<>();

    public void insert(String image) {
        mImages.add(image);
        notifyItemInserted(mImages.size() - 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }
}
