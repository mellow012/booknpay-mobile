package com.example.booknpay.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknpay.data.model.User

// ─── Drawer item model ────────────────────────────────────────────────────────
private data class DrawerItem(
    val icon: ImageVector,
    val label: String,
    val route: String,
    val badge: String? = null
)

private val mainItems = listOf(
    DrawerItem(Icons.Default.Home,               "Home",            Screen.Home.route),
    DrawerItem(Icons.Default.Inventory,        "Track Parcel",    Screen.ParcelTracker.route),
    DrawerItem(Icons.Default.Business,         "Bus Companies",   Screen.Companies.route),
    DrawerItem(Icons.Default.Storefront,       "Bus Stations",    Screen.Stations.route),
)

private val secondaryItems = listOf(
    DrawerItem(Icons.Default.Settings,           "Settings",        Screen.Settings.route),
    DrawerItem(Icons.AutoMirrored.Filled.Help,   "Help & support",  Screen.Help.route),
    DrawerItem(Icons.Default.Info,               "About",           Screen.About.route),
)

// ─── Drawer composable ────────────────────────────────────────────────────────
@Composable
fun AppNavigationDrawer(
    currentRoute: String?,
    user: User?,
    unreadCount: Int,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.width(300.dp),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 24.dp)
        ) {
            // ── User header ────────────────────────────────────────────────
            DrawerHeader(user = user)

            Spacer(modifier = Modifier.height(24.dp))

            // ── Main navigation ────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                mainItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val badge = if (item.label == "Notifications" && unreadCount > 0)
                        if (unreadCount > 9) "9+" else "$unreadCount"
                    else
                        item.badge

                    DrawerNavItem(
                        icon = item.icon,
                        label = item.label,
                        isSelected = isSelected,
                        badge = badge,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // ── Secondary navigation ───────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                secondaryItems.forEach { item ->
                    DrawerNavItem(
                        icon = item.icon,
                        label = item.label,
                        isSelected = currentRoute == item.route,
                        badge = item.badge,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }

            // ── Spacer to push logout to bottom ───────────────────────────
            Spacer(modifier = Modifier.weight(1f))

            // ── Logout ─────────────────────────────────────────────────────
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onLogout)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    "Sign out",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // ── App version ────────────────────────────────────────────────
            Text(
                text = "TibhukeBus v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .padding(horizontal = 28.dp, vertical = 12.dp)
            )
        }
    }
}

// ─── Header showing user avatar + info ────────────────────────────────────────
@Composable
private fun DrawerHeader(user: User?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar — initials circle
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            val initials = buildString {
                user?.firstName?.firstOrNull()?.let { append(it.uppercaseChar()) }
                user?.lastName?.firstOrNull()?.let { append(it.uppercaseChar()) }
            }.ifEmpty { "?" }
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Column {
            Text(
                text = user?.fullName ?: "Guest user",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = user?.email ?: "Not signed in",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (user != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Premium member",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

// ─── Single nav item ──────────────────────────────────────────────────────────
@Composable
private fun DrawerNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    badge: String?,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface

    val contentColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor,
            modifier = Modifier.weight(1f)
        )

        // Notification badge
        if (badge != null) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(2.dp))
}

// ─── Usage in MainActivity — wrap your NavHost with ModalNavigationDrawer ─────
//
// val drawerState = rememberDrawerState(DrawerValue.Closed)
// val scope = rememberCoroutineScope()
// val navBackStackEntry by navController.currentBackStackEntryAsState()
//
// ModalNavigationDrawer(
//     drawerState = drawerState,
//     drawerContent = {
//         AppNavigationDrawer(
//             currentRoute = navBackStackEntry?.destination?.route,
//             user = profileState.user,
//             unreadCount = notificationCount,
//             onNavigate = { route ->
//                 scope.launch { drawerState.close() }
//                 navController.navigate(route) { launchSingleTop = true }
//             },
//             onLogout = { /* call viewModel.logout */ }
//         )
//     }
// ) {
//     Scaffold(...) { NavHost(...) }
// }