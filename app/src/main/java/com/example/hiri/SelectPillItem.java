package com.example.hiri;

public class SelectPillItem {
    String number;
    String pill_name;

    public SelectPillItem(String num,String pill_name){
        this.number = num;
        this.pill_name = pill_name;
    }

    public String getPill_name() {
        return pill_name;
    }

    public void setPill_name(String pill_name) {
        this.pill_name = pill_name;
    }
    public String getNumber(){
        return number;
    }
    public void setNumber(String num){
        this.number = num;
    }
}
