package com.example.geoglow

data class Friend (
    val name: String,
    val friendId: String?,
    val devices: MutableList<String>
)