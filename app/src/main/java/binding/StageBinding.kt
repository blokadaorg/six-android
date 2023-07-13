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
import channel.stage.StageModal
import channel.stage.StageOps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import org.blokada.R
import service.AlertDialogService
import service.FlutterService
import service.Sheet
import service.SheetService

object StageBinding: StageOps {
    private val writeForeground = MutableStateFlow<Boolean?>(null)
    val enteredForegroundHot = writeForeground.filterNotNull().filter { it }

    private val flutter by lazy { FlutterService }
    private val command by lazy { CommandBinding }
    private val sheet by lazy { SheetService }
    private val dialog by lazy { AlertDialogService }

    var onShowNavBar: (Boolean) -> Unit = { }

    init {
        StageOps.setUp(flutter.engine.dartExecutor.binaryMessenger, this)
    }

    fun onForeground() {
        command.execute(CommandName.FOREGROUND)
    }

    fun onBackground() {
        command.execute(CommandName.BACKGROUND)
    }

    fun sheetShown(sheet: Sheet) {
        val name = when (sheet) {
            Sheet.Payment -> StageModal.PAYMENT
            Sheet.Activated -> StageModal.ONBOARDING
            Sheet.Location -> StageModal.PLUSLOCATIONSELECT
            else -> null
        }

        if (name != null) {
            command.execute(CommandName.MODALSHOWN, name.name)
        }
    }

    fun modalDismissed() {
        command.execute(CommandName.MODALDISMISSED)
    }

    override fun doShowModal(modal: StageModal, callback: (Result<Unit>) -> Unit) {
        if (showDialog(modal)) return callback(Result.success(Unit))

        val name = when (modal) {
            StageModal.PAYMENT -> Sheet.Payment
            StageModal.ONBOARDING -> Sheet.Activated
            StageModal.PLUSLOCATIONSELECT -> Sheet.Location
            else -> null
        }

        if (name != null) {
            sheet.showSheet(name)
        }

        callback(Result.success(Unit))
    }

    private fun showDialog(modal: StageModal): Boolean {
        val name = when (modal) {
            StageModal.FAULT, StageModal.ACCOUNTINITFAILED -> R.string.error_unknown
            StageModal.ACCOUNTEXPIRED -> R.string.error_account_inactive
            StageModal.ACCOUNTRESTOREFAILED -> R.string.error_payment_inactive_after_restore
            StageModal.ACCOUNTINVALID -> R.string.error_account_invalid
            StageModal.PLUSTOOMANYLEASES -> R.string.error_vpn_too_many_leases
            StageModal.PLUSVPNFAILURE -> R.string.error_vpn
            StageModal.PAYMENTUNAVAILABLE -> R.string.error_payment_not_available
            StageModal.PAYMENTTEMPUNAVAILABLE -> R.string.error_payment_failed
            StageModal.PAYMENTFAILED -> R.string.error_payment_failed_alternative
            else -> null
        }

        if (name != null) {
            dialog.showAlert(name, onDismiss = ::modalDismissed)
            command.execute(CommandName.MODALSHOWN, modal.name)
            return true
        }
        return false
    }

    override fun doDismissModal(callback: (Result<Unit>) -> Unit) {
        sheet.dismiss()
        callback(Result.success(Unit))
    }

    override fun doRouteChanged(path: String, callback: (Result<Unit>) -> Unit) {
        callback(Result.success(Unit))
    }

    override fun doShowNavbar(show: Boolean, callback: (Result<Unit>) -> Unit) {
        onShowNavBar(show)
        callback(Result.success(Unit))
    }
}