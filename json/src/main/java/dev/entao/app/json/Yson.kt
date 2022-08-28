@file:Suppress("unused")

package dev.entao.app.json

import kotlin.reflect.KClass
import kotlin.reflect.KType

object Yson {

    fun parseObject(text: String): YsonObject? {
        return YsonParser(text).parse(true) as? YsonObject
    }

    fun parseArray(text: String): YsonArray? {
        return YsonParser(text).parse(true) as? YsonArray
    }

    fun toYson(v: Any?): YsonValue {
        return YsonEncoder.encode(v, null)
    }

    fun toYson(v: Any?, config: YsonEncoderConfig?): YsonValue {
        return YsonEncoder.encode(v, config)
    }

    inline fun <reified T : Any> toModel(yson: YsonValue, config: YsonDecoderConfig? = null): T? {
        return YsonDecoder.decodeByClass(yson, T::class, config) as T?
    }

    fun toModelClass(yson: YsonValue, cls: KClass<*>, config: YsonDecoderConfig? = null): Any? {
        return YsonDecoder.decodeByClass(yson, cls, config)
    }

    inline fun <reified T : Any> toModelGeneric(yson: YsonValue, ktype: KType, config: YsonDecoderConfig? = null): T? {
        return YsonDecoder.decodeByType(yson, ktype, config) as T?
    }


    object Types {
        val ArrayListString: KType by lazy { object : TypeTake<ArrayList<String>>() {}.type }
        val ArrayListInt: KType by lazy { object : TypeTake<ArrayList<Int>>() {}.type }
        val ArrayListLong: KType by lazy { object : TypeTake<ArrayList<Long>>() {}.type }
        val HashMapStringString: KType by lazy { object : TypeTake<HashMap<String, String>>() {}.type }
        val HashMapStringInt: KType by lazy { object : TypeTake<HashMap<String, Int>>() {}.type }
        val HashMapStringLong: KType by lazy { object : TypeTake<HashMap<String, Long>>() {}.type }
    }
}

abstract class TypeTake<T> {

    val type: KType by lazy { this::class.supertypes.first().arguments.first().type!! }
}