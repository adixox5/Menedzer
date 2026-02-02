package com.example.menederwydatkw.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wydatki")
public class Wydatek {

    //Automatyczne nadawanie Id
    @PrimaryKey(autoGenerate = true)
    public int uid;

    public String tytul;
    public float kwota;
    public String kategoria;
    public long data;
    public String waluta;
}