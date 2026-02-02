package com.example.menederwydatkw;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter; // Potrzebne do Spinnera
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner; // Potrzebne do Spinnera
import androidx.appcompat.app.AppCompatActivity;

public class UstawieniaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ustawienia);

        EditText editLimit = findViewById(R.id.editLimit);
        Spinner spinnerWaluta = findViewById(R.id.spinnerWaluta); // Pobieramy Spinner
        RadioButton rbData = findViewById(R.id.rbSortData);
        RadioButton rbKwota = findViewById(R.id.rbSortKwota);
        Button btnZapisz = findViewById(R.id.btnZapiszUstawienia);

        // 1. Konfiguracja Spinnera (Lista walut)
        String[] dostepneWaluty = {"PLN", "EUR", "USD", "GBP", "CHF"};

        // ArrayAdapter łączy tablicę napisów z widokiem Spinnera
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                dostepneWaluty
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWaluta.setAdapter(adapter);

        // 2. Odczyt zapisanych ustawień (SharedPreferences - PDF 6)
        SharedPreferences sharedPref = getSharedPreferences("MojeUstawienia", MODE_PRIVATE);

        // Ustawienie limitu
        float limit = sharedPref.getFloat("LIMIT_MIESIECZNY", 1000f);
        editLimit.setText(String.valueOf(limit));

        // Ustawienie wybranej waluty w Spinnerze
        String zapisanaWaluta = sharedPref.getString("WALUTA", "PLN");
        // Znajdujemy na jakiej pozycji jest zapisana waluta (np. EUR to pozycja 1)
        int pozycja = adapter.getPosition(zapisanaWaluta);
        if (pozycja >= 0) {
            spinnerWaluta.setSelection(pozycja);
        }

        // Ustawienie sortowania
        String sortowanie = sharedPref.getString("SORTOWANIE", "DATA");
        if (sortowanie.equals("KWOTA")) {
            rbKwota.setChecked(true);
        } else {
            rbData.setChecked(true);
        }

        // 3. Zapisywanie ustawień
        btnZapisz.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPref.edit();

            // Pobieramy tekst z pola limitu
            String limitStr = editLimit.getText().toString();
            if(!limitStr.isEmpty()) {
                editor.putFloat("LIMIT_MIESIECZNY", Float.parseFloat(limitStr));
            }

            // Zmienione: Pobieramy wybraną wartość ze Spinnera
            String wybranaWaluta = spinnerWaluta.getSelectedItem().toString();
            editor.putString("WALUTA", wybranaWaluta);

            // Zapis sortowania
            if (rbData.isChecked()) {
                editor.putString("SORTOWANIE", "DATA");
            } else {
                editor.putString("SORTOWANIE", "KWOTA");
            }

            editor.apply(); // Zatwierdź zmiany
            finish(); // Wróć do ekranu głównego
        });
    }
}