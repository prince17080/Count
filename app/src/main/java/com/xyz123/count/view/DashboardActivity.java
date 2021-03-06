package com.xyz123.count.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.xyz123.count.R;
import com.xyz123.count.model.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = "DashboardTag";
    private static final int NEW_COUNTER_ACTIVITY_REQUEST_CODE = 1;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
//    private CollectionReference counterCollectionReference;
    private CounterAdapter counterAdapter;
    private TextView emptyTextView;
    private CircleImageView profileImageView;
    private TextView nameTextView;
    private TextView emailTextView;
//    private List<Counter> counterArrayList;
    private CounterViewModel counterViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        emptyTextView = (TextView) findViewById(R.id.empty_text_view);
        emptyTextView.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, AddCounterActivity.class);
                startActivityForResult(intent, NEW_COUNTER_ACTIVITY_REQUEST_CODE);
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
//        counterCollectionReference = db.collection("users").document(user.getUid()).collection("counters");
        getSupportActionBar().setTitle(user.getDisplayName().split(" ")[0] + "'s Dashboard");

        View headerLayout = navigationView.getHeaderView(0);

        profileImageView = (CircleImageView) headerLayout.findViewById(R.id.profile_image_view);
        nameTextView = (TextView) headerLayout.findViewById(R.id.dashboard_name_text_view);
        emailTextView = (TextView) headerLayout.findViewById(R.id.dashboard_email_text_view);

        Picasso.get().load(user.getPhotoUrl().toString()).into(profileImageView);
        nameTextView.setText(user.getDisplayName());
        emailTextView.setText(user.getEmail());


        // Here work starts

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), ((LinearLayoutManager) linearLayoutManager).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        counterAdapter = new CounterAdapter();

        counterAdapter.setOnItemClickListener(new CounterAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(DashboardActivity.this, CounterActivity.class);
                Counter counter = counterAdapter.getCounterList().get(position);
                intent.putExtra("counter", counter);
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(counterAdapter);

        counterViewModel = ViewModelProviders.of(this).get(CounterViewModel.class);
        counterViewModel.getAllCounters().observe(this, new Observer<List<Counter>>() {
            @Override
            public void onChanged(List<Counter> counters) {
                // isnt this same as changing the counterArrayList in this file?
                counterAdapter.setCounterList(counters);
            }
        });

        LinearLayout navViewLinearLayout = (LinearLayout) navigationView.findViewById(R.id.nav_view_linear_layout);
        Button playStoreButton = (Button) navViewLinearLayout.findViewById(R.id.playstore_button);

        playStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // reference: https://stackoverflow.com/questions/11753000/how-to-open-the-google-play-store-directly-from-my-android-application
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                Toast.makeText(DashboardActivity.this, "An informative review will help us improve! :)", Toast.LENGTH_LONG).show();
            }
        });

    }

//    private void setupRecyclerView() {
//        Query query = counterCollectionReference;
//        FirestoreRecyclerOptions<Counter> options = new FirestoreRecyclerOptions.Builder<Counter>()
//                .setQuery(query, Counter.class)
//                .build();
//
//        counterAdapter = new CounterAdapter(emptyTextView);
//
//        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list_view);
//        recyclerView.setHasFixedSize(true);
//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setAdapter(counterAdapter);
//
//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), ((LinearLayoutManager) layoutManager).getOrientation());
//        recyclerView.addItemDecoration(dividerItemDecoration);
//
//    }

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
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_logout) {
            Utils.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            // open settings activity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_contact) {
            sendEmail();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sendEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"firehousedroid@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Bug report from " +user.getDisplayName());
        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            Toast.makeText(this, "Make sure you select the correct account for sending mail in your email app", Toast.LENGTH_LONG).show();
            startActivity(emailIntent);
        }
        else {
            Toast.makeText(this, "Please install an email app for sending bug request via mail", Toast.LENGTH_LONG).show();
        }

    }
}
