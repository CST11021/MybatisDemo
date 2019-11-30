package com.whz.mybatis.plugins.example;

import java.util.Date;

public class User {
    private Long id;
    private String username;
    private String password;
    private Date regTime;


    public User() {
    }
    public User(String username, String password, Date regTime) {
        this.username = username;
        this.password = password;
        this.regTime = regTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", regTime=" + regTime +
                '}';
    }

    // getter and setter ...
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Date getRegTime() {
        return regTime;
    }
    public void setRegTime(Date regTime) {
        this.regTime = regTime;
    }
}