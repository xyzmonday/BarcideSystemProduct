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

    /**
     * 删除验收的整单缓存
     *
     * @param refCodeId
     * @return
     */
    @Override
    public boolean deleteInspectionByHeadId(String refCodeId) {
        // 删除头表
        SQLiteDatabase db = getWritableDB();
        int iResult = -1;
        String id = null;
        //仅仅删除ins_flag = 1的缓存
        Cursor cursor = db.rawQuery("select id from MTL_INSPECTION_HEADERS where po_id = ? and ins_flag = ?",
                new String[]{refCodeId, "1"});
        while (cursor.moveToNext()) {
            id = cursor.getString(0);
        }
        cursor.close();
        if (TextUtils.isEmpty(id))
            return false;
        iResult = db.delete("MTL_INSPECTION_HEADERS", "id = ?", new String[]{id});
        if (iResult < 0)
            return false;

        // 删除行表
        //在删除行表之前将所有的单据行id保存起来用于删除图片
        ArrayList<String> refLineIds = new ArrayList<>();
        cursor = db.rawQuery("select po_line_id from MTL_INSPECTION_LINES where inspection_id =?",
                new String[]{id});
        while (cursor.moveToNext()) {
            refLineIds.add(cursor.getString(0));
        }
        cursor.close();
        iResult = db.delete("MTL_INSPECTION_LINES", "inspection_id = ?", new String[]{id});
        if (iResult < 0)
            return false;

        // 删除扩展表
        iResult = db.delete("MTL_INSPECTION_LINES_CUSTOM", "inspection_id = ?", new String[]{id});
        if (iResult < 0)
            return false;

        // 删除照片
        if (refLineIds.size() > 0) {
            for (String refLineId : refLineIds) {
                db.delete("MTL_IMAGES", "ref_line_id = ?", new String[]{refLineId});
            }
        }
        db.close();
        return true;
    }

    @Override
    public boolean deleteInspectionByLineId(String refLineId) {
        SQLiteDatabase db = getWritableDB();
        String id = null;
        //缓存抬头表id
        String insId = null;
        Cursor cursor = db.rawQuery("select id,inspection_id from MTL_INSPECTION_LINES where po_line_id = ?",
                new String[]{refLineId});
        while (cursor.moveToNext()) {
            id = cursor.getString(0);
            insId = cursor.getString(1);
        }
        cursor.close();

        if (TextUtils.isEmpty(id))
            return false;
        int iResult = -1;
        // 删除行
        iResult = db.delete("MTL_INSPECTION_LINES", "id = ?", new String[]{id});
        if (iResult < 0)
            return false;
        // 删除扩展表
        iResult = db.delete("MTL_INSPECTION_LINES_CUSTOM", "inspection_line_id = ?", new String[]{id});
        if (iResult < 0)
            return false;
        // 删除照片
        db.delete("MTL_IMAGES", "ref_line_id = ?", new String[]{refLineId});

        // 查头里面是否还有其他行 没有其他行的话 把头删除
        if (!TextUtils.isEmpty(insId)) {
            int count = -1;
            cursor = db.rawQuery("select count(*) as count from MTL_INSPECTION_LINES where inspection_id = ?",
                    new String[]{insId});
            while (cursor.moveToNext()) {
                count = cursor.getInt(0);
            }
            cursor.close();
            if (count == 0) {
                iResult = db.delete("MTL_INSPECTION_HEADERS", "id = ?", new String[]{insId});
                if (iResult < 0)
                    return false;
            }
        }
        db.close();
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
    public boolean uploadInspectionDataSingle(ResultEntity param) {
        boolean customBoolean = false;// 标识是否更新扩展表
        switch (param.businessType) {
            case "00":
                break;
            case "01":
                // 青海的验收需要更新扩展表
                customBoolean = true;
                break;
            default:
                break;
        }

        // 1.头表
        String insId = saveInspectionHeader(param);
        param.insId = insId;
        // 2.行表
        String insLineId = saveInspectionLine(param);
        param.insLineId = insLineId;
        if (customBoolean) {
            // 3.扩展表
            saveInspectionCustom(param);
        }
        return true;
    }

    /**
     * 保存验收抬头
     *
     * @return
     */
    private String saveInspectionHeader(ResultEntity param) {
        clearStringBuffer();
        SQLiteDatabase db = getWritableDB();
        String insId = null;
        sb.append("select distinct id from MTL_INSPECTION_HEADERS H where H.po_id = ? and H.ins_flag = ? ");
        Cursor cursor = db.rawQuery(sb.toString(), new String[]{param.refCodeId, "1"});

        while (cursor.moveToNext()) {
            insId = cursor.getString(0);
        }
        clearStringBuffer();
        cursor.close();

        final String currentDate = UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1);
        ContentValues cv = new ContentValues();
        if (TextUtils.isEmpty(insId)) {
            //如果为空那么插入一条
            insId = UiUtil.getUUID();
            cv.put("id", insId);
            cv.put("po_id", param.refCodeId);
            cv.put("ins_flag", "1");
            cv.put("inspection_date", currentDate);
            cv.put("workflow_level", "0");
            cv.put("approval_flag", "1");
            cv.put("edit_flag", "N");
            cv.put("print_flag", "N");
            cv.put("status", "Y");
            cv.put("inspection_type", param.inspectionType);
            cv.put("system_flag", "1");
            cv.put("arrival_date", currentDate);
            //保存成功系统生成的验收单号
            cv.put("inspection_num", "");
            cv.put("created_by", param.userId);
            cv.put("creation_date", currentDate);
            db.insert("MTL_INSPECTION_HEADERS", null, cv);
        } else {
            cv.put("created_by", param.userId);
            cv.put("creation_date", currentDate);
            db.update("MTL_INSPECTION_HEADERS", cv, "id = ?", new String[]{insId});
        }
        db.close();
        return insId;
    }

    /**
     * 保存验收行
     *
     * @param param
     * @return
     */
    private String saveInspectionLine(ResultEntity param) {
        String insLineId = null;
        String insId = param.insId;
        clearStringBuffer();
        SQLiteDatabase db = getWritableDB();

        sb.append("select id from MTL_INSPECTION_LINES where ")
                .append("inspection_id = ? and po_line_id = ?");
        Cursor cursor = db.rawQuery(sb.toString(), new String[]{insId, param.refLineId});
        while (cursor.moveToNext()) {
            insLineId = cursor.getString(0);
        }
        clearStringBuffer();
        cursor.close();
        final String currentDate = UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1);
        ContentValues cv = new ContentValues();
        if (TextUtils.isEmpty(insLineId)) {
            insLineId = UiUtil.getUUID();
            cv.put("id", insLineId);
            cv.put("inspection_id", insId);
            cv.put("po_line_id", param.refLineId);
            cv.put("material_id", param.materialId);
            cv.put("inspection_person", param.userId);
            cv.put("inspection_date", currentDate);
            cv.put("line_num", param.refLineNum);
            cv.put("inspection_result", param.inspectionResult);
            cv.put("status", "Y");
            cv.put("unit", param.unit);
            cv.put("work_id", param.workId);
            cv.put("inv_id", param.invId);
            cv.put("created_by", param.userId);
            cv.put("creation_date", currentDate);
            cv.put("quantity", param.quantity);
            switch (param.companyCode) {
                case "20N0":
                case "20A0":
                    // 青海的收货数量 和 合格数量 单独赋值
                    cv.put("qualified_quantity", param.qualifiedQuantity);
                    break;
                case "8200":
                    //庆阳
                default:
                    break;
            }
            db.insert("MTL_INSPECTION_LINES", null, cv);
        } else {
            sb.append("update MTL_INSPECTION_LINES set inspection_result = ?,created_by = ?,creation_date = ?,inspection_date = ?");
            if ("Y".equalsIgnoreCase(param.modifyFlag)) {
                // 修改
                sb.append(",quantity = ?");
                switch (param.companyCode) {
                    case "20N0":
                    case "20A0":
                        //青海
                        sb.append(",qualified_quantity = ?");
                        sb.append(" where id = ?");
                        db.execSQL(sb.toString(), new Object[]{param.inspectionResult, param.userId, currentDate, currentDate,
                                param.quantity, param.qualifiedQuantity, insLineId});
                        break;
                    case "8200":
                        //庆阳
                    default:
                        break;
                }
            } else {
                // 累加
                switch (param.companyCode) {
                    case "20N0":
                    case "20A0":
                        //青海
                        sb.append(",quantity = quantity + ? , qualified_quantity = qualified_quantity + ?")
                                .append(" where id = ?");
                        db.execSQL(sb.toString(), new Object[]{param.inspectionResult, param.userId, currentDate, currentDate,
                                param.quantity, param.qualifiedQuantity, insLineId});
                        break;
                    case "8200":
                        //庆阳
                    default:
                        break;
                }
            }
        }
        db.close();
        return insLineId;
    }

    /**
     * 保存验收扩展表
     *
     * @param param
     */
    private void saveInspectionCustom(ResultEntity param) {
        //1. 扩展信息->每次删除 重新插入
        SQLiteDatabase db = getReadableDB();
        db.delete("MTL_INSPECTION_LINES_CUSTOM", "inspection_id = ? and inspection_line_id = ?",
                new String[]{param.insId, param.insLineId});
        ContentValues cv = new ContentValues();
        cv.put("id", UiUtil.getUUID());
        cv.put("inspection_id", param.insId);
        cv.put("inspection_line_id", param.insLineId);
        cv.put("random_quantity", param.randomQuantity);
        cv.put("rust_quantity", param.rustQuantity);
        cv.put("damaged_quantity", param.damagedQuantity);
        cv.put("bad_quantity", param.badQuantity);
        cv.put("other_quantity", param.otherQuantity);
        cv.put("z_package", param.sapPackage);
        cv.put("qm_num", param.qmNum);
        cv.put("certificate", param.certificate);
        cv.put("instructions", param.instructions);
        cv.put("qm_certificate", param.qmCertificate);
        cv.put("claim_num", param.claimNum);
        cv.put("manufacturer", param.manufacturer);
        cv.put("inspection_quantity", param.inspectionQuantity);
        db.insert("MTL_INSPECTION_LINES_CUSTOM", null, cv);
        cv.clear();
        db.close();

    }
}
