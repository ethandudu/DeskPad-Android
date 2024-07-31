package fr.ethanduault.deskpad;

import static fr.ethanduault.deskpad.Utils.getSHA256;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

    private static final String SERVER_IP = "192.168.1.161";
    private static final int SERVER_PORT = 9876;
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
    }

    private void sendMessage(String type, String message) {
        new SendMessageTask().execute(type, message);
    }

    private static class SendMessageTask extends AsyncTask<String, Void, Void> {
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
            try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);


                 PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {
                out.println(data.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}