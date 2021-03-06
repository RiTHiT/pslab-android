package org.fossasia.pslab.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.adapters.MPUDataAdapter;
import org.fossasia.pslab.models.DataMPU6050;
import org.fossasia.pslab.models.SensorLogged;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by viveksb007 on 12/8/17.
 */

public class ShowLoggedData extends AppCompatActivity {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.layout_container)
    LinearLayout linearLayout;

    private Realm realm;
    private Context context;
    private ListView sensorListView;
    private ListView trialListView;
    private RecyclerView recyclerView;
    private String mSensor;
    boolean isRecyclerViewOnStack = false;
    boolean isTrialListViewOnStack = false;
    boolean isSensorListViewOnStack = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_logged_data);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        realm = Realm.getDefaultInstance();
        context = this;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getResources().getString(R.string.sensor_logged_data));
        }
        showSensorList();
    }

    private void showSensorList() {
        sensorListView = new ListView(this);
        linearLayout.addView(sensorListView);
        isSensorListViewOnStack = true;
        RealmResults<SensorLogged> results = realm.where(SensorLogged.class).findAll();
        ArrayList<String> sensorList = new ArrayList<>();
        if (results != null) {
            for (SensorLogged temp : results) {
                sensorList.add(temp.getSensor());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sensorList);
        sensorListView.setAdapter(adapter);
        sensorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String sensor = ((TextView) view).getText().toString();
                mSensor = sensor;
                showSensorTrialData(sensor);
            }
        });
    }

    private void showSensorTrialData(final String sensor) {
        Number trial;
        ArrayList<String> trialList = new ArrayList<>();

        switch (sensor) {
            case "MPU6050":
                trial = realm.where(DataMPU6050.class).max("trial");
                if (trial == null) return;
                long maxTrials = (long) trial + 1;
                for (int i = 0; i < maxTrials; i++) {
                    trialList.add("Trial #" + (i + 1));
                }
                break;
            default:
                // Todo : Add cases for other sensor
        }

        linearLayout.removeView(sensorListView);
        isSensorListViewOnStack = false;
        trialListView = new ListView(context);
        linearLayout.addView(trialListView);
        isTrialListViewOnStack = true;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, trialList);
        trialListView.setAdapter(adapter);
        trialListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                populateSensorData(sensor, position);
            }
        });
    }

    private void populateSensorData(String sensor, long trial) {
        linearLayout.removeView(trialListView);
        isTrialListViewOnStack = false;
        recyclerView = new RecyclerView(this);
        linearLayout.addView(recyclerView);
        isRecyclerViewOnStack = true;

        switch (sensor) {
            case "MPU6050":
                RealmResults<DataMPU6050> queryResults = realm.where(DataMPU6050.class).equalTo("trial", trial).findAll();
                MPUDataAdapter mpuDataAdapter = new MPUDataAdapter(queryResults);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(mpuDataAdapter);
                break;
            default:
                // Todo : Add other cases
        }

    }

    @Override
    public void onBackPressed() {
        if (isRecyclerViewOnStack) {
            linearLayout.removeView(recyclerView);
            isRecyclerViewOnStack = false;
            showSensorTrialData(mSensor);
            return;
        } else if (isTrialListViewOnStack) {
            linearLayout.removeView(trialListView);
            isTrialListViewOnStack = false;
            showSensorList();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_show_logged_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.export_logged_data:
                // Exporting locally logged data
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
