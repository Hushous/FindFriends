package com.example.findfriends;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TCPConnection
{
    TCPConnection()
    {
        try {
            socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public int send(String data)
    {
        try
        {
            OutputStream output = socket.getOutputStream();
            output.write(data.getBytes());
        }
        catch(Exception ex)
        {
            return 0;
        }

        return data.length();
    }

    public String getPacket()
    {
        try {
            InputStream input = socket.getInputStream();
            InputStreamReader reader = new InputStreamReader(input);

            int character;
            StringBuilder data = new StringBuilder();

            while ((character = reader.read()) != -1) {
                data.append((char) character);
            }

            return data.toString();
        }
        catch(Exception ex)
        {
        }

        return "";
    }


    public void closeConnection()
    {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean connect(String hostname, short port)
    {
        try
        {
            socket = new Socket(hostname, port);


        } catch (UnknownHostException ex) {

            System.out.println("Server not found: " + ex.getMessage());
            return false;

        } catch (IOException ex) {

            System.out.println("I/O error: " + ex.getMessage());
            return false;
        }

        return true;
    }


    public String getHost() { return host; }
    public short getPort() { return port; }
    public boolean isConnected() { return connected; }
    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
        try {
            socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private String host;
    private short port;
    private boolean connected;
    private Socket socket;
    private int timeout = 10000;
}
