package com.example.findfriends;

import java.io.BufferedReader;
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
            BufferedReader bufferedReader =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()));
            char[] buffer = new char[200];
            int n = bufferedReader.read(buffer, 0, 200); // blockiert bis Nachricht empfangen
            String data = new String(buffer, 0, n);

            System.out.println(data);

            return data;
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

    public boolean connect(String hostname, int port)
    {
        try
        {
            socket = new Socket(hostname, port);
            socket.setSoTimeout(timeout);

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
