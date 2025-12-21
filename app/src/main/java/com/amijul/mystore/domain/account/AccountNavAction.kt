package com.amijul.mystore.domain.account

sealed class AccountNavAction {
    data object Cart: AccountNavAction()
    data object MyDetails: AccountNavAction()
    data object DeliveryAddress : AccountNavAction()
    data object Help : AccountNavAction()
    data object About : AccountNavAction()

}
