package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WifiCalling3
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.security.BiometricHelper
import com.example.ui.components.PinKeypadDialog
import com.example.ui.components.PinSetupDialog
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.JazzRuby
import com.example.ui.theme.JazzRubyContainer
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VaultEmerald
import com.example.ui.theme.ZongAmber
import com.example.ui.theme.ZongAmberContainer
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
    var showUssdDialog by remember { mutableStateOf(false) }
    var ussdTitle by remember { mutableStateOf("") }
    var ussdMessage by remember { mutableStateOf("") }
    var showStkUnavailableDialog by remember { mutableStateOf(false) }

    val activeCarrierName = if (selectedCarrierTab == 0) "Jazz" else "Zong 4G"
    val activeHelpline = if (selectedCarrierTab == 0) securityManager.jazzHelpline else securityManager.zongHelpline
    val activeCarrierColor = if (selectedCarrierTab == 0) JazzRuby else ZongAmber

    fun triggerVaultAuthentication() {
        if (!securityManager.isPinSetup) {
            showPinSetupDialog = true
            return
        }

        val activity = context as? FragmentActivity
        if (activity != null && securityManager.isBiometricEnabled && BiometricHelper.isBiometricAvailable(context)) {
            BiometricHelper.showBiometricPrompt(
                activity = activity,
                title = "$activeCarrierName Security Verification",
                subtitle = "Confirm biometric authentication to manage secure USIM services",
                onSuccess = {
                    showPostAuthSheet = true
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
                                .background(activeCarrierColor.copy(alpha = 0.2f)),
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
                                fontSize = 17.sp,
                                color = TextWhite
                            )
                            Text(
                                text = "Dual SIM Active • VoLTE Connected",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                },
                actions = {
                    // Discreet security lock icon in TopBar
                    IconButton(
                        onClick = { triggerVaultAuthentication() },
                        modifier = Modifier.testTag("carrier_security_entry_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "USIM Security",
                            tint = TextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceElevated)
            )
        },
        containerColor = DarkCarbon,
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
                containerColor = SurfaceElevated,
                contentColor = TextWhite,
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
                                    .background(if (selectedCarrierTab == 0) JazzRuby else TextMuted)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SIM 1: Jazz",
                                fontWeight = if (selectedCarrierTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCarrierTab == 0) TextWhite else TextMuted
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
                                    .background(if (selectedCarrierTab == 1) ZongAmber else TextMuted)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SIM 2: Zong 4G",
                                fontWeight = if (selectedCarrierTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCarrierTab == 1) TextWhite else TextMuted
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

                // Account Balance Card
                item {
                    if (selectedCarrierTab == 0) {
                        JazzAccountCard(
                            helpline = securityManager.jazzHelpline,
                            onCheckBalance = {
                                ussdTitle = "Jazz USSD Service"
                                ussdMessage = "Your prepaid balance is Rs. 248.50. Valid until 28 Nov 2026. Dial *111*1# for mini-statement."
                                showUssdDialog = true
                            },
                            onTapSecurity = { triggerVaultAuthentication() }
                        )
                    } else {
                        ZongAccountCard(
                            helpline = securityManager.zongHelpline,
                            onCheckBalance = {
                                ussdTitle = "Zong 4G USSD Service"
                                ussdMessage = "Your current account balance is Rs. 412.18. Valid until 04 Dec 2026. Dial *6464# for package details."
                                showUssdDialog = true
                            },
                            onTapSecurity = { triggerVaultAuthentication() }
                        )
                    }
                }

                // Active Bundle Usage Breakdown Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedCarrierTab == 0) "Super Duper Plus Bundle" else "Weekly Super Max Bundle",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "Auto-Renew ON",
                                    fontSize = 11.sp,
                                    color = activeCarrierColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Data Meter
                            val dataRemaining = if (selectedCarrierTab == 0) "11.4 GB / 15 GB" else "22.8 GB / 30 GB"
                            val dataFraction = if (selectedCarrierTab == 0) 0.76f else 0.76f
                            UsageMeterRow(
                                label = "4G High-Speed Data",
                                detail = dataRemaining,
                                fraction = dataFraction,
                                barColor = activeCarrierColor
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Minutes Meter
                            val minsRemaining = if (selectedCarrierTab == 0) "1,840 / 2,000 Mins" else "4,600 / 5,000 Mins"
                            UsageMeterRow(
                                label = if (selectedCarrierTab == 0) "On-Net Jazz Mins" else "On-Net Zong Mins",
                                detail = minsRemaining,
                                fraction = 0.92f,
                                barColor = VaultEmerald
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Off-Net Mins Meter
                            val offNet = if (selectedCarrierTab == 0) "124 / 150 Mins" else "88 / 100 Mins"
                            UsageMeterRow(
                                label = "Other Network Mins",
                                detail = offNet,
                                fraction = 0.84f,
                                barColor = Color(0xFF38BDF8)
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
                        color = TextDim,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionTile(
                            icon = Icons.Default.AccountBalanceWallet,
                            title = "Check Balance",
                            subtitle = if (selectedCarrierTab == 0) "*111#" else "*222#",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                ussdTitle = "$activeCarrierName Account Query"
                                ussdMessage = "Account Status: Active\nBalance: Rs. ${if (selectedCarrierTab == 0) "248.50" else "412.18"}\nExpiry: 30 days"
                                showUssdDialog = true
                            }
                        )

                        QuickActionTile(
                            icon = Icons.Default.Call,
                            title = "Helpline",
                            subtitle = "Dial $activeHelpline",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$activeHelpline"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionTile(
                            icon = Icons.Default.DataUsage,
                            title = "Data Packages",
                            subtitle = if (selectedCarrierTab == 0) "*443#" else "*6464#",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                ussdTitle = "$activeCarrierName Bundle Menu"
                                ussdMessage = "1. Daily 1GB Rs. 25\n2. Weekly Mega 10GB Rs. 280\n3. Monthly Super 30GB Rs. 950\nReply with bundle number to activate."
                                showUssdDialog = true
                            }
                        )

                        // Secret Gate Tile: Security & PIN Management (Triggers Biometric Prompt)
                        QuickActionTile(
                            icon = Icons.Default.Security,
                            title = "USIM Security",
                            subtitle = "Verify & Manage",
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

                // Radio Frequency & Telemetry Card
                item {
                    Text(
                        text = "NETWORK & CELL TELEMETRY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDim,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TelemetryRow("Carrier Network", if (selectedCarrierTab == 0) "Jazz Pakistan (PMCL)" else "Zong CMPak Ltd")
                            TelemetryRow("PLMN Code", if (selectedCarrierTab == 0) "410-01" else "410-04")
                            TelemetryRow("Signal Quality", "-89 dBm (Excellent, 4G+)")
                            TelemetryRow("LTE Frequency", "Band 3 (1800 MHz FDD)")
                            TelemetryRow("VoLTE Calling", "Enabled (AMR-WB Audio)")
                            TelemetryRow("SIM Hardware State", "Card Provisioned & Ready")
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }

    // USSD Response Modal (Authentic carrier popup)
    if (showUssdDialog) {
        AlertDialog(
            onDismissRequest = { showUssdDialog = false },
            title = { Text(ussdTitle, color = TextWhite, fontWeight = FontWeight.Bold) },
            text = { Text(ussdMessage, color = TextMuted, fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = { showUssdDialog = false }) {
                    Text("OK", color = activeCarrierColor, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceCard
        )
    }

    // PIN Keypad Dialog (Manual PIN fallback)
    if (showPinDialog) {
        PinKeypadDialog(
            securityManager = securityManager,
            onSuccess = {
                showPinDialog = false
                showPostAuthSheet = true
            },
            onDismiss = { showPinDialog = false },
            onTriggerBiometric = {
                val activity = context as? FragmentActivity
                if (activity != null) {
                    BiometricHelper.showBiometricPrompt(
                        activity = activity,
                        title = "$activeCarrierName Security Verification",
                        onSuccess = {
                            showPinDialog = false
                            showPostAuthSheet = true
                        },
                        onError = { /* fallback */ },
                        onFallbackToPin = { /* fallback */ }
                    )
                }
            }
        )
    }

    // PIN Setup Dialog
    if (showPinSetupDialog) {
        PinSetupDialog(
            securityManager = securityManager,
            onPinCreated = {
                showPinSetupDialog = false
                Toast.makeText(context, "Security PIN Initialized", Toast.LENGTH_SHORT).show()
                showPostAuthSheet = true
            },
            onDismiss = { showPinSetupDialog = false }
        )
    }

    // Post-Auth Action Sheet (The Secret Gateway)
    if (showPostAuthSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPostAuthSheet = false },
            sheetState = sheetState,
            containerColor = SurfaceElevated
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
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(VaultEmerald.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Authenticated",
                        tint = VaultEmerald,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "$activeCarrierName Security Hub",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Text(
                    text = "Biometric Verification Succeeded",
                    fontSize = 13.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(24.dp))

                // The Secret Action: Open Vault
                Button(
                    onClick = {
                        showPostAuthSheet = false
                        viewModel.unlockVault()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("open_vault_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VaultEmerald)
                ) {
                    Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, tint = DarkCarbon)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Open Private Media Safe",
                        color = DarkCarbon,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Carrier Action 1: Call Helpline
                OutlinedButton(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$activeHelpline"))
                        context.startActivity(dialIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = VaultEmerald)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Call Helpline ($activeHelpline)", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Carrier Action 2: Open SIM Toolkit
                OutlinedButton(
                    onClick = {
                        val pm = context.packageManager
                        val stkPackages = listOf("com.android.stk", "com.android.stk2", "com.google.android.stk")
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
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                ) {
                    Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = TextMuted)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Launch SIM Toolkit (STK)", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = { showPostAuthSheet = false }) {
                    Text("Lock Session", color = TextMuted)
                }
            }
        }
    }

    if (showStkUnavailableDialog) {
        AlertDialog(
            onDismissRequest = { showStkUnavailableDialog = false },
            title = { Text("SIM Toolkit Not Installed") },
            text = { Text("The hardware STK application is not available on this device's ROM. Carrier services can be reached via dialing the helpline.") },
            confirmButton = {
                TextButton(onClick = { showStkUnavailableDialog = false }) {
                    Text("OK", color = VaultEmerald)
                }
            }
        )
    }
}

@Composable
fun JazzAccountCard(
    helpline: String,
    onCheckBalance: () -> Unit,
    onTapSecurity: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("carrier_jazz_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
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
                            .background(JazzRubyContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.SimCard, contentDescription = null, tint = JazzRuby, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Jazz Pakistan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("+92 300 8472910 • Prepaid", fontSize = 12.sp, color = TextMuted)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(JazzRuby.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("LTE Ready", fontSize = 11.sp, color = JazzRuby, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Current Prepaid Balance", fontSize = 12.sp, color = TextMuted)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Rs. 248.50",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onCheckBalance,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Balance (*111#)", fontSize = 12.sp, color = TextWhite)
                    }

                    Button(
                        onClick = onTapSecurity,
                        colors = ButtonDefaults.buttonColors(containerColor = JazzRuby),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Security", fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ZongAccountCard(
    helpline: String,
    onCheckBalance: () -> Unit,
    onTapSecurity: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("carrier_zong_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
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
                            .background(ZongAmberContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.SimCard, contentDescription = null, tint = ZongAmber, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Zong 4G Pakistan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("+92 312 9182743 • Prepaid", fontSize = 12.sp, color = TextMuted)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ZongAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Super 4G", fontSize = 11.sp, color = ZongAmber, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Current Prepaid Balance", fontSize = 12.sp, color = TextMuted)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Rs. 412.18",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onCheckBalance,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Balance (*222#)", fontSize = 12.sp, color = TextWhite)
                    }

                    Button(
                        onClick = onTapSecurity,
                        colors = ButtonDefaults.buttonColors(containerColor = ZongAmber),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = DarkCarbon, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Security", fontSize = 12.sp, color = DarkCarbon, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun UsageMeterRow(label: String, detail: String, fraction: Float, barColor: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 13.sp, color = TextMuted)
            Text(text = detail, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = DarkCarbon
        )
    }
}

@Composable
fun QuickActionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color = VaultEmerald,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
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
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            Text(text = subtitle, fontSize = 12.sp, color = TextMuted)
        }
    }
}

@Composable
fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextMuted)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextWhite)
    }
}
