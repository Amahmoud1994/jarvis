#include <stdio.h>


char buff[100]; // we're going to use this to hold our string
String voice;
int lightLED = 2, //Connect LED 1 To Pin #2 
smokeSensor = A5,
tempSensor = 3;


void setup() {
    Serial.begin(9600);
    pinMode(lightLED, OUTPUT); 
    pinMode(smokeSensor, INPUT); 
    pinMode(tempSensor, INPUT);
}

void loop() {
  

  while (Serial.available()){  //Check if there is an available byte to read
    delay(10); //Delay added to make thing stable 
    char c = Serial.read(); //Conduct a serial read
    if (c == '#') {break;} //Exit the loop when the # is detected after the word
    voice += c; //Shorthand for voice = voice + c
  }  

  if (voice.length() > 0) {

    Serial.println(voice); 
//-----------------------------------------------------------------------//    

  //----------Get the Gas data to mobile----------// 
  if(voice == "*Send smoke ratio") {
    int analogSensor = analogRead(smokeSensor);
    sprintf(buff, "The Gas ratio is %d", analogSensor);  // and finally the integer
    Serial.write(buff);
    Serial.write("\n");
    delay(1000);
  }
  //--------------------------------------------// 
  
  //----------Turn On One-By-One----------// 
  else if(voice == "*Lights on") {digitalWrite(lightLED, HIGH);}
  else if(voice == "*Lights off") {digitalWrite(lightLED, LOW);}
  //--------------------------------------//
  
  //----------Get the temperature data to mobile----------// 
  else if(voice == "*Send temperature ratio") {
    int analogSensor = digitalRead(tempSensor);
    sprintf(buff, "The Temperature ratio is %d", analogSensor);  // and finally the integer
    Serial.write(buff);
    Serial.write("\n");
    delay(1000);  
  }
  //------------------------------------------------------// 
  
  voice="";
    
  }
  
}
