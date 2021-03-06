package com.byteshaft.foodtruck.truckowner;

import android.Manifest;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.byteshaft.foodtruck.R;
import com.byteshaft.foodtruck.utils.Helpers;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by s9iper1 on 1/16/17.
 */

public class AddNewTruck extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

    private ImageButton truckImage;
    private static final int REQUEST_CAMERA = 1212;
    private static final int SELECT_FILE = 1245;
    private File destination;
    public String imageUrl= "";
    private Bitmap profilePic;
    private Uri selectedImageUri;
    private AppCompatButton proceedButton;
    public EditText truckName;
    public EditText truckAddress;
    public EditText phoneNumber;
    public EditText products;
    public EditText locationCoordinates;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    public double lat;
    public double lng;
    private static AddNewTruck sInstance;
    private static final int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 0;
    public boolean updateMode = false;
    public int id;
    private AppCompatButton updateLocation;
    private TextInputLayout textInputLayout;
    private int locationCounter = 0;

    public static AddNewTruck getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_truck);
        sInstance = this;
        overridePendingTransition(R.anim.anim_left_in, R.anim.anim_left_out);
        truckImage = (ImageButton) findViewById(R.id.truck_image);
        truckImage.setOnClickListener(this);
        proceedButton = (AppCompatButton) findViewById(R.id.step_two);
        proceedButton.setOnClickListener(this);
        truckName = (EditText) findViewById(R.id.truck_name);
        phoneNumber = (EditText) findViewById(R.id.phone_number);
        truckAddress = (EditText) findViewById(R.id.address);
        phoneNumber = (EditText) findViewById(R.id.phone_number);
        products = (EditText) findViewById(R.id.products);
        locationCoordinates = (EditText) findViewById(R.id.location);
        updateLocation = (AppCompatButton) findViewById(R.id.update_location);
        textInputLayout = (TextInputLayout) findViewById(R.id.input_layout);
        updateLocation.setOnClickListener(this);
        locationCoordinates.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mGoogleApiClient != null) {
                    mGoogleApiClient.disconnect();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            updateMode = true;
            id = bundle.getInt("id");
            Log.i("TAG", "" + id);
            truckName.setText(bundle.getString("name"));
            truckAddress.setText(bundle.getString("address"));
            locationCoordinates.setText(bundle.getString("location"));
            phoneNumber.setText(bundle.getString("phone"));
            products.setText(bundle.getString("products"));
            imageUrl = bundle.getString("image");
            updateLocation.setVisibility(View.VISIBLE);
            Picasso.with(this)
                    .load(imageUrl)
                    .resize(150, 150)
                    .centerCrop()
                    .into(truckImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {


                        }
                    });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 3.0f;
            textInputLayout.setLayoutParams(params);
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT);
            buttonParams.weight = 1.0f;
            updateLocation.setLayoutParams(buttonParams);
        }
        if (!updateMode) {
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    public void stopLocationService() {
        if ( mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    public void startLocationUpdates() {
        long INTERVAL = 0;
        long FASTEST_INTERVAL = 0;
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("TAG", "Location changed called");
        lat = location.getLatitude();
        lng = location.getLongitude();
        locationCounter++;
        if (locationCounter == 2) {
            locationCoordinates.setText(lat + ", " + lng);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopLocationService();
        finish();
        overridePendingTransition(R.anim.anim_right_in, R.anim.anim_right_out);

    }

    // Dialog with option to capture image or choose from gallery
    private void selectImage() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Permission Request");
            alertDialogBuilder.setMessage("Storage permission is required to save the captured image or to select from gallery. please continue to grant it.")
                    .setCancelable(false).setPositiveButton("continue", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(AddNewTruck.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
                }
            });
            alertDialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            showPictureSelectionDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showPictureSelectionDialog();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void showPictureSelectionDialog() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Remove photo", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(AddNewTruck.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                } else if (items[item].equals("Remove photo")) {
                    truckImage.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_camera));
                }
            }
        });
        builder.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.truck_image:
                selectImage();
                break;
            case R.id.step_two:
                if (truckName.getText().toString().trim().isEmpty() ||
                        truckAddress.getText().toString().trim().isEmpty() ||
                        phoneNumber.getText().toString().trim().isEmpty() ||
                        products.getText().toString().trim().isEmpty() ||
                        locationCoordinates.getText().toString().isEmpty()) {
                    Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (imageUrl.trim().isEmpty()) {
                    Toast.makeText(this, "please add truck image!", Toast.LENGTH_SHORT).show();
                    return;
                }
                stopLocationService();
                Intent intent = new Intent(getApplicationContext(), AddNewTruckStepTwo.class);
                if (updateMode) {
                    intent.putExtra("facebook", getIntent().getStringExtra("facebook"));
                    intent.putExtra("website", getIntent().getStringExtra("website"));
                    intent.putExtra("instagram", getIntent().getStringExtra("instagram"));
                    intent.putExtra("twitter", getIntent().getStringExtra("twitter"));
                }
                startActivity(intent);
                break;
            case R.id.update_location:
                Toast.makeText(this, "acquiring location", Toast.LENGTH_SHORT).show();
                buildGoogleApiClient();
                mGoogleApiClient.connect();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");
                imageUrl = destination.getAbsolutePath();
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                profilePic = Helpers.getBitMapOfProfilePic(destination.getAbsolutePath());
                truckImage.setImageBitmap(thumbnail);
            } else if (requestCode == SELECT_FILE) {
                selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                String selectedImagePath = cursor.getString(column_index);
                profilePic = Helpers.getBitMapOfProfilePic(selectedImagePath);
                truckImage.setImageBitmap(profilePic);
                imageUrl = String.valueOf(selectedImagePath);
            }
        }
    }
}
