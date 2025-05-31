package com.example.soilhealthy;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class LocalDataActivity extends AppCompatActivity {
    private UnifiedDatabaseManager dbManager;
    private SoilDataAdapter adapter;
    private ListView listView;
    private EditText etSearchId;
    private RadioGroup rgDataFilter;
    private Button btnSearch, btnExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_data);

        // Initialize views
        dbManager = new UnifiedDatabaseManager(this);
        listView = findViewById(R.id.listView);
        etSearchId = findViewById(R.id.etSearchId);
        rgDataFilter = findViewById(R.id.rgDataFilter);
        btnSearch = findViewById(R.id.btnSearch);
        btnExport = findViewById(R.id.btnExport);

        // Load all data by default
        loadAllData();

        // Set up listeners
        rgDataFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbAll) {
                loadAllData();
            } else if (checkedId == R.id.rbAverages) {
                loadAverages();
            }
        });

        btnSearch.setOnClickListener(v -> {
            String searchId = etSearchId.getText().toString().trim();
            if (!searchId.isEmpty()) {
                searchData(searchId);
            } else {
                Toast.makeText(this, "Please enter search ID", Toast.LENGTH_SHORT).show();
            }
        });

        btnExport.setOnClickListener(v -> exportData());
    }

    private void loadAllData() {
        List<SoilData> dataList = dbManager.getAllSoilData();
        updateAdapter(dataList, "No data found in database");
    }

    private void loadAverages() {
        List<SoilData> averages = dbManager.getAverageRecords();
        updateAdapter(averages, "No average records found");
    }

    private void searchData(String personId) {
        List<SoilData> dataList = dbManager.getDataByPersonId(personId);
        updateAdapter(dataList, "No data found for ID: " + personId);
    }

    private void updateAdapter(List<SoilData> dataList, String emptyMessage) {
        if (dataList.isEmpty()) {
            Toast.makeText(this, emptyMessage, Toast.LENGTH_SHORT).show();
        }
        adapter = new SoilDataAdapter(this, dataList, dbManager);
        listView.setAdapter(adapter);
    }

    private void exportData() {
        List<SoilData> dataToExport;
        if (rgDataFilter.getCheckedRadioButtonId() == R.id.rbAverages) {
            dataToExport = dbManager.getAverageRecords();
        } else {
            String searchId = etSearchId.getText().toString().trim();
            dataToExport = searchId.isEmpty() ?
                    dbManager.getAllSoilData() :
                    dbManager.getDataByPersonId(searchId);
        }

        new DataExporter(this).exportData(dataToExport);
    }

    @Override
    protected void onDestroy() {
        dbManager.close();
        super.onDestroy();
    }
}