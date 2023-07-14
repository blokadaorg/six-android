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

import channel.plusvpn.PlusVpnOps
import channel.plusvpn.VpnConfig
import engine.EngineService
import service.FlutterService

object PlusVpnBinding: PlusVpnOps {
    private val flutter by lazy { FlutterService }
    private val engine by lazy { EngineService }

    init {
        PlusVpnOps.setUp(flutter.engine.dartExecutor.binaryMessenger, this)
    }

    override fun doSetVpnConfig(config: VpnConfig, callback: (Result<Unit>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun doSetVpnActive(active: Boolean, callback: (Result<Unit>) -> Unit) {
        TODO("Not yet implemented")
    }
}