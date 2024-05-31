package com.example.userdetails;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {
    ImageView btnCapture, btnBack, btnGallery, btnRecord;
    PreviewView previewView;
    ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    DBHelper dbHelper;
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        dbHelper = new DBHelper(CameraActivity.this); // Created instance here
        id = getIntent().getIntExtra("id", 0);

        if (!isPermissionGranted()) {
            requestPermission();
        }

        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                bindPreview();

            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(this));

        btnBack.setOnClickListener(v -> finish());
        btnGallery.setOnClickListener(v -> {
            Toast.makeText(CameraActivity.this, "Gallery", Toast.LENGTH_SHORT).show();
        });
        btnRecord.setOnClickListener(v -> {
            Toast.makeText(CameraActivity.this, "Record", Toast.LENGTH_SHORT).show();
        });
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageCapture.takePicture(ContextCompat.getMainExecutor(CameraActivity.this), new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        super.onCaptureSuccess(image);

                        // Convert ImageProxy to Bitmap
                        Bitmap bitmap = imageProxyToBitmap(image);
                        Toast.makeText(CameraActivity.this, "Success", Toast.LENGTH_SHORT).show();

                        // Save the image to internal storage
                        String imagePath = saveToInternalStorage(bitmap, "user" + id);
                        Toast.makeText(CameraActivity.this, "Image saved to internal storage", Toast.LENGTH_SHORT).show();
                        Toast.makeText(CameraActivity.this, imagePath, Toast.LENGTH_SHORT).show();

                        // Rotate image if needed
                        Bitmap rotatedBitmap = rotateBitmapIfNeeded(bitmap, imagePath);

                        // Save the rotated image to internal storage
                        String rotatedImagePath = saveToInternalStorage(rotatedBitmap, "user" + id);
                        Toast.makeText(CameraActivity.this, "Rotated image saved to internal storage", Toast.LENGTH_SHORT).show();
                        Toast.makeText(CameraActivity.this, rotatedImagePath, Toast.LENGTH_SHORT).show();

                        // Save the rotated image path to database
                        dbHelper.updatePhoto(id, rotatedImagePath);
                        Toast.makeText(CameraActivity.this, "Image saved to database", Toast.LENGTH_SHORT).show();

                        image.close();
                        finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        super.onError(exception);
                        Toast.makeText(CameraActivity.this, "Failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    private boolean isPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bindPreview();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                requestPermission();
            }
        }
    }

    private void bindPreview() {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        imageCapture = new ImageCapture.Builder().setTargetResolution(new Size(256, 256)).build();
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy planeProxy = image.getPlanes()[0];
        ByteBuffer buffer = planeProxy.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        try {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            Toast.makeText(CameraActivity.this, "Failed to decode captured image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private String saveToInternalStorage(Bitmap bitmap, String name) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File path = new File(directory, name + ".jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path.getAbsolutePath();
    }

    public int getBitmapOrientationFromExif(String imagePath) {
        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                default:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }

    public Bitmap rotateBitmapIfNeeded(Bitmap bitmap, String imagePath) {
        int rotation = getBitmapOrientationFromExif(imagePath);
        Toast.makeText(this, "Rotation: " + rotation, Toast.LENGTH_SHORT).show();
        return rotateBitmap(bitmap, rotation);
    }

    private void initViews() {
        btnBack = findViewById(R.id.imageView_back);
        btnGallery = findViewById(R.id.imageView_gallery);
        btnCapture = findViewById(R.id.imageView_capture);
        btnRecord = findViewById(R.id.imageView_record);
        previewView = findViewById(R.id.previewView);
    }
}
