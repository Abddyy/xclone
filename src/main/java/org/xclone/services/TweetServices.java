package org.xclone.services;
import java.util.List;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import org.xclone.SqlRepo;
import org.xclone.Tweet;

public class TweetServices {
        SqlRepo sql_repo =new SqlRepo();

    public List<Tweet> tweetlist(Jdbi jdbi){
        return jdbi.withHandle(handle ->
                handle.createQuery(sql_repo.getTweetContentQuery())
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
        return handle.createQuery(sql_repo.getUserIdQuery())
                .bind("email", email)
                .mapTo(Integer.class)
                .findOne()
                .orElse(null);
    }

    public void getNewTweet(Handle handle,Integer userId,String content,String location,String media,int replyToTweetId){
        handle.createUpdate(sql_repo.getNewTweetQuery())
                .bind("userId", userId)
                .bind("content", content)
                .bind("location", location)
                .bind("media", media)
                .bind("replyToTweetId", replyToTweetId==0 ? null : replyToTweetId)
                .execute();
    }

    public boolean isLiked(Handle handle,Integer tweetId,Integer userId){
        return handle.createQuery(sql_repo.getAddLikeQuery())
                .bind("tweetId", tweetId)
                .bind("userId", userId)
                .mapTo(Integer.class)
                .one() > 0;
    }

    public void removeLike(Handle handle,int tweetId, int userId){
        handle.createUpdate(sql_repo.getRemoveLikeQuery())
                .bind("tweetId", tweetId)
                .bind("userId", userId)
                .execute();
    }

    public void addLike(Handle handle,int tweetId,int userId){
        handle.createUpdate(sql_repo.getAddLikeQuery())
                .bind("tweetId", tweetId)
                .bind("userId", userId)
                .execute();
    }


}
