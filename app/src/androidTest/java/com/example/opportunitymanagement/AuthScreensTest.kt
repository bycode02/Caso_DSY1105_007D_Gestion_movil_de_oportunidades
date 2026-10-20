package com.example.opportunitymanagement

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.opportunitymanagement.data.local.AppDatabase
import com.example.opportunitymanagement.data.repository.AuthRepository
import com.example.opportunitymanagement.ui.auth.LoginScreen
import com.example.opportunitymanagement.ui.auth.RegisterScreen
import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.viewmodel.LoginUiState
import com.example.opportunitymanagement.viewmodel.LoginViewModel
import com.example.opportunitymanagement.viewmodel.RegisterUiState
import com.example.opportunitymanagement.viewmodel.RegisterViewModel
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class AuthScreensTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var database: AppDatabase
    private lateinit var login: LoginViewModel
    private lateinit var register: RegisterViewModel

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java
        ).build()
        val repository = AuthRepository(database)
        login = LoginViewModel(repository)
        register = RegisterViewModel(repository)
    }

    @After
    fun cleanup() {
        login.viewModelScope.cancel()
        register.viewModelScope.cancel()
        database.close()
    }

    private fun screenshot(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "Evidencia/$name.png")
        file.parentFile!!.mkdirs()
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    private fun replace(label: String, value: String) {
        compose.onNodeWithText(label).performScrollTo().performTextReplacement(value)
    }

    @Test
    fun registrationAndLoginWorkWithoutProductionNavigation() {
        compose.setContent {
            var registering by remember { mutableStateOf(false) }
            val loginState by login.uiState.collectAsState()
            val registerState by register.uiState.collectAsState()
            Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
                if (registering) {
                    RegisterScreen(
                        registerState, register::onNameChanged, register::onEmailChanged,
                        register::onPasswordChanged, register::onConfirmPasswordChanged,
                        register::register, { registering = false }
                    )
                } else {
                    LoginScreen(
                        loginState, login::onEmailChanged, login::onPasswordChanged,
                        login::login, { registering = true }
                    )
                }
            }
        }
        screenshot("30_login")
        compose.onNodeWithText("Iniciar sesión").performScrollTo().performClick()
        compose.waitUntil(5000) { login.uiState.value.emailError != null }
        compose.waitUntil(5000) {
            compose.onAllNodesWithText("Ingresa tu correo electrónico", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Ingresa tu correo electrónico", useUnmergedTree = true).assertExists()
        screenshot("31_login_validation")
        compose.onNodeWithText("Crear cuenta").performScrollTo().performClick()
        compose.onNodeWithText("Cuenta de vendedor").assertExists()
        compose.onNodeWithText("ADMINISTRADOR").assertDoesNotExist()
        screenshot("32_register")
        compose.onNodeWithText("Crear cuenta").performScrollTo().performClick()
        compose.waitUntil(5000) {
            compose.onAllNodesWithText("Confirma tu contraseña", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Confirma tu contraseña", useUnmergedTree = true).assertExists()
        screenshot("33_register_validation")
        replace("Nombre completo", "Ana Pérez")
        replace("Correo electrónico", "ana@ejemplo.cl")
        replace("Contraseña", "Clave1234!")
        replace("Confirmar contraseña", "Clave1234!")
        // IME Done debe ejecutar la misma acción que el botón.
        compose.onNodeWithText("Confirmar contraseña").performImeAction()
        compose.waitUntil(15000) { register.uiState.value.registeredSuccessfully }
        assertEquals("", register.uiState.value.password)
        assertEquals("", register.uiState.value.confirmPassword)
        screenshot("34_register_success")
        compose.onNodeWithText("Ir a iniciar sesión").performClick()
        replace("Correo electrónico", "ana@ejemplo.cl")
        replace("Contraseña", "incorrecta")
        compose.onNodeWithText("Contraseña").performImeAction()
        compose.waitUntil(15000) { login.uiState.value.errorMessage != null }
        screenshot("35_login_error")
        replace("Contraseña", "Clave1234!")
        compose.onNodeWithText("Contraseña").performImeAction()
        compose.waitUntil(15000) { login.uiState.value.authenticatedUser != null }
        assertEquals("", login.uiState.value.password)
        screenshot("36_login_success")
    }

    @Test
    fun loadingDisablesSubmissionAndNavigation() {
        var calls = 0
        compose.setContent {
            Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
                LoginScreen(LoginUiState(isLoading = true), {}, {}, { calls++ }, { calls++ })
            }
        }
        compose.onNodeWithText("Ingresando…").assertIsNotEnabled()
        compose.onNodeWithText("Crear cuenta").assertIsNotEnabled()
        compose.onNodeWithText("Correo electrónico").assertIsNotEnabled()
        assertEquals(0, calls)
        screenshot("37_login_loading")
    }

    @Test
    fun passwordVisibilityCanBeToggled() {
        compose.setContent {
            Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
                LoginScreen(LoginUiState(password = "Clave1234!"), {}, {}, {}, {})
            }
        }
        compose.onNodeWithContentDescription("Mostrar Contraseña").performClick()
        compose.onNodeWithText("Contraseña").assertTextContains("Clave1234!")
        compose.onNodeWithContentDescription("Ocultar Contraseña").performClick()
        compose.onNodeWithText("Contraseña").assertTextContains("••••••••••")
    }

    @Test
    fun darkRegisterKeepsFieldsAndRoleNotice() {
        compose.setContent {
            Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(darkTheme = true, dynamicColor = false) {
                RegisterScreen(RegisterUiState(), {}, {}, {}, {}, {}, {})
            }
        }
        compose.onNodeWithText("Confirmar contraseña").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Cuenta de vendedor").performScrollTo().assertIsDisplayed()
        screenshot("38_register_dark")
    }

    @Test
    fun registrationValidatesMismatchAndBlocksDoubleSubmission() {
        compose.runOnIdle {
            register.onNameChanged("Ana")
            register.onEmailChanged("ana@ejemplo.cl")
            register.onPasswordChanged("Clave1234!")
            register.onConfirmPasswordChanged("otra")
            register.register()
            assertNotNull(register.uiState.value.confirmPasswordError)
            assertFalse(register.uiState.value.isSaving)
            register.onConfirmPasswordChanged("Clave1234!")
            register.register()
            register.register()
            assertTrue(register.uiState.value.isSaving)
        }
        compose.waitUntil(15000) { register.uiState.value.registeredSuccessfully }
        assertNull(register.uiState.value.emailError)
    }
}
