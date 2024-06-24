package com.example.geoglow

data class Friend (
    val name: String,
    val id: String?,
    val devices: MutableList<String>
)