package com.richfit.barcodesystemproduct.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.motorolasolutions.adc.decoder.BarCodeReader;
import com.richfit.barcodesystemproduct.BuildConfig;
import com.richfit.common_lib.IInterface.IPresenter;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.L;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Semaphore;

/**
 * 兼容so和服务两种条码扫描
 * Created by monday on 2017/5/23.
 */

public abstract class BaseBarScannerActivity<T extends IPresenter> extends BaseActivity<T>
        implements BarCodeReader.DecodeCallback {

    //扫描模块
    static {
        System.loadLibrary("IAL");
        System.loadLibrary("SDL");
        System.loadLibrary("barcodereader44");
    }


    private static final String HEXSTRING = "0123456789ABCDEF";
    //使用服务作为扫描条码
    private static boolean isServiceDL = false;
    private boolean isStartScan = false;

    //大庆手持的扫码广播
    private static final String DQ_RECE_DATA_ACTION = "com.se4500.onDecodeComplete";
    private static final String DQ_START_SCAN_ACTION = "com.geomobile.se4500barcode";
    private static final String DQ_STOP_SCAN = "com.geomobile.se4500barcode.poweroff";

    //物资公司的扫码广播
    private static final String WZ_RECT_DATA_ACTION = "com.android.scancontext";
    private static final String WZ_START_SCAN_ACTION = "android.intent.action.FUNCTION_BUTTON_DOWN";
    private static final String WZ_STOP_SCAN = "android.intent.action.FUNCTION_BUTTON_UP";

    private static final int STATE_IDLE = 0;
    private static final int STATE_DECODE = 1;
    private static final int STATE_SNAPSHOT = 4;

    private boolean stateIsDecoding = false;
    private ToneGenerator tg = null;
    private static BarCodeReader bcr = null;
    private boolean beepMode = true;
    private int trigMode = BarCodeReader.ParamVal.AUTO_AIM;
    private static int state = STATE_IDLE;
    private int decodes = 0;
    private int motionEvents = 0;
    private int modechgEvents = 0;
    private static long decode_start = 0;
    private static long decode_end = 0;
    private boolean isnotdecode = true;
    private static Handler mHandler = null;
    private Semaphore mSemaphore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSemaphore = new Semaphore(1);
        isServiceDL = BuildConfig.ISSERVICEDL;
        if (isServiceDL) {
            isStartScan = false;
            IntentFilter scanDataIntentFilter = new IntentFilter();
            scanDataIntentFilter.addAction(WZ_RECT_DATA_ACTION);
            scanDataIntentFilter.addAction(DQ_RECE_DATA_ACTION);
            registerReceiver(receiver, scanDataIntentFilter);
        } else {
            tg = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
            state = STATE_IDLE;
            mHandler = new Handler();
        }
    }

    /**
     * 当Activity的UI可以与用户进行交互后(也就是当前Activity变成前台Activity)，启动条码扫描
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!isServiceDL) {
            state = STATE_IDLE;
            openBarcodeReader();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bcr != null) {
            bcr.setDecodeCallback(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isServiceDL) {
            if (bcr != null) {
                bcr.setDecodeCallback(null);
            }
            if (tg != null) {
                tg.release();
            }
        } else {
            unregisterReceiver(receiver);
        }
    }

    /**
     * 更具条码的类型，获取条码的信息
     * 条码类型|物料编码|批次|验收人|采购订单号
     */
    private void filerBarCodeInfo(String info) {
        if (TextUtils.isEmpty(info)) {
            return;
        }
        //仓位和单据不加密
        int length = info.split("\\|", -1).length;
        String barcodeInfo;
        if (length <= 1) {
            barcodeInfo = info;
        } else {
            switch (BuildConfig.APP_NAME) {
                //庆阳单独处理，而且必须保证前两个竖线没有任何值
                case Global.QINGYANG:
                    if (TextUtils.isEmpty(info)) {
                        showMessage("单据条码信息为空");
                        return;
                    }
                    String tmp[] = info.split("\\|", -1);
                    String materialNum = tmp[Global.MATERIAL_POS];
                    if (TextUtils.isEmpty(materialNum)) {
                        showMessage("获取物料条码为空");
                        return;
                    }
                    if (materialNum.length() <= 10) {
                        //说明此时扫描到的是加密的
                        barcodeInfo = CharTrans(info);
                    } else {
                        barcodeInfo = info;
                    }
                    break;
                default:
                    barcodeInfo = CharTrans(info);
                    break;
            }
        }
        if (TextUtils.isEmpty(barcodeInfo)) {
            showMessage("单据条码信息为空");
            return;
        }
        L.e("扫描条码的内容 = " + barcodeInfo);
        String a[] = barcodeInfo.split("\\|", -1);
        handleBarCodeScanResult("", a);
    }

    /**
     * 条码内容加密
     *
     * @param char_in
     * @return
     */
    public static String CharTrans(String char_in) {
        String char_out = "";
        int char_length = 0;

        char_length = char_in.length();

        int flg_mod = char_length % 2;
        for (int i = 0; i < char_length - 1; i += 2) {
            char_out = char_out + char_in.substring(i + 1, i + 2);
            char_out = char_out + char_in.substring(i, i + 1);
        }

        if (flg_mod != 0) {
            char_out = char_out + char_in.substring(char_length - 1);
        }
        return char_out;
    }


    /**
     * 将16进制数字解码成字符串,适用于所有字符（包括中文）
     */
    public static String decodeForChinese(String bytes) {
        String str = "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream(
                bytes.length() / 2);
        // 将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((HEXSTRING.indexOf(bytes.charAt(i)) << 4 | HEXSTRING
                    .indexOf(bytes.charAt(i + 1))));
        try {
            str = new String(baos.toByteArray(), "GB2312");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    private boolean isUTF8(byte[] sx) {
        for (int i = 0; i < sx.length; ) {
            if (sx[i] < 0) {
                if ((sx[i] >>> 5) == 0x7FFFFFE) {
                    if (((i + 1) < sx.length) && ((sx[i + 1] >>> 6) == 0x3FFFFFE)) {
                        i = i + 2;
                    } else {
                        return false;
                    }
                } else if ((sx[i] >>> 4) == 0xFFFFFFE) {
                    if (((i + 2) < sx.length) && ((sx[i + 1] >>> 6) == 0x3FFFFFE) && ((sx[i + 2] >>> 6) == 0x3FFFFFE)) {
                        i = i + 3;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                i++;
            }
        }
        return true;
    }

    private void beep() {
        if (tg != null)
            tg.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT);
    }


    private boolean isHandsFree() {
        return (trigMode == BarCodeReader.ParamVal.HANDSFREE);
    }

    private boolean isAutoAim() {
        return (trigMode == BarCodeReader.ParamVal.AUTO_AIM);
    }

    private void doDecode() {
        //由于是异步加载，所以主线程必须等待子线程将bcr初始化
        if (bcr == null) {
            try {
                mSemaphore.acquire();
            } catch (InterruptedException e) {
                L.e("线程同步 : " + e.getMessage());
                e.printStackTrace();
            }
            if(bcr == null) {
                //如果子线程完成后，主线程再次判断如果没有初始化再次重试
                mSemaphore.release();
                openBarcodeReader();
            }
            return;
        }
        if (setIdle() != STATE_IDLE)
            return;
        filerBarCodeInfo("");
        state = STATE_DECODE;
        isnotdecode = true;
        bcr.startDecode();
    }

    private static int setIdle() {
        int prevState = state;
        int ret = prevState;
        state = STATE_IDLE;
        switch (prevState) {
            case STATE_DECODE:
                bcr.stopDecode();
                break;
            default:
                ret = STATE_IDLE;
        }
        return ret;
    }


    @Override
    public void onDecodeComplete(int symbology, int length, byte[] data, BarCodeReader reader) {
        if (state == STATE_DECODE)
            state = STATE_IDLE;

        if (length > 0) {
            decode_end = SystemClock.elapsedRealtime();
            if (isHandsFree() == false && isAutoAim() == false)
                bcr.stopDecode();
            ++decodes;
            if (beepMode)
                beep();
            isnotdecode = false;
            if (symbology == 0x99) {
                symbology = data[0];
                int n = data[1];
                int s = 2;
                int d = 0;
                int len = 0;
                byte d99[] = new byte[data.length];
                for (int i = 0; i < n; ++i) {
                    s += 2;
                    len = data[s++];
                    System.arraycopy(data, s, d99, d, len);
                    s += len;
                    d += len;
                }
                d99[d] = 0;
                data = d99;
            } else {
                byte temp[] = new byte[length];
                System.arraycopy(data, 0, temp, 0, length);
                data = temp;
            }

            try {
                if (isUTF8(data)) {
                    String utf8str = new String(data, "utf8");
                    filerBarCodeInfo(utf8str);
                } else {
                    String gbkstr = new String(data, "gbk");
                    filerBarCodeInfo(gbkstr);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mHandler.postDelayed(mStartScan, 100);
        }
        stateIsDecoding = false;
    }

    @Override
    public void onEvent(int event, int info, byte[] data, BarCodeReader reader) {
        switch (event) {
            case BarCodeReader.BCRDR_EVENT_SCAN_MODE_CHANGED:
                ++modechgEvents;
                break;
            case BarCodeReader.BCRDR_EVENT_MOTION_DETECTED:
                ++motionEvents;
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_F5:
                responseKeyDown();
                break;
            case KeyEvent.KEYCODE_F4:
                responseKeyDown();
                break;
            case KeyEvent.KEYCODE_1:
                responseKeyDown();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 停止扫描
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_F5:
                stopScan();
                break;
            case KeyEvent.KEYCODE_F4:
                stopScan();
                break;
            case KeyEvent.KEYCODE_1:
                stopScan();
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void responseKeyDown() {
        if (!isServiceDL) {
            if (!stateIsDecoding) {
                decode_start = SystemClock.elapsedRealtime();
                doDecode();
                stateIsDecoding = true;
            }
        } else {
            if (!isStartScan) {
                isStartScan = true;
                //如果没有开启
                Intent intent = new Intent();
                intent.setAction(WZ_START_SCAN_ACTION);
                intent.setAction(DQ_START_SCAN_ACTION);
                sendBroadcast(intent, null);
            }
        }
    }

    /**
     * 停止扫描
     */
    private void stopScan() {
        isStartScan = false;
        Intent intent = new Intent();
        intent.setAction(WZ_STOP_SCAN);
        intent.setAction(DQ_STOP_SCAN);
        sendBroadcast(intent);
    }

    /**
     * 接收扫描信息的广播
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isStartScan = true;
            String action = intent.getAction();
            if (action.equals("com.android.scancontext")) {
                String data = intent.getStringExtra("Scan_context");
                filerBarCodeInfo(data);
            } else if (action.equals(WZ_RECT_DATA_ACTION)) {
                String data = intent.getStringExtra("Scan_context");
                filerBarCodeInfo(data);
            } else if (action.equals(DQ_RECE_DATA_ACTION)) {
                String data = intent.getStringExtra("se4500");
                filerBarCodeInfo(data);
            }
        }
    };


    private final static Runnable mStartScan = () -> {
        if (state == STATE_DECODE && bcr != null) {
            bcr.stopDecode();
        }
    };


    private void openBarcodeReader() {
        new Thread(() -> {
            try {
                //子线程获取许可
                mSemaphore.acquire();
                if (bcr == null) {
                    synchronized (BarCodeReader.class) {
                        if(bcr == null) {
                            bcr = BarCodeReader.open(getApplicationContext());
                        }
                        if (bcr == null) {
                            bcr.setDecodeCallback(null);
                            return;
                        }
                        bcr.setDecodeCallback(BaseBarScannerActivity.this);
                    }
                } else {
                    bcr.setDecodeCallback(null);
                    bcr.setDecodeCallback(BaseBarScannerActivity.this);
                }
                mSemaphore.release();
            } catch (Exception e) {
                L.e("获取扫描出错 = " + e.getMessage());
                if (bcr != null) {
                    bcr.setDecodeCallback(null);
                }
                bcr = null;
                mSemaphore.release();
            }
        }).start();

    }

    public static void releaseBarcodeReader() {
        if (isServiceDL)
            return;
        if (bcr != null) {
            bcr.setDecodeCallback(null);
            setIdle();
            bcr.release();
            bcr = null;
        }
    }
}
