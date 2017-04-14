package com.willowtreeapps.gcmpushexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class MainPushActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_push);

        final EditText chatText = (EditText) findViewById(R.id.chat_text);

        chatText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        new SendMessage().execute(chatText.getText().toString());
                    }
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(PushFcmListenerService.MSG_DELIVERY);

        LocalBroadcastManager.getInstance(this).registerReceiver(onMessageReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onMessageReceiver);
    }

    BroadcastReceiver onMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            EditText chatDisplay = (EditText) findViewById(R.id.chat_view);
            String bodyText = intent.getStringExtra(PushFcmListenerService.TEXT_KEY);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.US);
            String line = String.format("%s : %s%n", sdf.format(new Date()), bodyText);
            chatDisplay.setText(String.format("%s %s", line, chatDisplay.getText().toString()));
        }
    };

    private class SendMessage extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String msg = params[0];
            InputStream stream = null;
            HttpsURLConnection connection = null;
            try {
                URL url = new URL("https://fcm.googleapis.com/fcm/send");
                connection = (HttpsURLConnection) url.openConnection();
                connection.setReadTimeout(3000);
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setRequestProperty("Content-Type","application/json");
                connection.setRequestProperty("Authorization",String.format("key=%s", BuildConfig.FCM_API_KEY));
                connection.connect();

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("to", PushFcmListenerService.TOPICS_FIELD);
                JSONObject data = new JSONObject();
                data.put(PushFcmListenerService.TEXT_KEY, msg);
                jsonParam.put("data", data);

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(jsonParam.toString());
                wr.flush();
                wr.close();

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error code: " + responseCode);
                }
                stream = connection.getInputStream();
            } catch (Exception e) {
                Log.e("ChatException", "Couldn't deliver the message", e.getCause());
            } finally {
                // Close Stream and disconnect HTTPS connection.
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        Log.e("ChatException", "Couldn't close the connection", e.getCause());
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ((EditText) findViewById(R.id.chat_text)).setText(null);
        }
    }
}
