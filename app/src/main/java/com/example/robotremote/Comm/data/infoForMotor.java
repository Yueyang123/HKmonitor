package com.example.robotremote.Comm.data;
import android.util.Log;

import com.example.robotremote.Comm.Warn.RobotWarn;

import static com.example.robotremote.Comm.data.toF4.RobotStatus;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  与F1通讯，20位数据包
    抱死BK
    使能EN
    方向FR
    转速
    驱动器告警状态
    距离告警
    姿态告警
 * @modificationHistory
 */
public class infoForMotor {
    final String TAG="INFO_MOTO";
    boolean BK=false;
    boolean EN=false;
    boolean FR=false;
    boolean DRIVER_WARING=false;
    boolean DISTANCE_WARING=false;
    boolean PID=false;

    public infoForMotor(byte data)
    {
        Log.d(TAG,Integer.toBinaryString(data));
        if(((data>>7)&1)!=0)BK=true;
        else BK=false;
        if(((data>>6)&1)!=0)EN=true;
        else EN=false;
        if(((data>>5)&1)!=0){
            FR=true;
        }
        else {
            FR=false;
        }
        int temp=(data>>3)&3;
        if(((data>>2)&1)!=0)DRIVER_WARING=true;
        else DRIVER_WARING=false;
        if(((data>>1)&1)!=0)DISTANCE_WARING=true;
        else DISTANCE_WARING=false;
        if((data&1)!=0)PID=true;
        else PID=false;
    }

    public void set(byte data)
    {
        Log.d(TAG,Integer.toBinaryString(data));
        if(((data>>7)&1)!=0) BK=true;

        else BK=false;
        if(((data>>6)&1)!=0)EN=true;
        else EN=false;

        int temp=(data>>3)&3;
        if(temp==3) RobotStatus.infof1.pwmspeed=100;
        else if(temp==2) RobotStatus.infof1.pwmspeed=50;
        else if(temp==1) RobotStatus.infof1.pwmspeed=10;
        else if(temp==0) RobotStatus.infof1.pwmspeed=0;
        if(((data>>5)&1)!=0&&RobotStatus.status!=InfoForRobot.STATUS.STOP&&RobotStatus.status!=InfoForRobot.STATUS.MODE1){
            FR=true;
            RobotStatus.status= InfoForRobot.STATUS.LEFT;
        }
        if(((data>>5)&1)==0&&RobotStatus.status!=InfoForRobot.STATUS.STOP&&RobotStatus.status!=InfoForRobot.STATUS.MODE1) {
            FR=false;
            RobotStatus.status= InfoForRobot.STATUS.RIGHT;
        }
        if(((data>>2)&1)!=0)DRIVER_WARING=true;
        else DRIVER_WARING=false;
        if(((data>>1)&1)!=0)DISTANCE_WARING=true;
        else DISTANCE_WARING=false;
        if((data&1)!=0)PID=true;
        else PID=false;
    }

    public void show()
    {
        Log.d(TAG,"BK: "+BK);
        Log.d(TAG,"EN: "+EN);
        Log.d(TAG,"FR: "+FR);
        Log.d(TAG,"speed: "+RobotStatus.infof1.pwmspeed);
        Log.d(TAG,"DRIVER_WARING: "+DRIVER_WARING);
        Log.d(TAG,"DISTANCE_WARING: "+DISTANCE_WARING);
        Log.d(TAG,"PID: "+PID);
    }
}
