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

import channel.command.CommandEvents
import channel.command.CommandName
import channel.command.CommandOps
import service.FlutterService

object CommandBinding: CommandOps {
    private val cmd by lazy { CommandEvents(flutter.engine.dartExecutor.binaryMessenger) }
    private val flutter by lazy { FlutterService }

    private var canAcceptCommands = false
    private var queue = mutableListOf<Triple<CommandName, String?, String?>>()

    init {
        CommandOps.setUp(flutter.engine.dartExecutor.binaryMessenger, this)
    }

    override fun doCanAcceptCommands(callback: (Result<Unit>) -> Unit) {
        canAcceptCommands = true
        queue.forEach { (cmd, p1, p2) ->
            when {
                p1 != null && p2 != null -> execute(cmd, p1, p2)
                p1 != null -> execute(cmd, p1)
                else -> execute(cmd)
            }
        }
        queue.clear()
    }

    fun execute(command: CommandName) {
        if (canAcceptCommands) {
            cmd.onCommand(command.name) {}
        } else {
            queue.add(Triple(command, null, null))
        }
    }

    fun execute(command: CommandName, p1: String) {
        if (canAcceptCommands) {
            cmd.onCommandWithParam(command.name, p1) {}
        } else {
            queue.add(Triple(command, p1, null))
        }
    }

    fun execute(command: CommandName, p1: String, p2: String) {
        if (canAcceptCommands) {
            cmd.onCommandWithParams(command.name, p1, p2) {}
        } else {
            queue.add(Triple(command, p1, p2))
        }
    }
}