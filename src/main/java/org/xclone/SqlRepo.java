package org.xclone;

public class SqlRepo {
    private String tweet_content_query =
            "SELECT t.tweet_id, u.username, t.content, t.timestamp, t.location, t.media, t.in_reply_to_tweet_id, COUNT(l.like_id) as like_count " +
                    "FROM \"xcloneSchema\".\"tweet\" t " +
                    "JOIN \"xcloneSchema\".\"user\" u ON t.user_id = u.user_id " +
                    "LEFT JOIN \"xcloneSchema\".\"like\" l ON t.tweet_id = l.tweet_id " +
                    "GROUP BY t.tweet_id, u.username " +
                    "ORDER BY t.timestamp DESC";

    private String tweet_profile_query=
            "SELECT t.tweet_id, t.content, t.timestamp AS tempTime, " +
                    "(SELECT COUNT(*) FROM \"xcloneSchema\".\"like\" l WHERE l.tweet_id = t.tweet_id) AS like_count " +
                    "FROM \"xcloneSchema\".\"tweet\" t " +
                    "JOIN \"xcloneSchema\".\"user\" u ON t.user_id = u.user_id " +
                    "WHERE u.username = :username " +
                    "ORDER BY t.timestamp DESC";

    private String user_id_query =
            "SELECT user_id FROM \"xcloneSchema\".\"user\" " +
                    "WHERE email = :email";

    private String get_all_users_query =
            "SELECT username FROM \"xcloneSchema\".\"user\" ";

    private String new_tweet_query =
            "INSERT INTO \"xcloneSchema\".\"tweet\" " +
                    "(user_id, content, timestamp, location, media, in_reply_to_tweet_id) " +
                    "VALUES (:userId, :content, NOW(), :location, :media, :replyToTweetId)";

    private String like_count_query =
            "SELECT COUNT(*) FROM \"xcloneSchema\".\"like\" " +
                    "WHERE tweet_id = :tweetId";

    private String remove_like_query =
            "DELETE FROM \"xcloneSchema\".\"like\" " +
                    "WHERE tweet_id = :tweetId AND user_id = :userId";

    private String add_like_query =
            "INSERT INTO \"xcloneSchema\".\"like\" (tweet_id, user_id, timestamp) " +
                    "VALUES (:tweetId, :userId, NOW())";

    private String get_user_info =
            "SELECT password FROM \"xcloneSchema\".\"user\" WHERE email = :email";

    private String get_user_name =
            "SELECT username FROM \"xcloneSchema\".\"user\" WHERE email = :email";

    private String check_signup =
            "SELECT COUNT(*) FROM \"xcloneSchema\".\"user\" WHERE email = :email";

    private String get_new_user_info =
            "INSERT INTO \"xcloneSchema\".\"user\" (email, password, username) VALUES (:email, :password, :username)";

    public String getTweetContentQuery() {
        return tweet_content_query;
    }
    public String getTweetProfileQuery() {
        return tweet_profile_query;
    }

    public String getAllUsernameQuery() {
        return get_all_users_query;
    }

    public String getUserIdQuery() {
        return user_id_query;
    }

    public String getNewTweetQuery() {
        return new_tweet_query;
    }

    public String getLikeCountQuery() {
        return like_count_query;
    }

    public String getRemoveLikeQuery() {
        return remove_like_query;
    }

    public String getAddLikeQuery() {
        return add_like_query;
    }

    public String getGetUserInfo() {
        return get_user_info;
    }

    public String getGetUserName() {
        return get_user_name;
    }

    public String getCheckSignup() {
        return check_signup;
    }

    public String getNewUserInfo() {
        return get_new_user_info;
    }
}
