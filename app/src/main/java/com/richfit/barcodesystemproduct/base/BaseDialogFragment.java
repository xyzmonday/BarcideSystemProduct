package com.richfit.barcodesystemproduct.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.richfit.barcodesystemproduct.BarcodeSystemApplication;
import com.richfit.barcodesystemproduct.R;
import com.richfit.barcodesystemproduct.di.component.DaggerFragmentComponent;
import com.richfit.barcodesystemproduct.di.component.FragmentComponent;
import com.richfit.barcodesystemproduct.di.module.FragmentModule;
import com.richfit.common_lib.IInterface.IPresenter;
import com.richfit.domain.bean.RowConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by monday on 2016/11/21.
 */

public abstract class BaseDialogFragment<T extends IPresenter> extends DialogFragment implements BaseView {


}
