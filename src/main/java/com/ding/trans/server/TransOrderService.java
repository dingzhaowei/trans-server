package com.ding.trans.server;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ding.trans.server.core.TransOrderManager;
import com.ding.trans.server.model.TransOrder;
import com.ding.trans.server.model.TransOrderDetail;

public class TransOrderService extends ServiceBase {

    private static final long serialVersionUID = 1L;

    private static Logger log = LogManager.getLogger(TransOrderService.class);

    private static TransOrderManager manager = TransOrderManager.instance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userName = (String) req.getSession().getAttribute("username");
        Map<String, String> params = getRequestParams(req);
        String action = params.get("action");

        try {
            if (action.equals("getOrders")) {
                List<TransOrder> result = manager.getTransOrders(userName);
                sendResult(resp, om.writeValueAsString(result), true);
            } else if (action.equals("getDetail")) {
                String transId = params.get("transId");
                TransOrderDetail result = null;
                result = manager.getTransOrderDetail(userName, transId);
                sendResult(resp, om.writeValueAsString(result), true);
            } else {
                String error = URLEncoder.encode("不支持的操作: " + action, "UTF-8");
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
            }
        } catch (Exception e) {
            log.error("ServerError", e);
            String error = URLEncoder.encode("后台遇到问题，请您反馈", "UTF-8");
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
        }
    }

}
