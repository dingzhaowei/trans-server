package com.ding.trans.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import com.ding.trans.server.core.TransUtil;

public class ServiceBase extends HttpServlet {

    private static final long serialVersionUID = 4547341216443613175L;

    protected static final ObjectMapper om = new ObjectMapper();

    protected Map<String, String> getRequestParams(HttpServletRequest req)
            throws UnsupportedEncodingException, IOException {
        BufferedReader reader = req.getReader();
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            sb.append(line + "\n");
        }
        String entity = sb.toString().trim();

        Map<String, String> params = new HashMap<>();
        for (String entry : entity.split("&")) {
            String[] pair = entry.split("=");
            String key = pair[0];
            String value = pair.length >= 2 ? URLDecoder.decode(pair[1], "UTF-8") : null;
            params.put(key, value);
        }
        return params;
    }

    protected void sendResult(HttpServletResponse resp, String result, boolean gzipped) throws IOException {
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        OutputStream output = resp.getOutputStream();
        if (gzipped) {
            result = TransUtil.compress(result);
        }
        output.write(result.getBytes("UTF-8"));
        output.flush();
    }

}
