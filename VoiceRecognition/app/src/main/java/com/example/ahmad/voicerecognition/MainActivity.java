package com.example.ahmad.voicerecognition;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothSocket;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import java.io.IOException;
import java.util.UUID;

import static android.R.id.progress;

public class MainActivity extends Activity {

    private TextView txtSpeechInput;

    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    String address = null;
    BluetoothAdapter myBluetooth = null;
    private ProgressDialog progress;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean inTempratureQuestion2 =false;
    private boolean inSmokeQuestion2 =false;
    private boolean inLightOnQuestion2 =false;
    private boolean inLightOffQuestion2 =false;

    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent newint = getIntent();
        address = newint.getStringExtra("EXTRA_ADDRESS");
        new ConnectBT().execute();
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });
        // hide the action bar
       // getActionBar().hide();

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void ClearState()
    {
        inTempratureQuestion2 =false;
        inSmokeQuestion2 =false;
        inLightOnQuestion2 =false;
        inLightOffQuestion2 =false;
    }
    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));

                    if(result.get(0).equals("temperature")){
                        ClearState();
                        sendCommand("send temperature ratio");

                        textToSpeech.speak("would you like to see the smoke ratio too?", TextToSpeech.QUEUE_FLUSH, null);
                        inTempratureQuestion2=true;
                    }
                    else if(result.get(0).equals("yes") && inTempratureQuestion2) {
                        sendCommand("send smoke ratio");
                        ClearState();

                    }
                    else if(result.get(0).equals("no") && inTempratureQuestion2) {
                        ClearState();
                    }
                    else if(result.get(0).equals("smoke")){
                        sendCommand("send smoke ratio");
                        textToSpeech.speak("would you like to see the temprature ratio too?", TextToSpeech.QUEUE_FLUSH, null);
                        inSmokeQuestion2=true;
                    }
                    else if(result.get(0).equals("yes") && inSmokeQuestion2) {
                        sendCommand("send temperature ratio");
                        ClearState();
                    }
                    else if(result.get(0).equals("no") && inSmokeQuestion2) {
                        ClearState();
                    }
                    else if (result.get(0).contains("light") || inLightOnQuestion2 )
                    {
                        if ((result.get(0).contains("on") || inLightOnQuestion2) && !result.get(0).contains("off"))
                        {
                            if (result.get(0).contains("bathroom") )
                            {
                                sendCommand("bathroom on");
                                ClearState();
                            }
                            else if (result.get(0).contains("bedroom"))
                            {
                                sendCommand("bedroom on");
                                ClearState();
                            }
                            else if (result.get(0).contains("all"))
                            {
                                sendCommand("all lights on");
                                ClearState();
                            }
                            else {
                                textToSpeech.speak("which room?", TextToSpeech.QUEUE_FLUSH, null);
                                inLightOnQuestion2 =true;
                            }
                        }
                        else if ((result.get(0).contains("off")|| inLightOffQuestion2) && !result.get(0).contains("on"))
                        {
                            if (result.get(0).contains("bathroom") )
                            {
                                sendCommand("bathroom off");
                                ClearState();
                            }
                            else if (result.get(0).contains("bedroom"))
                            {
                                sendCommand("bedroom off");
                                ClearState();
                            }
                            else if (result.get(0).contains("all"))
                            {
                                sendCommand("all lights off");
                                ClearState();
                            }
                            else {
                                textToSpeech.speak("which room?", TextToSpeech.QUEUE_FLUSH, null);
                                inLightOffQuestion2 =true;
                            }
                        }
                    }
                }
                break;
            }

        }
    }



    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }


    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout
    }

    private void sendCommand(String command)
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write(command.getBytes());
                recieveReading();
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }

    }

    private void recieveReading()
    {
        Log.e("test","here");


            try
            {
                byte[] buffer = new byte[256];  // buffer store for the stream
                int bytes;
                bytes = btSocket.getInputStream().read(buffer);
                //String res = btSocket.getInputStream().;
                String res = new String(buffer, 0, bytes);
                Log.e("test",res);
                txtSpeechInput.setText(res);
                textToSpeech.speak(res, TextToSpeech.QUEUE_FLUSH, null);
                buffer = new byte[256];
                bytes = 0;
            }
            catch (IOException e)
            {
                msg("Error");
            }

    }


}