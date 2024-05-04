package org.xclone.services;
import org.jdbi.v3.core.Jdbi;
import org.xclone.SqlRepo;
import org.xclone.User;
import java.util.List;
public class UserServices {

    private SqlRepo sql_repo =new SqlRepo();

    public List<User> userlist(Jdbi jdbi){
        return jdbi.withHandle(handle ->
                handle.createQuery(sql_repo.getAllUsernameQuery())
                        .map((rs, mapCtx) -> new User(
                                rs.getString("email"),
                                rs.getString("username")
                        ))
                        .list()
        );
    }
}
