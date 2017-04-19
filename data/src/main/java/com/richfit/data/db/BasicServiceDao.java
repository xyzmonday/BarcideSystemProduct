package com.richfit.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.utils.CommonUtil;
import com.richfit.common_lib.utils.Global;
import com.richfit.common_lib.utils.L;
import com.richfit.common_lib.utils.UiUtil;
import com.richfit.domain.bean.BizFragmentConfig;
import com.richfit.domain.bean.InvEntity;
import com.richfit.domain.bean.MaterialEntity;
import com.richfit.domain.bean.MenuNode;
import com.richfit.domain.bean.RowConfig;
import com.richfit.domain.bean.SimpleEntity;
import com.richfit.domain.bean.UserEntity;
import com.richfit.domain.bean.WorkEntity;
import com.richfit.domain.repository.IBasicServiceDao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

/**
 * 基础数据Dao层。包括了数据采集需要的基础数据，额外字段配置信息，
 * 用户信息等
 * Created by monday on 2016/11/8.
 */

public class BasicServiceDao extends BaseDao implements IBasicServiceDao {

    @Inject
    public BasicServiceDao(@ContextLife("Application") Context context) {
        super(context);
    }

    public void updateExtraConfigTable(Map<String, Set<String>> map) {
        //尝试更新T_EXTRA_HEADER表
        updateExtraHeader(map.get(Global.HEADER_CONFIG_TYPE), "T_EXTRA_HEADER");
        updateExtraHeader(map.get(Global.COLLECT_CONFIG_TYPE), "T_EXTRA_LINE");
        updateExtraHeader(map.get(Global.LOCATION_CONFIG_TYPE), "T_EXTRA_CW");
        updateExtraHeader(map.get(Global.HEADER_CONFIG_TYPE), "T_TRANSACTION_EXTRA_HEADER");
        updateExtraHeader(map.get(Global.COLLECT_CONFIG_TYPE), "T_TRANSACTION_EXTRA_LINE");
        updateExtraHeader(map.get(Global.LOCATION_CONFIG_TYPE), "T_TRANSACTION_EXTRA_CW");
    }

    private void updateExtraHeader(Set<String> targetColumns, String tableName) {
        if (targetColumns == null || targetColumns.size() == 0)
            return;
        SQLiteDatabase db = getWritableDB();

        StringBuffer sb = new StringBuffer();
        sb.append("SELECT count(*) FROM sqlite_master WHERE type=");
        sb.append("\"table\"");
        sb.append(" AND name = ");
        sb.append("\"");
        sb.append(tableName);
        sb.append(("\""));
        Cursor cursor = db.rawQuery(sb.toString(), null);
        int count = -1;
        while (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        sb.setLength(0);

        if (count > 0) {
            Set<String> columns = getTableInfo(db, tableName);
            //需要插入的列减去已经存在的列
            targetColumns.removeAll(columns);
            if (targetColumns.size() == 0)
                return;
            if (targetColumns.size() > 0) {
                //开始插入列
                for (String column : targetColumns) {
                    sb.append("ALTER TABLE ")
                            .append(tableName)
                            .append(" ADD COLUMN ")
                            .append(column)
                            .append(" TEXT");
                    db.execSQL(sb.toString());
                    sb.setLength(0);
                }
            }
        }
        db.close();
    }

    /**
     * 保存用户的基本信息
     *
     * @param userEntity：用户信息实体类
     */
    public void saveUserInfo(UserEntity userEntity) {
        if (userEntity == null || TextUtils.isEmpty(userEntity.loginId))
            return;
        SQLiteDatabase db = getWritableDB();
        //获取当前的时间
        final long lastLoginDate = System.currentTimeMillis();
        StringBuffer sb = new StringBuffer();
        sb.append("INSERT OR REPLACE INTO T_USER(login_id,user_id,last_login_date,user_name,company_id,company_code,auth_orgs,batch_flag) ");
        sb.append(" values(?,?,?,?,?,?,?,?)");
        db.execSQL(sb.toString(), new Object[]{userEntity.loginId,
                userEntity.userId, lastLoginDate, userEntity.userName, userEntity.companyId,
                userEntity.companyCode, userEntity.authOrgs, userEntity.batchFlag});
        sb.setLength(0);
        db.close();
    }

    /**
     * 读取一周之内登陆的用户历史信息，用于在登陆的时候提示用户
     *
     * @return
     */
    @Override
    public ArrayList<String> readUserInfo(String userName, String password) {
        //注意这里如果用户没有登录过，那么返回一个空数组
        ArrayList<String> userList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {

            final long endLoginDate = System.currentTimeMillis();
            //计算一个星期之内登陆的用户
            final long startLoginDate = endLoginDate - 7 * 24 * 60 * 60 * 1000;
            db = getWritableDB();
            cursor = db.rawQuery("SELECT login_id FROM T_USER WHERE LAST_LOGIN_DATE BETWEEN ? AND ?  ",
                    new String[]{String.valueOf(startLoginDate), String.valueOf(endLoginDate)});

            while (cursor.moveToNext()) {
                userList.add(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return userList;
        } finally {
            if (cursor != null)
                cursor.close();
            db.close();
        }
        return userList;
    }

    /**
     * 离线登陆
     *
     * @param userName
     * @param password
     * @return
     */
    @Override
    public UserEntity login(String userName, String password) {
        SQLiteDatabase db = getWritableDB();
        clearStringBuffer();
        sb.append("select login_id,user_id,user_name,company_id,company_code,auth_orgs,batch_flag ")
                .append("from T_USER where user_name = ?");
        UserEntity user = new UserEntity();
        Cursor cursor = db.rawQuery(sb.toString(), new String[]{userName});

        while (cursor.moveToNext()) {
            user.loginId = cursor.getString(0);
            user.userId = cursor.getString(1);
            user.userName = cursor.getString(2);
            user.companyId = cursor.getString(3);
            user.companyCode = cursor.getString(4);
            user.authOrgs = cursor.getString(5);
            user.batchFlag = cursor.getString(6);
        }
        db.close();
        return user;
    }

    /**
     * 保存配置信息
     *
     * @param configs：配置信息列表
     */
    @Override
    public void saveExtraConfigInfo(List<RowConfig> configs) {
        if (configs == null || configs.size() == 0)
            return;
        SQLiteDatabase db = getWritableDB();
        //先删除数据
        db.delete("T_CONFIG", null, null);
        StringBuffer sb = new StringBuffer();
        for (RowConfig config : configs) {
            sb.append("INSERT INTO T_CONFIG(id,property_name,");
            sb.append("property_code,display_flag,input_flag,");
            sb.append("company_id,biz_type,");
            sb.append("ref_type,config_type,ui_type,col_num,col_name,data_source)");
            sb.append(" values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
            db.execSQL(sb.toString(), new Object[]{config.id, config.propertyName,
                    config.propertyCode, config.displayFlag, config.inputFlag,
                    config.companyId, config.businessType, config.refType, config.configType, config.uiType,
                    config.colNum, config.colName, config.dataSource});
            sb.setLength(0);
        }
        db.close();
    }


    /**
     * 读取配置文件
     *
     * @param companyId：地区公司Id
     * @param bizType：子模块编码
     * @param configType：配置类型
     * @return
     */
    @Override
    public ArrayList<RowConfig> readExtraConfigInfo(String companyId, String bizType, String refType,
                                                    String configType) {
        ArrayList<RowConfig> configs = new ArrayList<>();
        if (TextUtils.isEmpty(bizType)) {
            return configs;
        }
        SQLiteDatabase db = getWritableDB();
        configs = readExtraConfigInfo(db, refType, companyId, bizType, configType);
        db.close();
        return configs;
    }


    /**
     * 获取额外控件对应的字典数据
     *
     * @param dictionaryCode：需要查询的字典编码
     * @return
     */
    @Override
    public Map<String, Object> readExtraDataSourceByDictionary(String propertyCode, String dictionaryCode) {
        Map<String, Object> map = new LinkedHashMap<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            if (TextUtils.isEmpty(dictionaryCode))
                return map;
            db = getWritableDB();
            cursor = db.rawQuery("select val,name from T_EXTRA_DATA_SOURCE where code = ? order by sort asc",
                    new String[]{dictionaryCode});

            while (cursor.moveToNext()) {
                map.put(cursor.getString(0), cursor.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return map;
        } finally {
            if (cursor != null)
                cursor.close();
            db.close();
        }
        return map;
    }


    /**
     * 读取页面配置信息
     *
     * @param bizFragmentConfigs
     * @return
     */
    @Override
    public boolean saveBizFragmentConfig(ArrayList<BizFragmentConfig> bizFragmentConfigs) {
        if (bizFragmentConfigs == null || bizFragmentConfigs.size() == 0) {
            return false;
        }
        SQLiteDatabase db = getWritableDB();
        db.delete("T_FRAGMENT_CONFIGS", null, null);
        ContentValues cv = new ContentValues();
        for (BizFragmentConfig bizFragmentConfig : bizFragmentConfigs) {
            cv.clear();
            cv.put("id", bizFragmentConfig.id);
            cv.put("fragment_tag", bizFragmentConfig.fragmentTag);
            cv.put("fragment_type", bizFragmentConfig.fragmentType);
            cv.put("biz_type", bizFragmentConfig.bizType);
            cv.put("ref_type", bizFragmentConfig.refType);
            cv.put("tab_title", bizFragmentConfig.tabTitle);
            cv.put("mode", bizFragmentConfig.mode);
            cv.put("class_name", bizFragmentConfig.className);
            db.insert("T_FRAGMENT_CONFIGS", null, cv);
        }
        db.close();
        return true;
    }

    /**
     * 获取上次请求基础数据的日期
     *
     * @param queryType:查询基础数据类型，注意只有仓位和物料采用了增量跟新时 我们需要保存上一次用户的请求日期，其他的基础数据只给定
     *                                             当前的日期即可。
     * @return
     */
    @Override
    public String getLoadBasicDataTaskDate(String queryType) {
        SQLiteDatabase db = getWritableDB();

        Cursor cursor = db.rawQuery("select query_date from REQUEST_DATE where query_type = ?",
                new String[]{queryType});

        String date = "";
        while (cursor.moveToNext()) {
            date = cursor.getString(0);
        }
        if (TextUtils.isEmpty(date)) {
            date = "0001/01/01";
        } else if (!TextUtils.isEmpty(date) && "0001/01/01".equals(date)) {
            date = "0001/01/01";
        } else {
            //返回当前的时间
            date = UiUtil.getCurrentDate("yyyy/MM/dd");
        }
        cursor.close();
        db.close();
        return date;
    }

    /**
     * 设置当前基础数据更新的日期
     *
     * @param queryType:查询基础数据类型
     * @param queryDate：查询日期
     */
    @Override
    public void saveLoadBasicDataTaskDate(String queryType, String queryDate) {
        SQLiteDatabase db = getWritableDB();
        db.delete("REQUEST_DATE", "query_type = ?", new String[]{queryType});
        ContentValues cv = new ContentValues();
        cv.put("query_type", queryType);
        cv.put("query_date", queryDate);
        db.insert("REQUEST_DATE", null, cv);
        db.close();
    }

    /**
     * 将服务器获取的基础数据保存到本地
     *
     * @param maps:服务器获取的基本数据源
     * @return
     */
    @Override
    public int saveBasicData(List<Map<String, Object>> maps) {

        //获取基本参数的map
        Map<String, Object> basicParamMap = maps.get(0);

        final String queryType = (String) basicParamMap.get("queryType");

        if ("error".equals(queryType)) {
            return -1;
        }

        final String queryDate = (String) basicParamMap.get("queryDate");
        final boolean isFirstPage = (boolean) basicParamMap.get("isFirstPage");
        final int taskId = (int) basicParamMap.get("taskId");
        int tableIndex = -1;
        if ("CW".equals(queryType)) {
            tableIndex = 0;
        } else if ("ZZ".equals(queryType)) {
            tableIndex = 1;
        } else if ("LZ".equals(queryType)) {
            tableIndex = 2;
        } else if ("CZ".equals(queryType)) {
            tableIndex = 3;
        } else if ("XJD".equals(queryType)) {
            tableIndex = 4;
        } else if ("GYS".equals(queryType)) {
            tableIndex = 5;
        } else if ("SD".equals(queryType)) {
            tableIndex = 6;
        } else if ("ZZ2".equals(queryType)) {
            tableIndex = 7;
        } else if ("XM".equals(queryType)) {
            tableIndex = 8;
        }
        boolean isCWFirst = "0001/01/01".equalsIgnoreCase(queryDate);
        insertData(maps, tableIndex, isFirstPage, isCWFirst);
        return taskId;
    }

    /**
     * 数据保存控制
     *
     * @param source：需要爆粗你的数据源
     * @param tableIndex：数据要保存的本地数据表的索引，由开发者提前定义
     * @param isFirstPage：是否是第一页。对于非增量更新的数据源的分页数据请求，第一页时需要先删除所有的数据
     * @param isCWFirst：对于增量请求的仓位数据，如果是首次请求服务器(业绩说说本次请求获取的不是增量数据)需要删除之前所有的数据
     */
    private void insertData(List<Map<String, Object>> source, int tableIndex, boolean isFirstPage, boolean isCWFirst) {
        if (tableIndex < 0)
            return;
        if (source == null || source.size() == 0)
            return;
        SQLiteDatabase db = getWritableDB();
        String tableName;
        StringBuilder sql = new StringBuilder();
        switch (tableIndex) {
            case 0:
                /*表示第一次请求数据，那么直接插入.
                * 注意CW数据的特殊性在于，它根据日期返回增量数据。所以第一次是全新的数据，
                * 以后的增量数量使用INSERT OR REPLACE */
                tableName = "BASE_LOCATION";
                if (isCWFirst) {
                    db.execSQL("delete from " + tableName);
                }
                sql.append("INSERT OR REPLACE INTO ")
                        .append(tableName)
                        .append(" (id,storage_num,work_id,inv_id,location)")
                        .append(" VALUES (?,?,?,?,?)");

                break;
            case 1:
                tableName = "P_AUTH_ORG";
                db.execSQL("delete from " + tableName);
                sql.append("INSERT INTO ")
                        .append(tableName)
                        .append(" (org_id,org_code,org_name,org_level,parent_id,storage_code) ")
                        .append("VALUES (?,?,?,?,?,?)");
                break;
            case 7:
                tableName = "P_AUTH_ORG2";
                db.execSQL("delete from " + tableName);
                sql.append("INSERT INTO ")
                        .append(tableName)
                        .append(" (org_id,org_code,org_name,org_level,parent_id,storage_code) ")
                        .append("VALUES (?,?,?,?,?,?)");
                break;
            case 2:
                tableName = "BASE_WAREHOUSE_GROUP";
                db.execSQL("delete from " + tableName);
                sql.append("INSERT INTO ")
                        .append(tableName)
                        .append(" (id,group_code,group_desc) VALUES (?,?,?)");
                break;

            case 3:
                tableName = "BASE_COST_CENTER";
                if (isFirstPage) {
                    db.execSQL("delete from " + tableName);
                }
                //注意对于成本中心和供应商，由于是分页加载数据，所有仅仅是在第一页加载的时候需要删除数据。
                sql.append("INSERT INTO ")
                        .append(tableName)
                        .append(" (id,org_id,cost_center_code,cost_center_desc)")
                        .append(" VALUES (?,?,?,?)");

                break;
            case 4:
                tableName = "BASE_INSPECTION_PLACE";
                db.execSQL("delete from " + tableName);
                sql.append("INSERT INTO ")
                        .append(tableName)
                        .append(" (id,code,name)")
                        .append(" VALUES (?,?,?)");
                break;
            case 5:
                tableName = "BASE_SUPPLIER";
                if (isFirstPage) {
                    db.execSQL("delete from " + tableName);
                }
                sql.append("INSERT INTO ")
                        .append(tableName)
                        .append(" (id,org_id,supplier_code,supplier_desc,creation_date,last_update_date)")
                        .append(" VALUES (?,?,?,?,?,?)");
                break;
            case 6:
                tableName = "T_EXTRA_DATA_SOURCE";
                db.execSQL("delete from " + tableName);
                sql.append("INSERT INTO ")
                        .append(tableName)
                        .append(" (id,code,name,sort,val)")
                        .append(" VALUES (?,?,?,?,?)");

                break;
            case 8:
                tableName = "BASE_PROJECT_NUM";
                if (isFirstPage) {
                    db.execSQL("delete from " + tableName);
                }
                //注意对于成本中心和供应商，由于是分页加载数据，所有仅仅是在第一页加载的时候需要删除数据。
                sql.append("INSERT INTO ")
                        .append(tableName)
                        .append(" (id,org_id,project_num_code,project_num_desc)")
                        .append(" VALUES (?,?,?,?)");
                break;

        }
        db.close();
        patchUpdateBaseData(source, 1, source.size(), 0, sql.toString(), tableIndex, isCWFirst);
    }

    /**
     * 批量更新基础数据
     *
     * @param source:数据源
     * @param start:本次批量保存的下边界
     * @param end：本次批量保存的上边界
     * @param ptr：本次保存的页码
     * @param sql：本次保存需要执行的sql语句
     * @param tableIndex：同上
     * @param isCWFirst：同上
     */
    private void patchUpdateBaseData(List<Map<String, Object>> source, int start, int end, int ptr, String sql,
                                     int tableIndex, boolean isCWFirst) {
        SQLiteDatabase db = getWritableDB();
        db.beginTransaction();
        try {
            SQLiteStatement stmt = db.compileStatement(sql);
            Map<String, Object> item;
            //绑定数据
            switch (tableIndex) {
                case 0:
                    //插入数据
                    if (!isCWFirst) {
                        for (int i = start; i < end; i++) {
                            item = source.get(ptr * Global.MAX_PATCH_LENGTH + i);
                            db.execSQL(sql, new Object[]{item.get(Global.ID_KEY),
                                    item.get(Global.STORAGENUM_KEY),
                                    CommonUtil.Obj2String(item.get(Global.WORK_ID)),
                                    CommonUtil.Obj2String(item.get(Global.INV_ID)),
                                    item.get(Global.CODE_KEY)});
                        }
                    } else {
                        //仓位
                        for (int i = start; i < end; i++) {
                            item = source.get(ptr * Global.MAX_PATCH_LENGTH + i);
                            stmt.bindString(1, item.get(Global.ID_KEY).toString());
                            stmt.bindString(2, CommonUtil.Obj2String(item.get(Global.STORAGENUM_KEY)));
                            stmt.bindString(3, CommonUtil.Obj2String(item.get(Global.WORK_ID)));
                            stmt.bindString(4, CommonUtil.Obj2String(item.get(Global.INV_ID)));
                            stmt.bindString(5, CommonUtil.Obj2String(item.get(Global.CODE_KEY)));
                            stmt.execute();
                            stmt.clearBindings();
                        }
                    }
                    break;
                case 7:
                case 1:
                    //组织机构
                    for (int i = start; i < end; i++) {
                        item = source.get(ptr * Global.MAX_PATCH_LENGTH + i);
                        stmt.bindString(1, item.get(Global.ID_KEY).toString());
                        stmt.bindString(2, item.get(Global.CODE_KEY).toString());
                        stmt.bindString(3, item.get(Global.NAME_KEY).toString());
                        stmt.bindString(4, item.get(Global.ORGLEVEL_KEY).toString());
                        stmt.bindString(5, item.get(Global.PARENTID_KEY).toString());
                        stmt.bindString(6, CommonUtil.Obj2String(item.get(Global.STORAGENUM_KEY)));
                        stmt.execute();
                        stmt.clearBindings();
                    }
                    break;
                case 2:
                    //料组
                    for (int i = start; i < end; i++) {
                        item = source.get(ptr * Global.MAX_PATCH_LENGTH + i);
                        stmt.bindString(1, item.get(Global.ID_KEY).toString());
                        stmt.bindString(2, item.get(Global.CODE_KEY).toString());
                        stmt.bindString(3, item.get(Global.NAME_KEY).toString());
                        stmt.execute();
                        stmt.clearBindings();
                    }
                    break;
                case 8:
                case 3:
                    //成本中心
                    for (int i = start; i < end; i++) {
                        item = source.get(ptr * Global.MAX_PATCH_LENGTH + i);
                        stmt.bindString(1, item.get(Global.ID_KEY).toString());
                        stmt.bindString(2, item.get(Global.PARENTID_KEY).toString());
                        stmt.bindString(3, item.get(Global.CODE_KEY).toString());
                        stmt.bindString(4, item.get(Global.NAME_KEY).toString());
                        stmt.execute();
                        stmt.clearBindings();
                    }
                    break;
                case 4:
                    //巡检点
                    for (int i = start; i < end; i++) {
                        item = source.get(ptr * Global.MAX_PATCH_LENGTH + i);
                        stmt.bindString(1, item.get(Global.ID_KEY).toString());
                        stmt.bindString(2, item.get(Global.CODE_KEY).toString());
                        stmt.bindString(3, item.get(Global.NAME_KEY).toString());
                        stmt.execute();
                        stmt.clearBindings();
                    }
                    break;
                case 5:
                    //供应商
                    for (int i = start; i < end; i++) {
                        item = source.get(ptr * Global.MAX_PATCH_LENGTH + i);
                        stmt.bindString(1, item.get(Global.ID_KEY).toString());
                        stmt.bindString(2, item.get(Global.PARENTID_KEY).toString());
                        stmt.bindString(3, item.get(Global.CODE_KEY).toString());
                        stmt.bindString(4, item.get(Global.NAME_KEY).toString());
                        stmt.execute();
                        stmt.clearBindings();
                    }
                    break;
                case 6:
                    //额外字段字典表
                    for (int i = start; i < end; i++) {
                        item = source.get(ptr * Global.MAX_PATCH_LENGTH + i);
                        stmt.bindString(1, item.get(Global.ID_KEY).toString());
                        stmt.bindString(2, item.get(Global.CODE_KEY).toString());
                        stmt.bindString(3, item.get(Global.NAME_KEY).toString());
                        stmt.bindLong(4, Long.valueOf(item.get(Global.SORT_KEY).toString()));
                        stmt.bindString(5, item.get(Global.VALUE_KEY).toString());
                        stmt.execute();
                        stmt.clearBindings();
                    }
                    break;

            }
        } catch (Exception e) {
            L.d("插入基础数据报错 = " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
            source.clear();
            System.gc();
        }
    }

    /**
     * 根据工厂id查询所有的仓库
     *
     * @param workId:工厂id
     */
    @Override
    public ArrayList<InvEntity> getInvsByWorkId(String workId, int flag) {
        ArrayList<InvEntity> datas = new ArrayList<>();
        if (TextUtils.isEmpty(workId)) {
            return datas;
        }
        SQLiteDatabase db = getWritableDB();
        InvEntity tamp = new InvEntity();
        Cursor cursor = null;
        try {
            tamp.invName = "请选择";
            datas.add(tamp);

            final String tableName = flag == 0 ? PAuthOrgKey : PAuthOrg2Key;

            StringBuffer sb = new StringBuffer();
            sb.append("select org_id,org_name,org_code from ")
                    .append(tableName)
                    .append(" where parent_id = ? order by org_code ");
            cursor = db.rawQuery(sb.toString(), new String[]{workId});
            while (cursor.moveToNext()) {
                InvEntity entity = new InvEntity();
                entity.invId = cursor.getString(0);
                entity.invName = cursor.getString(1);
                entity.invCode = cursor.getString(2);
                datas.add(entity);
            }
            sb.setLength(0);
        } catch (Exception e) {
            e.printStackTrace();
            return datas;
        } finally {
            if (cursor != null)
                cursor.close();
            db.close();
        }

        return datas;
    }

    /**
     * 获取工厂列表
     */
    @Override
    public ArrayList<WorkEntity> getWorks(int flag) {
        SQLiteDatabase db = getWritableDB();
        ArrayList<WorkEntity> works = new ArrayList<>();
        Cursor cursor = null;
        WorkEntity data = new WorkEntity();
        try {
            data.workCode = "-1";
            data.workName = "请选择";
            works.add(0, data);
            ArrayList<String> authOrgs = new ArrayList<>();
            if (!TextUtils.isEmpty(Global.AUTH_ORG)) {
                authOrgs.addAll(Arrays.asList(Global.AUTH_ORG.split("\\|")));
            }

            StringBuffer sb = new StringBuffer();
            final String tableName = flag == 0 ? PAuthOrgKey : PAuthOrg2Key;
            sb.append("select org_id,org_name,org_code from ").append(tableName).append(" where org_level = 2 ");
            if (authOrgs.size() > 0 && flag == 0) {
                sb.append("and org_code IN (");
                for (int i = 0; i < authOrgs.size(); i++) {
                    sb.append("'").append(authOrgs.get(i)).append("'").append(i == authOrgs.size() - 1 ? "" : ",");
                }
                sb.append(") ");
            }

            sb.append(" order by org_code");

            cursor = db.rawQuery(sb.toString(), null);
            while (cursor.moveToNext()) {
                WorkEntity item = new WorkEntity();
                item.workId = cursor.getString(0);
                item.workName = cursor.getString(1);
                item.workCode = cursor.getString(2);
                works.add(item);
            }

            sb.setLength(0);
        } catch (Exception e) {
            e.printStackTrace();
            return works;
        } finally {
            if (cursor != null)
                cursor.close();
            db.close();
        }
        return works;
    }


    /**
     * 检查发出库位和接收库位是否同属一个ERP仓库号
     *
     * @param sendWorkId:发出工厂id
     * @param sendInvCode：发出库位编码
     * @param recWorkId：接收工厂id
     * @param recInvCode：接收库存地点编码
     * @return
     */
    @Override
    public boolean checkWareHouseNum(final String sendWorkId, final String sendInvCode,
                                     final String recWorkId, final String recInvCode, int flag) {
        SQLiteDatabase db = getReadableDB();
        try {
            String sendStorageNum = getStorageNum(db, sendWorkId, sendInvCode, flag);
            String recStorageNum = getStorageNum(db, recWorkId, recInvCode, flag);

            //如果两个都不为空
            if (!TextUtils.isEmpty(sendStorageNum) &&
                    !TextUtils.isEmpty(recStorageNum) &&
                    sendStorageNum.equals(recStorageNum)) {
                return true;
            }

            //如果两个都为空
            if (TextUtils.isEmpty(sendStorageNum) &&
                    TextUtils.isEmpty(recStorageNum)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
        return false;
    }

    private String getStorageNum(SQLiteDatabase db, String workId, String invCode, int flag) {
        String storageNum = null;
        final String tableName = flag == 0 ? PAuthOrgKey : PAuthOrg2Key;
        Cursor cursor = db.rawQuery("select storage_code from " + tableName + " where parent_id = ? and org_code = ? and org_level = ?",
                new String[]{workId, invCode, "3"});
        while (cursor.moveToNext()) {
            storageNum = cursor.getString(0);
        }
        cursor.close();
        return storageNum;
    }

    /**
     * 获取供应商列表
     *
     * @param workCode：工厂编码
     * @return
     */
    @Override
    public ArrayList<SimpleEntity> getSupplierList(String workCode, String keyWord, int defaultItemNum, int flag) {
        ArrayList<SimpleEntity> list = new ArrayList<>();
        if (TextUtils.isEmpty(workCode))
            return list;

        SQLiteDatabase db = getReadableDB();
        StringBuffer sb = new StringBuffer();
        String tableName = flag == 0 ? PAuthOrgKey : PAuthOrg2Key;
        Cursor cursor = null;
        try {
            sb.append("select B.org_id , B.supplier_code,B.supplier_desc from ")
                    .append(tableName)
                    .append("  P,BASE_SUPPLIER B ")
                    .append(" where P.parent_id = B.org_id ")
                    .append(" and P.org_level = 2 and P.org_code = ? ");
            if (!TextUtils.isEmpty(keyWord)) {
                sb.append("like ").append("'%").append(keyWord).append("%'");
            } else if (defaultItemNum > 0) {
                sb.append(" limit 0, ")
                        .append(defaultItemNum);
            }
            cursor = db.rawQuery(sb.toString(), new String[]{workCode});
            while (cursor.moveToNext()) {
                SimpleEntity supplierEntity = new SimpleEntity();
                supplierEntity.id = cursor.getString(0);
                supplierEntity.code = cursor.getString(1);
                supplierEntity.name = cursor.getString(2);
                list.add(supplierEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return list;
    }

    /**
     * 获取成本中心列表
     *
     * @param workCode
     * @param keyWord
     * @param defaultItemNum
     * @param flag
     * @return
     */
    @Override
    public ArrayList<SimpleEntity> getCostCenterList(String workCode, String keyWord, int defaultItemNum, int flag) {
        ArrayList<SimpleEntity> list = new ArrayList<>();
        if (TextUtils.isEmpty(workCode))
            return list;

        SQLiteDatabase db = getReadableDB();
        StringBuffer sb = new StringBuffer();
        String tableName = flag == 0 ? PAuthOrgKey : PAuthOrg2Key;
        Cursor cursor = null;
        try {
            sb.append("select B.org_id , B.cost_center_code,B.cost_center_desc from ")
                    .append(tableName)
                    .append("  P,BASE_COST_CENTER B ")
                    .append(" where P.parent_id = B.org_id ")
                    .append(" and P.org_level = 2 and P.org_code = ? ");
            if (!TextUtils.isEmpty(keyWord)) {
                sb.append("like ").append("'%").append(keyWord).append("%'");
            } else if (defaultItemNum > 0) {
                sb.append(" limit 0, ")
                        .append(defaultItemNum);
            }
            cursor = db.rawQuery(sb.toString(), new String[]{workCode});
            while (cursor.moveToNext()) {
                SimpleEntity entity = new SimpleEntity();
                entity.id = cursor.getString(0);
                entity.code = cursor.getString(1);
                entity.name = cursor.getString(2);
                list.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return list;
    }


    /**
     * 获取项目编号
     *
     * @param workCode:工厂编码
     */
    @Override
    public ArrayList<SimpleEntity> getProjectNumList(String workCode, String keyWord, int defaultItemNum, int flag) {
        ArrayList<SimpleEntity> list = new ArrayList<>();
        if (TextUtils.isEmpty(workCode))
            return list;
        SQLiteDatabase db = getReadableDB();
        StringBuffer sb = new StringBuffer();
        String tableName = flag == 0 ? PAuthOrgKey : PAuthOrg2Key;
        Cursor cursor = null;
        try {
            sb.append("select B.org_id , B.project_num_code,B.project_num_desc from ")
                    .append(tableName)
                    .append("  P,BASE_PROJECT_NUM B ")
                    .append(" where P.parent_id = B.org_id ")
                    .append(" and P.org_level = 2 and P.org_code = ? ");
            if (!TextUtils.isEmpty(keyWord)) {
                sb.append("like ").append("'%").append(keyWord).append("%'");
            } else if (defaultItemNum > 0) {
                sb.append(" limit 0, ")
                        .append(defaultItemNum);
            }
            cursor = db.rawQuery(sb.toString(), new String[]{workCode});
            while (cursor.moveToNext()) {
                SimpleEntity entity = new SimpleEntity();
                entity.id = cursor.getString(0);
                entity.code = cursor.getString(1);
                entity.name = cursor.getString(2);
                list.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return list;
    }

    /**
     * 获取Fragment页面的配置信息
     *
     * @param bizType:业务类型
     * @param refType:单据类型
     * @param fragmentType:Fragment类型，1~3分别表示抬头，数据明细，数据采集界面，-1表示明细修改
     * @return
     */
    @Override
    public ArrayList<BizFragmentConfig> readBizFragmentConfig(String bizType, String refType,
                                                              int fragmentType, int mode) {
        ArrayList<BizFragmentConfig> bizFragmentConfigs = new ArrayList<>();
        if (TextUtils.isEmpty(bizType)) {
            return bizFragmentConfigs;
        }
        SQLiteDatabase db = getWritableDB();
        String[] selections;
        ArrayList<String> selectionList = new ArrayList<>();
        clearStringBuffer();
        Cursor cursor = null;
        sb.append("select id,fragment_tag,biz_type,ref_type,tab_title,fragment_type,class_name ")
                .append("from T_FRAGMENT_CONFIGS where biz_type = ? ");
        selectionList.add(bizType);
        try {
            if (fragmentType < 0) {
                sb.append(" and fragment_type = -1 ");
                if (TextUtils.isEmpty(refType)) {
                    sb.append(" and mode = ? ");
                    selectionList.add(String.valueOf(mode));
                } else {
                    sb.append("and mode = ? and ref_type = ?");
                    selectionList.add(String.valueOf(mode));
                    selectionList.add(refType);
                }
            } else {
                sb.append(" and fragment_type >= 0 ");
                if (TextUtils.isEmpty(refType)) {
                    sb.append(" and mode = ? ");
                    selectionList.add(String.valueOf(mode));
                } else {
                    sb.append("and mode = ? and ref_type = ?");
                    selectionList.add(String.valueOf(mode));
                    selectionList.add(refType);
                }
            }
            sb.append(" order by fragment_type");
            selections = new String[selectionList.size()];
            selectionList.toArray(selections);
            cursor = db.rawQuery(sb.toString(), selections);
            int index;
            BizFragmentConfig config;
            while (cursor.moveToNext()) {
                index = -1;
                config = new BizFragmentConfig();
                config.id = cursor.getString(++index);
                config.fragmentTag = cursor.getString(++index);
                config.bizType = cursor.getString(++index);
                config.refType = cursor.getString(++index);
                config.tabTitle = cursor.getString(++index);
                config.fragmentType = cursor.getInt(++index);
                config.className = cursor.getString(++index);
                bizFragmentConfigs.add(config);
            }
            clearStringBuffer();
        } catch (Exception e) {
            e.printStackTrace();
            return bizFragmentConfigs;
        } finally {
            if (cursor != null)
                cursor.close();
            db.close();
        }
        return bizFragmentConfigs;
    }


    @Override
    public String getStorageNum(String workId, String workCode, String invId, String invCode) {
        SQLiteDatabase db = getReadableDB();
        Cursor cursor = null;
        String storageNum = null;
        try {
            if (!TextUtils.isEmpty(workId)) {
                //如果传入的workId不为空那么直接使用
                cursor = db.rawQuery("select storage_code from p_auth_org where parent_id = ? and org_code = ? and org_level = ?",
                        new String[]{workId, invCode, "3"});
            } else if (!TextUtils.isEmpty(workCode)) {
                StringBuffer sb = new StringBuffer();
                sb.append("select distinct  storage_code from ").append(" p_auth_org ").append(" where org_level = ? ")
                        .append("and storage_code != ").append("''")
                        .append(" and parent_id IN ( select org_id from ").append(" p_auth_org ");
                sb.append(" where org_code IN (");
                sb.append("'").append(workCode).append("'");
                sb.append(" ) ");
                sb.append(" and org_level = ? ");
                sb.append(")");
                L.e("查询仓库号sql = " + sb.toString());
                cursor = db.rawQuery(sb.toString(), new String[]{"3", "2"});
            }

            storageNum = "";
            while (cursor.moveToNext()) {
                storageNum = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            if (cursor != null)
                cursor.close();
            db.close();
        }
        L.e("查询到的storageNum = " + storageNum);
        return storageNum;
    }

    @Override
    public ArrayList<String> getStorageNumList(int flag) {
        SQLiteDatabase db = getReadableDB();
        ArrayList<String> list = new ArrayList<>();
        ArrayList<String> authOrgs = new ArrayList<>();
        if (!TextUtils.isEmpty(Global.AUTH_ORG)) {
            authOrgs.addAll(Arrays.asList(Global.AUTH_ORG.split("\\|")));
        }
        StringBuffer sb = new StringBuffer();
        String tableName = flag == 0 ? PAuthOrgKey : PAuthOrg2Key;
        Cursor cursor = null;
        try {
            sb.append("select distinct  storage_code from ").append(tableName).append(" where org_level = ? ")
                    .append(" and parent_id IN ( select org_id from ").append(tableName);

            if (authOrgs.size() > 0 && flag == 0) {
                sb.append(" where org_code IN (");
                for (int i = 0; i < authOrgs.size(); i++) {
                    sb.append("'").append(authOrgs.get(i)).append("'").append(i == authOrgs.size() - 1 ? "" : ",");
                }
                sb.append(" ) ");
                sb.append(" and org_level = ? ");
                sb.append(")");
            }
            cursor = db.rawQuery(sb.toString(), new String[]{"3", "2"});
            list.add("请选择");
            while (cursor.moveToNext()) {
                list.add(cursor.getString(0));
            }
            sb.setLength(0);
        } catch (Exception e) {
            e.printStackTrace();
            return list;
        } finally {
            if (cursor != null)
                cursor.close();
            db.close();
        }
        return list;
    }

    /**
     * 保存所有的菜单信息
     *
     * @param menus
     * @param loginId
     * @param mode
     */
    public ArrayList<MenuNode> saveMenuInfo(ArrayList<MenuNode> menus, String loginId, int mode) {
        SQLiteDatabase db = getWritableDB();
        //先删除历史的菜单
        db.delete("T_HOME_MENUS", "login_id = ?", new String[]{loginId});
        //插入一行
        ContentValues cv = new ContentValues();
        for (MenuNode menu : menus) {
            cv.clear();
            cv.put("id", menu.getId());
            cv.put("parent_id", menu.getParentId());
            cv.put("biz_type", menu.getBusinessType());
            cv.put("ref_type", menu.getRefType());
            cv.put("caption", menu.getCaption());
            cv.put("functionCode", menu.getFunctionCode());
            cv.put("login_id", loginId);
            cv.put("mode", menu.getMode());
            cv.put("tree_level", menu.getLevel());
            db.insert("T_HOME_MENUS", null, cv);
        }
        db.close();
        return menus;
    }

    /**
     * 查询出所有的菜单信息
     *
     * @param loginId
     * @param mode
     * @return
     */
    @Override
    public ArrayList<MenuNode> getMenuInfo(String loginId, int mode) {
        ArrayList<MenuNode> list = new ArrayList<>();
        if (TextUtils.isEmpty(loginId) || mode < 0 || mode > 1) {
            return list;
        }
        SQLiteDatabase db = getWritableDB();
        Cursor cursor = null;
        clearStringBuffer();
        try {
            sb.append("select id,parent_id,biz_type,ref_type,caption,functionCode,tree_level ");
            sb.append(" from T_HOME_MENUS where login_id = ? and mode = ?");
            cursor = db.rawQuery(sb.toString(), new String[]{loginId, String.valueOf(mode)});
            clearStringBuffer();
            MenuNode item;
            int index;
            while (cursor.moveToNext()) {
                item = new MenuNode();
                index = -1;
                item.setId(cursor.getString(++index));
                item.setParentId(cursor.getString(++index));
                item.setBusinessType(cursor.getString(++index));
                item.setRefType(cursor.getString(++index));
                item.setCaption(cursor.getString(++index));
                item.setFunctionCode(cursor.getString(++index));
                item.setLevel(cursor.getInt(++index));
                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return list;
    }

    /**
     * 读取离线模式下该用户的所有业务类型，注意这里仅仅需要查询的二级菜单l
     *
     * @param loginId
     * @return
     */
    @Override
    public ArrayList<MenuNode> readMenuInfo(String loginId) {
        ArrayList<MenuNode> list = new ArrayList<>();
        SQLiteDatabase db = getWritableDB();
        Cursor cursor = null;
        clearStringBuffer();
        try {
            //1. 查询离线根节点的id
            String rootId = null;
            cursor = db.rawQuery("select id from T_HOME_MENUS where parent_id = ? and biz_type = ?", new String[]{
                    "0", "local_mobile"});
            while (cursor.moveToNext()) {
                rootId = cursor.getString(0);
            }
            if (TextUtils.isEmpty(rootId)) {
                return list;
            }

            sb.append("select id,parent_id,biz_type,ref_type,caption,functionCode,tree_level ");
            sb.append(" from T_HOME_MENUS where login_id = ? and biz_type != ? and parent_id != ? and mode = ? and ref_type is null");
            cursor = db.rawQuery(sb.toString(), new String[]{loginId, "local_mobile", rootId, "1"});
            clearStringBuffer();
            MenuNode item;
            int index;
            while (cursor.moveToNext()) {
                item = new MenuNode();
                index = -1;
                item.setId(cursor.getString(++index));
                item.setParentId(cursor.getString(++index));
                item.setBusinessType(cursor.getString(++index));
                item.setRefType(cursor.getString(++index));
                item.setCaption(cursor.getString(++index));
                item.setFunctionCode(cursor.getString(++index));
                item.setLevel(cursor.getInt(++index));
                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return list;
    }

    @Override
    public boolean getLocationInfo(String queryType, String workId, String invId, String storageNum, String location) {
        SQLiteDatabase db = getWritableDB();
        clearStringBuffer();
        String[] selections;
        List<String> selectionList = new ArrayList<>();
        sb.append(" select count(*) from BASE_LOCATION where ");
        if (TextUtils.isEmpty(storageNum)) {
            // 如果仓库号为空 则先查询库存地点是否启用了WM
            Cursor cursor = db.rawQuery("select storage_code from p_auth_org where org_id = ? ",
                    new String[]{invId});
            String storageCode = null;
            while (cursor.moveToNext()) {
                storageCode = cursor.getString(0);
            }
            cursor.close();
            if (TextUtils.isEmpty(storageCode)) {
                sb.append(" work_id = ? and inv_id = ? and location = ?");
                selectionList.add(workId);
                selectionList.add(invId);
                selectionList.add(location);

            } else {
                sb.append(" storage_num = ? and location = ?");
                selectionList.add(storageCode);
                selectionList.add(location);
            }
        } else {
            // 如果传入的是仓库号 表示启用了WM 直接根据仓库号查询
            selectionList.add(storageNum);
            selectionList.add(location);
            sb.append(" storage_num = ? and location = ?");
        }

        selections = new String[selectionList.size()];
        selectionList.toArray(selections);
        Cursor cursor = db.rawQuery(sb.toString(), selections);
        int result = 0;
        while (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }
        clearStringBuffer();
        cursor.close();
        if (result == 0)
            return false;
        else if (result == 1)
            return true;
        else
            return false;
    }

    @Override
    public MaterialEntity getMaterialInfo(String queryType, String materialNum) {
        SQLiteDatabase db = getWritableDB();
        MaterialEntity item = new MaterialEntity();
        Cursor cursor = null;
        switch (queryType) {
            case "01":
                // 物料基础信息
                cursor = db.rawQuery("select id ,material_num,material_desc,material_group,unit from BASE_MATERIAL_CODE where material_num = ?",
                        new String[]{materialNum});
                while (cursor.moveToNext()) {
                    item.id = cursor.getString(0);
                    item.materialNum = cursor.getString(1);
                    item.materialDesc = cursor.getString(2);
                    item.materialGroup = cursor.getString(3);
                    item.unit = cursor.getString(4);
                }
                break;
            default:
                break;
        }
        if (cursor != null)
            cursor.close();
        db.close();
        return item;
    }
}
