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

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import utils.Logger

object NotificationPermissionService {

    const val requestCode: Int = 1

    private val log = Logger("NotifPerm")
    private val context = ContextService

    private val perms = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS,
    )

    var onPermissionGranted = {}

    fun hasPermission(): Boolean {
        val ctx = context.requireContext()
        return perms.map {
            ActivityCompat.checkSelfPermission(ctx, it)
        }.all {
            it == PackageManager.PERMISSION_GRANTED
        }
    }

    @TargetApi(33)
    fun askPermission() {
        log.w("Asking for notification permission")
        val activity = context.requireContext()
        if (activity !is Activity) {
            log.e("No activity context available")
            return
        }

        ActivityCompat.requestPermissions(activity, perms, requestCode)
    }

    fun resultReturned(result: IntArray) {
        if (result.all { it == PackageManager.PERMISSION_GRANTED }) {
            log.v("Notification permission granted")
            onPermissionGranted()
        } else log.w("Notification permission not granted")
    }

}