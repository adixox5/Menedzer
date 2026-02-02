package com.example.menederwydatkw.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface WydatekDao {
    @Insert
    void insert(Wydatek wydatek);

    @Delete
    void delete(Wydatek wydatek);

    @Query("SELECT * FROM wydatki")
    List<Wydatek> getAll();
}