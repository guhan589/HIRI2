package com.example.hiri;


//HospitalSubject 클래스.
public class HospitalSubject {
    String number;
    String name;

    public HospitalSubject(String number, String name) {
        this.number = number;
        this.name = name;
    }

    public String get_number() {
        return number;
    }

    public void set_number(String sub_number) {
        number = sub_number;
    }

    public String get_name() {
        return name;
    }

    public void set_name(String sub_name) {
        name = sub_name;
    }


}