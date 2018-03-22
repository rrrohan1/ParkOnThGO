package edu.scu.smurali.parkonthego.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import edu.scu.smurali.parkonthego.ParkOnTheGo;
import edu.scu.smurali.parkonthego.R;
import edu.scu.smurali.parkonthego.retrofit.reponses.LoginResponse;
import edu.scu.smurali.parkonthego.retrofit.services.UserServices;
import edu.scu.smurali.parkonthego.util.PreferencesManager;
import mehdi.sakout.fancybuttons.FancyButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements Validator.ValidationListener {

    public final int permissions = 100;
  //  private Button login, register;
    private FancyButton login, register;
    private Button maps;
    private TextView forgotPassword;
    private CheckBox stayLoggedIn;
    private Context mContext;
    private PreferencesManager pManager;

    @NotEmpty
    @Email
    private EditText email;
    @Password(min = 6, message = "Password should be 6 characters in length")
    private EditText pwd;
    @Override


    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ParkOnTheGo.getInstance().setCurrentActivityContext(this);
        this.mContext = this;
        pManager = PreferencesManager.getInstance(mContext);

        //Register validator for this activity
        final Validator validator = new Validator(this);
        validator.setValidationListener(this);

        try {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle("LogIn");
            actionBar.setIcon(R.mipmap.ic_park);
            //  actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            // actionBar.setHomeButtonEnabled(true);
        } catch (NullPointerException ex) {
            Log.d("Login", "onCreate: Null pointer in action bar " + ex.getMessage());
        }
//        login = (Button) findViewById(R.id.logInButton);
//        register = (Button) findViewById(R.id.registerButton);

        login = (FancyButton) findViewById(R.id.logInButton);
        register = (FancyButton) findViewById(R.id.registerButton);

        // maps = (Button)findViewById(R.id.maps);
        forgotPassword = (TextView) findViewById(R.id.forgotPasswordEmail);
        //  maps = (Button)findViewById(R.id.maps);
        stayLoggedIn = (CheckBox) findViewById(R.id.stayLoggedInCheckBox);
        email = (EditText) findViewById(R.id.loignEmailEditText);
        pwd = (EditText) findViewById(R.id.loginPasswordEditText);
        Log.d("UserId bc", "onCreate: " + pManager.getUserId());
        if (pManager.getUserId() > -1) {
            if(pManager.getUserId()==8)
            {
                Log.d("user id from shared p", "onCreate: " + pManager.getUserId());
                Log.d("user name from shared p", "onCreate: " + pManager.getEmail());

                PreferencesManager.getInstance(LoginActivity.this).clear();
            }
            else {
                Log.d("user id from shared p", "onCreate: " + pManager.getUserId());
                Intent intent = new Intent(LoginActivity.this, HomeScreenActivity.class);
                startActivity(intent);
                finish();
            }
        }

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SweetAlertDialog(mContext, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Confirmation")
                        .setContentText("Password reset email has been sent to you registered email")
                        .show();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String emailText = email.getText().toString();
                String passwordText = pwd.getText().toString();
                validator.validate();

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });


//        maps.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//
//
//
//            }
//        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });


        Log.d("**************", "onCreate: " + login);


    }


    @Override
    public void onValidationSucceeded() {
        String emailValue = email.getText().toString();
        String password = pwd.getText().toString();

        //background login task
        login(emailValue, password);
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }


    public void login(String email, String password) {

        if (ParkOnTheGo.getInstance().isConnectedToInterNet()) {
            UserServices userServices = ParkOnTheGo.getInstance().getUserServices();
            ParkOnTheGo.getInstance().showProgressDialog();
            Call<LoginResponse> call = userServices.login(email, password);
            Log.d("Calling", "register: " + call);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call,
                                       Response<LoginResponse> response) {
                    ParkOnTheGo.getInstance().hideProgressDialog();
                    if (response.isSuccessful()) {
                        parseResponse(response.body());
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                    ParkOnTheGo.getInstance().hideProgressDialog();
                    Toast.makeText(getApplicationContext(), "Request failed" + throwable, Toast.LENGTH_SHORT).show();


                    // ParkOnTheGo.getInstance().handleError(throwable);
                }
            });
        } else {
            ParkOnTheGo.getInstance().showAlert(mContext.getString(R.string.no_network));
        }
    }

    private void parseResponse(LoginResponse response) {
       // Toast.makeText(getApplicationContext(), "Login Sucess" + response.getSuccess(), Toast.LENGTH_SHORT).show();
        if (response.getSuccess() == true) {
            PreferencesManager pm = PreferencesManager.getInstance(mContext);
            pm.updateUserId(response.getData().getId());
            pm.updateUserName(response.getData().getDisplayName());
            response.getData().getId();
            Intent intent = new Intent(LoginActivity.this, HomeScreenActivity.class);
            intent.putExtra("userId",response.getData().getId());
            startActivity(intent);
            finish();

        } else {
            new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText("Invalid credentials")
                    .show();
        }
    }


}
