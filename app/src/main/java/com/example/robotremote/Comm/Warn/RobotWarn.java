package com.example.robotremote.Comm.Warn;
import android.util.Log;
import com.example.robotremote.Hardware.Led.LedCol;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  警告，以及相应的处理方法
 *  常见的警告方式主要是三个灯
 *  以及三个标
 *  @modificationHistory
 */
public  class RobotWarn implements Runnable{
    final String TAG="WARN";
    public enum Warn{
        /**
         * 机器人与遥控器通信状态
         * */
        LORAWarn,//0x87
        WIFIWarn,//0x8C
        /**
         * 机器人与上位机通性状态
         * */
        UDPWarn,//0x82
        TCPWarn,//0x86
        /**
         * 烟雾瓦斯告警
         * */
        SMOKEWarn,//0x89
        GASWarn,  //0x8A
        /**
         * 物理状态告警
         * */
        POSTUREWarn,//姿态告警0x83
        DISTANCEWarn,//距离告警0x84

        MOTORWarn,//0x88
        DATAWarn,//0x95
        MCUWarn//0x85
    }
    public static boolean WarnFlag[];
    public RobotWarn()
    {
        WarnFlag=new boolean[11];
        for(int i=0;i<11;i++)
            WarnFlag[i]=false;
    }

    public void run()
    {
        while (true)
        {
            /**
             * 机器人与遥控器通讯警告
             * */
            if(WarnFlag[Warn.LORAWarn.ordinal()]==true&&WarnFlag[Warn.WIFIWarn.ordinal()]==false){
                LedCol.ledblink[LedCol.LED.GREEN.ordinal()]=LedCol.BLINK.SINGLEBLINK.ordinal();
                Log.d(TAG,"LORA警告");
            }
            else if(WarnFlag[Warn.LORAWarn.ordinal()]==false&&WarnFlag[Warn.WIFIWarn.ordinal()]==true){
                LedCol.ledblink[LedCol.LED.GREEN.ordinal()]=LedCol.BLINK.DOUBLEBLINK.ordinal();
                Log.d(TAG,"WIFI警告");
            }
            else if(WarnFlag[Warn.LORAWarn.ordinal()]==false&&WarnFlag[Warn.WIFIWarn.ordinal()]==false){
                LedCol.LedOn(LedCol.LED.GREEN, LedCol.BLINK.NOBLINK);
            }
            else if(WarnFlag[Warn.LORAWarn.ordinal()]==true&&WarnFlag[Warn.WIFIWarn.ordinal()]==true){
                LedCol.LedOff(LedCol.LED.GREEN, LedCol.BLINK.NOBLINK);//绿灯常灭
                Log.d(TAG,"通讯警告");
            }
            /**
             * 烟雾与瓦斯警告
             * */
            if(WarnFlag[Warn.GASWarn.ordinal()]==true)
            {
                LedCol.LedOn(LedCol.LED.RED, LedCol.BLINK.DOUBLEBLINK);
                Log.d(TAG,"瓦斯警告");
            }else
            {
                LedCol.LedOff(LedCol.LED.RED, LedCol.BLINK.NOBLINK);
            }
            if(WarnFlag[Warn.SMOKEWarn.ordinal()]==true)
            {
                LedCol.LedOn(LedCol.LED.RED,LedCol.BLINK.SINGLEBLINK);
                Log.d(TAG,"烟雾警告");
            }else
            {
                LedCol.LedOff(LedCol.LED.RED, LedCol.BLINK.NOBLINK);
            }
            /**
             * 姿态与距离警告
             */
            if(WarnFlag[Warn.POSTUREWarn.ordinal()]==true)
            {
                LedCol.LedOn(LedCol.LED.YELLO, LedCol.BLINK.SINGLEBLINK);
                Log.d(TAG,"姿态警告");
            }else
            {
                LedCol.LedOff(LedCol.LED.YELLO, LedCol.BLINK.NOBLINK);
            }
            if(WarnFlag[Warn.DISTANCEWarn.ordinal()]==true)
            {
                LedCol.LedOn(LedCol.LED.YELLO, LedCol.BLINK.DOUBLEBLINK);
                Log.d(TAG,"距离警告");
            }else
            {
                LedCol.LedOff(LedCol.LED.YELLO, LedCol.BLINK.NOBLINK);
            }
            try {
                Thread.sleep(500);
            }catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
