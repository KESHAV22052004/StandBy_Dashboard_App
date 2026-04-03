import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import org.example.project.App
import org.example.project.LocalLocationData
import org.example.project.LocationData

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "StandBy Dashboard",
        state = WindowState(size = DpSize(1280.dp, 720.dp)),
        resizable = true
    ) {
        // Desktop uses default Delhi location
        // (No GPS on desktop — can be changed manually)
        CompositionLocalProvider(
            LocalLocationData provides LocationData(28.61, 77.20, "Delhi")
        ) {
            App()
        }
    }
}