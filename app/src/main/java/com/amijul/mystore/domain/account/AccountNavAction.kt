package com.amijul.mystore.domain.account

sealed class AccountNavAction {
    data object Orders: AccountNavAction()
    data object MyDetails: AccountNavAction()
    data object DeliveryAddress : AccountNavAction()
    data object Help : AccountNavAction()
    data object About : AccountNavAction()
    data object SellerUpgrade : AccountNavAction()

}
