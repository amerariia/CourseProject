package com.example.kursova;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button buttonSearch;
    Button buttonHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSearch = findViewById(R.id.search);
        buttonHelp = findViewById(R.id.help);
        buttonSearch.setOnClickListener(v -> {
        buttonSearch.setEnabled(false);
        buttonHelp.setEnabled(false);
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });

        buttonHelp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent);
        });

    }
    @Override
    protected void onResume(){
        super.onResume();
        buttonSearch.setEnabled(true);
        buttonHelp.setEnabled(true);
    }

    @Override
    protected void onStart(){
        super.onStart();
        buttonSearch.setEnabled(true);
        buttonHelp.setEnabled(true);
    }
}
