package com.example.geoglow

import java.util.UUID

object IDGenerator {
    fun generateUniqueID(): String {
        val uuid= UUID.randomUUID().toString()
        return uuid.substring(0, 8)
    }
}
