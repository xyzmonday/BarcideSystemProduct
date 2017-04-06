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
import com.richfit.domain.bean.ResultEntity;
import com.richfit.domain.repository.IInspectionServiceDao;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Created by monday on 2017/3/29.
 */

public class InspectionServiceDao extends BaseDao implements IInspectionServiceDao {

    @Inject
    public InspectionServiceDao(@ContextLife("Application") Context context) {
        super(context);
    }

    @Override
    public void deleteInspectionImages(String refNum, String refCodeId, boolean isLocal) {
        SQLiteDatabase db = getWritableDB();
        db.delete("MTL_IMAGES", "ref_num = ? and local_flag = ?", new String[]{refNum, isLocal ? "Y" : "N"});
        db.close();
    }

    @Override
    public void deleteInspectionImagesSingle(String refNum, String refLineNum, String refLineId, boolean isLocal) {
        if (TextUtils.isEmpty(refLineId)) {
            return;
        }
        SQLiteDatabase db = null;
        try {
            db = getWritableDB();
            db.delete("MTL_IMAGES", "ref_line_id = ? and local_flag = ?", new String[]{refLineId, isLocal ? "Y" : "N"});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    @Override
    public boolean deleteTakedImages(ArrayList<ImageEntity> images, boolean isLocal) {
        SQLiteDatabase db = getWritableDB();
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

    @Override
    public void saveTakedImages(ArrayList<ImageEntity> images, String refNum, String refLineId, int takePhotoType, String imageDir, boolean isLocal) {
        SQLiteDatabase db = getWritableDB();
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

    @Override
    public boolean deleteInspectionByHeadId(String refCodeId) {
        return false;
    }

    @Override
    public boolean deleteInspectionByLineId(String refLineId) {
        return false;
    }

    @Override
    public ArrayList<ImageEntity> readImagesByRefNum(String refNum, boolean isLocal) {
        ArrayList<ImageEntity> images = new ArrayList<>();
        SQLiteDatabase db = getWritableDB();
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

    @Override
    public boolean uploadInspectionDataSingle(ResultEntity result) {
        return false;
    }
}
