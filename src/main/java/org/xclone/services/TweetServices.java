package org.xclone.services;
import java.util.List;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import org.xclone.Tweet;

public class TweetServices {
    private String tweet_contant_query=
            "SELECT t.tweet_id, u.username, t.content, t.timestamp, t.location, t.media, t.in_reply_to_tweet_id, COUNT(l.like_id) as like_count " +
            "FROM \"xcloneSchema\".\"tweet\" t " +
            "JOIN \"xcloneSchema\".\"user\" u ON t.user_id = u.user_id " +
            "LEFT JOIN \"xcloneSchema\".\"like\" l ON t.tweet_id = l.tweet_id " +
            "GROUP BY t.tweet_id, u.username " +
            "ORDER BY t.timestamp DESC";

    private String user_id=
            "SELECT user_id FROM \"xcloneSchema\".\"user\" " +
            "WHERE email = :email";

    private String new_tweet_query=
            "INSERT INTO \"xcloneSchema\".\"tweet\" " +
            "(user_id, content, timestamp, location, media, in_reply_to_tweet_id) " +
            "VALUES (:userId, :content, NOW(), :location, :media, :replyToTweetId)";

    private String like_count_query=
            "SELECT COUNT(*) FROM \"xcloneSchema\".\"like\" " +
            "WHERE tweet_id = :tweetId";

    private String remove_like_query=
            "DELETE FROM \"xcloneSchema\".\"like\" " +
            "WHERE tweet_id = :tweetId AND user_id = :userId";

    private String add_like=
            "INSERT INTO \"xcloneSchema\".\"like\" (tweet_id, user_id, timestamp) " +
            "VALUES (:tweetId, :userId, NOW())";

    public List<Tweet> tweetlist(Jdbi jdbi){
        return jdbi.withHandle(handle ->
                handle.createQuery(tweet_contant_query)
                        .map((rs, mapCtx) -> new Tweet(
                                rs.getString("tweet_id"),
                                rs.getString("username"),
                                rs.getString("content"),
                                rs.getTimestamp("timestamp"),
                                rs.getString("location"),
                                rs.getString("media"),
                                rs.getString("in_reply_to_tweet_id"),
                                rs.getInt("like_count")
                        ))
                        .list()
        );
    }

    public Integer getUserID(Handle handle,String email){
        return handle.createQuery(user_id)
                .bind("email", email)
                .mapTo(Integer.class)
                .findOne()
                .orElse(null);
    }

    public void getNewTweet(Handle handle,Integer userId,String content,String location,String media,int replyToTweetId){
        handle.createUpdate(new_tweet_query)
                .bind("userId", userId)
                .bind("content", content)
                .bind("location", location)
                .bind("media", media)
                .bind("replyToTweetId", replyToTweetId==0 ? null : replyToTweetId)
                .execute();
    }

    public boolean isLiked(Handle handle,Integer tweetId,Integer userId){
        return handle.createQuery(like_count_query)
                .bind("tweetId", tweetId)
                .bind("userId", userId)
                .mapTo(Integer.class)
                .one() > 0;
    }

    public void removeLike(Handle handle,int tweetId, int userId){
        handle.createUpdate(remove_like_query)
                .bind("tweetId", tweetId)
                .bind("userId", userId)
                .execute();
    }

    public void addLike(Handle handle,int tweetId,int userId){
        handle.createUpdate(add_like)
                .bind("tweetId", tweetId)
                .bind("userId", userId)
                .execute();
    }


}
