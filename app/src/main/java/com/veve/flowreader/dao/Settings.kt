package com.veve.flowreader.dao

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Settings(
    @PrimaryKey @NonNull var name: String,
    var value: String?
)
