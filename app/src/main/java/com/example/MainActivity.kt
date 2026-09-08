package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BubbleWaterBackground
import com.example.ui.components.LiveTransparencyBar
import com.example.ui.components.TransparencyInspectorDialog
import com.example.ui.screens.ChatConversationScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.MarketplaceScreen
import com.example.ui.screens.MyStoreScreen
import com.example.ui.screens.PrivacySecurityScreen
import com.example.ui.theme.BubbleAquaPrimary
import com.example.ui.theme.BubbleAquaLight
import com.example.ui.theme.BubbleCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.TorPeerViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        FizzApp()
      }
    }
  }
}

@Composable
fun FizzApp(viewModel: TorPeerViewModel = viewModel()) {
  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val selectedPeerId by viewModel.selectedPeerId.collectAsStateWithLifecycle()
  val currentActivity by viewModel.currentActivity.collectAsStateWithLifecycle()
  val transparencyEvents by viewModel.transparencyEvents.collectAsStateWithLifecycle()
  val torStatus by viewModel.torStatus.collectAsStateWithLifecycle()
  val myListings by viewModel.myListings.collectAsStateWithLifecycle()
  val peerListings by viewModel.peerListings.collectAsStateWithLifecycle()
  var showTransparencyDialog by remember { mutableStateOf(false) }

  BackHandler(enabled = selectedPeerId != null) {
    viewModel.clearChatSelection()
  }

  BubbleWaterBackground {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = Color.Transparent,
      bottomBar = {
        // Hide bottom navigation if inside active conversation
        if (selectedPeerId == null) {
          NavigationBar(
            containerColor = DarkSurfaceElevated.copy(alpha = 0.95f),
            tonalElevation = 0.dp,
            modifier = Modifier
              .border(1.dp, DarkBorder)
              .windowInsetsPadding(WindowInsets.navigationBars)
              .testTag("main_navigation_bar")
          ) {
            NavigationBarItem(
              selected = currentTab == AppNavTab.MARKETPLACE,
              onClick = { viewModel.setNavTab(AppNavTab.MARKETPLACE) },
              icon = {
                Icon(
                  imageVector = if (currentTab == AppNavTab.MARKETPLACE) Icons.Default.ShoppingBag else Icons.Outlined.ShoppingBag,
                  contentDescription = "Marketplace",
                  modifier = Modifier.size(22.dp)
                )
              },
              label = {
                Text(
                  "Market",
                  fontSize = 11.sp,
                  fontWeight = if (currentTab == AppNavTab.MARKETPLACE) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DarkBackground,
                selectedTextColor = BubbleAquaLight,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted,
                indicatorColor = BubbleAquaPrimary
              ),
              modifier = Modifier.testTag("nav_item_marketplace")
            )

            NavigationBarItem(
              selected = currentTab == AppNavTab.MY_STORE,
              onClick = { viewModel.setNavTab(AppNavTab.MY_STORE) },
              icon = {
                Icon(
                  imageVector = if (currentTab == AppNavTab.MY_STORE) Icons.Default.Storefront else Icons.Outlined.Storefront,
                  contentDescription = "My Store",
                  modifier = Modifier.size(22.dp)
                )
              },
              label = {
                Text(
                  "My Store",
                  fontSize = 11.sp,
                  fontWeight = if (currentTab == AppNavTab.MY_STORE) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DarkBackground,
                selectedTextColor = BubbleAquaLight,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted,
                indicatorColor = BubbleAquaPrimary
              ),
              modifier = Modifier.testTag("nav_item_my_store")
            )

            NavigationBarItem(
              selected = currentTab == AppNavTab.CHATS,
              onClick = { viewModel.setNavTab(AppNavTab.CHATS) },
              icon = {
                Icon(
                  imageVector = if (currentTab == AppNavTab.CHATS) Icons.AutoMirrored.Filled.Chat else Icons.AutoMirrored.Outlined.Chat,
                  contentDescription = "Chats",
                  modifier = Modifier.size(22.dp)
                )
              },
              label = {
                Text(
                  "Chats",
                  fontSize = 11.sp,
                  fontWeight = if (currentTab == AppNavTab.CHATS) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DarkBackground,
                selectedTextColor = BubbleAquaLight,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted,
                indicatorColor = BubbleAquaPrimary
              ),
              modifier = Modifier.testTag("nav_item_chats")
            )

            NavigationBarItem(
              selected = currentTab == AppNavTab.SECURITY,
              onClick = { viewModel.setNavTab(AppNavTab.SECURITY) },
              icon = {
                Icon(
                  imageVector = if (currentTab == AppNavTab.SECURITY) Icons.Default.Security else Icons.Outlined.Security,
                  contentDescription = "Tor Vault",
                  modifier = Modifier.size(22.dp)
                )
              },
              label = {
                Text(
                  "Tor Vault",
                  fontSize = 11.sp,
                  fontWeight = if (currentTab == AppNavTab.SECURITY) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DarkBackground,
                selectedTextColor = BubbleAquaLight,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted,
                indicatorColor = BubbleAquaPrimary
              ),
              modifier = Modifier.testTag("nav_item_security")
            )
          }
        }
      }
    ) { innerPadding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        // Super transparent live activity pill bar across the app
        if (selectedPeerId == null) {
          LiveTransparencyBar(
            activityState = currentActivity,
            onClick = { showTransparencyDialog = true },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
          )
        }

        Box(modifier = Modifier.weight(1f)) {
          AnimatedContent(
            targetState = Pair(currentTab, selectedPeerId),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tab_transition"
          ) { (tab, peerId) ->
            when {
              tab == AppNavTab.CHATS && peerId != null -> {
                ChatConversationScreen(
                  peerId = peerId,
                  viewModel = viewModel,
                  onBack = { viewModel.clearChatSelection() }
                )
              }
              tab == AppNavTab.MARKETPLACE -> {
                MarketplaceScreen(viewModel = viewModel)
              }
              tab == AppNavTab.MY_STORE -> {
                MyStoreScreen(viewModel = viewModel)
              }
              tab == AppNavTab.CHATS -> {
                ChatListScreen(viewModel = viewModel)
              }
              tab == AppNavTab.SECURITY -> {
                PrivacySecurityScreen(viewModel = viewModel)
              }
            }
          }
        }
      }

      if (showTransparencyDialog) {
        TransparencyInspectorDialog(
          activityState = currentActivity,
          events = transparencyEvents,
          torStatus = torStatus,
          myListingsCount = myListings.size,
          peerListingsCount = peerListings.size,
          onProbeTor = { viewModel.probeTorConnectivity() },
          onClearLog = { viewModel.clearTransparencyLog() },
          onDismiss = { showTransparencyDialog = false }
        )
      }
    }
  }
}

// Keep TorPeerApp alias for backwards compatibility
@Composable
fun TorPeerApp(viewModel: TorPeerViewModel = viewModel()) {
  FizzApp(viewModel)
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Fizz $name", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Active") }
}

