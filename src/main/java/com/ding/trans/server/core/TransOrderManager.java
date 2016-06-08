package com.ding.trans.server.core;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ding.trans.server.model.TransOrder;
import com.ding.trans.server.model.TransOrderColl;
import com.ding.trans.server.model.TransOrderDetail;

public class TransOrderManager {

    private static final int PAGE_SIZE = 50;

    private static final int MAX_RETRY = 3;

    private static Logger log = LogManager.getLogger(TransOrderManager.class);

    private static TransOrderManager instance = new TransOrderManager();

    private Map<String, byte[]> transOrderSigs;

    private TransDao transDao = TransDao.instance();

    private TransSite transSite = TransSite.instance();

    private TransOrderManager() {
        transOrderSigs = new ConcurrentHashMap<>();
    }

    public static TransOrderManager instance() {
        return instance;
    }

    public void createTransOrderSigs() {
        transDao.walkTransOrders(doc -> {
            TransOrder order = TransOrder.fromDocument(doc);
            transOrderSigs.put(order.getTransId(), order.md5());
        }, 1000);
    }

    public List<TransOrder> getTransOrders(String userName) {
        return transDao.getTransOrders(userName);
    }

    public TransOrderDetail getTransOrderDetail(String userName, String transId) {
        TransOrder order = transDao.getTransOrder(transId);
        try {
            if (order == null) {
                throw new RuntimeException("No order found");
            }
            if (!order.getUserName().equals(userName)) {
                throw new RuntimeException("Not authorized");
            }
            String orderNo = order.getOrderNo();
            int flag = orderNo.equals(transId) ? 2 : 3;
            return transSite.fetchOrderDetail(orderNo, flag);
        } catch (Exception e) {
            String fmt = "Failed to get order detail: %s %s";
            log.error(String.format(fmt, userName, transId), e);
            return TransOrderDetail.createEmptyTransOrderDetail();
        }
    }

    public void syncTransOrders() throws Exception {
        Set<String> s = new HashSet<>();
        log.info("Synchronizing the orders with the site ...");

        TransOrderColl coll = transSite.fetchOrders(1, PAGE_SIZE);
        int totalPage = coll.getTotalPage();
        log.info("Found {} pages [len={}]", totalPage, PAGE_SIZE);
        s.addAll(checkToUpsertTransOrder(coll.getOrders()));

        for (int i = 2; i <= totalPage; i++) {
            int j = 0;
            while (true) {
                try {
                    coll = transSite.fetchOrders(i, PAGE_SIZE);
                } catch (Exception e) {
                    if (j++ >= MAX_RETRY) {
                        log.error("Failed to get page {}", i);
                        throw e;
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        // continue
                    }
                }
                break;
            }
            s.addAll(checkToUpsertTransOrder(coll.getOrders()));
        }

        checkToDeleteTransOrder(s);
        log.info("All the orders are synchronized successfully.");
    }

    private Set<String> checkToUpsertTransOrder(List<TransOrder> orders) {
        Set<String> s = new HashSet<>();
        for (TransOrder order : orders) {
            String transId = order.getTransId();
            if (transOrderSigs.containsKey(transId)) {
                byte[] sig = transOrderSigs.get(transId);
                if (compareTransOrderSigs(sig, order.md5())) {
                    s.add(transId);
                    continue;
                }
                transDao.updateTransOrder(order);
                log.info("Trans order {} is updated", transId);
            } else {
                order.setUserName(Config.getValue("DefaultUser"));
                transDao.insertTransOrder(order);
                log.info("Trans order {} is inserted", transId);
            }
            s.add(transId);
            transOrderSigs.put(transId, order.md5());
        }
        return s;
    }

    private void checkToDeleteTransOrder(Set<String> transOrdersToRetain) {
        for (String transId : transOrderSigs.keySet()) {
            if (transOrdersToRetain.contains(transId)) {
                continue;
            }
            transDao.removeTransOrder(transId);
            transOrderSigs.remove(transId);
            log.info("Trans order {} is removed", transId);
        }
    }

    private boolean compareTransOrderSigs(byte[] sig1, byte[] sig2) {
        if (sig1.length != sig2.length) {
            return false;
        }
        for (int i = 0; i < sig1.length; i++) {
            if (sig1[i] != sig2[i]) {
                return false;
            }
        }
        return true;
    }

}
