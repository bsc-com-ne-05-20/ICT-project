#include <WiFi.h>
#include <HTTPClient.h>

// Wi-Fi credentials
const char* ssid = "";       
const char* password = "YOUR_WIFI_PASSWORD";

// ThingSpeak API Key
const char* server = "api.thingspeak.com";
String apiKey = "YOUR_THINGSPEAK_API_KEY";

// Pin definitions
#define SALINITY_SENSOR_PIN 33    // GPIO33 (Analog Input)
#define PH_SENSOR_PIN 32          // GPIO32 (Analog Input)
#define LEAD_SENSOR_PIN 35        // GPIO35 (Analog Input)
#define MERCURY_SENSOR_PIN 34     // GPIO34 (Analog Input)