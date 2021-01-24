package com.example.robotremote.Hardware.Adc;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;

public class Adc{
    private final String ADC_PORT_PRE = "/sys/devices/platform/nxp-adc/iio:device0/in_voltage";
    private final String ADC_PORT_LAST = "_raw";
    private static final String TAG = "ReadAdcResult";
    private int adcChannel;
    private BufferedReader in;

    public Adc(int adcChannel){
        this.adcChannel = adcChannel;
        String file = ADC_PORT_PRE + adcChannel + ADC_PORT_LAST;//Adc 路径
        try {
            in = new BufferedReader(new FileReader(file));
        }catch (Exception e){e.printStackTrace();}
    }

    public void read()
    {
        try {
            String file = ADC_PORT_PRE + adcChannel + ADC_PORT_LAST;//Adc 路径
            in = new BufferedReader(new FileReader(file));
            int adc = Integer.parseInt(in.readLine());
            Log.d(TAG," "+adc);
        }catch (Exception e){e.printStackTrace();}
    }


}
