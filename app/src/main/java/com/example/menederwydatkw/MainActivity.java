package com.example.menederwydatkw;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.example.menederwydatkw.db.BazaDanych;
import com.example.menederwydatkw.db.Wydatek;

public class MainActivity extends AppCompatActivity {

    private BazaDanych db;
    private WykresView wykresView;
    private TextView tvSumaMiesiac;
    private LinearLayout layoutLista;

    // Zmienna przechowująca aktualną walutę (domyślna)
    private String waluta = "PLN";
    private float limitMiesieczny;
    private String sortowanie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wykresView = findViewById(R.id.wykresView);
        tvSumaMiesiac = findViewById(R.id.tvSumaMiesiac);
        layoutLista = findViewById(R.id.layoutLista);

        Button btnDodaj = findViewById(R.id.btnDodaj);
        Button btnUstawienia = findViewById(R.id.btnUstawienia);
        Button btnRaport = findViewById(R.id.btnRaport);

        db = Room.databaseBuilder(getApplicationContext(),
                        BazaDanych.class, "baza-wydatkow")
                .fallbackToDestructiveMigration()
                .build();

        btnDodaj.setOnClickListener(v -> startActivity(new Intent(this, DodajActivity.class)));
        btnUstawienia.setOnClickListener(v -> startActivity(new Intent(this, UstawieniaActivity.class)));
        btnRaport.setOnClickListener(v -> udostepnijRaport());
    }

    @Override
    protected void onResume() {
        super.onResume();
        wczytajUstawienia();
        odswiezDaneWTle();
    }

    private void wczytajUstawienia() {
        SharedPreferences sharedPref = getSharedPreferences("MojeUstawienia", MODE_PRIVATE);
        limitMiesieczny = sharedPref.getFloat("LIMIT_MIESIECZNY", 1000f);
        waluta = sharedPref.getString("WALUTA", "PLN");
        sortowanie = sharedPref.getString("SORTOWANIE", "DATA");
    }

    private void odswiezDaneWTle() {
        CompletableFuture.supplyAsync(() -> {
            List<Wydatek> lista = db.wydatekDao().getAll();
            if ("KWOTA".equals(sortowanie)) {
                lista.sort((w1, w2) -> Float.compare(w2.kwota, w1.kwota));
            } else {
                lista.sort((w1, w2) -> Long.compare(w2.data, w1.data));
            }
            return lista;
        }).thenAccept(lista -> {
            runOnUiThread(() -> aktualizujInterfejs(lista));
        });
    }

    private void aktualizujInterfejs(List<Wydatek> lista) {
        if (layoutLista == null) return;

        layoutLista.removeAllViews();
        float sumaCalkowita = 0;
        Map<String, Float> daneDoWykresu = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault());

        for (Wydatek w : lista) {
            sumaCalkowita += w.kwota;
            daneDoWykresu.put(w.kategoria, daneDoWykresu.getOrDefault(w.kategoria, 0f) + w.kwota);

            LinearLayout wiersz = new LinearLayout(this);
            wiersz.setOrientation(LinearLayout.HORIZONTAL);
            wiersz.setPadding(10, 10, 10, 10);
            wiersz.setBackgroundColor(Color.parseColor("#FAFAFA"));

            LinearLayout.LayoutParams paramsWiersza = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            paramsWiersza.setMargins(0, 0, 0, 5);
            wiersz.setLayoutParams(paramsWiersza);

            TextView tv = new TextView(this);
            String dataTekst = sdf.format(new java.util.Date(w.data));

            // Waluta brana z wydatku (jeśli null to PLN)
            String walutaWydatku = (w.waluta != null) ? w.waluta : "PLN";

            String tekst = String.format("%s\n%s: %.2f %s (%s)",
                    dataTekst, w.tytul, w.kwota, walutaWydatku, w.kategoria);

            tv.setText(tekst);
            tv.setTextSize(16);
            tv.setTextColor(Color.BLACK);

            LinearLayout.LayoutParams paramsTekst = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            paramsTekst.gravity = android.view.Gravity.CENTER_VERTICAL;
            tv.setLayoutParams(paramsTekst);

            // Tutaj był błąd - teraz klasa jest zaimportowana
            ImageButton btnUsun = new ImageButton(this);
            btnUsun.setImageResource(android.R.drawable.ic_menu_delete);
            btnUsun.setBackgroundColor(Color.TRANSPARENT);
            btnUsun.setPadding(20, 20, 20, 20);

            btnUsun.setOnClickListener(v -> {
                CompletableFuture.runAsync(() -> db.wydatekDao().delete(w))
                        .thenRun(() -> runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Usunięto", Toast.LENGTH_SHORT).show();
                            odswiezDaneWTle();
                        }));
            });

            wiersz.addView(tv);
            wiersz.addView(btnUsun);
            layoutLista.addView(wiersz);
        }

        if (tvSumaMiesiac != null) {
            tvSumaMiesiac.setText("Suma: " + String.format("%.2f", sumaCalkowita) + " " + waluta);
            tvSumaMiesiac.setTextColor(sumaCalkowita > limitMiesieczny ? Color.RED : Color.BLACK);
        }

        if (wykresView != null) {
            wykresView.ustawWalute(waluta);
            wykresView.ustawDane(daneDoWykresu);
            ObjectAnimator anim = ObjectAnimator.ofFloat(wykresView, "postep", 0f, 1f);
            anim.setDuration(1000);
            anim.setInterpolator(new BounceInterpolator());
            anim.start();
        }
    }

    private void udostepnijRaport() {
        String raport = (tvSumaMiesiac != null) ? tvSumaMiesiac.getText().toString() : "";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Mój raport: " + raport);
        startActivity(Intent.createChooser(intent, "Wyślij raport przez..."));
    }
}