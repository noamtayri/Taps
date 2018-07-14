package com.android.ronoam.taps.FireBase;

import java.io.Serializable;

public class Room implements Serializable {

    public String roomId;
    public MyUser user1;
    public MyUser user2;

    public Room (String roomId, String openTheRoom){
        this.roomId = roomId;
        user1 = new MyUser(openTheRoom);
    }

    public void addUser (String userId){
        user2 = new MyUser(userId);
    }
}
