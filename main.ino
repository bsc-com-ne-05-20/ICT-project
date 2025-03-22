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
