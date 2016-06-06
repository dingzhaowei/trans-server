package com.ding.trans.server.core;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.ding.trans.server.model.Account;
import com.ding.trans.server.model.TransOrder;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

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

    public static interface DocumentHandler {
        void handleDocument(Document doc);
    }

    public static TransDao instance() {
        return instance;
    }

    public Account getAccount(String userName) {
        MongoCollection<Document> coll = getCollection("accounts");
        Document doc = coll.find(eq("userName", userName)).first();
        return doc == null ? null : Account.fromDocument(doc);
    }

    public List<TransOrder> getTransOrders(String userName) {
        MongoCollection<Document> coll = getCollection("transOrders");
        List<TransOrder> orders = new ArrayList<>();
        Document filter = new Document("userName", userName);
        for (Document doc : coll.find(filter)) {
            orders.add(TransOrder.fromDocument(doc));
        }
        return orders;
    }

    public void insertTransOrder(TransOrder order) {
        MongoCollection<Document> coll = getCollection("transOrders");
        Document doc = order.toDocument();
        doc.append("garbage", "************************");
        coll.insertOne(doc);

        Document filter = new Document("_id", doc.getObjectId("_id"));
        Document update = new Document("garbage", true);
        coll.updateOne(filter, new Document("$unset", update));
    }

    public void updateTransOrder(TransOrder order) {
        MongoCollection<Document> coll = getCollection("transOrders");
        Document update = order.toDocument();
        update.remove("userName");
        String transId = (String) update.remove("transId");
        Document filter = new Document("transId", transId);
        coll.updateOne(filter, new Document("$set", update));
    }

    public void removeTransOrder(String transId) {
        MongoCollection<Document> coll = getCollection("transOrders");
        coll.deleteOne(new Document("transId", transId));
    }

    public void walkTransOrders(DocumentHandler handler, int blockSize) {
        MongoCollection<Document> coll = getCollection("transOrders");
        ObjectId lastId = null;
        Document sort = new Document("_id", 1);

        while (true) {
            FindIterable<Document> iter = null;
            if (lastId == null) {
                iter = coll.find().sort(sort).limit(blockSize);
            } else {
                Bson filter = Filters.gt("_id", lastId);
                iter = coll.find(filter).sort(sort).limit(blockSize);
            }
            List<Document> documents = new ArrayList<>();
            for (Document doc : iter) {
                documents.add(doc);
                handler.handleDocument(doc);
            }
            if (documents.isEmpty()) {
                break;
            }
            lastId = documents.get(documents.size() - 1).getObjectId("_id");
        }
    }

    private MongoCollection<Document> getCollection(String name) {
        return mongoClient.getDatabase("lunatrans").getCollection(name);
    }

}
