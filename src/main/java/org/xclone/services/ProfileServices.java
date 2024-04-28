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
                        .mapToBean(Tweet.class)
                        .list());
    }
}
