package com.richfit.domain.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 数据上传进度信息实体类
 * Created by monday on 2017/4/24.
 */

public class UploadMsgEntity implements Parcelable {
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
    public String checkId;
    public String checkLevel;
    public String specialFlag;
    public String storageNum;
    public String checkNum;
    public String workCode;
    public String invCode;
    public String workId;
    public String invId;
    public String recWorkId;
    public String recInvId;


    public UploadMsgEntity() {

    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.taskId);
        dest.writeByte(this.isEror ? (byte) 1 : (byte) 0);
        dest.writeInt(this.totalTaskNum);
        dest.writeString(this.bizType);
        dest.writeString(this.bizTypeDesc);
        dest.writeString(this.refType);
        dest.writeString(this.refTypeDesc);
        dest.writeString(this.errorMsg);
        dest.writeString(this.materialDoc);
        dest.writeString(this.transNum);
        dest.writeString(this.transId);
        dest.writeString(this.refCodeId);
        dest.writeString(this.refNum);
        dest.writeString(this.checkId);
        dest.writeString(this.checkLevel);
        dest.writeString(this.specialFlag);
        dest.writeString(this.storageNum);
        dest.writeString(this.checkNum);
        dest.writeString(this.workCode);
        dest.writeString(this.invCode);
        dest.writeString(this.workId);
        dest.writeString(this.invId);
        dest.writeString(this.recWorkId);
        dest.writeString(this.recInvId);
    }

    protected UploadMsgEntity(Parcel in) {
        this.taskId = in.readInt();
        this.isEror = in.readByte() != 0;
        this.totalTaskNum = in.readInt();
        this.bizType = in.readString();
        this.bizTypeDesc = in.readString();
        this.refType = in.readString();
        this.refTypeDesc = in.readString();
        this.errorMsg = in.readString();
        this.materialDoc = in.readString();
        this.transNum = in.readString();
        this.transId = in.readString();
        this.refCodeId = in.readString();
        this.refNum = in.readString();
        this.checkId = in.readString();
        this.checkLevel = in.readString();
        this.specialFlag = in.readString();
        this.storageNum = in.readString();
        this.checkNum = in.readString();
        this.workCode = in.readString();
        this.invCode = in.readString();
        this.workId = in.readString();
        this.invId = in.readString();
        this.recWorkId = in.readString();
        this.recInvId = in.readString();
    }

    public static final Creator<UploadMsgEntity> CREATOR = new Creator<UploadMsgEntity>() {
        @Override
        public UploadMsgEntity createFromParcel(Parcel source) {
            return new UploadMsgEntity(source);
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
                ", checkId='" + checkId + '\'' +
                ", checkLevel='" + checkLevel + '\'' +
                ", specialFlag='" + specialFlag + '\'' +
                ", storageNum='" + storageNum + '\'' +
                ", checkNum='" + checkNum + '\'' +
                ", workCode='" + workCode + '\'' +
                ", invCode='" + invCode + '\'' +
                ", workId='" + workId + '\'' +
                ", invId='" + invId + '\'' +
                ", recWorkId='" + recWorkId + '\'' +
                ", recInvId='" + recInvId + '\'' +
                '}';
    }
}

