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

import channel.perm.PermOps
import service.FlutterService

object PermBinding: PermOps {
    private val flutter by lazy { FlutterService }
    private val command by lazy { CommandBinding }

    init {
        PermOps.setUp(flutter.engine.dartExecutor.binaryMessenger, this)
    }

    override fun doPrivateDnsEnabled(tag: String, callback: (Result<Boolean>) -> Unit) {
        callback(Result.success(false))
    }

    override fun doSetSetPrivateDnsEnabled(tag: String, callback: (Result<Unit>) -> Unit) {
        // Cannot be done on Android
        callback(Result.success(Unit))
    }

    override fun doNotificationEnabled(callback: (Result<Boolean>) -> Unit) {
        callback(Result.success(false))
    }

    override fun doVpnEnabled(callback: (Result<Boolean>) -> Unit) {
        callback(Result.success(false))
    }

}