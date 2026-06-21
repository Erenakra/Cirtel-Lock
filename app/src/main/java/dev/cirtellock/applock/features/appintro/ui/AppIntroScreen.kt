package dev.cirtellock.applock.features.appintro.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import dev.cirtellock.appintro.AppIntro
import dev.cirtellock.appintro.IntroPage
import dev.cirtellock.applock.R
import dev.cirtellock.applock.core.navigation.Screen
import dev.cirtellock.applock.core.utils.appLockRepository
import dev.cirtellock.applock.core.utils.hasUsagePermission
import dev.cirtellock.applock.core.utils.isAccessibilityServiceEnabled
import dev.cirtellock.applock.core.utils.launchBatterySettings
import dev.cirtellock.applock.data.repository.BackendImplementation
import dev.cirtellock.applock.features.appintro.domain.AppIntroManager
import dev.cirtellock.applock.services.UsageLockService
import dev.cirtellock.applock.ui.icons.Accessibility
import dev.cirtellock.applock.ui.icons.BatterySaver
import dev.cirtellock.applock.ui.icons.Display

enum class AppUsageMethod {
    ACCESSIBILITY,
    USAGE_STATS,
}

@Composable
fun MethodSelectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.White.copy(alpha = 0.2f) else Color.White.copy(
                alpha = 0.1f,
            )
        ),
        border = if (isSelected) BorderStroke(2.dp, Color.White) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = { onClick() },
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.White,
                    unselectedColor = Color.White.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@SuppressLint("BatteryLife")
@Composable
fun AppIntroScreen(navController: NavController) {
    val context = LocalContext.current

    val allowDisplayOverOtherApps = stringResource(R.string.allow_display_over_other_apps)
    val allPermissionsRequired = stringResource(R.string.all_permissions_required)

    var selectedMethod by remember { mutableStateOf(AppUsageMethod.ACCESSIBILITY) }
    var overlayPermissionGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    var usageStatsPermissionGranted by remember { mutableStateOf(context.hasUsagePermission()) }
    var accessibilityServiceEnabled by remember { mutableStateOf(context.isAccessibilityServiceEnabled()) }

    // Dummy launcher as it was removed from usage but might be needed to keep structure if I don't want to re-indent too much.
    // Actually better to just remove it.


    LaunchedEffect(key1 = context) {
        overlayPermissionGranted = Settings.canDrawOverlays(context)
        accessibilityServiceEnabled = context.isAccessibilityServiceEnabled()
    }

    val onFinishCallback = {
        AppIntroManager.markIntroAsCompleted(context)
        navController.navigate(Screen.SetPassword.route) {
            popUpTo(Screen.AppIntro.route) { inclusive = true }
        }
    }

    val basicPages = listOf(
        IntroPage(
            title = stringResource(R.string.welc_applock),
            description = stringResource(R.string.welcome_desc),
            icon = Icons.Filled.Lock,
            backgroundColor = Color(0xFF0F52BA),
            contentColor = Color.White
        ) { true },
        IntroPage(
            title = stringResource(R.string.secure_apps),
            description = stringResource(R.string.secure_apps_desc),
            icon = Icons.Default.Lock,
            backgroundColor = Color(0xFF3C9401),
            contentColor = Color.White,
            onNext = { true }
        ),
        IntroPage(
            title = stringResource(R.string.display_over_apps),
            description = stringResource(R.string.display_over_apps_desc),
            icon = Display,
            backgroundColor = Color(0xFFDC143C),
            contentColor = Color.White,
            onNext = {
                overlayPermissionGranted = Settings.canDrawOverlays(context)
                if (!overlayPermissionGranted) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.data = "package:${context.packageName}".toUri()
                    Toast.makeText(
                        context,
                        allowDisplayOverOtherApps,
                        Toast.LENGTH_LONG
                    ).show()
                    context.startActivity(intent)
                    false
                } else {
                    true
                }
            }
        ),
        IntroPage(
            title = stringResource(R.string.disable_battery_optimization_title),
            description = stringResource(R.string.disable_battery_optimization_desc),
            icon = BatterySaver,
            backgroundColor = Color(0xFF08A471),
            contentColor = Color.White,
            onNext = {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                val isIgnoringOptimizations =
                    powerManager.isIgnoringBatteryOptimizations(context.packageName)
                if (!isIgnoringOptimizations) {
                    launchBatterySettings(context)
                    false
                } else {
                    true
                }
            }
        ),
    )

    val methodSelectionPage = IntroPage(
        title = stringResource(R.string.choose_app_detection),
        description = stringResource(R.string.app_detection_desc),
        icon = Icons.Default.Lock,
        backgroundColor = Color(0xFF6B46C1),
        contentColor = Color.White,
        customContent = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF6B46C1))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.choose_app_detection),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.app_detection_desc),
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))

                MethodSelectionCard(
                    title = stringResource(R.string.accessibility_service_title),
                    description = stringResource(R.string.accessibility_service_card_desc),
                    icon = Accessibility,
                    isSelected = selectedMethod == AppUsageMethod.ACCESSIBILITY,
                    onClick = { selectedMethod = AppUsageMethod.ACCESSIBILITY },
                )

                MethodSelectionCard(
                    title = stringResource(R.string.usage_stats_title),
                    description = stringResource(R.string.usage_stats_card_desc),
                    icon = Icons.Default.QueryStats,
                    isSelected = selectedMethod == AppUsageMethod.USAGE_STATS,
                    onClick = { selectedMethod = AppUsageMethod.USAGE_STATS },
                )


                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.can_change_later_in_settings),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        onNext = { true }
    )

    val methodSpecificPages = when (selectedMethod) {
        AppUsageMethod.ACCESSIBILITY -> listOf(
            IntroPage(
                title = stringResource(R.string.accessibility_service_title),
                description = stringResource(R.string.app_intro_accessibility_desc),
                icon = Accessibility,
                backgroundColor = Color(0xFFF1550E),
                contentColor = Color.White,
                onNext = {
                    accessibilityServiceEnabled = context.isAccessibilityServiceEnabled()
                    if (!accessibilityServiceEnabled) {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        false
                    } else {
                        context.appLockRepository()
                            .setBackendImplementation(BackendImplementation.ACCESSIBILITY)
                        true
                    }
                }
            )
        )

        AppUsageMethod.USAGE_STATS -> listOf(
            IntroPage(
                title = stringResource(R.string.app_intro_usage_stats_title),
                description = stringResource(R.string.app_intro_usage_stats_desc),
                icon = Icons.Default.QueryStats,
                backgroundColor = Color(0xFFB453A4),
                contentColor = Color.White,
                onNext = {
                    usageStatsPermissionGranted = context.hasUsagePermission()
                    if (!usageStatsPermissionGranted) {
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        false
                    } else {
                        context.appLockRepository()
                            .setBackendImplementation(BackendImplementation.USAGE_STATS)
                        context.startService(
                            Intent(context, UsageLockService::class.java)
                        )
                        true
                    }
                }
            )
        )
    }

    val finalPage = IntroPage(
        title = stringResource(R.string.app_intro_complete_privacy_title),
        description = stringResource(R.string.app_intro_complete_privacy_desc),
        icon = Icons.Default.Lock,
        backgroundColor = Color(0xFF0047AB),
        contentColor = Color.White,
        onNext = {
            overlayPermissionGranted = Settings.canDrawOverlays(context)

            val methodPermissionGranted = when (selectedMethod) {
                AppUsageMethod.ACCESSIBILITY -> context.isAccessibilityServiceEnabled()
                AppUsageMethod.USAGE_STATS -> context.hasUsagePermission()
            }

            // Only require all permissions if accessibility is selected
            val allPermissionsGranted = if (selectedMethod == AppUsageMethod.ACCESSIBILITY) {
                overlayPermissionGranted && methodPermissionGranted
            } else {
                overlayPermissionGranted && methodPermissionGranted
            }

            if (!allPermissionsGranted) {
                Toast.makeText(
                    context,
                    allPermissionsRequired,
                    Toast.LENGTH_SHORT
                ).show()
            }
            allPermissionsGranted
        }
    )

    val allPages =
        basicPages + methodSelectionPage + methodSpecificPages + finalPage

    AppIntro(
        pages = allPages,
        onSkip = {
            AppIntroManager.markIntroAsCompleted(context)
            navController.navigate(Screen.SetPassword.route) {
                popUpTo(Screen.AppIntro.route) { inclusive = true }
            }
        },
        onFinish = onFinishCallback,
        showSkipButton = false,
        useAnimatedPager = true,
        nextButtonText = stringResource(R.string.next_button),
        finishButtonText = stringResource(R.string.get_started_button)
    )
}
