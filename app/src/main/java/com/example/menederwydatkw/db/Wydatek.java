package com.example.menederwydatkw.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wydatki")
public class Wydatek {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    public String tytul;
    public float kwota;
    public String kategoria; // np. "Dom", "Paliwo"
    public long data;        // Data jako timestamp (ułatwia sortowanie)
}