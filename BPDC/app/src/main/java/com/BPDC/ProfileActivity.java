package com.BPDC;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.BPDC.noticesUpload.ImagesActivity;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.BPDC.fragments.ChangePasswordDialog;
import com.BPDC.model.Response;
import com.BPDC.model.User;
import com.BPDC.network.NetworkUtil;
import com.BPDC.utils.Constants;
import com.BPDC.noticesUpload.UploadActivity;

import java.io.IOException;

import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ProfileActivity extends AppCompatActivity implements ChangePasswordDialog.Listener {

    public static final String TAG = ProfileActivity.class.getSimpleName();

    private TextView mTvName;
    private TextView mTvEmail;
    private TextView mTvDate;
    private Button mBtChangePassword;
    private Button mBtLogout;
    private Button mBtPlace;
    private Button mBtUploads;
    private Button mBtShowUploads;

    private ProgressBar mProgressbar;

    private SharedPreferences mSharedPreferences;
    private String mToken;
    private String mEmail;

    private CompositeSubscription mSubscriptions;

    private int PLACE_PICKER_REQUEST=1;
    private TextView mTvPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mSubscriptions = new CompositeSubscription();
        initViews();
        initSharedPreferences();
        loadProfile();
    }

    private void initViews() {

        mTvName = (TextView) findViewById(R.id.tv_name);
        mTvEmail = (TextView) findViewById(R.id.tv_email);
        mTvDate = (TextView) findViewById(R.id.tv_date);
        mBtChangePassword = (Button) findViewById(R.id.btn_change_password);
        mBtLogout = (Button) findViewById(R.id.btn_logout);
        mProgressbar = (ProgressBar) findViewById(R.id.progress);
        mTvPlace = (TextView) findViewById(R.id.TvPlace);
        mBtPlace = (Button) findViewById(R.id.btn_placePicker);
        mBtUploads = (Button) findViewById(R.id.btn_Uploads);
        mBtShowUploads = (Button) findViewById(R.id.btn_Show_Uploads);

        mBtChangePassword.setOnClickListener(view -> showDialog());
        mBtLogout.setOnClickListener(view -> logout());
        mBtPlace.setOnClickListener(view -> goPlacePicker(view));
        mBtUploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProfileActivity.this, UploadActivity.class);
                startActivity(i);

            }
        });
        mBtShowUploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProfileActivity.this, ImagesActivity.class);
                startActivity(i);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ActivityCompat.checkSelfPermission
                    (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    &&
                    ActivityCompat.checkSelfPermission
                            (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, 1); // 1 is requestCode
                return;

            }
        }
    }

    private void initSharedPreferences() {

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = mSharedPreferences.getString(Constants.TOKEN,"");
        mEmail = mSharedPreferences.getString(Constants.EMAIL,"");
    }

    private void logout() {

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.EMAIL,"");
        editor.putString(Constants.TOKEN,"");
        editor.apply();
        finish();
    }

    private void showDialog(){

        ChangePasswordDialog fragment = new ChangePasswordDialog();

        Bundle bundle = new Bundle();
        bundle.putString(Constants.EMAIL, mEmail);
        bundle.putString(Constants.TOKEN,mToken);
        fragment.setArguments(bundle);

        fragment.show(getFragmentManager(), ChangePasswordDialog.TAG);
    }

    private void loadProfile() {

        mSubscriptions.add(NetworkUtil.getRetrofit(mToken).getProfile(mEmail)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(User user) {

        mProgressbar.setVisibility(View.GONE);
        mTvName.setText(user.getName());
        mTvEmail.setText(user.getEmail());
        mTvDate.setText(user.getCreated_at());
    }

    private void handleError(Throwable error) {

        mProgressbar.setVisibility(View.GONE);

        if (error instanceof HttpException) {

            Gson gson = new GsonBuilder().create();

            try {

                String errorBody = ((HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);
                showSnackBarMessage(response.getMessage());

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            showSnackBarMessage("Network Error !");
        }
    }

    private void showSnackBarMessage(String message) {

        Snackbar.make(findViewById(R.id.activity_profile),message,Snackbar.LENGTH_SHORT).show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

    @Override
    public void onPasswordChanged() {

        showSnackBarMessage("Password Changed Successfully !");
    }


    public void goPlacePicker(View v){

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try{

            startActivityForResult(builder.build(ProfileActivity.this), PLACE_PICKER_REQUEST);

        }catch (GooglePlayServicesRepairableException e){
            e.printStackTrace();
        }catch (GooglePlayServicesNotAvailableException e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){

        if(requestCode == PLACE_PICKER_REQUEST) {
            if(resultCode == RESULT_OK){

                Place place = PlacePicker.getPlace(data, ProfileActivity.this);
                mTvPlace.setText(place.getAddress());
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();


            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case 1:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(ProfileActivity.this,"PERMISSION_DENIED",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ProfileActivity.this,"PERMISSION_GRANTED",Toast.LENGTH_SHORT).show();

                }
                break;
        }
    }

}

