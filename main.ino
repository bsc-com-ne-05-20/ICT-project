#include <HardwareSerial.h>
#include <BluetoothSerial.h>

//pin definitions
#define MODEM_PWRKEY 4
#define RS485_RE 26
#define RS485_RX 27
#define RS485_TX 14

BluetoothSerial SerialBT;
HardwareSerial RS485Serial(2);  

// Modbus commands
const byte cmdMoisture[]    = {0x01, 0x03, 0x00, 0x00, 0x00, 0x01, 0x84, 0x0A};
const byte cmdTemperature[] = {0x01, 0x03, 0x00, 0x01, 0x00, 0x01, 0xD5, 0xCA};
const byte cmdEC[]          = {0x01, 0x03, 0x00, 0x02, 0x00, 0x01, 0x25, 0xCA};
const byte cmdPH[]          = {0x01, 0x03, 0x00, 0x03, 0x00, 0x01, 0x74, 0x0A};
const byte cmdN[]           = {0x01, 0x03, 0x00, 0x04, 0x00, 0x01, 0xC5, 0xCB};
const byte cmdP[]           = {0x01, 0x03, 0x00, 0x05, 0x00, 0x01, 0x94, 0x0B};
const byte cmdK[]           = {0x01, 0x03, 0x00, 0x06, 0x00, 0x01, 0x64, 0x0B};

void setup() {
  Serial.begin(115200);
  RS485Serial.begin(9600, SERIAL_8N1, RS485_RX, RS485_TX);
  SerialBT.begin("SoilMonitor-T7000");
  
  pinMode(RS485_RE, OUTPUT);
  digitalWrite(RS485_RE, LOW);

pinMode(MODEM_PWRKEY, OUTPUT);
  digitalWrite(MODEM_PWRKEY, HIGH);
  delay(1000);
  digitalWrite(MODEM_PWRKEY, LOW);

  Serial.println("Soil Monitoring System Initialized");
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

void loop() {
  // Read sensor values with error checking
  float moisture = readSensor(cmdMoisture) / 10.0;
  float temperature = readSensor(cmdTemperature) / 10.0;
  float ec = readSensor(cmdEC);
  float ph = readSensor(cmdPH) / 10.0;
  int nitrogen = readSensor(cmdN);
  int phosphorus = readSensor(cmdP);
  int potassium = readSensor(cmdK);

  // Validate readings before transmission
  if (moisture == -9999 || temperature == -9999 || ec == -9999 || 
      ph == -9999 || nitrogen == -9999 || phosphorus == -9999 || potassium == -9999) {
    Serial.println("Error: Failed to read one or more sensors");
    return;
  }

  // Build JSON payload
  String jsonData = "{";
  jsonData += "\"moisture\":" + String(moisture, 1) + ",";
  jsonData += "\"temperature\":" + String(temperature, 1) + ",";
  jsonData += "\"ec\":" + String(ec, 1) + ",";
  jsonData += "\"ph\":" + String(ph, 1) + ",";
  jsonData += "\"nitrogen\":" + String(nitrogen) + ",";
  jsonData += "\"phosphorus\":" + String(phosphorus) + ",";
  jsonData += "\"potassium\":" + String(potassium);
  jsonData += "}";

