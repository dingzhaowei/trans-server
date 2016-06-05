package com.ding.trans.server.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.ding.trans.server.model.TransOrder;
import com.ding.trans.server.model.TransOrderColl;

public class TransSite {

    private static final Logger log = LogManager.getLogger(TransSite.class);

    private static final String primaryDomain = "transrush.com";

    private static final TransSite instance = new TransSite();

    private String siteUser, sitePass;

    private Map<String, String> cookies = new ConcurrentHashMap<>();

    private TransSite() {
        String filePath = System.getProperty("siteIden");
        if (filePath == null) {
            String userHome = System.getProperty("user.home");
            filePath = userHome + File.separator + "siteIden";
        }
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(filePath)) {
            props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read siteIden", e);
        }
        siteUser = props.getProperty("UserName");
        sitePass = props.getProperty("Password");
    }

    public static TransSite instance() {
        return instance;
    }

    public void login() throws Exception {
        String passBase = "http://passport." + primaryDomain;
        String url = passBase + "/Login.aspx";
        Response resp = request(url, null);

        Map<String, String> params = new HashMap<>();
        params.put("time", "" + System.currentTimeMillis());
        params.put("type", "3");
        params.put("pwd", sitePass);
        params.put("email", siteUser);
        url = passBase + "/AjaxIndex.aspx";
        resp = request(TransUtil.makeUrlWithQueryParams(url, params), null);

        params = new HashMap<>();
        params.put("time", "" + System.currentTimeMillis());
        params.put("actionType", "0");
        params.put("pwd", sitePass);
        params.put("email", siteUser);
        url = passBase + "/AjaxPassport.aspx";
        resp = request(TransUtil.makeUrlWithQueryParams(url, params), null);

        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(resp.body());
        if (jsonNode.get("Code").getIntValue() != 1) {
            throw new RuntimeException("Failed to login");
        }
        log.info("Successfully logined on the trans site.");
    }

    public TransOrderColl fetchOrders(int pidx, int size) throws Exception {
        String homeBase = "http://www." + primaryDomain;
        String url = homeBase + "/Ajax/AjaxTransportInfo.aspx";
        Map<String, String> params = new HashMap<>();
        params.put("time", "" + System.currentTimeMillis());
        params.put("actionType", "1");
        params.put("pidx", "" + pidx);
        params.put("psize", "" + size);
        params.put("day", "30");
        params.put("pid", "");
        params.put("wid", "");
        params.put("orderno", "");

        url = TransUtil.makeUrlWithQueryParams(url, params);
        Response resp = request(url, null);
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(resp.body());

        TransOrderColl coll = new TransOrderColl();
        JsonNode pidxNode = jsonNode.get("PageIndex");
        coll.setPageIndex(Integer.parseInt(pidxNode.getValueAsText()));
        JsonNode psizeNode = jsonNode.get("PageSize");
        coll.setPageSize(Integer.parseInt(psizeNode.getValueAsText()));
        JsonNode totalCountNode = jsonNode.get("TotalCount");
        coll.setTotalCount(Integer.parseInt(totalCountNode.getValueAsText()));
        JsonNode totalPageNode = jsonNode.get("TotalPage");
        coll.setTotalPage(Integer.parseInt(totalPageNode.getValueAsText()));

        JsonNode resultList = jsonNode.get("ResultList");
        List<TransOrder> orders = new ArrayList<>();
        for (int i = 0; i < resultList.size(); i++) {
            JsonNode orderNode = resultList.get(i);
            TransOrder order = new TransOrder();
            order.setTransId(orderNode.get("DeliveryCode").getValueAsText());
            order.setOrderNo(orderNode.get("OrderNo").getValueAsText());
            order.setHappenTime(orderNode.get("HappenTime").getValueAsText());
            order.setOrderStatus(orderNode.get("OrderState").getValueAsText());
            order.setWarehouseName(orderNode.get("VWarehouseName").getValueAsText());
            order.setTransPlanName(orderNode.get("ProductName").getValueAsText());
            orders.add(order);
        }
        coll.setOrders(orders);
        return coll;
    }

    private Connection connect(String url, Map<String, String> data) {
        Connection conn = Jsoup.connect(url);
        conn.cookies(cookies);
        if (data != null) {
            conn.method(Method.POST).data(data);
        }
        conn.timeout(5000).userAgent(Config.getValue("UserAgent"));
        conn.ignoreContentType(true).excludeExpiredCookies(true);
        return conn;
    }

    private Response request(Connection conn) throws Exception {
        Response resp = conn.execute();
        cookies.putAll(resp.cookies());
        return resp;
    }

    private Response request(String url, Map<String, String> data) throws Exception {
        return request(connect(url, data));
    }

}
