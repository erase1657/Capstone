package com.example.alramapp.Alarm.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alramapp.R;

import java.util.List;

public class SoundAdapter extends RecyclerView.Adapter<SoundAdapter.SoundViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String soundName);
    }

    private List<String> soundList;
    private OnItemClickListener listener;

    // 선택된 위치를 저장, 기본은 선택 없음
    private int selectedPosition = RecyclerView.NO_POSITION;

    private Context context;

    public SoundAdapter(Context context, List<String> soundList, OnItemClickListener listener) {
        this.context = context;
        this.soundList = soundList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SoundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_soundlist, parent, false);
        return new SoundViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SoundViewHolder holder, int position) {
        String soundName = soundList.get(position);
        holder.textSoundName.setText(soundName);

        // 선택된 포지션과 현재 포지션이 같다면 배경색 변경, 아니면 기본 배경색
        if (position == selectedPosition) {
            holder.bgSoundItem.setBackgroundColor(Color.parseColor("#5774DD")); // 선택 배경색
        } else {
            holder.bgSoundItem.setBackgroundColor(Color.parseColor("#2A2828")); // 기본 배경색
        }

        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition == position) {
                // 같은 아이템 클릭 시 선택 해제
                int oldPosition = selectedPosition;
                selectedPosition = RecyclerView.NO_POSITION;
                notifyItemChanged(oldPosition);

                // 선택 해제 이벤트 알림 (null 또는 빈 문자열 전달)
                if (listener != null) {
                    listener.onItemClick(null);
                }
            } else {
                // 다른 아이템 클릭 시 선택 변경
                int oldPosition = selectedPosition;
                selectedPosition = position;

                notifyItemChanged(oldPosition);
                notifyItemChanged(selectedPosition);

                if (listener != null) {
                    listener.onItemClick(soundName);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return soundList.size();
    }

    // 선택된 사운드를 외부에서 셋팅(예: 저장된 사운드로 초기화)
    public void setSelectedSound(String soundName) {
        if (soundName == null) {
            // 선택 해제
            int oldPosition = selectedPosition;
            selectedPosition = RecyclerView.NO_POSITION;
            if (oldPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldPosition);
            }
            return;
        }


        // 선택된 사운드 위치 찾기
        int position = soundList.indexOf(soundName);
        if (position != -1 && position != selectedPosition) {
            int oldPosition = selectedPosition;
            selectedPosition = position;
            if (oldPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldPosition);
            }
            notifyItemChanged(selectedPosition);
        }
    }

    static class SoundViewHolder extends RecyclerView.ViewHolder {

        TextView textSoundName;
        LinearLayout bgSoundItem;

        public SoundViewHolder(@NonNull View itemView) {
            super(itemView);
            textSoundName = itemView.findViewById(R.id.sound_name);
            bgSoundItem = itemView.findViewById(R.id.bg_sound_item);
        }
    }
}