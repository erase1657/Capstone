package com.example.alramapp.Alarm.RecyclerView;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alramapp.Alarm.AlarmData;
import com.example.alramapp.R;

import java.util.List;
import java.util.Locale;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {

    private List<AlarmData> items;
    private OnItemClickListener itemClickListener;
    LinearLayout alramBackground;

    public interface OnItemClickListener {
        void onItemClick(AlarmData alarm);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    // 생성자
    public AlarmAdapter(List<AlarmData> items) {
        this.items = items;
    }


    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarmlist, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlarmData data = items.get(position);



        holder.bind(data);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }



    /* ViewHolder */
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvAmp, tvName, tvRepeat;
        SwitchCompat swEnable;
        private AlarmData currentAlarmData;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime   = itemView.findViewById(R.id.tvtime);
            tvAmp    = itemView.findViewById(R.id.tvamp);
            tvName   = itemView.findViewById(R.id.tvname);
            tvRepeat = itemView.findViewById(R.id.tvrepeat);
            swEnable = itemView.findViewById(R.id.switch_alarm);
            alramBackground = itemView.findViewById(R.id.alarmitem);
            itemView.setOnClickListener(v -> {
                if (itemClickListener != null && currentAlarmData != null) {
                    itemClickListener.onItemClick(currentAlarmData);
                }
            });

        }

        /**
         * 리사이클러 뷰에 표시할 아이템에 데이터를 바인딩하는 메서드
         */
        public void bind(AlarmData alarmData) {
            currentAlarmData = alarmData;

            int hour = alarmData.getHour();
            int minute = alarmData.getMinute();
            boolean isPM = hour >= 12;
            boolean mis_on = alarmData.getMisOn();
            tvTime.setText(String.format(Locale.getDefault(),"%02d:%02d", hour, minute));
            tvAmp.setText(isPM ? "PM" : "AM");
            tvName.setText(alarmData.getName());
            tvRepeat.setText(alarmData.getRepeat());
            swEnable.setChecked(alarmData.getIsEnabled());

            if (mis_on) {
                alramBackground.getBackground().mutate().setTint(Color.parseColor("#FFCA75"));
            }else{
                alramBackground.getBackground().mutate().setTint(Color.parseColor("#FFFFFF"));

            }
        }


    }


}