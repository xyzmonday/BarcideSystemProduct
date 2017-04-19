package com.richfit.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.ResultEntity;
import com.richfit.domain.repository.ICheckServiceDao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by monday on 2017/3/29.
 */

public class CheckServiceDao extends BaseDao implements ICheckServiceDao {

    @Inject
    public CheckServiceDao(@ContextLife("Application") Context context) {
        super(context);
    }

    @Override
    public ReferenceEntity getCheckInfo(String userId, String bizType, String checkLevel,
                                        String checkSpecial, String storageNum, String workId,
                                        String invId, String checkNum) {
        SQLiteDatabase db = getWritableDB();
        ReferenceEntity refData = new ReferenceEntity();
        clearStringBuffer();
        String[] selections;
        ArrayList<String> selectionList = new ArrayList<>();
        sb.append("select id from MTL_CHECK_HEADER H where trans_flag = '0' ");
        if (!TextUtils.isEmpty(workId)) {
            sb.append(" and work_id = ?");
            selectionList.add(workId);
        }
        if (!TextUtils.isEmpty(invId)) {
            sb.append(" and inv_id = ?");
            selectionList.add(invId);
        }
        if (!TextUtils.isEmpty(storageNum)) {
            sb.append(" and storage_num = ?");
            selectionList.add(storageNum);
        }

        sb.append(" and check_type = ?")
                .append(" and check_level = ?")
                .append(" and check_special = ?");
        selectionList.add(bizType);
        selectionList.add(checkLevel);
        selectionList.add(checkSpecial);

        switch (bizType) {
            case "C01":// 明盘
                break;
            case "C02":// 盲盘 增加创建人
                sb.append(" and created_by = ?");
                selectionList.add(userId);
                break;
        }
        selections = new String[selectionList.size()];
        selectionList.toArray(selections);
        Cursor cursor = db.rawQuery(sb.toString(), selections);
        while (cursor.moveToNext()) {
            refData.checkId = cursor.getString(0);
        }
        cursor.close();
        //如果没有查询到历史盘点，那么新建一个
        ContentValues cv = new ContentValues();
        final long creationDate = UiUtil.getSystemDate();
        if (TextUtils.isEmpty(refData.checkId)) {
            refData.checkId = UiUtil.getUUID();
            cv.put("id", refData.checkId);
            cv.put("storage_num", storageNum);
            cv.put("work_id", workId);
            cv.put("inv_id", invId);
            cv.put("check_special", checkSpecial);
            cv.put("check_num", checkNum);
            cv.put("creation_date", creationDate);
            cv.put("created_by", userId);
            cv.put("check_level", checkLevel);
            cv.put("check_type",bizType);
            cv.put("trans_flag", "0");
            db.insert("MTL_CHECK_HEADER", null, cv);
            cv.clear();
        } else {
            cv.put("last_updated_by", userId);
            cv.put("last_update_date", creationDate);
            db.update("MTL_CHECK_HEADER", cv, "id = ?", new String[]{refData.checkId});
        }
        db.close();
        return refData;
    }

    @Override
    public boolean deleteCheckData(String storageNum, String workId, String invId, String checkId, String userId, String bizType) {
        if (TextUtils.isEmpty(checkId))
            return false;
        SQLiteDatabase db = getWritableDB();
        int iResult = -1;
        // 删除头
        iResult = db.delete("MTL_CHECK_HEADER", "id = ?", new String[]{checkId});
        if (iResult <= 0)
            return false;
        // 删除行
        iResult = db.delete("MTL_CHECK_LINES", "check_id = ?", new String[]{checkId});
        if (iResult <= 0)
            return false;
        db.close();
        return true;
    }

    @Override
    public List<InventoryEntity> getCheckTransferInfoSingle(String checkId, String materialId, String materialNum,
                                                            String location, String bizType) {
        ArrayList<InventoryEntity> list = new ArrayList<>();
        SQLiteDatabase db = getWritableDB();
        clearStringBuffer();
        String[] selections;
        ArrayList<String> selectionList = new ArrayList<>();
        sb.append(" select t.id,")
                .append(" t.work_id,t.inv_id,t.special_flag,t.special_num,")
                .append(" t.line_num,t.material_id,t.location,t.inv_type,t.quantity,")
                .append(" t.new_flag,t.inv_quantity,m.material_num,")
                .append(" m.material_desc,m.material_group,m.unit,")
                .append(" worg.org_code     as work_code,")
                .append(" worg.org_name     as work_name,")
                .append(" iorg.org_code     as inv_code,")
                .append(" iorg.org_name     as inv_name")
                .append(" from mtl_check_lines t ")
                .append(" left join p_auth_org worg ")
                .append(" on t.work_id = worg.org_id ")
                .append(" left join p_auth_org iorg")
                .append(" on t.inv_id = iorg.org_id, base_material_code m ")
                .append("   where t.material_id = m.id ");
        if (!TextUtils.isEmpty(checkId)) {
            sb.append(" and T.check_id = ?");
            selectionList.add(checkId);
        }
        if (!TextUtils.isEmpty(materialId)) {
            sb.append(" and T.material_id = ?");
            selectionList.add(materialId);
        }

        if (!TextUtils.isEmpty(materialNum)) {
            sb.append(" and M.material_num = ?");
            selectionList.add(materialNum);
        }

        if (!TextUtils.isEmpty(location)) {
            sb.append(" and T.location = ?");
            selectionList.add(location);
        }
        selections = new String[selectionList.size()];
        selectionList.toArray(selections);

        Cursor cursor = db.rawQuery(sb.toString(), selections);
        InventoryEntity item = null;
        int index;
        while (cursor.moveToNext()) {
            index = -1;
            item = new InventoryEntity();
            item.id = cursor.getString(++index);
            item.workId = cursor.getString(++index);
            item.invId = cursor.getString(++index);
            item.specialInvFlag = cursor.getString(++index);
            item.specialInvNum = cursor.getString(++index);
            item.lineNum = cursor.getString(++index);
            item.materialId = cursor.getString(++index);
            item.location = cursor.getString(++index);
            item.inventoryType = cursor.getString(++index);
            item.totalQuantity = cursor.getString(++index);
            item.newFlag = cursor.getString(++index);
            item.invQuantity = cursor.getString(++index);
            item.materialNum = cursor.getString(++index);
            item.materialDesc = cursor.getString(++index);
            item.materialGroup = cursor.getString(++index);
            item.unit = cursor.getString(++index);
            item.workCode = cursor.getString(++index);
            item.workName = cursor.getString(++index);
            item.invCode = cursor.getString(++index);
            item.invName = cursor.getString(++index);
            list.add(item);
        }
        cursor.close();
        clearStringBuffer();
        db.close();
        return list;
    }

    @Override
    public ReferenceEntity getCheckTransferInfo(String checkId, String materialNum, String location,
                                                String isPageQuery, int pageNum, int pageSize, String bizType) {
        ReferenceEntity refData = new ReferenceEntity();
        ArrayList<InventoryEntity> list = new ArrayList<>();
        SQLiteDatabase db = getWritableDB();
        clearStringBuffer();
        Cursor cursor = null;
        int totalCount = -1;
        if (!TextUtils.isEmpty(isPageQuery) && "queryPage".equalsIgnoreCase(isPageQuery)) {
            sb.append("  SELECT COUNT(ID) AS TOTAL_COUNT ")
                    .append(" FROM MTL_CHECK_LINES T ")
                    .append(" WHERE T.CHECK_ID = ?");
            cursor = db.rawQuery(sb.toString(), new String[]{checkId});

            while (cursor.moveToNext()) {
                totalCount = cursor.getInt(0);
            }
            cursor.close();
            refData.totalCount = totalCount;
        }

        clearStringBuffer();
        String[] selections;
        ArrayList<String> selectionList = new ArrayList<>();
        sb.append("select t.id,")
                .append(" t.work_id,t.inv_id,t.special_flag,t.special_num,")
                .append(" t.line_num,t.material_id,t.location,t.inv_type,t.quantity,")
                .append(" t.new_flag,t.inv_quantity,m.material_num,")
                .append(" m.material_desc,m.material_group,m.unit,")
                .append(" worg.org_code     as work_code,")
                .append(" worg.org_name     as work_name,")
                .append(" iorg.org_code     as inv_code,")
                .append(" iorg.org_name     as inv_name")
                .append(" from mtl_check_lines t ")
                .append(" left join p_auth_org worg ")
                .append(" on t.work_id = worg.org_id ")
                .append(" left join p_auth_org iorg")
                .append(" on t.inv_id = iorg.org_id, base_material_code m ")
                .append("   where t.material_id = m.id ");

        if (!TextUtils.isEmpty(checkId)) {
            sb.append(" and T.check_id = ?");
            selectionList.add(checkId);
        }

        if (!TextUtils.isEmpty(materialNum)) {
            sb.append(" and M.material_num = ?");
            selectionList.add(materialNum);
        }

        if (!TextUtils.isEmpty(location)) {
            sb.append(" and T.location = ?");
            selectionList.add(location);
        }

        if (pageNum > 0 && pageSize > 0) {
            sb.append(" limit ? offset ? ");
            selectionList.add(String.valueOf(pageSize));
            selectionList.add(String.valueOf((pageNum-1) * pageSize));
        }
        selections = new String[selectionList.size()];
        selectionList.toArray(selections);

        cursor = db.rawQuery(sb.toString(), selections);
        InventoryEntity item = null;
        int index;
        while (cursor.moveToNext()) {
            index = -1;
            item = new InventoryEntity();
            item.id = cursor.getString(++index);
            item.workId = cursor.getString(++index);
            item.invId = cursor.getString(++index);
            item.specialInvFlag = cursor.getString(++index);
            item.specialInvNum = cursor.getString(++index);
            item.lineNum = cursor.getString(++index);
            item.materialId = cursor.getString(++index);
            item.location = cursor.getString(++index);
            item.inventoryType = cursor.getString(++index);
            item.totalQuantity = cursor.getString(++index);
            item.newFlag = cursor.getString(++index);
            item.invQuantity = cursor.getString(++index);
            item.materialNum = cursor.getString(++index);
            item.materialDesc = cursor.getString(++index);
            item.materialGroup = cursor.getString(++index);
            item.unit = cursor.getString(++index);
            item.workCode = cursor.getString(++index);
            item.workName = cursor.getString(++index);
            item.invCode = cursor.getString(++index);
            item.invName = cursor.getString(++index);
            list.add(item);
        }
        cursor.close();
        clearStringBuffer();

        if (list.size() == 0) {
            return refData;
        }

        refData.checkList = list;
        db.close();
        return refData;
    }

    @Override
    public boolean deleteCheckDataSingle(String checkId, String checkLineId, String userId, String bizType) {
        SQLiteDatabase db = getWritableDB();
        int iResult = -1;
        switch (bizType) {
            case "C01":
                // 明盘不能删除行 只能把盘点数量更新为0
                break;
            case "C02":
                // 盲盘可以删除行
                iResult = db.delete("MTL_CHECK_LINES", "id = ? and created_by = ?", new String[]{checkLineId,userId});
                break;
        }
        db.close();
        return iResult > 0;
    }

    @Override
    public boolean uploadCheckDataSingle(ResultEntity result) {
        SQLiteDatabase db = getWritableDB();
        String[] selections;
        ArrayList<String> selectionList = new ArrayList<>();
        // 查询缓存数据
        clearStringBuffer();
        sb.append("select id from MTL_CHECK_LINES where check_id = ? and material_id = ?");
        selectionList.add(result.checkId);
        selectionList.add(result.materialId);
        if (!TextUtils.isEmpty(result.location)) {
            sb.append(" and location = ?");
            selectionList.add(result.location);
        }

        if (!TextUtils.isEmpty(result.workId)) {
            sb.append(" and work_id = ?");
            selectionList.add(" and work_id = ?");
        }

        if (!TextUtils.isEmpty(result.invId)) {
            sb.append(" and inv_id = ? ");
            selectionList.add(result.invId);
        }

        if (!TextUtils.isEmpty(result.specialInvFlag) && !TextUtils.isEmpty(result.specialInvNum)) {
            sb.append(" and special_flag = ? and special_num = ?");
            sb.append(result.specialInvFlag);
            sb.append(result.specialInvNum);
        }
        selections = new String[selectionList.size()];
        selectionList.toArray(selections);
        Cursor cursor = db.rawQuery(sb.toString(), selections);
        String id = null;
        while (cursor.moveToNext()) {
            id = cursor.getString(0);
        }
        cursor.close();
        clearStringBuffer();

        //获取当前行表的数量
        int count = 0;
        cursor = db.rawQuery("select count(*) as count from MTL_CHECK_LINES where check_id = ?",
                new String[]{result.checkId});
        while (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        ContentValues cv = new ContentValues();
        final long creationDate = UiUtil.getSystemDate();
        if (TextUtils.isEmpty(id)) {
            //新增
            id = UiUtil.getUUID();
            cv.put("id", id);
            cv.put("work_id", result.workId);
            cv.put("inv_id", result.invId);
            cv.put("check_id", result.checkId);
            cv.put("special_flag", result.specialInvFlag);
            cv.put("special_num", result.specialInvNum);
            cv.put("material_id", result.materialId);
            cv.put("location", result.location);
            cv.put("quantity", result.quantity);
            cv.put("new_flag", "Y");
            cv.put("created_by", result.userId);
            cv.put("creation_date", creationDate);
            cv.put("line_num", count);
            cv.put("inv_quantity", "0");
            db.insert("MTL_CHECK_LINES", null, cv);
        } else {
            //更新
            if (!TextUtils.isEmpty(result.modifyFlag) && "Y".equalsIgnoreCase(result.modifyFlag)) {
                clearStringBuffer();
                sb.append("update MTL_CHECK_LINES set ")
                        .append("last_updated_by = ?,")
                        .append("last_update_date = ?,")
                        .append("quantity = ? where id = ?");
                db.execSQL(sb.toString(), new Object[]{result.userId, creationDate, result.quantity, id});
            } else {
                clearStringBuffer();
                sb.append("update MTL_CHECK_LINES set ")
                        .append("last_updated_by = ?,")
                        .append("last_update_date = ?,")
                        .append("quantity = quantity + ? where id = ?");
                db.execSQL(sb.toString(), new Object[]{result.userId, creationDate, result.quantity, id});
            }
        }

        db.close();
        return true;
    }
}
