package com.example.findfriends;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.provider.Settings;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.UUID;

public class DBHandler
{
    public DBHandler(Context context, String username)
    {
        UID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        this.username = username;
        ownUser.UID = UID;
        ownUser.Username = username;
    }


    public ArrayList<User> enterRoom(String roomname)
    {
        ArrayList<User> userlist;

        if(faf.isConnected() == false)
            faf.connect(SERVERNAME, PORT);

        String ans = faf.get(roomname);

        userlist = gson.fromJson(ans, new TypeToken<ArrayList<User>>(){}.getType());

        if(userlist == null)
            userlist = new ArrayList<>();

        boolean contains = false;
        for(User user: userlist)
            if(user.UID.equals(this.UID))
            {
                contains = true;
                break;
            }

        if (contains == false)
        {
            userlist.add(ownUser);
            faf.update(roomname, gson.toJson(userlist));
        }


        return userlist;
    }


    public ArrayList<UserLocation> getLocations(ArrayList<User> userlist)
    {
        ArrayList<UserLocation> userlocations = new ArrayList<>();

        if(faf.isConnected() == false)
            faf.connect(SERVERNAME, PORT);

        for (User user : userlist)
        {
            String JSONloc = faf.get(user.UID);

            UserLocation loc = gson.fromJson(JSONloc, UserLocation.class);

            userlocations.add(loc);
        }

        return userlocations;
    }

    public void refreshOwnLocation(Location location)
    {
        if(faf.isConnected() == false)
            faf.connect(SERVERNAME, PORT);

        UserLocation loc = new UserLocation();
        loc.latitude = location.getLatitude();
        loc.longitude = location.getLongitude();
        loc.speed = location.getSpeed();
        loc.UID = this.UID;

        if(faf.isConnected() == false)
            faf.connect(SERVERNAME, PORT);

        faf.update(this.UID, gson.toJson(loc));
    }

    private Gson gson = new Gson();
    private FAFDatabase faf = new FAFDatabase();
    private String username;
    private User ownUser = new User();
    private String UID;


    private final String SERVERNAME = "meiner.ml";
    private final short PORT = 2345;
}
