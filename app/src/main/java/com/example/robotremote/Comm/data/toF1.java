package com.example.robotremote.Comm.data;
import android.util.Log;
import com.example.robotremote.Comm.Warn.RobotWarn;
import com.example.robotremote.Hardware.Serial.*;
import com.example.robotremote.Comm.CRC.Crc;
import java.util.Calendar;
import static com.example.robotremote.Comm.data.toF4.RobotStatus;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  与F1通讯源代码
 *   左行
 *   右行
 *   停车
 *   刹车
 *   调速0-100%
 *   关闭电源
 *   WIFI故障后查询现场状况（兼作心跳包）
 *   查询F1姿态和测距值
 *   每秒查询F1电机状态（兼作心跳包）
 * @modificationHistory
 */
public class toF1 implements Runnable
{
    final String TAG="TOF1";
    public SerialCol serialCol;
    public dir Dir=dir.F12REMOTE;
    InfoForRobot RobotStatus;
    public boolean havedata=false;
    public static int[] sendflag={0,0,0,0,0,0,0,0,0,0};//发送位，如果要发将那一位置一
    public enum CMD
    {
        LEFT,//正转
        RIGHT,//反转
        STOP,//停机
        QUICKSTOP,//刹车
        SPEED,//调速0-100
        POWEROFF,// 关闭电源
        ASKHERT,//每秒查询现场状况（71）
        ASK,//查询F1姿态和超声测距值
        HEART,
        REMOVEWARN,
    };
    private byte[] head={0x00,0x04,0x40};
    public static int tof1Timeout=10000;
    final byte[][] cmd={{(byte) 0xA1,(byte)0xC1},
                        {(byte) 0xA1,(byte)0xC2},
                        {(byte) 0xA1,(byte)0xC3},
                        {(byte) 0xA1,(byte)0xC4},
                        {(byte) 0xA1,(byte)0xC5},
                        {(byte) 0xA1,(byte)0xD4},
                        {(byte) 0xA1,(byte)0x71},
                        {(byte) 0xA1,(byte)0xE2},
                        {(byte) 0xA1,(byte)0xE3},
                        {(byte) 0xA1,(byte)0xB1},};
    public enum dir
    {
        F12REMOTE,
        REMOTE2F1,
    };

    public toF1(SerialCol serialCol, dir Dir)
    {
        this.serialCol=serialCol;
        this.RobotStatus=toF4.RobotStatus;
        this.Dir=Dir;
    }

    static byte[] concat(byte[] a, byte[] b) {//字符串链接函数

        byte[] c= new byte[a.length+b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public void setSpeed(byte[] head, int speed)
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
        byte[] tof1=concat(head,tcmd);
        serialCol.send(tof1);
    }

    public void sendCMD(byte[] head,CMD Cmdtof1)
    {
        if(Cmdtof1!=CMD.SPEED) {
            Calendar calendar = Calendar.getInstance();
            byte[] tcmd = new byte[7];
            tcmd[0] = (cmd[Cmdtof1.ordinal()][0]);
            tcmd[1] = (cmd[Cmdtof1.ordinal()][1]);
            tcmd[2] = (byte) calendar.getTime().getHours();
            tcmd[3] = (byte) calendar.getTime().getMinutes();
            tcmd[4] = (byte) calendar.getTime().getSeconds();
            int crc = Crc.crc16(tcmd, 5);
            tcmd[5] = (byte) (crc >> 0 & 0xFF);
            tcmd[6] = (byte) (crc >> 8 & 0xFF);
            byte[] tof1=concat(head,tcmd);
            serialCol.send(tof1);
        }
    }

    @Override
    public void run()
    {
        boolean noHeart=false;
        int sta=0;
        int[] warntimes={0,0,0,0,0,0,0,0,0};//记录没有链接到的次数,超过十次没有收到相应警告
        while (true) {
            if (Dir == dir.F12REMOTE) {//接受F1的数据
                byte[] temp=new byte[2];
                serialCol.read(temp);
                if(temp[0]==0x1A) {
                    tof1Timeout=10000;
                    Log.d(TAG+"RECIVE DATA FROM F1",Integer.toHexString((int) temp[0])+" "+Integer.toHexString((int) temp[1]));
                    if((temp[1]&0xFF)!=0xC5&&(temp[1]&0xFF)!=0xE3&&(temp[1]&0xFF)!=0xE2&&temp[1]!=0x71)
                    {
                        byte[] temp1=new byte[5];
                        serialCol.read(temp1);
                        RobotStatus.data.daTa[0]=temp[0];
                        RobotStatus.data.daTa[1]=temp[1];
                        for(int i=0;i<temp1.length;i++) RobotStatus.data.daTa[i+2]=temp1[i];
                        RobotStatus.data.length=7;
                        if((temp[1]&0xFF)==0xC1||(temp[1]&0xFF)==0xC2||(temp[1]&0xFF)==0xC3)//向左，向右，不动
                        {
                            sendflag[CMD.RIGHT.ordinal()]=0;
                            sendflag[CMD.LEFT.ordinal()]=0;
                            sendflag[CMD.STOP.ordinal()]=0;
                        }//如果一直没有，就一直发
                        if((temp[1]&0xFF)==0xC4) sendflag[3]=0;
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
                        switch ((int)(temp[1]&0xFF))
                        {
                            case 0xC5:
                                sendflag[CMD.SPEED.ordinal()]=0;
                                temp1=new byte[6];
                                serialCol.read(temp1);
                                RobotWarn.WarnFlag[RobotWarn.Warn.LORAWarn.ordinal()]=false;
                                RobotStatus.data.daTa[0]=temp[0];
                                RobotStatus.data.daTa[1]=temp[1];
                                for(int i=0;i<temp1.length;i++)
                                    RobotStatus.data.daTa[i+2]=temp1[i];
                                RobotStatus.data.length=8;
                                break;
                            case 0xE3:
                                temp1=new byte[6];
                                serialCol.read(temp1);
                                RobotWarn.WarnFlag[RobotWarn.Warn.LORAWarn.ordinal()]=false;
                                RobotStatus.data.daTa[0]=temp[0];
                                RobotStatus.data.daTa[1]=temp[1];
                                for(int i=0;i<temp1.length;i++)
                                    RobotStatus.data.daTa[i+2]=temp1[i];
                                RobotStatus.data.length=8;
                                break;
                            case 0xE2:
                                temp1=new byte[18];
                                RobotStatus.data.daTa[0]=temp[0];
                                RobotStatus.data.daTa[1]=temp[1];
                                serialCol.read(temp1);
                                for(int i=0;i<temp1.length;i++) {
                                    RobotStatus.data.daTa[i + 2] = temp1[i];
                                }
                                RobotStatus.data.length=20;
                                break;
                            case 0x71:
                                temp1=new byte[62];
                                serialCol.read(temp1);
                                RobotStatus.data.daTa[0]=temp[0];
                                RobotStatus.data.daTa[1]=temp[1];
                                for(int i=0;i<temp1.length;i++)
                                    RobotStatus.data.daTa[i+2]=temp1[i];
                                RobotStatus.data.length=64;
                                break;
                        }
                    }
                    RobotStatus.processF1data();
                    havedata=true;
                }
            }else//发送数据
            {
                sta++;
                noHeart=false;
                if(sta==1) {
                    if (sendflag[CMD.LEFT.ordinal()] != 0) {//第一种发送命令
                        sendCMD(head, CMD.LEFT);
                        noHeart = true;
                        sendflag[CMD.LEFT.ordinal()]++;
                        if (sendflag[CMD.LEFT.ordinal()] > 3) sendflag[CMD.LEFT.ordinal()] = 0;//超过三次不再发送
                    }
                    if (sendflag[CMD.RIGHT.ordinal()] != 0) {//一种发送命令
                        sendCMD(head, CMD.RIGHT);
                        noHeart = true;
                        sendflag[CMD.RIGHT.ordinal()]++;
                        if (sendflag[CMD.RIGHT.ordinal()] > 3) sendflag[CMD.RIGHT.ordinal()] = 0;//超过三次不再发送
                    }
                    if (sendflag[CMD.STOP.ordinal()] != 0) {//一种发送命令
                        sendCMD(head, CMD.STOP);
                        RobotStatus.infof1.pwmspeed = 0;
                        noHeart = true;
                        sendflag[CMD.STOP.ordinal()]++;
                        if (sendflag[CMD.STOP.ordinal()] > 5) sendflag[CMD.STOP.ordinal()] = 0;//超过三次不再发送
                    }
                    if (sendflag[CMD.QUICKSTOP.ordinal()] != 0) {//一种发送命令
                        sendCMD(head, CMD.QUICKSTOP);
                        noHeart = true;
                        sendflag[CMD.QUICKSTOP.ordinal()]++;
                        if (sendflag[CMD.QUICKSTOP.ordinal()] > 3) sendflag[CMD.QUICKSTOP.ordinal()] = 0;//超过三次不再发送
                    }
                    if (sendflag[CMD.POWEROFF.ordinal()] != 0) {//一种发送命令
                        sendCMD(head, CMD.POWEROFF);
                        noHeart = true;
                        sendflag[CMD.POWEROFF.ordinal()]++;
                        if (sendflag[CMD.POWEROFF.ordinal()] > 3) sendflag[CMD.POWEROFF.ordinal()] = 0;//超过三次不再发送
                    }
                    if (sendflag[CMD.REMOVEWARN.ordinal()] != 0) {//一种发送命令
                        sendCMD(head, CMD.REMOVEWARN);
                        noHeart = true;
                        sendflag[CMD.REMOVEWARN.ordinal()]++;
                        if (sendflag[CMD.REMOVEWARN.ordinal()] > 3) sendflag[CMD.REMOVEWARN.ordinal()] = 0;//超过三次不再发送
                    }
                    if(noHeart==false)sendCMD(head,CMD.HEART);//LORA心跳包
                }
                if(sta==2)
                {
                    sendCMD(head,CMD.HEART);//LORA心跳包
                }
                if(sta==3)
                {
                    if (sendflag[CMD.SPEED.ordinal()] != 0) {//一种发送命令
                        if (sendflag[CMD.SPEED.ordinal()] % 4 == 2) {//一种发送命令 中速
                            setSpeed(head, 50);
                            sendflag[CMD.SPEED.ordinal()] += 4;
                            if (sendflag[CMD.SPEED.ordinal()] > 5) sendflag[CMD.SPEED.ordinal()] = 0;//超过三次不再发送
                        }
                        if (sendflag[CMD.SPEED.ordinal()] % 4 == 1) {//一种发送命令 低速
                            setSpeed(head, 10);
                            sendflag[CMD.SPEED.ordinal()] += 4;
                            if (sendflag[CMD.SPEED.ordinal()] > 5) sendflag[CMD.SPEED.ordinal()] = 0;//超过三次不再发送
                        }
                        if (sendflag[CMD.SPEED.ordinal()] % 4 == 3) {//一种发送命令 高速
                            setSpeed(head, 100);
                            sendflag[CMD.SPEED.ordinal()] += 4;
                            if (sendflag[CMD.SPEED.ordinal()] > 5) sendflag[CMD.SPEED.ordinal()] = 0;//超过三次不再发送
                        }
                    }
                    sta=0;
                }
                try {
                    Thread.sleep(333);
                }catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

        }
    }
}