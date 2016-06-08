package com.ding.trans.server.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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
import com.ding.trans.server.model.TransOrderDetail;

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

    public TransOrderDetail fetchOrderDetail(String orderNo, int flag) throws Exception {
        String homeBase = "http://www." + primaryDomain;
        String url = homeBase + "/Ajax/AjaxTransportInfo.aspx";
        Map<String, String> params = new HashMap<>();
        params.put("actionType", "6");
        params.put("orderno", orderNo);
        params.put("ordertypeflag", "" + flag);
        params.put("time", "" + System.currentTimeMillis());

        url = TransUtil.makeUrlWithQueryParams(url, params);
        Response resp = request(url, null);
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(resp.body());

        TransOrderDetail detail = new TransOrderDetail();
        String currency = jsonNode.get("CurrencyCode").getTextValue();
        currency = currency == null || currency.equals("人民币") ? "元" : currency;
        detail.setCanBeCancelled(jsonNode.get("CanBeCancel").getIntValue() != 0);

        StringBuilder sb = new StringBuilder();
        String totalCost = jsonNode.get("TotalCosts").getTextValue();
        int costsPayFlag = jsonNode.get("PayFlag").getIntValue();
        if (Double.parseDouble(totalCost) > 0.0 && costsPayFlag == 0) {
            costsPayFlag = 1;
        }
        detail.setCostsPayFlag(costsPayFlag);
        String payStat = costsPayFlag == 2 ? "已支付" : costsPayFlag == 0 ? "未生成" : "待支付";
        sb.append(String.format("<p>转运总金额：%s %s (%s)</p>", totalCost, currency, payStat));

        sb.append("\n<pre><code>");
        sb.append("包裹重量：" + jsonNode.get("Weight").getTextValue() + " Kg");
        sb.append("\n转运费用：" + jsonNode.get("Costs").getTextValue() + " " + currency);
        sb.append("\n</code></pre>");

        String totalTariff = jsonNode.get("TotalTariff").getTextValue();
        int tariffPayFlag = jsonNode.get("TariffPayFlag").getIntValue();
        if (Double.parseDouble(totalTariff) > 0.0 && tariffPayFlag == 0) {
            tariffPayFlag = 1;
        }
        detail.setTariffPayFlag(tariffPayFlag);
        payStat = tariffPayFlag == 2 ? "已支付" : tariffPayFlag == 0 ? "未生成" : "待支付";
        sb.append(String.format("\n<p>税费：%s %s (%s)</p>", totalTariff, currency, payStat));

        sb.append("\n<pre><code>");
        sb.append("申报价格：" + jsonNode.get("DeclaredPrice").getTextValue());
        sb.append(" " + jsonNode.get("DeclarePriceCurrencyCode").getTextValue());
        sb.append("\n关税费用：" + jsonNode.get("TariffPrice").getTextValue() + " " + currency);
        sb.append("\n</code></pre>");

        JsonNode productList = jsonNode.get("ProductList");
        sb.append("\n<p>产品：</p>");
        sb.append("\n<pre><code>");
        for (int i = 0; i < productList.size(); i++) {
            JsonNode product = productList.get(i);
            if (i > 0) {
                sb.append("\n\n");
            }
            sb.append("类别：" + product.get("CatagoryName").getTextValue());
            sb.append("\n名称：" + product.get("ProductName").getTextValue());
            sb.append("\n数量：" + product.get("ProductNumber").getIntValue());
            sb.append(" 价格：" + product.get("ProductPrice").getTextValue());
            sb.append(" " + product.get("CurrencyName").getTextValue());
        }
        sb.append("\n</code></pre>");

        File tmplFile = new File(Config.getResource("trans-order-detail.tmpl").toURI());
        FileReader reader = new FileReader(tmplFile);
        char[] cbuf = new char[1024 * 4];
        StringBuilder tmpl = new StringBuilder();
        try {
            int n;
            while ((n = reader.read(cbuf)) != -1) {
                tmpl.append(new String(cbuf, 0, n));
            }
        } finally {
            reader.close();
        }
        detail.setFormattedDetail(tmpl.toString().replace("${content}", sb.toString()));
        return detail;
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
