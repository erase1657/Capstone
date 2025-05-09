package com.example.alramapp.RecycleView;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alramapp.R;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.BoardViewHolder>{

    //데이터 리스트
    private List<Board> dataList;

    public BoardAdapter(List<Board> dataList){
        this.dataList = dataList;
    }


    //뷰 홀더 생성
    @NonNull
    @Override
    public BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new BoardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.main_page, parent, false));

    }


    //position에 해당하는 위치에 뷰를 생성.
    @Override
    public void onBindViewHolder(@NonNull BoardViewHolder holder, int position) {

        // ViewHolder 가 재활용 될 때 사용되는 메소드
        Board data = dataList.get(position);
        holder.Time.setText(data.getTime());
        holder.Amps.setText(data.getAmp());
        holder.Dow.setText(data.getDow());
        holder.IsChecked.setChecked(data.isChecked());
    }


    //데이터 개수 반환
    @Override
    public int getItemCount() {
        return dataList.size();
    }


    //ViewHolder 에 필요한 데이터들
    public class BoardViewHolder extends RecyclerView.ViewHolder {

        private TextView Time;
        private TextView Amps;
        private TextView Dow;
        private SwitchCompat IsChecked;

        //생성자
        public BoardViewHolder(@NonNull View itemView) {
            super(itemView);

            Time = itemView.findViewById(R.id.tvtime);
            Amps = itemView.findViewById(R.id.tvamp);
            Dow = itemView.findViewById(R.id.tvdow);
            IsChecked = itemView.findViewById(R.id.scischeck);


        }
    }
}