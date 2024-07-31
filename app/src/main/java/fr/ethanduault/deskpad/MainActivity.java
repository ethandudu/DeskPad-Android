package fr.ethanduault.deskpad;

import static fr.ethanduault.deskpad.Utils.getSHA256;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

import fr.ethanduault.deskpad.Utils;

public class MainActivity extends AppCompatActivity {

    private static final String SERVER_PASSWORD;

    static {
        try {
            SERVER_PASSWORD = getSHA256("password");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

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

        Button buttonF13 = findViewById(R.id.buttonF13);
        buttonF13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("key","F13");
            }
        });
        Button buttonCommand = findViewById(R.id.buttonCommand);
        buttonCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("run","explorer.exe");
            }
        });
    }

    private void sendMessage(String type, String message) {
        new SendMessageTask().execute(type, message);
    }

    private class SendMessageTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            JSONObject data = new JSONObject();
            try {
                data.put("password", SERVER_PASSWORD);
                data.put("type", params[0]);
                data.put("message", params[1]);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            EditText ipAddress = findViewById(R.id.ipAddress);
            EditText ipPort = findViewById(R.id.ipPort);
            System.out.println(ipAddress.getText().toString());
            System.out.println(ipPort.getText().toString());
            try (Socket socket = new Socket(ipAddress.getText().toString(), Integer.parseInt(ipPort.getText().toString()));


                 PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {
                out.println(data.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}