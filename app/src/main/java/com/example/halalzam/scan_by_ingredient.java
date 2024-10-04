package com.example.halalzam;

import static android.Manifest.permission.CAMERA;
import static android.content.pm.PackageManager.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;;

public class scan_by_ingredient extends AppCompatActivity {

    ImageView image;
    Button button,result_button;
    TextView itext;
    Bitmap bitmap;

    static final int REQUEST_IMAGE_CAPTURE = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_by_ingredient);

        image = findViewById(R.id.scanners);
        button = findViewById(R.id.scan_food_b);
        itext = findViewById(R.id.result_in_text);
        result_button = findViewById(R.id.result);

        result_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectText();
            }

        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()){
                    captureImage();
                }else {
                    requestPermission();
                }

            }
        });



        // Bottom Navigation Setup
        BottomNavigationView bottom_navigation = findViewById(R.id.bottom_navigation);
        bottom_navigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home_buttons) {
                Intent newintent = new Intent(scan_by_ingredient.this, MainActivity.class);
                startActivity(newintent);
                return true;
            }
            return scan_by_ingredient.super.onOptionsItemSelected(item);
        });




    }

    private boolean checkPermission(){
        int camPermission = ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA);
        return camPermission == PERMISSION_GRANTED;
    }

    private void requestPermission(){
        int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(this,new String[]{CAMERA}, PERMISSION_CODE);
    }

    private void captureImage(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length>0){
            boolean cameraPermission = grantResults[0] == PERMISSION_GRANTED;
            if (cameraPermission){
                Toast.makeText(this, "Permission Granted bes...", Toast.LENGTH_SHORT).show();
                captureImage();
            }else {
                Toast.makeText(this, "Permission Denied bes...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            image.setImageBitmap(bitmap);
        }
    }

    private void detectText(){
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(inputImage).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                StringBuilder result = new StringBuilder();
                for (Text.TextBlock block: text.getTextBlocks()){
                    String blockText = block.getText();
                    Point[] blockCornerPoint = block.getCornerPoints();
                    Rect blockFlame = block.getBoundingBox();
                    for (Text.Line line : block.getLines()){
                        String lineText = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect linRect = line.getBoundingBox();
                        for (Text.Element element: line.getElements()){
                            String elementText = element.getText();
                            result.append(elementText);
                        }

                        itext.setText(blockText);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(scan_by_ingredient.this, "fail to detect text from image" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }






    }


