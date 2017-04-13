package com.richfit.common_lib.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBUtils {

    // 导入到sd卡中
    // private static String db_path =
    // android.os.Environment.getExternalStorageDirectory().getAbsolutePath()
    // + "/yourpath/";

    private Context myContext;

    /**
     * 如果数据库文件较大，使用FileSplit分割为小于1M的小文件
     * 本例分割为test_db.001,test_db.002,test_db.003........
     */
    /**
     * 第一个文件名后缀
     */
    public static final int ASSETS_SUFFIX_BEGIN = 1;

    /**
     * 最后一个文件名后缀
     */
    private static final int ASSETS_SUFFIX_END = 3;

    // 数据库文件目标存放路径
    private String db_path;

    // 源db文件
    private String resource_db;

    // 复制到这个数据库
    private String db_name;

    /**
     * @param myContext
     * @param resource_db 源db文件
     * @param db_name     复制到这个数据库
     * @param db_path     将数据库文件复制到这个路径
     */
    public DBUtils(Context myContext, String resource_db, String db_name,
                   String db_path) {
        this.myContext = myContext;
        this.db_path = db_path;
        this.resource_db = resource_db;
        this.db_name = db_name;
    }

    /**
     * 复制数据库
     *
     * @throws IOException
     */
    public void createDataBase() {
        // 判断目标数据库是否已存在
        File dbf = new File(db_path + db_name);
        if (dbf.exists()) {
            return;
        }
        // 创建数据库
        try {
            File dir = new File(db_path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            dbf.deleteOnExit();
            SQLiteDatabase.openOrCreateDatabase(dbf, null);
            // 复制asseets中的db文件到db_path
            // 如果源db文件小于1M，可直接使用该方法
            // copyDataBase();
            // 如果超过1M使用该方法
            copyBigDataBase();
        } catch (IOException e) {
            throw new Error("Database creation failed");
        }
    }

    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transfering bytestream.
     */
    public void copyDataBase() throws IOException {
        // Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(resource_db);
        // Path to the just created empty db
        String outFileName = db_path + db_name;
        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    /**
     * 复制assets下的大数据库文件时用这个(源文件db大小超过1M)
     *
     * @throws IOException
     */
    public void copyBigDataBase() throws IOException {
        InputStream myInput;
        String outFileName = db_path + db_name;
        OutputStream myOutput = new FileOutputStream(outFileName);
        for (int i = ASSETS_SUFFIX_BEGIN; i < ASSETS_SUFFIX_END + 1; i++) {
            myInput = myContext.getAssets().open(resource_db + "." + "00" + i);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myInput.close();
        }
        myOutput.close();
    }

}
