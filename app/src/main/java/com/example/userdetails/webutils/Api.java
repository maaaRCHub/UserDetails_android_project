package com.example.userdetails.webutils;

import com.example.userdetails.model.User;
import com.example.userdetails.model.UsersList;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface Api {


    @GET("users")
     Call<List<User>> fetchData();

}
