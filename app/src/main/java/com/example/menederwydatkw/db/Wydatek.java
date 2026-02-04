package com.example.menederwydatkw.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Room wie, że to jest tabela w bazie danych "wydatki"
@Entity(tableName = "wydatki")
public class Wydatek {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    // Kolumny tabeli
    public String tytul;
    public float kwota;
    public String kategoria;
    public long data;
    public String waluta;
}