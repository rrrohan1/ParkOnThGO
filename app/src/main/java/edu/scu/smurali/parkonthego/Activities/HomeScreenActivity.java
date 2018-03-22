package edu.scu.smurali.parkonthego.Activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import edu.scu.smurali.parkonthego.ParkOnTheGo;
import edu.scu.smurali.parkonthego.R;
import edu.scu.smurali.parkonthego.retrofit.reponses.ProfileResponse;
import edu.scu.smurali.parkonthego.retrofit.reponses.SearchData;
import edu.scu.smurali.parkonthego.retrofit.reponses.SearchResponse;
import edu.scu.smurali.parkonthego.retrofit.services.LocationServices;
import edu.scu.smurali.parkonthego.retrofit.services.UserServices;
import edu.scu.smurali.parkonthego.util.PreferencesManager;
import mehdi.sakout.fancybuttons.FancyButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;

public class HomeScreenActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Validator.ValidationListener {

    static int userId;
    @NotEmpty(message = "Please select start date")
    private static TextView startDate;
    @NotEmpty(message = "Please select start time")
    private static TextView startTime;
    @NotEmpty(message = "Please select end date")
    private static TextView endDate;
    @NotEmpty(message = "Please select end time")
    private static TextView endTime;
    PlaceAutocompleteFragment autocompleteFragment;
    ArrayList<SearchData> locationList;
    ImageButton startDateButton, endDateButton, startTimeButton, endTimeButton;
    // private Button searchParkingLocations;

    private FancyButton searchParkingLocations;
    int backButtonCount=0;
    private LatLng searchedLatLng;
    private String searchedAddress;
    private Context mContext;
    private String sDateTime, eDateTime;
   // private Button currentLocationButton;

    private FancyButton currentLocationButton;

    private TextView navUserName;
    private TextView navEmail;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    private Double latitude, longitude;

    PreferencesManager pm;


    private DatePickerDialogFragment uDatePickerDialogFragment;
    private TimePickerDialogFragment uTimePickerDialogFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContext = this;
        uDatePickerDialogFragment = new DatePickerDialogFragment();
        uTimePickerDialogFragment = new TimePickerDialogFragment();
        uTimePickerDialogFragment.setTempCntext(mContext);
        pm = PreferencesManager.getInstance(mContext);
        try {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle("New Reservation");
            actionBar.setIcon(R.mipmap.ic_park);
            //  actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            // actionBar.setHomeButtonEnabled(true);


        } catch (NullPointerException ex) {
            Log.d("Home Screen", "onCreate: Null pointer in action bar " + ex.getMessage());
        }
        final Validator validator = new Validator(this);
        validator.setValidationListener(this);

        startTime = (TextView) findViewById(R.id.homeScreenStartTime);
        endTime = (TextView) findViewById(R.id.homeScreenEndTime);
        startDate = (TextView) findViewById(R.id.homeScreenStartDate);
        endDate = (TextView) findViewById(R.id.homeScreenEndDate);
        // getting array list of locations
        locationList = new ArrayList<SearchData>();
        final PreferencesManager pm = PreferencesManager.getInstance(mContext);
        userId = pm.getUserId();
        //currentLocationButton = (Button) findViewById(R.id.currentLocationButton);

        currentLocationButton = (FancyButton) findViewById(R.id.currentLocationButton);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
/////////////////////////////////// permission checks start////////////////////////////////////////////////////////

        //////////////fine location//////////////////////////////////////
        if (ContextCompat.checkSelfPermission(HomeScreenActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeScreenActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(HomeScreenActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);



                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        ////////////////////////course location///////////////////////////////////////////////////////////
        if (ContextCompat.checkSelfPermission(HomeScreenActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeScreenActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(HomeScreenActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);


                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        ////////////////////////// internet///////////////////////////////////////////////////////
        if (ContextCompat.checkSelfPermission(HomeScreenActivity.this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeScreenActivity.this,
                    Manifest.permission.INTERNET)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(HomeScreenActivity.this,
                        new String[]{Manifest.permission.INTERNET},
                        1);


                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        ////////////////////////////// call phone ///////////////////////////////////////////////
        if (ContextCompat.checkSelfPermission(HomeScreenActivity.this,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeScreenActivity.this,
                    Manifest.permission.CALL_PHONE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(HomeScreenActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        1);


                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        //////////////////////////////////////nfc//////////////////////////////////////////////////////
        if (ContextCompat.checkSelfPermission(HomeScreenActivity.this,
                Manifest.permission.NFC)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeScreenActivity.this,
                    Manifest.permission.NFC)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(HomeScreenActivity.this,
                        new String[]{Manifest.permission.NFC},
                        1);


                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        /////////////////////////////////// write external storage///////////////////////////////////////////////////
        if (ContextCompat.checkSelfPermission(HomeScreenActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeScreenActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(HomeScreenActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);


                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        //////////////////////////////////// permision checks end/////////////////////////////////////////////////


        // current location button//
        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    updateLocation();
                    if (latitude != null && longitude != null) {
                        searchedLatLng = new LatLng(latitude, longitude);
                        searchedAddress = getCompleteAddressString(latitude, longitude);
                        autocompleteFragment.setText(searchedAddress);
                    }
                }catch(Exception ex){
                    Log.d("Denied permision", "onClick: User denied gps permision "+ex.getMessage());
                }
            }
        });


//////////////////////date and time pickers/////////////////////////////////////

        startTimeButton = (ImageButton) findViewById(R.id.startTimeImageButton);
        endTimeButton = (ImageButton) findViewById(R.id.endTimeImageButton);

        startTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uTimePickerDialogFragment.setFlag(TimePickerDialogFragment.FLAG_START_TIME);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                uTimePickerDialogFragment.show(ft, "timePicker");
            }
        });


        endTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uTimePickerDialogFragment.setFlag(TimePickerDialogFragment.FLAG_END_TIME);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                uTimePickerDialogFragment.show(ft, "timePicker");
            }
        });


        startDateButton = (ImageButton) findViewById(R.id.startDateImageButton);
        endDateButton = (ImageButton) findViewById(R.id.endDateImageButton);

        startDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uDatePickerDialogFragment.setFlag(DatePickerDialogFragment.FLAG_START_DATE);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                uDatePickerDialogFragment.show(ft, "datePicker");
            }
        });

        endDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uDatePickerDialogFragment.setFlag(DatePickerDialogFragment.FLAG_END_DATE);
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

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uTimePickerDialogFragment.setFlag(TimePickerDialogFragment.FLAG_END_TIME);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                uTimePickerDialogFragment.show(ft, "timePicker");
            }
        });
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uDatePickerDialogFragment.setFlag(DatePickerDialogFragment.FLAG_START_DATE);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                uDatePickerDialogFragment.show(ft, "datePicker");

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

        initDatePicker();
        initTimePicker();


///////////////////////////// navigation bar//////////////////////////////////

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
///////////////////////// google search fragment////////////////////////////////////
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                searchedLatLng = place.getLatLng();
                searchedAddress = place.getAddress().toString();


                Log.i("place name", "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {

                Log.i("error ", "An error occurred: " + status);
            }
        });


//        searchParkingLocations = (Button) findViewById(R.id.searchParkingLocation);


        searchParkingLocations = (FancyButton) findViewById(R.id.searchParkingLocation);

        // searchParkingLocations = (Button) findViewById(R.id.searchParkingLocation);

        searchParkingLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchedLatLng == null) {
                    String message = " please select the destination adddress to find parking";
                    String title = " Select Location";
//                    ParkOnTheGo.getInstance().showAlert(HomeScreenActivity.this, message, title);
                    new SweetAlertDialog(HomeScreenActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Please select a location")
                            .setConfirmText("Ok")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    //startActivity(new Intent(HomeScreenActivity.this, HomeScreenActivity.class));
                                }
                            })
                            .show();

                } else {
                    validator.validate();

                }

            }
        });


        //Get profile from server
        getProfile();

        View header = navigationView.getHeaderView(0);

        navUserName = (TextView) header.findViewById(R.id.userName);

        navUserName.setText(pm.getUserName());
        navEmail = (TextView) header.findViewById(R.id.email);
        navEmail.setText(pm.getEmail());


    }

    ////////////////////update current location method////////////////////////////////////
    public void updateLocation() {
        if (ContextCompat.checkSelfPermission(HomeScreenActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeScreenActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(HomeScreenActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();

        }else {
            // get current locaation(last known location)
            Location loc = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
            if (latitude != null && longitude != null) {
                return;
            }

            if (loc != null) {
                latitude = loc.getLatitude();
                longitude = loc.getLongitude();
            } else {
                latitude = null;
                longitude = null;
            }
        }

    }
    private void buildAlertMessageNoGps() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("GPS permission")
                .setContentText("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelText("No")
                .setConfirmText("Ok")
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        sDialog.dismissWithAnimation();
                    }
                })
                .show();
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
//                .setCancelable(false)
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
//                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                    }
//                })
//                .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
//                        dialog.cancel();
//                    }
//                });
//        final AlertDialog alert = builder.create();
//        alert.show();
    }
    //////////////////// update method ends////////////////////////////////////////////////


    /////////////////////////convert lat ang long to readable address//////////////////////
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("Current loction add", "" + strReturnedAddress.toString());
            } else {
                Log.w("Current  add", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current  address", "Canont get Address!");
        }
        return strAdd;
    }
    //////////////////////////// get address method ends///////////////////////////////////////////


    @Override
    public void onValidationSucceeded() {
        String startDateValue = startDate.getText().toString();
        String startTimeValue = startTime.getText().toString();
        String endDateValue = endDate.getText().toString();
        String endTimeValue = endTime.getText().toString();
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

        sDateTime = startDateValue + " " + startTimeValue;
        eDateTime = endDateValue + " " + endTimeValue;
        Log.d("Data for getlocation", "onValidationSucceeded: " + sDateTime);
        Log.d("Data for getlocation", "onValidationSucceeded: " + eDateTime);


        searchLocationsNearMe(userId, searchedLatLng.latitude, searchedLatLng.longitude, 5, sDateTime, eDateTime);
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
                Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                v.setTextColor(Color.RED);
                toast.show();
                // Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void searchLocationsNearMe(int id,
                                      Double lat,
                                      Double lng,
                                      Integer distance, String sDateTime, String eDateTime) {

        if (ParkOnTheGo.getInstance().isConnectedToInterNet()) {
            LocationServices locationServices = ParkOnTheGo.getInstance().getLocationServices();
//            ParkOnTheGo.getInstance().showProgressDialog(mContext.getString(R.string
//                    .login_signin), mContext.getString(R.string.login_please_wait));
            Call<SearchResponse> call = locationServices.getLocationsNearMe(id, lat, lng, distance, sDateTime, eDateTime);
            Log.d("Calling", "register: " + call);
            call.enqueue(new Callback<SearchResponse>() {
                @Override
                public void onResponse(Call<SearchResponse> call,
                                       Response<SearchResponse> response) {
                    //ParkOnTheGo.getInstance().hideProgressDialog();
                    if (response.isSuccessful()) {
                        parseResponse(response.body());
                    }
                }

                @Override
                public void onFailure(Call<SearchResponse> call, Throwable throwable) {
                    Toast.makeText(getApplicationContext(), "Request failed" + throwable, Toast.LENGTH_SHORT).show();

                    // ParkOnTheGo.getInstance().hideProgressDialog();
                    // ParkOnTheGo.getInstance().handleError(throwable);
                }
            });
        } else {
            ParkOnTheGo.getInstance().showAlert(mContext.getString(R.string.no_network));
        }
    }

    private void parseResponse(SearchResponse response) {
        Toast.makeText(getApplicationContext(), "Login Sucess" + response.getSuccess(), Toast.LENGTH_SHORT).show();
        if (response.getSuccess() == true) {
            PreferencesManager pm = PreferencesManager.getInstance(mContext);
            Log.d("Data", "parseResponse: " + response.getData().size());

            //  locationList= (ArrayList<SearchData>) response.getData();
            for (int i = 0; i < response.getData().size(); i++) {

                locationList.add(response.getData().get(i));
                Log.d("Data", "parseResponse: " + locationList.get(i).toString());
            }
            Intent intent = new Intent(HomeScreenActivity.this, LocationsOnMap.class);
            intent.putExtra("locationList", locationList);
            intent.putExtra("searchedLocationLat", searchedLatLng.latitude);
            intent.putExtra("searchedLocationLong", searchedLatLng.longitude);
            intent.putExtra("searchedLocationAddress", searchedAddress);
            Log.d("Home screen", "parseResponse: " + sDateTime);
            Log.d("Home screen", "parseResponse: " + eDateTime);
            intent.putExtra("startDateTime", sDateTime);
            intent.putExtra("endDateTime", eDateTime);
            startActivity(intent);


        } else {

        }
    }


    @Override
    public void onBackPressed() {
        {

            if(backButtonCount >= 1)
            {
                backButtonCount=0;
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            else
            {
                Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
                backButtonCount++;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_uninstall) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(HomeScreenActivity.this, HomeScreenActivity.class);
            startActivity(intent);
            finish();
        }

        if (id == R.id.nav_reservation) {

            Intent intent = new Intent(HomeScreenActivity.this, ReservationsActivity.class);
            startActivity(intent);
            finish();


        } else if (id == R.id.nav_settings) {


            Intent intent = new Intent(HomeScreenActivity.this, SettingActivity.class);
            startActivity(intent);
            finish();


        } else if (id == R.id.nav_call) {

            final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;


            if (ContextCompat.checkSelfPermission(HomeScreenActivity.this,
                    Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(HomeScreenActivity.this,
                        Manifest.permission.CALL_PHONE)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(HomeScreenActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            1);


                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:+1 669 220 8549"));
            startActivity(callIntent);


        } else if (id == R.id.nav_help) {

            Intent intent = new Intent(HomeScreenActivity.this, HelpActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_logout) {
            PreferencesManager.getInstance(mContext).clear();
            Intent intent = new Intent(HomeScreenActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


//////////////////////// date and time  picker fragments//////////////////////////////////////////

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
                    Log.d("Compare value", "onTimeSet: " + startDateTimeTemp.compareTo(currentTimeTemp));
                    Log.d("Compare value", "onTimeSet: " + endDateTimeTemp.compareTo(currentTimeTemp));
                    if (startDateTimeTemp.compareTo(currentTimeTemp) < 0 || endDateTimeTemp.compareTo(currentTimeTemp) < 0) {
                        new SweetAlertDialog(tempCntext, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops...")
                                .setContentText("You can't select start or end past time")
                                .show();

                    } else if (startDateTimeTemp.compareTo(endDateTimeTemp) > 0 || startDateTimeTemp.compareTo(endDateTimeTemp) == 0) {
                        startTime.setText(time);
                        if (hourOfDay != 23) {
                            String endhou = String.format("%02d", (hourOfDay + 1));
                            String time2 = endhou + ":" + min;
                            endTime.setText(time2);
                            return;
                        } else {
                            Calendar c = Calendar.getInstance();
                            c.setTime(endDateTimeTemp);
                            c.add(Calendar.DATE, 1);
                            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                            endDate.setText(format.format(c.getTime()));
                            String time2 = "00" + ":" + min;
                            endTime.setText(time2);
                            return;
                        }
                    } else {
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
                    Log.d("Compare value", "onTimeSet: " + startDateTimeTemp.compareTo(currentTimeTemp));
                    Log.d("Compare value", "onTimeSet: " + endDateTimeTemp.compareTo(currentTimeTemp));
                    if (endDateTimeTemp.compareTo(currentTimeTemp) < 0 || startDateTimeTemp.compareTo(currentTimeTemp) < 0) {
                        new SweetAlertDialog(tempCntext, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops...")
                                .setContentText("You can't select start or end past time")
                                .show();
                    } else if (startDateTimeTemp.compareTo(endDateTimeTemp) > 0 || startDateTimeTemp.compareTo(endDateTimeTemp) == 0) {
                        if (hourOfDay != 0) {
                            endTime.setText(time);
                            String endhou = String.format("%02d", (hourOfDay - 1));
                            String time2 = endhou + ":" + min;
                            startTime.setText(time2);
                        } else {
                            Calendar c = Calendar.getInstance();
                            c.setTime(startDateTimeTemp);
                            c.add(Calendar.DATE, -1);
                            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                            startDate.setText(format.format(c.getTime()));
                            String time2 = "23" + ":" + min;
                            startTime.setText(time2);
                            return;
                        }
                    } else {
                        endTime.setText(time);
                    }
                } catch (Exception ex) {
                    Log.d("Date pull error", "onCreateDialog: " + ex.getMessage());
                    endTime.setText(time);
                }

            }

        }
    }


    ///////////////////////////////////////////////////// date and time picker fragments end/////////////////////////////////////////////////////////

    public void getProfile() {

        if (ParkOnTheGo.getInstance().isConnectedToInterNet()) {
            UserServices userServices = ParkOnTheGo.getInstance().getUserServices();
            // ParkOnTheGo.getInstance().showProgressDialog("Login", "Please Wait");
            ParkOnTheGo.getInstance().showProgressDialog();
            Call<ProfileResponse> call = userServices.getProfile(PreferencesManager.getInstance(mContext).getUserId());
            Log.d("Calling", "Get profile: " + call);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call,
                                       Response<ProfileResponse> response) {
                    ParkOnTheGo.getInstance().hideProgressDialog();
                    if (response.isSuccessful()) {
                        parseProfileResponse(response.body());
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable throwable) {
                    Toast.makeText(getApplicationContext(), "Request failed" + throwable, Toast.LENGTH_SHORT).show();

                    ParkOnTheGo.getInstance().hideProgressDialog();
                    ParkOnTheGo.getInstance().handleError(throwable);
                }
            });
        } else {
            ParkOnTheGo.getInstance().showAlert(mContext.getString(R.string.no_network));
        }
    }

    private void parseProfileResponse(ProfileResponse response) {
        if (response.getSuccess() == true) {
            pm.updateFirstName(response.getData().getFirstName());
            pm.updateLastName(response.getData().getLastName());
            pm.updateEmail(response.getData().getEmail());
            pm.updateUserName(response.getData().getFirstName() + " " + response.getData().getLastName());

        } else {

        }
    }


}
