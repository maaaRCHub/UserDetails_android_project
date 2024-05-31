package com.example.userdetails.model;

import com.google.gson.annotations.Expose;

import java.util.List;

public class UsersList {
    @Expose
    List<User> userList;

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    @Override
    public String toString() {
        return "UsersList{" +
                "userList=" + userList +
                '}';
    }
}
