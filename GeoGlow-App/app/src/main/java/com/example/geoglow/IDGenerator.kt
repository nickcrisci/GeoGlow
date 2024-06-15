package com.example.geoglow

import java.util.UUID

object IDGenerator {
    fun generateUniqueID(): String {
        return UUID.randomUUID().toString()
    }
}
