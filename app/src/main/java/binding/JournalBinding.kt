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

import channel.journal.JournalEntry
import channel.journal.JournalFilter
import channel.journal.JournalOps
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import model.CustomListEntry
import model.HistoryEntry
import service.FlutterService

object JournalBinding: JournalOps {
    // Ensure they emit on same value
    private val writeEntries = MutableSharedFlow<List<HistoryEntry>?>()
    private val writeCustomList = MutableSharedFlow<List<CustomListEntry>?>(replay = 1)

    val entriesHot = writeEntries.filterNotNull()

    val allowedListHot = writeCustomList.filterNotNull().map { c ->
        c.filter { it.action == "allow" }.map { it.domain_name }
    }

    val deniedListHot = writeCustomList.filterNotNull().map { c ->
        c.filter { it.action == "block" }.map { it.domain_name }
    }

    val customList = writeCustomList.filterNotNull()
    private val flutter by lazy { FlutterService }
    private val command by lazy { CommandBinding }

    init {
        JournalOps.setUp(flutter.engine.dartExecutor.binaryMessenger, this)
    }

    override fun doReplaceEntries(entries: List<JournalEntry>, callback: (Result<Unit>) -> Unit) {
        callback(Result.success(Unit))
    }

    override fun doFilterChanged(filter: JournalFilter, callback: (Result<Unit>) -> Unit) {
        callback(Result.success(Unit))
    }

    override fun doDevicesChanged(devices: List<String>, callback: (Result<Unit>) -> Unit) {
        callback(Result.success(Unit))
    }

    override fun doHistoricEntriesAvailable(
        entries: List<JournalEntry>,
        callback: (Result<Unit>) -> Unit
    ) {
        callback(Result.success(Unit))
    }
}