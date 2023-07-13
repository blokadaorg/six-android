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

import channel.pluslease.Lease
import channel.pluslease.PlusLeaseOps
import engine.EngineService
import service.FlutterService

object PlusLeaseBinding: PlusLeaseOps {
    private val flutter by lazy { FlutterService }
    private val command by lazy { CommandBinding }
    private val engine by lazy { EngineService }

    init {
        PlusLeaseOps.setUp(flutter.engine.dartExecutor.binaryMessenger, this)
    }

    override fun doLeasesChanged(leases: List<Lease>, callback: (Result<Unit>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun doCurrentLeaseChanged(lease: Lease?, callback: (Result<Unit>) -> Unit) {
        TODO("Not yet implemented")
    }
}