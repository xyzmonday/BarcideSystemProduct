package com.richfit.barcodesystemproduct.crash;

import java.io.File;

/**
 * 将崩溃日志上传到服务器
 * Created by monday on 2016/4/8.
 */
public class HttpCrashReport extends BaseCrashReport {

    public HttpCrashReport() {
    }


    @Override
    public void sendLogFileToTarget(Thread thread, Throwable ex,final File logFile) {

    }

    /**
     * 删除崩溃日志文件
     * @param file
     */
    private void deleteLogFile(final File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
