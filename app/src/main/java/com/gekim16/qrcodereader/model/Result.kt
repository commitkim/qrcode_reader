package com.gekim16.qrcodereader.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Result(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo(name = "type") val type: String = "URL",
    @ColumnInfo(name = "url") val url: String = ""
)

