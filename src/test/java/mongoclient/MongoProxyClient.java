package mongoclient;

import com.client.exception.IgnoredException;
import com.client.proxy.ClosableClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

/**
 * @author 谢俊权
 * @create 2016/5/12 18:28
 */
public class MongoProxyClient implements ClosableClient {

    private com.mongodb.MongoClient client;
    private String database;

    public MongoProxyClient(String host, int port, String database) {
        this.database = database;
        this.client = new com.mongodb.MongoClient(host, port);
    }

    public void insertOne(String collection, Document document) {
        this.client.getDatabase(this.database).getCollection(collection).insertOne(document);
    }

    public void insertMany(String collection, List<Document> documents) {
        this.client.getDatabase(this.database).getCollection(collection).insertMany(documents);
    }

    public DeleteResult deleteOne(String collection, Bson filter) {
        return this.client.getDatabase(this.database).getCollection(collection).deleteOne(filter);
    }

    public DeleteResult deleteMany(String collection, Bson filter) {
        return this.client.getDatabase(this.database).getCollection(collection).deleteMany(filter);
    }

    public UpdateResult updateOne(String collection, Bson filter, Bson update) {
        return this.client.getDatabase(this.database).getCollection(collection).updateOne(filter, update);
    }

    public UpdateResult updateMany(String collection, Bson filter, Bson update) {
        return this.client.getDatabase(this.database).getCollection(collection).updateMany(filter, update);
    }

    public FindIterable<Document> find(String collection) {
        return this.client.getDatabase(this.database).getCollection(collection).find();
    }
    public FindIterable<Document> find(String collection, Bson filter) {
        return this.client.getDatabase(this.database).getCollection(collection).find(filter);
    }


    public String createIndex(String collection, Bson keys) {
        return this.client.getDatabase(this.database).getCollection(collection).createIndex(keys);
    }

    public void dropIndex(String collection, Bson keys) {
        this.client.getDatabase(this.database).getCollection(collection).dropIndex(keys);
    }

    public long count(String collection) {
        return this.client.getDatabase(this.database).getCollection(collection).count();
    }

    public long count(String collection, Bson filter) {
        return this.client.getDatabase(this.database).getCollection(collection).count(filter);
    }

    @Override
    public void close(){
        try{
            client.close();
        }catch (Exception e){
            throw new IgnoredException(e);
        }
    }
}
