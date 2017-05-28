package com.richfit.barcodesystemproduct.module_approval.xinanGD_ao.collect;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.base.BaseFragment;
import com.richfit.barcodesystemproduct.module_approval.xinanGD_ao.collect.imp.XiNanGDAOCollectPresenterImp;
import com.richfit.common_lib.widget.RichEditText;

import butterknife.BindView;

/**
 * Created by monday on 2017/5/26.
 */

public class XiNanGDAOCollectFragment extends BaseFragment<XiNanGDAOCollectPresenterImp>
        implements IXiNanGDAOCollectView {

    @BindView(R.id.sp_ref_line_num)
    Spinner spRefLineNum;
    @BindView(R.id.et_material_num)
    RichEditText etMaterialNum;
    @BindView(R.id.tv_material_desc)
    TextView tvMaterialDesc;
    @BindView(R.id.tv_special_inv_flag)
    TextView tvSpecialInvFlag;
    @BindView(R.id.tv_work)
    TextView tvWork;
    @BindView(R.id.tv_act_quantity)
    TextView tvActQuantity;
    @BindView(R.id.sp_inv)
    Spinner spInv;
    @BindView(R.id.tv_location_name)
    TextView tvLocationName;
    @BindView(R.id.et_location)
    RichEditText etLocation;
    @BindView(R.id.tv_location_quantity)
    TextView tvLocationQuantity;
    @BindView(R.id.quantity_name)
    TextView quantityName;
    @BindView(R.id.et_quantity)
    EditText etQuantity;
    @BindView(R.id.cb_single)
    CheckBox cbSingle;
    @BindView(R.id.tv_total_quantity)
    TextView tvTotalQuantity;
    @BindView(R.id.et_total_inspection_quantity)
    EditText etTotalInspectionQuantity;
    @BindView(R.id.et_sample_quantity)
    EditText etSampleQuantity;
    @BindView(R.id.et_inspection_condition)
    EditText etInspectionCondition;
    @BindView(R.id.et_process_condition)
    EditText etProcessConditon;
    @BindView(R.id.sp_inspection_method)
    Spinner spInspectionMethod;

    @Override
    protected int getContentId() {
        return R.layout.fragment_xinangd_ao_collect;
    }

    @Override
    public void initInjector() {

    }


}
