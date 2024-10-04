package com.example.halalzam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private Button home_to_scanner;
    private Button home_to_scanner_by_label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        home_to_scanner=findViewById(R.id.scan_food_by_label);
        home_to_scanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, scanners_screen.class);
                startActivity(intent);

            }
        });

        home_to_scanner_by_label=findViewById(R.id.scan_food_by_ingredients);
        home_to_scanner_by_label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, scan_by_ingredient.class);
                startActivity(intent);

            }
        });







    }

}