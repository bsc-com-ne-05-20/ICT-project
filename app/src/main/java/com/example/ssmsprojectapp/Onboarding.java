package com.example.ssmsprojectapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;

public class Onboarding extends AppCompatActivity {


    private ViewPager viewPager;
    private LinearLayout dotLayout;

    private Button back, next ,skip;

    private TextView[] dots;

    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        //init components

        back = findViewById(R.id.back_button);
        next = findViewById(R.id.next_button);
        skip = findViewById(R.id.skip_button);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getitem(0) > 0){

                    viewPager.setCurrentItem(getitem(-1),true);

                }

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (getitem(0) < 3)
                    viewPager.setCurrentItem(getitem(1),true);
                else {

                    Intent i = new Intent(Onboarding.this,Login2.class);
                    startActivity(i);
                    finish();

                }

            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //go straght to the log in page
                Intent i = new Intent(Onboarding.this,Login2.class);
                startActivity(i);
                finish();
            }
        });

        dotLayout = findViewById(R.id.dot_layout);
        viewPager = findViewById(R.id.slide_viewpager);

        //create the adapter instance
        adapter = new ViewPagerAdapter(this);

        viewPager.setAdapter(adapter);
    }


    public void setUpindicator(int position){

        dots = new TextView[4];
        dotLayout.removeAllViews();

        for (int i = 0 ; i < dots.length ; i++){

            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.main_color_sub2,getApplicationContext().getTheme()));
            dotLayout.addView(dots[i]);

        }

        dots[position].setTextColor(getResources().getColor(R.color.main_color,getApplicationContext().getTheme()));

    }


    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            setUpindicator(position);

            if (position > 0){

                back.setVisibility(View.VISIBLE);

            }else {

                back.setVisibility(View.INVISIBLE);

            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };


    private int getitem(int i){

        return viewPager.getCurrentItem() + i;
    }
}