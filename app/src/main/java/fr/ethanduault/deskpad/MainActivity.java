package fr.ethanduault.deskpad;

import static fr.ethanduault.deskpad.Utils.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
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

        createWebSocketClient(preferences.getString("ipAddress", ""), preferences.getInt("port", 9876));

        Button buttonF13 = findViewById(R.id.button1);
        buttonF13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("key","F13");
            }
        });


        Button buttonCommand = findViewById(R.id.button2);
        buttonCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("run","explorer.exe");
            }
        });

        buttonCommand.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(MainActivity.this, "Long click", Toast.LENGTH_SHORT).show();
                sendMessage("run","calc.exe");
                return true;
            }
        });

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
                    data.put("password", preferences.getString("password", ""));
                    send(data.toString());
                } catch (JSONException e) {
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
                        } else {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show());
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
}