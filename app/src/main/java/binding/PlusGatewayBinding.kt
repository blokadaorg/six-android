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

import androidx.lifecycle.MutableLiveData
import channel.plusgateway.Gateway
import channel.plusgateway.PlusGatewayOps
import service.FlutterService

object PlusGatewayBinding: PlusGatewayOps {
    val gateways = MutableLiveData<List<Gateway>>()
    val selected = MutableLiveData<String?>()

    private val flutter by lazy { FlutterService }

    init {
        PlusGatewayOps.setUp(flutter.engine.dartExecutor.binaryMessenger, this)
    }

    override fun doGatewaysChanged(gateways: List<Gateway>, callback: (Result<Unit>) -> Unit) {
        this.gateways.value = gateways
        callback(Result.success(Unit))
    }

    override fun doSelectedGatewayChanged(publicKey: String?, callback: (Result<Unit>) -> Unit) {
        this.selected.value = publicKey
        callback(Result.success(Unit))
    }
}