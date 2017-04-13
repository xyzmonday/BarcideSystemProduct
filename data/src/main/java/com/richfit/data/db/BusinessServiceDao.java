package com.richfit.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.LocationInfoEntity;
import com.richfit.domain.bean.ResultEntity;
import com.richfit.domain.repository.IBusinessService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by monday on 2017/3/29.
 */

public class BusinessServiceDao extends BaseDao implements IBusinessService {

    @Inject
    public BusinessServiceDao(@ContextLife("Application") Context context) {
        super(context);
    }

    /**
     * 保存单条数据
     *
     * @param result
     * @return
     */
    @Override
    public boolean uploadBusinessDataSingle(ResultEntity result) {
        boolean yk = false;// 标识是否是移库:移库的话 查询条件增加 recWorkId & recInvId
        boolean custom = false;// 标识是否需要扩展表赋值:201成本中心 221WBS
        boolean subGroup = false;// 标识组件类型->543用到:需要匹配预留和预留行
        boolean refDoc = false;// 标识是否需要匹配参考物料凭证和行号读取缓存
        boolean prodGroup = false;// 标识产成品类型：需要增加refDoc is null的条件
        boolean device = false;// 庆阳公司的库存需要设备字段

        switch (result.businessType) {
            case "13":// 采购入库-105(非必检)
                refDoc = true;
                break;
            case "19":
                prodGroup = true;
                break;
            case "19_ZJ":
                subGroup = true;
                // 针对委外的组件进行单独处理：把组件信息和成品信息合并到一张单据中
                result.businessType = "19";
                break;
            case "26":// 无参考-201
            case "27":// 无参考-221
            case "46":// 无参考-202
            case "47":// 无参考-222
                custom = true;
                break;
            case "32":// 301(无参考)
            case "34":// 311(无参考)
            case "74":// 代管料移库
            case "94":// 代管料调拨-HRM
                yk = true;
                if ("8200".equals(result.companyCode)) {
                    device = true;
                }
                break;
            default:
                break;
        }

        //1.抬头
        final String transId = saveBusinessHeader(result);
        if (TextUtils.isEmpty(transId)) {
            return false;
        }

        result.transId = transId;
        // 2.行表
        String transLineId = saveBusinessLine(result, yk, subGroup, refDoc, prodGroup);
        if (TextUtils.isEmpty(transLineId)) {
            return false;
        }
        result.transLineId = transLineId;

        // 3.行表-拆分表
        String transLineSplitId = saveBusinessLineSplit(result, yk);
        if (TextUtils.isEmpty(transLineSplitId)) {
            return false;
        }
        result.transLineSplitId = transLineSplitId;

        // 4.仓位表
        String locationId = saveBusinessLocation(result, yk, device);
        if (TextUtils.isEmpty(locationId)) {
            return false;
        }

        // 5.更新行的累计数量
        updateLineTotalQuantity(result);

        // 6.更新拆分行的累计数量
        updateLineSplitTotalQuantity(result);

        return true;
    }

    /**
     * 保存单条缓存数据的抬头部分
     *
     * @param param
     * @return
     */
    private String saveBusinessHeader(ResultEntity param) {
        // 1.查头是否存在
        // 条件
        // 无参考：bizType、createdBy、
        // 有参考：bizType、refType、refCodeId
        clearStringBuffer();
        SQLiteDatabase db = getWritableDB();
        String transId = "";

        //定义一个匹配条件的容器
        List<String> selectionsList = new ArrayList<>();
        String[] selections;

        //1.获取抬头id是否存在，如果存在则跟新，否者新增一条
        sb.append("select id from mtl_transaction_headers H where H.biz_type = ? and trans_flag = ? ");
        selectionsList.add(param.businessType);
        selectionsList.add("0");
        switch (param.businessType) {
            case "16":// 其他入库-无参考
            case "25":// 其他出库-无参考
            case "26":// 无参考-201
            case "27":// 无参考-221
            case "32":// 301(无参考)
            case "34":// 311(无参考)
            case "44":// 其他退库-无参考
            case "46":// 无参考-202
            case "47":// 无参考-222
            case "94":// 代管料调拨-HRM
                sb.append(" and H.created_by = ?");
                selectionsList.add(param.userId);
                break;
            default:
                sb.append(" and H.ref_type = ? and H.ref_code_id = ? ");
                selectionsList.add(param.refType);
                selectionsList.add(param.refCodeId);
                break;
        }
        //注意这里不能使用list.toArray直接转，因为Object[]不能直接转String[]
        selections = new String[selectionsList.size()];
        selectionsList.toArray(selections);
        Cursor cursor = db.rawQuery(sb.toString(), selections);
        while (cursor.moveToNext()) {
            transId = cursor.getString(0);
        }
        sb.setLength(0);
        cursor.close();
        ContentValues cv;
        if (TextUtils.isEmpty(transId)) {
            //如果不存在，那么直接插入一条数据
            cv = new ContentValues();
            //随机生成一个主键
            transId = UiUtil.getUUID();
            cv.put("id", transId);
            cv.put("ref_code_id", param.refCodeId);
            cv.put("biz_type", param.businessType);
            cv.put("ref_type", param.refType);
            cv.put("ref_code", param.refCode);
            cv.put("trans_flag", "0");
            cv.put("voucher_date", param.voucherDate);
            cv.put("move_type", param.moveType);
            cv.put("supplier_id", param.supplierId);
            cv.put("supplier_code", param.supplierNum);
            cv.put("created_by", param.userId);
            cv.put("creation_date", UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1));
            long iResult = db.insert("mtl_transaction_headers", null, cv);
            if (iResult < 1) {
                return "";
            }
        } else {
            //修改
            cv = new ContentValues();
            cv.put("id", transId);
            cv.put("created_by", param.userId);
            cv.put("creation_date", UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1));
            int iResult = db.update("mtl_transaction_headers", cv, "id = ?", new String[]{transId});
            if (iResult < 1) {
                return "";
            }
        }
        return transId;

    }

    /**
     * 保存单条缓存数据的明细部分
     *
     * @param param
     * @param yk
     * @param subGroup
     * @param refDoc
     * @param prodGroup
     * @return
     */
    private String saveBusinessLine(ResultEntity param, boolean yk, boolean subGroup,
                                    boolean refDoc, boolean prodGroup) {
        String transLineId = null;
        String transId = param.transId;
        // 2.行 查询行是否存在
        // 条件
        // 无参考：workId、invId、materialId 移库增加 recWorkId、recInvId
        // 有参考：refLineId
        clearStringBuffer();
        SQLiteDatabase db = getWritableDB();
        String[] selections;
        List<String> selectionList = new ArrayList<>();
        sb.append("select L.id from mtl_transaction_lines L where L.trans_id = ?");
        selectionList.add(transId);
        switch (param.businessType) {
            case "16":// 其他入库-无参考
            case "25":// 其他出库-无参考
            case "26":// 无参考-201
            case "27":// 无参考-221
            case "32":// 301(无参考)
            case "34":// 311(无参考)
            case "44":// 其他退库-无参考
            case "46":// 无参考-202
            case "47":// 无参考-222
            case "94":// 代管料调拨-HRM
                if (yk) {
                    sb.append(" and L.rec_inv_id = ? and L.rec_work_id = ? ");
                    selectionList.add(param.recInvId);
                    selectionList.add(param.recWorkId);
                }
                break;
            default:
                sb.append(" and L.ref_line_id = ?");
                selectionList.add(param.refLineId);
                break;
        }

        if (subGroup || refDoc) {
            // 组件和有参考物料凭证的业务还需要加上对应的单号和行号
            sb.append(" and L.ref_doc = ? ");
            selectionList.add(param.refDoc);
            if (param.refDocItem != null) {
                sb.append("  and L.ref_doc_item = ? ");
                //注意list不能给出
                selectionList.add(String.valueOf(param.refDocItem));
            }
        }
        if (prodGroup) {
            // 产成品需要去除参考单据号
            sb.append(" and L.ref_doc is null");
        }

        selections = new String[selectionList.size()];
        selectionList.toArray(selections);

        Cursor cursor = db.rawQuery(sb.toString(), selections);
        while (cursor.moveToNext()) {
            transLineId = cursor.getString(0);
        }
        sb.setLength(0);
        cursor.close();

        ContentValues cv = new ContentValues();
        cv.put("trans_id", transId);
        cv.put("material_id", param.materialId);
        cv.put("work_id", param.workId);
        cv.put("inv_id", param.invId);
        cv.put("ref_line_id", param.refLineId);
        cv.put("ref_line_num", param.refLineNum);
        cv.put("ref_doc", param.refDoc);
        cv.put("ref_doc_item", param.refDocItem);
        // 移库增加接收工厂、库存地点、接收批次
        if (yk) {
            cv.put("rec_work_id", param.recWorkId);
            cv.put("rec_inv_id", param.recInvId);
            cv.put("rec_inv_id", param.invType);
        }

        if ("110".equals(param.businessType)) {
            // 采购入库-105（青海必检）
            cv.put("ins_lot", param.insLot);
            cv.put("decision_code", param.decisionCode);
            cv.put("project_text", param.projectText);
            cv.put("move_cause", param.moveCause);
            cv.put("move_cause_desc", param.moveCauseDesc);
            cv.put("return_quantity", param.returnQuantity);
        }

        if (TextUtils.isEmpty(transLineId)) {
            // 新增
            transLineId = UiUtil.getUUID();
            cv.put("id", transLineId);
            cv.put("created_by", param.userId);
            cv.put("creation_date", UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1));
            long iResult = db.insert("mtl_transaction_lines", null, cv);
            if (iResult < 1) {
                return "";
            }
        } else {
            // 修改
            cv.put("id", transLineId);
            cv.put("last_updated_by", param.userId);
            cv.put("last_update_date", UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1));
            int iResult = db.update("mtl_transaction_lines", cv, "id = ?", new String[]{transLineId});
            if (iResult < 1) {
                return "";
            }
        }
        db.close();
        return transLineId;
    }

    /**
     * 保存单条行表拆分的数据
     *
     * @param param
     * @param yk
     * @return
     */
    private String saveBusinessLineSplit(ResultEntity param, boolean yk) {
        String transLineSplitId = null;
        String transId = param.transId;
        String transLineId = param.transLineId;
        clearStringBuffer();
        SQLiteDatabase db = getWritableDB();
        String[] selections;
        List<String> selectionList = new ArrayList<>();

        // 2.行 查询行是否存在
        // 条件
        sb.append("select id from mtl_transaction_lines_split ")
                .append(" where trans_id = ? and trans_line_id = ? ");
        selectionList.add(transId);
        selectionList.add(transLineId);
        if (!TextUtils.isEmpty(param.batchFlag)) {
            // 行里增加批次的条件(传进来批次就按照批次查询 没有传进来就按照is null查询)
            sb.append(" and batch_num = ?");
            selectionList.add(param.batchFlag);
        } else {
            sb.append(" and batch_num is null ");
        }

        if (yk) {
            // 移库增加接收批次的条件
            if (!TextUtils.isEmpty(param.recBatchFlag)) {
                sb.append(" and rec_batch_num = ? ");
                selectionList.add(!TextUtils.isEmpty(param.recBatchFlag) ? param.recBatchFlag : "null");
            } else {
                sb.append(" and rec_batch_num is null");
            }
        }

        // 行里增加特殊库存的条件(传进来批次就按照特殊库存查询 没有传进来就按照is null查询)
        if (!TextUtils.isEmpty(param.specialInvFlag)) {
            sb.append(" and special_flag = ? ");
            selectionList.add(param.specialInvFlag);
        } else {
            sb.append(" and special_flag is null");
        }

        if (!TextUtils.isEmpty(param.specialInvNum)) {
            sb.append(" and special_num = ? ");
            selectionList.add(param.specialInvNum);
        } else {
            sb.append(" and special_num is null");
        }

        selections = new String[selectionList.size()];
        selectionList.toArray(selections);
        Cursor cursor = db.rawQuery(sb.toString(), selections);
        while (cursor.moveToNext()) {
            transLineSplitId = cursor.getString(0);
        }
        cursor.close();
        sb.setLength(0);

        ContentValues cv = new ContentValues();
        cv.put("trans_id", transId);
        cv.put("trans_line_id", transLineId);
        cv.put("material_id", param.materialId);
        cv.put("work_id", param.workId);
        cv.put("inv_id", param.invId);
        cv.put("inv_type", param.invType);
        cv.put("special_flag", param.specialInvFlag);
        cv.put("special_num", param.specialInvNum);
        cv.put("ref_line_id", param.refLineId);
        cv.put("batch_num", param.batchFlag);
        cv.put("ref_line_num", param.refLineNum);
        cv.put("ref_doc", param.refDoc);
        if (param.refDocItem != null) {
            cv.put("ref_doc_item", param.refDocItem);
        }

        if ("110".equals(param.businessType)) {
            // 采购入库-105（青海必检）
            cv.put("ins_lot", param.insLot);
            cv.put("decision_code", param.decisionCode);
            cv.put("project_text", param.projectText);
            cv.put("move_cause", param.moveCause);
            cv.put("move_cause_desc", param.moveCauseDesc);
            cv.put("return_quantity", param.returnQuantity);
        }

        // 移库增加接收工厂、库存地点、接收批次
        if (yk) {
            cv.put("rec_work_id", param.recWorkId);
            cv.put("rec_inv_id", param.recInvId);
            cv.put("inv_type", param.invType);
            cv.put("rec_batch_num", param.recBatchFlag);
        }

        if (TextUtils.isEmpty(transLineSplitId)) {
            // 新增
            transLineSplitId = UiUtil.getUUID();
            cv.put("id", transLineSplitId);
            cv.put("created_by", param.userId);
            cv.put("creation_date", UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1));

            long iResult = db.insert("mtl_transaction_lines_split", null, cv);
            if (iResult < 1) {
                return "";
            }
        } else {
            // 修改
            cv.put("id", transLineSplitId);
            cv.put("last_updated_by", param.userId);
            cv.put("last_update_date", UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1));
            int iResult = db.update("mtl_transaction_lines_split", cv, "id = ?", new String[]{transLineSplitId});
            if (iResult < 1) {
                return "";
            }
        }
        db.close();
        return transLineSplitId;
    }

    /**
     * 保存仓位缓存
     *
     * @param param
     * @param yk
     * @param device
     * @return
     */
    private String saveBusinessLocation(ResultEntity param, boolean yk, boolean device) {
        String locationId = null;
        String transId = param.transId;
        String transLineId = param.transLineId;
        String transLineSplitId = param.transLineSplitId;
        clearStringBuffer();
        SQLiteDatabase db = getWritableDB();
        String[] selections;
        List<String> selectionList = new ArrayList<>();

        // 如果是修改的话 把本行数据先删除 再重新插入 原因：避免把两行修改成同一仓位(所以这里修改必须传入)
        if (!TextUtils.isEmpty(param.modifyFlag) && "Y".equalsIgnoreCase(param.modifyFlag)) {
            db.delete("mtl_transaction_lines_location", "id = ?", new String[]{param.locationId});
        }

        sb.append(" select id from mtl_transaction_lines_location ");

        // 查询条件 transId transLineId transLineSplitId location
        sb.append(" where trans_id = ? and trans_line_id = ? and trans_line_split_id = ? and location = ? ");
        selectionList.add(transId);
        selectionList.add(transLineId);
        selectionList.add(transLineSplitId);
        selectionList.add(param.location);

        if (yk) {
            sb.append(" and rec_location = ? ");
            selectionList.add(param.recLocation);
        }

        if (device) {
            // 庆阳的增加了设备ID的纬度
            sb.append(" and device_id = ? ");
            selectionList.add(param.deviceId);
        }

        selections = new String[selectionList.size()];
        selectionList.toArray(selections);
        Cursor cursor = db.rawQuery(sb.toString(), selections);
        while (cursor.moveToNext()) {
            locationId = cursor.getString(0);
        }
        sb.setLength(0);
        cursor.close();

        ContentValues cv = new ContentValues();
        if (TextUtils.isEmpty(locationId)) {
            // 新增
            locationId = UiUtil.getUUID();
            cv.put("id", locationId);
            cv.put("trans_id", transId);
            cv.put("trans_line_id", transLineId);
            cv.put("trans_line_split_id", transLineSplitId);
            cv.put("location", param.location);

            if (yk) {
                cv.put("rec_location", param.recLocation);
            }
            if (device) {
                cv.put("device_id", param.deviceId);
                if (yk) {
                    cv.put("rec_device_id", param.deviceId);
                }
            }
            cv.put("created_by", param.userId);
            cv.put("creation_date", UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1));
            cv.put("quantity", param.quantity);
            if (yk) {
                cv.put("rec_quantity", param.quantity);
            }

            long iResult = db.insert("mtl_transaction_lines_location", null, cv);
            if (iResult < 1) {
                return "";
            }
        } else {
            // 修改
            cv.put("last_updated_by", param.userId);
            cv.put("last_update_date", UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1));

            int iResult = db.update("mtl_transaction_lines_location", cv, "id = ?", new String[]{locationId});
            if (iResult < 0) {
                return "";
            }

            //如果是修改则累加数量
            db.execSQL("update mtl_transaction_lines_location set quantity = quantity + ? where id = ?",
                    new Object[]{param.quantity, locationId});
            //如果是移库，那么还需要修改接收累计数量
            if (yk) {
                db.execSQL("update mtl_transaction_lines_location set rec_quantity = rec_quantity + ? where id = ?",
                        new Object[]{param.quantity, locationId});
            }
        }
        return locationId;
    }

    /**
     * 更新行的累计数量。将TANS_LINES_LOCATION  中的全部查出来，然后更新TRANS_LINES表的quantity
     *
     * @param param
     */
    public void updateLineTotalQuantity(ResultEntity param) {
        // mtl_transaction_lines
        SQLiteDatabase db = getWritableDB();
        clearStringBuffer();
        sb.append(" SELECT IFNULL(SUM(T.QUANTITY), 0) AS A ")
                .append(" FROM MTL_TRANSACTION_LINES_LOCATION T ")
                .append(" WHERE T.TRANS_LINE_ID = ?");
        Cursor cursor = db.rawQuery(sb.toString(), new String[]{param.transLineId});
        sb.setLength(0);
        String totalQuantity = null;
        while (cursor.moveToNext()) {
            totalQuantity = cursor.getString(0);
        }
        cursor.close();

        db.execSQL("update MTL_TRANSACTION_LINES set quantity =  ? where id = ?",
                new String[]{totalQuantity, param.transLineId});
        db.close();
    }

    /**
     * .更新拆分行的累计数量。将MTL_TRANSACTION_LINES_SPLIT表中某一行的quantity查出来，更新自己的quantity
     *
     * @param param
     */
    private void updateLineSplitTotalQuantity(ResultEntity param) {
        SQLiteDatabase db = getWritableDB();
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT SUM(QUANTITY) AS A ")
                .append("FROM MTL_TRANSACTION_LINES_SPLIT T ")
                .append(" WHERE T.id = ?");
        String totalQuantity = null;
        Cursor cursor = db.rawQuery(sb.toString(), new String[]{param.transLineSplitId});
        while (cursor.moveToNext()) {
            totalQuantity = cursor.getString(0);
        }
        totalQuantity = TextUtils.isEmpty(totalQuantity) ? "0" : totalQuantity;
        cursor.close();
        db.execSQL("update MTL_TRANSACTION_LINES_SPLIT set quantity = ? where id = ?",
                new String[]{totalQuantity, param.transLineSplitId});
        db.close();
    }

    /**
     * 删除整单缓存
     *
     * @param refNum
     * @param transId
     * @param refCodeId
     * @param refType
     * @param bizType
     * @param userId
     * @param companyCode
     * @return
     */
    @Override
    public boolean deleteBusinessData(String refNum, String transId, String refCodeId, String refType,
                                      String bizType, String userId, String companyCode) {
        // 1.查头是否存在
        // 条件
        // 无参考：bizType、createdBy、
        // 有参考：bizType、refType、refCodeId
        if (TextUtils.isEmpty(bizType)) {
            return false;
        }
        clearStringBuffer();
        SQLiteDatabase db = getWritableDB();
        List<String> headerIdList = new ArrayList<>();
        String[] selections;
        List<String> selectionList = new ArrayList<>();
        sb.append("select distinct id from MTL_TRANSACTION_HEADERS ");
        sb.append(" where biz_type = ? ");
        selectionList.add(bizType);
        //注意这里的缓存标识
        sb.append(" and trans_flag = ?");
        selectionList.add("0");
        switch (bizType) {
            case "16":// 其他入库-无参考
            case "25":// 其他出库-无参考
            case "26":// 无参考-201
            case "27":// 无参考-221
            case "32":// 301(无参考)
            case "34":// 311(无参考)
            case "44":// 其他退库-无参考
            case "46":// 无参考-202
            case "47":// 无参考-222
            case "94":
                if (!TextUtils.isEmpty(userId)) {
                    sb.append(" and created_by = ? ");
                    selectionList.add(userId);
                }
                break;
            default:
                if (!TextUtils.isEmpty(refType)) {
                    sb.append(" and ref_type = ? ");
                    selectionList.add(refType);
                }
                if (!TextUtils.isEmpty(refCodeId)) {
                    sb.append(" and ref_code_id = ? ");
                    selectionList.add(refCodeId);
                }
                break;
        }

        selections = new String[selectionList.size()];
        selectionList.toArray(selections);
        Cursor cursor = db.rawQuery(sb.toString(), selections);
        while (cursor.moveToNext()) {
            headerIdList.add(cursor.getString(0));
        }
        cursor.close();
        clearStringBuffer();

        //如果没有数据，那么也默认是删除成功
        if (headerIdList.size() > 0) {
            // 清空所有暂存的数据
            int iResult;
            for (String headerId : headerIdList) {
                iResult = -1;
                // 删头
                iResult = db.delete("MTL_TRANSACTION_HEADERS", "id = ?", new String[]{headerId});
                if (iResult < 0) {
                    return false;
                }
                // 删行
                iResult = db.delete("MTL_TRANSACTION_LINES", "trans_id = ?", new String[]{headerId});
                if (iResult < 0) {
                    return false;
                }
                // 删行的拆分表
                iResult = db.delete("MTL_TRANSACTION_LINES_SPLIT", "trans_id = ?", new String[]{headerId});
                if (iResult < 0) {
                    return false;
                }
                // 删仓位
                iResult = db.delete("MTL_TRANSACTION_LINES_LOCATION", "trans_id = ?", new String[]{headerId});
                if (iResult < 0) {
                    return false;
                }
            }
        }

        db.close();
        return true;
    }

    @Override
    public boolean deleteBusinessDataByLineId(String businessType, String transId, String transLineId) {
        return false;
    }

    @Override
    public boolean deleteBusinessDataByLocationId(String locationId, String transId, String transLineId) {

        if (TextUtils.isEmpty(locationId)) {
            return false;
        }
        if (TextUtils.isEmpty(transLineId)) {
            return false;
        }

        //1. 先将对应仓位的缓存保存
        SQLiteDatabase db = getWritableDB();
        clearStringBuffer();
        sb.append("select trans_id,trans_line_id,trans_line_split_id from MTL_TRANSACTION_LINES_LOCATION")
                .append(" where id = ?");
        Cursor cursor = db.rawQuery(sb.toString(), new String[]{locationId});
        final LocationInfoEntity location = new LocationInfoEntity();
        while (cursor.moveToNext()) {
            location.transId = cursor.getString(0);
            location.transLineId = cursor.getString(1);
            location.transLineSplitId = cursor.getString(2);
        }
        clearStringBuffer();
        cursor.close();

        //2. 删除仓位
        int iResult = db.delete("MTL_TRANSACTION_LINES_LOCATION", "id = ?", new String[]{locationId});
        if (iResult < 0) {
            return false;
        }

        //3. 查询该明细下是否存在子明细 不存在删除
        sb.append("select id from MTL_TRANSACTION_LINES_LOCATION where trans_line_id = ?");
        cursor = db.rawQuery(sb.toString(), new String[]{transLineId});
        String lineLocId = null;
        while (cursor.moveToNext()) {
            lineLocId = cursor.getString(0);
        }
        cursor.close();

        if (TextUtils.isEmpty(lineLocId)) {
            // 如果该行下面的所有第三级缓存都删除完后，那么需要删除明细行里面的缓存
            iResult = db.delete("MTL_TRANSACTION_LINES", "id = ?", new String[]{transLineId});
            if (iResult < 0) {
                return false;
            }
            // 删明细拆分表
            iResult = db.delete("MTL_TRANSACTION_LINES_SPLIT", "trans_line_id = ?", new String[]{transLineId});
            if (iResult < 0) {
                return false;
            }
            // 如果所有的缓存明细行都删除完毕了，那么直接将缓存的抬头也删除
            String lineId = null;
            if (!TextUtils.isEmpty(transId)) {
                cursor = db.rawQuery("select id from MTL_TRANSACTION_LINES where trans_id = ?",
                        new String[]{transId});
                while (cursor.moveToNext()) {
                    lineId = cursor.getString(0);
                }
                cursor.close();

                if (TextUtils.isEmpty(lineId)) {
                    iResult = db.delete("MTL_TRANSACTION_HEADERS", "id = ?", new String[]{transId});
                    if (iResult < 0) {
                        return false;
                    }
                }
            }
        } else {
            // 如果该行下还存在其他的第三级的缓存,那么修改行的累计数量
            cursor = db.rawQuery("select id from MTL_TRANSACTION_LINES where id = ?",
                    new String[]{transLineId});
            cursor.close();
            clearStringBuffer();
            sb.append(" SELECT IFNULL(SUM(T.QUANTITY), 0) AS A ")
                    .append(" FROM MTL_TRANSACTION_LINES_LOCATION T ")
                    .append(" WHERE T.TRANS_LINE_ID = ?");
            String totalQuantity = "0";
            cursor = db.rawQuery(sb.toString(), new String[]{transLineId});
            while (cursor.moveToNext()) {
                totalQuantity = cursor.getString(0);
            }
            cursor.close();
            db.execSQL("update MTL_TRANSACTION_LINES set quantity = ? where id = ?",
                    new Object[]{totalQuantity, transId});
            clearStringBuffer();

            // 查询拆分表中是否有子明细 不存在删除
            if (!TextUtils.isEmpty(location.transLineSplitId)) {
                cursor = db.rawQuery("select id from MTL_TRANSACTION_LINES_LOCATION where trans_line_split_id = ?",
                        new String[]{location.transLineSplitId});
                String transSplitId = null;
                while (cursor.moveToNext()) {
                    transSplitId = cursor.getString(0);
                }
                cursor.close();
                if (TextUtils.isEmpty(transSplitId)) {
                    db.delete("MTL_TRANSACTION_LINES_SPLIT", "id = ?", new String[]{location.transLineSplitId});
                } else {
                    clearStringBuffer();
                    sb.append(" SELECT SUM(QUANTITY) AS A ")
                            .append("FROM MTL_TRANSACTION_LINES_LOCATION T ")
                            .append(" WHERE T.TRANS_LINE_SPLIT_ID = ?");
                    cursor = db.rawQuery(sb.toString(), new String[]{location.transLineSplitId});
                    String quantity = "0";
                    while (cursor.moveToNext()) {
                        quantity = cursor.getString(0);
                    }
                    cursor.close();
                    db.execSQL("update MTL_TRANSACTION_LINES_SPLIT set quantity = ? where id = ?",
                            new Object[]{quantity, location.transLineSplitId});
                }
            }
        }
        db.close();
        return true;
    }

    @Override
    public String uploadBusinessDataOffline(List<ResultEntity> params) {
        return null;
    }
}
