package fr.ethanduault.deskpad;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        EditText ipAddress = findViewById(R.id.ipAddress);
        EditText port = findViewById(R.id.port);
        EditText password = findViewById(R.id.password);

        try {
            ipAddress.setText(preferences.getString("ipAddress", ""));
            port.setText(preferences.getInt("port", 0) + "");
            password.setText(preferences.getString("password", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button buttonSave = findViewById(R.id.save);
        buttonSave.setOnClickListener(v -> {
            if (ipAddress.getText().toString().isEmpty() || port.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Erreur")
                        .setMessage("Veuillez remplir tous les champs.")
                        .setPositiveButton("OK", null)
                        .create();
                alertDialog.show();
                return;
            }
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("ipAddress", ipAddress.getText().toString());
            editor.putInt("port", Integer.parseInt(port.getText().toString()));
            editor.putString("password", password.getText().toString());
            editor.apply();
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Attention");
        builder.setMessage("Voulez-vous vraiment quitter sans sauvegarder ?");
        builder.setPositiveButton("Oui", (dialog, which) -> {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
            super.onBackPressed();
        });
        builder.setNegativeButton("Non", null);
        builder.create();
        builder.show();
    }
}