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
import channel.device.DeviceOps
import kotlinx.coroutines.flow.MutableStateFlow
import service.EnvironmentService
import service.FlutterService

typealias CloudActivityRetention = String

object DeviceBinding: DeviceOps {
    val cloudEnabled = MutableStateFlow(false)
    val retention = MutableStateFlow<CloudActivityRetention>("")
    val deviceTag = MutableStateFlow<String?>(null)

//    val dnsProfileConfiguredHot = writeDnsProfileActivated.filterNotNull().distinctUntilChanged()
//    val dnsProfileActivatedHot = dnsProfileConfiguredHot.map { it == PrivateDnsConfigured.CORRECT }

    private val flutter by lazy { FlutterService }
    private val command by lazy { CommandBinding }
    private val env by lazy { EnvironmentService }

    init {
        DeviceOps.setUp(flutter.engine.dartExecutor.binaryMessenger, this)
    }

    fun setActivityRetention(retention: CloudActivityRetention) {
        command.execute(CommandName.SETRETENTION, retention)
    }

    fun setPaused(paused: Boolean) {
        if (paused) command.execute(CommandName.DISABLECLOUD)
        else command.execute(CommandName.ENABLECLOUD)
    }

    // Produces a hostname where the subdomain is unique to user and has a few properties:
    // - is max 63 chars length (56 for device name and the rest for the tag)
    // - is only ascii (unicode is normalized to ascii in EnvironmentService)
    // - spaces are replaces with two dashes
    // - device name won't end with a dash
    fun getExpectedDnsString(): String? {
        val tag = deviceTag.value ?: return null
        val deviceName = env.getDeviceAlias().replace(" ", "--").take(56).trimEnd('-')
        return "$deviceName-$tag.cloud.blokada.org"
    }

    override fun doCloudEnabled(enabled: Boolean, callback: (Result<Unit>) -> Unit) {
        cloudEnabled.value = enabled
        callback(Result.success(Unit))
    }

    override fun doRetentionChanged(retention: String, callback: (Result<Unit>) -> Unit) {
        this.retention.value = retention
        callback(Result.success(Unit))
    }

    override fun doDeviceTagChanged(deviceTag: String, callback: (Result<Unit>) -> Unit) {
        this.deviceTag.value = deviceTag
        callback(Result.success(Unit))
    }
}