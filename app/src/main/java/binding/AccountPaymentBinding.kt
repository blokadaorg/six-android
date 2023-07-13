/*
 * This file is part of Blokada.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright © 2023 Blocka AB. All rights reserved.
 *
 * @author Karol Gusak (karol@blocka.net)
 */

package binding

import channel.account.Account
import channel.accountpayment.AccountPaymentOps
import channel.accountpayment.PaymentStatus
import channel.accountpayment.Product
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import model.ProductId
import model.UserInitiated
import service.FlutterService

object AccountPaymentBinding: AccountPaymentOps {
    private val writeProducts = MutableSharedFlow<List<Product>?>(replay = 1)
    private val writeSuccessfulPurchases = MutableSharedFlow<Pair<Account, UserInitiated>>()
    private val writeActiveSub = MutableSharedFlow<ProductId?>(replay = 1)

    val productsHot = writeProducts.filterNotNull()
    val successfulPurchasesHot = writeSuccessfulPurchases
    val activeSubHot = writeActiveSub.distinctUntilChanged()
    private val flutter by lazy { FlutterService }
    private val command by lazy { CommandBinding }

    init {
        AccountPaymentOps.setUp(flutter.engine.dartExecutor.binaryMessenger, this)
    }

    override fun doArePaymentsAvailable(callback: (Result<Boolean>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun doFetchProducts(callback: (Result<List<Product>>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun doPurchaseWithReceipt(productId: String, callback: (Result<String>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun doRestoreWithReceipt(callback: (Result<String>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun doChangeProductWithReceipt(productId: String, callback: (Result<String>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun doFinishOngoingTransaction(callback: (Result<Unit>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun doPaymentStatusChanged(status: PaymentStatus, callback: (Result<Unit>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun doProductsChanged(products: List<Product>, callback: (Result<Unit>) -> Unit) {
        TODO("Not yet implemented")
    }

}