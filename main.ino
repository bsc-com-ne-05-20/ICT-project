#include <HardwareSerial.h>
#include <BluetoothSerial.h>
#include <TinyGPS++.h>
#include <ArduinoJson.h>

//pin definitions
#define MODEM_PWRKEY 4
#define RS485_RE 26
#define RS485_RX 27
#define RS485_TX 14

// Bluetooth Serial Object
BluetoothSerial SerialBT;

// Serial Ports
HardwareSerial RS485Serial(2);  // For soil sensors
HardwareSerial gpsSerial(1);    // UART1 for SIM7000G
TinyGPSPlus gps;

// Modbus commands
const byte cmdMoisture[]    = {0x01, 0x03, 0x00, 0x00, 0x00, 0x01, 0x84, 0x0A};
const byte cmdTemperature[] = {0x01, 0x03, 0x00, 0x01, 0x00, 0x01, 0xD5, 0xCA};
const byte cmdEC[]          = {0x01, 0x03, 0x00, 0x02, 0x00, 0x01, 0x25, 0xCA};
const byte cmdPH[]          = {0x01, 0x03, 0x00, 0x03, 0x00, 0x01, 0x74, 0x0A};
const byte cmdN[]           = {0x01, 0x03, 0x00, 0x04, 0x00, 0x01, 0xC5, 0xCB};
const byte cmdP[]           = {0x01, 0x03, 0x00, 0x05, 0x00, 0x01, 0x94, 0x0B};
const byte cmdK[]           = {0x01, 0x03, 0x00, 0x06, 0x00, 0x01, 0x64, 0x0B};

void powerOnGPS() {
  gpsSerial.println("AT+CGNSPWR=1");  // Power on GPS
  delay(200);
  gpsSerial.println("AT+CGNSURC=2");  // Set update rate to 1Hz
  delay(200);
  Serial.println("GPS Powered On");
}

int readSensor(const byte* command) {
  byte response[7];
  int attempt = 0;
  bool validResponse = false;

  while (attempt < 3 && !validResponse) {
    digitalWrite(RS485_RE, HIGH);
    delay(10);
    RS485Serial.write(command, 8);
    RS485Serial.flush();
    digitalWrite(RS485_RE, LOW);
    delay(100);

    if (RS485Serial.available() >= 7) {
      for (int i = 0; i < 7; i++) response[i] = RS485Serial.read();
      if ((response[5] == 0x00) && (response[6] == 0x00)) { // CRC check
        validResponse = true;
        return (response[3] << 8) | response[4];
      }
    }
    attempt++;
    delay(50);
  }
  return -9999; // Error value
}

String prepareJSON(float moisture, float temperature, float ec, float ph, 
                  int nitrogen, int phosphorus, int potassium) {
  DynamicJsonDocument doc(512);

  // Soil parameters
  doc["moisture"] = moisture;
  doc["temperature"] = temperature;
  doc["ec"] = ec;
  doc["ph"] = ph;
  doc["nitrogen"] = nitrogen;
  doc["phosphorus"] = phosphorus;
  doc["potassium"] = potassium;

  // GPS data
  if (gps.location.isValid()) {
    doc["latitude"] = String(gps.location.lat(), 6);
    doc["longitude"] = String(gps.location.lng(), 6);
  } else {
    doc["location"] = "unavailable";
  }

  String output;
  serializeJson(doc, output);
  return output;
}

void readGPSData() {
  while (gpsSerial.available() > 0) {
    gps.encode(gpsSerial.read());
  }
  
  if (gps.location.isValid()) {
    Serial.print("GPS Location - Lat: ");
    Serial.print(gps.location.lat(), 6);
    Serial.print(", Lng: ");
    Serial.println(gps.location.lng(), 6);
  } else {
    Serial.println("GPS location not available");
  }
  
  if (gps.time.isValid()) {
    Serial.print("GPS Time - ");
    Serial.print(gps.time.hour());
    Serial.print(":");
    Serial.print(gps.time.minute());
    Serial.print(":");
    Serial.println(gps.time.second());
  }
}

void printSensorReadings(float moisture, float temperature, float ec, float ph,
                        int nitrogen, int phosphorus, int potassium) {
  String readings = "Sensor Readings:\n";
  readings += "Moisture: " + String(moisture) + "%\n";
  readings += "Temperature: " + String(temperature) + "°C\n";
  readings += "EC: " + String(ec) + "µS/cm\n";
  readings += "pH: " + String(ph) + "\n";
  readings += "NPK - N:" + String(nitrogen) + " P:" + String(phosphorus) + " K:" + String(potassium);
  
  Serial.println(readings);
  if(SerialBT.connected()) {
    SerialBT.println(readings);
  }
}

void setup() {
  Serial.begin(115200);
  Serial.println("Initializing Soil Monitoring System...");

  // Initialize Bluetooth
  SerialBT.begin("SoilMonitor-T7000"); // Bluetooth device name
  Serial.println("Bluetooth device ready for pairing!");

  // Initialize RS485 for soil sensors
  RS485Serial.begin(9600, SERIAL_8N1, RS485_RX, RS485_TX);
  pinMode(RS485_RE, OUTPUT);
  digitalWrite(RS485_RE, LOW);

  // Initialize GPS
  gpsSerial.begin(115200, SERIAL_8N1, 18, 19);
  powerOnGPS();

  // Initialize modem power
  pinMode(MODEM_PWRKEY, OUTPUT);
  digitalWrite(MODEM_PWRKEY, HIGH);
  delay(1000);
  digitalWrite(MODEM_PWRKEY, LOW);
}

void loop() {
  static uint32_t lastSend = 0;
  
  if (millis() - lastSend >= 10000) {
    lastSend = millis();
    
    Serial.println("\n--- Collecting Sensor Data ---");
    if(SerialBT.connected()) {
      SerialBT.println("\n--- Collecting Sensor Data ---");
    }
    
    float moisture = readSensor(cmdMoisture) / 10.0;
    float temperature = readSensor(cmdTemperature) / 10.0;
    float ec = readSensor(cmdEC);
    float ph = readSensor(cmdPH) / 10.0;
    int nitrogen = readSensor(cmdN);
    int phosphorus = readSensor(cmdP);
    int potassium = readSensor(cmdK);

    readGPSData();
    printSensorReadings(moisture, temperature, ec, ph, nitrogen, phosphorus, potassium);

    String jsonData = prepareJSON(moisture, temperature, ec, ph, 
                                nitrogen, phosphorus, potassium);
    
    Serial.println("Prepared JSON Data:");
    Serial.println(jsonData);
    
    bool readingsValid = (moisture != -9999 && temperature != -9999 && 
                         ec != -9999 && ph != -9999 && 
                         nitrogen != -9999 && phosphorus != -9999 && 
                         potassium != -9999);
    
    if (SerialBT.connected() && readingsValid) {
      SerialBT.println(jsonData);
      Serial.println("Data sent via Bluetooth");
    } else if (!readingsValid) {
      Serial.println("Sensor read failed - data not sent");
      if(SerialBT.connected()) {
        SerialBT.println("Sensor read failed - data not sent");
      }
    } else {
      Serial.println("No Bluetooth connection - data not sent");
    }
    
    Serial.println("--- End of Cycle ---");
    if(SerialBT.connected()) {
      SerialBT.println("--- End of Cycle ---");
    }
  }
}


