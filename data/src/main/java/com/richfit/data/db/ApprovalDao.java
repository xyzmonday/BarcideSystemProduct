package com.richfit.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.ImageEntity;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;


/**
 * 验收模块的Dao层
 * Created by monday on 2016/12/13.
 */

public class ApprovalDao extends BaseDao {

    @Inject
    public ApprovalDao(@ContextLife("Application") Context context) {
        super(context);
    }

    /**
     * 删除验收整单的缓存图片
     *
     * @param refNum
     * @param isLocal
     * @return
     */
    @Override
    public void deleteInspectionImages(String refNum, String refCodeId, boolean isLocal) {
        SQLiteDatabase db = mSqliteHelper.getReadableDatabase();
        db.delete("MTL_IMAGES", "ref_num = ? and local_flag = ?", new String[]{refNum, isLocal ? "Y" : "N"});
        db.close();
    }

    /**
     * 删除整行图片
     *
     * @param refNum：参考单据
     * @param refLineId：明细行id
     * @param refLineNum：行号
     * @param isLocal：是否是离线模式
     * @return
     */
    public void deleteInspectionImagesSingle(String refNum, String refLineNum, String refLineId, boolean isLocal) {

        if (TextUtils.isEmpty(refLineId)) {
            return;
        }
        SQLiteDatabase db = null;
        try {
            db = mSqliteHelper.getReadableDatabase();
            db.delete("MTL_IMAGES", "ref_line_id = ? and local_flag = ?", new String[]{refLineId, isLocal ? "Y" : "N"});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * 在拍照界面，用户选择需要删除的图片集合，然后进行删除。
     *
     * @param images：需要删除的图片集合
     * @return
     */
    @Override
    public Flowable<String> deleteTakedImages(ArrayList<ImageEntity> images, boolean isLocal) {
        return Flowable.create(emitter -> {
            if(deleteTakedImagesInternal(images, isLocal)) {
                emitter.onError(new Throwable("删除图片失败"));
                return;
            }
            emitter.onNext("删除本地缓存图片成功");
            emitter.onComplete();
        }, BackpressureStrategy.LATEST);

    }

    private boolean deleteTakedImagesInternal(ArrayList<ImageEntity> images, boolean isLocal) {
        SQLiteDatabase db = mSqliteHelper.getReadableDatabase();
        int row = -1;
        try {
            for (ImageEntity image : images) {
                if (!image.isSelected)
                    continue;
                row = db.delete("MTL_IMAGES", "image_name = ? and local_flag = ?", new String[]{image.imageName,
                        isLocal ? "Y" : "N"});
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
        return row > 0 ? true : false;
    }


    /**
     * 保存拍照获取的照片信息
     *
     * @param images：照片数据源
     * @param refNum：单据号
     * @param refLineId：行id
     * @param takePhotoType：拍照类型
     * @param imageDir：sd卡缓存的照片目录
     * @param isLocal：离线或者在线模式
     */
    public void saveTakedImages(ArrayList<ImageEntity> images, String refNum, String refLineId,
                                int takePhotoType, String imageDir, boolean isLocal) {
        SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
        for (ImageEntity image : images) {
            db.delete("MTL_IMAGES", "ref_num = ? and local_flag = ? and image_name = ?",
                    new String[]{refNum, isLocal ? "Y" : "N", image.imageName});
            ContentValues cv = new ContentValues();
            String id = UiUtil.getUUID();
            image.id = id;
            cv.put("id", id);
            cv.put("ref_line_id", refLineId);
            cv.put("ref_num", refNum);
            cv.put("image_dir", imageDir);
            cv.put("image_name", image.imageName);
            cv.put("created_by", Global.USER_ID);
            cv.put("local_flag", isLocal ? "Y" : "N");
            cv.put("take_photo_type", takePhotoType);
            cv.put("biz_type", image.bizType);
            cv.put("ref_type", image.refType);
            cv.put("creation_date", UiUtil.transferLongToDate("yyyyMMddHHmmss", image.lastModifiedTime));
            db.insert("MTL_IMAGES", null, cv);
        }
        db.close();
    }

    /**
     * 读取整单缓存图片
     *
     * @param refNum
     * @param isLocal
     */
    public ArrayList<ImageEntity> readImagesByRefNum(String refNum, boolean isLocal) {
        ArrayList<ImageEntity> images = new ArrayList<>();
        SQLiteDatabase db = mSqliteHelper.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("select ref_line_id,image_dir,image_name,created_by,creation_date,take_photo_type,biz_type,ref_type from MTL_IMAGES where ref_num = ? and local_flag = ?",
                new String[]{refNum, isLocal ? "Y" : "N"});
        while (cursor.moveToNext()) {
            ImageEntity image = new ImageEntity();
            image.refLineId = cursor.getString(0);
            image.imageDir = cursor.getString(1);
            image.imageName = cursor.getString(2);
            image.createBy = cursor.getString(3);
            image.createDate = cursor.getString(4);
            image.takePhotoType = cursor.getInt(5);
            image.bizType = cursor.getString(6);
            image.refType = cursor.getString(7);
            images.add(image);
        }
        cursor.close();
        db.close();
        return images;
    }
}
