package au.com.invitro.take5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import au.com.invitro.take5.ui.DetailScreen
import au.com.invitro.take5.ui.FormScreen
import au.com.invitro.take5.ui.HomeScreen
import au.com.invitro.take5.ui.IvtTheme
import au.com.invitro.take5.ui.Take5FormViewModel
import au.com.invitro.take5.util.EmailSender
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { IvtTheme { Take5App() } }
    }
}

private const val HOME = "home"
private const val FORM = "form"
private const val DETAIL = "detail:"

@Composable
private fun Take5App() {
    val context = LocalContext.current
    val vm: Take5FormViewModel = viewModel()
    val repository = vm.repository
    var screen by rememberSaveable { mutableStateOf(HOME) }
    var refreshKey by remember { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    when {
        screen == FORM -> {
            LaunchedEffect(Unit) { if (!vm.active) vm.startNew() }
            if (vm.active) {
                FormScreen(
                    vm = vm,
                    onDiscard = {
                        vm.discard()
                        screen = HOME
                    },
                    onCompleted = { record ->
                        EmailSender.send(context, record, repository.dirFor(record.id))
                        repository.save(record.copy(emailedAt = System.currentTimeMillis()))
                        vm.finish()
                        refreshKey++
                        screen = HOME
                        scope.launch {
                            snackbar.showSnackbar(
                                "Take 5 saved. Make sure you tapped SEND in your email app.",
                                duration = SnackbarDuration.Long,
                            )
                        }
                    },
                )
            }
        }

        screen.startsWith(DETAIL) -> DetailScreen(
            repository = repository,
            id = screen.removePrefix(DETAIL),
            onBack = { screen = HOME },
            onDeleted = {
                refreshKey++
                screen = HOME
            },
        )

        else -> HomeScreen(
            repository = repository,
            refreshKey = refreshKey,
            snackbar = snackbar,
            onNew = {
                vm.startNew()
                screen = FORM
            },
            onOpen = { screen = DETAIL + it },
        )
    }
}
