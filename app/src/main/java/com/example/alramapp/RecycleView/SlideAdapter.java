package com.example.alramapp.RecycleView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;


public class SlideAdapter extends RecyclerView.Adapter<SlideAdapter.SlideViewHolder> {

    private List<SlideItem> slideItems;

    public SlideAdapter(List<SlideItem> slideItems) {
        this.slideItems = slideItems;
    }

    public static class SlideViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SlideViewHolder(@NonNull ImageView itemView) {
            super(itemView);
            imageView = itemView;
        }
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new SlideViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        holder.imageView.setImageResource(slideItems.get(position).getImageResId());
    }

    @Override
    public int getItemCount() {
        return slideItems.size();
    }
}