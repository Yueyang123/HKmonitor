package com.example.robotremote.Hardware.Led;
import com.example.robotremote.Hardware.Gpio.GpioCol;

public class LedCol implements Runnable{

    public static GpioCol gpioCol;
    public enum LED{RED,GREEN,YELLO}
    public enum BLINK{NOBLINK,SINGLEBLINK,DOUBLEBLINK}
    public static int[] ledflag={0,0,0}; //0红  1绿  2黄
    public static int[] ledblink={0,0,0};//LED 闪烁选项0:不闪 1:单闪  2：双闪

    public LedCol(GpioCol gpioCol)
    {
        this.gpioCol=gpioCol;
        //M0 M1
        gpioCol.gpioInit(0,"GPIOD0");
        gpioCol.gpioInit(1,"GPIOC31");
        //LED0 LED1 LED2
        gpioCol.gpioInit(2,"GPIOA7");
        gpioCol.gpioInit(3,"GPIOA27");
        gpioCol.gpioInit(4,"GPIOA13");

        gpioCol.gpioSetDirection(0, GpioCol.GpioDirection.GPIO_DIRECTION_OUTPUT);
        gpioCol.gpioSetDirection(1,GpioCol.GpioDirection.GPIO_DIRECTION_OUTPUT);

        gpioCol.gpioSetDirection(2,GpioCol.GpioDirection.GPIO_DIRECTION_OUTPUT);
        gpioCol.gpioSetDirection(3,GpioCol.GpioDirection.GPIO_DIRECTION_OUTPUT);
        gpioCol.gpioSetDirection(4,GpioCol.GpioDirection.GPIO_DIRECTION_OUTPUT);
    }

    public static boolean LedOn(LED led,BLINK blink)
    {
        gpioCol.gpioSetValue(led.ordinal()+2,1);
        ledflag[led.ordinal()]=1;
        ledblink[led.ordinal()]=blink.ordinal();
        return true;
    }

    public static boolean LedOff(LED led,BLINK blink)
    {
        gpioCol.gpioSetValue(led.ordinal()+2,0);
        ledflag[led.ordinal()]=0;
        ledblink[led.ordinal()]=blink.ordinal();
        return true;
    }

    public void run()
    {
        //状态初始化
        int sta=0;
        gpioCol.gpioSetValue(0,0);//M0
        gpioCol.gpioSetValue(1,0);//M1
        LedOff(LED.RED,BLINK.NOBLINK);// RED
        LedOff(LED.GREEN,BLINK.NOBLINK);//GREEN
        LedOff(LED.YELLO,BLINK.NOBLINK);//YELLOW
        while(true)
        {
            sta++;
            for(int i=0;i<3;i++)
            {
                if(ledblink[i]==BLINK.NOBLINK.ordinal()) {//不需要闪烁
                       if(ledflag[i] == 1) LedOn(LED.values()[i],BLINK.NOBLINK);
                       else LedOff(LED.values()[i],BLINK.NOBLINK);
                }else if(ledblink[i]==BLINK.SINGLEBLINK.ordinal()) {//需要闪烁
                    if(sta%2==1) {
                        if (ledflag[i] == 0) ledflag[i] = 1;
                        else ledflag[i] = 0;
                        if (ledflag[i] == 0) LedOff(LED.values()[i],BLINK.SINGLEBLINK);
                        else LedOn(LED.values()[i],BLINK.SINGLEBLINK);
                    }
                }else if(ledblink[i]==BLINK.DOUBLEBLINK.ordinal()){//双闪
                    if (ledflag[i] == 0) ledflag[i] = 1;
                    else ledflag[i] = 0;
                    if (ledflag[i] == 0) LedOff(LED.values()[i],BLINK.DOUBLEBLINK);
                    else LedOn(LED.values()[i],BLINK.DOUBLEBLINK);
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.getStackTrace();
            }

        }
    }
}
