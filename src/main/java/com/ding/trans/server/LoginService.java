package com.ding.trans.server;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ding.trans.server.core.AccountManager;

public class LoginService extends ServiceBase {

    private static final long serialVersionUID = 1L;

    private static Logger log = LogManager.getLogger(LoginService.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> params = getRequestParams(req);
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");

        String username = params.get("username");
        String password = params.get("password");
        log("Received login request of " + username + " from " + req.getRemoteAddr());

        try {
            String p = AccountManager.instance().getPassword(username);
            if (p != null && p.equals(password)) {
                log("Successful login of " + username);
                HttpSession session = req.getSession();
                session.setAttribute("username", username);
                session.setMaxInactiveInterval(24 * 60 * 60);
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                log("Failed login of " + username + " with password " + password);
                String error = URLEncoder.encode("账号或密码不正确", "UTF-8");
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, error);
            }
        } catch (Exception e) {
            log.error("ServerError", e);
            String error = URLEncoder.encode("后台遇到问题，请您反馈", "UTF-8");
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
        }
    }

}
