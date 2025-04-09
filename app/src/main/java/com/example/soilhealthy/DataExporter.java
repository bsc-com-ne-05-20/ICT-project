package com.example.soilhealthy;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataExporter {
    private final Context context;

    public DataExporter(Context context) {
        this.context = context;
    }

    public void exportData(List<SoilData> dataList) {
        if (dataList.isEmpty()) {
            Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File exportDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "SoilHealthData");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            File file = new File(exportDir, "soil_data_" + timestamp + ".csv");

            FileWriter writer = new FileWriter(file);

            // Write CSV header
            writer.write("Timestamp,Location,Temperature,Salinity,pH,Moisture,Nitrogen,Phosphorus,Potassium\n");

            // Write data
            for (SoilData data : dataList) {
                writer.write(String.format(Locale.US, "\"%s\",\"%s\",%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
                        data.getTimestamp(),
                        data.getPersonId(),
                        data.getTemperature(),
                        data.getSalinity(),
                        data.getPh(),
                        data.getMoisture(),
                        data.getNitrogen(),
                        data.getPhosphorus(),
                        data.getPotassium()));
            }

            writer.close();
            Toast.makeText(context, "Exported to " + file.getPath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(context, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
