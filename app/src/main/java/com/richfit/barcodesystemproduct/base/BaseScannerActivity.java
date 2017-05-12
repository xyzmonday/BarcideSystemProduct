package com.richfit.barcodesystemproduct.base;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.TextView;

import com.motorolasolutions.adc.decoder.BarCodeReader;
import com.richfit.barcodesystemproduct.BuildConfig;
import com.richfit.common_lib.IInterface.IPresenter;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.L;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * 如果该Activity需要扫描服务那么需要继承该Activity
 * Created by monday on 2017/5/12.
 */

public abstract class BaseScannerActivity<T extends IPresenter> extends BaseActivity<T> implements BarCodeReader.DecodeCallback {
    //扫描模块

    private static final int STATE_IDLE = 0;
    private static final int STATE_DECODE = 1;
    private static final int STATE_SNAPSHOT = 4;

    private boolean stateIsDecoding = false;
    private ToneGenerator tg = null;
    private BarCodeReader bcr = null;
    private TextView tvData = null;
    private boolean beepMode = true;
    private int trigMode = BarCodeReader.ParamVal.AUTO_AIM;
    private int state = STATE_IDLE;
    private int decodes = 0;
    private int motionEvents = 0;
    private int modechgEvents = 0;
    private static long decode_start = 0;
    private static long decode_end = 0;
    private boolean isnotdecode = true;
    private Handler mHandler = null;

    static {
        System.loadLibrary("IAL");
        System.loadLibrary("SDL");
        System.loadLibrary("barcodereader44");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tg = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
        mHandler = new Handler();
    }

    /**
     * 当Activity的UI可以与用户进行交互后(也就是当前Activity变成前台Activity)，启动条码扫描
     */
    @Override
    protected void onResume() {
        super.onResume();
        state = STATE_IDLE;
        //注意这里内部类容易发出内存泄露
        openBarcodeReader();
    }

    /**
     * 当Activity变成后台Activity释放条码扫描
     */
    @Override
    protected void onPause() {
        super.onPause();
        releaseBarcodeReader();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bcr != null) {
            setIdle();
            bcr.release();
            bcr = null;
        }
        if (tg != null)
            tg.release();
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
     * 16进制数字字符集
     */
    private static final String hexString = "0123456789ABCDEF";

    /**
     * 将16进制数字解码成字符串,适用于所有字符（包括中文）
     */
    public static String decodeForChinese(String bytes) {
        String str = "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream(
                bytes.length() / 2);
        // 将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
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
        //由于是异步加载，所以需要判断
        if (bcr == null) {
            openBarcodeReader();
            return;
        }
        if (setIdle() != STATE_IDLE)
            return;
        filerBarCodeInfo("");
        state = STATE_DECODE;
        isnotdecode = true;
        bcr.startDecode();
    }

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


    private int setIdle() {
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

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_F5:
                if (!stateIsDecoding) {
                    decode_start = SystemClock.elapsedRealtime();
                    doDecode();
                    stateIsDecoding = true;
                }
                break;
            case KeyEvent.KEYCODE_F4:
                if (!stateIsDecoding) {
                    decode_start = SystemClock.elapsedRealtime();
                    doDecode();
                    stateIsDecoding = true;
                }
                break;
            case KeyEvent.KEYCODE_1:
                if (!stateIsDecoding) {
                    decode_start = SystemClock.elapsedRealtime();
                    doDecode();
                    stateIsDecoding = true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private final Runnable mStartScan = () -> {
        if (state == STATE_DECODE && bcr != null) {
            bcr.stopDecode();
        }
    };


    private void openBarcodeReader() {
        new Thread(() -> {
            try {
                bcr = BarCodeReader.open(getApplicationContext());
                if (bcr == null) {
                    return;
                }
                bcr.setDecodeCallback(BaseScannerActivity.this);
            } catch (Exception e) {
            }
        }).start();

    }

    private void releaseBarcodeReader() {
        new Thread(() -> {
            if (bcr != null) {
                setIdle();
                bcr.release();
                bcr = null;
            }
        }).start();
    }
}
