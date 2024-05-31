package com.example.userdetails;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.userdetails.model.User;
import com.example.userdetails.webutils.Api;
import com.example.userdetails.webutils.RetrofitInstance;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    Button btnViewUsers;
    LinearLayout btnDownload;
    RetrofitInstance retrofitInstance;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        dbHelper = new DBHelper(this);

        btnViewUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShowUsersDetailsActivity.class);
                startActivity(intent);
            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Api api = RetrofitInstance.getRetrofit().create(Api.class);
                Call<List<User>> call = api.fetchData();

                call.enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                        if (response.isSuccessful()) {
                            Log.d("TAG", "onResponse: SUCCESS" + response.body());
                            Toast.makeText(MainActivity.this, "Data downloaded", Toast.LENGTH_SHORT).show();

                            List<User> userList = response.body();
                            for (User user : userList) {
                                DBUsersDetails dbUsersDetails = new DBUsersDetails();
                                if (user.getId() == 1) {
                                    dbHelper.deleteUser();
                                }

                                dbUsersDetails.setId(user.getId());
                                dbUsersDetails.setName(user.getFirstname() + " " + user.getLastname());
                                dbUsersDetails.setEmail(user.getEmail());
                                dbUsersDetails.setAddress(user.getAddress().toString());
                                dbUsersDetails.setLocation(user.getAddress().getGeo().getLat() + ", " + user.getAddress().getGeo().getLng());
                                dbUsersDetails.setBirthDate(user.getBirthDate());
                                dbUsersDetails.setUsername(user.getLogin().getUsername());
                                dbUsersDetails.setPhone(user.getPhone());
                                dbUsersDetails.setPhoto("NULL");
                                dbHelper.insertUser(dbUsersDetails);

                            }


                        }
                    }

                    @Override
                    public void onFailure(Call<List<User>> call, Throwable throwable) {
                        Log.d("TAG", "onResponse: FAILED" + throwable.getMessage());
                        Toast.makeText(MainActivity.this, "Failed to download data", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });


    }



    private void initViews() {
        btnViewUsers = findViewById(R.id.button_view_users);
        btnDownload = findViewById(R.id.lLayout_download);
    }
}