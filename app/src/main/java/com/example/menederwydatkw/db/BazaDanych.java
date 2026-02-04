package com.example.menederwydatkw.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// Główna klasa konfiguracyjna bazy danych
@Database(entities = {Wydatek.class}, version = 3)
public abstract class BazaDanych extends RoomDatabase {
    public abstract WydatekDao wydatekDao();
}