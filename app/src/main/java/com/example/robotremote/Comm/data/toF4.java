package com.example.robotremote.Comm.data;
import android.util.Log;
import com.example.robotremote.Comm.Warn.RobotWarn;
import com.example.robotremote.Hardware.NetCol.*;
import com.example.robotremote.Comm.CRC.Crc;
import java.util.Calendar;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  与F4通讯源代码
 * @modificationHistory
 */
public class toF4 implements Runnable
{
    final String TAG="TOF4";
    public static NetCol net;
    public dir Dir=dir.F42REMOTE;
    public static InfoForRobot RobotStatus=new InfoForRobot();
    public boolean havedata=false;//通讯控制器向UI进行更新
    public static int tof4Timeout=0;//链接超时
    public static int[] sendflag={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};//发送位，如果要发将那一位置一
    final byte[][] cmd= {{(byte) 0xA4,(byte)0xC1},
                        {(byte) 0xA4,(byte)0xC2},
                        {(byte) 0xA4,(byte)0xC3},
                        {(byte) 0xA4,(byte)0xC4},
                        {(byte) 0xA4,(byte)0xC5},
                        {(byte) 0xA4,(byte)0xD0},
                        {(byte) 0xA4,(byte)0xD1},
                        {(byte) 0xA4,(byte)0xD2},
                        {(byte) 0xA4,(byte)0xD3},
                        {(byte) 0xA4,(byte)0xD4},
                        {(byte) 0xA4,(byte)0xD5},
                        {(byte) 0xA4,(byte)0x71},
                        {(byte) 0xA4,(byte)0xE2},
                        {(byte) 0xA4,(byte)0xE3},
                        {(byte) 0xA4,(byte)0xB1}};

    public enum dir
    {
        F42REMOTE,
        REMOTE2F4,
    };
    public enum CMD
    {
        LEFT,//正转
        RIGHT,//反转
        STOP,//停机
        QUICKSTOP,//刹车
        SPEED,//调速0-100
        POS,  //运动到指定坐标，00 00表示起点，xx xx为终点
        MODE1,//遥控模式
        MODE2,//跟机模式
        MODE3,//自动巡航
        POWEROFF,// 关闭电源
        PREPARE,
        ASKHERT,//每秒查询现场状况（兼作心跳包）
        ASK,//查询F1姿态和超声测距值
        F1ASK,//F1心跳包
        REMOVEWARN
    };

    public toF4(NetCol net,dir Dir)
    {
        this.net=net;
        this.Dir=Dir;
    }

    public void setSpeed(int speed)
    {
        Calendar calendar = Calendar.getInstance();
        byte[] tcmd=new byte[8];
        tcmd[0]=(cmd[4][0]);
        tcmd[1]=(cmd[4][1]);
        tcmd[2]=(byte)speed;
        tcmd[3]=(byte)calendar.getTime().getHours();
        tcmd[4]=(byte)calendar.getTime().getMinutes();;
        tcmd[5]=(byte)calendar.getTime().getSeconds();;
        int crc=Crc.crc16(tcmd,6);
        tcmd[6]=(byte)(crc>>0&0xFF);
        tcmd[7]=(byte)(crc>>8&0xFF);
        net.send(tcmd);
    }

    public void setPos(int x,int y)
    {
        Calendar calendar = Calendar.getInstance();
        byte[] tcmd=new byte[9];
        tcmd[0]=(cmd[5][0]);
        tcmd[1]=(cmd[5][1]);
        tcmd[2]=(byte) x;
        tcmd[3]=(byte) y;
        tcmd[4]=(byte)calendar.getTime().getHours();
        tcmd[5]=(byte)calendar.getTime().getMinutes();;
        tcmd[6]=(byte)calendar.getTime().getSeconds();;
        int crc=Crc.crc16(tcmd,7);
        Log.d(TAG, Integer.toString(crc));
        tcmd[7]=(byte)(crc>>0&0xFF);
        tcmd[8]=(byte)(crc>>8&0xFF);
        net.send(tcmd);
    }

    /**
     * 7位定长发送
     * */
    public void sendCMD(CMD Cmdtof4)
    {
        if(Cmdtof4!=CMD.SPEED||Cmdtof4!=CMD.POS) {
            Calendar calendar = Calendar.getInstance();
            byte[] tcmd = new byte[7];
            tcmd[0] = (cmd[Cmdtof4.ordinal()][0]);
            tcmd[1] = (cmd[Cmdtof4.ordinal()][1]);
            tcmd[2] = (byte) calendar.getTime().getHours();
            tcmd[3] = (byte) calendar.getTime().getMinutes();
            tcmd[4] = (byte) calendar.getTime().getSeconds();
            int crc = Crc.crc16(tcmd, 5);
            tcmd[5] = (byte) (crc >> 0 & 0xFF);
            tcmd[6] = (byte) (crc >> 8 & 0xFF);
            net.send(tcmd);
        }
    }
    /**
     * 清除发送指示位
     * */
    public static void clear()
    {
        for(int i=0;i<13;i++)
            sendflag[i]=0;
    }

    public void run()
    {
        int sta=0;
        int[] warntimes={0,0,0,0,0,0,0,0,0};//记录没有链接到的次数,超过十次没有收到相应警告
        while (true) {
            if (Dir == dir.F42REMOTE) {//接受环节
                byte[] temp=new byte[2];
                net.read(temp);
                if(temp[0]==0x4A) {
                    Log.d(TAG+"RECIVE DATA FROM F4",Integer.toHexString((int) temp[0])+" "+Integer.toHexString((int) temp[1]));
                    tof4Timeout=10000;//更新timeout
                    if((temp[1]&0xFF)!=0xC5&&(temp[1]&0xFF)!=0xD0&&(temp[1]&0xFF)!=0xE2&&temp[1]!=0x71&&(temp[1]&0xFF)!=0xE3)
                    {
                        byte[] temp1=new byte[5];
                        net.read(temp1);
                        RobotStatus.data.daTa[0]=temp[0];
                        RobotStatus.data.daTa[1]=temp[1];
                        for(int i=0;i<temp1.length;i++) RobotStatus.data.daTa[i+2]=temp1[i];
                        RobotStatus.data.length=7;
                        if((temp[1]&0xFF)==0xC1||(temp[1]&0xFF)==0xC2||(temp[1]&0xFF)==0xC3)//向左，向右，不动
                        {
                            sendflag[CMD.LEFT.ordinal()]=0;
                            sendflag[CMD.RIGHT.ordinal()]=0;
                            sendflag[CMD.STOP.ordinal()]=0;
                        }//如果一直没有，就一直发
                        if((temp[1]&0xFF)==0xC4) sendflag[CMD.QUICKSTOP.ordinal()] = 0;

                        if((temp[1]&0xFF)==0x89){
                            warntimes[0]=0;
                            RobotWarn.WarnFlag[RobotWarn.Warn.SMOKEWarn.ordinal()]=true;//警告
                        }
                        else if((temp[1]&0xFF)==0x8A){
                            warntimes[1]=0;
                            RobotWarn.WarnFlag[RobotWarn.Warn.GASWarn.ordinal()]=true;//警告
                        }
                        else if((temp[1]&0xFF)==0x83){
                            warntimes[2]=0;
                            RobotWarn.WarnFlag[RobotWarn.Warn.POSTUREWarn.ordinal()]=true;//警告
                        }
                        else if((temp[1]&0xFF)==0x84){
                            warntimes[3]=0;
                            RobotWarn.WarnFlag[RobotWarn.Warn.DISTANCEWarn.ordinal()]=true;//警告
                        }
                        else if((temp[1]&0xFF)==0x82){
                            warntimes[4]=0;
                            RobotWarn.WarnFlag[RobotWarn.Warn.UDPWarn.ordinal()]=true;//警告
                        }
                        else if((temp[1]&0xFF)==0x86){
                            warntimes[5]=0;
                            RobotWarn.WarnFlag[RobotWarn.Warn.TCPWarn.ordinal()]=true;//警告
                        }
                        else if((temp[1]&0xFF)==0x88){
                            warntimes[6]=0;
                            RobotWarn.WarnFlag[RobotWarn.Warn.MOTORWarn.ordinal()]=true;//警告
                        }
                        else if((temp[1]&0xFF)==0x95){
                            warntimes[7]=0;
                            RobotWarn.WarnFlag[RobotWarn.Warn.DATAWarn.ordinal()]=true;//警告
                        }
                        else if((temp[1]&0xFF)==0x85){
                            warntimes[8]=0;
                            RobotWarn.WarnFlag[RobotWarn.Warn.MCUWarn.ordinal()]=true;//警告
                        }
                        if((temp[1]&0xFF)!=0x89){
                            warntimes[0]++;
                            if(warntimes[0]%10==0) RobotWarn.WarnFlag[RobotWarn.Warn.SMOKEWarn.ordinal()]=false;//警告
                        }
                        if((temp[1]&0xFF)!=0x8A){
                            warntimes[1]++;
                            if(warntimes[1]%10==0) RobotWarn.WarnFlag[RobotWarn.Warn.GASWarn.ordinal()]=false;//警告
                        }
                        if((temp[1]&0xFF)!=0x83){
                            warntimes[2]++;
                            if(warntimes[2]%10==0) RobotWarn.WarnFlag[RobotWarn.Warn.POSTUREWarn.ordinal()]=false;//警告
                        }
                        if((temp[1]&0xFF)!=0x84){
                            warntimes[3]++;
                            if(warntimes[3]%10==0) RobotWarn.WarnFlag[RobotWarn.Warn.DISTANCEWarn.ordinal()]=false;//警告
                        }
                        if((temp[1]&0xFF)!=0x82){
                            warntimes[4]++;
                            if(warntimes[4]%10==0) RobotWarn.WarnFlag[RobotWarn.Warn.UDPWarn.ordinal()]=false;//警告
                        }
                        if((temp[1]&0xFF)!=0x86){
                            warntimes[5]++;
                            if(warntimes[5]%10==0) RobotWarn.WarnFlag[RobotWarn.Warn.TCPWarn.ordinal()]=false;
                        }
                        if((temp[1]&0xFF)!=0x88){
                            warntimes[6]++;
                            if(warntimes[6]%10==0) RobotWarn.WarnFlag[RobotWarn.Warn.MOTORWarn.ordinal()]=false;
                        }
                        if((temp[1]&0xFF)!=0x95){
                            warntimes[7]++;
                            if(warntimes[7]%10==0) RobotWarn.WarnFlag[RobotWarn.Warn.DATAWarn.ordinal()]=false;
                        }
                        if((temp[1]&0xFF)!=0x85){
                            warntimes[8]++;
                            if(warntimes[8]%10==0) RobotWarn.WarnFlag[RobotWarn.Warn.MCUWarn.ordinal()]=false;
                        }
                    }
                    else {
                        byte[] temp1;
                        switch (temp[1]&0xFF)
                        {
                            case 0xC5:
                                sendflag[4]=0;
                                temp1=new byte[6];
                                net.read(temp1);
                                RobotStatus.data.daTa[0]=temp[0];
                                RobotStatus.data.daTa[1]=temp[1];
                                for(int i=0;i<temp1.length;i++)
                                    RobotStatus.data.daTa[i+2]=temp1[i];
                                RobotStatus.data.length=8;
                                break;
                            case 0xD0:
                                temp1=new byte[7];
                                net.read(temp1);
                                RobotStatus.data.daTa[0]=temp[0];
                                RobotStatus.data.daTa[1]=temp[1];
                                for(int i=0;i<temp1.length;i++)
                                    RobotStatus.data.daTa[i+2]=temp1[i];
                                RobotStatus.data.length=9;
                                break;
                            case 0xE2:
                                temp1=new byte[18];
                                net.read(temp1);
                                RobotStatus.data.daTa[0]=temp[0];
                                RobotStatus.data.daTa[1]=temp[1];
                                for(int i=0;i<temp1.length;i++)
                                    RobotStatus.data.daTa[i+2]=temp1[i];
                                RobotStatus.data.length=20;
                                break;
                            case 0x71:
                                sendflag[11]=0;
                                RobotWarn.WarnFlag[RobotWarn.Warn.WIFIWarn.ordinal()]=false;
                                temp1=new byte[62];
                                net.read(temp1);
                                RobotStatus.data.daTa[0]=temp[0];
                                RobotStatus.data.daTa[1]=temp[1];
                                for(int i=0;i<temp1.length;i++)
                                    RobotStatus.data.daTa[i+2]=temp1[i];
                                RobotStatus.data.length=64;
                                break;

                            case 0xE3:
                                temp1=new byte[6];
                                net.read(temp1);
                                RobotWarn.WarnFlag[RobotWarn.Warn.WIFIWarn.ordinal()]=false;
                                RobotStatus.data.daTa[0]=temp[0];
                                RobotStatus.data.daTa[1]=temp[1];
                                for(int i=0;i<temp1.length;i++)
                                    RobotStatus.data.daTa[i+2]=temp1[i];
                                RobotStatus.data.length=8;
                                break;
                        }
                    }
                    RobotStatus.processF4data();
                    havedata=true;
                }
            }else//发送环节
            {
                sta++;//一秒发送一次
                    if (sta == 1) {
                        if (sendflag[CMD.QUICKSTOP.ordinal()] != 0) {//一种发送命令
                            sendCMD(CMD.QUICKSTOP);
                            sendflag[CMD.QUICKSTOP.ordinal()]++;
                            if (sendflag[CMD.QUICKSTOP.ordinal()] > 3) sendflag[CMD.QUICKSTOP.ordinal()] = 0;//超过三次不再发送
                        } else {
                            if (sendflag[CMD.LEFT.ordinal()] != 0) {//第一种发送命令
                                sendCMD(CMD.LEFT);
                                sendflag[CMD.LEFT.ordinal()]++;
                                if (sendflag[CMD.LEFT.ordinal()] > 3) sendflag[CMD.LEFT.ordinal()] = 0;//超过三次不再发送
                            }
                            if (sendflag[CMD.RIGHT.ordinal()] != 0) {//一种发送命令
                                sendCMD(CMD.RIGHT);
                                sendflag[CMD.RIGHT.ordinal()]++;
                                if (sendflag[CMD.RIGHT.ordinal()] > 3) sendflag[CMD.RIGHT.ordinal()] = 0;//超过三次不再发送
                            }
                            if (sendflag[CMD.STOP.ordinal()] != 0) {//一种发送命令
                                sendCMD(CMD.STOP);
                                sendflag[CMD.STOP.ordinal()]++;
                                if (sendflag[CMD.STOP.ordinal()] > 3) sendflag[CMD.STOP.ordinal()] = 0;//超过三次不再发送
                            }
                            if (sendflag[CMD.POS.ordinal()] != 0) {//一种发送命令
                                setPos(0, 0);
                                sendflag[CMD.POS.ordinal()]++;
                                if (sendflag[CMD.POS.ordinal()] > 3) sendflag[CMD.POS.ordinal()] = 0;//超过三次不再发送
                            }
                            if (sendflag[CMD.MODE1.ordinal()] != 0) {//一种发送命令
                                sendCMD(CMD.MODE1);
                                sendflag[CMD.MODE1.ordinal()]++;
                                if (sendflag[CMD.MODE1.ordinal()] > 3) sendflag[CMD.MODE1.ordinal()] = 0;//超过三次不再发送
                            }
                            if (sendflag[CMD.MODE2.ordinal()] != 0) {//一种发送命令
                                sendCMD(CMD.MODE2);
                                sendflag[CMD.MODE2.ordinal()]++;
                                if (sendflag[CMD.MODE2.ordinal()] > 3) sendflag[CMD.MODE2.ordinal()] = 0;//超过三次不再发送
                            }
                            if (sendflag[CMD.MODE3.ordinal()] != 0) {//一种发送命令
                                sendCMD(CMD.MODE3);
                                sendflag[CMD.MODE3.ordinal()]++;
                                if (sendflag[CMD.MODE3.ordinal()] > 3) sendflag[CMD.MODE3.ordinal()] = 0;//超过三次不再发送
                            }
                            if (sendflag[CMD.POWEROFF.ordinal()] != 0) {//一种发送命令
                                sendCMD(CMD.POWEROFF);
                                sendflag[CMD.POWEROFF.ordinal()]++;
                                if (sendflag[CMD.POWEROFF.ordinal()] > 3) sendflag[CMD.POWEROFF.ordinal()] = 0;//超过三次不再发送
                            }
                            if (sendflag[CMD.PREPARE.ordinal()] != 0) {//一种发送命令
                                sendCMD(CMD.PREPARE);
                                sendflag[CMD.PREPARE.ordinal()]++;
                                if (sendflag[CMD.PREPARE.ordinal()] > 3) sendflag[CMD.PREPARE.ordinal()] = 0;//超过三次不再发送
                            }
                            if (sendflag[CMD.ASK.ordinal()] != 0) {//一种发送命令
                                sendCMD(CMD.ASK);
                                sendflag[CMD.ASK.ordinal()]++;
                                if (sendflag[CMD.ASK.ordinal()] > 3) sendflag[CMD.ASK.ordinal()] = 0;//超过三次不再发送
                            }
                            if (sendflag[CMD.REMOVEWARN.ordinal()] != 0) {//一种发送命令
                                sendCMD(CMD.REMOVEWARN);
                                sendflag[CMD.REMOVEWARN.ordinal()]++;
                                if (sendflag[CMD.REMOVEWARN.ordinal()] > 3) sendflag[CMD.REMOVEWARN.ordinal()] = 0;//超过三次不再发送
                            }
                        }
                    }
                    if (sta == 2) {
                        sendCMD(CMD.ASKHERT);//LORA心跳包
                    }
                    if (sta == 3) {
                        if (sendflag[CMD.SPEED.ordinal()] != 0) {//一种发送命令
                            if (sendflag[CMD.SPEED.ordinal()] % 4 == 2) {//一种发送命令 中速
                                setSpeed(50);
                                sendflag[CMD.SPEED.ordinal()] += 4;
                                if (sendflag[CMD.SPEED.ordinal()] > 5) sendflag[CMD.SPEED.ordinal()] = 0;//超过三次不再发送
                            }
                            if (sendflag[CMD.SPEED.ordinal()] % 4 == 1) {//一种发送命令 低速
                                setSpeed(10);
                                sendflag[CMD.SPEED.ordinal()] += 4;
                                if (sendflag[CMD.SPEED.ordinal()] > 5) sendflag[CMD.SPEED.ordinal()] = 0;//超过三次不再发送
                            }
                            if (sendflag[CMD.SPEED.ordinal()] % 4 == 3) {//一种发送命令 高速
                                setSpeed(100);
                                sendflag[CMD.SPEED.ordinal()] += 4;
                                if (sendflag[CMD.SPEED.ordinal()] > 5) sendflag[CMD.SPEED.ordinal()] = 0;//超过三次不再发送
                            }
                        }
                    }
                    if(sta==4){
                        if(RobotWarn.WarnFlag[RobotWarn.Warn.LORAWarn.ordinal()]==true)sendCMD(CMD.F1ASK);
                        sendflag[CMD.F1ASK.ordinal()]=0;
                        sta = 0;
                    }
                try {
                    Thread.sleep(250);
                }catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

}
