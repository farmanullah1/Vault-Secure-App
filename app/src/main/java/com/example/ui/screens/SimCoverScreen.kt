package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.R
import com.example.security.BiometricHelper
import com.example.ui.components.PinKeypadDialog
import com.example.ui.components.PinSetupDialog
import com.example.ui.theme.CoverBackground
import com.example.ui.theme.CoverChevron
import com.example.ui.theme.CoverHairline
import com.example.ui.theme.CoverRowPressed
import com.example.ui.theme.CoverTextPrimary
import com.example.ui.theme.CoverTextSecondary
import com.example.ui.theme.PostAuthSheetBackground
import com.example.ui.theme.PostAuthSheetSurface
import com.example.ui.theme.PostAuthTextMuted
import com.example.ui.theme.VaultAccentBrush
import com.example.ui.theme.VaultViolet
import com.example.viewmodel.Screen
import com.example.viewmodel.VaultViewModel

import androidx.compose.foundation.ExperimentalFoundationApi

/**
 * Cover screen "SIM Service" (the disguise) — exact layout conforming to Master Build Spec v3 (Section 8.1).
 * High-contrast pure black system-utility look.
 * Top bar with back arrow and bold title "SIM Service".
 * Two clean list rows ("Jazz" and "Zong") with SIM slot icons and chevrons in the upper-middle.
 * Completely empty black screen below. No menus, no overflow, no fake cards, no vault hints.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SimCoverScreen(
    viewModel: VaultViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val securityManager = viewModel.securityManager

    var showPinDialog by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showPostAuthSheet by remember { mutableStateOf(false) }
    var showStkUnavailableDialog by remember { mutableStateOf(false) }
    var activeCarrierName by remember { mutableStateOf("Jazz") }
    var activeHelpline by remember { mutableStateOf(securityManager.jazzHelpline) }

    fun performHapticTick() {
        if (securityManager.isHapticEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    fun launchDialer(number: String) {
        val encoded = Uri.encode(number)
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$encoded"))
        try {
            context.startActivity(dialIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "No dialer application found", Toast.LENGTH_SHORT).show()
        }
    }

    fun triggerAuthentication(carrier: String, helpline: String) {
        activeCarrierName = carrier
        activeHelpline = helpline
        performHapticTick()

        if (!securityManager.isPinSetup) {
            viewModel.navigateTo(Screen.PinEntry)
            return
        }

        val activity = context as? FragmentActivity
        if (activity != null && securityManager.isBiometricEnabled && BiometricHelper.isBiometricAvailable(context)) {
            BiometricHelper.showBiometricPrompt(
                activity = activity,
                title = context.getString(R.string.auth_confirm_title),
                subtitle = context.getString(R.string.auth_confirm_subtitle),
                onSuccess = {
                    if (securityManager.goStraightToVault) {
                        viewModel.unlockVault()
                    } else {
                        showPostAuthSheet = true
                    }
                },
                onError = {
                    viewModel.navigateTo(Screen.PinEntry)
                },
                onFallbackToPin = {
                    viewModel.navigateTo(Screen.PinEntry)
                }
            )
        } else {
            viewModel.navigateTo(Screen.PinEntry)
        }
    }

    // Spec 8.1 Entry Animation: 150 ms fade-in with 4 dp upward settle
    val alphaAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(4f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(150))
    }
    LaunchedEffect(Unit) {
        offsetYAnim.animateTo(0f, animationSpec = tween(150))
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("sim_cover_screen"),
        color = CoverBackground // Always pure black #000000 (Spec 7.2 & 8.1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // =========================================================================
            // Top Navigation Bar (Spec 8.1: Height 56 dp, edge-to-edge #000000, no elevation, no divider)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(start = 4.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left-pointing back arrow (24 dp, white, 48 dp touch target, closes screen without auth)
                IconButton(
                    onClick = {
                        performHapticTick()
                        (context as? Activity)?.finish()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("cover_back_button")
                        .semantics {
                            role = Role.Button
                            contentDescription = "Back"
                        }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back_action),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Title "SIM Service": bold (700), clean sans-serif, 20 sp, white, left-aligned, not centered
                Text(
                    text = stringResource(R.string.sim_service_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier
                        .testTag("cover_title")
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                // Optional alternate unlock: 2-second long-press on title text (Spec 8.1)
                                triggerAuthentication("Jazz", securityManager.jazzHelpline)
                            }
                        )
                )
            }

            // =========================================================================
            // Menu List (Upper-middle of screen: ~20-25% screen height below top bar)
            // =========================================================================
            Spacer(modifier = Modifier.weight(0.22f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetYAnim.value.dp)
                    .alpha(alphaAnim.value)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: SIM 1 Jazz
                SimCarrierRow(
                    carrierName = stringResource(R.string.sim_1_jazz),
                    simIconRes = R.drawable.ic_sim_slot_1,
                    talkBackDescription = stringResource(R.string.sim_1_talkback),
                    testTag = "carrier_row_jazz",
                    onClick = {
                        triggerAuthentication("Jazz", securityManager.jazzHelpline)
                    }
                )

                // Hairline divider between rows (#1C1C1E)
                HorizontalDivider(
                    thickness = 0.8.dp,
                    color = CoverHairline,
                    modifier = Modifier.padding(start = 52.dp, end = 4.dp)
                )

                // Row 2: SIM 2 Zong
                SimCarrierRow(
                    carrierName = stringResource(R.string.sim_2_zong),
                    simIconRes = R.drawable.ic_sim_slot_2,
                    talkBackDescription = stringResource(R.string.sim_2_talkback),
                    testTag = "carrier_row_zong",
                    onClick = {
                        triggerAuthentication("Zong", securityManager.zongHelpline)
                    }
                )

                // Hairline divider between rows (#1C1C1E)
                HorizontalDivider(
                    thickness = 0.8.dp,
                    color = CoverHairline,
                    modifier = Modifier.padding(start = 52.dp, end = 4.dp)
                )

                // Row 3: SIM Toolkit Services
                SimCarrierRow(
                    carrierName = "SIM Toolkit",
                    simIconRes = R.drawable.ic_sim_slot_1,
                    talkBackDescription = "SIM Toolkit Services",
                    testTag = "carrier_row_stk",
                    onClick = {
                        triggerAuthentication("SIM Toolkit", securityManager.jazzHelpline)
                    }
                )

                // Hairline divider between rows (#1C1C1E)
                HorizontalDivider(
                    thickness = 0.8.dp,
                    color = CoverHairline,
                    modifier = Modifier.padding(start = 52.dp, end = 4.dp)
                )

                // Row 4: Customer Care & Services
                SimCarrierRow(
                    carrierName = "Customer Care & Helplines",
                    simIconRes = R.drawable.ic_sim_slot_2,
                    talkBackDescription = "Customer Care & Helplines",
                    testTag = "carrier_row_helpline",
                    onClick = {
                        triggerAuthentication("Customer Care", securityManager.jazzHelpline)
                    }
                )
            }

            // =========================================================================
            // Everything below the list: completely black (#000000) and empty (Spec 8.1)
            // =========================================================================
            Spacer(modifier = Modifier.weight(0.78f))
        }
    }

    // =========================================================================
    // PIN Keypad Dialog
    // =========================================================================
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
                        title = context.getString(R.string.auth_confirm_title),
                        subtitle = context.getString(R.string.auth_confirm_subtitle),
                        onSuccess = {
                            showPinDialog = false
                            if (securityManager.goStraightToVault) {
                                viewModel.unlockVault()
                            } else {
                                showPostAuthSheet = true
                            }
                        },
                        onError = { /* fallback to keypad */ },
                        onFallbackToPin = { /* stays on keypad */ }
                    )
                }
            }
        )
    }

    // =========================================================================
    // PIN Setup Dialog (First launch setup)
    // =========================================================================
    if (showPinSetupDialog) {
        PinSetupDialog(
            securityManager = securityManager,
            onPinCreated = {
                showPinSetupDialog = false
                Toast.makeText(context, "PIN Created", Toast.LENGTH_SHORT).show()
                if (securityManager.goStraightToVault) {
                    viewModel.unlockVault()
                } else {
                    showPostAuthSheet = true
                }
            },
            onDismiss = { showPinSetupDialog = false }
        )
    }

    // =========================================================================
    // Post-Auth Panel (Spec 8.2 & 31.1: Black bottom sheet #101012, 28dp top corners)
    // =========================================================================
    if (showPostAuthSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPostAuthSheet = false },
            sheetState = sheetState,
            containerColor = PostAuthSheetBackground,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(CoverHairline)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .testTag("post_auth_sheet"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = activeCarrierName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = stringResource(R.string.identity_confirmed),
                    fontSize = 13.sp,
                    color = PostAuthTextMuted,
                    modifier = Modifier.padding(top = 2.dp, bottom = 24.dp)
                )

                // Action 1: Call Helpline (dialer opens with pre-filled number; never auto-dials)
                OutlinedButton(
                    onClick = {
                        showPostAuthSheet = false
                        launchDialer(activeHelpline)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = PostAuthSheetSurface,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = VaultViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.call_helpline, activeHelpline),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action 2: Open SIM Toolkit (launchable only if installed, never fake)
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
                                    showPostAuthSheet = false
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
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = PostAuthSheetSurface,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = PostAuthTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.open_sim_toolkit),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action 3: Open Vault (Primary, Refined violet-to-cyan gradient button)
                Button(
                    onClick = {
                        showPostAuthSheet = false
                        viewModel.unlockVault()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(VaultAccentBrush)
                        .testTag("open_vault_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.open_vault),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { showPostAuthSheet = false },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.close),
                        color = PostAuthTextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // SIM Toolkit Not Available Dialog (Truthful error message per Spec 10.3)
    if (showStkUnavailableDialog) {
        AlertDialog(
            onDismissRequest = { showStkUnavailableDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.stk_unavailable_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.stk_unavailable_desc),
                    color = PostAuthTextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showStkUnavailableDialog = false }) {
                    Text(stringResource(R.string.ok), color = VaultViolet, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = PostAuthSheetBackground
        )
    }
}

/**
 * Clean SIM Carrier Row matching Section 8.1:
 * Row height 64 dp, 20 × 24 dp SIM icon with slot numeral, 16 dp gap,
 * 17 sp medium weight text in white, light gray chevron "›" on far right.
 */
@Composable
private fun SimCarrierRow(
    carrierName: String,
    simIconRes: Int,
    talkBackDescription: String,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPressed) CoverRowPressed else Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp)
            .testTag(testTag)
            .semantics {
                role = Role.Button
                contentDescription = talkBackDescription
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // SIM Card Icon with numeral in bottom-right corner (20 x 24 dp)
        Icon(
            painter = painterResource(id = simIconRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(width = 20.dp, height = 24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Carrier label (17 sp, medium weight, white)
        Text(
            text = carrierName,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        // Light-gray chevron "›" (#C7C7CC, 20 dp, right-pointing)
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = CoverChevron,
            modifier = Modifier.size(20.dp)
        )
    }
}
