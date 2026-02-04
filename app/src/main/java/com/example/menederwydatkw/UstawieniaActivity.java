package com.example.menederwydatkw;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class UstawieniaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ustawienia);

        EditText editLimit = findViewById(R.id.editLimit);
        Spinner spinnerWaluta = findViewById(R.id.spinnerWaluta);
        RadioButton rbData = findViewById(R.id.rbSortData);
        RadioButton rbKwota = findViewById(R.id.rbSortKwota);
        Button btnZapisz = findViewById(R.id.btnZapiszUstawienia);

        // Konfiguracja listy walut
        String[] dostepneWaluty = {"PLN", "EUR", "USD", "GBP", "CHF"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, dostepneWaluty
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWaluta.setAdapter(adapter);

        // Odczyt obecnych ustawień
        SharedPreferences sharedPref = getSharedPreferences("MojeUstawienia", MODE_PRIVATE);

        editLimit.setText(String.valueOf(sharedPref.getFloat("LIMIT_MIESIECZNY", 1000f)));

        // Ustawienie listy na zapisaną walutę
        String zapisanaWaluta = sharedPref.getString("WALUTA", "PLN");
        int pozycja = adapter.getPosition(zapisanaWaluta);
        if (pozycja >= 0) spinnerWaluta.setSelection(pozycja);

        // Ustawienie przełącznika sortowania
        String sortowanie = sharedPref.getString("SORTOWANIE", "DATA");
        if ("KWOTA".equals(sortowanie)) rbKwota.setChecked(true);
        else rbData.setChecked(true);

        // Zapis ustawień
        btnZapisz.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPref.edit();

            String limitStr = editLimit.getText().toString();
            if(!limitStr.isEmpty()) {
                editor.putFloat("LIMIT_MIESIECZNY", Float.parseFloat(limitStr));
            }

            editor.putString("WALUTA", spinnerWaluta.getSelectedItem().toString());
            editor.putString("SORTOWANIE", rbData.isChecked() ? "DATA" : "KWOTA");

            editor.apply();
            finish(); // Wraca do MainActivity
        });
    }
}