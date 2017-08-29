package com.whz.mybatis.resultMap;

import java.util.List;

/**
 * Created by wb-whz291815 on 2017/8/21.
 */
public interface UserMapper {

    User getUserById(int i);
    List<User> getAllUsers();
    void insertUser1(com.whz.mybatis.typeHandler.User user);

}
