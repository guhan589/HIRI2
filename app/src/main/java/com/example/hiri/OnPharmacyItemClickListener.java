package com.example.hiri;

import android.view.View;

import com.example.hiri.PharmacyAdapter;

//Pharmacy 아이템을 클릭했을 때 리스너 인터페이스
public interface OnPharmacyItemClickListener {
    public void onItemClick(PharmacyAdapter.ViewHolder holder, View view, int position);
    public void onCallClick(int position);
}