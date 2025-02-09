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

import channel.command.CommandName
import channel.payment.PaymentOps
import channel.payment.PaymentStatus
import channel.payment.Product
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import model.PaymentPayload
import model.ProductId
import service.BillingService
import service.FlutterService
import utils.Logger

object AccountPaymentBinding: PaymentOps {
    val products = MutableStateFlow<List<Product>?>(null)
    val status = MutableStateFlow(PaymentStatus.UNKNOWN)
    val activeSub = MutableStateFlow<ProductId?>(null)

    private val restored = mutableSetOf<String>()

    private val flutter by lazy { FlutterService }
    private val command by lazy { CommandBinding }
    private val billing by lazy { BillingService }

    private val scope = GlobalScope

    init {
        PaymentOps.setUp(flutter.engine.dartExecutor.binaryMessenger, this)
        scope.async {
            billing.setup()
        }
    }

    suspend fun refreshProducts() {
        try {
            val active = billing.getActivePurchase()
            activeSub.value = active
        } catch (e: Exception) {
            activeSub.value = null
        }
        command.execute(CommandName.FETCHPRODUCTS)
    }

    suspend fun restorePurchase() {
        command.execute(CommandName.RESTOREPAYMENT)
    }

    suspend fun buyProduct(productId: String) {
        command.execute(CommandName.PURCHASE, productId)
    }

    suspend fun changeProduct(productId: String) {
        command.execute(CommandName.CHANGEPRODUCT, productId)
    }

    private var debounceJob: Job? = null

    suspend fun verifyWaitingPurchases() {
        debounceJob?.cancel()

        debounceJob = GlobalScope.launch {
            delay(3000)
            if (this.isActive) {
                try {
                    billing.restorePurchase().forEach { payload ->
                        if (payload.purchase_token !in restored) {
                            restored.add(payload.purchase_token)
                            command.execute(CommandName.RECEIPT, convertToReceipt(payload))
                        }
                    }
                } catch (e: Exception) {
                    Logger.v("Payment", "No waiting purchases found")
                    // ignore
                }
            }
        }
    }

    override fun doArePaymentsAvailable(callback: (Result<Boolean>) -> Unit) {
        // TODO
        callback(Result.success(true))
    }

    override fun doFetchProducts(callback: (Result<List<Product>>) -> Unit) {
        scope.async {
            try {
                val products = billing.refreshProducts()
                callback(Result.success(products))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }

    override fun doPurchaseWithReceipts(productId: String, callback: (Result<List<String>>) -> Unit) {
        scope.async {
            try {
                val payload = billing.buyProduct(productId)
                callback(Result.success(listOf(convertToReceipt(payload))))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }

    override fun doRestoreWithReceipts(callback: (Result<List<String>>) -> Unit) {
        scope.async {
            try {
                val payloads = billing.restorePurchase()
                payloads.map { restored.add(it.purchase_token) }
                callback(Result.success(payloads.map { convertToReceipt(it) }))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }

    private fun convertToReceipt(payload: PaymentPayload): String {
        return "${payload.purchase_token}:::${payload.subscription_id}"
    }

    override fun doChangeProductWithReceipt(productId: String, callback: (Result<String>) -> Unit) {
        scope.async {
            try {
                val payload = billing.changeProduct(productId)
                callback(Result.success(convertToReceipt(payload)))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }

    override fun doFinishOngoingTransaction(callback: (Result<Unit>) -> Unit) {
        // TODO
        callback(Result.success(Unit))
    }

    override fun doPaymentStatusChanged(status: PaymentStatus, callback: (Result<Unit>) -> Unit) {
        this.status.value = status
        callback(Result.success(Unit))
    }

    override fun doProductsChanged(products: List<Product>, callback: (Result<Unit>) -> Unit) {
        this.products.value = products
        callback(Result.success(Unit))
    }

    var accountType: String = "libre"

    override fun doAccountTypeChanged(accountType: String, callback: (Result<Unit>) -> Unit) {
        this.accountType = accountType
        callback(Result.success(Unit))
    }

}