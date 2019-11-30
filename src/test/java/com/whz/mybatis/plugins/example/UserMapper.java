package com.whz.mybatis.plugins.example;

import java.util.List;

/**
 * Created by wb-whz291815 on 2017/8/21.
 */
public interface UserMapper {

    List<User> getAllUsers();

    void insertUser(User user);

}
