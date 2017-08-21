package com.whz.mybatis.typeHandler;

import java.util.List;

/**
 * Created by wb-whz291815 on 2017/8/21.
 */
public interface UserMapper {

    User getUserById(int i);
    List<User> getAllUsers();
    void insertUser1(User user);
    void insertUser2(User user);
    void insertUser3(User user);


}
