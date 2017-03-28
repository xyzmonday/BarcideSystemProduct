package com.richfit.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.richfit.common_lib.utils.CommonUtil;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.L;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.BizFragmentConfig;
import com.richfit.domain.bean.ImageEntity;
import com.richfit.domain.bean.InvEntity;
import com.richfit.domain.bean.InventoryEntity;
import com.richfit.domain.bean.MenuNode;
import com.richfit.domain.bean.ReferenceEntity;
import com.richfit.domain.bean.ResultEntity;
import com.richfit.domain.bean.RowConfig;
import com.richfit.domain.bean.SimpleEntity;
import com.richfit.domain.bean.UserEntity;
import com.richfit.domain.bean.WorkEntity;
import com.richfit.domain.repository.ILocalDataDao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

/**
 * 从本次缓存的构架来说，可以让BaseDao实现IRepository接口，
 * 但是这样不同业务的Dao层相应的方法将多余很多。权衡利弊这里
 * 不实现IRepository接口。
 * Created by monday on 2016/11/8.
 */

public class BaseDao implements ILocalDataDao {

    protected final static String PAuthOrgKey = "P_AUTH_ORG";
    protected final static String PAuthOrg2Key = "P_AUTH_ORG2";


    protected BCSSQLiteHelper mSqliteHelper;

    @Inject
    public BaseDao(Context context) {
        this.mSqliteHelper = BCSSQLiteHelper.getInstance(context);
    }

    protected SQLiteDatabase getReadableDB() {
        return mSqliteHelper.getReadableDatabase();
    }

    protected SQLiteDatabase getWritableDB() {
        return mSqliteHelper.getWritableDatabase();
    }

    /**
     * 保存额外字段信息。如果服务器返回的扩展字段超出了对应的表的列，那么先过滤掉它们。
     * 这里仅仅是防止服务的错误导致app崩溃做的检查。
     *
     * @param db
     * @param extraMap：扩展字段数据集合
     * @param refCodeId：单据抬头id
     * @param refLineId：单据行id
     * @param locationId：仓位id
     * @param configType：配置类型
     */
    protected void saveExtraMap(SQLiteDatabase db, Map<String, Object> extraMap,
                                String refCodeId, String refLineId, String locationId, String configType) {
        //如果没有额外字段
        if (extraMap == null || extraMap.size() == 0)
            return;

        StringBuffer sb = new StringBuffer();
        Set<String> keys = extraMap.keySet();
        //插入抬头的额外字段
        switch (configType) {
            case Global.HEADER_CONFIG_TYPE:
                if (!TextUtils.isEmpty(refCodeId)) {
                    Set<String> extraHeadKeys = getTableInfo(db, "T_EXTRA_HEADER");
                    //如果Set集合包含参数c指定的集合对象中的所有内容
                    if (extraHeadKeys.containsAll(keys)) {
                        sb.append("INSERT OR REPLACE INTO T_EXTRA_HEADER ");
                        sb.append("(id,config_type");
                        sb.append(createSql(keys));
                        db.execSQL(sb.toString(), createInsertObjs(extraMap, refCodeId, configType));
                    }
                }
                break;
            case Global.COLLECT_CONFIG_TYPE:
                if (!TextUtils.isEmpty(refLineId)) {
                    Set<String> extraHeadKeys = getTableInfo(db, "T_EXTRA_LINE");
                    //如果Set集合包含参数c指定的集合对象中的所有内容
                    if (extraHeadKeys.containsAll(keys)) {
                        sb.append("INSERT OR REPLACE INTO T_EXTRA_LINE ");
                        sb.append("(id,ref_code_id,config_type");
                        sb.append(createSql(keys));
                        db.execSQL(sb.toString(), createInsertObjs(extraMap, refLineId, refCodeId, configType));
                    }
                }
                break;
            case Global.LOCATION_CONFIG_TYPE:
                if (!TextUtils.isEmpty(locationId)) {
                    Set<String> extraHeadKeys = getTableInfo(db, "T_EXTRA_CW");
                    //如果Set集合包含参数c指定的集合对象中的所有内容
                    if (extraHeadKeys.containsAll(keys)) {
                        //保存第三级的额外字段数据
                        sb.append("INSERT OR REPLACE INTO T_EXTRA_CW ");
                        sb.append("(id,ref_line_id,config_type");
                        sb.append(createSql(keys));
                        db.execSQL(sb.toString(), createInsertObjs(extraMap, locationId, refLineId, configType));
                    }
                    break;
                }
                sb.setLength(0);
        }
    }

    private String createSql(Set<String> keys) {
        StringBuffer sb = new StringBuffer();
        for (String key : keys) {
            sb.append(",").append(key);
        }
        sb.append(") ");
        sb.append("VALUES(?,?,?");
        for (int i = 0, size = keys.size(); i < size; i++) {
            sb.append(",").append("?");
        }
        sb.append(")");
        return sb.toString();
    }

    private Object[] createInsertObjs(Map<String, Object> extraMap, String... fixElements) {
        ArrayList<Object> list = new ArrayList<>();
        list.addAll(Arrays.asList(fixElements));
        Iterator<Map.Entry<String, Object>> iterator = extraMap.entrySet().iterator();
        while (iterator.hasNext()) {
            list.add(iterator.next().getValue());
        }
        return list.toArray();
    }

    /**
     * 保存缓存的额外的字段信息
     *
     * @param db：数据库
     * @param extraMap：需要缓存的额外字段数据源
     * @param configType：保存的额外字段类型
     * @param transId：单据抬头缓存表的主键id
     * @param transLineId：单据行明细的缓存表的主键id
     * @param transLocationId：仓位缓存表的主键id
     */
    protected void saveTransExtraMap(SQLiteDatabase db, Map<String, Object> extraMap, String configType,
                                     String transId, String transLineId, String transLocationId) {
        if (extraMap == null || extraMap.size() == 0)
            return;
        StringBuffer sb = new StringBuffer();
        Set<String> keys = extraMap.keySet();
        switch (configType) {
            case Global.HEADER_CONFIG_TYPE:
                if (!TextUtils.isEmpty(transId)) {
                    Set<String> extraHeadKeys = getTableInfo(db, "T_EXTRA_HEADER");
                    //如果Set集合包含参数c指定的集合对象中的所有内容
                    if (extraHeadKeys.containsAll(keys)) {
                        sb.append("INSERT OR REPLACE INTO T_EXTRA_HEADER ");
                        sb.append("(id,config_type");
                        sb.append(createSql(keys));
                        db.execSQL(sb.toString(), createInsertObjs(extraMap, transId, configType));
                    }
                }
                break;
            case Global.COLLECT_CONFIG_TYPE:
                if (!TextUtils.isEmpty(transLineId)) {
                    Set<String> extraHeadKeys = getTableInfo(db, "T_EXTRA_LINE");
                    //如果Set集合包含参数c指定的集合对象中的所有内容
                    if (extraHeadKeys.containsAll(keys)) {
                        sb.append("INSERT OR REPLACE INTO T_EXTRA_LINE ");
                        sb.append("(id,ref_code_id,config_type");
                        sb.append(createSql(keys));
                        db.execSQL(sb.toString(), createInsertObjs(extraMap, transLineId, transId, configType));
                    }
                }
                break;
            case Global.LOCATION_CONFIG_TYPE:
                if (!TextUtils.isEmpty(transLocationId)) {
                    Set<String> extraHeadKeys = getTableInfo(db, "T_EXTRA_CW");
                    //如果Set集合包含参数c指定的集合对象中的所有内容
                    if (extraHeadKeys.containsAll(keys)) {
                        //保存第三级的额外字段数据
                        sb.append("INSERT OR REPLACE INTO T_EXTRA_CW ");
                        sb.append("(id,ref_line_id,config_type");
                        sb.append(createSql(keys));
                        db.execSQL(sb.toString(), createInsertObjs(extraMap, transLocationId, transLineId, configType));
                    }
                    break;
                }
                sb.setLength(0);
        }
    }

    /**
     * 读取额外字段信息。这里表中某一行的所有列的数据。注意由于id和config_type也同时
     * 被读入了map中，所以配置信息不应该有以id和config_type作为propertyCode的扩展
     * 字段。
     *
     * @param db
     * @param refCodeId：抬头id
     * @param refLineId：行id
     * @param LocationId：仓位id
     * @return
     */
    protected Map<String, Object> readExtraMap(SQLiteDatabase db, String configType, String refCodeId,
                                               String refLineId, String LocationId) {
        Map<String, Object> extraMap = new HashMap<>();
        Cursor cursor = null;
        switch (configType) {
            case Global.HEADER_CONFIG_TYPE:
                if (!TextUtils.isEmpty(refCodeId)) {
                    //读取抬头额外信息
                    cursor = db.rawQuery("SELECT * FROM T_EXTRA_HEADER WHERE id = ?", new String[]{refCodeId});
                }
                break;
            case Global.COLLECT_CONFIG_TYPE:
                if (!TextUtils.isEmpty(refLineId)) {
                    cursor = db.rawQuery("SELECT * FROM T_EXTRA_LINE WHERE id = ?", new String[]{refLineId});
                }
                break;
            case Global.LOCATION_CONFIG_TYPE:
                if (!TextUtils.isEmpty(LocationId)) {
                    cursor = db.rawQuery("SELECT * FROM T_EXTRA_CW WHERE id = ?", new String[]{LocationId});
                }
                break;
        }
        if (cursor == null)
            return extraMap;

        while (cursor.moveToNext()) {
            String[] columnNames = cursor.getColumnNames();
            for (String columnName : columnNames) {
                Object obj = cursor.getString(cursor.getColumnIndex(columnName));
                if (obj != null) {
                    extraMap.put(columnName, obj);
                }
            }
        }
        cursor.close();
        return extraMap;
    }

    /**
     * 读取已经缓存的额外字段
     *
     * @param db：数据库
     * @param configType：额外字段配置类型
     * @param transId：单据抬头缓存表的主键id
     * @param transLineId：单据行明细的缓存表的主键id
     * @param transLocationId：仓位缓存表的主键id
     * @return
     */
    protected Map<String, Object> readTransExtraMap(SQLiteDatabase db, String configType, String transId,
                                                    String transLineId, String transLocationId) {
        Map<String, Object> extraMap = new HashMap<>();
        Cursor cursor = null;
        switch (configType) {
            case Global.HEADER_CONFIG_TYPE:
                if (!TextUtils.isEmpty(transId)) {
                    //读取抬头额外信息
                    cursor = db.rawQuery("SELECT * FROM T_TRANSACTION_EXTRA_HEADER WHERE id = ?"
                            , new String[]{transId});
                }
                break;
            case Global.COLLECT_CONFIG_TYPE:
                if (!TextUtils.isEmpty(transLineId)) {
                    cursor = db.rawQuery("SELECT * FROM T_TRANSACTION_EXTRA_LINE WHERE id = ?"
                            , new String[]{transLineId});
                }
                break;
            case Global.LOCATION_CONFIG_TYPE:
                if (!TextUtils.isEmpty(transLocationId)) {
                    cursor = db.rawQuery("SELECT * FROM T_TRANSACTION_EXTRA_CW WHERE id = ?"
                            , new String[]{transLineId});
                }
                break;
        }
        if (cursor == null)
            return extraMap;

        while (cursor.moveToNext()) {
            String[] columnNames = cursor.getColumnNames();
            for (String columnName : columnNames) {
                Object obj = cursor.getString(cursor.getColumnIndex(columnName));
                if (obj != null) {
                    extraMap.put(columnName, obj);
                }
            }
        }

        cursor.close();
        return extraMap;
    }

    /**
     * 读取扩展字段的配置信息
     *
     * @param companyId：公司Id
     * @param refType：单据类型
     * @param bizType：子功能模块编码
     * @param configType：配置类型
     * @return
     */
    protected synchronized ArrayList<RowConfig> readExtraConfigInfo(SQLiteDatabase db, String refType, String companyId,
                                                                    String bizType, String configType) {
        ArrayList<RowConfig> configs = new ArrayList<>();

        if (TextUtils.isEmpty(bizType)) {
            return configs;
        }

        StringBuffer sb = new StringBuffer();
        sb.append("select id,property_name,property_code,");
        sb.append("display_flag,input_flag,company_id,");
        sb.append("biz_type,ref_type,");
        sb.append("config_type,ui_type,col_num,col_name,data_source ");
        sb.append(" from T_CONFIG ");
        Cursor cursor;
        if (!TextUtils.isEmpty(refType)) {
            sb.append("where  biz_type = ? and ref_type = ? and config_type = ?");
            cursor = db.rawQuery(sb.toString(), new String[]{bizType, refType, configType});
        } else {
            sb.append("where  biz_type = ? and config_type = ?");
            cursor = db.rawQuery(sb.toString(), new String[]{bizType, configType});
        }
//        Log.e("yff","配置信息的sql = " + sb.toString() + "; bizType = " + bizType + "; refType = " + refType + "; configType = " + configType);
        while (cursor.moveToNext()) {
            RowConfig config = new RowConfig();
            config.id = cursor.getString(0);
            config.propertyName = cursor.getString(1);
            config.propertyCode = cursor.getString(2);
            config.displayFlag = cursor.getString(3);
            config.inputFlag = cursor.getString(4);
            config.companyId = cursor.getString(5);
            config.businessType = cursor.getString(6);
            config.refType = cursor.getString(7);
            config.configType = cursor.getString(8);
            config.uiType = cursor.getString(9);
            config.colNum = cursor.getString(10);
            config.colName = cursor.getString(11);
            config.dataSource = cursor.getString(12);

            configs.add(config);
        }
        sb.setLength(0);
        cursor.close();
        return configs;
    }

    /**
     * 获取表的列信息
     *
     * @param db
     * @param tableName：目标表
     * @return
     */
    protected Set<String> getTableInfo(SQLiteDatabase db, String tableName) {
        Set<String> columns = new HashSet<>();
        StringBuffer sb = new StringBuffer();
        sb.append("PRAGMA table_info");
        sb.append("([");
        sb.append(tableName);
        sb.append("])");
        Cursor columnCursor = db.rawQuery(sb.toString(), null);
        while (columnCursor.moveToNext()) {
            columns.add(columnCursor.getString(columnCursor.getColumnIndex("name")));
        }
        columnCursor.close();
        sb.setLength(0);
        return columns;
    }

    /**
     * 保存单抬头的sql
     *
     * @param bizType
     * @param refType
     * @return
     */
    protected String createSqlForWriteHeader(String bizType, String refType) {
        StringBuffer sb = new StringBuffer();
        sb.append("insert or replace into mtl_po_headers ");

        switch (bizType) {
            //103-出库
            case "12":
                sb.append("(id,po_num,po_date,doc_people,")
                        .append("supplier_code,supplier_desc,po_org,zd_flag,type,status) ")
                        .append(" values(?,?,?,?,?,?,?,?,?,?)");
                break;
        }
        return sb.toString();
    }

    /**
     * 封装保存明细的sql
     *
     * @param bizType
     * @param refType
     * @return
     */
    protected String createSqlForWriteDetail(String bizType, String refType) {
        StringBuffer sb = new StringBuffer();
        sb.append("insert or replace into mtl_po_lines ");
        switch (bizType) {
            case "12":
                sb.append("(id,po_id,biz_type,ref_type,line_num,work_id,material_id,")
                        .append("material_num,material_desc,material_group,unit,act_quantity,order_quantity)")
                        .append(" values(?,?,?,?,?,?,?,?,?,?,?,?,?) ");
                break;
        }
        return sb.toString();
    }

    /**
     * 读取单据抬头的sql
     *
     * @param bizType
     * @param refType
     * @return
     */
    protected String createSqlForReadHeader(String bizType, String refType) {
        StringBuffer sb = new StringBuffer();
        //输出的字段信息
        sb.append("select ");
        switch (bizType) {
            //103-出库
            case "12":
                sb.append(" po.id,po.po_num,po.supplier_code,po.supplier_desc,")
                        .append("po.doc_people,po.work_id,")
                        .append("w.org_code as work_code,w.org_name as work_name ");

                break;
        }
        //查询的表
        sb.append("from MTL_PO_HEADERS po left join P_AUTH_ORG w on ")
                .append("po.work_id = w.org_id where po.po_num = ?");
        return sb.toString();
    }

    /**
     * 读取明单据细数据的sql
     *
     * @return
     */
    protected String createSqlForReadDetail(String bizType, String refType) {
        StringBuffer sb = new StringBuffer();
        sb.append("select ");
        //输出的字段
        switch (bizType) {
            case "00":
                sb.append("L.work_id,WORG.org_code as work_code,WORG.org_name as work_name,")
                        .append("L.inv_id,IORG.org_code as inv_code,IORG.org_name as inv_name,");
                break;
            case "01":
                sb.append("L.work_id,WORG.org_code as work_code ,WORG.org_name as work_name,")
                        .append("L.inv_id,IORG.org_code as inv_code ,IORG.org_name as inv_name,")
                        .append("L.act_quantity,");
                break;
            case "38":
                sb.append("L.send_work_id as work_id,SWORG.org_code as work_code,SWORG.org_name as work_name,")
                        .append("L.send_inv_id as inv_id,SIORG.org_code as inv_code,SIORG.org_name as inv_name,")
                        .append("L.act_quantity,");
                break;
            case "311":
                sb.append("L.work_id,WORG.org_code as work_code,WORG.org_name as work_name,")
                        .append("L.inv_id,IORG.org_code as inv_code,IORG.org_name as inv_name,")
                        .append("L.act_quantity,");
                break;
            case "11":
                sb.append("L.work_id,WORG.org_code as work_code,WORG.org_name as work_name,")
                        .append("L.inv_id,IORG.org_code as inv_code,IORG.org_name as inv_name,")
                        .append("L.act_quantity,");
                break;
            case "12":
                sb.append("L.work_id,WORG.org_code as work_code,WORG.org_name as work_name,")
                        .append("L.inv_id,IORG.org_code as inv_code,IORG.org_name as inv_name,")
                        .append("L.act_quantity,");
                break;
            case "13":
                sb.append("L.work_id,WORG.org_code as work_code,WORG.org_name as work_name,")
                        .append("L.inv_id,IORG.org_code as inv_code,IORG.org_name as inv_name,")
                        .append("H.ref_mat_doc,H.ref_mat_doc_item,")
                        .append("H.ref_mat_doc ")
                        .append(" || ").append("'_'").append(" || ").append("H.ref_mat_doc_item")
                        .append(" as line_num_105")
                        .append("H.act_quantity as act_quantity");
                break;
            case "110":
                sb.append("L.work_id,WORG.org_code as work_code,WORG.org_name as work_name,")
                        .append("L.inv_id,IORG.org_code as inv_code,IORG.org_name as inv_name,")
                        .append("INSR.ins_lot,INSR.mat_doc as ref_mat_doc,")
                        .append("INSR.mat_doc_item as ref_mat_doc_item")
                        .append("INSR.ins_lot_quantity,")
                        .append("INSR.qualified_quantity as act_quantity")
                        .append("INSR.unquanlified_quantity,INSR.unqalified_quantity as return_quantity");
                break;
            case "19":
                sb.append("L.work_id,WORG.org_code as work_code,WORG.org_name as work_name,")
                        .append("L.inv_id,IORG.org_code as inv_code,IORG.org_name as inv_name,")
                        .append("L.act_quantity,").append("L.line_type");
                break;
            case "19_ZJ":
                sb.append("MRL.reservation_num as ref_mat_doc,MRL.line_num as ref_mat_doc_item,")
                        .append("MRL.work_id as work_id,")
                        .append("RWORG.org_code as work_code")
                        .append("RWORG.org_name as work_name")
                        .append("RIORG.org_code as inv_code")
                        .append("RIORG.org_name as org_name")
                        .append("MRL.material_id ,RM.material_num,RM.material_desc,RM.material_group")
                        .append("MRL.order_quantity,MRL.order_quantity as act_quantity");
                break;
            case "23":
                sb.append("MRL.work_id as work_id ,RWORG.org_code as work_code ,")
                        .append("MRL.inv_id as inv_id,MRL.org_name as inv_name")
                        .append("MRL.material_id ,RM.material_num,RM.material_desc,RM.material_group")
                        .append("MRL.order_quantity,MRL.order_quantity as act_quantity")
                        .append("MRL.last_flag,");
                break;
            case "45":
                sb.append("L.send_work_id as work_id , SWORG.org_code as org_code")
                        .append("SWORG.org_name as org_name ,L.send_inv_id as inv_id,")
                        .append("SIORG.org_code as inv_code ,SIORG.org_name as inv_name,")
                        .append("L.act_quantity");
                break;
            case "51":
                sb.append("L.work_id,WORG.org_code as work_code,")
                        .append("WORG.org_name as work_name")
                        .append("IORG.org_code as inv_code,")
                        .append("IORG.org_name as inv_name,")
                        .append("L.act_quantity,")
                        .append("L.is_return,L.line_type");
                break;
        }
        //查询的表
        sb.append(" L.id, l.line_num,  L.material_id, L.material_num,  L.material_desc,  L.material_group,")
                .append(" L.unit,L.order_quantity, L.qm_flag ")
                .append(" from mtl_po_lines L  left join p_auth_org WORG ")
                .append(" on L.work_id = WORG.org_id left join p_auth_org IORG ")
                .append(" on L.inv_id = IORG.org_id left join p_auth_org SWORG ")
                .append(" on L.send_work_id = SWORG.org_id  left join p_auth_org SIORG ")
                .append(" on L.send_inv_id = SIORG.org_id ");

        switch (bizType) {
            case "01":
                sb.append(" left join mtl_po_lines_custom CUSTOM ")
                        .append(" on L.id = CUSTOM.po_line_id");
                break;
            case "13":
                sb.append(", MTL_PO_HISTORY H ");
                break;
            case "110":
                sb.append(", MTL_INSPECTION_RESULT INSR ");
                break;
            case "23":
                sb.append(", MTL_RESERVATION_LINES MRL left join p_auth_org RWORG on MRL.work_id = RWORG.id ")
                        .append("left join p_auth_org RIORG on MRL.inv_id = RIORG.id")
                        .append("left join BASE_MATERIAL_CODE RM on MRL.material_id = RM.id");
                break;
            case "19_ZJ":
                sb.append(", MTL_RESERVATION_LINES MRL left join p_auth_org RWORG on MRL.work_id = RWORG.id ")
                        .append("left join p_auth_org RIORG on MRL.inv_id = RIORG.id")
                        .append("left join BASE_MATERIAL_CODE RM on MRL.material_id = RM.id ");
                break;


        }

        //查询条件
        sb.append(" WHERE 1 = 1 ");

        switch (bizType) {
            case "13":
                sb.append("and L.id = H.po_line_id");
                break;
            case "110":
                sb.append("and  L.id = INSR.po_line_id");
                break;
            case "23":
                sb.append("and L.id = MRL.po_line_id");
                break;
            case "19_ZJ":
                sb.append("and L.id = MRL.po_line_id");
        }

        //抬头id和行id
        sb.append("and L.po_id = ? and L.biz_type = ? order by L.line_num");
        L.e("读取明细sql = " + sb.toString());
        return sb.toString();
    }


    /**
     * 读取单条缓存数据抬头的sql
     *
     * @param bizType
     * @param refType
     * @return
     */
    protected String createSqlForReadHeaderTransSingle(String bizType, String refType) {
        StringBuffer sb = new StringBuffer();
        sb.append("select ");
        switch (bizType) {
            case "12":
                sb.append(" H.id,H.ref_code_id,H.voucher_date ");
                break;
        }
        sb.append(" from MTL_TRANSACTION_HEADERS H ");
        sb.append("where H.ref_code_id = ? ");

        //如果是无参考的需要给出userId
        if (TextUtils.isEmpty(refType)) {
            sb.append(" and H.created_by = ?");
        }
        L.e("读取单条缓存的sql = " + sb.toString());
        return sb.toString();
    }

    /**
     * 读取单条缓存数据明细的sql
     *
     * @param bizType
     * @param refType
     * @return
     */
    protected String createSqlForReadDetailTransSingle(String bizType, String refType) {
        StringBuffer sb = new StringBuffer();
        sb.append("select ");
        switch (bizType) {
            case "12":
                sb.append(" WORG.org_id as work_id , WORG.org_code as work_code ,WORG.org_name as work_name, ")
                        .append(" IORG.org_id as inv_id , IORG.org_code as inv_code ,IORG.org_name as inv_name,")
                        .append(" L.id,L.trans_id,L.ref_line_id ,L.line_num,L.material_id,L.quantity ");
                break;
        }
        sb.append(" from MTL_TRANSACTION_LINES L left join p_auth_org WORG ")
                .append(" on L.work_id = WORG.org_id ")
                .append(" left join p_auth_org IORG ")
                .append(" on L.inv_id = IORG.org_id ");

        sb.append(" where L.trans_id = ? ");

        L.e("读取缓存行的 sql = " + sb.toString());
        return sb.toString();
    }

    /**
     * 封装单条缓存仓位级数据的sql
     *
     * @param bizType
     * @param refType
     * @return
     */
    protected String createSqlForReadLocTransSingle(String bizType, String refType) {
        StringBuffer sb = new StringBuffer();
        sb.append("select L.id ,L.location,L.batch_num,L.quantity,L.rec_location,L.rec_batch_num ")
                .append(" from MTL_TRANSACTION_LINES_LOCATION L ")
                .append(" where L.trans_id = ? and L.trans_line_id = ?");

        L.e("读取单条缓存的sql = " + sb.toString());
        return sb.toString();
    }

    /**
     * 保存单条缓存抬头级别的sql
     *
     * @param bizType
     * @param refType
     * @return
     */
    protected String createSqlForWriteHeaderTransSingle(String bizType, String refType) {
        StringBuffer sb = new StringBuffer();
        sb.append("select id from ");
        switch (bizType) {
            case "12":
                sb.append("mtl_transaction_headers ");
                break;
        }
        sb.append(" where ref_code_id = ? ");
        L.e("保存缓存抬头sql = " + sb.toString());
        return sb.toString();
    }

    /**
     * 保存单条缓存明细sql
     *
     * @param bizType
     * @param refType
     * @return
     */
    protected String createSqlForWriteDetailTransSingle(String bizType, String refType) {
        StringBuffer sb = new StringBuffer();
        sb.append("select id from ");
        switch (bizType) {
            case "12":
                sb.append(" mtl_transaction_lines ");

                break;
        }
        switch (bizType) {
            case "12":
                sb.append(" where trans_id = ? and ref_line_id = ?");
                break;
        }
        L.e("保存缓存明细sql = " + sb.toString());
        return sb.toString();
    }

    /**
     * 保存单条缓存仓位级sql
     *
     * @param bizType
     * @param refType
     * @return
     */
    protected String createSqlForWriteLocTransSingle(String bizType, String refType) {
        StringBuffer sb = new StringBuffer();
        sb.append("select id from ");
        switch (bizType) {
            case "12":
                sb.append(" mtl_transaction_lines_location ");
                break;
        }
        sb.append(" where trans_id = ? and trans_line_id = ?");
        L.e("保存缓存仓位sql = " + sb.toString());
        return sb.toString();
    }

    /**
     * 读取整单缓存的抬头
     *
     * @param bizType
     * @param refType
     * @return
     */
    protected String createSqlForReadHeaderTrans(String bizType, String refType) {
        StringBuffer sb = new StringBuffer();
        sb.append("select H.id as trans_id ,H.ref_code_id,H.voucher_date ")
                .append(" from mtl_transaction_headers H ");

        switch (bizType) {
            case "12":
                sb.append(" where  H.ref_code_id = ? ");
                break;
        }
        //对于无参考的需要用工厂，库存地点，用户名进行查询
        L.e("读取整单缓存抬头sql = " + sb.toString());
        return sb.toString();
    }

    protected String createSqlForReadDetailTrans(String bizType, String refType) {
        StringBuffer sb = new StringBuffer();

        sb.append("select  WORG.org_id as work_id ,WORG.org_code as work_code,WORG.org_name as work_name, ")
                .append("  IORG.org_id as inv_id ,IORG.org_code as inv_code ,IORG.org_name as inv_name,")
                .append("  L.id,L.trans_id,L.ref_line_id,L.material_id ,")
                .append("  L.material_group,L.material_desc,L.unit,L.quantity as total_quantity")
                .append("  from mtl_transaction_lines L left join p_auth_org WORG on L.work_id = WORG.org_id ")
                .append("  left joint p_auth_org IORG on L.inv_id = IORG.org_id ")
                .append("  where L.trans_id = ? ");

        L.e("读取整单缓存明细行sql = " + sb.toString());
        return sb.toString();
    }

    protected String createSqlForReadLocTrans(String bizType, String refType) {
        StringBuffer sb = new StringBuffer();
        sb.append("select L.id ,L.trans_id,L.trans_line_id,L.quantity");

        Log.e("yff", "读取整单缓存sql = " + sb.toString());
        return sb.toString();
    }

    @Override
    public ReferenceEntity getReference(String refNum, String refType, String bizType, String moveType, String refLineId, String userId) {
        return null;
    }

    @Override
    public void saveReferenceInfo(ReferenceEntity refData, String bizType, String refType) {

    }

    @Override
    public boolean deleteCollectionData(String refNum, String transId, String refCodeId, String refType, String bizType, String userId, String companyCode) {
        return false;
    }

    @Override
    public ReferenceEntity getTransferInfo(String recordNum, String refCodeId, String bizType, String refType, String userId, String workId, String invId, String recWorkId, String recInvId) {
        return null;
    }

    @Override
    public ReferenceEntity getTransferInfoSingle(String refCodeId, String refType, String bizType, String refLineId, String workId, String invId, String recWorkId, String recInvId, String materialNum, String batchFlag, String location, String refDoc, int refDocItem, String userId) {
        return null;
    }

    @Override
    public String deleteCollectionDataSingle(String lineDeleteFlag, String transId, String transLineId, String locationId, String refType, String bizType, String refLineId, String userId, int position, String companyCode) {
        return null;
    }

    /**
     * 保存单条数据
     *
     * @param result
     * @return
     */
    @Override
    public boolean uploadCollectionDataSingle(ResultEntity result) {
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
        StringBuffer sb = new StringBuffer();
        SQLiteDatabase db = getWritableDB();
        String transId = "";

        //定义一个匹配条件的容器
        List<String> selectionsList = new ArrayList<>();
        String[] selections;

        //1.获取抬头id是否存在，如果存在则跟新，否者新增一条
        sb.append("select id from mtl_transaction_headers H where H.biz_type = ? ");
        selectionsList.add(param.businessType);
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
                selectionsList.add(param.createdBy);
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
            cv.put("voucher_date", param.voucherDate);
            cv.put("created_by", param.userId);
            cv.put("move_type", param.moveType);
            cv.put("supplier_id", param.supplierId);
            cv.put("supplier_code", param.supplierNum);
            cv.put("created_by", param.createdBy);
            cv.put("creation_date", UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1));
            long iResult = db.insert("mtl_transaction_headers", null, cv);
            if (iResult < 1) {
                return "";
            }
        } else {
            //修改
            cv = new ContentValues();
            cv.put("id", transId);
            cv.put("created_by", param.createdBy);
            cv.put("creation_date", UiUtil.getCurrentDate(Global.GLOBAL_DATE_PATTERN_TYPE1));
            int iResult = db.update("mtl_transaction_headers", cv, "id = ?", new String[]{transId});
            if (iResult < 1) {
                return "";
            }
        }
        return transId;

    }

    /**
     * 保存单条数据的明细部分
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
        StringBuffer sb = new StringBuffer();
        SQLiteDatabase db = getWritableDB();
        String[] selections ;
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
            sb.append(" and L.ref_doc = ? and L.ref_doc_item = ?");
            selectionList.add(param.refDoc);
            selectionList.add(CommonUtil.valueOf(param.refDocItem));
        }
        if (prodGroup) {
            // 产成品需要去除参考单据号
            sb.append(" and L.ref_doc is null");
        }

        selections = new String[selectionList.size()];
        selectionList.toArray(selections);

        Cursor cursor = db.rawQuery(sb.toString(), selections);
        while (cursor.moveToNext()){
            transLineId = cursor.getString(0);
        }
        sb.setLength(0);
        cursor.close();





        db.close();
        return transLineId;
    }


    @Override
    public ReferenceEntity getCheckInfo(String userId, String bizType, String checkLevel, String checkSpecial, String storageNum, String workId, String invId, String checkNum) {
        return null;
    }

    @Override
    public boolean deleteCheckData(String storageNum, String workId, String invId, String checkId, String userId, String bizType) {
        return false;
    }

    @Override
    public List<InventoryEntity> getCheckTransferInfoSingle(String checkId, String materialId, String materialNum, String location, String bizType) {
        return null;
    }

    @Override
    public ReferenceEntity getCheckTransferInfo(String checkId, String materialNum, String location, String isPageQuery, int pageNum, int pageSize, String bizType) {
        return null;
    }

    @Override
    public boolean deleteCheckDataSingle(String checkId, String checkLineId, String userId, String bizType) {
        return false;
    }

    @Override
    public ArrayList<String> readUserInfo(String userName, String password) {
        return null;
    }

    @Override
    public void saveUserInfo(UserEntity userEntity) {

    }

    @Override
    public void saveExtraConfigInfo(List<RowConfig> configs) {

    }

    @Override
    public ArrayList<RowConfig> readExtraConfigInfo(String companyCode, String bizType, String refType, String configType) {
        return null;
    }

    @Override
    public Map<String, Object> readExtraDataSourceByDictionary(@NonNull String propertyCode, @NonNull String dictionaryCode) {
        return null;
    }

    @Override
    public String getLoadBasicDataTaskDate(String queryType) {
        return null;
    }

    @Override
    public void saveLoadBasicDataTaskDate(String queryType, String queryDate) {

    }

    @Override
    public int saveBasicData(List<Map<String, Object>> maps) {
        return 0;
    }

    @Override
    public void updateExtraConfigTable(Map<String, Set<String>> map) {

    }

    @Override
    public ArrayList<InvEntity> getInvsByWorkId(String workId, int flag) {
        return null;
    }

    @Override
    public ArrayList<WorkEntity> getWorks(int flag) {
        return null;
    }

    @Override
    public boolean checkWareHouseNum(String sendWorkId, String sendInvCode, String recWorkId, String recInvCode, int flag) {
        return false;
    }

    @Override
    public ArrayList<SimpleEntity> getSupplierList(String workCode, String keyWord, int defaultItemNum, int flag) {
        return null;
    }

    @Override
    public ArrayList<SimpleEntity> getCostCenterList(String workCode, String keyWord, int defaultItemNum, int flag) {
        return null;
    }

    @Override
    public ArrayList<SimpleEntity> getProjectNumList(String workCode, String keyWord, int defaultItemNum, int flag) {
        return null;
    }

    @Override
    public boolean saveBizFragmentConfig(ArrayList<BizFragmentConfig> bizFragmentConfigs) {
        return false;
    }

    @Override
    public ArrayList<BizFragmentConfig> readBizFragmentConfig(String bizType, String refType, int fragmentType) {
        return null;
    }

    @Override
    public void deleteInspectionImages(String refNum, String refCodeId, boolean isLocal) {

    }

    @Override
    public void deleteInspectionImagesSingle(String refNum, String refLineNum, String refLineId, boolean isLocal) {

    }

    @Override
    public boolean deleteTakedImages(ArrayList<ImageEntity> images, boolean isLocal) {
        return false;
    }

    @Override
    public void saveTakedImages(ArrayList<ImageEntity> images, String refNum, String refLineId, int takePhotoType, String imageDir, boolean isLocal) {

    }

    @Override
    public ArrayList<ImageEntity> readImagesByRefNum(String refNum, boolean isLocal) {
        return null;
    }

    @Override
    public String getStorageNum(String workId, String workCode, String invId, String invCode) {
        return null;
    }

    @Override
    public ArrayList<String> getStorageNumList(int flag) {
        return null;
    }

    @Override
    public void saveMenuInfo(List<MenuNode> menus, String loginId, int mode) {

    }

    @Override
    public ArrayList<MenuNode> readMenuInfo(String loginId, int mode) {
        return null;
    }


}