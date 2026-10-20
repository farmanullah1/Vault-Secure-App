package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.security.BiometricHelper
import com.example.ui.components.PinKeypadDialog
import com.example.ui.components.PinSetupDialog
import com.example.ui.theme.CoverAccentBlue
import com.example.ui.theme.CoverBackground
import com.example.ui.theme.CoverSurface
import com.example.ui.theme.CoverSurfaceBorder
import com.example.ui.theme.CoverTextPrimary
import com.example.ui.theme.CoverTextSecondary
import com.example.ui.theme.JazzGradientStart
import com.example.ui.theme.VaultAccentBrush
import com.example.ui.theme.VaultEmerald
import com.example.ui.theme.ZongGradientStart
import com.example.viewmodel.VaultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimCoverScreen(
    viewModel: VaultViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val securityManager = viewModel.securityManager

    var selectedCarrierTab by remember { mutableIntStateOf(0) } // 0: Jazz, 1: Zong
    var showPinDialog by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showPostAuthSheet by remember { mutableStateOf(false) }
    var showStkUnavailableDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    val activeCarrierName = if (selectedCarrierTab == 0) "Jazz" else "Zong 4G"
    val activeHelpline = if (selectedCarrierTab == 0) securityManager.jazzHelpline else securityManager.zongHelpline
    val activeCarrierColor = if (selectedCarrierTab == 0) JazzGradientStart else ZongGradientStart

    fun launchDialer(ussdOrNumber: String) {
        val encoded = Uri.encode(ussdOrNumber)
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$encoded"))
        try {
            context.startActivity(dialIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "No dialer application found", Toast.LENGTH_SHORT).show()
        }
    }

    fun triggerVaultAuthentication() {
        if (!securityManager.isPinSetup) {
            showPinSetupDialog = true
            return
        }

        val activity = context as? FragmentActivity
        if (activity != null && securityManager.isBiometricEnabled && BiometricHelper.isBiometricAvailable(context)) {
            BiometricHelper.showBiometricPrompt(
                activity = activity,
                title = "Security Verification",
                subtitle = "Authenticate to access secure services",
                onSuccess = {
                    if (securityManager.goStraightToVault) {
                        viewModel.unlockVault()
                    } else {
                        showPostAuthSheet = true
                    }
                },
                onError = {
                    showPinDialog = true
                },
                onFallbackToPin = {
                    showPinDialog = true
                }
            )
        } else {
            showPinDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(activeCarrierColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SimCard,
                                contentDescription = "SIM Card",
                                tint = activeCarrierColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SIM Services Manager",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = CoverTextPrimary
                            )
                            Text(
                                text = "Dual SIM Active • VoLTE Connected",
                                fontSize = 11.sp,
                                color = CoverTextSecondary
                            )
                        }
                    }
                },
                actions = {
                    // Discreet security lock icon for quick unlock
                    IconButton(
                        onClick = { triggerVaultAuthentication() },
                        modifier = Modifier.testTag("carrier_security_entry_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "USIM Security",
                            tint = CoverTextSecondary
                        )
                    }

                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = CoverTextSecondary
                            )
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false },
                            modifier = Modifier.background(CoverSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("About SIM Services", color = CoverTextPrimary) },
                                onClick = {
                                    showOverflowMenu = false
                                    showAboutDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Help & Support", color = CoverTextPrimary) },
                                onClick = {
                                    showOverflowMenu = false
                                    showHelpDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CoverSurface)
            )
        },
        containerColor = CoverBackground,
        modifier = modifier.testTag("sim_cover_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Carrier Switcher Tabs
            TabRow(
                selectedTabIndex = selectedCarrierTab,
                containerColor = CoverSurface,
                contentColor = CoverTextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedCarrierTab]),
                        color = activeCarrierColor
                    )
                }
            ) {
                Tab(
                    selected = selectedCarrierTab == 0,
                    onClick = { selectedCarrierTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedCarrierTab == 0) JazzGradientStart else CoverTextSecondary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SIM 1: Jazz",
                                fontWeight = if (selectedCarrierTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCarrierTab == 0) CoverTextPrimary else CoverTextSecondary
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_jazz")
                )

                Tab(
                    selected = selectedCarrierTab == 1,
                    onClick = { selectedCarrierTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedCarrierTab == 1) ZongGradientStart else CoverTextSecondary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SIM 2: Zong 4G",
                                fontWeight = if (selectedCarrierTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCarrierTab == 1) CoverTextPrimary else CoverTextSecondary
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_zong")
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(6.dp)) }

                // Realistic Carrier Account Balance Card
                item {
                    CarrierAccountBalanceCard(
                        carrierTitle = if (selectedCarrierTab == 0) "Jazz Pakistan" else "Zong 4G Pakistan",
                        phoneNumber = if (selectedCarrierTab == 0) "+92 300 8472910" else "+92 312 9182743",
                        balance = if (selectedCarrierTab == 0) "Rs. 248.50" else "Rs. 412.18",
                        badgeLabel = if (selectedCarrierTab == 0) "LTE Ready" else "Super 4G",
                        ussdCode = if (selectedCarrierTab == 0) "*111#" else "*222#",
                        accentColor = activeCarrierColor,
                        onCheckBalance = {
                            launchDialer(if (selectedCarrierTab == 0) "*111#" else "*222#")
                        },
                        onTapSecurity = { triggerVaultAuthentication() }
                    )
                }

                // Active Bundle Usage Breakdown Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CoverSurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CoverSurfaceBorder)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedCarrierTab == 0) "Super Duper Plus Bundle" else "Weekly Super Max Bundle",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CoverTextPrimary
                                )
                                Text(
                                    text = "Auto-Renew ON",
                                    fontSize = 11.sp,
                                    color = activeCarrierColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 4G Data Meter
                            val dataRemaining = if (selectedCarrierTab == 0) "11.4 GB / 15 GB" else "22.8 GB / 30 GB"
                            CarrierUsageMeterRow(
                                label = "4G High-Speed Data",
                                detail = dataRemaining,
                                fraction = 0.76f,
                                barColor = activeCarrierColor
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Minutes Meter
                            val minsRemaining = if (selectedCarrierTab == 0) "1,840 / 2,000 Mins" else "4,600 / 5,000 Mins"
                            CarrierUsageMeterRow(
                                label = if (selectedCarrierTab == 0) "On-Net Jazz Mins" else "On-Net Zong Mins",
                                detail = minsRemaining,
                                fraction = 0.92f,
                                barColor = VaultEmerald
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Off-Net Minutes
                            val offNet = if (selectedCarrierTab == 0) "124 / 150 Mins" else "88 / 100 Mins"
                            CarrierUsageMeterRow(
                                label = "Other Network Mins",
                                detail = offNet,
                                fraction = 0.84f,
                                barColor = CoverAccentBlue
                            )
                        }
                    }
                }

                // Interactive Quick Actions Grid
                item {
                    Text(
                        text = "CARRIER ACTIONS & SERVICES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoverTextSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CarrierActionTile(
                            icon = Icons.Default.AccountBalanceWallet,
                            title = "Check Balance",
                            subtitle = if (selectedCarrierTab == 0) "*111#" else "*222#",
                            accentColor = activeCarrierColor,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                launchDialer(if (selectedCarrierTab == 0) "*111#" else "*222#")
                            }
                        )

                        CarrierActionTile(
                            icon = Icons.Default.Call,
                            title = "Helpline",
                            subtitle = "Dial $activeHelpline",
                            accentColor = CoverAccentBlue,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                launchDialer(activeHelpline)
                            }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CarrierActionTile(
                            icon = Icons.Default.DataUsage,
                            title = "Data Packages",
                            subtitle = if (selectedCarrierTab == 0) "*443#" else "*6464#",
                            accentColor = CoverAccentBlue,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                launchDialer(if (selectedCarrierTab == 0) "*443#" else "*6464#")
                            }
                        )

                        // Secret Gate Tile: Intercepts and triggers PIN verification!
                        CarrierActionTile(
                            icon = Icons.Default.Security,
                            title = "USIM Security",
                            subtitle = "PIN & Access",
                            accentColor = activeCarrierColor,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("carrier_security_tile"),
                            onClick = {
                                triggerVaultAuthentication()
                            }
                        )
                    }
                }

                // Network & Cell Telemetry Card
                item {
                    Text(
                        text = "NETWORK & CELL TELEMETRY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoverTextSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CoverSurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CoverSurfaceBorder)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CarrierTelemetryRow("Carrier Network", if (selectedCarrierTab == 0) "Jazz Pakistan (PMCL)" else "Zong CMPak Ltd")
                            CarrierTelemetryRow("PLMN Code", if (selectedCarrierTab == 0) "410-01" else "410-04")
                            CarrierTelemetryRow("Signal Quality", "-89 dBm (Excellent, 4G+)")
                            CarrierTelemetryRow("LTE Frequency", "Band 3 (1800 MHz FDD)")
                            CarrierTelemetryRow("VoLTE Calling", "Enabled (AMR-WB Audio)")
                            CarrierTelemetryRow("SIM Hardware State", "Card Provisioned & Ready")
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    // Generic About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("SIM Services Manager", color = CoverTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "A dual-SIM utility providing convenient shortcuts to carrier customer care helplines, balance queries, and hardware USIM status.\n\nVersion 1.0",
                    color = CoverTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = CoverAccentBlue, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = CoverSurface
        )
    }

    // Generic Help Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("SIM Services Help", color = CoverTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Select your SIM card tab to view active bundle info or trigger balance checks. Calls will open pre-filled in your phone's dialer.",
                    color = CoverTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("OK", color = CoverAccentBlue, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = CoverSurface
        )
    }

    // PIN Keypad Dialog (Intercepts upon trigger)
    if (showPinDialog) {
        PinKeypadDialog(
            securityManager = securityManager,
            onSuccess = {
                showPinDialog = false
                if (securityManager.goStraightToVault) {
                    viewModel.unlockVault()
                } else {
                    showPostAuthSheet = true
                }
            },
            onDismiss = { showPinDialog = false },
            onTriggerBiometric = {
                val activity = context as? FragmentActivity
                if (activity != null) {
                    BiometricHelper.showBiometricPrompt(
                        activity = activity,
                        title = "Security Verification",
                        onSuccess = {
                            showPinDialog = false
                            if (securityManager.goStraightToVault) {
                                viewModel.unlockVault()
                            } else {
                                showPostAuthSheet = true
                            }
                        },
                        onError = { /* fallback */ },
                        onFallbackToPin = { /* fallback */ }
                    )
                }
            }
        )
    }

    // PIN Setup Dialog (Initial Launch Setup)
    if (showPinSetupDialog) {
        PinSetupDialog(
            securityManager = securityManager,
            onPinCreated = {
                showPinSetupDialog = false
                Toast.makeText(context, "Security PIN Created", Toast.LENGTH_SHORT).show()
                if (securityManager.goStraightToVault) {
                    viewModel.unlockVault()
                } else {
                    showPostAuthSheet = true
                }
            },
            onDismiss = { showPinSetupDialog = false }
        )
    }

    // Post-Auth Hub (Spec 8.2)
    if (showPostAuthSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPostAuthSheet = false },
            sheetState = sheetState,
            containerColor = CoverSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .testTag("post_auth_sheet"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(CoverAccentBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = CoverAccentBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "$activeCarrierName Security Hub",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = CoverTextPrimary
                )

                Text(
                    text = "Identity Confirmed",
                    fontSize = 13.sp,
                    color = CoverTextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Core App Gateway: Open Private Safe
                Button(
                    onClick = {
                        showPostAuthSheet = false
                        viewModel.unlockVault()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(VaultAccentBrush)
                        .testTag("open_vault_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Open Private Safe",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action 1: Call Helpline
                OutlinedButton(
                    onClick = {
                        launchDialer(activeHelpline)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoverTextPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = CoverAccentBlue)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Call Helpline ($activeHelpline)", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action 2: Open SIM Toolkit
                OutlinedButton(
                    onClick = {
                        val pm = context.packageManager
                        val stkPackages = listOf(
                            "com.android.stk",
                            "com.android.stk2",
                            "com.google.android.stk",
                            "com.sec.android.app.servicemodeapp"
                        )
                        var launched = false
                        for (pkg in stkPackages) {
                            val intent = pm.getLaunchIntentForPackage(pkg)
                            if (intent != null) {
                                try {
                                    context.startActivity(intent)
                                    launched = true
                                    break
                                } catch (e: Exception) {
                                    // ignore
                                }
                            }
                        }
                        if (!launched) {
                            showStkUnavailableDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoverTextPrimary)
                ) {
                    Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = CoverTextSecondary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Open SIM Toolkit (STK)", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(onClick = { showPostAuthSheet = false }) {
                    Text("Close", color = CoverTextSecondary)
                }
            }
        }
    }

    if (showStkUnavailableDialog) {
        AlertDialog(
            onDismissRequest = { showStkUnavailableDialog = false },
            title = { Text("SIM Toolkit Not Available", color = CoverTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "The SIM Toolkit (STK) application is not available on this device's ROM. Carrier services can be reached directly via dialing the helpline.",
                    color = CoverTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showStkUnavailableDialog = false }) {
                    Text("OK", color = CoverAccentBlue)
                }
            },
            containerColor = CoverSurface
        )
    }
}

@Composable
fun CarrierAccountBalanceCard(
    carrierTitle: String,
    phoneNumber: String,
    balance: String,
    badgeLabel: String,
    ussdCode: String,
    accentColor: Color,
    onCheckBalance: () -> Unit,
    onTapSecurity: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("carrier_account_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CoverSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CoverSurfaceBorder)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.SimCard, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(carrierTitle, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = CoverTextPrimary)
                        Text("$phoneNumber • Prepaid", fontSize = 12.sp, color = CoverTextSecondary)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(badgeLabel, fontSize = 11.sp, color = accentColor, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Current Prepaid Balance", fontSize = 12.sp, color = CoverTextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = balance,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CoverTextPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onCheckBalance,
                        colors = ButtonDefaults.buttonColors(containerColor = CoverBackground),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(ussdCode, fontSize = 12.sp, color = CoverTextPrimary, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = onTapSecurity,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Security", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CarrierUsageMeterRow(label: String, detail: String, fraction: Float, barColor: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 13.sp, color = CoverTextSecondary)
            Text(text = detail, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CoverTextPrimary)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = CoverBackground
        )
    }
}

@Composable
fun CarrierActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CoverSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CoverSurfaceBorder)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CoverTextPrimary)
            Text(text = subtitle, fontSize = 12.sp, color = CoverTextSecondary)
        }
    }
}

@Composable
fun CarrierTelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = CoverTextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = CoverTextPrimary)
    }
}
