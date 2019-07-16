package com.grace.customer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bdhobare.mpesa.Mpesa;
import com.bdhobare.mpesa.interfaces.AuthListener;
import com.bdhobare.mpesa.interfaces.MpesaListener;
import com.bdhobare.mpesa.models.STKPush;
import com.bdhobare.mpesa.utils.Pair;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.grace.customer.utils.Utils;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AuthListener, MpesaListener {

    Button payButton;
    public static final String BUSINESS_SHORT_CODE = "174379";
    public static final String PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";
    public static final String CONSUMER_KEY = "0UaR9N6dARGay8eTKZfVxWHVsUVoLLn5";
    public static final String CONSUMER_SECRET = "TU3L572FHBd89Jbw";
    public static final String CALLBACK_URL = "https://retailer.bdhobare.com/";

    MaterialDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mapLayout,MapViewFragment.newInstance());
        transaction.commit();

        Mpesa.with(HomeActivity.this, CONSUMER_KEY, CONSUMER_SECRET);

        payButton = (Button) findViewById(R.id.pay);
        payButton.setAlpha(.5f);
        payButton.setEnabled(true);

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askPhoneNumber();
            }
        });
    }
    private void askPhoneNumber(){
        MaterialDialog dialog = new MaterialDialog.Builder(HomeActivity.this)
                .input("07xxxxxxxx", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (!input.toString().startsWith("07")){
                            Toast.makeText(HomeActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        makePayment(input.toString().trim());
                        dialog.dismiss();

                    }
                })
                .title("ENTER MPESA PHONE NUMBER")
                .titleGravity(GravityEnum.CENTER)
                .positiveText("PAY")
                .negativeText("CANCEL")
                .widgetColorRes(R.color.colorPrimary)
                .build();
        dialog.show();
    }
    private void makePayment(String phone){
        STKPush.Builder builder = new STKPush.Builder(BUSINESS_SHORT_CODE, PASSKEY, 1,BUSINESS_SHORT_CODE, phone);
        builder.setCallBackURL(CALLBACK_URL + "mpesa");

        STKPush push = builder.build();

        Mpesa.getInstance().pay(HomeActivity.this, push);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onAuthError(Pair<Integer, String> result) {

    }

    @Override
    public void onAuthSuccess() {
        payButton.setEnabled(true);
        payButton.setAlpha(1f);
    }

    @Override
    public void onMpesaError(Pair<Integer, String> result) {
        Toast.makeText(HomeActivity.this, "MPESA payment failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMpesaSuccess(String MerchantRequestID, String CheckoutRequestID, String CustomerMessage) {
        dialog = Utils.configureDialog(HomeActivity.this, "Complete Payment", "Please enter your MPESA Pin in the pop up screen to complete payment.", "OK", new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {

                dialog.dismiss();

            }
        });
        dialog.show();
    }
}
