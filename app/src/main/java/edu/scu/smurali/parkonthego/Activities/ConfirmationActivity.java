package edu.scu.smurali.parkonthego.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import edu.scu.smurali.parkonthego.ParkOnTheGo;
import edu.scu.smurali.parkonthego.R;
import edu.scu.smurali.parkonthego.retrofit.reponses.ReservationCfnResponse;
import edu.scu.smurali.parkonthego.retrofit.reponses.SearchData;
import edu.scu.smurali.parkonthego.retrofit.services.ReservationServices;
import edu.scu.smurali.parkonthego.util.PreferencesManager;
import mehdi.sakout.fancybuttons.FancyButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmationActivity extends AppCompatActivity {

    private TextView confirmationLocationTextView;
    private String selectedLocation;
    private String startDateTime, endDateTime;
  //  private Button cfnReserveButton;
    private FancyButton cfnReserveButton;
    private Context mContext;
    private SearchData locationObject;
    private TextView confirmationStartDateTextView;
    private TextView confirmationEndDateTextView, confirmationStartTimeTextView, confirmationEndTimeTextView, confirmationPricePerHrTextView,confirmationTotalCost, confirmationTotalTime;
    private Double hours;
    private Double totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        ParkOnTheGo.getInstance().setCurrentActivityContext(this);

        try {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle("Confirmation Page");
            actionBar.setIcon(R.mipmap.ic_park);
            //  actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            mContext = this;
            // actionBar.setHomeButtonEnabled(true);
        } catch (NullPointerException ex) {
            Log.d("Confirmation:", "onCreate: Null pointer in action bar " + ex.getMessage());
        }

       // cfnReserveButton = (Button) findViewById(R.id.confirmationReserveButton);

        cfnReserveButton =(FancyButton) findViewById(R.id.confirmationReserveButton);


//        ////////////////////////////////////////////////testing/////////////////////////////////////////////////////////////////////////////
        Intent intent = getIntent();
        confirmationLocationTextView = (TextView) findViewById(R.id.confirmationLocationTextView);
        confirmationStartDateTextView = (TextView) findViewById(R.id.confirmationStartDateTextView);
        confirmationEndDateTextView = (TextView) findViewById(R.id.confirmationEndDateTextView);
        confirmationEndTimeTextView = (TextView) findViewById(R.id.confirmationEndTimeTextView);
        confirmationStartTimeTextView = (TextView) findViewById(R.id.confirmationStartTimeTextView);
        confirmationPricePerHrTextView = (TextView) findViewById(R.id.confirmationPricePerHrTextView);
        confirmationTotalCost =  (TextView) findViewById(R.id.confirmationTotalPriceTextView);
        confirmationTotalTime =  (TextView) findViewById(R.id.confirmationTotalTimeTextView);

        LatLng location = intent.getParcelableExtra("location");
        String title = intent.getExtras().getString("title");
        title = intent.getExtras().getString("title");
        startDateTime = intent.getExtras().getString("startDateTime");
        endDateTime = intent.getExtras().getString("startEndTime");
        selectedLocation = intent.getExtras().getString("selectedLocation");
        Log.d("Json", "onCreate: " + selectedLocation);
        locationObject = new Gson().fromJson(selectedLocation, SearchData.class);
        Log.d("Confirmation Id", "onCreate: " + locationObject.getId());


//       // LatLng location = (LatLng) intent.getExtras().get("ltdLng");
//        String title = intent.getExtras().getString("title");
        confirmationLocationTextView.setText(locationObject.getDescription());
        String[] t = startDateTime.split(" ");
        List<String> sDateTimeList = Arrays.asList(t);
        t = endDateTime.split(" ");
        List<String> eDateTimeList = Arrays.asList(t);
        Log.d("Select location details", "onCreate: " + intent.getStringExtra("startDateTime"));
        confirmationStartDateTextView.setText(sDateTimeList.get(0));
        confirmationStartTimeTextView.setText(sDateTimeList.get(1));
        confirmationEndDateTextView.setText(eDateTimeList.get(0));
        confirmationEndTimeTextView.setText(eDateTimeList.get(1));
        confirmationPricePerHrTextView.setText(new Double(locationObject.getPrice()).toString());
        hours = ParkOnTheGo.getInstance().getDateTimeDiff(startDateTime, endDateTime);
        confirmationTotalTime.setText(hours.toString()+" hours");
        totalPrice = new Double(hours * locationObject.getPrice());
        confirmationTotalCost.setText("$"+totalPrice.toString());

        cfnReserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveReservation(locationObject.getId(), PreferencesManager.getInstance(mContext).getUserId(), startDateTime, endDateTime, totalPrice);
            }
        });

    }


    public void saveReservation(Integer parkingId,
                                Integer userId,
                                String sDateTime,
                                String eDatetTime, Double totalPrice) {

        if (ParkOnTheGo.getInstance().isConnectedToInterNet()) {
            ReservationServices reservationServices = ParkOnTheGo.getInstance().getReservationServices();
            ParkOnTheGo.getInstance().showProgressDialog();

            Call<ReservationCfnResponse> call = reservationServices.createReservation(parkingId, userId, sDateTime, eDatetTime, totalPrice);
            Log.d("Calling", "register: " + call + " " + parkingId + " " + userId + " " + sDateTime + " " + eDatetTime + " " + totalPrice);
            call.enqueue(new Callback<ReservationCfnResponse>() {
                @Override
                public void onResponse(Call<ReservationCfnResponse> call,
                                       Response<ReservationCfnResponse> response) {
                    ParkOnTheGo.getInstance().hideProgressDialog();
                    if (response.isSuccessful()) {
                        parseResponse(response.body());
                    }
                }

                @Override
                public void onFailure(Call<ReservationCfnResponse> call, Throwable throwable) {
                    Toast.makeText(getApplicationContext(), "Request failed " + throwable, Toast.LENGTH_SHORT).show();
                    Log.d("Failed", "onFailure: " + throwable);
                     ParkOnTheGo.getInstance().hideProgressDialog();
                     ParkOnTheGo.getInstance().handleError(throwable);
                }
            });
        } else {
            ParkOnTheGo.getInstance().showAlert(mContext.getString(R.string.no_network));
        }
    }

    private void parseResponse(ReservationCfnResponse response) {
        //Toast.makeText(getApplicationContext(), "Login Sucess" + response.getSuccess(), Toast.LENGTH_SHORT).show();
        if (response.getSuccess() == true) {
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Reservation saved")
                    .setConfirmText("Ok")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            Intent intent = new Intent(getApplicationContext(), HomeScreenActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            ConfirmationActivity.this.finish();

                        }
                    })
                    .show();


        } else {
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Opps !")
                    .setContentText("Reservation is conflicted with other reservation")
                    .show();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.confirmation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_cancel) {
            new SweetAlertDialog(ConfirmationActivity.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Cancel Reservation?")
                    .setConfirmText("Yes")
                    .setCancelText("No")
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.hide();
                        }
                    })
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            Intent intent = new Intent(ConfirmationActivity.this,HomeScreenActivity.class);
                            startActivity(intent);
                        }
                    })
                    .show();


        }

        return super.onOptionsItemSelected(item);
    }


}





