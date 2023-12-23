package it.unipi.lsmsdb.bookadvisor.service;

import it.unipi.lsmsdb.bookadvisor.dao.documentDB.VoteDao;
import it.unipi.lsmsdb.bookadvisor.model.review.Review;
import it.unipi.lsmsdb.bookadvisor.model.vote.Vote;
import org.bson.types.ObjectId;

public class VoteService {

    private VoteDao voteDao;

    public VoteService(VoteDao voteDao) {
        this.voteDao = voteDao;
    }

    // Business logic for casting a vote
    public boolean castVote(Vote vote, Review associatedReview) {
        // Check if the vote is for a non-empty review and not from the same user
        if (isValidVote(vote, associatedReview)) {
            return voteDao.castVote(vote);
        }
        return false;
    }

    // Business logic for updating a vote
    public boolean updateVote(ObjectId voteId, boolean upVote, String userId) {
        // Check if the vote is from the same user
        if (isVoteFromUser(voteId, userId)) {
            return voteDao.updateVote(voteId, upVote);
        }
        return false;
    }

    // Business logic for removing a vote
    public boolean removeVote(ObjectId voteId, String userId) {
        // Check if the vote is from the same user
        if (isVoteFromUser(voteId, userId)) {
            return voteDao.removeVote(voteId);
        }
        return false;
    }

    // Additional business logic for validating a vote
    private boolean isValidVote(Vote vote, Review review) {
        return vote != null
                && review != null
                && !new ObjectId(vote.getUserId().toHexString()).equals(new ObjectId(review.getUserId().toHexString())) // Ensure the vote is not from the same user
                && review.getText() != null && !review.getText().isEmpty(); // Ensure the associated review has non-empty text
    }

    // Additional business logic for checking if a vote is from the same user
    private boolean isVoteFromUser(ObjectId voteId, String userId) {
        Vote existingVote = voteDao.getVoteById(voteId);
        return existingVote != null && existingVote.getUserId().equals(new ObjectId(userId));
    }
}
