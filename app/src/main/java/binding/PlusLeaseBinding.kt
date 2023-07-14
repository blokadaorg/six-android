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
import channel.pluslease.Lease
import channel.pluslease.PlusLeaseOps
import kotlinx.coroutines.flow.MutableStateFlow
import service.FlutterService

object PlusLeaseBinding: PlusLeaseOps {
    val leases = MutableStateFlow<List<Lease>>(emptyList())
    val currentLease = MutableStateFlow<Lease?>(null)

    private val flutter by lazy { FlutterService }
    private val command by lazy { CommandBinding }

    init {
        PlusLeaseOps.setUp(flutter.engine.dartExecutor.binaryMessenger, this)
    }

    fun deleteLease(lease: Lease) {
        command.execute(CommandName.DELETELEASE, lease.publicKey)
    }

    override fun doLeasesChanged(leases: List<Lease>, callback: (Result<Unit>) -> Unit) {
        this.leases.value = leases
        callback(Result.success(Unit))
    }

    override fun doCurrentLeaseChanged(lease: Lease?, callback: (Result<Unit>) -> Unit) {
        this.currentLease.value = lease
        callback(Result.success(Unit))
    }
}