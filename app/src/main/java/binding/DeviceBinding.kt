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

import channel.device.DeviceOps
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import model.DevicePayload
import model.PrivateDnsConfigured
import service.EnvironmentService
import service.FlutterService

object DeviceBinding: DeviceOps {
    private val writeDeviceInfo = MutableSharedFlow<DevicePayload?>(replay = 1)
    private val writeDnsProfileActivated = MutableStateFlow<PrivateDnsConfigured?>(null)
    private val writePrivateDnsSetting = MutableStateFlow<String?>(null)

    val deviceInfoHot = writeDeviceInfo.filterNotNull()
    val activityRetentionHot = deviceInfoHot.map { it.retention }
    val deviceTagHot = deviceInfoHot.map { it.device_tag }.distinctUntilChanged()
    val blocklistsHot = deviceInfoHot.map { it.lists }.distinctUntilChanged()
    val adblockingPausedHot = deviceInfoHot.map { it.paused }.distinctUntilChanged()

    // Produces a hostname where the subdomain is unique to user and has a few properties:
    // - is max 63 chars length (56 for device name and the rest for the tag)
    // - is only ascii (unicode is normalized to ascii in EnvironmentService)
    // - spaces are replaces with two dashes
    // - device name won't end with a dash
    val expectedDnsStringHot = deviceInfoHot.map {
           val deviceName = env.getDeviceAlias().replace(" ", "--").take(56).trimEnd('-')
            val tag = it.device_tag
            "$deviceName-$tag.cloud.blokada.org"
        }

    val dnsProfileConfiguredHot = writeDnsProfileActivated.filterNotNull().distinctUntilChanged()
    val dnsProfileActivatedHot = dnsProfileConfiguredHot.map { it == PrivateDnsConfigured.CORRECT }

    private val env by lazy { EnvironmentService }
    private val flutter by lazy { FlutterService }
    private val command by lazy { CommandBinding }

    init {
        DeviceOps.setUp(flutter.engine.dartExecutor.binaryMessenger, this)
    }

    override fun doCloudEnabled(enabled: Boolean, callback: (Result<Unit>) -> Unit) {
        callback(Result.success(Unit))
    }

    override fun doRetentionChanged(retention: String, callback: (Result<Unit>) -> Unit) {
        callback(Result.success(Unit))
    }

    override fun doDeviceTagChanged(deviceTag: String, callback: (Result<Unit>) -> Unit) {
        callback(Result.success(Unit))
    }
}