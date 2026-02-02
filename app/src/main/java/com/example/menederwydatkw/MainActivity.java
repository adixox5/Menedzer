package com.example.menederwydatkw;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.example.menederwydatkw.db.BazaDanych;
import com.example.menederwydatkw.db.Wydatek;

public class MainActivity extends AppCompatActivity {

    // Zmienne globalne
    private BazaDanych db;
    private WykresView wykresView;
    private TextView tvSumaMiesiac;
    private LinearLayout layoutLista; // Kontener na listę wydatków

    // Zmienne ustawień
    private float limitMiesieczny;
    private String waluta;
    private String sortowanie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Przypisanie widoków z XML
        wykresView = findViewById(R.id.wykresView);
        tvSumaMiesiac = findViewById(R.id.tvSumaMiesiac);
        layoutLista = findViewById(R.id.layoutLista);

        Button btnDodaj = findViewById(R.id.btnDodaj);
        Button btnUstawienia = findViewById(R.id.btnUstawienia);
        Button btnRaport = findViewById(R.id.btnRaport);

        // 2. Inicjalizacja Bazy Danych
        // .fallbackToDestructiveMigration() pozwala uniknąć błędów przy zmianie struktury bazy
        db = Room.databaseBuilder(getApplicationContext(),
                        BazaDanych.class, "baza-wydatkow")
                .fallbackToDestructiveMigration()
                .build();

        // 3. Obsługa przycisków
        btnDodaj.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DodajActivity.class);
            startActivity(intent);
        });

        btnUstawienia.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UstawieniaActivity.class);
            startActivity(intent);
        });

        // 4. Udostępnianie raportu
        btnRaport.setOnClickListener(v -> udostepnijRaport());
    }

    // Metoda wywoływana ZAWSZE gdy wracamy do ekranu (np. po dodaniu wydatku)
    @Override
    protected void onResume() {
        super.onResume();
        wczytajUstawienia();
        odswiezDaneWTle();
    }

    // Pobieranie ustawień z SharedPreferences
    private void wczytajUstawienia() {
        SharedPreferences sharedPref = getSharedPreferences("MojeUstawienia", MODE_PRIVATE);
        limitMiesieczny = sharedPref.getFloat("LIMIT_MIESIECZNY", 1000f); // Domyślnie 1000
        waluta = sharedPref.getString("WALUTA", "PLN");
        sortowanie = sharedPref.getString("SORTOWANIE", "DATA");
    }

    // Praca na wątkach przy użyciu CompletableFuture (
    private void odswiezDaneWTle() {
        CompletableFuture.supplyAsync(() -> {
            List<Wydatek> lista = db.wydatekDao().getAll();

            // Sortowanie listy
            if ("KWOTA".equals(sortowanie)) {
                lista.sort((w1, w2) -> Float.compare(w2.kwota, w1.kwota)); // Malejąco po kwocie
            } else {
                lista.sort((w1, w2) -> Long.compare(w2.data, w1.data));    // Malejąco po dacie (najnowsze)
            }
            return lista;
        }).thenAccept(lista -> {
            runOnUiThread(() -> aktualizujInterfejs(lista));
        });
    }

    // Główna metoda aktualizująca wygląd aplikacji
    private void aktualizujInterfejs(List<Wydatek> lista) {
        if (layoutLista == null) return;

        layoutLista.removeAllViews(); // Czyścimy starą listę
        float sumaCalkowita = 0;
        Map<String, Float> daneDoWykresu = new HashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault());

        for (Wydatek w : lista) {
            sumaCalkowita += w.kwota;
            float obecnaSumaKat = daneDoWykresu.getOrDefault(w.kategoria, 0f);
            daneDoWykresu.put(w.kategoria, obecnaSumaKat + w.kwota);

            // 1. Tworzymy KONTENER na jeden wiersz (Poziomy układ: Tekst + Kosz)
            LinearLayout wiersz = new LinearLayout(this);
            wiersz.setOrientation(LinearLayout.HORIZONTAL);
            wiersz.setPadding(10, 10, 10, 10);
            wiersz.setBackgroundColor(Color.parseColor("#FAFAFA")); // Tło wiersza

            // Parametry wiersza (odstęp od dołu)
            LinearLayout.LayoutParams paramsWiersza = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            paramsWiersza.setMargins(0, 0, 0, 5);
            wiersz.setLayoutParams(paramsWiersza);

            // 2. Tworzymy TEKST (Szczegóły wydatku)
            TextView tv = new TextView(this);
            String dataTekst = sdf.format(new java.util.Date(w.data));
            String tekst = String.format("%s\n%s: %.2f %s (%s)",
                    dataTekst, w.tytul, w.kwota, waluta, w.kategoria);
            tv.setText(tekst);
            tv.setTextSize(16);
            tv.setTextColor(Color.BLACK);

            // LayoutParams dla tekstu: waga 1, żeby zajął całe miejsce po lewej
            LinearLayout.LayoutParams paramsTekst = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f // Waga 1.0 rozciąga tekst
            );
            paramsTekst.gravity = android.view.Gravity.CENTER_VERTICAL; // Wyśrodkowanie w pionie
            tv.setLayoutParams(paramsTekst);

            // 3. Tworzymy PRZYCISK KOSZA (ImageButton)
            android.widget.ImageButton btnUsun = new android.widget.ImageButton(this);
            // Używamy systemowej ikony kosza
            btnUsun.setImageResource(android.R.drawable.ic_menu_delete);
            btnUsun.setBackgroundColor(Color.TRANSPARENT); // Przezroczyste tło przycisku
            btnUsun.setPadding(20, 20, 20, 20);

            // --- LOGIKA USUWANIA (Instrukcja 8 + 7) ---
            btnUsun.setOnClickListener(v -> {
                // Usuwanie w tle
                CompletableFuture.runAsync(() -> {
                    db.wydatekDao().delete(w); // Instrukcja 8: delete
                }).thenRun(() -> {
                    // Po usunięciu odświeżamy wszystko
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Usunięto wydatek", Toast.LENGTH_SHORT).show();
                        odswiezDaneWTle(); // Ponowne pobranie listy i rysowanie
                    });
                });
            });

            // 4. Składamy wiersz w całość
            wiersz.addView(tv);      // Dodaj tekst
            wiersz.addView(btnUsun); // Dodaj przycisk

            // 5. Dodajemy gotowy wiersz do głównej listy
            layoutLista.addView(wiersz);
        }

        // Aktualizacja nagłówka sumy
        if (tvSumaMiesiac != null) {
            tvSumaMiesiac.setText("Suma: " + sumaCalkowita + " " + waluta);
            if (sumaCalkowita > limitMiesieczny) {
                tvSumaMiesiac.setTextColor(Color.RED);
            } else {
                tvSumaMiesiac.setTextColor(Color.BLACK);
            }
        }

        // Aktualizacja Wykresu
        if (wykresView != null) {
            wykresView.ustawDane(daneDoWykresu);
            ObjectAnimator anim = ObjectAnimator.ofFloat(wykresView, "postep", 0f, 1f);
            anim.setDuration(1000);
            anim.setInterpolator(new BounceInterpolator());
            anim.start();
        }
    }
    // Metoda obsługująca przycisk Raport
    private void udostepnijRaport() {
        String tekstRaportu = (tvSumaMiesiac != null) ? tvSumaMiesiac.getText().toString() : "Brak danych";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Mój miesięczny raport wydatków. " + tekstRaportu);

        // Sprawdzenie czy jest aplikacja do wysłania
        startActivity(Intent.createChooser(intent, "Wyślij raport przez..."));
    }
}