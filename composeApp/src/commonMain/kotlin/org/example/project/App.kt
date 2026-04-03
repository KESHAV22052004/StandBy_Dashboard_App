package org.example.project

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import kotlinx.datetime.*

// ─────────────────────────────────────────────────
//  TIME OF DAY
// ─────────────────────────────────────────────────

enum class TimeOfDay { MORNING, AFTERNOON, EVENING, NIGHT }

fun getTimeOfDay(hour: Int): TimeOfDay = when (hour) {
    in 5..11  -> TimeOfDay.MORNING
    in 12..16 -> TimeOfDay.AFTERNOON
    in 17..20 -> TimeOfDay.EVENING
    else      -> TimeOfDay.NIGHT
}

// ─────────────────────────────────────────────────
//  MUSIC INFO MODEL (no Android APIs — safe for commonMain)
// ─────────────────────────────────────────────────

data class MusicInfo(
    val title: String = "",
    val artist: String = "",
    val isPlaying: Boolean = false
)

val LocalMusicInfo = compositionLocalOf { MusicInfo() }

// ─────────────────────────────────────────────────
//  MAIN APP
// ─────────────────────────────────────────────────

@Composable
fun App() {
    val location = LocalLocationData.current
    val musicInfo = LocalMusicInfo.current

    var isAodMode by remember { mutableStateOf(false) }
    var currentHour by remember {
        mutableStateOf(
            Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).hour
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            currentHour = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).hour
        }
    }

    val timeOfDay = getTimeOfDay(currentHour)

    val bg1 by animateColorAsState(
        targetValue = when (timeOfDay) {
            TimeOfDay.MORNING   -> Color(0xFF1B4F72)
            TimeOfDay.AFTERNOON -> Color(0xFF1A1A2E)
            TimeOfDay.EVENING   -> Color(0xFF6E2C00)
            TimeOfDay.NIGHT     -> Color(0xFF0D0221)
        },
        animationSpec = tween(3000, easing = LinearEasing),
        label = "bg1"
    )

    val bg2 by animateColorAsState(
        targetValue = when (timeOfDay) {
            TimeOfDay.MORNING   -> Color(0xFF2471A3)
            TimeOfDay.AFTERNOON -> Color(0xFF16213E)
            TimeOfDay.EVENING   -> Color(0xFF7D3C98)
            TimeOfDay.NIGHT     -> Color(0xFF1A0533)
        },
        animationSpec = tween(3000, easing = LinearEasing),
        label = "bg2"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isAodMode) 0.25f else 1f,
        animationSpec = tween(800),
        label = "aodAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(bg1, bg2)))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { if (isAodMode) isAodMode = false }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentAlpha)
                .padding(if (isAodMode) 24.dp else 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                ClockPanel(isAodMode)
            }

            if (!isAodMode) {
                Spacer(modifier = Modifier.width(24.dp))
                WidgetGrid(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    musicInfo = musicInfo
                )
            }
        }

        // AOD Toggle
        Text(
            text = if (isAodMode) "🌕" else "🌙",
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .alpha(0.6f)
                .clickable { isAodMode = !isAodMode }
        )
    }
}

// ─────────────────────────────────────────────────
//  CLOCK PANEL
// ─────────────────────────────────────────────────

@Composable
fun ClockPanel(isAodMode: Boolean) {
    var time by remember { mutableStateOf(getCurrentTime()) }
    var date by remember { mutableStateOf(getCurrentDate()) }
    var colonVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            colonVisible = !colonVisible
            time = getCurrentTime()
            date = getCurrentDate()
        }
    }

    val colonAlpha by animateFloatAsState(
        targetValue = if (colonVisible) 1f else 0.05f,
        animationSpec = tween(150),
        label = "colonBlink"
    )

    val parts = time.split(":")
    val hours   = if (parts.isNotEmpty()) parts[0] else "00"
    val minutes = if (parts.size > 1) parts[1] else "00"
    val seconds = if (parts.size > 2) parts[2] else "00"

    Column {
        if (!isAodMode) {
            Text(
                text = date,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = hours,
                color = Color.White,
                fontSize = if (isAodMode) 60.sp else 86.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = ":",
                color = Color.White.copy(alpha = colonAlpha),
                fontSize = if (isAodMode) 60.sp else 86.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = minutes,
                color = Color.White,
                fontSize = if (isAodMode) 60.sp else 86.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (!isAodMode) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ":",
                    color = Color.White.copy(alpha = colonAlpha * 0.4f),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = seconds,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFFFF4444), RoundedCornerShape(3.dp))
                )
                Text(
                    text = "LIVE",
                    color = Color(0xFFFF4444),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────
//  WIDGET GRID
// ─────────────────────────────────────────────────

@Composable
fun WidgetGrid(
    modifier: Modifier = Modifier,
    musicInfo: MusicInfo
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) { WeatherWidget() }
            Box(modifier = Modifier.weight(1f)) { MusicWidget(musicInfo) }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) { CalendarWidget() }
            Box(modifier = Modifier.weight(1f)) { QuoteWidget() }
        }
    }
}

// ─────────────────────────────────────────────────
//  GLASS CARD
// ─────────────────────────────────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.06f)
                    )
                )
            )
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

// ─────────────────────────────────────────────────
//  WEATHER WIDGET
// ─────────────────────────────────────────────────

data class WeatherResult(
    val temperature: String = "--°C",
    val icon: String = "🌤️",
    val condition: String = "Loading..."
)

@Composable
fun WeatherWidget() {
    val location = LocalLocationData.current
    var weather by remember { mutableStateOf(WeatherResult()) }

    val alpha by animateFloatAsState(
        targetValue = if (weather.condition == "Loading...") 0.5f else 1f,
        animationSpec = tween(800),
        label = "weatherFade"
    )

    LaunchedEffect(location.lat, location.lng) {
        weather = fetchWeather(location.lat, location.lng)
    }

    GlassCard(modifier = Modifier.alpha(alpha)) {
        Text(
            text = "WEATHER",
            color = Color.White.copy(0.45f),
            fontSize = 9.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(text = weather.icon, fontSize = 30.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = weather.temperature,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = location.city,
            color = Color.White.copy(0.55f),
            fontSize = 10.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = weather.condition,
            color = Color.White.copy(0.55f),
            fontSize = 10.sp
        )
    }
}

// ─────────────────────────────────────────────────
//  MUSIC WIDGET
// ─────────────────────────────────────────────────

@Composable
fun MusicWidget(musicInfo: MusicInfo) {
    val fallbackSongs = remember {
        listOf(
            "Blinding Lights" to "The Weeknd",
            "Levitating" to "Dua Lipa",
            "Heat Waves" to "Glass Animals",
            "Stay" to "Kid LAROI",
            "As It Was" to "Harry Styles"
        )
    }

    var fallbackIndex by remember { mutableStateOf(0) }
    var localPlaying by remember { mutableStateOf(false) }

    // Use real music if playing, else show fallback
    val hasRealMusic = musicInfo.title.isNotEmpty()
    val displayTitle  = if (hasRealMusic) musicInfo.title
    else fallbackSongs[fallbackIndex].first
    val displayArtist = if (hasRealMusic) musicInfo.artist
    else fallbackSongs[fallbackIndex].second
    val isPlaying     = if (hasRealMusic) musicInfo.isPlaying
    else localPlaying

    LaunchedEffect(localPlaying) {
        if (localPlaying && !hasRealMusic) {
            while (true) {
                delay(8000)
                fallbackIndex = (fallbackIndex + 1) % fallbackSongs.size
            }
        }
    }

    GlassCard {
        Text(
            text = "NOW PLAYING",
            color = Color.White.copy(0.45f),
            fontSize = 9.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(text = "🎵", fontSize = 26.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = displayTitle,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = displayArtist,
            color = Color.White.copy(0.55f),
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏮",
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    if (!hasRealMusic) {
                        fallbackIndex = if (fallbackIndex == 0)
                            fallbackSongs.size - 1
                        else fallbackIndex - 1
                    }
                }
            )
            Text(
                text = if (isPlaying) "⏸" else "▶️",
                fontSize = 20.sp,
                modifier = Modifier.clickable {
                    if (!hasRealMusic) localPlaying = !localPlaying
                }
            )
            Text(
                text = "⏭",
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    if (!hasRealMusic) {
                        fallbackIndex = (fallbackIndex + 1) % fallbackSongs.size
                    }
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────
//  CALENDAR WIDGET
// ─────────────────────────────────────────────────

@Composable
fun CalendarWidget() {
    val now = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())

    val events = listOf(
        "09:00  Team Standup",
        "13:00  Code Review",
        "16:00  Project Demo"
    )

    GlassCard {
        Text(
            text = "CALENDAR",
            color = Color.White.copy(0.45f),
            fontSize = 9.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${now.dayOfMonth}",
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp
        )
        Text(
            text = "${getMonthName(now.monthNumber)} ${now.year}",
            color = Color.White.copy(0.6f),
            fontSize = 11.sp
        )
        Spacer(Modifier.height(6.dp))
        events.take(2).forEach { event ->
            Text(
                text = event,
                color = Color.White.copy(0.55f),
                fontSize = 9.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                textAlign = TextAlign.Start
            )
        }
    }
}

// ─────────────────────────────────────────────────
//  QUOTE WIDGET
// ─────────────────────────────────────────────────

@Composable
fun QuoteWidget() {
    val quotes = listOf(
        "Stay hungry,\nstay foolish." to "Steve Jobs",
        "First solve\nthe problem." to "J. Johnson",
        "Code is\npoetry." to "WordPress",
        "Make it work,\nthen make it fast." to "Unknown",
        "Simplicity is the\nsoul of efficiency." to "A. Freeman",
        "Build something\npeople want." to "Y Combinator"
    )

    var index by remember { mutableStateOf(0) }
    var visible by remember { mutableStateOf(true) }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label = "quoteFade"
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(7000)
            visible = false
            delay(500)
            index = (index + 1) % quotes.size
            visible = true
        }
    }

    GlassCard(modifier = Modifier.alpha(alpha)) {
        Text(
            text = "QUOTE",
            color = Color.White.copy(0.45f),
            fontSize = 9.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(text = "💭", fontSize = 24.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            text = quotes[index].first,
            color = Color.White.copy(0.9f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "— ${quotes[index].second}",
            color = Color.White.copy(0.4f),
            fontSize = 9.sp,
            fontStyle = FontStyle.Italic
        )
    }
}

// ─────────────────────────────────────────────────
//  WEATHER API
// ─────────────────────────────────────────────────

suspend fun fetchWeather(lat: Double, lng: Double): WeatherResult {
    return try {
        val client = HttpClient(CIO)
        val response = client.get(
            "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=$lat" +
                    "&longitude=$lng" +
                    "&current_weather=true"
        ).bodyAsText()
        client.close()

        val temp = """"temperature":([0-9.\-]+)""".toRegex()
            .find(response)?.groupValues?.get(1)
            ?.toDoubleOrNull() ?: 0.0

        val code = """"weathercode":([0-9]+)""".toRegex()
            .find(response)?.groupValues?.get(1)
            ?.toIntOrNull() ?: 0

        val (icon, condition) = when {
            code == 0      -> "☀️" to "Clear Sky"
            code in 1..3   -> "🌤️" to "Partly Cloudy"
            code in 45..48 -> "🌫️" to "Foggy"
            code in 51..67 -> "🌧️" to "Rainy"
            code in 71..77 -> "❄️" to "Snowy"
            code in 80..82 -> "🌦️" to "Showers"
            code in 95..99 -> "⛈️" to "Thunderstorm"
            else           -> "☁️" to "Cloudy"
        }

        WeatherResult("${temp}°C", icon, condition)
    } catch (e: Exception) {
        WeatherResult("--°C", "🌐", "Unavailable")
    }
}

// ─────────────────────────────────────────────────
//  HELPERS
// ─────────────────────────────────────────────────

fun getCurrentTime(): String {
    val now = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return "${now.hour.toString().padStart(2, '0')}:" +
            "${now.minute.toString().padStart(2, '0')}:" +
            now.second.toString().padStart(2, '0')
}

fun getCurrentDate(): String {
    val now = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    return "${days[now.dayOfWeek.ordinal]}, " +
            "${now.dayOfMonth} ${getMonthName(now.monthNumber)} ${now.year}"
}

fun getMonthName(month: Int) = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)[month - 1]