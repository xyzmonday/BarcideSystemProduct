package com.richfit.domain.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 数据上传进度信息实体类
 * Created by monday on 2017/4/24.
 */

public class UploadMsgEntity implements Parcelable{
    /*该上传任务id*/
    public int taskId;
    /*是否发生错误*/
    public boolean isEror;
    /*本次数据上传任务总数*/
    public int totalTaskNum;
    /*当前任务的业务类型BizType*/
    public String bizType;
    public String bizTypeDesc;
    /*当前任务的单据类型*/
    public String refType;
    public String refTypeDesc;
    /*当前任务的错误消息*/
    public String errorMsg;
    /*物料凭证*/
    public String materialDoc;
    /*过账凭证*/
    public String transNum;
    /*缓存id*/
    public String transId;
    /*单据id*/
    public String refCodeId;
    /*单据号*/
    public String refNum;

    public UploadMsgEntity() {

    }


    protected UploadMsgEntity(Parcel in) {
        taskId = in.readInt();
        isEror = in.readByte() != 0;
        totalTaskNum = in.readInt();
        bizType = in.readString();
        bizTypeDesc = in.readString();
        refType = in.readString();
        refTypeDesc = in.readString();
        errorMsg = in.readString();
        materialDoc = in.readString();
        transNum = in.readString();
        transId = in.readString();
        refCodeId = in.readString();
        refNum = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(taskId);
        dest.writeByte((byte) (isEror ? 1 : 0));
        dest.writeInt(totalTaskNum);
        dest.writeString(bizType);
        dest.writeString(bizTypeDesc);
        dest.writeString(refType);
        dest.writeString(refTypeDesc);
        dest.writeString(errorMsg);
        dest.writeString(materialDoc);
        dest.writeString(transNum);
        dest.writeString(transId);
        dest.writeString(refCodeId);
        dest.writeString(refNum);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UploadMsgEntity> CREATOR = new Creator<UploadMsgEntity>() {
        @Override
        public UploadMsgEntity createFromParcel(Parcel in) {
            return new UploadMsgEntity(in);
        }

        @Override
        public UploadMsgEntity[] newArray(int size) {
            return new UploadMsgEntity[size];
        }
    };

    @Override
    public String toString() {
        return "UploadMsgEntity{" +
                "taskId=" + taskId +
                ", isEror=" + isEror +
                ", totalTaskNum=" + totalTaskNum +
                ", bizType='" + bizType + '\'' +
                ", bizTypeDesc='" + bizTypeDesc + '\'' +
                ", refType='" + refType + '\'' +
                ", refTypeDesc='" + refTypeDesc + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", materialDoc='" + materialDoc + '\'' +
                ", transNum='" + transNum + '\'' +
                ", transId='" + transId + '\'' +
                ", refCodeId='" + refCodeId + '\'' +
                ", refNum='" + refNum + '\'' +
                '}';
    }
}
