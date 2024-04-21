package org.xclone.services;

import org.jdbi.v3.core.Handle;

public class AuthentcationServices {

    private String get_user_info_query = "SELECT password FROM \"xcloneSchema\".\"user\" WHERE email = :email";
    private String check_signup_query = "SELECT COUNT(*) FROM \"xcloneSchema\".\"user\" WHERE email = :email";
    private String get_new_user_info_query="INSERT INTO \"xcloneSchema\".\"user\" (email, password, username) VALUES (:email, :password, :username)";

    public String getUserInfoQuery(Handle handle, String email){
        return handle.createQuery(get_user_info_query)
                .bind("email", email)
                .mapTo(String.class)
                .findOne()
                .orElse(null);
    }

    public long checkSignupAvailability(Handle handle,String email){
        return handle.createQuery(check_signup_query)
                .bind("email", email)
                .mapTo(Long.class)
                .one();
    }

    public void doSignupQuery(Handle handle,String email,String hashedPassword,String username){
        handle.createUpdate(get_new_user_info_query)
                .bind("email", email)
                .bind("password", hashedPassword)
                .bind("username", username)
                .execute();
    }
}
