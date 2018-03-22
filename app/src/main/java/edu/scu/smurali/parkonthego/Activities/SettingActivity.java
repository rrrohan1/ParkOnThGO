package edu.scu.smurali.parkonthego.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import edu.scu.smurali.parkonthego.ParkOnTheGo;
import edu.scu.smurali.parkonthego.R;
import edu.scu.smurali.parkonthego.retrofit.reponses.LoginResponse;
import edu.scu.smurali.parkonthego.retrofit.reponses.ProfileData;
import edu.scu.smurali.parkonthego.retrofit.reponses.ProfileResponse;
import edu.scu.smurali.parkonthego.retrofit.reponses.UpdateProfileResponse;
import edu.scu.smurali.parkonthego.retrofit.services.UserServices;
import edu.scu.smurali.parkonthego.util.PreferencesManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Validator.ValidationListener {

    private Context mContext;
    @NotEmpty
    private AutoCompleteTextView firstName;
    @NotEmpty
    private AutoCompleteTextView lastName;
    @NotEmpty
    @Email
    private AutoCompleteTextView email;

    final Validator validator = new Validator(this);


    TextView changePassword;
    private boolean isDataChanged;

    private TextView navUserName;
    private TextView navEmail;

    PreferencesManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ParkOnTheGo.getInstance().setCurrentActivityContext(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle("Account Setting");
            actionBar.setIcon(R.mipmap.ic_park);
            //  actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            mContext = this;
            // actionBar.setHomeButtonEnabled(true);
        } catch (NullPointerException ex) {
            Log.d("Confirmation:", "onCreate: Null pointer in action bar " + ex.getMessage());
        }

        mContext = this;
        isDataChanged =false;
        pm = PreferencesManager.getInstance(mContext);

        firstName = (AutoCompleteTextView) findViewById(R.id.settingFirstName);
        lastName = (AutoCompleteTextView) findViewById(R.id.settingLastName);
        email = (AutoCompleteTextView) findViewById(R.id.settingEmail);

        try {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle("ParkOnTheGo");
            actionBar.setIcon(R.mipmap.ic_park);
            //  actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            // actionBar.setHomeButtonEnabled(true);
        } catch (NullPointerException ex) {
            Log.d("Settings", "onCreate: Null pointer in action bar " + ex.getMessage());
        }


//        changePassword = (TextView) findViewById(R.id.settingChangePassword);
//
//        changePassword.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        navUserName = (TextView) header.findViewById(R.id.userName);
        navUserName.setText(pm.getUserName());
        navEmail = (TextView) header.findViewById(R.id.email);
        navEmail.setText(pm.getEmail());


        firstName.setText(pm.getFirstName());
        lastName.setText(pm.getLastName());
        email.setText(pm.getEmail());

        //Register validator for this activity

        validator.setValidationListener(this);


        firstName.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                isDataChanged =  true;
            }
        });

        lastName.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                isDataChanged =  true;
            }
        });

        email.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                isDataChanged =  true;
            }
        });
    }

    @Override
    public void onValidationSucceeded() {
        String firstNameText = firstName.getText().toString();
        String lastNameText = lastName.getText().toString();
        String emailText = email.getText().toString();

        //background login task
        updateProfile(firstNameText, lastNameText, emailText);
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof AutoCompleteTextView) {
                ((AutoCompleteTextView) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            Intent intent = new Intent(SettingActivity.this,HomeScreenActivity.class);
            startActivity(intent);
            finish();
        } else if(isDataChanged){

            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Are you sure?")
                    .setContentText("Won't be able to save profile!")
                    .setCancelText("No,cancel plx!")
                    .setConfirmText("Yes")
                    .showCancelButton(true)
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.cancel();
                        }
                    })
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            isDataChanged = false;
                            Intent intent = new Intent(SettingActivity.this,HomeScreenActivity.class);
                            startActivity(intent);
                            finish();
                            //goback();
                        }


                    })
                    .show();
        }
        else {
            Intent intent = new Intent(SettingActivity.this,HomeScreenActivity.class);
            startActivity(intent);
            finish();
        }


    }

    private void goback() {
        onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            validator.validate();
            //finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(SettingActivity.this, HomeScreenActivity.class);
            startActivity(intent);
            finish();

        }

        if (id == R.id.nav_reservation) {


            Intent intent = new Intent(SettingActivity.this, ReservationsActivity.class);
            startActivity(intent);
            finish();


        } else if (id == R.id.nav_settings) {

            Intent intent = new Intent(SettingActivity.this, SettingActivity.class);
            startActivity(intent);
            finish();


        } else if (id == R.id.nav_call) {

            final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;

            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:+1669 220 8549"));

            if (ActivityCompat.checkSelfPermission(SettingActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(SettingActivity.this,
                        Manifest.permission.CALL_PHONE)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(SettingActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            MY_PERMISSIONS_REQUEST_CALL_PHONE);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }


            }
            startActivity(callIntent);



        } else if (id == R.id.nav_help) {

            Intent intent = new Intent(SettingActivity.this, HelpActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_logout) {
            PreferencesManager.getInstance(mContext).clear();
            Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    public void updateProfile(String firstNameText,String lastNameText,String emailText) {

        if (ParkOnTheGo.getInstance().isConnectedToInterNet()) {
            UserServices userServices = ParkOnTheGo.getInstance().getUserServices();
            ParkOnTheGo.getInstance().showProgressDialog();
            Call<UpdateProfileResponse> call = userServices.updateProfile(PreferencesManager.getInstance(mContext).getUserId(),firstNameText,lastNameText,emailText);
            Log.d("Calling", "Get profile: " + call);
            call.enqueue(new Callback<UpdateProfileResponse>() {
                @Override
                public void onResponse(Call<UpdateProfileResponse> call,
                                       Response<UpdateProfileResponse> response) {
                    ParkOnTheGo.getInstance().hideProgressDialog();
                    if (response.isSuccessful()) {
                        parseUpdateProfileResponse(response.body());
                    }
                }

                @Override
                public void onFailure(Call<UpdateProfileResponse> call, Throwable throwable) {
                    Toast.makeText(getApplicationContext(), "Request failed" + throwable, Toast.LENGTH_SHORT).show();

                    ParkOnTheGo.getInstance().hideProgressDialog();
                    ParkOnTheGo.getInstance().handleError(throwable);
                }
            });
        } else {
            ParkOnTheGo.getInstance().showAlert(mContext.getString(R.string.no_network));
        }
    }

    private void parseUpdateProfileResponse(UpdateProfileResponse response) {
        if (response.getSuccess() == true) {
       //     Toast.makeText(getApplicationContext(), "Profile Updated" + response.getSuccess(), Toast.LENGTH_SHORT).show();
            pm.updateFirstName(firstName.getText().toString());
            pm.updateLastName(lastName.getText().toString());
            pm.updateEmail(email.getText().toString());
            isDataChanged = false;
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Profile saved")
                    .setConfirmText("Ok")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            startActivity(new Intent(SettingActivity.this, HomeScreenActivity.class));
                        }
                    })
                    .show();
        } else {
            Toast.makeText(getApplicationContext(), "Update failed" + response.getSuccess(), Toast.LENGTH_SHORT).show();
        }
    }
}
