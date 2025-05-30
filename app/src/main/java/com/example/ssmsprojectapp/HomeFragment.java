package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ssmsprojectapp.databasehelpers.MeasurementDbHelper;
import com.example.ssmsprojectapp.datamodels.Farm;
import com.example.ssmsprojectapp.datamodels.FirestoreRepository;
import com.example.ssmsprojectapp.datamodels.Measurement;
import com.example.ssmsprojectapp.weather.ForecastResponse;
import com.example.ssmsprojectapp.weather.WeatherRepository;
import com.example.ssmsprojectapp.weather.WeatherResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class HomeFragment extends Fragment {

    private TextView recommenda;

    private static final String RENDER_URL = "https://zithekatu-6.onrender.com/ask";

    private String query = "Give 2 very short,precise top Crop recommendation in bullet form based on the following soil data preferably crops grown in malawi,please dont put the asterisks";
    private LinearLayout linearLayout;
   private MeasurementsAdapter farmMeasurementRAdapter;
   private RecyclerView recyclerView;

   private HomeFarmAdapter homeFarmAdapter;
   private TextView seeAll;
   private Button recommendations;

    private FirestoreRepository repository;
    private String currentFarmerId;

   private TextView farmerName;

   //logged in farmers name
   private String name;
   private  TextView farmName;

   //measurements values textviews
    private TextView salinity_val,moisture_val,ph_val,nitrogen_val,phosphorous_val,potassium_val;
    private String selectedFarmId,selectedFarmName;

    //the progress bar
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;

    //weather fields
    private static final String ICON_URL = "https://openweathermap.org/img/wn/%s@4x.png";

    private TextView tvCity, tvTemp, tvDescription, tvHumidity, tvWind;
    private ImageView ivWeatherIcon;
    private SwipeRefreshLayout swipeRefreshLayout;
    private WeatherRepository weatherRepository;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    private List<Measurement> currMeasurements = new ArrayList<>();

    private Button btnAnalytics;

    //database
    private MeasurementDbHelper measurementDb;


    //linearlayouts
    private LinearLayout linearLayoutTop,linearLayoutStatus,linearLayoutBottom;
    //passing of data
    private MeasurementsListDataListener measurementListDataListener;

    //the interface to handle data sharing to activities
    public interface MeasurementsListDataListener {
        void onListDataPassed(Farm farm);
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    public HomeFragment(FirestoreRepository repository, String currentFarmerId) {
        this.repository = repository;
        this.currentFarmerId = currentFarmerId;
    }


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_home, container, false);


        //init layouts
        linearLayoutTop = view.findViewById(R.id.linearLayout1);
        linearLayoutBottom = view.findViewById(R.id.linearLayout2);
        linearLayoutStatus = view.findViewById(R.id.no_measurement_found);


        //init database helper
        measurementDb = new MeasurementDbHelper(getContext());

        recommenda = view.findViewById(R.id.cRec);

        //init the layout
        linearLayout = view.findViewById(R.id.homepage_layout);
        linearLayout.setVisibility(View.INVISIBLE);

        //init weather views
        initWeatherViews(view);
        weatherRepository = new WeatherRepository();

        CardView wCard = view.findViewById(R.id.weather_card);
        wCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.container,new weatherFragment(currentLatitude,currentLongitude));
                transaction.commit();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (currentLatitude != 0.0 && currentLongitude != 0.0) {
                fetchWeatherData(currentLatitude, currentLongitude);
            } else {
                Toast.makeText(getContext(), "your farm's coordinates are invalid", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        //setting the current user name
        farmerName = view.findViewById(R.id.farmer_name);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();        // Farmer's email
            name = user.getDisplayName();   // Farmer's name
            String uid = user.getUid();// Unique user ID
            farmerName.setText("Hi " + name + ",");
        }
        TextView greeting = view.findViewById(R.id.greeting_text);
        greeting.setText(getMessage());


        //farm measurements recyclerView
        recyclerView = view.findViewById(R.id.recycler);
        farmMeasurementRAdapter = new MeasurementsAdapter(new ArrayList<>(),this::onMeasurementSelected);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(),LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(farmMeasurementRAdapter);

        //init the measurejents value text views
        //these will be updated when the measurements for a farm have been loaded

        salinity_val = view.findViewById(R.id.salinity_value);
        moisture_val = view.findViewById(R.id.moisture_value);
        ph_val = view.findViewById(R.id.ph_value);
        nitrogen_val = view.findViewById(R.id.nitrogen_value);
        phosphorous_val = view.findViewById(R.id.phosphorous_value);
        potassium_val = view.findViewById(R.id.potassium_value);



        //init recommendations button
        recommendations = view.findViewById(R.id.recommendations_button);
        recommendations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(view.getContext(), AIRecommendations.class));
            }
        });

        //init see all
        seeAll = view.findViewById(R.id.see_all_text);
        seeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(view.getContext(), SeeAllMeasurements.class));

                Intent intent = new Intent(view.getContext(), SeeAllMeasurements.class);
                intent.putParcelableArrayListExtra("ALL_MEASUREMENTS",new ArrayList<>(currMeasurements));
                intent.putExtra("FARM_NAME", selectedFarmName);
                startActivity(intent);
            }
        });


        //init farmer and farm name
        farmerName = view.findViewById(R.id.farmer_name);

        homeFarmAdapter = new HomeFarmAdapter(new ArrayList<>(), this::onFarmSelected);
        //load farms
        loadFarms(view);

        farmName= view.findViewById(R.id.farm_name);
        farmName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFarmsDialog(v);
            }
        });

        SharedPreferences prefs = requireContext().getSharedPreferences("TempStorage", Context.MODE_PRIVATE);
        Gson gson = new Gson();

        //init the analytics button
        btnAnalytics  = view.findViewById(R.id.btn_analytics);
        btnAnalytics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currMeasurements.isEmpty()){
                    if (!measurementDb.isDatabaseEmpty()){
                        measurementDb.clearAllMeasurements();
                    }

                    for (Measurement m : currMeasurements) {
                        measurementDb.insertMeasurement(m);
                    }
                    startActivity(new Intent(getActivity(), Graphs.class));
                }
                else {
                    Toast.makeText(getContext(), "Empty measurement List", Toast.LENGTH_SHORT).show();
                }
            }
        });




        return view;

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            measurementListDataListener = (MeasurementsListDataListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ListDataListener");
        }
    }

    //attach the measurement list to yhe fragment
    private void sendListToActivity(Farm farm) {
        if (measurementListDataListener != null) {
            measurementListDataListener.onListDataPassed(farm);
        }
    }

    private void openFarmsDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

        View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.home_farms_layout, null);
        builder.setView(dialogView);

        RecyclerView recyclerFarms = dialogView.findViewById(R.id.f_recycler);
        recyclerFarms.setLayoutManager(new LinearLayoutManager(v.getContext(),LinearLayoutManager.VERTICAL,false));
        recyclerFarms.setAdapter(homeFarmAdapter);

        loadFarms2(v);

        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismissAlertDialog();
            }
        });
        builder.setCancelable(true);

        // Create the AlertDialog object and show it
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void dismissAlertDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    private void onFarmSelected(Farm farm) {

        selectedFarmId = farm.getId();
        selectedFarmName = farm.getFarmName();

        currentLatitude = farm.getLatitude();
        currentLongitude = farm.getLongitude();

        sendListToActivity(farm);
        dismissAlertDialog();
        //update ui

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                farmName.setText(selectedFarmName);

            }
        });
        //fetch weather data
        fetchWeatherData(currentLatitude, currentLongitude);
        loadMeasurements(selectedFarmId);


    }

    public void loadFarms2(View view){

        repository.getFarmsByFarmer(currentFarmerId, task -> {

            //dismiss the dialog
            progressDialog.dismiss();

            if (task.isSuccessful()) {
                List<Farm> farms = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    farms.add(repository.snapshotToFarm(document));
                }

                homeFarmAdapter.updateFarmData(farms);

            } else {
                Toast.makeText(view.getContext(), "Failed to load farms: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadFarms(View view) {

        //show progress dialog
        showProgress(view,"Loading farms...");

        repository.getFarmsByFarmer(currentFarmerId, task -> {

            //dismiss the dialog
            progressDialog.dismiss();

            if (task.isSuccessful()) {
                List<Farm> farms = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    farms.add(repository.snapshotToFarm(document));
                }

                homeFarmAdapter.updateFarmData(farms);

                // Select first farm by default if available
                if (!farms.isEmpty()) {
                    linearLayout.setVisibility(View.VISIBLE);
                    onFarmSelected(farms.get(0));
                }
                else {
                    //show the no farms dialog view
                    linearLayout.setVisibility(View.INVISIBLE);
                }
            } else {
                Toast.makeText(view.getContext(), "Failed to load farms: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMeasurements(String farmId) {
        repository.getMeasurementsByFarm(farmId, task -> {
            if (task.isSuccessful()) {
                List<Measurement> measurements = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    measurements.add(repository.snapshotToMeasurement(document));
                }

                //change the recyclerView data here
                farmMeasurementRAdapter.updateData(measurements);
                //new HomePage(measurements,farmName);

                //getting the latest measurement
                if (!measurements.isEmpty()){
                    Measurement latestMeasurement = measurements.get(0);

                    currMeasurements = measurements;

                    // Update UI with measurements
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            salinity_val.setText(latestMeasurement.getSalinity() +"");
                            moisture_val.setText(latestMeasurement.getMoisture() +"");
                            ph_val.setText(latestMeasurement.getPh() +"");
                            nitrogen_val.setText(latestMeasurement.getNitrogen() +"");
                            phosphorous_val.setText(latestMeasurement.getPhosphorus() +"");
                            potassium_val.setText(latestMeasurement.getPotassium() +"");

                            sendQuestionToApi(query + latestMeasurement.getSalinity() + latestMeasurement.getMoisture() + latestMeasurement.getPh() + latestMeasurement.getNitrogen() +latestMeasurement.getPhosphorus() + latestMeasurement.getPotassium());

                        }
                    });

                }
                else { //add else to handle the case where the farm is just created and there is no measurements yet

                    linearLayoutTop.setVisibility(View.GONE);
                    linearLayoutBottom.setVisibility(View.GONE);
                    linearLayoutStatus.setVisibility(View.VISIBLE);

                }





            } else {
                Log.e("FarmerDashboard", "Error loading measurements", task.getException());
            }
        });
    }


    @SuppressLint("MissingInflatedId")
    public void onMeasurementSelected(View v,Measurement measurement){
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.measurement_info_dialog_layout, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        TextView date1 = dialogView.findViewById(R.id.date);
        TextView ph = dialogView.findViewById(R.id.tvph);
        TextView salinity = dialogView.findViewById(R.id.tvsalinity);
        TextView moisture = dialogView.findViewById(R.id.tvmoisture);
        TextView nitrogen = dialogView.findViewById(R.id.tvnitrogen);
        TextView phosphorous = dialogView.findViewById(R.id.tvphosphorous);
        TextView potassium = dialogView.findViewById(R.id.tvpotassium);

        date1.setText(measurement.getTimestamp().toLocaleString());
        ph.setText(measurement.getPh()+"");
        salinity.setText(measurement.getSalinity()+" dS/m");
        moisture.setText(measurement.getMoisture()+" %");
        nitrogen.setText(measurement.getNitrogen()+" ppm");
        phosphorous.setText(measurement.getPh()+" ppm");
        potassium.setText(measurement.getPotassium()+" ppm");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss the dialog
            }
        });
        builder.show();
    }

    //the m
    private String getMessage() {
        String message = "good day";
        Calendar calendar = Calendar.getInstance();
        int time = calendar.get(Calendar.HOUR_OF_DAY);

        if (time >= 0 && time < 12){
            message = "Good morning";
        }
        else if (time >= 12 && time < 16){
            message = "Good afternoon";
        }
        else if (time >= 16 && time < 20){
            message = "Good evening";
        }
        else {
            message = "Good night";
        }

        return message;
    }

    private void showProgress(View view,String message){
        progressDialog = new ProgressDialog(view.getContext());
        progressDialog.setMessage(message); // Set message
        progressDialog.setTitle("Please wait"); // Set title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // or STYLE_HORIZONTAL
        progressDialog.setCancelable(false); // Optional - prevent dismissing by tapping outside
        progressDialog.show();
    }
    public void dismissProgress(){
        progressDialog.dismiss();
    }

    

    //weather methods

    private void initWeatherViews(View view) {
        tvCity = view.findViewById(R.id.tvCity);
        tvTemp = view.findViewById(R.id.tvTemp);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvHumidity = view.findViewById(R.id.tv_humidity);
        tvWind = view.findViewById(R.id.tv_wind);
        ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon);
        swipeRefreshLayout = view.findViewById(R.id.swiperefreshlayout);

    }

    private void fetchWeatherData(double lat, double lon) {
        swipeRefreshLayout.setRefreshing(true);
        weatherRepository.getCurrentWeather(lat, lon, new WeatherRepository.WeatherCallback() {
            @Override
            public void onSuccess(WeatherResponse response) {
                requireActivity().runOnUiThread(() -> {
                    updateUI(response);
                    fetchForecastData(lat, lon);
                });
            }

            @Override
            public void onFailure(String message) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        });
    }


    private void fetchForecastData(double lat, double lon) {
        weatherRepository.getWeatherForecast(lat, lon, new WeatherRepository.ForecastCallback() {
            @Override
            public void onSuccess(ForecastResponse response) {
                requireActivity().runOnUiThread(() -> {
                    // Here you can update forecast UI if needed
                    swipeRefreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onFailure(String message) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Forecast: " + message, Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        });
    }

    private void updateUI(WeatherResponse weather) {
        if (weather.getCityName() != null && weather.getSys() != null) {
            tvCity.setText(String.format("%s, %s", weather.getCityName(), weather.getSys().getCountry()));
        } else {
            tvCity.setText("Unknown Location");
        }

        if (weather.getMain() != null) {
            tvTemp.setText(String.format(Locale.getDefault(), "%.1f°C", weather.getMain().getTemp()));
            tvHumidity.setText(String.format(Locale.getDefault(), "Humidity: %d%%", weather.getMain().getHumidity()));
        }

        if (weather.getWeather() != null && weather.getWeather().length > 0) {
            tvDescription.setText(weather.getWeather()[0].getDescription());
            String iconUrl = String.format(ICON_URL, weather.getWeather()[0].getIcon());
            Glide.with(this).load(iconUrl).into(ivWeatherIcon);
        }

        if (weather.getWind() != null) {
            tvWind.setText(String.format(Locale.getDefault(), "Wind: %.1f m/s", weather.getWind().getSpeed()));
        }

    }


    private void sendQuestionToApi(String question) {
        new Thread(() -> {
            try {
                URL url = new URL(RENDER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                JSONObject jsonInput = new JSONObject();
                jsonInput.put("question", question);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInput.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String reply = jsonResponse.getString("response");


                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //set recommendation
                        recommenda.setText(reply);
                    }
                });

            } catch (Exception e) {
                //runOnUiThread(() -> responseText.setText("Error: " + e.getMessage()));
            }
        }).start();
    }
}