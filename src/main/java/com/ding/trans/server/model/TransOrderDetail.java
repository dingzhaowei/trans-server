package com.ding.trans.server.model;

public class TransOrderDetail {

    private String formattedDetail;

    private int costsPayFlag;

    private int tariffPayFlag;

    private boolean canBeCancelled;

    public String getFormattedDetail() {
        return formattedDetail;
    }

    public void setFormattedDetail(String formattedDetail) {
        this.formattedDetail = formattedDetail;
    }

    public int getCostsPayFlag() {
        return costsPayFlag;
    }

    public void setCostsPayFlag(int costsPayFlag) {
        this.costsPayFlag = costsPayFlag;
    }

    public int getTariffPayFlag() {
        return tariffPayFlag;
    }

    public void setTariffPayFlag(int tariffPayFlag) {
        this.tariffPayFlag = tariffPayFlag;
    }

    public boolean isCanBeCancelled() {
        return canBeCancelled;
    }

    public void setCanBeCancelled(boolean canBeCancelled) {
        this.canBeCancelled = canBeCancelled;
    }

    public static TransOrderDetail createEmptyTransOrderDetail() {
        // TODO Auto-generated method stub
        return null;
    }

}
