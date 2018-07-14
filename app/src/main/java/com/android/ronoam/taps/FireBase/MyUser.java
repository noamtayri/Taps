package com.android.ronoam.taps.FireBase;

import java.io.Serializable;

public class MyUser implements Serializable{

    public String id;
    public int counter;

    public MyUser (String id){
        this.id = id;
        counter = 0;
    }
}
