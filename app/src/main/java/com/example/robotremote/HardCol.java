package com.example.robotremote;


import java.io.FileDescriptor;

public class HardCol {
    public  static boolean GpioInit(int portIndex,int port){return  true;};
    public  static int GpioSetDirection(int portIndex,boolean gpioDirection){return  0;};
    public  static void GpioClose(int portIndex){};
    public  static int GpioGetValue(int portIndex){return  0;};
    public  static void GpioSetValue(int portIndex,int value){};
    public  static void GpioMalloc(int max){};
    public  static FileDescriptor SerialOpen(String path, int baudrate, int flags){FileDescriptor fd=new FileDescriptor();return fd;};
    public  static void SerialClose(){};
}
