package com.amijul.mystore.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amijul.mystore.ui.order.component.ActiveBody
import com.amijul.mystore.ui.order.component.PastBody
import com.amijul.mystore.ui.order.component.TabBtn
import org.koin.androidx.compose.koinViewModel

enum class OrdersTab { Active, Past }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    modifier: Modifier = Modifier,
    orderViewModel: OrderViewModel = koinViewModel(),
) {
    val state by orderViewModel.state.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(OrdersTab.Active) }

    LaunchedEffect(Unit) {

        orderViewModel.start()
    }


    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFD9E3FF))
            .statusBarsPadding()
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            Spacer(Modifier.height(12.dp))

            OrdersTabs(
                tab = tab,
                onTabChange = { tab = it },
            )

            Spacer(Modifier.height(12.dp))

            when (tab) {
                OrdersTab.Active -> ActiveBody(
                    state = state,
                    onIncrease = { productId -> orderViewModel.increaseQty(productId) },
                    onDecrease = { productId -> orderViewModel.decreaseQty(productId) },
                    onBuyNow = { //orderViewModel.buyNow()
                               },
                    isLoading = state.isLoading
                )



                OrdersTab.Past -> PastBody(state = state)
            }
        }



        // simple message popup (optional)
        if (state.message != null) {
            LaunchedEffect(state.message) {
                // auto clear after showing once (or keep)
                // viewModel.clearMessage()
            }
        }
    }
}

@Composable
private fun OrdersTabs(
    tab: OrdersTab,
    onTabChange: (OrdersTab) -> Unit,
) {

        Row(modifier = Modifier.fillMaxWidth()
            .background(Color.White)
            .padding(6.dp)) {
            TabBtn(
                selected = tab == OrdersTab.Active,
                text = "Active Orders",
                onClick = { onTabChange(OrdersTab.Active) },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            TabBtn(
                selected = tab == OrdersTab.Past,
                text = "Past Orders",
                onClick = { onTabChange(OrdersTab.Past) },
                modifier = Modifier.weight(1f)
            )
        }

}












