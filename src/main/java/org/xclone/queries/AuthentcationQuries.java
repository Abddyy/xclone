package org.xclone.queries;

import org.jdbi.v3.core.Handle;

public class AuthentcationQuries {

    private String loginQuery = "SELECT password FROM \"xcloneSchema\".\"user\" WHERE email = :email";

    public String getLoginQuery(Handle handle, String email){
        return handle.createQuery(loginQuery)
                .bind("email", email)
                .mapTo(String.class)
                .findOne()
                .orElse(null);
    }
}
