package com.example.hiri;


//Pharmacy 클래스.
public class Hospital {
    String addr; //주소
    String scale; // 규모
    String telno;// 전화번호
    String yadmNm; //병원 이름
    String level; //병원등급
    double x, y ;
    public Hospital(String addr, String scale, String telno,  String yadmNm, double latitude,double longitude) {
        this.addr = addr;
        this.scale = scale;
        this.telno = telno;
        this.yadmNm = yadmNm;
        this.level = level;
        this.x = latitude;
        this.y = longitude;

    }
    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }

    public String getTelno() {
        return telno;
    }

    public void setTelno(String telno) {
        this.telno = telno;
    }

    public String getYadmNm() {
        return yadmNm;
    }

    public void setYadmNm(String yadmNm) {
        this.level = level;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setX(double latitude){this.x = latitude;}

    public double getX(){return x;}

    public void setY(double longitude){this.y =longitude ;}

    public double getY(){return y;}
}