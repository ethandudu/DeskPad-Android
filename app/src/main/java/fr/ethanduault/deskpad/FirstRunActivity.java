package fr.ethanduault.deskpad;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import fr.ethanduault.deskpad.fragments.FragmentWelcome;

public class FirstRunActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_run);

        if (savedInstanceState == null) {
            FragmentWelcome fragmentWelcome = new FragmentWelcome();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragmentWelcome)
                    .commit();
        }
    }
}
