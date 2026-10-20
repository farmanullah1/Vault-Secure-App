package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// 1. Cover / SIM Disguise Personality (Light)
// ==========================================
val CoverBackground = Color(0xFFF6F8FB)
val CoverSurface = Color(0xFFFFFFFF)
val CoverSurfaceBorder = Color(0xFFE2E8F0)
val CoverTextPrimary = Color(0xFF0F172A)
val CoverTextSecondary = Color(0xFF64748B)
val CoverAccentBlue = Color(0xFF2563EB)

// Generic Own-Designed Gradients for Jazz and Zong (Non-proprietary)
val JazzGradientStart = Color(0xFFEA580C) // Warm orange-red
val JazzGradientEnd = Color(0xFFC2410C)
val JazzBrush = Brush.linearGradient(listOf(JazzGradientStart, JazzGradientEnd))

val ZongGradientStart = Color(0xFF0D9488) // Cool teal
val ZongGradientEnd = Color(0xFF047857)
val ZongBrush = Brush.linearGradient(listOf(ZongGradientStart, ZongGradientEnd))

// ==========================================
// 2. Vault Personality (Deep Cinematic Dark)
// ==========================================
val VaultDarkBackground = Color(0xFF0B0D12)
val VaultSurfaceElevated = Color(0xFF14181F)
val VaultSurfaceHigh = Color(0xFF1C2230)
val VaultOutline = Color(0xFF262E3D)

// Primary Vault Accent: Refined Violet-to-Cyan Gradient
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

val JazzRuby = JazzGradientStart
val JazzRubyContainer = Color(0xFFFFEDD5)
val ZongAmber = ZongGradientStart
val ZongAmberContainer = Color(0xFFCCFBF1)

val TextWhite = Color(0xFFF8FAFC)
val TextMuted = Color(0xFF94A3B8)
val TextDim = Color(0xFF64748B)

val DangerRed = Color(0xFFEF4444)
val DangerRedContainer = Color(0xFF450A0A)
val SuccessGreen = Color(0xFF10B981)
