package com.example.hiri;

import android.view.View;

//HospitalSubject 아이템을 클릭했을 때 리스너 인터페이스
public interface OnHospitalSubjectItemClickListener {
    public void onItemClick(HospitalSubjectAdapter.ViewHolder holder, View view, int position);
}
