package edu.scu.smurali.parkonthego.Activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import edu.scu.smurali.parkonthego.ParkOnTheGo;
import edu.scu.smurali.parkonthego.R;
import edu.scu.smurali.parkonthego.retrofit.reponses.LocationData;
import edu.scu.smurali.parkonthego.retrofit.reponses.LocationResponse;
import edu.scu.smurali.parkonthego.retrofit.reponses.ReservationCfnResponse;
import edu.scu.smurali.parkonthego.retrofit.reponses.ReservationData;
import edu.scu.smurali.parkonthego.retrofit.reponses.ReservationUpdateResponse;
import edu.scu.smurali.parkonthego.retrofit.reponses.SearchData;
import edu.scu.smurali.parkonthego.retrofit.services.LocationServices;
import edu.scu.smurali.parkonthego.retrofit.services.ReservationServices;
import mehdi.sakout.fancybuttons.FancyButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditReservationActivity extends AppCompatActivity {

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "Nfc Functionality";
    private static TextView startDate;
    private static TextView startTime;
    private static TextView endDate;
    private static TextView endTime;
    LocationData recognisedLocation;
    //    private TextView selectLocationStartDate;
//    private TextView selectLocationEndDate;
//    private TextView selectLocationStartTime;
//    private TextView selectLocationEndTime;
//
    private MapFragment mSupportMapFragment;
    private NfcAdapter mNfcAdapter;
    private TextView selectLocation, price;
//    private Button selectLocationReserveButton;
      private FancyButton selectLocationReserveButton;
    private Context mContext;
    private String sDateTime = "", eDateTime = "";
    private String selectedLocation;


    private DatePickerDialogFragment uDatePickerDialogFragment;
    private TimePickerDialogFragment uTimePickerDialogFragment;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;



    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reservation);
        ParkOnTheGo.getInstance().setCurrentActivityContext(this);
        mContext = this;
        uDatePickerDialogFragment = new DatePickerDialogFragment();
        uTimePickerDialogFragment = new TimePickerDialogFragment();
        uTimePickerDialogFragment.setTempCntext(mContext);

        try {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle("Edit Reservation");
            actionBar.setIcon(R.mipmap.ic_park);
            //  actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);

            // actionBar.setHomeButtonEnabled(true);
        } catch (NullPointerException ex) {
            Log.d("Confirmation:", "onCreate: Null pointer in action bar " + ex.getMessage());
        }


        Intent intent = getIntent();
        final LatLng location = (LatLng) intent.getExtras().get("ltdLng");
        final double priceOfParking = intent.getExtras().getDouble("price");
        final LatLng searchedLocation = (LatLng) intent.getExtras().get("searchedLocation");
        final String searchedLocationAddress = intent.getStringExtra("searchedLocationAddress");
        final ReservationData reservationData = (ReservationData)intent.getSerializableExtra("reservationData");
        selectedLocation = intent.getStringExtra("selectedLocationObject");
        Log.d("SelectionLocation json", "onCreate: " + selectedLocation);
        selectLocation = (TextView) findViewById(R.id.selectLocation);
        price = (TextView)findViewById(R.id.pricePerHour);

        startDate = (TextView) findViewById(R.id.selectLocationStartDate);
        startTime = (TextView) findViewById(R.id.selectLocationStartTime);
        endDate = (TextView) findViewById(R.id.selectLocationEndDate);
        endTime = (TextView) findViewById(R.id.selectLocationEndTime);

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uDatePickerDialogFragment.setFlag(DatePickerDialogFragment.FLAG_START_DATE);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                uDatePickerDialogFragment.show(ft, "datePicker");


            }
        });

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uTimePickerDialogFragment.setFlag(TimePickerDialogFragment.FLAG_START_TIME);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                uTimePickerDialogFragment.show(ft, "timePicker");

            }
        });
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uDatePickerDialogFragment.setFlag(DatePickerDialogFragment.FLAG_END_DATE);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                uDatePickerDialogFragment.show(ft, "datePicker");
            }
        });
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uTimePickerDialogFragment.setFlag(TimePickerDialogFragment.FLAG_END_TIME);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                uTimePickerDialogFragment.show(ft, "timePicker");

            }
        });




        ArrayList<SearchData> locationList = (ArrayList<SearchData>) intent.getSerializableExtra("listOfLocations");


        ////////////////////////////////////////////// to set price and other variables in positions//////////////////////////






        String intentSource= (String) intent.getExtras().get("activityName");
        //       // LatLng location = (LatLng) intent.getExtras().get("ltdLng");
//        String title = intent.getExtras().getString("title");
//        confirmationLocation.setText(title);


        //  FragmentManager fragmentManager = getChildFragmentManager();

        /////////////////// if intent is from locations_on_maps activity////////////////////////////////////////////////////////
        if(intentSource!=null)
            if(intentSource.equalsIgnoreCase("LocationsOnMap")) {
                //Date & time picker
                sDateTime = intent.getStringExtra("startDateTime");
                eDateTime = intent.getStringExtra("endDateTime");
                String[] t = sDateTime.split(" ");
                List<String> sDateTimeList = Arrays.asList(t);
                t = eDateTime.split(" ");
                List<String> eDateTimeList = Arrays.asList(t);
                Log.d("Select location details", "onCreate: " + intent.getStringExtra("startDateTime"));
                startDate.setText(sDateTimeList.get(0));
                startTime.setText(sDateTimeList.get(1));
                endDate.setText(eDateTimeList.get(0));
                endTime.setText(eDateTimeList.get(1));
                SearchData locationSelected = new SearchData();

//                for(int i=0;i<locationList.size();i++)
//                {
//                    if(locationList.get(i).getLatitude()==location.latitude&&locationList.get(i).getLongitude()==location.longitude)
//                    {
//                        locationSelected= locationList.get(i);
//                    }
//                }

//                String priceString = new Double(locationSelected.getPrice()).toString();
                selectLocation.setText(searchedLocationAddress);
                price.setText(reservationData.getPrice().toString());
                final String selectedLocationDescription = locationSelected.getDescription();



                mSupportMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFrameLayout);
                if (mSupportMapFragment == null) {
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    mSupportMapFragment = MapFragment.newInstance();
                    fragmentTransaction.replace(R.id.mapFrameLayout, mSupportMapFragment).commit();
                }

                if (mSupportMapFragment != null) {
                    mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            if (googleMap != null) {

                                googleMap.getUiSettings().setAllGesturesEnabled(true);
                                // googleMap.setMapType();

                                // -> marker_latlng // MAKE THIS WHATEVER YOU WANT

                                MarkerOptions custom = new MarkerOptions().position(searchedLocation).title("" + searchedLocationAddress)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                                googleMap.addMarker(custom);

                                googleMap.addMarker(new MarkerOptions().position(location)
                                        .title(""+selectedLocationDescription ));


                                Polyline line = googleMap.addPolyline(new PolylineOptions()
                                        .add(location, searchedLocation)
                                        .width(5)
                                        .color(Color.RED));

                                CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom(15.0f).build();
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                                googleMap.moveCamera(cameraUpdate);
                                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                    @Override
                                    public void onMapClick(LatLng latLng) {
                                        ////////////////////////////////////////////////////////////////
                                    }
                                });

                            }

                        }
                    });


                }
                client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
            }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


      //  selectLocationReserveButton = (Button)findViewById(R.id.selectLocationReserveButton);

        selectLocationReserveButton = (FancyButton) findViewById(R.id.selectLocationReserveButton);

        selectLocationReserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reservationData.getId();
                reservationData.getParkingId();
                reservationData.getUserId();
                String startDateTimeString =  startDate.getText().toString()+ " " +startTime.getText().toString();
                String endDateTimeString =  endDate.getText().toString()+ " " +endTime.getText().toString();
                Log.d("ERS", "onClick: " + sDateTime + " "+ eDateTime);
                Log.d("ERS", "Edit reservation: " + startDateTimeString + " "+endDateTimeString);
                Double hours = ParkOnTheGo.getInstance().getDateTimeDiff(startDateTimeString, endDateTimeString);
                double totalPrice = new Double(hours * reservationData.getPrice());

                Calendar calendar = Calendar.getInstance();
                Date startDateTimeTemp;
                Date endDateTimeTemp;
                Date currentTimeTemp;
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                try {
                    startDateTimeTemp = dateFormat.parse(startDate.getText().toString() + " " + startTime.getText().toString());
                    endDateTimeTemp = dateFormat.parse(endDate.getText().toString() + " " + endTime.getText().toString());
                    currentTimeTemp = dateFormat.parse(dateFormat.format(calendar.getTime()));
                    Log.d("StartDate", "onTimeSet: " + startDateTimeTemp);
                    Log.d("EndDate", "onTimeSet: " + endDateTimeTemp);
                    Log.d("CuurentDate", "onTimeSet: " + currentTimeTemp);
                    Log.d("Compare value", "onTimeSet: " + startDateTimeTemp.compareTo(currentTimeTemp));
                    Log.d("Compare value", "onTimeSet: " + endDateTimeTemp.compareTo(currentTimeTemp));
                    if (startDateTimeTemp.compareTo(currentTimeTemp) < 0 || endDateTimeTemp.compareTo(currentTimeTemp) < 0) {
                        new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops...")
                                .setContentText("You can't select start or end past time")
                                .show();

                        return;
                    }
                } catch (Exception ex) {
                    Log.d("parse error", "onValidationSucceeded: " + ex.getMessage());
                }
                updateReservation(reservationData.getId(), reservationData.getParkingId(),reservationData.getUserId(),startDateTimeString,endDateTimeString,totalPrice);




            }
        });



    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "SelectLocationToReserve Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://edu.scu.smurali.parkonthego/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(EditReservationActivity.this,ReservationsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "SelectLocationToReserve Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://edu.scu.smurali.parkonthego/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */

    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */


        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */

    }

  // Date and Time picker

    //Ini date picker
    public void initDatePicker() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Calendar cal = Calendar.getInstance();
            startDate.setText(dateFormat.format(cal.getTime()));
            endDate.setText(dateFormat.format(cal.getTime()));
        } catch (Exception ex) {
            Log.e("parse error init ", "onCreateDialog: " + ex.getMessage());
        }
    }
    public void initTimePicker() {
        try {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            String min = String.format("%02d", minute);
            String hou = String.format("%02d", hour);
            String time = hou + ":" + min;
            startTime.setText(time);

            if (hour != 23) {
                String endhou = String.format("%02d", (hour + 1));
                String time2 = endhou + ":" + min;
                endTime.setText(time2);
                return;
            } else {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, 1);
                SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                endDate.setText(format.format(c.getTime()));
                String time2 = "00" + ":" + min;
                endTime.setText(time2);
                return;
            }

        } catch (Exception ex) {
            Log.e("parse error init ", "onCreateDialog: " + ex.getMessage());
        }
    }

    public static class DatePickerDialogFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {
        public static final int FLAG_START_DATE = 0;
        public static final int FLAG_END_DATE = 1;

        private int flag = 0;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Date date;
            Calendar calendar = Calendar.getInstance();
            if (flag == FLAG_START_DATE) {
                try {
                    date = dateFormat.parse(startDate.getText().toString());
                    calendar.setTime(date);
                } catch (Exception ex) {
                    Log.d("Date pull error", "onCreateDialog: " + ex.getMessage());
                }
            } else {
                try {
                    date = dateFormat.parse(endDate.getText().toString());
                    calendar.setTime(date);
                } catch (Exception ex) {
                    Log.d("Date pull error", "onCreateDialog: " + ex.getMessage());
                }
            }


            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dt = new DatePickerDialog(getActivity(), this, year, month, day);
            if (flag == FLAG_START_DATE) {
                Calendar cal = Calendar.getInstance();
                DatePicker datePicker = dt.getDatePicker();
                datePicker.setMinDate(cal.getTimeInMillis());
                return dt;
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                Log.d("date1", "onDateChanged: " + startDate.getText().toString());
                try {
                    Date d = sdf.parse(startDate.getText().toString());
                    DatePicker datePicker = dt.getDatePicker();
                    Log.d("timestamp", "onDateChanged: " + d.getTime());
                    datePicker.setMinDate(d.getTime());
                } catch (Exception ex) {
                    Log.d("Date parse error", "onCreateDialog: " + ex.getMessage());
                }
                return dt;
            }

        }

        public void setFlag(int i) {
            flag = i;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, monthOfYear, dayOfMonth);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            if (flag == FLAG_START_DATE) {
                try {
                    Date date1 = format.parse(format.format(calendar.getTime()));
                    Date date2 = format.parse(endDate.getText().toString());
                    if (date1.compareTo(date2) > 0) {
                        endDate.setText(format.format(calendar.getTime()));
                    }
                } catch (Exception ex) {
                    Log.d("Date parse error", "onDateSet: " + ex.getMessage());
                }
                startDate.setText(format.format(calendar.getTime()));
            } else if (flag == FLAG_END_DATE) {
                endDate.setText(format.format(calendar.getTime()));
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////

    public static class TimePickerDialogFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        public static final int FLAG_START_TIME = 0;
        public static final int FLAG_END_TIME = 1;

        public Context getTempCntext() {
            return tempCntext;
        }

        public void setTempCntext(Context tempCntext) {
            this.tempCntext = tempCntext;
        }

        Context tempCntext;
        private int flag = 0;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            Date date;
            Calendar calendar = Calendar.getInstance();
            if (flag == FLAG_START_TIME) {
                try {
                    date = dateFormat.parse(startDate.getText().toString() + " " + startTime.getText().toString());
                    calendar.setTime(date);
                } catch (Exception ex) {
                    Log.d("Date pull error", "onCreateDialog: " + ex.getMessage());
                }
            } else {
                try {
                    date = dateFormat.parse(endDate.getText().toString() + " " + endTime.getText().toString());
                    calendar.setTime(date);
                } catch (Exception ex) {
                    Log.d("Date pull error", "onCreateDialog: " + ex.getMessage());
                }
            }
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            TimePickerDialog tp = new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
            if (flag == FLAG_START_TIME) {
                return tp;
            } else {
                // tp.updateTime(hour + 1, minute);
                return tp;
            }
        }

        public void setFlag(int i) {
            flag = i;
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar calendar = Calendar.getInstance();
            Date startDateTimeTemp;
            Date endDateTimeTemp;
            Date currentTimeTemp;
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            String min = String.format("%02d", minute);
            String hou = String.format("%02d", hourOfDay);

            String time = hou + ":" + min;
            if (flag == FLAG_START_TIME) {
                try {
                    startDateTimeTemp = dateFormat.parse(startDate.getText().toString() + " " + hourOfDay + ":" + minute);
                    endDateTimeTemp = dateFormat.parse(endDate.getText().toString() + " " + endTime.getText().toString());
                    currentTimeTemp = dateFormat.parse(dateFormat.format(calendar.getTime()));
                    Log.d("StartDate", "onTimeSet: " + startDateTimeTemp);
                    Log.d("EndDate", "onTimeSet: " + endDateTimeTemp);
                    Log.d("CuurentDate", "onTimeSet: " + currentTimeTemp);
                    Log.d("Compare value", "onTimeSet: "+startDateTimeTemp.compareTo(currentTimeTemp));
                    Log.d("Compare value", "onTimeSet: "+endDateTimeTemp.compareTo(currentTimeTemp));
                    if(startDateTimeTemp.compareTo(currentTimeTemp) < 0 || endDateTimeTemp.compareTo(currentTimeTemp) < 0){
                        new SweetAlertDialog(tempCntext, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops...")
                                .setContentText("You can't select start or end past time")
                                .show();

                    }
                    else if (startDateTimeTemp.compareTo(endDateTimeTemp) > 0 || startDateTimeTemp.compareTo(endDateTimeTemp) == 0) {
                        startTime.setText(time);
                        if (hourOfDay != 23) {
                            String endhou = String.format("%02d", (hourOfDay + 1));
                            String time2 = endhou + ":" + min;
                            endTime.setText(time2);
                            return;
                        }else{
                            Calendar c = Calendar.getInstance();
                            c.setTime(endDateTimeTemp);
                            c.add(Calendar.DATE, 1);
                            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                            endDate.setText(format.format(c.getTime()));
                            String time2 = "00" + ":" + min;
                            endTime.setText(time2);
                            return;
                        }
                    }else{
                        startTime.setText(time);
                    }
                } catch (Exception ex) {
                    Log.d("Date pull error", "onCreateDialog: " + ex.getMessage());
                    startTime.setText(time);
                }

            } else if (flag == FLAG_END_TIME) {
                try {
                    startDateTimeTemp = dateFormat.parse(startDate.getText().toString() + " " + startTime.getText().toString());
                    endDateTimeTemp = dateFormat.parse(endDate.getText().toString() + " " + hourOfDay + ":" + minute);
                    currentTimeTemp = dateFormat.parse(dateFormat.format(calendar.getTime()));
                    Log.d("StartDate", "onTimeSet: " + startDateTimeTemp);
                    Log.d("EndDate", "onTimeSet: " + endDateTimeTemp);
                    Log.d("CuurentDate", "onTimeSet: " + currentTimeTemp);
                    Log.d("Compare value", "onTimeSet: "+startDateTimeTemp.compareTo(currentTimeTemp));
                    Log.d("Compare value", "onTimeSet: "+endDateTimeTemp.compareTo(currentTimeTemp));
                    if(endDateTimeTemp.compareTo(currentTimeTemp) < 0 || startDateTimeTemp.compareTo(currentTimeTemp) < 0){
                        new SweetAlertDialog(tempCntext, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops...")
                                .setContentText("You can't select start or end past time")
                                .show();
                    }else if (startDateTimeTemp.compareTo(endDateTimeTemp) > 0 || startDateTimeTemp.compareTo(endDateTimeTemp) == 0) {
                        if (hourOfDay != 0) {
                            endTime.setText(time);
                            String endhou = String.format("%02d", (hourOfDay - 1));
                            String time2 = endhou + ":" + min;
                            startTime.setText(time2);
                        }else{
                            Calendar c = Calendar.getInstance();
                            c.setTime(startDateTimeTemp);
                            c.add(Calendar.DATE, -1);
                            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                            startDate.setText(format.format(c.getTime()));
                            String time2 = "23" + ":" + min;
                            startTime.setText(time2);
                            return;
                        }
                    }else{
                        endTime.setText(time);
                    }
                } catch (Exception ex) {
                    Log.d("Date pull error", "onCreateDialog: " + ex.getMessage());
                    endTime.setText(time);
                }

            }

        }
    }

    //End of date time picker

    public void updateReservation(Integer reservationId, Integer parkingId,
                                Integer userId,
                                String sDateTime,
                                String eDatetTime, Double totalPrice) {

        if (ParkOnTheGo.getInstance().isConnectedToInterNet()) {
            ReservationServices reservationServices = ParkOnTheGo.getInstance().getReservationServices();
            ParkOnTheGo.getInstance().showProgressDialog();
            Call<ReservationUpdateResponse> call = reservationServices.updateReservation(reservationId,parkingId, userId, sDateTime, eDatetTime, totalPrice);
            Log.d("Calling", "register: " + call + " " +reservationId +" " + parkingId + " " + userId + " " + sDateTime + " " + eDatetTime + " " + totalPrice);
            call.enqueue(new Callback<ReservationUpdateResponse>() {
                @Override
                public void onResponse(Call<ReservationUpdateResponse> call,
                                       Response<ReservationUpdateResponse> response) {
                    ParkOnTheGo.getInstance().hideProgressDialog();
                    Log.d("Edit reponse", "onResponse: "+response.code()+response.message()+response.body().getSuccess());
                    if (response.isSuccessful()) {
                        parseReservationUpdateResponse(response.body());
                    }
                }

                @Override
                public void onFailure(Call<ReservationUpdateResponse> call, Throwable throwable) {
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

    private void parseReservationUpdateResponse(ReservationUpdateResponse response) {

        if (response.getSuccess() == true) {
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Reservation updated")
                    .setConfirmText("Ok")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            startActivity(new Intent(EditReservationActivity.this, ReservationsActivity.class));
                            finish();
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


}
