package com.ding.trans.server.core;

import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;

import com.ding.trans.server.model.Account;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

public class TransDao {

    private static final TransDao instance = new TransDao();

    private MongoClient mongoClient = new MongoClient();

    private TransDao() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                mongoClient.close();
            }
        });
    }

    public static TransDao instance() {
        return instance;
    }

    public Account getAccount(String userName) {
        MongoCollection<Document> coll = getCollection("accounts");
        Document doc = coll.find(eq("username", userName)).first();
        return doc == null ? null : Account.fromDocument(doc);
    }

    private MongoCollection<Document> getCollection(String name) {
        return mongoClient.getDatabase("lunatrans").getCollection(name);
    }

}
