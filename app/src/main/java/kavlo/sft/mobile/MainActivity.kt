package kavlo.sft.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kavlo.sft.mobile.ui.theme.SmartHeadbandTheme
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartHeadbandTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmartHeadbandApp()
                }
            }
        }
    }
}

sealed class NavigationItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : NavigationItem("home", Icons.Default.Home, "Home")
    object Profile : NavigationItem("profile", Icons.Default.Person, "Profile")
    object Settings : NavigationItem("settings", Icons.Default.Settings, "Settings")
    object History : NavigationItem("history", Icons.Default.History, "History")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartHeadbandApp() {
    var selectedItem by remember { mutableStateOf(0) }
    var isConnected by remember { mutableStateOf(true) }
    var heartRate by remember { mutableStateOf(72) }
    var oxygenLevel by remember { mutableStateOf(98) }
    var isActiveActivity by remember { mutableStateOf(false) }
    var temperature by remember { mutableStateOf(36.5f) }
    var stressLevel by remember { mutableStateOf(25) }
    var batteryLevel by remember { mutableStateOf(85) }

    var userName by remember { mutableStateOf("John Doe") }
    var userAge by remember { mutableStateOf("25") }
    var userHeight by remember { mutableStateOf("175") }
    var userWeight by remember { mutableStateOf("70") }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            heartRate = if (isActiveActivity) {
                Random.nextInt(120, 160)
            } else {
                Random.nextInt(60, 90)
            }
            oxygenLevel = Random.nextInt(95, 100)
            isActiveActivity = Random.nextBoolean()
            temperature = Random.nextFloat() * 2 + 36f
            stressLevel = Random.nextInt(10, 80)
            batteryLevel = maxOf(0, batteryLevel - Random.nextInt(0, 2))
        }
    }

    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Profile,
        NavigationItem.Settings,
        NavigationItem.History
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E1E2E),
                contentColor = Color.White
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.title,
                                tint = if (selectedItem == index) Color(0xFFE94560) else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                item.title,
                                color = if (selectedItem == index) Color(0xFFE94560) else Color.Gray
                            )
                        },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE94560),
                            selectedTextColor = Color(0xFFE94560),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFFE94560).copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E),
                            Color(0xFF0F3460)
                        )
                    )
                )
        ) {
            when (selectedItem) {
                0 -> HomeScreen(
                    isConnected = isConnected,
                    heartRate = heartRate,
                    oxygenLevel = oxygenLevel,
                    isActiveActivity = isActiveActivity,
                    temperature = temperature,
                    stressLevel = stressLevel,
                    batteryLevel = batteryLevel
                )
                1 -> ProfileScreen(
                    userName = userName,
                    userAge = userAge,
                    userHeight = userHeight,
                    userWeight = userWeight,
                    onNameChange = { userName = it },
                    onAgeChange = { userAge = it },
                    onHeightChange = { userHeight = it },
                    onWeightChange = { userWeight = it }
                )
                2 -> SettingsScreen()
                3 -> HistoryScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isConnected: Boolean,
    heartRate: Int,
    oxygenLevel: Int,
    isActiveActivity: Boolean,
    temperature: Float,
    stressLevel: Int,
    batteryLevel: Int
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFE94560)
                    )
                    Text(
                        "KAVLO Mobile App",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            actions = {
                ConnectionIndicator(isConnected = isConnected)
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeartRateCard(
                        heartRate = heartRate,
                        modifier = Modifier.weight(1f)
                    )
                    OxygenCard(
                        oxygenLevel = oxygenLevel,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                ActivityCard(
                    isActive = isActiveActivity,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Temperature",
                        value = "${String.format("%.1f", temperature)}°C",
                        icon = Icons.Default.Thermostat,
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Stress Level",
                        value = "$stressLevel%",
                        icon = Icons.Default.Psychology,
                        color = Color(0xFF4ECDC4),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BatteryCard(
                        batteryLevel = batteryLevel,
                        modifier = Modifier.weight(1f)
                    )
                    DeviceInfoCard(
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String,
    userAge: String,
    userHeight: String,
    userWeight: String,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E2E).copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                Color(0xFFE94560).copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFFE94560),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = userName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Smart Headband User",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E2E).copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Personal Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = userName,
                        onValueChange = onNameChange,
                        label = { Text("Name", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFE94560),
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = userAge,
                        onValueChange = onAgeChange,
                        label = { Text("Age", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFE94560),
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = userHeight,
                        onValueChange = onHeightChange,
                        label = { Text("Height (cm)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFE94560),
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = userWeight,
                        onValueChange = onWeightChange,
                        label = { Text("Weight (kg)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFE94560),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E2E).copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Health Goals",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileGoalItem(
                        title = "Target Heart Rate",
                        value = "120-150 BPM",
                        icon = Icons.Default.Favorite,
                        color = Color(0xFFE94560)
                    )

                    ProfileGoalItem(
                        title = "Daily Activity Goal",
                        value = "60 minutes",
                        icon = Icons.Default.DirectionsRun,
                        color = Color(0xFFFFD93D)
                    )

                    ProfileGoalItem(
                        title = "Stress Management",
                        value = "Keep below 30%",
                        icon = Icons.Default.Psychology,
                        color = Color(0xFF4ECDC4)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileGoalItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.White
            )
            Text(
                text = value,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SettingsScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E2E).copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Device Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsItem(
                        title = "Bluetooth Connection",
                        subtitle = "Manage device connection",
                        icon = Icons.Default.Bluetooth,
                        color = Color(0xFF2196F3)
                    )

                    SettingsItem(
                        title = "Auto-sync",
                        subtitle = "Sync data automatically",
                        icon = Icons.Default.Sync,
                        color = Color(0xFF4CAF50)
                    )

                    SettingsItem(
                        title = "Battery Optimization",
                        subtitle = "Optimize battery usage",
                        icon = Icons.Default.Battery6Bar,
                        color = Color(0xFFFFD93D)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E2E).copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Notifications",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsItem(
                        title = "Health Alerts",
                        subtitle = "Get notified about health changes",
                        icon = Icons.Default.Notifications,
                        color = Color(0xFFE94560)
                    )

                    SettingsItem(
                        title = "Activity Reminders",
                        subtitle = "Reminders to stay active",
                        icon = Icons.Default.AccessAlarm,
                        color = Color(0xFFFF9800)
                    )

                    SettingsItem(
                        title = "Low Battery Warning",
                        subtitle = "Alert when battery is low",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFFF5722)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E2E).copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Privacy & Security",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsItem(
                        title = "Data Privacy",
                        subtitle = "Control your data sharing",
                        icon = Icons.Default.Security,
                        color = Color(0xFF9C27B0)
                    )

                    SettingsItem(
                        title = "Export Data",
                        subtitle = "Download your health data",
                        icon = Icons.Default.Download,
                        color = Color(0xFF607D8B)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun HistoryScreen() {
    val historyData = remember {
        listOf(
            HistoryEntry("Today", "Heart Rate: 72 BPM", "12:30 PM", Icons.Default.Favorite, Color(0xFFE94560)),
            HistoryEntry("Today", "SpO2: 98%", "12:00 PM", Icons.Default.Air, Color(0xFF4ECDC4)),
            HistoryEntry("Today", "Exercise: 30 min", "11:30 AM", Icons.Default.DirectionsRun, Color(0xFFFFD93D)),
            HistoryEntry("Today", "Stress: Low", "10:00 AM", Icons.Default.Psychology, Color(0xFF4ECDC4)),
            HistoryEntry("Yesterday", "Heart Rate: 75 BPM", "8:00 PM", Icons.Default.Favorite, Color(0xFFE94560)),
            HistoryEntry("Yesterday", "Temperature: 36.8°C", "7:30 PM", Icons.Default.Thermostat, Color(0xFFFF6B6B)),
            HistoryEntry("Yesterday", "Exercise: 45 min", "6:00 PM", Icons.Default.DirectionsRun, Color(0xFFFFD93D)),
            HistoryEntry("2 days ago", "Heart Rate: 68 BPM", "9:00 AM", Icons.Default.Favorite, Color(0xFFE94560)),
            HistoryEntry("2 days ago", "SpO2: 97%", "8:30 AM", Icons.Default.Air, Color(0xFF4ECDC4)),
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Health History",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(historyData.groupBy { it.date }.toList()) { (date, entries) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E2E).copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = date,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    entries.forEach { entry ->
                        HistoryItem(entry = entry)
                        if (entry != entries.last()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

data class HistoryEntry(
    val date: String,
    val description: String,
    val time: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun HistoryItem(entry: HistoryEntry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(entry.color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                entry.icon,
                contentDescription = null,
                tint = entry.color,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = entry.description,
                fontSize = 14.sp,
                color = Color.White
            )
            Text(
                text = entry.time,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ConnectionIndicator(isConnected: Boolean) {
    val color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFFF5722)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color.copy(alpha = alpha), CircleShape)
    )
}

@Composable
fun HeartRateCard(heartRate: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = Color(0xFFE94560),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$heartRate",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "BPM",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            HeartRateWave(heartRate = heartRate)
        }
    }
}

@Composable
fun HeartRateWave(heartRate: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2

        for (i in 0 until width.toInt() step 4) {
            val x = i.toFloat()
            val y = centerY + sin(phase + x * 0.02f) * 10f
            drawCircle(
                color = Color(0xFFE94560),
                radius = 1.5f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun OxygenCard(oxygenLevel: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Air,
                contentDescription = null,
                tint = Color(0xFF4ECDC4),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$oxygenLevel%",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "SpO2",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator(
                progress = { oxygenLevel / 100f },
                modifier = Modifier.size(40.dp),
                color = Color(0xFF4ECDC4),
                strokeWidth = 4.dp
            )
        }
    }
}

@Composable
fun ActivityCard(isActive: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isActive) Icons.Default.DirectionsRun else Icons.Default.SelfImprovement,
                contentDescription = null,
                tint = if (isActive) Color(0xFFFFD93D) else Color(0xFF4ECDC4),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (isActive) "Active Exercise" else "Resting",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (isActive) "High intensity detected" else "Low activity level",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun BatteryCard(batteryLevel: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Battery6Bar,
                contentDescription = null,
                tint = when {
                    batteryLevel > 50 -> Color(0xFF4CAF50)
                    batteryLevel > 20 -> Color(0xFFFFD93D)
                    else -> Color(0xFFE94560)
                },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$batteryLevel%",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Battery",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun DeviceInfoCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Headset,
                contentDescription = null,
                tint = Color(0xFF9C27B0),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SH-001",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Smart Headband",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SmartHeadbandAppPreview() {
    SmartHeadbandTheme {
        SmartHeadbandApp()
    }
}