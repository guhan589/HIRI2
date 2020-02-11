package com.example.hiri;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

//Hospital 객체를 관리하는 어댑터 만들기
public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.ViewHolder> {

    public ArrayList<Hospital> items = new ArrayList<Hospital>();
    public OhHospitalItemClickListener listener; //리싸이클러뷰에서 병원을 선택했을 때 리스너 등록

    public HospitalAdapter(ArrayList<Hospital> mArrayList) {
        this.items = mArrayList;
    }
    public interface OhHospitalItemClickListener{
        void onItemClick(HospitalAdapter.ViewHolder holder, View view, int position);
        void onCallClick(int position);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.pharmacy1, parent, false); //뷰 객체 인플레이션  //pharmacy1.xml
        return new ViewHolder(itemView, this);
    }

    //ViewHolder 객체 재사용. 스크롤하면 밑에 데이터만 다시 세팅함. 다시 인플레이션을 하지 않음.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hospital item = items.get(position);
        holder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Hospital item) {
        items.add(item);
    }

    public void setItems(ArrayList<Hospital> items) {
        this.items = items;
    }

    public Hospital getItem(int position) {
        return items.get(position);
    }

    public void setItem(int position, Hospital item) {
        items.set(position, item);
    }

    //외부에서 리스너를 설정할 수 있도록 하는 메소드
    public void setOnItemClickListener(OhHospitalItemClickListener listener) {
        this.listener = listener;
    }

    public void onItemClick(ViewHolder holder, View view, int position) {
        if (listener != null) {
            listener.onItemClick(holder, view, position);
        }
    }
    public void onCallClick(int position) {
        if (listener != null) {
            listener.onCallClick(position);
        }
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textview_name,textView_scale,textview_telno,textview_address;
        Button call_btn;

        //ViewHolder 생성자로 전달되는 뷰 객체 참조
        public ViewHolder(final View itemView, final HospitalAdapter listener) {
            super(itemView);

            textview_name = itemView.findViewById(R.id.textView_phar_name);
            textView_scale = itemView.findViewById(R.id.textView_phar_information);
            textview_telno = itemView.findViewById(R.id.textView_number);
            textview_address = itemView.findViewById(R.id.textView_address);
            call_btn = itemView.findViewById(R.id.button_calling);

            //itemView에 OnClickListener 설정
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    if (listener != null) {
                        int position = getAdapterPosition();
                        listener.onItemClick(ViewHolder.this, view, position);
                    }
                }
            });
            call_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (listener != null) {
                        int position = getAdapterPosition();
                        Log.d("TAG", "position: "+position);// 버튼 클릭시 위치 추출
                        listener.onCallClick(position);
                    }
                }
            });

        }

        public void setItem(Hospital item) {
            textview_name.setText(item.getYadmNm());
            textView_scale.setText(item.getScale());
            textview_telno.setText(item.getTelno());
            textview_address.setText(item.getAddr());

        }
    }
}