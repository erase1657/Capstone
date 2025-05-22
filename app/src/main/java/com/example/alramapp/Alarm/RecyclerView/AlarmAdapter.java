package com.example.alramapp.Alarm.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
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
import com.example.alramapp.Alarm.AlarmManagerHelper;
import com.example.alramapp.Alarm.SQLlite.AlarmDBHelper;
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
        private boolean isBinding = false;  // 바인딩 상태 변수



        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime   = itemView.findViewById(R.id.tvtime);
            tvAmp    = itemView.findViewById(R.id.tvamp);
            tvName   = itemView.findViewById(R.id.tvname);
            tvRepeat = itemView.findViewById(R.id.tvrepeat);
            swEnable = itemView.findViewById(R.id.sw_alarm);
            alramBackground = itemView.findViewById(R.id.alarmitem);

            swEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isBinding) {
                    // bind 중 이벤트 무시
                    return;
                }
                if (currentAlarmData != null) {
                    if (currentAlarmData.getIsEnabled() == isChecked) {
                        // 상태 그대로면 무시
                        return;
                    }

                    currentAlarmData.setIsEnabled(isChecked);
                    // DB 업데이트
                    AlarmDBHelper dbHelper = new AlarmDBHelper(itemView.getContext());
                    int updatedRows = dbHelper.updateAlarm(currentAlarmData);
                    Log.d(TAG, "Alarm isEnabled updatedRows: " + updatedRows);

                    if (isChecked) {
                        // 알람 등록
                        AlarmManagerHelper.register(itemView.getContext(), currentAlarmData);
                    } else {
                        // 알람 취소
                        AlarmManagerHelper.cancelRepeatingAlarms(itemView.getContext(), currentAlarmData);
                    }

                    // 🔔 명시적으로 상태 변경 후 브로드캐스트로 UI 갱신 요청!
                    Intent intent = new Intent("com.example.alramapp.ACTION_ALARM_STATUS_CHANGED");
                    itemView.getContext().sendBroadcast(intent);
                }
            });



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
            isBinding = true;  // 바인딩 시작

            int hour = alarmData.getHour();
            int minute = alarmData.getMinute();
            boolean isPM = hour >= 12;
            boolean mis_on = alarmData.getMisOn();

            tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
            tvAmp.setText(isPM ? "PM" : "AM");
            tvName.setText(alarmData.getName());
            tvRepeat.setText(alarmData.getRepeat());

            swEnable.setChecked(alarmData.getIsEnabled());

            if (mis_on) {
                alramBackground.getBackground().mutate().setTint(Color.parseColor("#FFCA75"));
            } else {
                alramBackground.getBackground().mutate().setTint(Color.parseColor("#FFFFFF"));
            }

            isBinding = false;  // 바인딩 종료
        }
    }

    private static final String TAG = "AlarmAdapter";

    }


