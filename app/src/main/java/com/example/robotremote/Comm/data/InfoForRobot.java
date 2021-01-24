package com.example.robotremote.Comm.data;
import com.example.robotremote.Comm.CRC.Crc;

/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  机器人信息，
 *  包括与F1的20位数据
 *  与F4的64位数据
 * @modificationHistory
 */

public class InfoForRobot
{
    final String TAG="ROBOT DATA";
    public dataPacket data=new dataPacket();
    public STATUS status=STATUS.STOP;
    public InfoForF1 infof1=new InfoForF1();
    public InfoForF4 infof4=new InfoForF4();

    public enum STATUS
    {
        LEFT,//正转
        RIGHT,//反转
        STOP,//停机
        QUICKSTOP,//刹车
        MODE1,//遥控模式
        MODE2,//跟机模式
        MODE3,//自动巡航
        POWEROFF,// 确认关闭电源
    };

    public void processF1data()
    {
        int crc = Crc.crc16(data.daTa, data.length - 2);
        if (data.daTa[data.length - 1] == (byte) (crc >> 8 & 0xFF) && data.daTa[data.length - 2] == (byte) (crc >> 0 & 0xFF)) {
            switch ((int)data.daTa[1]&0xFF)
            {
                case 0xC1:
                    if(status!= STATUS.QUICKSTOP)
                    status= STATUS.LEFT;
                    break;
                case 0xC2:
                    if(status!= STATUS.QUICKSTOP)
                    status= STATUS.RIGHT;
                    break;
                case 0xC3:
                    if(status!= STATUS.QUICKSTOP)
                    status= STATUS.STOP;
                    break;
                case 0xC4:
                    status= STATUS.QUICKSTOP;
                    break;
                case 0xD4:
                    status= STATUS.POWEROFF;
                    break;
                case 0xE2:
                    infof1.init(data);
                    break;
                case 0x71:
                    infof4.init(data);
                    break;
                case 0xC5:
                    infof1.initSpeed(data);
                    break;
                case 0xE3:
                    infof1.initMotor(data);
                    break;
                default:
                    break;
            }
        } else {
            for (int i = 0; i < data.length; i++) data.daTa[i] = 0;
            return;
        }
        if (toF4.net.connectstatus == true && data.daTa[0] != 0) {
            for (int i = 0; i < data.length; i++) data.daTa[i] = 0;
        }
    }

    public void processF4data()
    {
        int crc = Crc.crc16(data.daTa, data.length - 2);
        if (data.daTa[data.length - 1] == (byte) (crc >> 8 & 0xFF) && data.daTa[data.length - 2] == (byte) (crc >> 0 & 0xFF)) {
            switch ((int)data.daTa[1]&0xFF)
            {
                case 0xC1:
                    if(status!= STATUS.QUICKSTOP)
                    status= STATUS.LEFT;
                    break;
                case 0xC2:
                    if(status!= STATUS.QUICKSTOP)
                    status= STATUS.RIGHT;
                    break;
                case 0xC3:
                    if(status!= STATUS.QUICKSTOP)
                    status= STATUS.STOP;
                    break;
                case 0xC4:
                    status= STATUS.QUICKSTOP;
                    break;
                case 0xD1:
                    if(status!= STATUS.QUICKSTOP)
                    status= STATUS.MODE1;
                    break;
                case 0xD2:
                    if(status!= STATUS.QUICKSTOP)
                    status= STATUS.MODE2;
                    break;
                case 0xD3:
                    if(status!= STATUS.QUICKSTOP)
                    status= STATUS.MODE3;
                    break;
                case 0xD4:
                    status= STATUS.POWEROFF;
                    break;
                case 0xE2:
                    infof1.init(data);
                    break;
                case 0xE3:
                    infof1.initMotor(data);
                    break;
                case 0x71:
                    infof4.init(data);
                    break;
                case 0xC5:
                    infof1.initSpeed(data);
                    break;
                default:
                    break;
            }
        } else {
            for (int i = 0; i < data.length; i++) data.daTa[i] = 0;
            return;
        }
        if (toF4.net.connectstatus == true && data.daTa[0] != 0) {
            for (int i = 0; i < data.length; i++) data.daTa[i] = 0;
        }
    }
}

