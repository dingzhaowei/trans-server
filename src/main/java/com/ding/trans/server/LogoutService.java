package com.ding.trans.server;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogoutService extends ServiceBase {

    private static final long serialVersionUID = 1L;

    private static Logger log = LogManager.getLogger(LogoutService.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            HttpSession session = req.getSession(false);
            String username = session.getAttribute("username").toString();
            session.invalidate();
            log("Received logout request of " + username + " from " + req.getRemoteAddr());
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            log.error("ServerError", e);
            String error = URLEncoder.encode("后台遇到问题，请您反馈", "UTF-8");
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
        }
    }

}
