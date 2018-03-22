package edu.scu.smurali.parkonthego.Activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import edu.scu.smurali.parkonthego.ParkOnTheGo;
import edu.scu.smurali.parkonthego.R;

public class ForgotPasswordActivity extends AppCompatActivity implements Validator.ValidationListener {

    @NotEmpty
    @Email
    EditText email;
    Button submit;
    Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        ParkOnTheGo.getInstance().setCurrentActivityContext(this);
        mContext = this;
        setContentView(R.layout.activity_forgot_password);
        //Register validator for this activity
        final Validator validator = new Validator(this);
        validator.setValidationListener(this);
        try {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle("Forgot Password");
            actionBar.setIcon(R.mipmap.ic_park);
            //  actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            // actionBar.setHomeButtonEnabled(true);
        }
        catch(NullPointerException ex){
            Log.d("Forgot Password", "onCreate: Null pointer in action bar "+ex.getMessage());
        }

        submit = (Button) findViewById(R.id.forgotPasswordSubmitButton);
        email =  (EditText) findViewById(R.id.forgotPasswordEmail);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validate();
            }
        });

    }

    @Override
    public void onValidationSucceeded() {
        String emailValue = email.getText().toString();

        new SweetAlertDialog(mContext, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Confirmation")
                .setContentText("Password reset email has been sent to you registered email")
                .setConfirmText("Ok")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        ForgotPasswordActivity.this.finish();

                    }
                })
                .show();
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



}
