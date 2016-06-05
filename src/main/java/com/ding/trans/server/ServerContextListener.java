package com.ding.trans.server;

import java.util.Timer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.ding.trans.server.core.TransOrderManager;
import com.ding.trans.server.core.TransSite;

public class ServerContextListener implements ServletContextListener {

    private static Timer timer;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            TransOrderManager.instance().createTransOrderSigs();
            TransSite.instance().login();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize", e);
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TransServerTimerTask(), 0, 5000);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
