package me.camilorojas.unabshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            var startDestination: String = "login"

            val auth = Firebase.auth
            val currentUser = auth.currentUser

            if (currentUser != null) {
                startDestination = "home"
            } else {
                startDestination = "login"
            }
            
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(route = "login") {
                    LoginScreen(
                        navController = navController,
                        onSuccessfullLogin = {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }

                composable(route = "register") {
                    RegisterScreen(
                        navController = navController,
                        onSuccessfulRegister = {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }

                composable(route = "home") {
                    HomeScreen(onClickLogout = {
                            navController.navigate("login") {
                                popUpTo(0)}
                        }
                    )
                }
            }
        }
    }
}
