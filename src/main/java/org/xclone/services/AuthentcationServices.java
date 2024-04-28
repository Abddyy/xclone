package org.xclone.services;

import org.jdbi.v3.core.Handle;
import org.xclone.SqlRepo;

public class AuthentcationServices {
    private SqlRepo sqlRepo=new SqlRepo();

    public String getUserInfoQuery(Handle handle, String email){
        return handle.createQuery(sqlRepo.getGetUserInfo())
                .bind("email", email)
                .mapTo(String.class)
                .findOne()
                .orElse(null);
    }

    public String getUserNameQuery(Handle handle, String email){
        return handle.createQuery(sqlRepo.getGetUserName())
                .bind("email", email)
                .mapTo(String.class)
                .findOne()
                .orElse(null);
    }

    public long checkSignupAvailability(Handle handle,String email){
        return handle.createQuery(sqlRepo.getCheckSignup())
                .bind("email", email)
                .mapTo(Long.class)
                .one();
    }

    public void doSignupQuery(Handle handle,String email,String hashedPassword,String username){
        handle.createUpdate(sqlRepo.getNewUserInfo())
                .bind("email", email)
                .bind("password", hashedPassword)
                .bind("username", username)
                .execute();
    }
}
