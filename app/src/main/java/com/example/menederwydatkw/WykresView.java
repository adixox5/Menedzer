package com.example.menederwydatkw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.Map;

public class WykresView extends View {
    private Paint paint;
    private Map<String, Float> daneKategorii;
    private float maxKwota = 1;
    private float postepAnimacji = 0f; // Od 0 do 1

    public WykresView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStrokeWidth(5); // PDF 9
    }

    // Metoda dla ObjectAnimator
    public void setPostep(float postep) {
        this.postepAnimacji = postep;
        invalidate(); // Wymuszenie przerysowania (PDF 9)
    }

    public void ustawDane(Map<String, Float> dane) {
        this.daneKategorii = dane;
        // Obliczamy max, aby skalować słupki
        maxKwota = 0;
        for (float kwota : dane.values()) {
            if (kwota > maxKwota) maxKwota = kwota;
        }
        if (maxKwota == 0) maxKwota = 1;
        invalidate(); // Odśwież widok po otrzymaniu danych
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Tło
        canvas.drawColor(Color.parseColor("#E0E0E0"));

        int width = getWidth();
        int height = getHeight();
        int padding = 60; // Zwiększony padding, żeby napis nad słupkiem się zmieścił

        // 1. Rysowanie osi (PDF 9)
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
        canvas.drawLine(padding, padding, padding, height - padding, paint); // Oś Y
        canvas.drawLine(padding, height - padding, width - padding, height - padding, paint); // Oś X

        if (daneKategorii == null || daneKategorii.isEmpty()) return;

        // 2. Rysowanie słupków
        int iloscSlupkow = daneKategorii.size();
        // Obliczamy szerokość tak, aby słupki nie były za szerokie
        int szerokoscSlupka = (width - 2 * padding) / (iloscSlupkow * 2);

        int x = padding + 40; // Początkowy X z lekkim przesunięciem

        int[] kolory = {Color.BLUE, Color.parseColor("#006400"), Color.MAGENTA, Color.CYAN, Color.DKGRAY};
        int i = 0;

        for (Map.Entry<String, Float> entry : daneKategorii.entrySet()) {
            float kwota = entry.getValue();

            // Obliczanie wysokości z uwzględnieniem animacji
            // (height - 2 * padding) to dostępna wysokość robocza
            float wysokoscRelatywna = (kwota / maxKwota) * (height - 2 * padding);
            float aktualnaWysokosc = wysokoscRelatywna * postepAnimacji;

            // Współrzędna Y góry słupka
            float topSlupka = (height - padding) - aktualnaWysokosc;

            // --- RYSOWANIE SŁUPKA ---
            paint.setColor(kolory[i % kolory.length]);
            canvas.drawRect(
                    x,
                    topSlupka,
                    x + szerokoscSlupka,
                    height - padding,
                    paint
            );

            // --- RYSOWANIE WARTOŚCI NAD SŁUPKIEM (Nowość) ---
            paint.setColor(Color.BLACK);
            paint.setTextSize(35);
            paint.setTextAlign(Paint.Align.CENTER); // Wyśrodkowanie tekstu względem X

            // Rysujemy tekst: X = środek słupka, Y = 10px nad słupkiem
            canvas.drawText(
                    String.valueOf(kwota),
                    x + (szerokoscSlupka / 2f),
                    topSlupka - 10,
                    paint
            );

            // --- RYSOWANIE PODPISU KATEGORII (Pod osią X) ---
            paint.setTextSize(30); // Mniejszy tekst dla kategorii
            // Rysujemy pod osią (height - padding + 40)
            canvas.drawText(entry.getKey(), x + (szerokoscSlupka / 2f), height - padding + 40, paint);

            // Przywracamy domyślne wyrównanie (dla bezpieczeństwa kolejnych operacji)
            paint.setTextAlign(Paint.Align.LEFT);

            // Przesunięcie X dla następnego słupka
            x += szerokoscSlupka * 2;
            i++;
        }
    }
}