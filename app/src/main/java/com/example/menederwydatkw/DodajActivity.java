package com.example.menederwydatkw;

import android.content.SharedPreferences; // Dodaj ten import
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import java.util.concurrent.CompletableFuture;

import com.example.menederwydatkw.db.BazaDanych;
import com.example.menederwydatkw.db.Wydatek;

public class DodajActivity extends AppCompatActivity {

    private BazaDanych db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj);

        // Elementy interfejsu
        EditText inputTytul = findViewById(R.id.inputTytul);
        EditText inputKwota = findViewById(R.id.inputKwota);
        RadioGroup rgKategorie = findViewById(R.id.rgKategorie);
        Button btnZapisz = findViewById(R.id.btnZapiszWydatek);

        // Inicjalizacja bazy
        db = Room.databaseBuilder(getApplicationContext(),
                        BazaDanych.class, "baza-wydatkow")
                .fallbackToDestructiveMigration()
                .build();

        // Kliknięcie zapisz
        btnZapisz.setOnClickListener(v -> {
            String tytul = inputTytul.getText().toString();
            String kwotaStr = inputKwota.getText().toString();

            int selectedId = rgKategorie.getCheckedRadioButtonId();
            String kategoria = "Inne";
            if (selectedId != -1) {
                RadioButton rb = findViewById(selectedId);
                kategoria = rb.getText().toString();
            }

            // Walidacja czy pola nie są puste
            if (!tytul.isEmpty() && !kwotaStr.isEmpty()) {
                Wydatek nowyWydatek = new Wydatek();
                nowyWydatek.tytul = tytul;
                nowyWydatek.kwota = Float.parseFloat(kwotaStr);
                nowyWydatek.kategoria = kategoria;
                nowyWydatek.data = System.currentTimeMillis();

                // Pobieramy walutę z ustawień i zapisujemy w wydatku
                SharedPreferences sharedPref = getSharedPreferences("MojeUstawienia", MODE_PRIVATE);
                String aktualnaWaluta = sharedPref.getString("WALUTA", "PLN");
                nowyWydatek.waluta = aktualnaWaluta;

                // Zapis do bazy
                CompletableFuture.runAsync(() -> db.wydatekDao().insert(nowyWydatek))
                        .thenRun(() -> runOnUiThread(() -> {
                            Toast.makeText(this, "Zapisano!", Toast.LENGTH_SHORT).show();
                            finish();
                        }));
            } else {
                Toast.makeText(this, "Uzupełnij pola!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}