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


