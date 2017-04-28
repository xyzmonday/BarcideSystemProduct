package com.richfit.common_lib.utils;

/**
 * Created by monday on 2016/11/13.
 */

public class Global {

    public final static String RETURN_SUCCESS_CODE = "S";
    /*App是否第一次启动*/
    public final static String IS_APP_FIRST_KEY = "is_app_first";

    public static String USER_ID;
    public static String LOGIN_ID;
    public static String USER_NAME;
    public static String COMPANY_ID;
    public static String COMPANY_CODE;
    public static String MAC_ADDRESS;
    public static String AUTH_ORG;
    //是否打开批次管理Y表示打开
    public static boolean BATCH_FLAG;

    /*主模块编码*/
    public static final String WZYS = "100";
    public static final String WZRK = "101";
    public static final String WZCK = "102";
    //物资退库
    public static final String WZTK = "103";
    //物资移库
    public static final String WZYK = "104";
    //物资退货
    public static final String WZTH = "105";
    public static final String WZPD = "106";
    //仓位调整
    public static final String CWTZ = "107";
    public static final String DJCX = "108";
    public static final String XXCX = "109";
    public static final String DGRK = "110";
    public static final String DGCK = "111";
    public static final String SETTING = "112";
    //离线模式
    public static final String L_LOADDATA = "113";
    public static final String L_UPLOADDATA = "114";
    public static final String DGYK = "115";

    //用户选择的模式
    public static final int ONLINE_MODE = 0x0;
    public static final int OFFLINE_MODE = 0x1;

    /*地区公司*/
    public static final String QINGYANG = "qingyang";
    public static final String QINGHAI = "qinghai";

    /*配置文件类型*/
    public static final String HEADER_CONFIG_TYPE = "0";
    public static final String DETAIL_PARENT_NODE_CONFIG_TYPE = "1";
    public static final String DETAIL_CHILD_NODE_CONFIG_TYPE = "2";
    public static final String COLLECT_CONFIG_TYPE = "3";
    public static final String LOCATION_CONFIG_TYPE = "4";

    /*配置字段，返回map的位置，1抬头，2行，3仓位*/
    public static final String EXTRA_HEADER_MAP_TYPE = "1";
    public static final String EXTRA_LINE_MAP_TYPE = "2";
    public static final String EXTRA_LOCATION_MAP_TYPE = "3";

    /*数据上传成功，跳转到抬头界面后清除抬头界面的数据*/
    public static final String CLEAR_HEADER_UI = "clear_header_ui";

    /*全局日期格式*/
    public static final String GLOBAL_DATE_PATTERN_TYPE1 = "yyyyMMdd";
    public static final String GLOBAL_DATE_PATTERN_TYPE2 = "yyyy MM dd";
    public static final String GLOBAL_DATE_PATTERN_TYPE3 = "yyyy-MM-dd";
    public static final String GLOBAL_DATE_PATTERN_TYPE4 = "yyyy/MM/dd";

    /*重试类型*/
    public static final String NETWORK_CONNECT_ERROR_TAG = "network_connect_error_tag";
    /*注册*/
    public static final String RETRY_REGISTER_ACTION = "retry_register_action";
    /*登陆*/
    public static final String RETRY_LOGIN_ACTION = "retry_login_action";
    /*初始化主菜单*/
    public static final String RETRY_SETUP_MENUS_ACTION = "retry_setup_menus_action";
    /*同步基础数据*/
    public static final String RETRY_SYNC_BASIC_DATA_ACTION = "retry_sync_basic_data_action";
    /*查询物料信息*/
    public static final String RETRY_QUERY_MATERIAL_INFO = "retry_query_material_info";
    /*抬头界面获取单据数据*/
    public static final String RETRY_LOAD_REFERENCE_ACTION = "retry_load_reference_action";
    /*数据采集界面获取单条缓存数据*/
    public static final String RETRY_LOAD_SINGLE_CACHE_ACTION = "retry_load_single_cache_action";
    /*数据采集界面保存单条数据*/
    public static final String RETRY_SAVE_COLLECTION_DATA_ACTION = "retry_save_collection_data_action";
    /*明细界面获取缓存*/
    public static final String RETRY_DELETE_TRANSFERED_CACHE_ACTION = "retry_delete_transfered_cache_action";
    /*过账*/
    public static final String RETRY_TRANSFER_DATA_ACTION = "retry_transfer_data_action";
    /*数据上传*/
    public static final String RETRY_UPLOAD_DATA_ACTION = "retry_upload_data_action";
    /*数据采集界面获取库存*/
    public static final String RETRY_LOAD_INVENTORY_ACTION = "retry_load_inventory_action";
    public static final String RETRY_LOAD_REC_INVENTORY_ACTION = "retry_load_rec_inventory_action";
    /*数据修改*/
    public static final String RETRY_EDIT_DATA_ACTION = "retry_edit_data_action";
   /*离线结束本次操作*/
    public static final String RETRY_SET_TRANS_FLAG_ACTION = "retry_set_trans_flag_action";

    /*
     * 父节点的布局类型,存在两种状态的父节点
     * 1. 带子节点的；2。 不带子节点的；统一规定PARENT_NODE_HEADER_TYPE不具有修改删除(针对于有参考的明细);
     * PARENT_NODE_ITEM_TYPE具有修改删除功能(针对无参考的明细)；
     */
    public static final int PARENT_NODE_HEADER_TYPE = 0x0;
    public static final int PARENT_NODE_ITEM_TYPE = 0x1;
    /*
     * 子节点的布局类型同样分为两种；一种是纯抬头，仅仅起到的指示作用，另一种是显示数据
     */
    public static final int CHILD_NODE_HEADER_TYPE = 0x2;
    public static final int CHILD_NODE_ITEM_TYPE = 0x3;

    /*子节点的起始id*/
    public static int CHILD_NODE_MAX_ID = 100000;
    public static int MAX_PATCH_LENGTH = 200;

    //服务器返回的时间
    public static final String SYNC_DATE_KEY = "syncDate";

    /*基础数据的key*/
    public static final String ID_KEY = "id";
    public static final String CODE_KEY = "code";
    public static final String NAME_KEY = "name";
    public static final String PARENTID_KEY = "parentId";
    public static final String ORGLEVEL_KEY = "orgLevel";
    public static final String STORAGENUM_KEY = "storageNum";
    public static final String SORT_KEY = "sort";
    public static final String VALUE_KEY = "value";
    public static final String SAPUPDATEDATE_KEY = "sapUpdateDate";
    public static final String WORK_ID = "work_id";
    public static final String INV_ID = "inv_id";
    public static final String EXTRA_LOCATION_LIST_KEY = "extra_location_list";
    public static final String EXTRA_REC_LOCATION_LIST_KEY = "extra_rec_location_list";
    public static final String EXTRA_BATCH_FLAG_KEY = "extra_batch_flag";
    public static final String EXTRA_QUANTITY_KEY = "extra_quantity";
    public static final String EXTRA_LOCATION_KEY = "extra_location";
    public static final String EXTRA_INV_ID_KEY = "extra_inv_id";
    public static final String EXTRA_WORK_ID_KEY = "extra_work_id";
    public static final String EXTRA_INV_CODE_KEY = "extra_inv_code";
    public static final String EXTRA_LOCATION_ID_KEY = "extra_location_id";
    public static final String EXTRA_MATERIAL_NUM_KEY = "extra_material_num";
    public static final String EXTRA_MATERIAL_ID_KEY = "extra_material_id";
    public static final String EXTRA_MATERIAL_DESC_KEY = "extra_material_desc";
    public static final String EXTRA_MATERIAL_GROUP_KEY = "extra_material_group";
    public static final String EXTRA_INV_QUANTITY_KEY = "extra_inv_quantity";
    public static final String EXTRA_BIZ_TYPE_KEY = "extra_biz_type";
    public static final String EXTRA_REF_TYPE_KEY = "extra_ref_type";
    public static final String START_DATE_KEY = "start_date_key";
    public static final String EXTRA_DEVICE_ID_KEY = "extra_device_id_key";
    public static final String END_DATE_KEY = "end_date_key";

    public static final String EXTRA_REC_LOCATION_KEY = "extra_rec_location";
    public static final String EXTRA_REC_BATCH_FLAG_KEY = "extra_rec_batch_flag";
    public static final String EXTRA_TOTAL_QUANTITY_KEY = "extra_total_quantity";
    /*特殊库存标识*/
    public static final String EXTRA_SPECIAL_INV_FLAG_KEY = "extra_special_inv_flag_key";
    public static final String EXTRA_SPECIAL_INV_NUM_KEY = "extra_special_inv_num_key";
    /*制造商*/
    public static final String EXTRA_MANUFUCTURER_KEY = "extra_manufucturer";
    /*抽检数量*/
    public static final String EXTRA_SAMPLE_QUANTITY_KEY = "extra_sample_quantity";
    /*完好数量*/
    public static final String EXTRA_QUALIFIED_QUANTITY_KEY = "extra_qualified_quantity";
    /*损坏数量*/
    public static final String EXTRA_DAMAGED_QUANTITY_KEY = "extra_damaged_quantity";
    /*送检数量*/
    public static final String EXTRA_INSPECTION_QUANTITY_KEY = "extra_inspection_quantity";
    /*锈蚀数量*/
    public static final String EXTRA_RUST_QUANTITY_KEY = "extra_rust_quantity";
    /*变质*/
    public static final String EXTRA_BAD_QUANTITY_KEY = "extra_bad_quantity";
    /*其他数量*/
    public static final String EXTRA_OTHER_QUANTITY_KEY = "extra_other_quantity";
    /*包装情况*/
    public static final String EXTRA_PACKAGE_KEY = "extra_package";
    /*质检单号*/
    public static final String EXTRA_QM_NUM_KEY = "extra_qm_num";
    /*索赔单号*/
    public static final String EXTRA_CLAIM_NUM_KEY = "extra_claim_num";
    /*合格证*/
    public static final String EXTRA_CERTIFICATE_KEY = "extra_certificate";
    /*说明书*/
    public static final String EXTRA_INSTRUCTIONS_KEY = "extra_instructions";
    /*质检证书*/
    public static final String EXTRA_QM_CERTIFICATE_KEY = "extra_qm_certificate";
    /*检验结果*/
    public static final String EXTRA_INSPECTION_RESULT_KEY = "extra_inspection_result";

    /*子节点的额外字段的数据*/
    public static final String LOCATION_EXTRA_MAP_KEY = "location_extra_map";
    /*父节点额外字段的数据*/
    public static final String COLLECT_EXTRA_MAP_KEY = "collect_extra_map";
    public static final String IMAGE_DEFAULT_FORMAT = ".jpeg";
    public static final String EXTRA_TAKE_PHOTO_TYPE = "extra_take_photo_type";
    public static final String EXTRA_TITLE_KEY = "extra_title";
    public static final String EXTRA_FRAGMENT_TYPE_KEY = "extra_fragment_type";
    public static final String EXTRA_MODULE_CODE_KEY = "extra_module_code";
    public static final String EXTRA_COMPANY_CODE_KEY = "extra_company_code";
    public static final String EXTRA_REF_NUM_KEY = "extra_ref_num";
    public static final String EXTRA_REF_LINE_NUM_KEY = "extra_ref_line_num";
    public static final String EXTRA_REF_LINE_ID_KEY = "extra_ref_line_id";
    public static final String EXTRA_POSITION_KEY = "extra_position";
    public static final String EXTRA_IS_LOCAL_KEY = "extra_is_local";
    public static final String EXTRA_RETURN_QUANTITY_KEY = "extra_return_quantity";
    public static final String EXTRA_PROJECT_TEXT_KEY = "extra_project_text";
    public static final String EXTRA_MOVE_CAUSE_DESC_KEY = "extra_move_cause_desc";
    public static final String EXTRA_MOVE_CAUSE_KEY = "extra_move_cause";
    public static final String EXTRA_DECISION_CAUSE_KEY = "extra_decision_cause";
    public static final String EXTRA_CAPTION_KEY = "extra_caption";
    public static final String EXTRA_TRANS_ID_KEY = "extra_trans_id";
    public static final String EXTRA_MODE_KEY = "extra_mode_key";

    /*扫描物料和批次位置*/
    public static final int MATERIAL_POS = 2;
    public static final int BATCHFALG_POS = 12;
    public static final int LOCATION_POS = 0;
    /*料签*/
    public static final int MATERIAL_POS_L = 4;
    public static final int BATCHFALG_POS_L = 6;

}
