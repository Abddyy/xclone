package org.xclone;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class  Tweet {
    String tweetId, username, content, location, media, replyToTweetId, formattedTimestamp;
    java.sql.Timestamp timestamp;
    int likeCount;

    public Tweet(){}
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTweetId(String tweetId) {
        this.tweetId = tweetId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public void setFormattedTimestamp(String formattedTimestamp) {
        this.formattedTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(formattedTimestamp);
    }

    public void setReplyToTweetId(String replyToTweetId) {
        this.replyToTweetId = replyToTweetId;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
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
