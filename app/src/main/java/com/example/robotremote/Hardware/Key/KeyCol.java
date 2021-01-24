package com.example.robotremote.Hardware.Key;

import android.util.Log;
import com.example.robotremote.Comm.data.InfoForRobot;
import com.example.robotremote.Comm.data.toF1;
import com.example.robotremote.Comm.data.toF4;
import com.example.robotremote.HIKIVideo.Audio;
import com.example.robotremote.Hardware.Adc.Adc;
import com.example.robotremote.Hardware.Beep.Beep;
import com.example.robotremote.Hardware.Gpio.GpioCol;
import com.example.robotremote.ui.MainActivity;

import static com.example.robotremote.Comm.data.toF4.RobotStatus;
import static com.example.robotremote.Comm.data.toF4.sendflag;

/**
 * @author yueyang
 * @version V1.0
 * @describation
 * 3个LED灯各占1个GPIO：LORA通信正常亮绿灯，瓦斯报警闪红灯，烟雾报警闪黄灯
 * 另外7个GPIO功能：
 * 一个紧急停止按键占用1个GPIO；SW0
 * 喊话键（按下后自锁，就可以喊话了）占用1个GPIO；SW1
 * 告警键（按下后自锁，机器人上的警报器响起）占用1个GPIO；RED SW2
 * 两个3档拨动开关占4个GPIO：
 * 低速  <--中速-->高速；
 * 左行  <--停车-->右行 （正常情况下走到端头会自动停车）；
 *  @modificationHistory
 */
public class KeyCol implements Runnable{
    public static int[] SWStatus={1,1,1,1,1,1,1,1,1,1,1};
    public enum SWKind{STOPKey,AudioKey,GREENKey,SPEEDKey1,SPEEDKey2,DIRKey1,DIRKey2,SOUNDKey1,SOUNDKey2,REDKey}
    private GpioCol gpioCol;
    public KeyCol(GpioCol gpiocol)
    {
        this.gpioCol=gpiocol;
        gpioCol.gpioInit(5,"GPIOA26");//SW0急停按键
        gpioCol.gpioInit(6,"GPIOA12");//SW1(喊话)
        gpioCol.gpioInit(7,"GPIOA16");//SW2(告警)
        gpioCol.gpioInit(14,"GPIOA22");//SW9(重联)

        gpioCol.gpioInit(8,"GPIOA23");//调速SW3
        gpioCol.gpioInit(9,"GPIOA5");//调速SW4

        gpioCol.gpioInit(10,"GPIOA11");//向左sw5
        gpioCol.gpioInit(11,"GPIOA15");//向右sw6

        gpioCol.gpioInit(12,"GPIOC0");//SW7音量按键
        gpioCol.gpioInit(13,"GPIOC1");//SW8音量按键
        for(int i=0;i<10;i++)
            gpioCol.gpioSetDirection(i+5,GpioCol.GpioDirection.GPIO_DIRECTION_INPUT);
    }

    public void run()
    {

        while (true) {
            int[] Swstatustemp=new int[11];
            for(int i=0;i<10;i++) Swstatustemp[i]=SWStatus[i];
            for(int i=0;i<10;i++) SWStatus[i] = gpioCol.gpioGetValue(i+5);
            /**
             * 每秒更新一次按键数值
             * */
            if(SWStatus[SWKind.DIRKey1.ordinal()]==0&&SWStatus[SWKind.DIRKey2.ordinal()]==1){
                toF4.sendflag[toF4.CMD.RIGHT.ordinal()]=1;
                toF1.sendflag[toF1.CMD.RIGHT.ordinal()]=1;
            }//向右
            else if(SWStatus[SWKind.DIRKey1.ordinal()]==1&&SWStatus[SWKind.DIRKey2.ordinal()]==0){
                toF4.sendflag[toF4.CMD.LEFT.ordinal()]=1;
                toF1.sendflag[toF1.CMD.LEFT.ordinal()]=1;
            }//向左
            if (SWStatus[SWKind.STOPKey.ordinal()] == 0){
                toF4.sendflag[toF4.CMD.QUICKSTOP.ordinal()]=1;
                toF1.sendflag[toF1.CMD.QUICKSTOP.ordinal()]=1;
                RobotStatus.status = InfoForRobot.STATUS.STOP;
            }else {
                toF4.sendflag[toF4.CMD.QUICKSTOP.ordinal()]=0;
                toF1.sendflag[toF1.CMD.QUICKSTOP.ordinal()]=0;
            }//急停
            if(RobotStatus.status != InfoForRobot.STATUS.STOP&&RobotStatus.status != InfoForRobot.STATUS.QUICKSTOP&&RobotStatus.status != InfoForRobot.STATUS.MODE1) {
                if (SWStatus[SWKind.SPEEDKey1.ordinal()] == 1 && SWStatus[SWKind.SPEEDKey2.ordinal()] == 1) {
                    toF4.sendflag[toF4.CMD.SPEED.ordinal()] = 2;//调速命令
                    toF1.sendflag[toF1.CMD.SPEED.ordinal()] = 2;
                } else if (SWStatus[SWKind.SPEEDKey1.ordinal()] == 0 && SWStatus[SWKind.SPEEDKey2.ordinal()] == 1) {
                    toF4.sendflag[toF4.CMD.SPEED.ordinal()] = 1;//调速命令
                    toF1.sendflag[toF1.CMD.SPEED.ordinal()] = 1;
                } else if (SWStatus[SWKind.SPEEDKey1.ordinal()] == 1 && SWStatus[SWKind.SPEEDKey2.ordinal()] == 0) {
                    toF4.sendflag[toF4.CMD.SPEED.ordinal()] = 3;//调速命令
                    toF1.sendflag[toF1.CMD.SPEED.ordinal()] = 3;
                }
            }//在没有停止的时候，发送速度
            /**
             * 发生跳变
             * */
            if(SWStatus!=Swstatustemp) {
                if (SWStatus[SWKind.STOPKey.ordinal()]!=Swstatustemp[SWKind.STOPKey.ordinal()]) {//急停命令发生跳变
                    if (SWStatus[SWKind.STOPKey.ordinal()] == 0) {
                        toF4.sendflag[toF4.CMD.QUICKSTOP.ordinal()] = 1;
                        toF1.sendflag[toF1.CMD.QUICKSTOP.ordinal()] = 1;
                    } else {
                        toF4.sendflag[toF4.CMD.QUICKSTOP.ordinal()] = 0;
                        toF1.sendflag[toF1.CMD.QUICKSTOP.ordinal()] = 0;
                        RobotStatus.status = InfoForRobot.STATUS.STOP;
                    }
                }
                if(SWStatus[SWKind.DIRKey1.ordinal()]!=Swstatustemp[SWKind.DIRKey1.ordinal()]||SWStatus[SWKind.DIRKey2.ordinal()]!=Swstatustemp[SWKind.DIRKey2.ordinal()]) {
                    if(SWStatus[SWKind.DIRKey1.ordinal()]==0&&SWStatus[SWKind.DIRKey2.ordinal()]==1){
                        toF4.sendflag[toF4.CMD.RIGHT.ordinal()]=1;
                        toF1.sendflag[toF1.CMD.RIGHT.ordinal()]=1;
                    }
                    else if(SWStatus[SWKind.DIRKey1.ordinal()]==1&&SWStatus[SWKind.DIRKey2.ordinal()]==0){
                        toF4.sendflag[toF4.CMD.LEFT.ordinal()]=1;
                        toF1.sendflag[toF1.CMD.LEFT.ordinal()]=1;
                    }
                    else if (SWStatus[SWKind.DIRKey1.ordinal()] == 1 && SWStatus[SWKind.DIRKey2.ordinal()] == 1) {
                        toF4.sendflag[toF4.CMD.STOP.ordinal()] = 1;
                        toF1.sendflag[toF1.CMD.STOP.ordinal()] = 1;
                    }
                }//向左向右
                if (SWStatus[SWKind.AudioKey.ordinal()]!=Swstatustemp[SWKind.AudioKey.ordinal()]){
                    if(SWStatus[SWKind.AudioKey.ordinal()]==0){//下降跳变
                        MainActivity.audio[0].startVoiceTalk();
                        MainActivity.audio[1].startVoiceTalk();
                        Log.d("SW1","喊话");
                    }else {
                        MainActivity.audio[0].stopVoiceTalk();
                        MainActivity.audio[1].stopVoiceTalk();
                        Log.d("SW1","取消喊话");
                    }
                }//Audio喊话按键
                if (SWStatus[SWKind.GREENKey.ordinal()]!=Swstatustemp[SWKind.GREENKey.ordinal()]){
                    if(SWStatus[SWKind.GREENKey.ordinal()]==0) {//下降跳变
                        Log.d("SW2", "告警");
                        Beep.BeepOn();
                    }
                    else {
                        Log.d("SW2","取消告警");
                        Beep.BeepOff();
                    }
                }//BEEP告警按键
                if (SWStatus[SWKind.REDKey.ordinal()]!=Swstatustemp[SWKind.REDKey.ordinal()]){//发生跳变
                    if(SWStatus[SWKind.REDKey.ordinal()]==0) {//下降跳变
                        toF4.sendflag[toF4.CMD.REMOVEWARN.ordinal()]=1;
                        toF1.sendflag[toF1.CMD.REMOVEWARN.ordinal()]=1;
                    }
                }//NET重联
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.getStackTrace();
            }
        }
    }

    public int getSWStatus(SWKind swKind)
    {
        return SWStatus[swKind.ordinal()];
    }
    public void clear(){
        for (int i=0;i<10;i++)
            SWStatus[i]=1;
    }
}
