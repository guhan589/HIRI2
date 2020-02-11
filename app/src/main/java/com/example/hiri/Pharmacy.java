package com.example.hiri;


//Pharmacy 클래스.
public class Pharmacy {
    String addr; //주소
    String distance; //거리
    String telno;// 전화번호
    String yadmNm; //약국 이름
    double x, y ;
    public Pharmacy(String addr, String distance, String telno, String yadmNm, double latitude,double longitude) {
        this.addr = addr;
        this.distance = distance+"m";
        this.telno = telno;
        this.yadmNm = yadmNm;
        this.x = latitude;
        this.y = longitude;

    }
  public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance+"m";
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
        this.yadmNm = yadmNm;
    }

    public void setX(double latitude){this.x = latitude;}

    public double getX(){return x;}

    public void setY(double longitude){this.y =longitude ;}

    public double getY(){return y;}
}