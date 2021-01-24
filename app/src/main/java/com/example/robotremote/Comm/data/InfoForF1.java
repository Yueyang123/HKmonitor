package com.example.robotremote.Comm.data;
import android.util.Log;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  与F1通讯，20位数据包
 *  X加速度
 *  y加速度
 *  z加速度
 *  X角速度
 *  y角速度
 *  z角速度
 *  X角度
 *  y角度
 *  z角度
 *  超声波测距
 * @modificationHistory
 */
public class InfoForF1
{
    final String TAG="F1DATA";
    int ax;
    int ay;
    int az;
    int atherax;
    int atheray;
    int atheraz;
    int therax;
    int theray;
    int theraz;
    int fordistance;
    public float pwmspeed;
    public infoForMotor infomotor=new infoForMotor((byte) 0);
    /**
     * F1 20位数据包初始化
     * */
    public void init(dataPacket data)
    {
        if(data.length!=20)return;
        else {
            ax=(int)data.daTa[0+2];
            ay=(int)data.daTa[1+2];
            az=(int)data.daTa[2+2];
            atherax=(int)data.daTa[3+2];
            atheray=(int)data.daTa[4+2];
            atheraz=(int)data.daTa[5+2];
            therax=((int)data.daTa[6+2]<<8)|(int)data.daTa[7+2];
            theray=((int)data.daTa[8+2]<<8)|(int)data.daTa[9+2];
            theraz=((int)data.daTa[10+2]<<8)|(int)data.daTa[11+2];
            fordistance=(int)data.daTa[12+2];
        }
    }

    public void initMotor(dataPacket data)
    {
        if(data.length!=8)return;
        infomotor.set(data.daTa[2]);
    }

    public void initSpeed(dataPacket speed)
    {
        this.pwmspeed=(int)speed.daTa[2];
    }
    public void show()
    {
        Log.d(TAG,Integer.toString(ax));
        Log.d(TAG,Integer.toString(ay));
        Log.d(TAG,Integer.toString(az));
        Log.d(TAG,Integer.toString(atherax));
        Log.d(TAG,Integer.toString(atheray));
        Log.d(TAG,Integer.toString(atheraz));
        Log.d(TAG,Integer.toString(therax));
        Log.d(TAG,Integer.toString(theray));
        Log.d(TAG,Integer.toString(theraz));
        Log.d(TAG,Integer.toString(fordistance));
        infomotor.show();
        Log.d(TAG,Float.toString(pwmspeed));
    }
}
