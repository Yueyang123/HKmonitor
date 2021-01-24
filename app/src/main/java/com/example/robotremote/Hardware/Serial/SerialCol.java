package com.example.robotremote.Hardware.Serial;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialCol{

    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private int baudrate;
    private String SerialPath;

    public SerialCol(String SericalPath,int baudrate)
    {
        mSerialPort = getSerialPort(SericalPath,baudrate,0);
        mOutputStream = mSerialPort.getOutputStream();
        mInputStream = mSerialPort.getInputStream();
        this.baudrate=baudrate;
        this.SerialPath=SericalPath;
    }

    @Override
    public void finalize()
    {
        mSerialPort.serialClose();
    }

    private SerialPort getSerialPort(String path, int baudrate, int flag)  {
        if (mSerialPort == null) {
            /* Open the serial port */
            mSerialPort = new SerialPort(new File(path), baudrate, flag);
        }
        return mSerialPort;
    }

    public void send(byte[] data){
        try {
            if (mOutputStream != null) {
                mOutputStream.write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read(byte[] recvdata)
    {
        int length=0,l=0;
        try {
            if (mInputStream != null) {
                while(length<recvdata.length) {
                    l = mInputStream.read(recvdata, length, 1);
                    length+=l;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getSerialPath()
    {
        return SerialPath;
    }
    public  int getBaudrate()
    {
        return baudrate;
    }
}
