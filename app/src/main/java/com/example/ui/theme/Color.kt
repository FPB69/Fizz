package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Minimalist Medical App Palette (Dark Theme & Light Theme as in Dribbble reference)
// Light Theme Palette
val MedicalLightBg = Color(0xFFF8FAFC)           // Crisp clinic off-white
val MedicalLightSurface = Color(0xFFFFFFFF)      // Pure sterile white card
val MedicalLightSurfaceElevated = Color(0xFFF1F5F9) // Subtle elevated card
val MedicalLightSurfaceHigh = Color(0xFFE2E8F0)  // Highlighted pill container
val MedicalLightBorder = Color(0xFFE2E8F0)       // Ultra-fine light border
val MedicalLightTextPrimary = Color(0xFF0F172A)  // Deep slate primary
val MedicalLightTextSecondary = Color(0xFF475569)// Refined secondary slate
val MedicalLightTextMuted = Color(0xFF94A3B8)    // Soft slate muted

// Dark Theme Palette
val MedicalDarkBg = Color(0xFF0B0F19)            // Obsidian charcoal
val MedicalDarkSurface = Color(0xFF131B2E)       // Medical elevated surface
val MedicalDarkSurfaceElevated = Color(0xFF1A243B) // High-contrast container
val MedicalDarkSurfaceHigh = Color(0xFF23314A)   // Pill surface
val MedicalDarkBorder = Color(0xFF243044)        // Sleek subtle border line
val MedicalDarkTextPrimary = Color(0xFFF8FAFC)   // Crisp ice white
val MedicalDarkTextSecondary = Color(0xFF94A3B8) // Soft metallic slate
val MedicalDarkTextMuted = Color(0xFF64748B)     // Muted deep slate

// Medical & Anonymity Brand Accents
val MedicalTeal = Color(0xFF0EA5E9)              // Surgical clinical cyan-sky
val MedicalTealDark = Color(0xFF0284C7)          // Deep cyan
val MedicalCyan = Color(0xFF38BDF8)              // Electric pulse cyan
val MedicalMint = Color(0xFF10B981)              // Heartbeat health green
val MedicalMintDark = Color(0xFF059669)          // Muted health green
val MedicalAmber = Color(0xFFF59E0B)             // Diagnostic warning
val MedicalRed = Color(0xFFEF4444)               // Emergency alert
val MedicalViolet = Color(0xFF6366F1)            // Encrypted biometric indigo

// Backward compatibility references
val BubbleAquaPrimary = MedicalCyan
val BubbleAquaLight = Color(0xFF7DD3FC)
val BubbleAquaDark = MedicalTealDark
val BubbleCyan = MedicalCyan
val BubbleCarbonationGreen = MedicalMint
val BubbleFoamWhite = Color(0xFFF0FDF4)

val TorPurple = MedicalTeal
val TorPurpleLight = MedicalCyan
val TorPurpleDark = MedicalTealDark
val TorOnionGreen = MedicalMint
val TorCyan = MedicalCyan

val DarkBackground = MedicalDarkBg
val DarkSurface = MedicalDarkSurface
val DarkSurfaceElevated = MedicalDarkSurfaceElevated
val DarkSurfaceHigh = MedicalDarkSurfaceHigh
val DarkBorder = MedicalDarkBorder
val BubbleGlass = Color(0x1A38BDF8)

val TextPrimary = MedicalDarkTextPrimary
val TextSecondary = MedicalDarkTextSecondary
val TextMuted = MedicalDarkTextMuted

val AccentAmber = MedicalAmber
val ErrorRed = MedicalRed
val SecurityShieldGreen = MedicalMint
