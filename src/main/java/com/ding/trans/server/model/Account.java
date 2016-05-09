package com.ding.trans.server.model;

import org.bson.Document;

import com.ding.trans.server.core.TransUtil;

public class Account {

    private String userName;

    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Document toDocument() {
        Document doc = new Document();
        doc.append("email", userName);
        doc.append("password", TransUtil.cipherSimply(password));
        return doc;
    }

    public static Account fromDocument(Document doc) {
        Account account = new Account();
        account.setUserName(doc.getString("userName"));
        String encyptedPass = doc.getString("password");
        account.setPassword(TransUtil.decipherSimply(encyptedPass));
        return account;
    }

}
