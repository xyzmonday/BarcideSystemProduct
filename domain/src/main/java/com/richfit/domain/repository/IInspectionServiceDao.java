package com.richfit.domain.repository;

import com.richfit.domain.bean.ImageEntity;
import com.richfit.domain.bean.ResultEntity;

import java.util.ArrayList;

/**
 * Created by monday on 2017/3/29.
 */

public interface IInspectionServiceDao {
    /**
     * 删除该张单据的所有验收图片
     * @param refNum
     * @param refCodeId
     * @param isLocal
     */
    void deleteInspectionImages(String refNum, String refCodeId, boolean isLocal);

    /**
     * 删除该条验收明细下的所有图片
     * @param refNum
     * @param refLineNum
     * @param refLineId
     * @param isLocal
     */
    void deleteInspectionImagesSingle(String refNum, String refLineNum, String refLineId, boolean isLocal);

    /**
     * 拍照界面，删除指定的图片集合
     * @param images
     * @param isLocal
     * @return
     */
    boolean deleteTakedImages(ArrayList<ImageEntity> images, boolean isLocal);

    /**
     * 保存拍照获取的图片
     * @param images
     * @param refNum
     * @param refLineId
     * @param takePhotoType
     * @param imageDir
     * @param isLocal
     */
    void saveTakedImages(ArrayList<ImageEntity> images, String refNum, String refLineId, int takePhotoType,
                         String imageDir, boolean isLocal);

    /**
     * 读取该张验收单的所有图片信息
     * @param refNum
     * @param isLocal
     * @return
     */
    ArrayList<ImageEntity> readImagesByRefNum(String refNum, boolean isLocal);

    boolean uploadInspectionDataSingle(ResultEntity result);

}
