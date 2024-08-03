package fr.ethanduault.deskpad.fragments;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import fr.ethanduault.deskpad.MainActivity;
import fr.ethanduault.deskpad.R;
import fr.ethanduault.deskpad.Utils;

public class FragmentSettings extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Button buttonNext = view.findViewById(R.id.next);
        buttonNext.setOnClickListener(v -> {
            EditText ipAddress = view.findViewById(R.id.ipAddress);
            EditText port = view.findViewById(R.id.port);
            EditText password = view.findViewById(R.id.password);

            if (ipAddress.getText().toString().isEmpty() || port.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setTitle("Erreur");
                builder.setMessage("Veuillez remplir tous les champs");
                builder.setPositiveButton("OK", null);
                builder.show();
                return;
            }
            SharedPreferences preferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("ipAddress", ipAddress.getText().toString());
            editor.putInt("port", Integer.parseInt(port.getText().toString()));
            editor.putString("password", password.getText().toString());
            editor.putBoolean("firstRun", false);

            JSONArray profiles = new JSONArray();
            JSONObject profile = new JSONObject();
            try {
                profile.put("name", "Default");
                for (int i = 1; i < 7; i++) {
                    JSONObject data = new JSONObject();
                    data.put("type", "key");
                    data.put("value", i);
                    profile.put("Button" + i, data);
                }
                profiles.put(profile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            editor.putString("profiles", profiles.toString());

            editor.apply();
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });
        return view;
    }
}