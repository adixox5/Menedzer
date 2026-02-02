package com.example.menederwydatkw.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Wydatek.class}, version = 2)
public abstract class BazaDanych extends RoomDatabase {
    public abstract WydatekDao wydatekDao();
}