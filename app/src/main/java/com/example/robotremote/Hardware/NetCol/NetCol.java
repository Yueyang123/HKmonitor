/**
 TCP  TO  COLTROL   F4
 */
package com.example.robotremote.Hardware.NetCol;
import android.util.Log;
import com.example.robotremote.Comm.data.toF4;

import java.net.*;
import java.io.*;


public class NetCol implements Runnable{

    final String   TAG="NET_COL";
    public String  severip;
    public int     port;
    public boolean connectstatus=false;
    public Socket  clintsocket;
    public OutputStream   cio;
    public InputStream    cin;

    public NetCol(String ip,int port)
    {
        Log.d(TAG,"TRY TO CONNECT1 ");
        this.severip=ip;
        this.port=port;
    }

    public void send(byte[] writebuf)
    {
        if(connectstatus==true&&writebuf!=null)
        {
            try {
                cio.write(writebuf);
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void read(byte[] recebufs)
    {
        try {
            if(connectstatus==true){
                    cin.read(recebufs);
                    if(recebufs[0]!=0)
                        toF4.tof4Timeout=10000;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void run()
    {
        try {
            if(clintsocket!=null)//如果不是第一次链接先断开链接
            {
                clintsocket.close();
                cio.close();
                cin.close();
            }
            clintsocket=new Socket(severip,port);
            connectstatus = true;
            Log.d(TAG, "CONNECT " + clintsocket.getRemoteSocketAddress() + " SUCCESS ");
            cio=clintsocket.getOutputStream();
            cin=clintsocket.getInputStream();
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
