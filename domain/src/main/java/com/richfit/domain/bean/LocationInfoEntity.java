package com.richfit.domain.bean;

/**
 * 仓位相关的实体类
 *
 * @author HM
 */

public class LocationInfoEntity implements Cloneable{

    /*保存的id*/
    public String id;
    /*头ID*/
    public String transId;
    /*行ID*/
    public String transLineId;
    public String transLineSplitId;
    /*仓位号*/
    public String location;
    /*批次*/
    public String batchFlag;
    /*数量*/
    public String quantity;
    /*接收仓位*/
    public String recLocation;
    /*接收批次*/
    public String recBatchFlag;
    /*特殊库存标识*/
    public String specialInvFlag;
    /*特殊库存编号*/
    public String specialInvNum;
    public String locationCombine;

    public String specialConvert;
    public String deviceId;

    @Override
    public LocationInfoEntity clone() {
        try {
            return (LocationInfoEntity) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "LocationInfoEntity{" +
                "id='" + id + '\'' +
                ", transId='" + transId + '\'' +
                ", transLineId='" + transLineId + '\'' +
                ", transLineSplitId='" + transLineSplitId + '\'' +
                ", location='" + location + '\'' +
                ", batchFlag='" + batchFlag + '\'' +
                ", quantity='" + quantity + '\'' +
                ", recLocation='" + recLocation + '\'' +
                ", recBatchFlag='" + recBatchFlag + '\'' +
                ", specialInvFlag='" + specialInvFlag + '\'' +
                ", specialInvNum='" + specialInvNum + '\'' +
                ", locationCombine='" + locationCombine + '\'' +
                ", specialConvert='" + specialConvert + '\'' +
                ", deviceId='" + deviceId + '\'' +
                '}';
    }
}
