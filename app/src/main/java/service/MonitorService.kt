/*
 * This file is part of Blokada.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright © 2021 Blocka AB. All rights reserved.
 *
 * @author Karol Gusak (karol@blocka.net)
 */

package service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import binding.UiJournalEntry
import channel.app.AppStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import model.BlokadaException
import model.TunnelStatus
import ui.utils.cause
import utils.Logger
import utils.MonitorNotification


typealias Host = String

object MonitorService {

    private var strategy: MonitorServiceStrategy = SimpleMonitorServiceStrategy()

    fun setup(useForeground: Boolean) {
        if (useForeground) {
            strategy = ForegroundMonitorServiceStrategy()
        }

        strategy.setup()
    }

    fun setCounter(counter: Long) = strategy.setCounter(counter)
    fun setHistory(history: List<UiJournalEntry>) = strategy.setHistory(history)
    fun setTunnelStatus(tunnelStatus: TunnelStatus) = strategy.setTunnelStatus(tunnelStatus)
    fun setAppState(appState: AppStatus) = strategy.setAppState(appState)
    fun setWorking(working: Boolean) = strategy.setWorking(working)

}

private interface MonitorServiceStrategy {
    fun setup()
    fun setCounter(counter: Long)
    fun setHistory(history: List<UiJournalEntry>)
    fun setTunnelStatus(tunnelStatus: TunnelStatus)
    fun setAppState(appState: AppStatus)
    fun setWorking(working: Boolean)
}

// This strategy just shows the notification
private class SimpleMonitorServiceStrategy: MonitorServiceStrategy {

    private val notification = NotificationService

    private var counter: Long = 0
    private var lastDenied: List<Host> = emptyList()
    private var tunnelStatus: TunnelStatus = TunnelStatus.off()
    private var appState: AppStatus = AppStatus.DEACTIVATED
    private var working: Boolean = false

    override fun setup() {}

    override fun setCounter(counter: Long) {
        this.counter = counter
        updateNotification()
    }

    override fun setHistory(history: List<UiJournalEntry>) {
        lastDenied = history.sortedByDescending { it.time }.take(3).map { it.entry.domainName }
        updateNotification()
    }

    override fun setTunnelStatus(tunnelStatus: TunnelStatus) {
        this.tunnelStatus = tunnelStatus
        updateNotification()
    }

    override fun setAppState(appState: AppStatus) {
        this.appState = appState
        updateNotification()
    }

    override fun setWorking(working: Boolean) {
        this.working = working
        updateNotification()
    }

    private fun updateNotification() {
        val prototype = MonitorNotification(tunnelStatus, counter, lastDenied, appState, working)
        notification.show(prototype)
    }

}

// This strategy keeps the app alive while showing the notification
private class ForegroundMonitorServiceStrategy: MonitorServiceStrategy {

    private val log = Logger("Monitor")
    private val context = ContextService
    private val scope = GlobalScope

    private var connection: ForegroundConnection? = null
        @Synchronized get
        @Synchronized set

    override fun setup() {
        try {
            log.v("Starting Foreground Service")
            val ctx = context.requireAppContext()
            val intent = Intent(ctx, ForegroundService::class.java)
            ctx.startForegroundService(intent)
        } catch (ex: Exception) {
            log.w("Could not start ForegroundService".cause(ex))
        }

        scope.launch {
            // To initially bind to the ForegroundService
            getConnection()
        }
    }

    override fun setCounter(counter: Long) {
        scope.launch {
            getConnection().binder.onNewStats(counter, null, null, null, null, null)
        }
    }

    override fun setHistory(history: List<UiJournalEntry>) {
        scope.launch {
            val lastDenied = history.sortedByDescending { it.time }.take(3).map { it.entry.domainName }
            getConnection().binder.onNewStats(null, lastDenied, null, null, null, null)
        }
    }

    override fun setTunnelStatus(tunnelStatus: TunnelStatus) {
        scope.launch {
            getConnection().binder.onNewStats(null, null, tunnelStatus, null, null, null)
        }
    }

    override fun setAppState(appState: AppStatus) {
        scope.launch {
            getConnection().binder.onNewStats(null, null, null, null, appState, null)
        }
    }

    override fun setWorking(working: Boolean) {
        scope.launch {
            getConnection().binder.onNewStats(null, null, null, null, null, working)
        }
    }

    private suspend fun getConnection(): ForegroundConnection {
        return connection ?: run {
            val deferred = CompletableDeferred<ForegroundBinder>()
            val connection = bind(deferred)
            deferred.await()
            this.connection = connection
            connection
        }
    }

    private suspend fun bind(deferred: ConnectDeferred): ForegroundConnection {
        val ctx = context.requireAppContext()
        val intent = Intent(ctx, ForegroundService::class.java).apply {
            action = FOREGROUND_BINDER_ACTION
        }

        val connection = ForegroundConnection(deferred,
            onConnectionClosed = {
                this.connection = null
            })

        if (!ctx.bindService(intent, connection,
                Context.BIND_AUTO_CREATE or Context.BIND_ABOVE_CLIENT or Context.BIND_IMPORTANT
            )) {
            deferred.completeExceptionally(BlokadaException("Could not bindService()"))
        }
        return connection
    }

}

class ForegroundService: Service() {

    private val notification = NotificationService
    private var binder: ForegroundBinder? = null

    private var counter: Long = 0
    private var lastDenied: List<Host> = emptyList()
    private var tunnelStatus: TunnelStatus = TunnelStatus.off()
    private var appState: AppStatus = AppStatus.DEACTIVATED
    private var working: Boolean = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ContextService.setApp(this.application)
        updateNotification()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        if (FOREGROUND_BINDER_ACTION == intent?.action) {
            ContextService.setApp(this.application)
            binder = ForegroundBinder { counter, lastDenied, tunnelStatus, dnsLabel, appState, working ->
                this.counter = counter ?: this.counter
                this.lastDenied = lastDenied ?: this.lastDenied
                this.tunnelStatus = tunnelStatus ?: this.tunnelStatus
                this.appState = appState ?: this.appState
                this.working = working ?: this.working
                updateNotification()
            }
            return binder
        }
        return null
    }

    private fun updateNotification() {
        val prototype = MonitorNotification(tunnelStatus, counter, lastDenied, appState, working)
        val n = notification.build(prototype)
        try {
            startForeground(prototype.id, n)
        } catch (ex: Exception) {
            Logger.e("ForegroundService", "Could not startForeground()".cause(ex));
        }
    }

}

class ForegroundBinder(
    val onNewStats: (
        counter: Long?, lastDenied: List<Host>?,
        tunnelStatus: TunnelStatus?, dnsLabel: String?,
        appState: AppStatus?, working: Boolean?
    ) -> Unit
) : Binder()

const val FOREGROUND_BINDER_ACTION = "ForegroundBinder"

private class ForegroundConnection(
    private val deferred: ConnectDeferred,
    val onConnectionClosed: () -> Unit
): ServiceConnection {

    private val log = Logger("ForegroundConnection")

    lateinit var binder: ForegroundBinder

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        this.binder = binder as ForegroundBinder
        deferred.complete(this.binder)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        log.w("onServiceDisconnected")
        onConnectionClosed()
    }

}

private typealias ConnectDeferred = CompletableDeferred<ForegroundBinder>
