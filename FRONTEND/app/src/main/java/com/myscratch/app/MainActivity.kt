package com.myscratch.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.myscratch.app.ui.navigation.AppNavGraph
import com.myscratch.app.ui.theme.AppColors
import com.myscratch.app.ui.theme.MyScratchTheme
import com.myscratch.app.ui.theme.ThemeState
import com.myscratch.app.viewmodel.AuthViewModel

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeState = remember { ThemeState.create() }

            MyScratchTheme(themeState = themeState) {
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.provideFactory(MyScratchApp.instance.authRepository)
                )

                val googleSignInLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        val idToken = account?.idToken ?: ""
                        val name = account?.displayName ?: "Pengguna Google"
                        val email = account?.email ?: ""
                        val photoUrl = account?.photoUrl?.toString()

                        if (email.isNotBlank()) {
                            authViewModel.loginWithGoogle(idToken, name, email, photoUrl)
                        } else {
                            Toast.makeText(this, "Tidak dapat membaca email akun Google.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: ApiException) {
                        if (e.statusCode == 10 || e.statusCode == 12500) {
                            Toast.makeText(
                                this,
                                "Google Sign-In memerlukan SHA-1 dan Web Client ID di strings.xml (lihat panduan README).",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(this, "Login Google dibatalkan atau gagal: ${e.statusCode}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppColors.background
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val isTablet = maxWidth >= 600.dp
                        AppNavGraph(
                            authViewModel = authViewModel,
                            onGoogleSignInClick = {
                                val defaultClientId = getString(R.string.default_web_client_id)
                                try {
                                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestEmail()
                                        .requestIdToken(defaultClientId)
                                        .build()
                                    val client = GoogleSignIn.getClient(this@MainActivity, gso)
                                    googleSignInLauncher.launch(client.signInIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Masukkan Web Client ID di res/values/strings.xml untuk mengaktifkan Google Login.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            isTablet = isTablet
                        )
                    }
                }
            }
        }
    }
}
