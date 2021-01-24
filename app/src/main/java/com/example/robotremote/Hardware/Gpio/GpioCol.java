package com.example.robotremote.Hardware.Gpio;


import com.example.robotremote.HardCol;

public class GpioCol {

    public GpioCol(int max)
    {
        HardCol.GpioMalloc(max);
    }
    public static enum GpioDirection{
        GPIO_DIRECTION_INPUT(0),GPIO_DIRECTION_OUTPUT(1);
        private int value;
        private GpioDirection(int value){
            this.value = value;
        }
        public int getValue(){
            return value;
        }
    }

    public  enum GpioPortLimit{
        GPIO_PORT_MAX("max",166),GPIO_PORT_MIN("min",0);

        private int value;
        private String name;
        private GpioPortLimit(String name, int value){
            this.name = name;
            this.value = value;
        }

        public int getValue(){
            return value;
        }
    }

    public boolean gpioInit(int portIndex,String gpioString){

        int port = 0;

        if(gpioString.substring(0,4).equals("GPIO"))
            if(gpioString.length() == "GPIOA0".length()){
                port = (gpioString.charAt(4) - 'A') * 32;
                port += Character.getNumericValue(gpioString.charAt(5));
            }else if(gpioString.length() == "GPIOA10".length()){
                port = (gpioString.charAt(4) - 'A') * 32;
                port += Character.getNumericValue(gpioString.charAt(5)) * 10;
                port += Character.getNumericValue(gpioString.charAt(6));
            }else if(gpioString.length() == "GPIOALIVE0".length()){

                port = 160;
                port += Character.getNumericValue(gpioString.charAt(9));
            }

        if(port <= GpioPortLimit.GPIO_PORT_MAX.getValue() && port >= GpioPortLimit.GPIO_PORT_MIN.getValue()){
            return HardCol.GpioInit(portIndex,port);
        }
        return false;
    }

    public void gpioSetDirection(int portIndex,GpioDirection gpioDirection ){
        HardCol.GpioSetDirection(portIndex,gpioDirection==GpioDirection.GPIO_DIRECTION_OUTPUT);
    }

    public void gpioSetValue(int portIndex,int value){
        HardCol.GpioSetValue(portIndex,value);
    }

    public int gpioGetValue(int portIndex){
        return HardCol.GpioGetValue(portIndex);
    }

    public void gpioClose(int portIndex){
        HardCol.GpioClose(portIndex);
    }


}
