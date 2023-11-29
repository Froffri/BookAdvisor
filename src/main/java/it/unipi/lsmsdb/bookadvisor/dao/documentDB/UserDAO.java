package it.unipi.lsmsdb.bookadvisor.dao.documentDB;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import it.unipi.lsmsdb.bookadvisor.model.User;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private static final String COLLECTION_NAME = "users";
    private MongoCollection<Document> collection;

    public UserDao(MongoDBConnector connector) {
        MongoDatabase database = connector.getDatabase();
        collection = database.getCollection(COLLECTION_NAME);
    }

    // Insert a new user into the database
    public void insertUser(User user) {
        Document doc = user.toDocument();
        collection.insertOne(doc);
    }

    // Find a user by their ID
    public User findUserById(String id) {
        Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
        return doc != null ? new User(doc) : null;
    }

    // Find a user by their username
    public User findUserByUsername(String username) {
        Document doc = collection.find(Filters.eq("username", username)).first();
        return doc != null ? new User(doc) : null;
    }

    // Update a user's information
    public boolean updateUser(String id, User user) {
        UpdateResult result = collection.updateOne(Filters.eq("_id", new ObjectId(id)), new Document("$set", user.toDocument()));
        return result.getModifiedCount() > 0;
    }

    // Delete a user from the database
    public boolean deleteUser(String id) {
        DeleteResult result = collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
        return result.getDeletedCount() > 0;
    }

    // List all users
    public List<User> listAllUsers() {
        List<User> users = new ArrayList<>();
        for (Document doc : collection.find()) {
            users.add(new User(doc));
        }
        return users;
    }
}
