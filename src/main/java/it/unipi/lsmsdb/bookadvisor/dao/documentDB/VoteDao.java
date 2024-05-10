package it.unipi.lsmsdb.bookadvisor.dao.documentDB;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;

public class VoteDao {

    private MongoCollection<Document> collection;

    public VoteDao(MongoDBConnector connector) {
        MongoDatabase database = connector.getDatabase();
        this.collection = database.getCollection("votes");
    }

    // Method to cast a vote
    public boolean castVote(Vote vote) {
        try {
            Document voteDoc = vote.toDocument();
            UpdateResult result = collection.updateOne(
                    Filters.and(
                            Filters.eq("reviewId", vote.getReviewId()),
                            Filters.eq("userId", vote.getUserId())
                    ),
                    new Document("$set", voteDoc),
                    new UpdateOptions().upsert(true)
            );
            return result.wasAcknowledged();
        } catch (Exception e) {
            System.err.println("Error casting vote: " + e.getMessage());
            return false;
        }
    }

    // Method to update a vote
    public boolean updateVote(ObjectId voteId, boolean upDown) {
        try {
            UpdateResult result = collection.updateOne(
                    Filters.eq("_id", voteId),
                    new Document("$set", new Document("upDown", upDown))
            );
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error updating vote: " + e.getMessage());
            return false;
        }
    }

    // Method to remove a vote
    public boolean removeVote(ObjectId voteId) {
        try {
            return collection.deleteOne(Filters.eq("_id", voteId)).getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error removing vote: " + e.getMessage());
            return false;
        }
    }

    // Method to get a vote by its id
    public Vote getVoteById(ObjectId voteId) {
        try {
            Document voteDoc = collection.find(Filters.eq("_id", voteId)).first();
            if (voteDoc == null) return null;
            return new Vote(voteDoc);
        } catch (Exception e) {
            System.err.println("Error getting vote by id: " + e.getMessage());
            return null;
        }
    }

    // Method to get a vote by its review id and user id
    public Vote getVoteByReviewAndUser(ObjectId reviewId, String userId) {
        try {
            Document voteDoc = collection.find(
                    Filters.and(
                            Filters.eq("reviewId", reviewId),
                            Filters.eq("userId", userId)
                    )
            ).first();
            if (voteDoc == null) return null;
            return new Vote(voteDoc);
        } catch (Exception e) {
            System.err.println("Error getting vote by review and user: " + e.getMessage());
            return null;
        }
    }
}
