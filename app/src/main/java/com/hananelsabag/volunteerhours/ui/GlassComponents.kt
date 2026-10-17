package com.hananelsabag.volunteerhours.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

// ─────────────────────────────────────────────────────────────────────────────
// PillTabSelector
//
// Root cause of B1: TabRow draws its indicator in a separate SubcomposeLayout
// pass at a higher z-order than the Tab content, so the filled primary Box
// always renders ON TOP of the text — making selected-tab text invisible.
//
// Fix: drop TabRow entirely. Use a plain Row with one Surface per tab.
// Each tab owns its own background — no separate indicator layer, no z-conflict.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PillTabSelector(
    selectedIndex: Int,
    tabs: List<String>,
    modifier: Modifier = Modifier,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = modifier.height(48.dp),
        shape    = RoundedCornerShape(50.dp),
        color    = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            tabs.forEachIndexed { i, tab ->
                val selected = i == selectedIndex
                Surface(
                    onClick   = { onTabSelected(i) },
                    modifier  = Modifier.weight(1f).fillMaxHeight(),
                    shape     = RoundedCornerShape(44.dp),
                    // selected tab: primary background; unselected: invisible
                    color     = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            tab,
                            // text is part of this Surface's own draw pass — never covered
                            color      = if (selected) MaterialTheme.colorScheme.onPrimary
                                         else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            style      = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GlassNavBar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun GlassNavBar(
    items: List<NavItem>,
    currentRoute: String?,
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    // Slightly higher elevation surface for the pill — visible in both themes
    Surface(
        modifier      = modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(32.dp),
        color         = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 12.dp,
        border        = BorderStroke(
            0.8.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                NavPillItem(
                    item     = item,
                    selected = currentRoute == item.route,
                    onClick  = { onNavigate(item.route) }
                )
            }
        }
    }
}

@Composable
private fun NavPillItem(item: NavItem, selected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
                      else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "nav_bg"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary
                      else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "nav_icon"
    )

    Surface(
        onClick = onClick,
        shape   = RoundedCornerShape(24.dp),
        color   = bgColor
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (selected) 18.dp else 12.dp,
                vertical   = 9.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(
                imageVector     = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                modifier        = Modifier.size(22.dp),
                tint            = iconTint
            )
            AnimatedVisibility(visible = selected) {
                Text(
                    item.label,
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
