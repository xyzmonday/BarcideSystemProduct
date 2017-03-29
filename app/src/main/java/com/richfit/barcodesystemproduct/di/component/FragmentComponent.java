package com.richfit.barcodesystemproduct.di.component;

import android.app.Activity;
import android.content.Context;

import com.richfit.barcodesystemproduct.camera.ShowAndTakePhotoFragment;
import com.richfit.barcodesystemproduct.di.module.FragmentModule;
import com.richfit.barcodesystemproduct.module_acceptstore.qinghai_103.QingHaiAS103CollectFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qinghai_103.QingHaiAS103DetailFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qinghai_103.QingHaiAS103EditFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qinghai_103.QingHaiAS103HeaderFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qinghai_105.QingHaiAS105CollectFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qinghai_105.QingHaiAS105DetailFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qinghai_105.QingHaiAS105EditFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qinghai_105.QingHaiAS105HeaderFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qinghai_105n.QingHaiAS105NCollectFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qinghai_105n.QingHaiAS105NDetailFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qinghai_105n.QingHaiAS105NEditFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qinghai_105n.QingHaiAS105NHeaderFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qinghai_ww.QingHaiASWWCollectFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qinghai_ww.QingHaiASWWDetailFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qinghai_ww.QingHaiASWWEditFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qinghai_ww.QingHaiASWWHeaderFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qingyang_asn.collect.QingYangASNCollectFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qingyang_asn.detail.QingYangASNDetailFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qingyang_asn.edit.QingYangASNEditFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.qingyang_asn.header.QingYangASNHeaderFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.ww_component.collect.QingHaiWWCCollectFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.ww_component.detail.QingHaiWWCDetailFragment;
import com.richfit.barcodesystemproduct.module_acceptstore.ww_component.edit.QingHaiWWCEditFragment;
import com.richfit.barcodesystemproduct.module_approval.qinghai_ao.collect.QingHaiAOCollectFragment;
import com.richfit.barcodesystemproduct.module_approval.qinghai_ao.detail.QingHaiAODetailFragment;
import com.richfit.barcodesystemproduct.module_approval.qinghai_ao.edit.QingHaiAOEditFragment;
import com.richfit.barcodesystemproduct.module_approval.qinghai_ao.header.QingHaiAOHeaderFragment;
import com.richfit.barcodesystemproduct.module_approval.qingyang_ao.collect.QingYangAOCollectFragment;
import com.richfit.barcodesystemproduct.module_approval.qingyang_ao.detail.QingYangAODetailFragment;
import com.richfit.barcodesystemproduct.module_approval.qingyang_ao.edit.QingYangAOEditFragment;
import com.richfit.barcodesystemproduct.module_approval.qingyang_ao.header.QingYangAOHeaderFragment;
import com.richfit.barcodesystemproduct.module_check.qinghai_blind.collect.QingHaiBlindCollectFragment;
import com.richfit.barcodesystemproduct.module_check.qinghai_blind.detail.QingHaiBlindDetailFragment;
import com.richfit.barcodesystemproduct.module_check.qinghai_blind.edit.QingHaiBlindEditFragment;
import com.richfit.barcodesystemproduct.module_check.qinghai_blind.header.QingHaiBlindHeaderFragment;
import com.richfit.barcodesystemproduct.module_check.qinghai_cn.collect.QingHaiCNCollectFragment;
import com.richfit.barcodesystemproduct.module_check.qinghai_cn.detail.QingHaiCNDetailFragment;
import com.richfit.barcodesystemproduct.module_check.qinghai_cn.edit.QingHaiCNEditFragment;
import com.richfit.barcodesystemproduct.module_check.qinghai_cn.header.QingHaiCNHeaderFragment;
import com.richfit.barcodesystemproduct.module_delivery.qinghai_dsn.QingHaiDSNCollectFragment;
import com.richfit.barcodesystemproduct.module_delivery.qinghai_dsn.QingHaiDSNDetailFragment;
import com.richfit.barcodesystemproduct.module_delivery.qinghai_dsn.QingHaiDSNEditFragment;
import com.richfit.barcodesystemproduct.module_delivery.qinghai_dsn.QingHaiDSNHeaderFragment;
import com.richfit.barcodesystemproduct.module_delivery.qinghai_dsww.QingHaiDSWWCollectFragment;
import com.richfit.barcodesystemproduct.module_delivery.qinghai_dsww.QingHaiDSWWDetailFragment;
import com.richfit.barcodesystemproduct.module_delivery.qinghai_dsww.QingHaiDSWWEditFragment;
import com.richfit.barcodesystemproduct.module_delivery.qinghai_dsww.QingHaiDSWWHeaderFragment;
import com.richfit.barcodesystemproduct.module_delivery.qinghai_dsxs.QingHaiDSXSCollectFragment;
import com.richfit.barcodesystemproduct.module_delivery.qinghai_dsxs.QingHaiDSXSDetailFFragment;
import com.richfit.barcodesystemproduct.module_delivery.qinghai_dsxs.QingHaiDSXSEditFragment;
import com.richfit.barcodesystemproduct.module_delivery.qinghai_dsxs.QingHaiDSXSHeaderFragment;
import com.richfit.barcodesystemproduct.module_delivery.qingyang_dsy.QingYangDSYCollectFragment;
import com.richfit.barcodesystemproduct.module_delivery.qingyang_dsy.QingYangDSYDetailFragment;
import com.richfit.barcodesystemproduct.module_delivery.qingyang_dsy.QingYangDSYEditFragment;
import com.richfit.barcodesystemproduct.module_delivery.qingyang_dsy.QingYangDSYHeaderFragment;
import com.richfit.barcodesystemproduct.module_infoquery.material_infoquery.MaterialInfoQueryFragment;
import com.richfit.barcodesystemproduct.module_infoquery.material_liaoqian.detail.LQDetailFragment;
import com.richfit.barcodesystemproduct.module_infoquery.material_liaoqian.header.LQHeaderFragment;
import com.richfit.barcodesystemproduct.module_locationadjust.collect.LACollectFragment;
import com.richfit.barcodesystemproduct.module_locationadjust.header.LAHeaderFragment;
import com.richfit.barcodesystemproduct.module_movestore.qinghai_311n.QingHaiMSN311CollectFragment;
import com.richfit.barcodesystemproduct.module_movestore.qinghai_311n.QingHaiMSN311DetailFragment;
import com.richfit.barcodesystemproduct.module_movestore.qinghai_311n.QingHaiMSN311EditFragment;
import com.richfit.barcodesystemproduct.module_movestore.qinghai_311n.QingHaiMSN311HeaderFragment;
import com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto101.QingHaiUbSto101CollectFragment;
import com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto101.QingHaiUbSto101DetailFragment;
import com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto101.QingHaiUbSto101EditFragment;
import com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto101.QingHaiUbSto101HeaderFragment;
import com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto351.QingHaiUbSto351CollectFragment;
import com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto351.QingHaiUbSto351DetailFragment;
import com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto351.QingHaiUbSto351EditFragment;
import com.richfit.barcodesystemproduct.module_movestore.qinghai_ubsto351.QingHaiUbSto351HeaderFragment;
import com.richfit.barcodesystemproduct.module_movestore.qingyang_301n.QingYangMSN301CollectFragment;
import com.richfit.barcodesystemproduct.module_movestore.qingyang_301n.QingYangMSN301DetailFragment;
import com.richfit.barcodesystemproduct.module_movestore.qingyang_301n.QingYangMSN301EditFragment;
import com.richfit.barcodesystemproduct.module_movestore.qingyang_301n.QingYangMSN301HeaderFragment;
import com.richfit.barcodesystemproduct.module_returngoods.QingHaiRGCollectFragment;
import com.richfit.barcodesystemproduct.module_returngoods.QingHaiRGDetailFragment;
import com.richfit.barcodesystemproduct.module_returngoods.QingHaiRGEditFragment;
import com.richfit.barcodesystemproduct.module_returngoods.QingHaiRGHeaderFragment;
import com.richfit.barcodesystemproduct.module_returnstore.qinghai_rsn.collect.QingHaiRSNCollectFragment;
import com.richfit.barcodesystemproduct.module_returnstore.qinghai_rsn.detail.QingHaiRSNDetailFragment;
import com.richfit.barcodesystemproduct.module_returnstore.qinghai_rsn.edit.QingHaiRSNEditFragment;
import com.richfit.barcodesystemproduct.module_returnstore.qinghai_rsn.header.QingHaiRSNHeaderFragment;
import com.richfit.barcodesystemproduct.module_returnstore.qinghai_rsy.QingHaiRSYCollectFragment;
import com.richfit.barcodesystemproduct.module_returnstore.qinghai_rsy.QingHaiRSYDetailFragment;
import com.richfit.barcodesystemproduct.module_returnstore.qinghai_rsy.QingHaiRSYEditFragment;
import com.richfit.barcodesystemproduct.module_returnstore.qinghai_rsy.QingHaiRSYHeaderFragment;
import com.richfit.barcodesystemproduct.module_returnstore.qingyang_rsy.QingYangRSYCollectFragment;
import com.richfit.barcodesystemproduct.module_returnstore.qingyang_rsy.QingYangRSYDetailFragment;
import com.richfit.barcodesystemproduct.module_returnstore.qingyang_rsy.QingYangRSYEditFragment;
import com.richfit.barcodesystemproduct.module_returnstore.qingyang_rsy.QingYangRSYHeaderFragment;
import com.richfit.common_lib.scope.ContextLife;
import com.richfit.common_lib.scope.FragmentScope;

import dagger.Component;

@FragmentScope
@Component(modules = FragmentModule.class, dependencies = AppComponent.class)
public interface FragmentComponent {

    @ContextLife("Application")
    Context getApplicationContext();

    @ContextLife("Activity")
    Context getActivityContext();

    Activity getActivity();

    void inject(QingYangASNHeaderFragment fragment);

    void inject(QingYangASNEditFragment fragment);

    void inject(QingYangASNDetailFragment fragment);

    void inject(QingYangASNCollectFragment fragment);

    void inject(QingYangAOHeaderFragment fragment);

    void inject(QingYangAODetailFragment fragment);

    void inject(QingYangAOCollectFragment fragment);

    void inject(QingYangAOEditFragment fragment);

    void inject(ShowAndTakePhotoFragment fragment);

    void inject(QingYangDSYHeaderFragment fragment);

    void inject(QingYangDSYDetailFragment fragment);

    void inject(QingYangDSYCollectFragment fragment);

    void inject(QingYangDSYEditFragment fragment);
//
    void inject(QingYangMSN301HeaderFragment fragment);

    void inject(QingYangMSN301DetailFragment fragment);

    void inject(QingYangMSN301CollectFragment fragment);

    void inject(QingYangMSN301EditFragment fragment);

    void inject(QingHaiDSXSHeaderFragment fragment);

    void inject(QingHaiDSXSDetailFFragment fragment);

    void inject(QingHaiDSXSCollectFragment fragment);

    void inject(QingHaiDSXSEditFragment fragment);

    void inject(QingHaiUbSto351HeaderFragment fragment);

    void inject(QingHaiUbSto351DetailFragment fragment);

    void inject(QingHaiUbSto351CollectFragment fragment);

    void inject(QingHaiUbSto351EditFragment fragment);

    void inject(QingHaiUbSto101HeaderFragment fragment);

    void inject(QingHaiUbSto101DetailFragment fragment);

    void inject(QingHaiUbSto101CollectFragment fragment);

    void inject(QingHaiUbSto101EditFragment fragment);

    void inject(QingHaiMSN311HeaderFragment fragment);

    void inject(QingHaiMSN311DetailFragment fragment);

    void inject(QingHaiMSN311CollectFragment fragment);

    void inject(QingHaiMSN311EditFragment fragment);

    void inject(QingHaiAS103HeaderFragment fragment);

    void inject(QingHaiAS103CollectFragment fragment);

    void inject(QingHaiAS103DetailFragment fragment);

    void inject(QingHaiAS103EditFragment fragment);

    void inject(QingHaiAS105NHeaderFragment fragment);

    void inject(QingHaiAS105NDetailFragment fragment);

    void inject(QingHaiAS105NCollectFragment fragment);

    void inject(QingHaiAS105NEditFragment fragment);

    void inject(QingHaiASWWHeaderFragment fragment);

    void inject(QingHaiASWWDetailFragment fragment);

    void inject(QingHaiASWWCollectFragment fragment);

    void inject(QingHaiASWWEditFragment fragment);

    void inject(QingHaiRGHeaderFragment fragment);

    void inject(QingHaiRGDetailFragment fragment);

    void inject(QingHaiRGCollectFragment fragment);

    void inject(QingHaiRGEditFragment fragment);

    void inject(QingHaiDSNHeaderFragment fragment);

    void inject(QingHaiDSNDetailFragment fragment);

    void inject(QingHaiDSNCollectFragment fragment);

    void inject(QingHaiDSNEditFragment fragment);

    void inject(QingHaiRSYHeaderFragment fragment);

    void inject(QingHaiRSYDetailFragment fragment);

    void inject(QingHaiRSYCollectFragment fragment);

    void inject(QingHaiRSYEditFragment fragment);
//
    void inject(QingHaiAOHeaderFragment fragment);

    void inject(QingHaiAODetailFragment fragment);

    void inject(QingHaiAOCollectFragment fragment);

    void inject(QingHaiAOEditFragment fragment);
//
    void inject(QingHaiAS105HeaderFragment fragment);

    void inject(QingHaiAS105DetailFragment fragment);

    void inject(QingHaiAS105CollectFragment fragment);

    void inject(QingHaiAS105EditFragment fragment);
//
    void inject(QingHaiRSNHeaderFragment fragment);

    void inject(QingHaiRSNDetailFragment fragment);

    void inject(QingHaiRSNCollectFragment fragment);

    void inject(QingHaiRSNEditFragment fragment);
//
    void inject(QingHaiCNHeaderFragment fragment);

    void inject(QingHaiCNDetailFragment fragment);

    void inject(QingHaiCNCollectFragment fragment);

    void inject(QingHaiCNEditFragment fragment);

    void inject(QingHaiBlindHeaderFragment fragment);

    void inject(QingHaiBlindDetailFragment fragment);

    void inject(QingHaiBlindCollectFragment fragment);

    void inject(QingHaiBlindEditFragment fragment);

    void inject(QingHaiDSWWHeaderFragment fragment);

    void inject(QingHaiDSWWDetailFragment fragment);

    void inject(QingHaiDSWWCollectFragment fragment);

    void inject(QingHaiDSWWEditFragment fragment);

    void inject(QingHaiWWCDetailFragment fragment);

    void inject(QingHaiWWCCollectFragment fragment);

    void inject(QingHaiWWCEditFragment fragment);

    void inject(LAHeaderFragment fragment);

    void inject(LACollectFragment fragment);

    void inject(MaterialInfoQueryFragment fragment);
    void inject(LQHeaderFragment fragment);
    void inject(LQDetailFragment fragment);

    void inject(QingYangRSYHeaderFragment fragment);
    void inject(QingYangRSYDetailFragment fragment);
    void inject(QingYangRSYCollectFragment fragment);
    void inject(QingYangRSYEditFragment framgent);

}