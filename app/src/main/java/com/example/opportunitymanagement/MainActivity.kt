package com.example.opportunitymanagement
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.opportunitymanagement.data.local.AppDatabase
import com.example.opportunitymanagement.data.repository.AuthRepository
import com.example.opportunitymanagement.data.repository.OportunidadRepository
import com.example.opportunitymanagement.ui.navigation.AppNavigation
import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val database = AppDatabase.obtener(applicationContext)
  val repository = OportunidadRepository(database.oportunidades())
  val authRepository = AuthRepository(database)
  setContent {
   Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
    Surface(Modifier.fillMaxSize()) { AppNavigation(repository, authRepository) }
   }
  }
 }
}
