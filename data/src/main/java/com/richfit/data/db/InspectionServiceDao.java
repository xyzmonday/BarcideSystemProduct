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
import com.richfit.domain.bean.RefDetailEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.ResultEntity;
import com.richfit.domain.repository.IInspectionServiceDao;

import java.util.ArrayList;
import java.util.List;

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
        return true;
    }

    @Override
    public ArrayList<ImageEntity> readImagesByRefNum(String refNum, boolean isLocal) {
        ArrayList<ImageEntity> images = new ArrayList<>();
        if (TextUtils.isEmpty(refNum)) {
            return images;
        }
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

    /**
     * 保存验收单条缓存
     *
     * @param param
     * @return
     */
    @Override
    public boolean uploadInspectionDataSingle(ResultEntity param) {
        SQLiteDatabase db = getWritableDB();
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
        String insId = saveInspectionHeader(db, param);
        param.insId = insId;
        // 2.行表
        String insLineId = saveInspectionLine(db, param);
        param.insLineId = insLineId;
        if (customBoolean) {
            // 3.扩展表
            saveInspectionCustom(db, param);
        }
        db.close();
        return true;
    }

    /**
     * 读取验收需要上传的数据
     *
     * @return
     */
    @Override
    public List<ReferenceEntity> readTransferedData() {
        SQLiteDatabase db = getWritableDB();
        ArrayList<ReferenceEntity> datas = new ArrayList<>();

        clearStringBuffer();
        StringBuffer sql = new StringBuffer();
        sql.append("select H.id,H.inspection_date,H.po_id,H.ref_code,H.approval_flag,H.edit_flag, ")
                .append("H.inspection_type,H.created_by,H.creation_date,H.last_updated_by,H.last_update_date ")
                .append("from MTL_INSPECTION_HEADERS H ")
                .append(" where (H.ins_flag = '0' or H.ins_flag = '2') ")
                .append(" order by H.creation_date");
        ReferenceEntity header = null;
        int index;
        //1. 读取抬头的信息
        Cursor cursor = db.rawQuery(sql.toString(), null);
        while (cursor.moveToNext()) {
            index = -1;
            header = new ReferenceEntity();
            header.transId = cursor.getString(++index);
            header.voucherDate = cursor.getString(++index);
            header.refCodeId = cursor.getString(++index);
            header.recordNum = cursor.getString(++index);
            header.bizType = cursor.getString(++index);
            header.refType = cursor.getString(++index);
            header.inspectionType = cursor.getInt(++index);
            header.recordCreator = cursor.getString(++index);
            header.creationDate = UiUtil.transferLongToDate("yyyyMMddHHmmss", cursor.getLong(++index));
            header.lastUpdatedBy = cursor.getString(++index);
            header.lastUpdateDate = UiUtil.transferLongToDate("yyyyMMddHHmmss", cursor.getLong(++index));
            datas.add(header);
        }
        cursor.close();
        sql.setLength(0);
        //2. 读取明细
        if (datas.size() == 0) {
            return datas;
        }

        sb.append("select L.id,L.inspection_id,L.po_line_id,")
                .append("L.material_id,M.material_num,M.material_group,M.material_desc,")
                .append("L.inspection_person,L.inspection_date,")
                .append("L.line_num,L.inspection_result,L.unit,")
                .append("L.work_id,WORG.org_code as work_code,WORG.org_name as work_name, ")
                .append("L.inv_id,IORG.org_code as inv_code,IORG.org_name as inv_name,")
                .append("L.created_by,L.quantity,L.qualified_quantity,")
                .append("S.random_quantity,S.rust_quantity,S.damaged_quantity,")
                .append("S.bad_quantity,S.other_quantity,S.z_package,S.qm_num,")
                .append("S.certificate,S.instructions,S.qm_certificate,S.claim_num,")
                .append("S.manufacturer,S.inspection_quantity ")
                .append("from MTL_INSPECTION_LINES L ")
                .append("left join base_material_code M ")
                .append("on L.material_id = M.id ")
                .append("left join P_AUTH_ORG WORG ")
                .append("on L.work_id = WORG.org_id ")
                .append("left join P_AUTH_ORG IORG ")
                .append("on L.inv_id = IORG.org_id ")
                .append("left join MTL_INSPECTION_LINES_CUSTOM S ")
                .append("on L.id = S.inspection_line_id ")
                .append(" where L.inspection_id = ?");

        for (int i = 0, size = datas.size(); i < size; i++) {
            final ReferenceEntity refData = datas.get(i);
            refData.billDetailList = new ArrayList<>();
            RefDetailEntity detail = null;
            cursor = db.rawQuery(sb.toString(), new String[]{refData.transId});
            while (cursor.moveToNext()) {
                detail = new RefDetailEntity();
                index = -1;
                detail.transLineId = cursor.getString(++index);
                detail.transId = cursor.getString(++index);
                detail.refLineId = cursor.getString(++index);
                detail.materialId = cursor.getString(++index);
                detail.materialNum = cursor.getString(++index);
                detail.materialGroup = cursor.getString(++index);
                detail.materialDesc = cursor.getString(++index);
                detail.inspectionPerson = cursor.getString(++index);
                detail.inspectionDate = cursor.getString(++index);
                detail.lineNum = cursor.getString(++index);
                detail.inspectionResult = cursor.getString(++index);
                detail.unit = cursor.getString(++index);
                detail.workId = cursor.getString(++index);
                detail.workCode = cursor.getString(++index);
                detail.workName = cursor.getString(++index);
                detail.invId = cursor.getString(++index);
                detail.invCode = cursor.getString(++index);
                detail.invName = cursor.getString(++index);
                detail.userId = cursor.getString(++index);
                detail.quantity = cursor.getString(++index);
                detail.qualifiedQuantity = cursor.getString(++index);
                detail.randomQuantity = cursor.getString(++index);
                detail.rustQuantity = cursor.getString(++index);
                detail.damagedQuantity = cursor.getString(++index);
                detail.badQuantity = cursor.getString(++index);
                detail.otherQuantity = cursor.getString(++index);
                detail.sapPackage = cursor.getString(++index);
                detail.qmNum = cursor.getString(++index);
                detail.certificate = cursor.getString(++index);
                detail.instructions = cursor.getString(++index);
                detail.qmCertificate = cursor.getString(++index);
                detail.claimNum = cursor.getString(++index);
                detail.manufacturer = cursor.getString(++index);
                detail.inspectionQuantity = cursor.getString(++index);
                refData.billDetailList.add(detail);
            }
            cursor.close();
        }
        //进行数据转换，将子节点转换为Item
        db.close();
        return datas;
    }

    @Override
    public boolean setTransFlag(String transId, String transFlag) {
        SQLiteDatabase db = getWritableDB();
        ContentValues cv = new ContentValues();
        cv.put("ins_flag", transFlag);
        int iResult = db.update("MTL_INSPECTION_HEADERS", cv, "id = ?", new String[]{transId});
        db.close();
        return iResult > 0;
    }

    @Override
    public boolean uploadEditedHeadData(ResultEntity resultEntity) {
        return false;
    }

    @Override
    public void deleteOfflineDataAfterUploadSuccess(String transId, String bizType, String refType, String userId) {
        SQLiteDatabase db = getWritableDB();
        //删除缓存
        db.delete("MTL_INSPECTION_HEADERS", null, null);
        db.delete("MTL_INSPECTION_LINES", null, null);
        db.delete("MTL_INSPECTION_LINES_CUSTOM", null, null);
        //删除单据
        db.delete("MTL_PO_HEADERS", null, null);
        db.delete("MTL_PO_LINES", null, null);
        db.close();
    }

    /**
     * 保存验收抬头
     *
     * @return
     */
    private String saveInspectionHeader(SQLiteDatabase db, ResultEntity param) {
        clearStringBuffer();
        String insId = null;
        sb.append("select distinct id from MTL_INSPECTION_HEADERS H where H.po_id = ? and H.ins_flag = ?" );
        Cursor cursor = db.rawQuery(sb.toString(), new String[]{param.refCodeId, "0"});

        while (cursor.moveToNext()) {
            insId = cursor.getString(0);
        }
        clearStringBuffer();
        cursor.close();

        final long creationDate = UiUtil.getSystemDate();
        final String currentDate = UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1);
        long iResult = -1;
        ContentValues cv = new ContentValues();
        if (TextUtils.isEmpty(insId)) {
            //如果为空那么插入一条
            insId = UiUtil.getUUID();
            cv.put("id", insId);
            cv.put("po_id", param.refCodeId);
            cv.put("ref_code", param.refCode);
            //缓存标识
            cv.put("ins_flag", "0");
            //过账日期
            cv.put("inspection_date", param.voucherDate);
            cv.put("inspection_type", param.inspectionType);
            cv.put("arrival_date", currentDate);
            //保存成功系统生成的验收单号
            cv.put("created_by", param.userId);
            cv.put("creation_date", creationDate);
            //注意这里实际上没有保存bizType和refType，为了数据上传方便强制保存
            cv.put("approval_flag", param.businessType);
            cv.put("edit_flag", param.refType);
            iResult = db.insert("MTL_INSPECTION_HEADERS", null, cv);
        } else {
            cv.put("inspection_date", param.voucherDate);
            cv.put("last_updated_by", param.userId);
            cv.put("last_update_date", creationDate);
            iResult = db.update("MTL_INSPECTION_HEADERS", cv, "id = ?", new String[]{insId});
        }
        return iResult > 0 ? insId : null;
    }

    /**
     * 保存验收行
     *
     * @param param
     * @return
     */
    private String saveInspectionLine(SQLiteDatabase db, ResultEntity param) {
        String insLineId = null;
        String insId = param.insId;
        clearStringBuffer();

        sb.append("select id from MTL_INSPECTION_LINES where ")
                .append("inspection_id = ? and po_line_id = ?");
        Cursor cursor = db.rawQuery(sb.toString(), new String[]{insId, param.refLineId});
        while (cursor.moveToNext()) {
            insLineId = cursor.getString(0);
        }
        clearStringBuffer();
        cursor.close();
        final String currentDate = UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1);
        final long creationDate = UiUtil.getSystemDate();
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
            cv.put("creation_date", creationDate);
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
            sb.append("update MTL_INSPECTION_LINES set po_line_id = ?,material_id = ?,inspection_person = ?,line_num = ?,")
                    .append("inspection_result = ?,work_id = ?,inv_id = ?,")
                    .append("last_updated_by = ?,last_update_date = ?,inspection_date = ?");
            if ("Y".equalsIgnoreCase(param.modifyFlag)) {
                // 修改
                sb.append(",quantity = ?");
                switch (param.companyCode) {
                    case "20N0":
                    case "20A0":
                        //青海
                        sb.append(",qualified_quantity = ?");
                        sb.append(" where id = ?");
                        db.execSQL(sb.toString(), new Object[]{param.refLineId, param.materialId,
                                param.userId, param.refLineNum, param.inspectionResult,
                                param.workId, param.invId, param.userId, currentDate, currentDate,
                                param.quantity, param.qualifiedQuantity, insLineId});
                        break;
                    case "8200":
                        //庆阳
                }
            } else {
                // 累加
                switch (param.companyCode) {
                    case "20N0":
                    case "20A0":
                        //青海
                        sb.append(",quantity = quantity + ? , qualified_quantity = qualified_quantity + ?")
                                .append(" where id = ?");
                        db.execSQL(sb.toString(), new Object[]{
                                param.refLineId, param.materialId,
                                param.userId, param.refLineNum, param.inspectionResult,
                                param.workId, param.invId, param.userId, currentDate, currentDate,
                                param.quantity, param.qualifiedQuantity, insLineId});
                        break;
                    case "8200":
                        //庆阳
                }
            }
        }
        return insLineId;
    }

    /**
     * 保存验收扩展表
     *
     * @param param
     */
    private void saveInspectionCustom(SQLiteDatabase db, ResultEntity param) {
        //1. 扩展信息->每次删除 重新插入
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
    }
}
