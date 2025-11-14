package com.pmob.baseproj5

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "barang")
data class Barang(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "nama")
    val nama: String,

    @ColumnInfo(name = "jenis")
    val jenis: String,

    @ColumnInfo(name = "harga")
    val harga: Int,

    @ColumnInfo(name = "imageUri")
    val imageUri: String? = null

)