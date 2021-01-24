package com.example.robotremote.ui;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.robotremote.Comm.Warn.RobotWarn;
import com.example.robotremote.Comm.data.InfoForRobot;
import com.example.robotremote.HIKIVideo.Audio;
import com.example.robotremote.HIKIVideo.HIKILogin;
import com.example.robotremote.HIKIVideo.MethodUtils;
import com.example.robotremote.HIKIVideo.PlaySurfaceview;
import com.example.robotremote.HIKIVideo.Videoinfo;
import com.example.robotremote.Hardware.Beep.Beep;
import com.example.robotremote.Hardware.Gpio.GpioCol;
import com.example.robotremote.Hardware.Key.KeyCol;
import com.example.robotremote.Hardware.Led.LedCol;
import com.example.robotremote.Hardware.NetCol.NetCol;
import com.example.robotremote.R;
import com.example.robotremote.Hardware.Serial.SerialCol;
import com.example.robotremote.Comm.data.toF4;
import com.example.robotremote.Comm.data.toF1;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.PTZCommand;
import static com.example.robotremote.Comm.data.toF4.RobotStatus;

/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  主活动源代码
 * @modificationHistory
 */
public class MainActivity extends Activity {
    private final String TAG = "MainActivity";
    private Context context;
    public  static RobotWarn robotWarn;//处理所有的警告
    public  int    Warnat=0;           //当前显示的警告
    private NetCol netCol;
    /**
     * 通讯控制
     * */
    private toF4      remotetof4;
    private toF4      f4toremote;
    /**
     * 子线程
     * */
    private Thread    remotetof4th;
    private Thread    f4toremoteth;
    private Thread    remotetof1th;
    private Thread    f1toremoteth;
    private Thread    robotwarnTextviewth;
    private Thread    netth;
    private Thread    initThread;
    private Thread    logicth;
    private LogicMain logicmain;
    /**
     * 窗口上的元素
     */
    private RobotwarnTextview  robotwarnTextview=null;
    private PlaySurfaceview    surface1   = null;
    private PlaySurfaceview    surface2   = null;
    private TextView           gas        = null;
    private TextView           speed      = null;
    private TextView           status     = null;
    private RobotProgressBar   progressBar= null;
    private ImageView          pcconncet  = null;//链接警告
    private ImageView          dir        = null;//方向警告
    private ImageView          alarm      = null;//其他警告
    private ImageView          Smoke      = null;//烟雾警告
    private Button             leftButton = null;
    private Button             rightButton= null;
    private Button             upButton   = null;
    private Button             downButton = null;
    private BatteryView        batteryView= null;
    private RelativeLayout     ptzvis     = null;
    /**
     * 图片资源
     * */
    Bitmap b_smoke;
    Bitmap b_pcconnect;
    Bitmap b_dir;
    Bitmap b_alarm;
    Bitmap b_stop;
    /**
     * 相关变量
     * */
    private int Robotwhere=0;                  //当前机器人所出的位置
    private int ptzsurface=0;                  //PTZ遥控指向
    private int ptztimeout=10000;              //PTZ消除所需要的时间
    private GestureDetector        mDetector1; //手势检测
    private GestureDetector        mDetector2;
    private ColorMatrixColorFilter grayColorFilter;
    /**
     * 决定当前那个窗口
     */
    private boolean isSurface1;
    private boolean isSurface2;
    /**
     * 与摄像头有关的变量
     * */
    private  HIKILogin[] login =new HIKILogin[2];//存储登录信息
    private  Videoinfo[] vid   =new Videoinfo[2];//存储摄像头信息
    public static Audio[]audio =new Audio[2];    //存储话筒信息
    /**
     * 在线程中进行,UI显示
     * */
    public   Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x00://更新UI
                    String gasstr="瓦斯:"+ RobotStatus.infof4.Gas;
                    String statusstr;
                    switch (RobotStatus.status)
                    {
                        case MODE2:
                            statusstr="状态:"+"跟机";
                            break;
                        case MODE3:
                            statusstr="状态:"+"自动";
                            break;
                        default:
                            statusstr="状态:"+"遥控";
                            break;
                    }
                    String carspeed="车速:"+ RobotStatus.infof4.speed+"m/s";
                    /**
                     * 更新字符显示
                     * */
                    gas.setText(gasstr);
                    speed.setText(carspeed);
                    status.setText(statusstr);
                    /**
                     * 更新警告按键
                     * */
                    if(RobotWarn.WarnFlag[RobotWarn.Warn.TCPWarn.ordinal()]==true&&RobotWarn.WarnFlag[RobotWarn.Warn.UDPWarn.ordinal()]==true)
                        pcconncet.setColorFilter(grayColorFilter);
                    else
                        pcconncet.clearColorFilter();
                    if(RobotWarn.WarnFlag[RobotWarn.Warn.SMOKEWarn.ordinal()]==true)
                        Smoke.clearColorFilter();
                    else
                        Smoke.setColorFilter(grayColorFilter);
                    /**
                     * 更新方向图标
                     * */
                    if(RobotStatus.status== InfoForRobot.STATUS.RIGHT){
                        dir.setImageBitmap(b_dir);
                        dir.setRotation(0);
                    }
                    else if(RobotStatus.status== InfoForRobot.STATUS.LEFT){
                        dir.setImageBitmap(b_dir);
                        dir.setRotation(180);
                    }
                    else{
                        dir.setImageBitmap(b_stop);
                        dir.setRotation(0);
                    }
                    break;
                case 0x01:
                    /**
                     * 更新机器人的位置
                     * */
                    if(Robotwhere==0||Robotwhere==50)progressBar.setCenterColor(Color.RED);
                    else progressBar.setCenterColor(Color.GREEN);
                    progressBar.setProgress(Robotwhere);

                    break;
                case 0x02:
                    /**
                     * 摄像头方向按键
                     * */
                    ptzvis.setVisibility(View.GONE);
                    break;
                case 0x03:
                    /**
                     * 更新其他警告
                     * */
                    if(robotwarnTextview.showlen!=0){
                        Warnat++;
                        if(Warnat>=robotwarnTextview.showlen) Warnat=0;
                        robotwarnTextview.setText(robotwarnTextview.showText[Warnat]);
                        alarm.clearColorFilter();
                    }else robotwarnTextview.setText("运行正常");
                    break;
                default:
                    break;
            }
        }
    };
    /**
     * UI逻辑线程
     * */
    class LogicMain implements Runnable
    {
        public int Heart=0;
        public void run()
        {
            while (true)
            {
                Heart++;//逻辑子线程心跳包
                if(Heart%10000==0) {//摄像头重新链接
                    if (login[0].loginStatus == false) {
                        Thread Hiki = new Thread() {
                            public void run() {
                                Log.d(TAG,"第一个摄像头初始化");
                                HiKiInit(0);//摄像头初始化
                            }
                        };
                        Hiki.start();
                    }
                    if (login[1].loginStatus == false) {
                        Thread Hiki = new Thread() {
                            public void run() {
                                Log.d(TAG,"第二个摄像头初始化");
                                HiKiInit(1);//摄像头初始化
                            }
                        };
                        Hiki.start();
                    }
                }
                if(Heart%2000==0){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Message message = new Message();
                            message.what = 0x03;
                            handler.sendMessage(message);
                        }
                    }).start();
                }
                //方向按键更新
                if(ptztimeout>0)
                    ptztimeout--;
                if(ptztimeout==0)
                {
                    Message message = new Message();
                    message.what = 0x02;
                    handler.sendMessage(message);
                }
                //网络更新
                if(toF4.tof4Timeout>0)//如果10000个心跳没有收到任何东西，就尝试重新链接
                    toF4.tof4Timeout--;
                if(toF4.tof4Timeout==0)
                {
                    netth = new Thread(toF4.net);//prevent connect failed
                    netth.start();
                    Log.d(TAG,"F4重新链接");
                    RobotWarn.WarnFlag[RobotWarn.Warn.WIFIWarn.ordinal()]=true;
                    toF4.tof4Timeout=10000;
                }
                //串口更新
                if(toF1.tof1Timeout>0)//如果10000个心跳没有收到任何东西，就尝试重新链接
                    toF1.tof1Timeout--;
                if(toF1.tof1Timeout==0)
                {
                    Log.d(TAG,"F1重新链接");
                    RobotWarn.WarnFlag[RobotWarn.Warn.LORAWarn.ordinal()]=true;
                    toF1.tof1Timeout=10000;
                }
                if(f4toremote.havedata==true) {//F4有数据，更新
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Message message = new Message();
                            message.what = 0x00;
                            handler.sendMessage(message);
                        }
                    }).start();
                    f4toremote.havedata=false;
                }
                //更新机器人位置的Progressbar
                if(RobotStatus.infof4.totaldistance/100<=50&& RobotStatus.infof4.totaldistance/100>=0)
                Robotwhere=(RobotStatus.infof4.totaldistance/100)%51;
                else if(RobotStatus.infof4.totaldistance/100>50&& RobotStatus.infof4.totaldistance/100<100) Robotwhere=50;
                else if(RobotStatus.infof4.totaldistance/100>1000) Robotwhere=0;
                //REFRESH PROGRESSBAR
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.what = 0x01;
                        handler.sendMessage(message);
                    }
                }).start();
            }
        }
    }
    /**
     * 设置APP风格
     * */
    public void setStyle() {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        int flags;
        int curApiVersion = android.os.Build.VERSION.SDK_INT;
        if(curApiVersion >= Build.VERSION_CODES.KITKAT){
            flags = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }else{
            // touch the screen, the navigation bar will show
            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }
    /**
     * 初始化两个摄像头
     **/
    public void HiKiInit(int index){
        if(index==0) {
            vid[0] = new Videoinfo("192.168.1.64", 8000, "admin", "iris2020");
            login[0] = new HIKILogin();
            login[0].CameraLogin(vid[0]);
            surface1.startPreview(login[0].getLoginID(), login[0].getStartChannel());
            surface1.login = login[0];
            audio[0] = new Audio(login[0]);
        }else {
            vid[1] = new Videoinfo("192.168.1.65", 8000, "admin", "root1234");
            login[1] = new HIKILogin();
            login[1].CameraLogin(vid[1]);
            surface2.startPreview(login[1].getLoginID(), login[1].getStartChannel());
            surface2.login = login[1];
            audio[1] = new Audio(login[1]);
        }
    }
    /**
     * 初始化其他硬件
     * */
    public void HardInit() {
        netCol = new NetCol("192.168.1.42", 8081);
        netth = new Thread(netCol);//prevent connect failed
        netth.start();
        remotetof4=new toF4(netCol, toF4.dir.REMOTE2F4);
        f4toremote=new toF4(netCol,toF4.dir.F42REMOTE);
        remotetof4th=new Thread(remotetof4);//thread to write
        f4toremoteth=new Thread(f4toremote);//thread to read
        remotetof4th.start();
        f4toremoteth.start();
        robotWarn =new RobotWarn();
        robotwarnTextviewth.start();
    }
    /**
     * 活动初始化
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setStyle();
        setContentView(R.layout.activity_main);
        MethodUtils.initHCNetSDK();
        mDetector1 = new GestureDetector(this, new MyGestureListener());
        mDetector2 = new GestureDetector(this, new MyGestureListener());
        surface1         =(PlaySurfaceview)  findViewById(R.id.surface1);
        surface2         =(PlaySurfaceview)  findViewById(R.id.surface2);
        gas              =(TextView)         findViewById(R.id.Gas);
        speed            =(TextView)         findViewById(R.id.speed);
        status           =(TextView)         findViewById(R.id.status);
        robotwarnTextview=(RobotwarnTextview)findViewById(R.id.Alter);
        progressBar      =(RobotProgressBar) findViewById(R.id.progressBar);
        batteryView      =(BatteryView)      findViewById(R.id.power);
        Smoke            =(ImageView)        findViewById(R.id.smoke);
        alarm            =(ImageView)        findViewById(R.id.alarm);
        dir              =(ImageView)        findViewById(R.id.Dir);
        pcconncet        =(ImageView)        findViewById(R.id.PCconnect);
        leftButton       =(Button)           findViewById(R.id.btn_PTZ_left);
        rightButton      =(Button)           findViewById(R.id.btn_PTZ_right);
        upButton         =(Button)           findViewById(R.id.btn_PTZ_up);
        downButton       =(Button)           findViewById(R.id.btn_PTZ_down);
        ptzvis           =(RelativeLayout)   findViewById(R.id.dirlay);
        b_smoke          =(Bitmap)           BitmapFactory.decodeResource(getResources(), R.drawable.smoke);
        b_alarm          =(Bitmap)           BitmapFactory.decodeResource(getResources(), R.drawable.alarm);
        b_dir            =(Bitmap)           BitmapFactory.decodeResource(getResources(), R.drawable.right);
        b_pcconnect      =(Bitmap)           BitmapFactory.decodeResource(getResources(), R.drawable.networking);
        b_stop           =(Bitmap)           BitmapFactory.decodeResource(getResources(), R.drawable.stop);
        ptzvis.setVisibility(View.GONE);
        robotwarnTextviewth=new Thread(robotwarnTextview);
        Smoke.setImageBitmap(b_smoke);
        alarm.setImageBitmap(b_alarm);
        dir.setImageBitmap(b_stop);
        pcconncet.setImageBitmap(b_pcconnect);
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0); // 设置饱和度
        grayColorFilter = new ColorMatrixColorFilter(cm);
        Smoke.setColorFilter(grayColorFilter);
        alarm.setColorFilter(grayColorFilter);

        leftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                HIKILogin log;
                if(ptzsurface==1)log=login[0];
                else log=login[1];
                try {
                    if (log.getLoginID() < 0) {
                        Log.e(TAG, "please login on a device first");
                        return false;
                    }
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                log.getLoginID(), log.getStartChannel(), PTZCommand.PAN_LEFT, 0)) {
                        }
                    }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                        if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                log.getLoginID(), log.getStartChannel(), PTZCommand.PAN_RIGHT, 1)) {
                        }
                    }
                    ptztimeout=10000;
                    return true;
                }catch (Exception err) {
                    Log.e(TAG, "error: " + err.toString());
                    return false;
                }
            }
        });

        rightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                HIKILogin log;
                if(ptzsurface==1)log=login[0];
                else log=login[1];
                try {
                    if (log.getLoginID() < 0) {
                        Log.e(TAG, "please login on a device first");
                        return false;
                    }
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                log.getLoginID(), log.getStartChannel(), PTZCommand.PAN_RIGHT, 0)) {
                        }
                    }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                        if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                log.getLoginID(), log.getStartChannel(), PTZCommand.PAN_LEFT, 1)) {
                        }
                    }
                    ptztimeout=10000;
                    return true;
                }catch (Exception err) {
                    Log.e(TAG, "error: " + err.toString());
                    return false;
                }
            }
        });

        upButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                HIKILogin log;
                if(ptzsurface==1)log=login[0];
                else log=login[1];
                try {
                    if (log.getLoginID() < 0) {
                        Log.e(TAG, "please login on a device first");
                        return false;
                    }
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                log.getLoginID(), log.getStartChannel(), PTZCommand.TILT_UP, 0)) {
                        }
                    }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                        if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                log.getLoginID(), log.getStartChannel(), PTZCommand.TILT_DOWN, 1)) {
                        }
                    }
                    ptztimeout=10000;
                    return true;
                }catch (Exception err) {
                    Log.e(TAG, "error: " + err.toString());
                    return false;
                }
            }
        });

        downButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                HIKILogin log;
                if(ptzsurface==1)log=login[0];
                else log=login[1];
                try {
                    if (log.getLoginID() < 0) {
                        Log.e(TAG, "please login on a device first");
                        return false;
                    }
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                log.getLoginID(), log.getStartChannel(), PTZCommand.TILT_DOWN, 0)) {
                        }
                    }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                        if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                log.getLoginID(), log.getStartChannel(), PTZCommand.TILT_UP, 1)) {
                        }
                    }
                    ptztimeout=10000;
                    return true;
                }catch (Exception err) {
                    Log.e(TAG, "error: " + err.toString());
                    return false;
                }
            }
        });

        //SurfaceView1实现双击与滑动效果
        surface1.setLongClickable(true);
        surface1.setOnTouchListener(new View.OnTouchListener() {
            double nLenStart=0,nLenEnd=0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int pCount = event.getPointerCount();// 触摸设备时手指的数量
                int action = event.getAction();// 获取触屏动作。比如：按下、移动和抬起等手势动作 // 手势按下且屏幕上是两个手指数量时
                if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN
                        && pCount == 2) { // 获取按下时候两个坐标的x轴的水平距离，取绝对值
                    int xLen = Math.abs((int) event.getX(0) - (int) event.getX(1)); // 获取按下时候两个坐标的y轴的水平距离，取绝对值
                    int yLen = Math.abs((int) event.getY(0) - (int) event.getY(1)); // 根据x轴和y轴的水平距离，求平方和后再开方获取两个点之间的直线距离。此时就获取到了两个手指刚按下时的直线距离
                    nLenStart = Math.sqrt((double) xLen * xLen + (double) yLen * yLen);
                } else if((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE
                        && pCount == 2) {
                    int xLen = Math.abs((int) event.getX(0) - (int) event.getX(1)); // 获取抬起时候两个坐标的y轴的水平距离，取绝对值
                    int yLen = Math.abs((int) event.getY(0) - (int) event.getY(1)); // 根据x轴和y轴的水平距离，求平方和后再开方获取两个点之间的直线距离。此时就获取到了两个手指抬起时的直线距离
                    nLenEnd = Math.sqrt((double) xLen * xLen + (double) yLen * yLen); // 根据手势按下时两个手指触点之间的直线距离A和手势抬起时两个手指触点之间的直线距离B。比较A和B的大小，得出用户是手势放大还是手势缩小
                    HIKILogin log;
                    log=login[0];
                    if (nLenEnd > nLenStart) {
                        try {
                            if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                    log.getLoginID(), log.getStartChannel(), PTZCommand.ZOOM_IN, 0)) {
                            }
                        }catch (Exception err) {
                            Log.e(TAG, "error: " + err.toString());
                        }
                    } else if (nLenEnd < nLenStart) {
                        try {
                            if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                    log.getLoginID(), log.getStartChannel(), PTZCommand.ZOOM_OUT, 0)) {
                            }
                        }catch (Exception err) {
                            Log.e(TAG, "error: " + err.toString());
                        }
                    }
                }
                else if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP
                        && pCount == 2) {// 手势抬起且屏幕上是两个手指数量时 // 获取抬起时候两个坐标的x轴的水平距离，取绝对值
                    int xLen = Math.abs((int) event.getX(0) - (int) event.getX(1)); // 获取抬起时候两个坐标的y轴的水平距离，取绝对值
                    int yLen = Math.abs((int) event.getY(0) - (int) event.getY(1)); // 根据x轴和y轴的水平距离，求平方和后再开方获取两个点之间的直线距离。此时就获取到了两个手指抬起时的直线距离
                    nLenEnd = Math.sqrt((double) xLen * xLen + (double) yLen * yLen); // 根据手势按下时两个手指触点之间的直线距离A和手势抬起时两个手指触点之间的直线距离B。比较A和B的大小，得出用户是手势放大还是手势缩小
                    HIKILogin log;
                    log=login[0];
                    if (nLenEnd > nLenStart) {
                        try {
                            if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                    log.getLoginID(), log.getStartChannel(), PTZCommand.ZOOM_OUT, 1)) {
                            }
                        }catch (Exception err) {
                            Log.e(TAG, "error: " + err.toString());
                        }
                    } else if (nLenEnd < nLenStart) {
                        try {
                            if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                    log.getLoginID(), log.getStartChannel(), PTZCommand.ZOOM_IN, 1)) {
                            }
                        }catch (Exception err) {
                            Log.e(TAG, "error: " + err.toString());
                        }
                    }
                }
                isSurface1 = true;
                mDetector1.onTouchEvent(event);
                isSurface1 = false;
                return true;
            }
        });

        //SurfaceView2实现双击与滑动效果
        surface2.setLongClickable(true);
        surface2.setOnTouchListener(new View.OnTouchListener() {
            double nLenStart=0,nLenEnd=0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int pCount = event.getPointerCount();// 触摸设备时手指的数量
                int action = event.getAction();// 获取触屏动作。比如：按下、移动和抬起等手势动作 // 手势按下且屏幕上是两个手指数量时
                if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN
                        && pCount == 2) { // 获取按下时候两个坐标的x轴的水平距离，取绝对值
                    int xLen = Math.abs((int) event.getX(0) - (int) event.getX(1)); // 获取按下时候两个坐标的y轴的水平距离，取绝对值
                    int yLen = Math.abs((int) event.getY(0) - (int) event.getY(1)); // 根据x轴和y轴的水平距离，求平方和后再开方获取两个点之间的直线距离。此时就获取到了两个手指刚按下时的直线距离
                    nLenStart = Math.sqrt((double) xLen * xLen + (double) yLen * yLen);
                } else if((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE
                        && pCount == 2) {
                    int xLen = Math.abs((int) event.getX(0) - (int) event.getX(1)); // 获取抬起时候两个坐标的y轴的水平距离，取绝对值
                    int yLen = Math.abs((int) event.getY(0) - (int) event.getY(1)); // 根据x轴和y轴的水平距离，求平方和后再开方获取两个点之间的直线距离。此时就获取到了两个手指抬起时的直线距离
                    nLenEnd = Math.sqrt((double) xLen * xLen + (double) yLen * yLen); // 根据手势按下时两个手指触点之间的直线距离A和手势抬起时两个手指触点之间的直线距离B。比较A和B的大小，得出用户是手势放大还是手势缩小
                    HIKILogin log;
                    log=login[1];
                    if (nLenEnd > nLenStart) {
                        try {
                            if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                    log.getLoginID(), log.getStartChannel(), PTZCommand.ZOOM_IN, 0)) {
                            }
                        }catch (Exception err) {
                            Log.e(TAG, "error: " + err.toString());
                        }
                    } else if (nLenEnd < nLenStart) {
                        try {
                            if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                    log.getLoginID(), log.getStartChannel(), PTZCommand.ZOOM_OUT, 0)) {
                            }
                        }catch (Exception err) {
                            Log.e(TAG, "error: " + err.toString());
                        }
                    }
                }
                else if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP
                        && pCount == 2) {// 手势抬起且屏幕上是两个手指数量时 // 获取抬起时候两个坐标的x轴的水平距离，取绝对值
                    int xLen = Math.abs((int) event.getX(0) - (int) event.getX(1)); // 获取抬起时候两个坐标的y轴的水平距离，取绝对值
                    int yLen = Math.abs((int) event.getY(0) - (int) event.getY(1)); // 根据x轴和y轴的水平距离，求平方和后再开方获取两个点之间的直线距离。此时就获取到了两个手指抬起时的直线距离
                    nLenEnd = Math.sqrt((double) xLen * xLen + (double) yLen * yLen); // 根据手势按下时两个手指触点之间的直线距离A和手势抬起时两个手指触点之间的直线距离B。比较A和B的大小，得出用户是手势放大还是手势缩小
                    HIKILogin log;
                    log=login[1];
                    if (nLenEnd > nLenStart) {
                        try {
                            if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                    log.getLoginID(), log.getStartChannel(), PTZCommand.ZOOM_OUT, 1)) {
                            }
                        }catch (Exception err) {
                            Log.e(TAG, "error: " + err.toString());
                        }
                    } else if (nLenEnd < nLenStart) {
                        try {
                            if (!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(
                                    log.getLoginID(), log.getStartChannel(), PTZCommand.ZOOM_IN, 1)) {
                            }
                        }catch (Exception err) {
                            Log.e(TAG, "error: " + err.toString());
                        }
                    }
                }
                isSurface2 = true;
                mDetector2.onTouchEvent(event);
                isSurface2 = false;
                return true;
            }
        });


        initThread=new Thread()
        {
            public void run() {
                HiKiInit(0);//摄像头初始化
                HiKiInit(1);
                HardInit();
                logicmain=new LogicMain();
                logicth= new Thread(logicmain);
                logicth.start();
            }
        };
        initThread.start();
    }
    //手势处理监听器
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //移动速度大于1000时判定为滑动
            if (velocityX < -1000 && isSurface1) {
                surface2.setVisibility(View.VISIBLE);
            } else if (velocityX > 1000 && isSurface2) {
                surface1.setVisibility(View.VISIBLE);
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //实现surfaceview的单独显示
            if (isSurface1) {
                surface2.setVisibility(View.GONE);
            } else if (isSurface2) {
                surface1.setVisibility(View.GONE);
            }
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            ptzvis.setVisibility(View.VISIBLE);
            if (isSurface1) {
                ptzsurface=1;
                ptztimeout=10000;
            } else if (isSurface2) {
                ptzsurface=2;
                ptztimeout=10000;
            }
            return super.onSingleTapUp(e);
        }
    }
}
