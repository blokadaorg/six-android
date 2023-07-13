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

import channel.deck.Deck
import channel.deck.DeckOps
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import model.Blocklist
import model.MappedBlocklist
import model.Pack
import service.FlutterService
import ui.advanced.decks.PackDataSource
import ui.advanced.decks.convertBlocklists

object DeckBinding: DeckOps {
    private val dataSource = PackDataSource

    private val writeBlocklists = MutableSharedFlow<List<Blocklist>?>(replay = 1)
    private val writePacks = MutableSharedFlow<List<Pack>?>(replay = 1)

    // Blocklists is server side representation of all known lists.
    private val blocklistsHot = writeBlocklists.filterNotNull()

    // Intermediate representation, internal to this class.
    private val mappedBlocklistsHot = blocklistsHot.map { b ->
        convertBlocklists(b.filter { !it.is_allowlist })
    }

    // Used internally to access the map synchronously
    private var mappedBlocklistsInternal = emptyList<MappedBlocklist>()
        @Synchronized set
        @Synchronized get

    // Packs is app's representation. One pack may have multiple configs.
    val packsHot = writePacks.filterNotNull()

    private val flutter by lazy { FlutterService }
    private val command by lazy { CommandBinding }

    init {
        DeckOps.setUp(flutter.engine.dartExecutor.binaryMessenger, this)
    }

    override fun doDecksChanged(decks: List<Deck>, callback: (Result<Unit>) -> Unit) {
    }

}