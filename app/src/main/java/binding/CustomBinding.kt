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
import channel.custom.CustomOps
import kotlinx.coroutines.flow.MutableStateFlow
import service.FlutterService

object CustomBinding: CustomOps {
    val allowed = MutableStateFlow<List<String>>(emptyList())
    val denied = MutableStateFlow<List<String>>(emptyList())

    private val flutter by lazy { FlutterService }
    private val command by lazy { CommandBinding }

    init {
        CustomOps.setUp(flutter.engine.dartExecutor.binaryMessenger, this)
    }

    fun allow(name: String) {
        command.execute(CommandName.ALLOW, name)
    }

    fun deny(name: String) {
        command.execute(CommandName.DENY, name)
    }

    fun delete(name: String) {
        command.execute(CommandName.DELETE, name)
    }

    fun isAllowed(name: String): Boolean {
        return allowed.value.contains(name)
    }

    fun isDenied(name: String): Boolean {
        return denied.value.contains(name)
    }

    override fun doCustomAllowedChanged(allowed: List<String>, callback: (Result<Unit>) -> Unit) {
        this.allowed.value = allowed
        callback(Result.success(Unit))
    }

    override fun doCustomDeniedChanged(denied: List<String>, callback: (Result<Unit>) -> Unit) {
        this.denied.value = denied
        callback(Result.success(Unit))
    }
}