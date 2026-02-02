package com.example.menederwydatkw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import java.util.Map;

public class WykresView extends View {
    private Paint paint;
    private Map<String, Float> daneKategorii;

    // Domyślnie 50, aby narysować pustą skalę 0-50, gdy brak danych
    private float maxKwota = 50;
    private float postepAnimacji = 1f; // 1.0 = 100% narysowania (domyślnie bez animacji)

    // Paleta kolorów słupków
    private final int[] kolory = {
            Color.parseColor("#2196F3"), // Niebieski
            Color.parseColor("#4CAF50"), // Zielony
            Color.parseColor("#FF9800"), // Pomarańczowy
            Color.parseColor("#E91E63"), // Różowy
            Color.parseColor("#9C27B0"), // Fioletowy
            Color.parseColor("#607D8B")  // Szary
    };

    public WykresView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true); // Wygładzanie krawędzi
    }

    // Metoda dla animacji (ObjectAnimator w MainActivity)
    public void setPostep(float postep) {
        this.postepAnimacji = postep;
        invalidate();
    }

    // Główna metoda przekazywania danych
    public void ustawDane(Map<String, Float> dane) {
        this.daneKategorii = dane;

        // 1. Znajdź największą kwotę w danych
        float maxZnaleziona = 0;
        if (dane != null) {
            for (float kwota : dane.values()) {
                if (kwota > maxZnaleziona) maxZnaleziona = kwota;
            }
        }

        // 2. Zaokrąglij w górę do najbliższej "pięćdziesiątki"
        // Np. 120 -> 150, 45 -> 50, 0 -> 50
        maxKwota = (float) Math.ceil(maxZnaleziona / 50.0) * 50;

        // Zabezpieczenie, żeby nie dzielić przez 0
        if (maxKwota == 0) maxKwota = 50;

        invalidate(); // Odśwież widok
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Tło wykresu
        canvas.drawColor(Color.parseColor("#F5F5F5"));

        int width = getWidth();
        int height = getHeight();

        // Marginesy (Padding)
        // Lewy margines większy (120px), żeby zmieściły się napisy "1500 zł"
        int paddingLeft = 120;
        int paddingBottom = 80;
        int paddingTop = 50;
        int paddingRight = 40;

        // Obszar roboczy, w którym rysujemy słupki
        float chartWidth = width - paddingLeft - paddingRight;
        float chartHeight = height - paddingTop - paddingBottom;

        // --- 1. RYSOWANIE SKALI I SIATKI (co 50 PLN) ---
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.RIGHT); // Wyrównanie tekstu do prawej (do osi)

        // Pętla od 0 do maxKwota z krokiem 50
        for (int wartosc = 0; wartosc <= maxKwota; wartosc += 50) {
            // Obliczamy pozycję Y dla danej wartości
            // wartosc=0 -> dół wykresu, wartosc=maxKwota -> góra wykresu
            float ratio = (float) wartosc / maxKwota;
            float y = (height - paddingBottom) - (ratio * chartHeight);

            // Rysowanie linii poziomej (siatki)
            if (wartosc == 0) {
                paint.setColor(Color.BLACK); // Oś X czarna i grubsza
                paint.setStrokeWidth(3);
            } else {
                paint.setColor(Color.LTGRAY); // Linie pomocnicze szare
                paint.setStrokeWidth(2);
            }
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, paint);

            // Rysowanie podpisu (np. "150 zł")
            paint.setColor(Color.DKGRAY);
            paint.setStrokeWidth(1);
            // Tekst rysujemy nieco na lewo od osi (paddingLeft - 15)
            canvas.drawText(wartosc + " zł", paddingLeft - 15, y + 10, paint);
        }

        // --- 2. RYSOWANIE SŁUPKÓW ---
        if (daneKategorii != null && !daneKategorii.isEmpty()) {
            int ilosc = daneKategorii.size();
            // Obliczamy szerokość jednego słupka
            float szerokoscSekcji = chartWidth / ilosc;
            float odstep = szerokoscSekcji * 0.25f; // 25% sekcji to odstęp
            float szerokoscSlupka = szerokoscSekcji - 2 * odstep;

            float currentX = paddingLeft + odstep;
            int colorIndex = 0;

            paint.setTextAlign(Paint.Align.CENTER); // Teksty nad słupkami centrujemy

            for (Map.Entry<String, Float> entry : daneKategorii.entrySet()) {
                float kwota = entry.getValue();

                // Wysokość słupka zależna od wartości i animacji
                float ratio = (kwota / maxKwota);
                float barHeight = ratio * chartHeight * postepAnimacji;

                // Współrzędne prostokąta
                float left = currentX;
                float top = (height - paddingBottom) - barHeight;
                float right = currentX + szerokoscSlupka;
                float bottom = height - paddingBottom;

                // Rysujemy słupek
                paint.setColor(kolory[colorIndex % kolory.length]);
                canvas.drawRect(left, top, right, bottom, paint);

                // Podpis kwoty nad słupkiem (pogrubiony)
                paint.setColor(Color.BLACK);
                paint.setTextSize(32);
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                canvas.drawText(String.format("%.0f", kwota), left + szerokoscSlupka / 2, top - 10, paint);

                // Podpis kategorii pod osią X (zwykły)
                paint.setTypeface(Typeface.DEFAULT);
                paint.setTextSize(30);
                String kat = entry.getKey();
                // Skracanie długich nazw
                if (kat.length() > 5) kat = kat.substring(0, 4) + ".";
                canvas.drawText(kat, left + szerokoscSlupka / 2, bottom + 40, paint);

                currentX += szerokoscSekcji;
                colorIndex++;
            }
        } else {
            // Komunikat gdy brak danych
            paint.setColor(Color.GRAY);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(40);
            canvas.drawText("Brak danych", width / 2f, height / 2f, paint);
        }

        // Pionowa linia osi Y (domknięcie z lewej)
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, height - paddingBottom, paint);
    }
}