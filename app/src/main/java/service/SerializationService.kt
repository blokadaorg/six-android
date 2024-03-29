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

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import model.BlockaAfterUpdate
import model.BlockaConfig
import model.BlockaRepo
import model.BlockaRepoConfig
import model.BlockaRepoPayload
import model.BlockaRepoUpdate
import model.BlokadaException
import model.BypassedAppIds
import model.DnsWrapper
import model.LegacyAccount
import model.LocalConfig
import model.NetworkSpecificConfigs
import model.Packs
import model.SyncableConfig
import repository.TranslationPack
import java.util.Date
import kotlin.reflect.KClass

interface SerializationService {
    fun serialize(obj: Any): String
    fun <T: Any> deserialize(serialized: Any, type: KClass<T>): T
}

object JsonSerializationService : SerializationService {

    val moshi = Moshi.Builder()
        .add(Date::class.java, Rfc3339DateJsonAdapter())
        .build()

    override fun serialize(obj: Any): String {
        when (obj) {
            is Packs -> {
                val adapter = moshi.adapter(Packs::class.java)
                return adapter.toJson(obj)
            }
            is BlockaConfig -> {
                val adapter = moshi.adapter(BlockaConfig::class.java)
                return adapter.toJson(obj)
            }
            is LocalConfig -> {
                val adapter = moshi.adapter(LocalConfig::class.java)
                return adapter.toJson(obj)
            }
            is SyncableConfig -> {
                val adapter = moshi.adapter(SyncableConfig::class.java)
                return adapter.toJson(obj)
            }
            is DnsWrapper -> {
                val adapter = moshi.adapter(DnsWrapper::class.java)
                return adapter.toJson(obj)
            }
            is BypassedAppIds -> {
                val adapter = moshi.adapter(BypassedAppIds::class.java)
                return adapter.toJson(obj)
            }
            is TranslationPack -> {
                val adapter = moshi.adapter(TranslationPack::class.java)
                return adapter.toJson(obj)
            }
            is BlockaRepo -> {
                val adapter = moshi.adapter(BlockaRepo::class.java)
                return adapter.toJson(obj)
            }
            is BlockaRepoConfig -> {
                val adapter = moshi.adapter(BlockaRepoConfig::class.java)
                return adapter.toJson(obj)
            }
            is BlockaRepoUpdate -> {
                val adapter = moshi.adapter(BlockaRepoUpdate::class.java)
                return adapter.toJson(obj)
            }
            is BlockaRepoPayload -> {
                val adapter = moshi.adapter(BlockaRepoPayload::class.java)
                return adapter.toJson(obj)
            }
            is BlockaAfterUpdate -> {
                val adapter = moshi.adapter(BlockaAfterUpdate::class.java)
                return adapter.toJson(obj)
            }
            is NetworkSpecificConfigs -> {
                val adapter = moshi.adapter(NetworkSpecificConfigs::class.java)
                return adapter.toJson(obj)
            }
            is LegacyAccount -> {
                val adapter = moshi.adapter(LegacyAccount::class.java)
                return adapter.toJson(obj)
            }
            else -> throw BlokadaException("Unsupported type for json serialization: ${obj.javaClass}")
        }
    }

    override fun <T: Any> deserialize(serialized: Any, type: KClass<T>): T {
        serialized as String
        when (type) {
            Packs::class -> {
                val adapter = moshi.adapter(Packs::class.java)
                return adapter.fromJson(serialized) as T
            }
            BlockaConfig::class -> {
                val adapter = moshi.adapter(BlockaConfig::class.java)
                return adapter.fromJson(serialized) as T
            }
            LocalConfig::class -> {
                val adapter = moshi.adapter(LocalConfig::class.java)
                return adapter.fromJson(serialized) as T
            }
            SyncableConfig::class -> {
                val adapter = moshi.adapter(SyncableConfig::class.java)
                return adapter.fromJson(serialized) as T
            }
            DnsWrapper::class -> {
                val adapter = moshi.adapter(DnsWrapper::class.java)
                return adapter.fromJson(serialized) as T
            }
            Sheet.AdsCounter::class -> {
                val adapter = moshi.adapter(Sheet.AdsCounter::class.java)
                return adapter.fromJson(serialized) as T
            }
            BypassedAppIds::class -> {
                val adapter = moshi.adapter(BypassedAppIds::class.java)
                return adapter.fromJson(serialized) as T
            }
            TranslationPack::class -> {
                val adapter = moshi.adapter(TranslationPack::class.java)
                return adapter.fromJson(serialized) as T
            }
            BlockaRepo::class -> {
                val adapter = moshi.adapter(BlockaRepo::class.java)
                return adapter.fromJson(serialized) as T
            }
            BlockaRepoConfig::class -> {
                val adapter = moshi.adapter(BlockaRepoConfig::class.java)
                return adapter.fromJson(serialized) as T
            }
            BlockaRepoUpdate::class -> {
                val adapter = moshi.adapter(BlockaRepoUpdate::class.java)
                return adapter.fromJson(serialized) as T
            }
            BlockaRepoPayload::class -> {
                val adapter = moshi.adapter(BlockaRepoPayload::class.java)
                return adapter.fromJson(serialized) as T
            }
            BlockaAfterUpdate::class -> {
                val adapter = moshi.adapter(BlockaAfterUpdate::class.java)
                return adapter.fromJson(serialized) as T
            }
            NetworkSpecificConfigs::class -> {
                val adapter = moshi.adapter(NetworkSpecificConfigs::class.java)
                return adapter.fromJson(serialized) as T
            }
            LegacyAccount::class -> {
                val adapter = moshi.adapter(LegacyAccount::class.java)
                return adapter.fromJson(serialized) as T
            }
            else -> throw BlokadaException("Unsupported type for json deserialization: $type")
        }
    }

}

object NewlineSerializationService : SerializationService {
    override fun serialize(obj: Any): String {
        return when (obj) {
            else -> throw BlokadaException("Unsupported type for newline serialization: ${obj.javaClass}")
        }
    }

    override fun <T : Any> deserialize(serialized: Any, type: KClass<T>): T {
        serialized as String
        return when (type) {
            else -> throw BlokadaException("Unsupported type for newline deserialization: $type")
        }
    }

}

object PassthroughSerializationService : SerializationService {

    override fun serialize(obj: Any): String {
        throw BlokadaException("PassthroughSerializationService is only used for legacy import")
    }

    override fun <T: Any> deserialize(serialized: Any, type: KClass<T>): T {
        return serialized as T
    }

}