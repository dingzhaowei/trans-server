package com.ding.trans.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.ding.trans.server.core.TransSite;

public class ServerContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            TransSite.instance().login();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
