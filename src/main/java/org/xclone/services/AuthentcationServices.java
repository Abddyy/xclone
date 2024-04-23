package org.xclone.services;

import org.jdbi.v3.core.Handle;

public class AuthentcationServices {

    private String get_user_info = "SELECT password FROM \"xcloneSchema\".\"user\" WHERE email = :email";
    private String get_user_name = "SELECT username FROM \"xcloneSchema\".\"user\" WHERE email = :email";
    private String check_signup = "SELECT COUNT(*) FROM \"xcloneSchema\".\"user\" WHERE email = :email";
    private String get_new_user_info="INSERT INTO \"xcloneSchema\".\"user\" (email, password, username) VALUES (:email, :password, :username)";

    public String getUserInfoQuery(Handle handle, String email){
        return handle.createQuery(get_user_info)
                .bind("email", email)
                .mapTo(String.class)
                .findOne()
                .orElse(null);
    }

    public String getUserNameQuery(Handle handle, String email){
        return handle.createQuery(get_user_name)
                .bind("email", email)
                .mapTo(String.class)
                .findOne()
                .orElse(null);
    }

    public long checkSignupAvailability(Handle handle,String email){
        return handle.createQuery(check_signup)
                .bind("email", email)
                .mapTo(Long.class)
                .one();
    }

    public void doSignupQuery(Handle handle,String email,String hashedPassword,String username){
        handle.createUpdate(get_new_user_info)
                .bind("email", email)
                .bind("password", hashedPassword)
                .bind("username", username)
                .execute();
    }
}
