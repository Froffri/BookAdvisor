package it.unipi.lsmsdb.bookadvisor.service;

import it.unipi.lsmsdb.bookadvisor.dao.graphDB.FollowGraphDAO;
import it.unipi.lsmsdb.bookadvisor.model.follow.Follow;
import it.unipi.lsmsdb.bookadvisor.model.user.Reviewer;
import org.bson.types.ObjectId;

public class FollowService {
    private final FollowGraphDAO followGraphDAO;

    public FollowService(FollowGraphDAO followGraphDAO) {
        this.followGraphDAO = followGraphDAO;
    }

    // Create

    /**
     * Add a follow relationship between two users.
     * @param follower the user who follows.
     * @param followed the user who is followed.
     */
    public boolean addFollow(Reviewer follower, Reviewer followed) {
        return followGraphDAO.addFollow(follower, followed);
    }

    /**
     * Add a follow relationship using Follow object.
     * @param follow the follow relationship object.
     */
    public boolean addFollow(Follow follow) {
        return followGraphDAO.addFollow(follow);
    }

    /**
     * Add a follow relationship using user IDs.
     * @param followerId the ID of the follower.
     * @param followedId the ID of the followed.
     */
    public boolean followUser(ObjectId followerId, ObjectId followedId) {        
        return followGraphDAO.addFollowByIds(followerId, followedId);
    }

    // Read

    /**
     * Check if a follow relationship exists between two users.
     * @param follower the user who follows.
     * @param followed the user who is followed.
     * @return true if the follow relationship exists, otherwise false.
     */
    public boolean isFollowing(Reviewer follower, Reviewer followed) {
        return followGraphDAO.getFollow(follower, followed);
    }

    /**
     * Check if a follow relationship exists using user IDs.
     * @param followerId the ID of the follower.
     * @param followedId the ID of the followed.
     * @return true if the follow relationship exists, otherwise false.
     */
    public boolean isFollowingByIds(ObjectId followerId, ObjectId followedId) {
        return followGraphDAO.getFollowbyId(followerId, followedId);
    }

    // Delete

    /**
     * Delete a follow relationship between two users.
     * @param follower the user who follows.
     * @param followed the user who is followed.
     */
    public boolean removeFollow(Reviewer follower, Reviewer followed) {
        return followGraphDAO.deleteFollow(follower, followed);
    }

    /**
     * Delete a follow relationship using Follow object.
     * @param follow the follow relationship object.
     */
    public boolean removeFollow(Follow follow) {
        return followGraphDAO.deleteFollow(follow);
    }

    /**
     * Delete a follow relationship using user IDs.
     * @param followerId the ID of the follower.
     * @param followedId the ID of the followed.
     */
    public boolean removeFollowByIds(ObjectId followerId, ObjectId followedId) {
        return followGraphDAO.deleteFollow(followerId, followedId);
    }
}
