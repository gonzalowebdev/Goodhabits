package com.gonzalowebdev.goodhabits

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private val vm: AppVm by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.init(this)
        Notifier.ensureChannel(this)
        enableEdgeToEdge()
        setContent { Root(vm) }
    }
}

@Composable
private fun Root(vm: AppVm) {
    val st by Prefs.flow.collectAsState()
    val snap by vm.data.collectAsState()
    val ui = remember { Ui(vm) }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            try {
                val text = vm.exportJson()
                withContext(Dispatchers.IO) { ctx.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) } }
                Prefs.update { it.copy(lastBackup = System.currentTimeMillis()) }
                Toast.makeText(ctx, "Copia guardada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(ctx, "No se pudo guardar la copia", Toast.LENGTH_LONG).show()
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            try {
                val text = withContext(Dispatchers.IO) { ctx.contentResolver.openInputStream(uri)?.use { String(it.readBytes()) } ?: "" }
                vm.importJson(text)
                Toast.makeText(ctx, "Copia restaurada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(ctx, "El archivo no es una copia válida", Toast.LENGTH_LONG).show()
            }
        }
    }
    SideEffect {
        ui.askNotif = { if (Build.VERSION.SDK_INT >= 33) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
        ui.doExport = { exportLauncher.launch("good-habits-${LocalDate.now()}.json") }
        ui.doImport = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }
    }

    GhTheme(st.themeMode) {
        val dark = gh.dark
        val activity = ctx as ComponentActivity
        DisposableEffect(dark) {
            val style = if (dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
            activity.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            onDispose { }
        }
        BackHandler(enabled = ui.stack.size > 1) { ui.pop() }

        Box(Modifier.fillMaxSize().background(gh.bg)) {
            if (snap.loaded) {
                HomeScreen(ui, snap, st)
                ui.stack.drop(1).forEach { r ->
                    key(r) {
                        val vis = remember { MutableTransitionState(false).apply { targetState = true } }
                        AnimatedVisibility(
                            visibleState = vis,
                            enter = slideInHorizontally { it / 6 } + fadeIn()
                        ) {
                            Box(
                                Modifier.fillMaxSize().background(gh.bg).pointerInput(Unit) { detectTapGestures { } }
                            ) {
                                when (r) {
                                    is Route.Detail -> DetailScreen(ui, snap, st, r.id)
                                    is Route.Notes -> NotesScreen(ui, snap, r.id)
                                    is Route.Stats -> StatsScreen(ui, snap)
                                    is Route.Cfg -> SettingsScreen(ui, snap, st, r.page)
                                    is Route.Home -> {}
                                }
                            }
                        }
                    }
                }
            }
            ui.editor?.let { EditorSheet(ui, snap, it) }
            ui.log?.let { LogSheet(ui, snap, it, st) }
            ui.number?.let { NumberDialog(ui, snap, it) }
            ui.confirm?.let { ConfirmDialog(ui, it) }
        }
    }
}
