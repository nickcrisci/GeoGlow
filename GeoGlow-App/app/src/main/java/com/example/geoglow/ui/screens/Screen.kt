package com.example.geoglow.ui.screens

sealed class Screen(val route: String) {
    object MainScreen: Screen("main_screen")
    object ImageScreen: Screen("image_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("?$arg={$arg}")
            }
        }
    }
}