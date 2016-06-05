package com.ding.trans.server;

import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ding.trans.server.core.Config;
import com.ding.trans.server.core.TransOrderManager;

public class TransServerTimerTask extends TimerTask {

    private static Logger log = LogManager.getLogger(TransServerTimerTask.class);

    private long lastTransOrderSyncTime;

    private long transOrderSyncInterval;

    public TransServerTimerTask() {
        transOrderSyncInterval = Config.getLong("TransOrderSyncInterval");
    }

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastTransOrderSyncTime >= transOrderSyncInterval) {
            try {
                TransOrderManager.instance().syncTransOrders();
                lastTransOrderSyncTime = System.currentTimeMillis();
            } catch (Exception e) {
                log.error("Failed to synchronize the trans orders", e);
            }
        }
    }

}
