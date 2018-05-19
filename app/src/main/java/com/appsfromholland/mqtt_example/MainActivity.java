package com.appsfromholland.mqtt_example;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Random;

public class MainActivity extends AppCompatActivity  {

    private MqttAndroidClient client;
    private PahoMqttClient pahoMqttClient;

    private MyBroadcastReceiver myBroadCastReceiver;
    static final String BROADCAST_ACTION = "com.appsfromholland.mqttpayloadavailabe";


    Button submitBtn, subscribeBtn, unsubscribeBtn;
    TextView tv;
    ImageView colorBlock;
    ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pahoMqttClient = new PahoMqttClient();

        client = pahoMqttClient.getMqttClient(
                getApplicationContext(),
                MQTTConfig.getInstance().MQTT_BROKER_URL(),
                MQTTConfig.getInstance().CLIENT_ID());

        tv = findViewById(R.id.mqttTextId);

        layout = findViewById(R.id.mainLayoutId);


        submitBtn = findViewById(R.id.publish);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = tv.getText().toString().trim();
                if( !msg.isEmpty()) {
                    try {
                        pahoMqttClient.publishMessage(client, msg, 0, MQTTConfig.getInstance().PUBLISH_TOPIC());
                    } catch (MqttException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Unsubscribe to TOPIC
        subscribeBtn = findViewById(R.id.subscribe);
        subscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    pahoMqttClient.subscribe(client, MQTTConfig.getInstance().PUBLISH_TOPIC(), 0);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        // Unsubscribe to topic
        unsubscribeBtn = findViewById(R.id.unsubscribe);
        unsubscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    pahoMqttClient.unSubscribe(client, MQTTConfig.getInstance().PUBLISH_TOPIC());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        // Setup Broadcast receiver
        myBroadCastReceiver = new MyBroadcastReceiver();

        // Start Broadcast receiver
        try
        {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BROADCAST_ACTION);
            registerReceiver(myBroadCastReceiver, intentFilter);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        // Start services
        try {
            Intent intent = new Intent(MainActivity.this, MqttMessageService.class);
            startService(intent);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Defineer een eigen broadcast receiver, deze vangt alles op voor
    public class MyBroadcastReceiver extends BroadcastReceiver {

        private final String TAG = "MyBroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            try
            {
                String payload = intent.getStringExtra("payload");
                Log.i(TAG,  payload);

                try {
                    JSONObject jsonObject = new JSONObject(payload);
                    int red = jsonObject.getJSONObject("ledColor").getInt("r");
                    int green = jsonObject.getJSONObject("ledColor").getInt("g");
                    int blue = jsonObject.getJSONObject("ledColor").getInt("b");

                    layout.setBackgroundColor( Color.rgb(red, green, blue) );

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    /**
     * This method called when this Activity finished
     * Override this method to unregister MyBroadCastReceiver
     * */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // make sure to unregister your receiver after finishing of this activity
        unregisterReceiver(myBroadCastReceiver);
    }
}
