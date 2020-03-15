package com.example.findfriends;

import android.provider.ContactsContract;


//FastAsFuck Database

public class FAFDatabase
{
    public FAFDatabase(){}


    public boolean connect(String ip, int port)
    {
        return con.connect(ip, port);
    }

    public void close()
    {
        con.closeConnection();
    }

    public boolean insert(String key, String data)
    {
        con.send("s;" + key + ";" + data);
        con.getPacket();
        return true;
    }

    public boolean update(String key, String data)
    {
        con.send("u;" + key + ";" + data);
        con.getPacket();
        return true;
    }

    public boolean add(String key, String data)
    {
        con.send("a;" + key + ";" + data);
        con.getPacket();
        return true;
    }

    public String get(String key)
    {
        con.send("g;" + key);
        String ans = con.getPacket();

        if(ans.length() > 0)
            return ans.substring(1);
        else
            return "";
    }

    public String remove(String key)
    {
        con.send("d;" + key);
        return con.getPacket();
    }

    public String increase(String key)
    {
        con.send("in;" + key);
        return con.getPacket();
    }

    public String decrease(String key)
    {
        con.send("de;" + key);
        return con.getPacket();
    }

    public void forceSave()
    {
        con.send("f;");
        con.getPacket();
    }


    private TCPConnection con = new TCPConnection();
}
