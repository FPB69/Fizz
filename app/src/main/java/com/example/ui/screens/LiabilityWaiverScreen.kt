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
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.text.font.FontFamily
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

            // Architectural Sovereign Header
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(theme.accentPrimary.copy(alpha = 0.15f))
                    .border(1.5.dp, theme.borderGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = theme.accentPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "FIZZ.1 PROTOCOL COVENANT",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = theme.textPrimary
            )

            Text(
                text = "Mandatory 0-Fault & Zero-Liability Acknowledgment",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = theme.accentPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable covenant terms
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Warning Notice Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, theme.accentPrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = theme.accentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "0-FAULT & COMPLETE DEVELOPER INDEMNITY",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = theme.accentPrimary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "The creators, architects, and open-source contributors of Fizz.1 hold ZERO FAULT and ZERO LEGAL LIABILITY for any user conduct, messages, barter listings, transactions, or regulatory matters. You operate this software entirely under sovereign personal discretion.",
                                    fontSize = 11.sp,
                                    color = theme.textPrimary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                // Interactive Clause 1
                item {
                    WaiverClauseItem(
                        title = "1. Decentralized Local-Only SQLite Storage",
                        description = "There are no central servers, no cloud databases, and no intermediaries. All listings, keys, and chat logs reside exclusively on your physical phone.",
                        isChecked = checkZeroCloud,
                        onCheckedChange = { checkZeroCloud = it }
                    )
                }

                // Interactive Clause 2
                item {
                    WaiverClauseItem(
                        title = "2. Absolute 0-Fault Developer Covenant",
                        description = "You irrevocably agree that the software authors cannot monitor, recover, or moderate content, and bear zero liability for any use or outcome.",
                        isChecked = checkZeroLiability,
                        onCheckedChange = { checkZeroLiability = it }
                    )
                }

                // Interactive Clause 3
                item {
                    WaiverClauseItem(
                        title = "3. Sovereign Peer Responsibility & Legal Compliance",
                        description = "You assume 100% legal responsibility for ensuring your communications and listings comply with all applicable local and international jurisdictions.",
                        isChecked = checkUserResponsible,
                        onCheckedChange = { checkUserResponsible = it }
                    )
                }

                // Interactive Clause 4
                item {
                    WaiverClauseItem(
                        title = "4. Irreversible Cryptographic Data Deletion",
                        description = "Self-destructing messages and panic wipes destroy data with zero recovery possibility. No master keys or backdoors exist.",
                        isChecked = checkNoRecovery,
                        onCheckedChange = { checkNoRecovery = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Accept & Enter Button
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
                    text = if (allChecked) "Accept Covenant & Enter Fizz.1" else "Check All 4 Clauses to Proceed",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
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
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = theme.accentPrimary,
                    uncheckedColor = theme.textMuted,
                    checkmarkColor = Color.White
                ),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isChecked) theme.textPrimary else theme.textSecondary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    fontSize = 10.5.sp,
                    color = theme.textMuted,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
