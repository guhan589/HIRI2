package com.example.hiri;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

//Hospital 객체를 관리하는 어댑터 만들기
public class SelectPillItemAdapter extends RecyclerView.Adapter<SelectPillItemAdapter.ViewHolder> {

    public ArrayList<SelectPillItem> items = new ArrayList<SelectPillItem>();
    public OnSelectPillItemClickListener listener; //리싸이클러뷰에서 병원을 선택했을 때 리스너 등록

    public SelectPillItemAdapter(ArrayList<SelectPillItem> mArrayList) {
        this.items = mArrayList;
    }
    public interface OnSelectPillItemClickListener{
        void onItemClick(SelectPillItemAdapter.ViewHolder holder, View view, int position);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.pill_select, parent, false); //뷰 객체 인플레이션  //pharmacy1.xml
        return new ViewHolder(itemView, this);
    }

    //ViewHolder 객체 재사용. 스크롤하면 밑에 데이터만 다시 세팅함. 다시 인플레이션을 하지 않음.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SelectPillItem item = items.get(position);
        holder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(SelectPillItem item) {
        items.add(item);
    }

    public void setPill_name(ArrayList<SelectPillItem> items) {
        this.items = items;
    }

    public SelectPillItem getItem(int position) {
        return items.get(position);
    }

    public void setItem(int position, SelectPillItem item) {
        items.set(position, item);
    }

    //외부에서 리스너를 설정할 수 있도록 하는 메소드
    public void setOnItemClickListener(OnSelectPillItemClickListener listener) {
        this.listener = listener;
    }

    public void onItemClick(ViewHolder holder, View view, int position) {
        if (listener != null) {
            listener.onItemClick(holder, view, position);
        }
    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView_number;
        TextView textView_name;

        //ViewHolder 생성자로 전달되는 뷰 객체 참조
        public ViewHolder(final View itemView, final SelectPillItemAdapter listener) {
            super(itemView);

            textView_number = itemView.findViewById(R.id.textView_sub_number);
            textView_name = itemView.findViewById(R.id.textView_sub_name);


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
        }

        public void setItem(SelectPillItem item) {
            textView_number.setText(item.getNumber());
            textView_name.setText(item.getPill_name());
        }
    }
}
