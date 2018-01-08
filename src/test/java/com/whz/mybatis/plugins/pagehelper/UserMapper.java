package com.whz.mybatis.plugins.pagehelper;

import java.util.List;

/**
 * Created by wb-whz291815 on 2017/8/21.
 */
public interface UserMapper {

    List<User> getAllUsers();

    void insertUser(User user);

}
