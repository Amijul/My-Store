package com.amijul.mystore.ui.account


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amijul.mystore.domain.account.AccountNavAction
import com.amijul.mystore.domain.account.AccountRowItem
import com.amijul.mystore.domain.account.AccountUi
import com.amijul.mystore.ui.account.component.AccountMenuRow
import com.amijul.mystore.ui.account.component.ProfileHeaderCard


@Composable
fun AccountScreen(
    accountUi: AccountUi,
    onItemClick: (AccountNavAction) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = Color(0xFFF3F4F6) // soft light background like screenshot
    val items = listOf(
        AccountRowItem("Orders", Icons.AutoMirrored.Filled.ReceiptLong) { onItemClick(AccountNavAction.Orders) },
        AccountRowItem("My Details", Icons.Filled.VerifiedUser) { onItemClick(AccountNavAction.MyDetails) },
        AccountRowItem("Delivery Address", Icons.Filled.LocationOn) { onItemClick(AccountNavAction.DeliveryAddress) },
        AccountRowItem("Help", Icons.Filled.SupportAgent) { onItemClick(AccountNavAction.Help) },
        AccountRowItem("About", Icons.Filled.Info) { onItemClick(AccountNavAction.About) },
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 140.dp // leave space for floating logout button
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ProfileHeaderCard(
                    name = accountUi.name,
                    email = accountUi.email,
                    photoUrl = accountUi.photoUrl,
                )
            }

            item {
                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        items.forEachIndexed { index, row ->
                            AccountMenuRow(
                                title = row.title,
                                icon = row.icon,
                                onClick = row.onClick
                            )
                            if (index != items.lastIndex) {
                                HorizontalDivider(color = Color(0xFFEAEAEA))
                            }
                        }
                    }

                }
            }
        }

        // Floating logout button (like screenshot)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF16A34A) // green-ish like screenshot
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null)
                Spacer(Modifier.size(10.dp))
                Text(
                    text = "Log Out",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}






