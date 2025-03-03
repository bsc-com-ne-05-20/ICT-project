#include <WiFi.h>
#include <HTTPClient.h>

// Wi-Fi credentials
const char* ssid = "";       
const char* password = "";

// ThingSpeak API Key
const char* server = "api.thingspeak.com";
String apiKey = "";

// Pin definitions
#define SALINITY_SENSOR_PIN 33    // GPIO33 (Analog Input)
#define PH_SENSOR_PIN 32          // GPIO32 (Analog Input)
#define LEAD_SENSOR_PIN 35        // GPIO35 (Analog Input)
#define MERCURY_SENSOR_PIN 34     // GPIO34 (Analog Input)

// Calibration values 
#define SALINITY_CALIBRATION 0.5  
#define PH_CALIBRATION 3.0        
#define LEAD_CALIBRATION 0.05     
#define MERCURY_CALIBRATION 0.02 

void setup() {
    Serial.begin(115200);
   // Serial.println("\nInitializing system...");
  
    // Initialize WiFi
    WiFi.begin(ssid, password);
    Serial.print("Connecting to WiFi");
    int wifiTimeout = 0;
    while (WiFi.status() != WL_CONNECTED && wifiTimeout < 20) { // 20-second timeout
      delay(500);
      Serial.print(".");
      wifiTimeout++;
    }