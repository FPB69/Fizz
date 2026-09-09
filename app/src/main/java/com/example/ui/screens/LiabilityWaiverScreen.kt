package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalMedicalTheme

@Composable
fun LiabilityWaiverScreen(
    onAcceptWaiver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalMedicalTheme.current

    var checkZeroCloud by remember { mutableStateOf(false) }
    var checkZeroLiability by remember { mutableStateOf(false) }
    var checkUserResponsible by remember { mutableStateOf(false) }
    var checkNoRecovery by remember { mutableStateOf(false) }

    val allChecked = checkZeroCloud && checkZeroLiability && checkUserResponsible && checkNoRecovery

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("mandatory_liability_waiver_screen"),
        color = theme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(theme.accentPrimary.copy(alpha = 0.15f))
                    .border(1.5.dp, theme.borderGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = theme.accentPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Protocol Terms",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )

            Text(
                text = "Please check all 4 boxes to continue",
                fontSize = 12.sp,
                color = theme.textMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    WaiverClauseItem(
                        title = "1. Local Only",
                        description = "No servers or cloud. Data stays on your phone.",
                        isChecked = checkZeroCloud,
                        onCheckedChange = { checkZeroCloud = it }
                    )
                }

                item {
                    WaiverClauseItem(
                        title = "2. No Intermediary",
                        description = "Connections are direct peer-to-peer over Tor.",
                        isChecked = checkZeroLiability,
                        onCheckedChange = { checkZeroLiability = it }
                    )
                }

                item {
                    WaiverClauseItem(
                        title = "3. Personal Responsibility",
                        description = "You control your keys, messages, and listings.",
                        isChecked = checkUserResponsible,
                        onCheckedChange = { checkUserResponsible = it }
                    )
                }

                item {
                    WaiverClauseItem(
                        title = "4. Permanent Deletion",
                        description = "Deleted messages and wiped keys cannot be recovered.",
                        isChecked = checkNoRecovery,
                        onCheckedChange = { checkNoRecovery = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onAcceptWaiver,
                enabled = allChecked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.accentPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = theme.surfaceElevated,
                    disabledContentColor = theme.textMuted
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(
                        1.dp,
                        if (allChecked) theme.borderGold else theme.border,
                        RoundedCornerShape(12.dp)
                    )
                    .testTag("accept_liability_waiver_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (allChecked) "Agree & Continue" else "Check 4 Boxes to Continue",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun WaiverClauseItem(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val theme = LocalMedicalTheme.current

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isChecked) theme.borderGold else theme.border,
                RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = theme.accentPrimary,
                    uncheckedColor = theme.textMuted,
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isChecked) theme.textPrimary else theme.textSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.5.sp,
                    color = theme.textMuted,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
