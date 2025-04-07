package com.example.ssmsprojectapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class HomeStatsFragment extends Fragment {

    private FragmentContainerView container;


    public HomeStatsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view =  inflater.inflate(R.layout.fragment_home_stats, container, false);

       container = view.findViewById(R.id.stats_container);
       return view;
    }
}