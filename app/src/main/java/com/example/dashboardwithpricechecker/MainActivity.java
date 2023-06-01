package com.example.dashboardwithpricechecker;

import android.content.SharedPreferences;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static ArrayList<Product> productList = new ArrayList<Product>();

    private ListView listView;
    private String selectedFilter = "all";

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showHomeFragment();

        bottomNavigationView = findViewById(R.id.bottom_nav);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                switch (item.getItemId()){
                    case R.id.mnu_home:

                        transaction.replace(R.id.fragment_container, new HomeFragment());
                        transaction.commit();
                        break;

                    case R.id.mnu_product:
                        transaction.replace(R.id.fragment_container, new ProductFinder());
                        transaction.commit();
                        break;

                    case R.id.mnu_pricechecker:
                        transaction.replace(R.id.fragment_container, new pricechecker());
                        transaction.commit();
                        break;

                    case R.id.mnu_receipt:
                        transaction.replace(R.id.fragment_container, new Receipt());
                        transaction.commit();
                        break;
                }
                return true;
            }
        });



    }
    private void showHomeFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, new HomeFragment());
        transaction.commit();
    }
}