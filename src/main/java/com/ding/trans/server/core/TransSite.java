package com.ding.trans.server.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
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
        log.info("Login successfully: ", jsonNode.toString());
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
