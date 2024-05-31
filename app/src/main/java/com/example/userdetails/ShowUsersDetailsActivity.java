package com.example.userdetails;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ShowUsersDetailsActivity extends AppCompatActivity {

    DBHelper dbHelper;
    RecyclerView recyclerView;
    UsersDetailsAdapter usersDetailsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_users_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        dbHelper = new DBHelper(this);
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        List<DBUsersDetails> usersDetailsList = dbHelper.getAllUsers();
        if (usersDetailsList == null) {
            return;
        }
        usersDetailsAdapter = new UsersDetailsAdapter(this, usersDetailsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(usersDetailsAdapter);
    }
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView_users_details);
    }
}