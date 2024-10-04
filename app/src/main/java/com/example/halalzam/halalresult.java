package com.example.halalzam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class halalresult extends AppCompatActivity {

    private TextView productNameTextView;
    private TextView productStatusTextView;
    private TextView productEcodesTextView;
    private ImageView capturedImageView;

    // Constants for intent extras
    private static final String EXTRA_PRODUCT_NAME = "PRODUCT_NAME";
    private static final String EXTRA_PRODUCT_STATUS = "PRODUCT_STATUS";
    private static final String EXTRA_PRODUCT_ECODES = "PRODUCT_ECODES";
    private static final String EXTRA_CAPTURED_IMAGE = "CAPTURED_IMAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halalresult);

        // Initialize TextViews and ImageView
        productNameTextView = findViewById(R.id.product_name);
        productStatusTextView = findViewById(R.id.product_status);
        productEcodesTextView = findViewById(R.id.product_ecodes);
        capturedImageView = findViewById(R.id.capturedImageView);

        // Retrieve data from Intent
        Intent intent = getIntent();
        String productName = intent.getStringExtra(EXTRA_PRODUCT_NAME);
        String productStatus = intent.getStringExtra(EXTRA_PRODUCT_STATUS);
        String productEcodes = intent.getStringExtra(EXTRA_PRODUCT_ECODES);

        // Set the data to the TextViews
        productNameTextView.setText(productName != null ? productName : "Unknown Product");
        productStatusTextView.setText(productStatus != null ? productStatus : "Unknown Status");
        productEcodesTextView.setText(productEcodes != null ? productEcodes : "No E-codes");

        // Retrieve and set the captured image
        byte[] byteArray = intent.getByteArrayExtra(EXTRA_CAPTURED_IMAGE);
        if (byteArray != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            capturedImageView.setImageBitmap(bitmap);
        }

        // Bottom Navigation Setup
        BottomNavigationView bottomnavi = findViewById(R.id.bottom_nav);
        bottomnavi.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.scanner_buttons) {
                Intent newIntent = new Intent(halalresult.this, MainActivity.class);
                startActivity(newIntent);
                finish(); // Finish current activity
                return true;
            }
            return halalresult.super.onOptionsItemSelected(item);
        });
    }
}
