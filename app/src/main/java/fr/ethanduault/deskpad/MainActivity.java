package fr.ethanduault.deskpad;

import static fr.ethanduault.deskpad.Utils.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private WebSocketClient webSocketClient;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        preferences = getSharedPreferences("settings", MODE_PRIVATE);
        if (preferences.getBoolean("firstRun", true)) {
            Intent intent = new Intent(this, FirstRunActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        Button buttonSettings = findViewById(R.id.settings);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button buttonProfiles = findViewById(R.id.profiles);
        buttonProfiles.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String[] choices;
                JSONArray profiles;
                try {
                    profiles = new JSONArray(preferences.getString("profiles", ""));
                    choices = new String[profiles.length()];
                    for (int i = 0; i < profiles.length(); i++) {
                        choices[i] = profiles.getJSONObject(i).getString("name");
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder
                        .setTitle("Profils")
                        .setPositiveButton("Sélectionner", (dialog, which) -> {
                            loadProfile(choices[which+1]);
                        })
                        .setNegativeButton("Nouveau", (dialog, which) -> {

                        })
                        .setNeutralButton("Sauvegarder", (dialog, which) -> {

                        })
                        .setSingleChoiceItems(choices, 0, (dialog, which) -> {
                        });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        createWebSocketClient(preferences.getString("ipAddress", ""), preferences.getInt("port", 9876));
    }

    private void sendMessage(String type, String message) {
        JSONObject data = new JSONObject();
        try {
            data.put("type", type);
            data.put("message", message);
            webSocketClient.send(data.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void createWebSocketClient(String address, int port) {
        URI uri;
        try {
            uri = new URI("ws://" + address + ":" + port);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.i("WebSocket", "Connecting");
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connecting", Toast.LENGTH_SHORT).show());
                JSONObject data = new JSONObject();
                try {
                    data.put("type", "auth");
                    data.put("password", getSHA256(preferences.getString("password", "")));
                    send(data.toString());
                } catch (JSONException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onMessage(String message) {
                JSONObject data;
                try {
                    data = new JSONObject(message);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                try {
                    if (data.getString("type").equals("auth")) {
                        if (data.getBoolean("success")) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show());
                            TextView textView = findViewById(R.id.status);
                            textView.setText("Status : Connected");
                        } else {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show());
                            TextView textView = findViewById(R.id.status);
                            textView.setText("Status : Authentication failed");
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i("WebSocket", "Closed " + reason);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Closed " + reason, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(Exception ex) {

            }
        };
        webSocketClient.connect();
    }

    private void loadProfile(String name) {
        JSONArray profiles;

        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);
        Button button4 = findViewById(R.id.button4);
        Button button5 = findViewById(R.id.button5);
        Button button6 = findViewById(R.id.button6);

        try {
            profiles = new JSONArray(preferences.getString("profiles", ""));
            for (int i = 0; i < profiles.length(); i++) {
                JSONObject profile = profiles.getJSONObject(i);
                if (profile.getString("name").equals(name)) {
                    for (int j = 1; j < profile.length(); j++) {
                        JSONObject data = profile.getJSONObject("Button" + j);
                        if (data.getString("type").equals("key")) {
                            String value = data.getString("value");
                            switch (j) {
                                case 1:
                                    button1.setText(data.getString("value"));
                                    button1.setOnClickListener(v -> sendMessage("key", value));
                                    button1.setOnLongClickListener(v -> {
                                        sendMessage("key", value);
                                        return true;
                                    });
                                    break;
                                case 2:
                                    button2.setText(data.getString("value"));
                                    button2.setOnClickListener(v -> sendMessage("key", value));
                                    break;
                                case 3:
                                    button3.setText(data.getString("value"));
                                    button3.setOnClickListener(v -> sendMessage("key", value));
                                    break;
                                case 4:
                                    button4.setText(data.getString("value"));
                                    button4.setOnClickListener(v -> sendMessage("key", value));
                                    break;
                                case 5:
                                    button5.setText(data.getString("value"));
                                    button5.setOnClickListener(v -> sendMessage("key", value));
                                    break;
                                case 6:
                                    button6.setText(data.getString("value"));
                                    button6.setOnClickListener(v -> sendMessage("key", value));
                                    break;
                            }
                        }
                    }
                    return;
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}