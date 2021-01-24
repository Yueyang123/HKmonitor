package com.example.robotremote.ui;
import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;
import com.example.robotremote.Comm.Warn.RobotWarn;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  带有警告的源代码
 * @modificationHistory
 */
public class RobotwarnTextview extends AppCompatTextView implements Runnable{
        private String[] warnText = {
                "LORA失联",
                "WIFI失联",
                "UDP失联",
                "TCP失联",
                "烟雾告警",
                "瓦斯告警",
                "姿态告警",
                "距离告警",
                "电机告警",
                "里程机告警",
                "工控机告警"
        };
        public String[] showText=new String[11];//需要显示的字符串
        public int showlen=0;
        public RobotWarn robotWarn=MainActivity.robotWarn;
        public RobotwarnTextview(Context context) { super(context); }
        public RobotwarnTextview(Context context,AttributeSet attrs) {
            super(context, attrs);
        }
        public void run()
        {
                while (true){
                        showlen=0;
                        for (int i = 0; i < robotWarn.WarnFlag.length; i++) {
                                if (robotWarn.WarnFlag[i] == true)//存在相应警告
                                        {
                                                showText[showlen] = warnText[i];
                                                showlen++;
                                        }
                                }
                        try {
                                Thread.sleep(1000);
                        }catch (InterruptedException e)
                        {
                                e.printStackTrace();
                        }
                }
        }

}
