package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design tokens for Layout, Spacing, Sizing, Borders, and Radii.
 * Conforms strictly to Master Build Spec v3 (Section 34).
 * Single source of truth: no hard-coded dp values across UI screens.
 */
object Dimens {

    // =========================================================================
    // 34.1 Spacing Scale (4-dp base)
    // =========================================================================
    val space0 = 0.dp
    val space1 = 4.dp
    val space2 = 8.dp
    val space3 = 12.dp
    val space4 = 16.dp
    val space5 = 20.dp
    val space6 = 24.dp
    val space7 = 32.dp
    val space8 = 40.dp
    val space9 = 48.dp
    val space10 = 64.dp

    // Screen edge margins
    val screenEdgeCompact = 16.dp
    val screenEdgeMedium = 24.dp
    val screenEdgeExpanded = 32.dp

    // Layout gaps
    val sectionGap = 24.dp
    val titleContentGap = 8.dp
    val titleContentGapLarge = 12.dp
    val itemGap = 8.dp
    val itemGapLarge = 12.dp

    // Grid gutters
    val gutterDensePhotos = 4.dp
    val gutterCards = 8.dp
    val gutterTabletCards = 12.dp

    // Paddings
    val cardPadding = 16.dp
    val cardPaddingExpanded = 20.dp
    val listRowPaddingHorizontal = 16.dp
    val listRowPaddingVertical = 12.dp
    val dialogPadding = 24.dp
    val bottomSheetPadding = 16.dp
    val dragHandleWidth = 32.dp
    val dragHandleHeight = 4.dp
    val dragHandleMargin = 12.dp

    // =========================================================================
    // 34.2 Borders & Shape
    // =========================================================================
    val border1 = 1.dp // Default border width
    val border2 = 2.dp // Focus / Selected border width

    val outlineColor = Color(0xFF262E3D)
    val outlineStrongColor = Color(0xFF3A4560)
    val errorBorderColor = Color(0xFFF87171)

    // Corner Radii
    val radiusChip = 12.dp
    val radiusButton = 14.dp
    val radiusTextField = 14.dp
    val radiusCard = 20.dp
    val radiusAlbumTile = 20.dp
    val radiusSheet = 28.dp
    val radiusDialog = 28.dp
    val radiusThumbnail = 12.dp
    val radiusThumbnailDense = 8.dp
    val radiusMiniPlayer = 12.dp
    val radiusSnackbar = 12.dp

    val shapeChip = RoundedCornerShape(radiusChip)
    val shapeButton = RoundedCornerShape(radiusButton)
    val shapeButtonPill = CircleShape
    val shapeTextField = RoundedCornerShape(radiusTextField)
    val shapeCard = RoundedCornerShape(radiusCard)
    val shapeAlbumTile = RoundedCornerShape(radiusAlbumTile)
    val shapeSheet = RoundedCornerShape(topStart = radiusSheet, topEnd = radiusSheet)
    val shapeDialog = RoundedCornerShape(radiusDialog)
    val shapeThumbnail = RoundedCornerShape(radiusThumbnail)
    val shapeThumbnailDense = RoundedCornerShape(radiusThumbnailDense)
    val shapeMiniPlayer = RoundedCornerShape(radiusMiniPlayer)
    val shapeKeypadKey = CircleShape

    // =========================================================================
    // 34.3 Component Sizing Table
    // =========================================================================
    // Primary Button
    val buttonPrimaryHeight = 52.dp
    val buttonPrimaryMinWidth = 120.dp
    val buttonPrimaryPaddingHorizontal = 24.dp

    // Secondary / Outlined Button
    val buttonSecondaryHeight = 48.dp
    val buttonSecondaryPaddingHorizontal = 20.dp

    // Small / Tonal Button
    val buttonSmallHeight = 40.dp
    val touchTargetMin = 48.dp

    // Icon Button
    val iconSizeVisual = 24.dp
    val iconSizeSmall = 20.dp
    val iconButtonTarget = 48.dp
    val iconButtonInnerPadding = 4.dp

    // Floating Action Button
    val fabSize = 56.dp
    val fabExtendedPadding = 20.dp

    // Text Field
    val textFieldHeight = 56.dp
    val textFieldPaddingHorizontal = 16.dp
    val textFieldErrorTopGap = 4.dp

    // Chip
    val chipHeight = 36.dp
    val chipTouchTarget = 48.dp
    val chipPadding = 12.dp
    val chipGap = 8.dp

    // Switch Row
    val switchRowHeight = 64.dp
    val switchWidth = 52.dp
    val switchHeight = 32.dp
    val switchRowPadding = 16.dp

    // List Items
    val listItemHeightSingle = 56.dp
    val listItemHeightWithThumb = 72.dp
    val listItemHeightVideo = 88.dp
    val listThumbSize = 48.dp
    val listVideoThumbWidth = 96.dp
    val listVideoThumbHeight = 64.dp

    // Bars
    val topAppBarHeight = 56.dp
    val topAppBarLargeTitleHeight = 112.dp
    val bottomBarGlassHeight = 64.dp
    val bottomBarFloatingOffset = 16.dp
    val bottomBarSideMargin = 16.dp

    // Mini Player
    val miniPlayerHeight = 64.dp
    val miniPlayerPadding = 12.dp
    val miniPlayerArtworkSize = 48.dp

    // Keypad & PIN
    val keypadKeySizeCompact = 72.dp
    val keypadKeySizeLarge = 80.dp
    val keypadKeyGap = 16.dp
    val pinDotSize = 14.dp
    val pinDotGap = 16.dp

    // Seek bar
    val seekBarTrackHeight = 4.dp
    val seekBarTrackHeightDragging = 12.dp
    val seekBarThumbSize = 16.dp
    val seekBarThumbSizeActive = 24.dp
    val seekBarTouchHeight = 48.dp

    // Player controls
    val playerCenterPlaySize = 72.dp
    val playerSeekButtonSize = 56.dp
    val playerBottomIconSize = 24.dp
    val playerBottomIconTarget = 48.dp
    val playerControlGap = 8.dp

    // Dialog & Overlays
    val dialogMaxWidth = 560.dp
    val dialogButtonHeight = 48.dp
    val dialogButtonGap = 8.dp
    val snackbarMinHeight = 48.dp
    val snackbarEdgeMargin = 16.dp

    // Section Header
    val sectionHeaderMarginTop = 24.dp
    val sectionHeaderMarginBottom = 8.dp

    // =========================================================================
    // 34.5 Responsive Constraints
    // =========================================================================
    val maxContentWidth = 720.dp
    val minCellWidthPhotosDense = 96.dp
    val minCellWidthPhotosDefault = 120.dp
    val minCellWidthVideoCards = 160.dp
    val minCellWidthAlbumTiles = 180.dp
}
