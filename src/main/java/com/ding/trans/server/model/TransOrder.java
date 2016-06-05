package com.ding.trans.server.model;

import java.security.MessageDigest;

import org.bson.Document;

public class TransOrder {

    private String transId;

    private String orderNo;

    private String userName;

    private String happenTime;

    private String orderStatus;

    private String warehouseName;

    private String transPlanName;

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHappenTime() {
        return happenTime;
    }

    public void setHappenTime(String happenTime) {
        this.happenTime = happenTime;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getTransPlanName() {
        return transPlanName;
    }

    public void setTransPlanName(String transPlanName) {
        this.transPlanName = transPlanName;
    }

    public byte[] md5() {
        StringBuilder sb = new StringBuilder();
        sb.append(transId).append(orderNo);
        sb.append(happenTime).append(orderStatus);
        sb.append(warehouseName).append(transPlanName);

        MessageDigest md = null;
        String s = sb.toString();
        try {
            md = MessageDigest.getInstance("MD5");
            return md.digest(s.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Document toDocument() {
        Document doc = new Document();
        doc.append("transId", transId);
        doc.append("orderNo", orderNo);
        doc.append("userName", userName);
        doc.append("happenTime", happenTime);
        doc.append("orderStatus", orderStatus);
        doc.append("warehouseName", warehouseName);
        doc.append("transPlanName", transPlanName);
        return doc;
    }

    public static TransOrder fromDocument(Document doc) {
        TransOrder order = new TransOrder();
        order.setTransId(doc.getString("transId"));
        order.setOrderNo(doc.getString("orderNo"));
        order.setUserName(doc.getString("userName"));
        order.setHappenTime(doc.getString("happenTime"));
        order.setOrderStatus(doc.getString("orderStatus"));
        order.setWarehouseName(doc.getString("warehouseName"));
        order.setTransPlanName(doc.getString("transPlanName"));
        return order;
    }

}
