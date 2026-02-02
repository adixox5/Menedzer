package com.example.menederwydatkw;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import com.example.menederwydatkw.db.BazaDanych;
import com.example.menederwydatkw.db.Wydatek;

public class DodajActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj);

        EditText inputTytul = findViewById(R.id.inputTytul);
        EditText inputKwota = findViewById(R.id.inputKwota);
        RadioGroup rgKategorie = findViewById(R.id.rgKategorie);
        Button btnZapisz = findViewById(R.id.btnZapiszWydatek);

        BazaDanych db = Room.databaseBuilder(getApplicationContext(),
                BazaDanych.class, "baza-wydatkow").allowMainThreadQueries().build();

        btnZapisz.setOnClickListener(v -> {
            String tytul = inputTytul.getText().toString();
            String kwotaStr = inputKwota.getText().toString();

            // Pobranie zaznaczonej kategorii
            int selectedId = rgKategorie.getCheckedRadioButtonId();
            RadioButton selectedRb = findViewById(selectedId);
            String kategoria = (selectedRb != null) ? selectedRb.getText().toString() : "Inne";

            if (!kwotaStr.isEmpty()) {
                Wydatek w = new Wydatek();
                w.tytul = tytul;
                w.kwota = Float.parseFloat(kwotaStr);
                w.kategoria = kategoria;
                w.data = System.currentTimeMillis(); // Data bieżąca

                db.wydatekDao().insert(w);
                finish(); // PDF 4: Zakończenie aktywności
            }
        });
    }
}