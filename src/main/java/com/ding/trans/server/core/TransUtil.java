package com.ding.trans.server.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class TransUtil {

    public static String normalizedPath(String path) {
        try {
            return URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void savePageSource(Document doc, String path) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(path);
            writer.print(doc.outerHtml());
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static Map<String, String> extractFormData(Element form) {
        Map<String, String> data = new HashMap<String, String>();
        for (Element input : form.getElementsByTag("input")) {
            if (!input.attr("type").equals("submit")) {
                String name = input.attr("name");
                String value = input.attr("value");
                if (!name.isEmpty()) {
                    data.put(name, value);
                }
            }
        }
        return data;
    }

    public static Object castValueToType(Object value, Class<?> type) {
        if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            value = Boolean.valueOf(value.toString());
        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            value = Integer.parseInt(value.toString());
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            value = Long.valueOf(value.toString());
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            value = Double.parseDouble(value.toString());
        }
        return value;
    }

    public static String cipherSimply(String s) {
        try {
            byte[] bytes = s.getBytes("UTF-8");
            byte[] encoded = Base64.getEncoder().encode(bytes);
            for (int i = 0; i < encoded.length - 3; i += 2) {
                byte b = encoded[i];
                encoded[i] = encoded[i + 1];
                encoded[i + 1] = b;
            }
            return new String(encoded, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decipherSimply(String s) {
        try {
            byte[] encoded = s.getBytes("UTF-8");
            for (int i = 0; i < encoded.length - 3; i += 2) {
                byte b = encoded[i];
                encoded[i] = encoded[i + 1];
                encoded[i + 1] = b;
            }
            byte[] bytes = Base64.getDecoder().decode(encoded);
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String compress(String s) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(s.getBytes("UTF-8"));
        gzip.close();
        return new String(Base64.getEncoder().encode(out.toByteArray()), "UTF-8");
    }

    public static String decompress(String s) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(s));
        GZIPInputStream gzip = new GZIPInputStream(in);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int i = 0;
        while ((i = gzip.read(buf)) >= 0) {
            out.write(buf, 0, i);
        }
        gzip.close();
        return out.toString("UTF-8");
    }

    public static String makeUrlWithQueryParams(String url, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }

        StringBuilder sb = new StringBuilder(url + "?");
        Iterator<Entry<String, String>> iter = params.entrySet().iterator();
        try {
            while (iter.hasNext()) {
                Entry<String, String> entry = iter.next();
                String key = URLEncoder.encode(entry.getKey(), "UTF-8");
                String value = URLEncoder.encode(entry.getValue(), "UTF-8");
                sb.append(String.format("%s=%s", key, value));
                if (iter.hasNext()) {
                    sb.append('&');
                }
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to make url with query params", e);
        }
    }

}
