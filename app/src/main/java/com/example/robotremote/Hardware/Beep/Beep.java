package com.example.robotremote.Hardware.Beep;
import com.example.robotremote.Hardware.Gpio.GpioCol;

public class Beep{
    public static GpioCol gpioCol;
    public static boolean beepflag=false;

    public Beep(GpioCol gpioCol)
    {
        this.gpioCol=gpioCol;
        gpioCol.gpioInit(16,"GPIOA3");
        gpioCol.gpioSetDirection(16,GpioCol.GpioDirection.GPIO_DIRECTION_OUTPUT);
        BeepOff();
    }
    public static boolean BeepOn()
    {
        gpioCol.gpioSetValue(16,1);
        beepflag=true;
        return true;
    }

    public static boolean BeepOff()
    {
        gpioCol.gpioSetValue(16,0);
        beepflag=false;
        return true;
    }
}
