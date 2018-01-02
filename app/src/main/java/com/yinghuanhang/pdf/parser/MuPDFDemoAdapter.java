package com.yinghuanhang.pdf.parser;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cao-Human on 2018/1/2
 */

public class MuPDFDemoAdapter extends RecyclerView.Adapter<MuPDFDemoAdapter.MuPDFDemoHolder> {
    private List<String> mImages = new ArrayList<>();

    public void insert(String image) {
        mImages.add(image);
        notifyItemInserted(mImages.size() - 1);
    }

    @Override
    public MuPDFDemoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ImageView view = (ImageView) inflater.inflate(R.layout.adapter_choose_image_item, parent, false);
        return new MuPDFDemoHolder(view);
    }

    @Override
    public void onBindViewHolder(MuPDFDemoHolder holder, int position) {
        File file = new File(mImages.get(position));
        Glide.with(holder.mImage.getContext()).load(file).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.mImage);
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

    public static class MuPDFDemoHolder extends RecyclerView.ViewHolder {
        public MuPDFDemoHolder(ImageView image) {
            super(image);
            mImage = image;
        }

        ImageView mImage;
    }
}
