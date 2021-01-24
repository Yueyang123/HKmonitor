
#海康威视 SDK
HCNetSDK
## NET_DVR_DEVICEINFO_V30: 设备信息
In class com.hikvision.netsdk.HCNetSDK
public class NET_DVR_DEVICEINFO_V30
{
    public byte[] sSerialNumber = new byte[SERIALNO_LEN];
    public byte byAlarmInPortNum;
    public byte byAlarmOutPortNum;
    public byte byDiskNum;
    public byte byDVRType;
    public byte byChanNum;
    public byte byStartChan;
    public byte byAudioChanNum;
    public byte byIPChanNum;
    public byte byZeroChanNum;
    public short  wDevType;
    public byte byStartDChan
    public byte byHighDChanNum
}

## 注册接收异常、重连消息回调函数 NET_DVR_SetExceptionCallBack
函 数：  public boolean NET_DVR_SetExceptionCallBack(ExceptionCallBack CallBack)
参 数：  [in] CallBack  接收异常消息的回调函数，回调当前异常的相关信息
    public interface ExceptionCallBack {
    public void fExceptionCallBack(int iType, int iUserID, int iHandle);
    }
    [out] iType
    [out] iUserID
    [out] iHandle
异常或重连等消息的类型，详见表 3.1
登录 ID
出现异常的相应类型的句柄
表 3.1 异常消息类型
dwType  宏定义  宏定义值  含义
    EXCEPTION_EXCHANGE  0x8000  用户交互时异常（注册心跳超时，心跳间隔为 2 分钟）
    EXCEPTION_AUDIOEXCHANGE  0x8001  语音对讲异常
    EXCEPTION_ALARM  0x8002  报警异常
    EXCEPTION_PREVIEW  0x8003  网络预览异常
    EXCEPTION_SERIAL  0x8004  透明通道异常
    EXCEPTION_RECONNECT  0x8005  预览时重连
    EXCEPTION_ALARMRECONNECT  0x8006  报警时重连
    EXCEPTION_SERIALRECONNECT  0x8007  透明通道重连
    SERIAL_RECONNECTSUCCESS  0x8008  透明通道重连成功
    EXCEPTION_PLAYBACK  0x8010  回放异常
    EXCEPTION_DISKFMT  0x8011  硬盘格式化
    EXCEPTION_EMAILTEST  0x8013  邮件测试异常
    EXCEPTION_BACKUP  0x8014  备份异常
    PREVIEW_RECONNECTSUCCESS  0x8015  预览时重连成功
    ALARM_RECONNECTSUCCESS  0x8016  报警时重连成功
    RESUME_EXCHANGE  0x8017  用户交互恢复
返回值：  TRUE 表示成功，FALSE 表示失败。接口返回失败请调用 NET_DVR_GetLastError 获取错误码，通
过错误码判断出错原因。
说 明：  In class com.hikvision.netsdk.HCNetSDK ，JNI



## 用户注册设备 NET_DVR_Login_V30
函 数：  public int NET_DVR_Login_V30(String sDvrIp, int iDvrPort, ava.lang.String sUserName, String
sPassword, NET_DVR_DEVICEINFO_V30 DeviceInfo)
    参 数：  [in] sDvrIp
    [in] iDvrPort
    [in] sUserName
    [in] sPassword
    [out] DeviceInfo
设备 IP 地址或静态域名
设备端口号
登录的用户名
用户密码
设备信息，详见：NET_DVR_DEVICEINFO_V30
返回值：  -1 表示失败，其他值表示返回的用户 ID 值。该用户 ID 具有唯一性，后续对设备的操作都需要通
过此 ID 实现。接口返回失败请调用 NET_DVR_GetLastError 获取错误码，通过错误码判断出错原
因。
说 明：  In class com.hikvision.netsdk.HCNetSDK ，JNI  接口。SDK 注册设备新增支持静态域名的方式，即
可设置 sDVRIP="test.vicp.net"。

## 用户注销 NET_DVR_Logout_V30
函 数：  public boolean NET_DVR_Logout_V30 (int lUserID)
参 数：  [in]lUserID  用户 ID 号，NET_DVR_Login_V30 的返回值
返回值：  TRUE 表示成功，FALSE 表示失败。接口返回失败请调用 NET_DVR_GetLastError 获取错误码，通
过错误码判断出错原因。
说 明：  In class com.hikvision.netsdk.HCNetSDK ，JNI  接口。




## NET_DVR_PREVIEWINFO: 预览参数
In class com.hikvision.netsdk.HCNetSDK
public class NET_DVR_PREVIEWINFO
{
    public int lChannel;
    public int dwStreamType;
    public int dwLinkMode;
    public int bBlocked;
    public int bPassbackRecord;
    public byte byPreviewMode;
    public byte byProtoType;
    public SurfaceHolder hHwnd;
}
Members
lChannel
通道号，目前设备模拟通道号从 1 开始，数字通道的起始通道号一般从 33 开始，具体取值在登录接口
返回
dwStreamType
码流类型：0-主码流，1-子码流，2-码流 3，3-虚拟码流，以此类推
dwLinkMode
连接方式：0- TCP 方式，1- UDP 方式，2- 多播方式，3- RTP 方式，4-RTP/RTSP，5-RSTP/HTTP
bBlocked
0- 非阻塞取流，1- 阻塞取流
bPassbackRecord
0-不启用录像回传，1-启用录像回传。ANR 断网补录功能，客户端和设备之间网络异常恢复之后自动将
前端数据同步过来，需要设备支持。
byPreviewMode
预览模式：0- 正常预览，1- 延迟预览
byProtoType
应用层取流协议：0- 私有协议，1- RTSP 协议
hHwnd
播放窗口的句柄，为 NULL 表示不解码显示


实时预览 NET_DVR_RealPlay_V40
函 数：  public int NET_DVR_RealPlay_V40(int lUserID, NET_DVR_PREVIEWINFO previewInfo, RealPlayCallBack
CallBack)
参 数：  [in] lUserID
[in] previewInfo
[in] CallBack
NET_DVR_Login_V30 的返回值
预览参数，包括码流类型、取流协议、通道号等，详见：
NET_DVR_PREVIEWINFO
码流数据回调函数
public interface RealPlayCallBack {
public void fRealDataCallBack(int iRealHandle, int iDataType, byte[] pDataBuffer, int iDataSize);
}
[out] iRealHandle
[out] iDataType
[out] pDataBuffer
[out] iDataSize
当前的预览句柄
数据类型
存放数据的缓冲区指针
缓冲区大小
表 3.4 码流数据类型
dwDataType  宏定义  宏定义值  含义
NET_DVR_SYSHEAD  1  系统头数据
NET_DVR_STREAMDATA  2  流数据（包括复合流或音视频分开的视频流数据）
NET_DVR_AUDIOSTREAMDATA  3  音频数据
返回值：  -1 表示失败，其他值作为 NET_DVR_StopRealPlay 等函数的句柄参数。接口返回失败请调用
NET_DVR_GetLastError 获取错误码，通过错误码判断出错原因。
说 明：  In class com.hikvision.netsdk.HCNetSDK ，JNI  接口。通过该接口设置实时流回调函数获取实时流
音视频数据，然后可以通过播放库进行解码显示。



//# PLAYER
## 设置流播放模式 setStreamOpenMode
函 数：
boolean setStreamOpenMode(int nPort, int nMode)
参 数： Int nPort
unsigned int nMode
STREAME_REALTIME
STREAME_FILE
播放通道号
流播放模式：STREAME_REALTIME、STREAME_FILE
此模式（默认）下, 会尽量保正实时性, 防止数据阻塞，而且数据
检查严格。适合播放网络实时数据，立刻解码
文件模式，适合用户将文件以流方式输入，按时间戳播放
返回值： 成功返回 true；失败返回 false
说 明：
若码流为 RTP 封装的，则需要注意正确送入 RTP 的文件头，以及数据包大小和
内容。
设置流播放模式，其中若设置为 STREAM_REALTIME 模式则表示尽量保证实时
性，防止数据阻塞；且数据检查严格；
设置为 STREAM_FILE 则表示按时间戳播放。
注 意：
STREAME_REALTIME 模式下 fast、slow 调用无效且不返回错误码。



函 数： boolean play(int nPort, SurfaceHolder holder)
参 数： int nPort
SurfaceHolder holder
播放通道号
显示窗口
返回值： 成功返回 true；失败返回 false
说 明：
播放开始，播放视频画面大小将根据 holder 窗口调整，要全屏显示，只要把 holder
窗口放大到全屏。在 play 之前需要 holder 创建成功。
如果已经播放，重置当前播放速度为正常速度。
注 意：
I 帧大于 2MB 时可能无法解析

## 进行播放的步骤
previewButton.setOnClickListener->startSinglePreview->getRealPlayerCbf->processRealData->
Player.getInstance().setStreamOpenMode(playPort,iStreamMode)
Player.getInstance().openStream(playPort, pDataBuffer,iDataSize, 2 * 1024 * 1024)
play(playPort,surface.getHolder()))
Player.getInstance().playSound(playPort)
Player.getInstance().playSound(playPort)
