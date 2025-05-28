package com.example.ssmsprojectapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.ssmsprojectapp.databasehelpers.MeasurementDbHelper;
import com.example.ssmsprojectapp.datamodels.Measurement;
import com.google.android.material.tabs.TabLayout;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Graphs extends AppCompatActivity{

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private List<Measurement> measurements;

    private  MeasurementDbHelper measurementDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_graphs);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        measurementDbHelper = new MeasurementDbHelper(this);

        measurements = new ArrayList<>();

        getMeasurements();

        if (measurements.isEmpty()){
            Toast.makeText(this, "Hey i didnt get the data", Toast.LENGTH_SHORT).show();
        }

        // Setup ViewPager and TabLayout
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        //init the measurements List
        measurements = new ArrayList<>();


        setupViewPager();
        tabLayout.setupWithViewPager(viewPager);
    }

    public void getMeasurements(){
        measurements.clear();
        measurements.addAll(measurementDbHelper.getAllMeasurements());
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Add tabs for each parameter
        adapter.addFragment(new MoistureFragment(), "Moisture");
        //adapter.addFragment(new PhFragment(), "Ph");
        adapter.addFragment(new NutrientsFragment(), "Nutrients");
        adapter.addFragment(new SalinityFragment(), "Salinity");
        //adapter.addFragment(new MetalsFragment(measurements), "Metals");

        viewPager.setAdapter(adapter);
    }


    // ViewPager Adapter
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> titles = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}