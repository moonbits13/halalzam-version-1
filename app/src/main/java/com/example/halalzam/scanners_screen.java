package com.example.halalzam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import android.content.res.AssetFileDescriptor;
import java.nio.ByteOrder;
import org.tensorflow.lite.Interpreter;

import java.util.HashMap;

public class scanners_screen extends AppCompatActivity {

    ImageView image;
    Button button;

    // HashMap to store product info
    HashMap<String, ProductInfo> productMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanners_screen);

        image = findViewById(R.id.scanner);
        button = findViewById(R.id.scan_food_button);

        // Initialize the product map
        productMap = new HashMap<>();
        initializeProductMap();

        // Check for camera permission
        if (ContextCompat.checkSelfPermission(scanners_screen.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(scanners_screen.this, new String[]{Manifest.permission.CAMERA}, 101);
        }

        // Set button click listener to trigger image capture
        button.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 101);
        });

        // Bottom Navigation Setup
        BottomNavigationView bottomnavi = findViewById(R.id.bottom_navi);
        bottomnavi.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home_buttons) {
                Intent newintent = new Intent(scanners_screen.this, MainActivity.class);
                startActivity(newintent);
                return true;
            }
            return scanners_screen.super.onOptionsItemSelected(item);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            image.setImageBitmap(bitmap);

            // Recognize the product
            String recognizedProduct = recognizeImage(bitmap);

            // Display product info or show unrecognized message
            if (recognizedProduct != null && productMap.containsKey(recognizedProduct)) {
                displayProductInfo(recognizedProduct, bitmap);
            } else {
                // If product is not recognized, show toast and no further action
                Toast.makeText(this, "Product not recognized", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeProductMap() {
        productMap.put("Frutos Sampaloc", new ProductInfo("Non Halal", "E123"));
        productMap.put("Mentos", new ProductInfo("Halal", "None"));
        productMap.put("Frutos Strawberry", new ProductInfo("Non Halal", "E120"));
        productMap.put("Potchi Strawberry Cream", new ProductInfo("Non Halal", "E120"));
        productMap.put("Maxx", new ProductInfo("Halal", "None"));
        productMap.put("White Rabbit", new ProductInfo("Non Halal", "E120"));
        productMap.put("Icool", new ProductInfo("Halal", "None"));
        productMap.put("Choco Joy", new ProductInfo("Non Halal", "E120"));
        productMap.put("Frutos Tropical", new ProductInfo("Non Halal", "E120"));
        productMap.put("Vfresh", new ProductInfo("Halal", "None"));
        productMap.put("Payless Pancit Canton Xtra Big Kalamansi", new ProductInfo("Halal", "None"));
        productMap.put("Payless Pancit Canton Xtra Big Xtra Hot", new ProductInfo("Halal", "None"));
    }

    private void displayProductInfo(String recognizedProduct, Bitmap bitmap) {
        ProductInfo info = productMap.get(recognizedProduct);
        if (info != null) {
            // Prepare data to pass to halalresult activity
            Intent intent = new Intent(scanners_screen.this, halalresult.class);
            intent.putExtra("PRODUCT_NAME", recognizedProduct);
            intent.putExtra("PRODUCT_STATUS", info.status);
            intent.putExtra("PRODUCT_ECODES", info.ecodes);

            // Convert Bitmap to ByteArray and pass it
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            intent.putExtra("CAPTURED_IMAGE", byteArray);

            startActivity(intent);
        }
    }

    private String recognizeImage(Bitmap bitmap) {
        try {
            int width = 224;
            int height = 224;
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

            ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * width * height * 3);
            inputBuffer.order(ByteOrder.nativeOrder());
            int[] intValues = new int[width * height];

            resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());
            for (int pixelValue : intValues) {
                inputBuffer.putFloat(((pixelValue >> 16) & 0xFF) / 255.0f);
                inputBuffer.putFloat(((pixelValue >> 8) & 0xFF) / 255.0f);
                inputBuffer.putFloat((pixelValue & 0xFF) / 255.0f);
            }

            Interpreter tflite = new Interpreter(loadModelFile());

            // Update the output size to match the number of product classes (12 in this case)
            float[][] output = new float[1][12];
            tflite.run(inputBuffer, output);

            int recognizedIndex = getMaxIndex(output[0]);
            float confidence = output[0][recognizedIndex];

            // Confidence threshold to determine if the product is recognized
            float confidenceThreshold = 0.75f;
            if (confidence > confidenceThreshold) {
                return getLabelFromIndex(recognizedIndex);
            } else {
                // If confidence is too low, return null indicating unrecognized product
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;  // If recognition fails, return null
        }
    }


    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private int getMaxIndex(float[] output) {
        int maxIndex = 0;
        for (int i = 1; i < output.length; i++) {
            if (output[i] > output[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private String getLabelFromIndex(int index) {
        String[] labels = {"Frutos Sampalok", "Mentos", "Frutos Strawberry", "Potchi Strawberry Cream", "Maxx", "White Rabbit", "Icool", "Choco Joy", "Frutos Tropical", "Vfresh", "Payless Pancit Canton Xtra Big Kalamansi", "Payless Pancit Canton Xtra Big Xtra Hot"};
        if (index >= 0 && index < labels.length) {
            return labels[index];
        } else {
            return null; // If index is out of bounds, return null
        }
    }

    private static class ProductInfo {
        String status;
        String ecodes;

        ProductInfo(String status, String ecodes) {
            this.status = status;
            this.ecodes = ecodes;
        }
    }
}
