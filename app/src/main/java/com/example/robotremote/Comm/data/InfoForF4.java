package com.example.robotremote.Comm.data;

import android.util.Log;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  与F4通讯，64位数据包
 *   BMS总电量
 *   BMS总电压
 *   BMS总电流
 *   BMS温度1
 *   BMS温度15
 *   BMS电芯1电压
 *   BMS电芯15电压
 *  传感器湿度
 *  传感器温度
 *  气体浓度
 *  当前已运行距离
 * @modificationHistory
 */
public class InfoForF4
{
    final String TAG="F4DATA";
    public int BmsW;
    public int BmsV;
    public int BmsI;
    public int[] Bmst=new int[15];
    public int[] BmscoreV=new int[15];
    public int SenseS;
    public int SenseT;
    public int Gas;
    public int totaldistance;
    public float speed;
    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
     *
     * @param src
     *            byte数组
     * @param offset
     *            从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt16(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset+1] & 0xFF)
                | ((src[offset] & 0xFF)<<8)
                | ((0 & 0xFF)<<16)
                | ((0 & 0xFF)<<24));
        return value;
    }

    public void init(dataPacket data)
    {
        if(data.length!=64)return;
        BmsW=(int)data.daTa[0+2];
        BmsV=bytesToInt16(data.daTa,3);
        BmsI=bytesToInt16(data.daTa,5);;
        for(int i=0;i<15;i++)
            Bmst[i]=-50+(int)data.daTa[5+2+i];
        for(int i=0;i<15;i++)
            BmscoreV[i]=bytesToInt16(data.daTa,20+2+2*i);
        SenseS=(int)data.daTa[50+2];
        SenseT=bytesToInt16(data.daTa,51+2);
        Gas=bytesToInt16(data.daTa,53+2);
        float temp=(float) totaldistance;
        totaldistance=bytesToInt16(data.daTa,55+2);;
        speed= Math.abs((float)temp-(float)totaldistance)/100;
    }

    public void show()
    {
        Log.d(TAG,Integer.toString(BmsW));
        Log.d(TAG,Integer.toString(BmsV));
        Log.d(TAG,Integer.toString(BmsI));
        Log.d(TAG,Integer.toString(SenseS));
        Log.d(TAG,Integer.toString(SenseT));
        Log.d(TAG,Integer.toString(Gas));
        Log.d(TAG,Integer.toString(totaldistance));
    }
}