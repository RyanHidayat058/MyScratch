package com.myscratch.app.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Otp : Screen("otp/{email}") {
        fun createRoute(email: String): String =
            "otp/" + URLEncoder.encode(email, StandardCharsets.UTF_8.toString())
    }
    object Profile : Screen("profile")
    object Home : Screen("home")
    object Finance : Screen("finance")
    object Notes : Screen("notes")
    object AddTransaction : Screen("add_transaction")
    object EditTransaction : Screen("edit_transaction/{id}") {
        fun createRoute(id: String) = "edit_transaction/$id"
    }
    object EditNote : Screen("edit_note/{noteId}/{folderId}") {
        fun createRoute(noteId: String, folderId: String) = "edit_note/$noteId/$folderId"
    }
    object Vault : Screen("vault")
}
