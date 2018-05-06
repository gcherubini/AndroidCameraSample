package com.example.guilherme.mynewapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_CAPTURE_AND_STORE = 2;
    private static final int CAMERA_PERMISSION_CODE = 1;
    private static final int WRITE_STORAGE_PERMISSION_CODE = 2;
    private String storedImageFilePath;
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        imageView = findViewById(R.id.image_view);

        Picasso.get().load("http://www.rmmfi.org/wp-content/uploads/2017/05/image-coming-soon.png")
                .fit()
                .into(imageView);
    }


    public void onClickTakePictureButton(View view) {
        takePicture();
    }


    public void onClickTakePictureAndStoreButton(View view) {
        takePictureAndStoreInDevice();
    }


    /**
     * The Android way of delegating actions to other applications is to invoke an Intent
     * that describes what you want done. This process involves three pieces: The Intent itself,
     * a call to start the external Activity, and some code to handle the image data when focus returns to your activity.
     */
    private void takePicture() {
        if (requestPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)) return;

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    /**
     * You have to manage run time permission for this, Because whichever permissions you have
     * defined in AndroidManifest will not be automatically granted. So like below method you can
     * check whether you permission is approved or not
     */
    private Boolean requestPermission(String permission, int permissionCode) {
        boolean wasRequested = false;

        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            wasRequested = true;
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{permission},
                    permissionCode);

        }

        return wasRequested;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageView.setImageBitmap(imageBitmap);
                break;

            case REQUEST_IMAGE_CAPTURE_AND_STORE:
                Toast.makeText(this, "Picture taken is stored.", Toast.LENGTH_LONG).show();
                Picasso.get().load(storedImageFilePath).into(imageView);
                break;
        }
    }

    private void takePictureAndStoreInDevice() {
        if (requestPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)) return;

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;

            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Some error happened saving photo. " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.guilherme.mynewapplication.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_AND_STORE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );


        File image = new File(storageDir, imageFileName + ".jpeg");
        image.createNewFile();


        // Save a file: path for use with ACTION_VIEW intents
        storedImageFilePath = image.getAbsolutePath();
        return image;
    }
}
