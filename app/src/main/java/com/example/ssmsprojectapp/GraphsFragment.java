package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ssmsprojectapp.datamodels.Measurement;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;


public class GraphsFragment extends Fragment {

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private List<Measurement> measurements;

    public GraphsFragment() {
        // Required empty public constructor
    }

    public GraphsFragment(List<Measurement> measurements){
        this.measurements = measurements;
    }


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_graphs, container, false);

        //get the measurements from the home activity
        //measurements = ((HomePage)requireActivity()).getMeasurements();


        // Setup ViewPager and TabLayout
        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);

        setupViewPager();
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

    private void setupViewPager() {
        GraphsFragment.ViewPagerAdapter adapter = new ViewPagerAdapter(getParentFragmentManager());

        // Add tabs for each parameter
        adapter.addFragment(new MoistureFragment(measurements), "Moisture");
        adapter.addFragment(new TemperatureFragment(measurements), "Temperature");
        adapter.addFragment(new NutrientsFragment(measurements), "Nutrients");
        adapter.addFragment(new SalinityFragment(measurements), "Salinity");
        adapter.addFragment(new MetalsFragment(measurements), "Metals");

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