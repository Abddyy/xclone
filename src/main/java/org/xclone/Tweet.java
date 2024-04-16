package org.xclone;

import java.text.SimpleDateFormat;

public class  Tweet {
    String tweetId, username, content, location, media, replyToTweetId, formattedTimestamp;
    java.sql.Timestamp timestamp;
    int likeCount;

    public Tweet(String tweetId, String username, String content, java.sql.Timestamp timestamp, String location, String media, String replyToTweetId, int likeCount) {
        this.tweetId = tweetId;
        this.username = username;
        this.content = content;
        this.timestamp = timestamp;
        this.location = location;
        this.media = media;
        this.replyToTweetId = replyToTweetId;
        this.likeCount = likeCount;
        this.formattedTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp);
    }

    // Getters
    public String getTweetId() { return tweetId; }
    public String getUsername() { return username; }
    public String getContent() { return content; }
    public java.sql.Timestamp getTimestamp() { return timestamp; }
    public String getLocation() { return location; }
    public String getMedia() { return media; }
    public String getReplyToTweetId() { return replyToTweetId; }
    public String getFormattedTimestamp() { return formattedTimestamp; }
    public int getLikeCount() { return likeCount; }
}
