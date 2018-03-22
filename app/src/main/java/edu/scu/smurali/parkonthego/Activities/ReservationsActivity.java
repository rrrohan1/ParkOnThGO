package edu.scu.smurali.parkonthego.Activities;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import cn.pedant.SweetAlert.SweetAlertDialog;
import edu.scu.smurali.parkonthego.ParkOnTheGo;
import edu.scu.smurali.parkonthego.R;
import edu.scu.smurali.parkonthego.retrofit.reponses.LoginData;
import edu.scu.smurali.parkonthego.retrofit.reponses.ReservationCfnResponse;
import edu.scu.smurali.parkonthego.retrofit.reponses.ReservationData;
import edu.scu.smurali.parkonthego.retrofit.reponses.ReservationDeleteResponse;
import edu.scu.smurali.parkonthego.retrofit.reponses.ReservationResponse;
import edu.scu.smurali.parkonthego.retrofit.services.ReservationServices;
import edu.scu.smurali.parkonthego.util.PreferencesManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    private Context mContext;

    GestureDetector gestureDetector;
    TouchListener onTouchListener;

    private TextView navUserName;
    private TextView navEmail;
    private HashMap<String,ReservationData> reservationListMap = new HashMap<String,ReservationData>();

    PreferencesManager pm;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);
        ParkOnTheGo.getInstance().setCurrentActivityContext(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContext = this;
        pm = PreferencesManager.getInstance(mContext);


        gestureDetector = new GestureDetector(this, new GestureListener());
        onTouchListener = new TouchListener();




        try {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle("My Reservations");
            actionBar.setIcon(R.mipmap.ic_park);
            //  actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            // actionBar.setHomeButtonEnabled(true);


        }
        catch(NullPointerException ex){
            Log.d("MyReservation Screen", "onCreate: Null pointer in action bar "+ex.getMessage());
        }


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

        // LIST VIEW

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.expandableListView);


        // preparing list data
        getUserReservation();

     //   expListView.setOnTouchListener(onTouchListener);



        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

                if(childPosition==0)
                {
                    // set directions
                    String desc = listDataHeader.get(groupPosition);
                    ReservationData clickedReservation = reservationListMap.get(desc);

                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+clickedReservation.getDescription());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }


                if(childPosition==1)
                {
                    // street view
                    String desc = listDataHeader.get(groupPosition);
                    ReservationData clickedReservation = reservationListMap.get(desc);
                    //                  ///////////////////////street view////////////////////
                    Uri gmmIntentUri = Uri.parse("google.streetview:cbll="+clickedReservation.getLatitude()+","+clickedReservation.getLongitude()+"&cbp=0,30,0,0,-15");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }

                if(childPosition==3)
                {
                    // Have to delete the reservation
                    String desc = listDataHeader.get(groupPosition);
                    final ReservationData clickedReservation = reservationListMap.get(desc);
                    new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Are you sure?")
                            .setConfirmText("Yes,delete it!")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    deleteReservation(clickedReservation.getId());
                                    sDialog.dismissWithAnimation();
                                }
                            })
                            .show();


                }

                if(childPosition==2)
                {
                    // Have to Edit the reservation
                    // get the reservation details
                    String desc = listDataHeader.get(groupPosition);
                    ReservationData clickedReservation = reservationListMap.get(desc);

                    // send intent to edit reservation page

                    Intent intent = new Intent(ReservationsActivity.this, EditReservationActivity.class);
                    intent.putExtra("ltdLng", new LatLng(clickedReservation.getLatitude(),clickedReservation.getLongitude()));
                    intent.putExtra("price", clickedReservation.getPrice());
                   intent.putExtra("searchedLocation", new LatLng(clickedReservation.getLatitude(),clickedReservation.getLongitude()));
                    intent.putExtra("searchedLocationAddress", clickedReservation.getDescription());
                    intent.putExtra("reservationData", (Serializable) clickedReservation);
                     intent.putExtra("activityName", "LocationsOnMap");
                   // intent.putExtra("listOfLocations", locationList);
                    intent.putExtra("startDateTime", clickedReservation.getStartingTime());
                    intent.putExtra("endDateTime", clickedReservation.getEndTime());
                    startActivity(intent);
                    finish();




                }





                return false;
            }
        });

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                //Toast.makeText(getApplicationContext(),
                  //      listDataHeader.get(groupPosition) + " Expanded",
                  //      Toast.LENGTH_SHORT).show();
            }
        });

        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {

                //Toast.makeText(getApplicationContext(),
                  //      listDataHeader.get(groupPosition) + " Collapsed",
                  //      Toast.LENGTH_SHORT).show();
            }
        });

    }


//Getting data from server

    public void getUserReservation() {

        if (ParkOnTheGo.getInstance().isConnectedToInterNet()) {
            ReservationServices reservationServices = ParkOnTheGo.getInstance().getReservationServices();
            ParkOnTheGo.getInstance().showProgressDialog();
            Call<ReservationResponse> call = reservationServices.getUserReservations(PreferencesManager.getInstance(mContext).getUserId());
            Log.d("Calling", "Reservation: " + call);
            call.enqueue(new Callback<ReservationResponse>() {
                @Override
                public void onResponse(Call<ReservationResponse> call,
                                       Response<ReservationResponse> response) {
                    ParkOnTheGo.getInstance().hideProgressDialog();
                    Log.d("Reservation parse", "parseResponse: "+response.code());
                    if (response.isSuccessful()) {
                        parseResponse(response.body());
                    }else if(response.code() == 404){
                        listDataHeader = new ArrayList<String>();
                        listDataChild = new HashMap<String, List<String>>();
                        listAdapter = new ExpandableListAdapter(mContext, listDataHeader, listDataChild);
                        expListView.setAdapter(listAdapter);
                        listAdapter.notifyDataSetChanged();
                    }

                }

                @Override
                public void onFailure(Call<ReservationResponse> call, Throwable throwable) {
                    Toast.makeText(getApplicationContext(), "Request failed" + throwable, Toast.LENGTH_SHORT).show();

                     ParkOnTheGo.getInstance().hideProgressDialog();
                     ParkOnTheGo.getInstance().handleError(throwable);
                }
            });
        } else {
            ParkOnTheGo.getInstance().showAlert(mContext.getString(R.string.no_network));
        }
    }

    private void parseResponse(ReservationResponse response) {
        //Toast.makeText(getApplicationContext(), "Reservation Data Sucess " + response.getSuccess(), Toast.LENGTH_SHORT).show();
        if (response.getSuccess() == true) {
            listDataHeader = new ArrayList<String>();
            listDataChild = new HashMap<String, List<String>>();


            List<String> reservationOption = new ArrayList<String>();

            reservationOption.add("Directions");
            reservationOption.add("Street View");
            reservationOption.add("Edit Reservation");
            reservationOption.add("Cancel Reservation");

            List<ReservationData> reservationsData = response.getData();
            for (ReservationData rev : reservationsData) {
                String temp = rev.getDescription() + "\n" +
                        rev.getStartingTime() + " " + rev.getEndTime();
                listDataChild.put(temp, reservationOption);
                listDataHeader.add(temp);
                reservationListMap.put(temp,rev);

            }
            listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

            //  setting list adapter
            expListView.setAdapter(listAdapter);
            listAdapter.notifyDataSetChanged();
        } else {
            listDataHeader = new ArrayList<String>();
            listDataChild = new HashMap<String, List<String>>();
            expListView.setAdapter(listAdapter);
            listAdapter.notifyDataSetChanged();
        }



    }





    protected class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        private static final int SWIPE_MIN_DISTANCE = 150;
        private static final int SWIPE_MAX_OFF_PATH = 100;
        private static final int SWIPE_THRESHOLD_VELOCITY = 100;

        private MotionEvent mLastOnDownEvent = null;

        @Override
        public boolean onDown(MotionEvent e)
        {
            mLastOnDownEvent = e;
            return super.onDown(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
        {
            if(e1 == null){
                e1 = mLastOnDownEvent;
            }
            if(e1==null || e2==null){
                return false;
            }

            float dX = e2.getX() - e1.getX();
            float dY = e1.getY() - e2.getY();

            if (Math.abs(dY) < SWIPE_MAX_OFF_PATH && Math.abs(velocityX) >= SWIPE_THRESHOLD_VELOCITY && Math.abs(dX) >= SWIPE_MIN_DISTANCE ) {
                if (dX > 0) {
                    //Toast.makeText(getApplicationContext(), "Right Swipe", Toast.LENGTH_SHORT).show();

//                    listAdapter.listDataHeader.remove(position);
//                    listAdapter.notifyDataSetChanged();

                } else {
                    //Toast.makeText(getApplicationContext(), "Left Swipe", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            else if (Math.abs(dX) < SWIPE_MAX_OFF_PATH && Math.abs(velocityY)>=SWIPE_THRESHOLD_VELOCITY && Math.abs(dY)>=SWIPE_MIN_DISTANCE ) {
                if (dY>0) {
                    //Toast.makeText(getApplicationContext(), "Up Swipe", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(getApplicationContext(), "Down Swipe", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        }
    }







    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ReservationsActivity.this,HomeScreenActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_home){
            Intent intent = new Intent(ReservationsActivity.this,HomeScreenActivity.class);
            startActivity(intent);
            finish();

        }

        if (id == R.id.nav_reservation) {


            Intent intent = new Intent(ReservationsActivity.this,ReservationsActivity.class);
            startActivity(intent);
            finish();


        } else if (id == R.id.nav_settings) {

            Intent intent = new Intent(ReservationsActivity.this,SettingActivity.class);
            startActivity(intent);
            finish();


        } else if (id == R.id.nav_call) {

            final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;

            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:+1669 220 8549"));

            if (ActivityCompat.checkSelfPermission(ReservationsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(ReservationsActivity.this,
                        Manifest.permission.CALL_PHONE)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(ReservationsActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            MY_PERMISSIONS_REQUEST_CALL_PHONE);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }


            }
            startActivity(callIntent);



        } else if (id == R.id.nav_help) {

            Intent intent = new Intent(ReservationsActivity.this,HelpActivity.class);
            startActivity(intent);
            finish();

        } else if(id == R.id.nav_logout){
            PreferencesManager.getInstance(mContext).clear();
            Intent intent = new Intent(ReservationsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }




    protected class TouchListener implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent e)
        {
            if (gestureDetector.onTouchEvent(e)){
                return true;
            }else{
                return false;
            }
        }
    }


    public void deleteReservation(Integer reservationId ) {

        if (ParkOnTheGo.getInstance().isConnectedToInterNet()) {
            ReservationServices reservationServices = ParkOnTheGo.getInstance().getReservationServices();
            ParkOnTheGo.getInstance().showProgressDialog();

            Log.d("ID", "deleteReservation: "+reservationId.toString());
            Call<ReservationDeleteResponse> call = reservationServices.deleteReservation(reservationId.toString());

            call.enqueue(new Callback<ReservationDeleteResponse>() {
                @Override
                public void onResponse(Call<ReservationDeleteResponse> call,
                                       Response<ReservationDeleteResponse> response) {
                    ParkOnTheGo.getInstance().hideProgressDialog();
                    if (response.isSuccessful()) {
                        parseDeleteReservationResponse(response.body());
                    }
                }

                @Override
                public void onFailure(Call<ReservationDeleteResponse> call, Throwable throwable) {
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

    private void parseDeleteReservationResponse(ReservationDeleteResponse response) {

        if (response.getSuccess() == true) {
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Reservation deleted")
                    .setConfirmText("Ok")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            getUserReservation();
                            listAdapter.notifyDataSetChanged();
                        }
                    })
                    .show();


        } else {
            new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText("Somthing went wrong, Please try later")
                    .show();
        }
    }
}
