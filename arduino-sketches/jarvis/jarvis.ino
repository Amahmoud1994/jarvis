#include <stdio.h>


char buff[100]; // we're going to use this to hold our string
String voice;
int bedroomLED = 2,  //Connect bedroom LED 1 To Pin #2
bathroomLED = 4,    //Connect bedroom LED 1 To Pin #4
smokeSensor = A5,
tempSensor = 3;


void setup() {
    Serial.begin(9600);
    pinMode(bedroomLED, OUTPUT); 
    pinMode(bathroomLED, OUTPUT); 
    pinMode(smokeSensor, INPUT); 
    pinMode(tempSensor, INPUT);
}

void loop() {
  

  while (Serial.available())
  {  
    //Check if there is an available byte to read
    voice = Serial.readString();
    Serial.println(voice);

  }  
  
  if (voice.length() > 0) 
  {
    
  //-----------------------------------------------------------------------//    

  //----------Get the Gas data to mobile----------// 
    if(voice == "send smoke ratio") {

      int analogSensor = analogRead(smokeSensor);
      sprintf(buff, "%d", analogSensor);  // and finally the integer
      Serial.write(buff);
      Serial.write("\n");
      delay(1000);
    }
  //--------------------------------------------// 
  
  //----------Turn On Bedroom OR Bathroom Lights----------// 
    else if(voice == "bedroom on") {digitalWrite(bedroomLED, HIGH);}
    else if(voice == "bedroom off") {digitalWrite(bedroomLED, LOW);}
    else if(voice == "bathroom on") {digitalWrite(bathroomLED, HIGH);}
    else if(voice == "bathroom off") {digitalWrite(bathroomLED, LOW);}
  //--------------------------------------//
  
    else if(voice == "all lights on") 
    {
      digitalWrite(bedroomLED, HIGH);
      digitalWrite(bathroomLED, HIGH);
    }
    else if(voice == "all lights off") 
    {
      digitalWrite(bedroomLED, LOW);
      digitalWrite(bathroomLED, LOW);
    }
   
  //----------Get the temperature data to mobile----------// 
    else if(voice == "send temperature ratio") {
      int analogSensor = digitalRead(tempSensor);
      sprintf(buff, "%d", analogSensor);  // and finally the integer
      Serial.write(buff);
      Serial.write("\n");
      delay(1000);  
    }
  //------------------------------------------------------// 
  
    voice="";
    
  }
  
}
