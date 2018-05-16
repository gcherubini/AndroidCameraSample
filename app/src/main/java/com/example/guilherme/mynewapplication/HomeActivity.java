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

    private static final String AUTHORITIES = "com.example.guilherme.mynewapplication.fileprovider";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_CAPTURE_AND_STORE = 2;
    private static final int CAMERA_PERMISSION_CODE = 1;
    private static final int WRITE_STORAGE_PERMISSION_CODE = 2;
    private Uri storedImageFilePath;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        imageView = findViewById(R.id.image_view);
    }


    public void onClickTakePictureButton(View view) {
        takePicture();
    }


    public void onClickTakePictureAndStoreButton(View view) {
        takePictureAndStoreInDevice();
    }


    /**
     * The Android way of delegating actions to other applications is to invoke an Intent.
     */
    private void takePicture() {
        if (requestPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)) return;

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    /**
     * You have to manage run time permission for this on new Android devices,
     * Because whichever permissions you have defined in AndroidManifest will not be automatically granted.
     * So like below method you can check whether you permission is approved or not and request it
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
        if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_STORAGE_PERMISSION_CODE)) return;


        if(!isExternalStorageWritable()) {
            Toast.makeText(this, "External storage unavailable", Toast.LENGTH_LONG).show();
            return;
        }

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
                        AUTHORITIES,
                        photoFile);
                storedImageFilePath = photoURI;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_AND_STORE);
            }
        }
    }

    /**
     * Because the external storage might be unavailable—such as when the user has mounted the storage to a PC
     * or has removed the SD card that provides the external storage—you should always verify that the volume
     * is available before accessing it. You can query the external storage state by calling getExternalStorageState().
     * If the returned state is MEDIA_MOUNTED, then you can read and write your files. If it's MEDIA_MOUNTED_READ_ONLY, you can only read the files.
     * */
    public boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = new File(storageDir, imageFileName + ".jpeg");
        image.createNewFile();

        // Save a file: path for use with ACTION_VIEW intents
        // imageAbsolutePath = image.getAbsolutePath();
        return image;
    }
}
