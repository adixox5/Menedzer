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

    private String symbolWaluty = "PLN";
    private float maxKwota = 50;
    private float postepAnimacji = 1f;

    // Kolory
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
        paint.setAntiAlias(true);
    }

    public void ustawWalute(String nowaWaluta) {
        this.symbolWaluty = nowaWaluta;
    }

    public void setPostep(float postep) {
        this.postepAnimacji = postep;
        invalidate();
    }

    // Ustawia dane i oblicza skalę wykresu
    public void ustawDane(Map<String, Float> dane) {
        this.daneKategorii = dane;

        float maxZnaleziona = 0;
        if (dane != null) {
            for (float kwota : dane.values()) {
                if (kwota > maxZnaleziona) maxZnaleziona = kwota;
            }
        }

        maxKwota = (float) Math.ceil(maxZnaleziona / 50.0) * 50;
        if (maxKwota == 0) maxKwota = 50;

        invalidate();
    }

    // Metoda rysowania
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.parseColor("#F5F5F5"));

        int width = getWidth();
        int height = getHeight();

        // Marginesy rzeby napisy nie wychodziły poza krawędź
        int paddingLeft = 180;
        int paddingBottom = 80;
        int paddingTop = 50;
        int paddingRight = 40;

        float chartWidth = width - paddingLeft - paddingRight;
        float chartHeight = height - paddingTop - paddingBottom;

        // rysowanie skali i siatki
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.RIGHT);

        // Pętla rysuje linie co 50
        for (int wartosc = 0; wartosc <= maxKwota; wartosc += 50) {
            float ratio = (float) wartosc / maxKwota;
            float y = (height - paddingBottom) - (ratio * chartHeight);

            if (wartosc == 0) {
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(3);
            } else {
                paint.setColor(Color.LTGRAY);
                paint.setStrokeWidth(2);
            }
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, paint);

            // Podpis wartości na osi Y
            paint.setColor(Color.DKGRAY);
            paint.setStrokeWidth(1);

            // Rysujemy tekst z uwzględnieniem waluty
            canvas.drawText(wartosc + " " + symbolWaluty, paddingLeft - 20, y + 10, paint);
        }

        // Słupki
        if (daneKategorii != null && !daneKategorii.isEmpty()) {
            int ilosc = daneKategorii.size();
            float szerokoscSekcji = chartWidth / ilosc;
            float odstep = szerokoscSekcji * 0.25f;
            float szerokoscSlupka = szerokoscSekcji - 2 * odstep;

            float currentX = paddingLeft + odstep;
            int colorIndex = 0;

            paint.setTextAlign(Paint.Align.CENTER);

            // Iteracja po danych
            for (Map.Entry<String, Float> entry : daneKategorii.entrySet()) {
                float kwota = entry.getValue();
                float ratio = (kwota / maxKwota);
                float barHeight = ratio * chartHeight * postepAnimacji;

                // Współrzędne prostokąta
                float left = currentX;
                float top = (height - paddingBottom) - barHeight;
                float right = currentX + szerokoscSlupka;
                float bottom = height - paddingBottom;

                paint.setColor(kolory[colorIndex % kolory.length]);
                canvas.drawRect(left, top, right, bottom, paint);

                // Wartość nad słupkiem
                paint.setColor(Color.BLACK);
                paint.setTextSize(32);
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                canvas.drawText(String.format("%.0f", kwota), left + szerokoscSlupka / 2, top - 10, paint);

                // Kategoria pod słupkiem
                paint.setTypeface(Typeface.DEFAULT);
                paint.setTextSize(25);
                String kat = entry.getKey();
                canvas.drawText(kat, left + szerokoscSlupka / 2, bottom + 40, paint);

                currentX += szerokoscSekcji;
                colorIndex++;
            }
        } else {
            paint.setColor(Color.GRAY);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(40);
            canvas.drawText("Brak danych", width / 2f, height / 2f, paint);
        }

        // Pionowa linia osi Y
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, height - paddingBottom, paint);
    }
}