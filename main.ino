#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <ModbusMaster.h>
#include <TinyGPS++.h>
#include <HardwareSerial.h>

// BLE Definitions
BLEServer* pServer;
BLEService* pService;
BLECharacteristic* pDataCharacteristic;

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define DATA_UUID           "beb5483e-36e1-4688-b7f5-ea07361b26a8"

// BLE Connection Tracking
bool deviceConnected = false;
class ServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) { deviceConnected = true; }
    void onDisconnect(BLEServer* pServer) { deviceConnected = false; }
};

// RS485 Modbus Configuration
ModbusMaster node;
#define MAX485_DE_RE 23     
#define RS485_BAUD 9600

// GPS Configuration
HardwareSerial gpsSerial(1); // UART1 for SIM7000G
TinyGPSPlus gps;
#define GPS_BAUD 115200

// Sensor Pins (Analog)
#define MOISTURE_PIN 34
#define SALINITY_PIN 35

void powerOnGPS() {
  gpsSerial.println("AT+CGNSPWR=1");  // Power on GPS
  delay(200);
  gpsSerial.println("AT+CGNSURC=2");  // Set update rate to 1Hz
  delay(200);
}

String prepareJSON(float ph, float temp, float n, float p, float k, 
  float moisture, float salinity) {
DynamicJsonDocument doc(512);

doc["ph"] = ph;
doc["temperature"] = temp;
doc["nitrogen"] = n;
doc["phosphorus"] = p;
doc["potassium"] = k;
doc["moisture"] = moisture;
doc["salinity"] = salinity;

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

void readModbusSensor(float *ph, float *temp, float *npk) {
  digitalWrite(MAX485_DE_RE, HIGH);
  delay(10);

  // Read pH (register 0x0000)
  uint8_t result = node.readInputRegisters(0x0000, 1);
  if (result == node.ku8MBSuccess) {
    *ph = node.getResponseBuffer(0) / 10.0;
  }

  // Read Temperature (register 0x0001)
  result = node.readInputRegisters(0x0001, 1);
  if (result == node.ku8MBSuccess) {
    *temp = node.getResponseBuffer(0) / 10.0;
  }

  // Read NPK (registers 0x0002-0x0004)
  result = node.readInputRegisters(0x0002, 3);
  if (result == node.ku8MBSuccess) {
    npk[0] = node.getResponseBuffer(0);  // N
    npk[1] = node.getResponseBuffer(1);  // P
    npk[2] = node.getResponseBuffer(2);  // K
  }

  digitalWrite(MAX485_DE_RE, LOW);
  delay(10);
}
