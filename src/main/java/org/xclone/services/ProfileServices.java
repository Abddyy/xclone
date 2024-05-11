package org.xclone.services;

import org.jdbi.v3.core.Jdbi;
import org.xclone.SqlRepo;
import org.xclone.Tweet;

import java.util.List;


public class ProfileServices {
    private SqlRepo sqlRepo=new SqlRepo();

    public List<Tweet> findTweetsByUsername(Jdbi jdbi,String username) {

        return jdbi.withHandle(handle ->
                handle.createQuery(sqlRepo.getTweetProfileQuery())
                        .bind("username", username)
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
}
