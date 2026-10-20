package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// 1. Cover / SIM Service Screen (Always Dark, Ignores System Theme - Spec 7.2)
// =========================================================================
val CoverBackground = Color(0xFF000000)
val CoverSurface = Color(0xFF000000)
val CoverTopBar = Color(0xFF000000)
val CoverTextPrimary = Color(0xFFFFFFFF)
val CoverTextSecondary = Color(0xFF8E8E93)
val CoverChevron = Color(0xFFC7C7CC)
val CoverRowPressed = Color(0xFF141416)
val CoverHairline = Color(0xFF1C1C1E)

// Post-Auth Panel Sheet (Spec 31.1: #101012 with 28dp top corners)
val PostAuthSheetBackground = Color(0xFF101012)
val PostAuthSheetSurface = Color(0xFF1C1C1E)
val PostAuthTextMuted = Color(0xFFA1A1AA)

// =========================================================================
// 2. Vault Personality (Deep Cinematic Dark - Spec 7.2)
// =========================================================================
val VaultDarkBackground = Color(0xFF0B0D12)
val VaultSurfaceElevated = Color(0xFF14181F)
val VaultSurfaceHigh = Color(0xFF1C2230)
val VaultOutline = Color(0xFF262E3D)

// Primary Vault Accent: Refined Violet-to-Cyan Gradient (#7C5CFF -> #22D3EE)
val VaultViolet = Color(0xFF7C5CFF)
val VaultCyan = Color(0xFF22D3EE)
val VaultAccentBrush = Brush.linearGradient(listOf(VaultViolet, VaultCyan))

// Compatibility aliases for existing components
val DarkCarbon = VaultDarkBackground
val SurfaceElevated = VaultSurfaceElevated
val SurfaceCard = VaultSurfaceHigh
val SurfaceBorder = VaultOutline
val VaultEmerald = VaultViolet
val VaultEmeraldContainer = Color(0xFF2E1065)
val VaultEmeraldLight = VaultCyan

val TextWhite = Color(0xFFF8FAFC)
val TextMuted = Color(0xFF94A3B8)
val TextDim = Color(0xFF64748B)

val DangerRed = Color(0xFFEF4444)
val DangerRedContainer = Color(0xFF450A0A)
val SuccessGreen = Color(0xFF10B981)
