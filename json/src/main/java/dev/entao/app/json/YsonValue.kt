@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.app.json

import android.util.Base64
import kotlin.reflect.KClass

abstract class YsonValue {

    abstract fun yson(buf: StringBuilder)

    open fun yson(): String {
        val sb = StringBuilder(preferBufferSize())
        yson(sb)
        return sb.toString()
    }

    open fun preferBufferSize(): Int {
        return 64
    }

    override fun toString(): String {
        return yson()
    }

    val isCollection: Boolean get() = this is YsonObject || this is YsonArray


    @Suppress("UNCHECKED_CAST")
    fun <T : Any> toType(cls: KClass<T>): T? {
        return YsonCoders[cls]?.fromYsonValue(this) as? T
    }


    companion object {
        fun from(value: Any?): YsonValue {
            if (value == null) {
                return YsonNull.inst
            }
            if (value is YsonValue) {
                return value
            }
            return YsonCoders[value::class]?.toYsonValue(value) ?: YsonString(value.toString())
        }
    }
}

val YsonValue.asInt: Int? get() = toType(Int::class)
val YsonValue.asLong: Long? get() = toType(Long::class)
val YsonValue.asDouble: Double? get() = toType(Double::class)
val YsonValue.asFloat: Float? get() = toType(Float::class)
val YsonValue.asString: String? get() = toType(String::class)
val YsonValue.asObject: YsonObject? get() = this as? YsonObject
val YsonValue.asArray: YsonArray? get() = this as? YsonArray

val YsonValue.isNull: Boolean get() = this is YsonNull

class YsonNull private constructor() : YsonValue() {

    override fun yson(buf: StringBuilder) {
        buf.append("null")
    }

    override fun equals(other: Any?): Boolean {
        return other is YsonNull
    }

    override fun preferBufferSize(): Int {
        return 8
    }

    override fun hashCode(): Int {
        return 1000
    }

    companion object {
        val inst: YsonNull = YsonNull()
    }
}

class YsonNum(val data: Number) : YsonValue() {

    override fun yson(buf: StringBuilder) {
        buf.append(data.toString())
    }

    override fun equals(other: Any?): Boolean {
        if (other is YsonNum) {
            return other.data == data
        }
        return false
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    override fun preferBufferSize(): Int {
        return 12
    }

}

class YsonString(val data: String) : YsonValue() {

    constructor(v: Char) : this(String(charArrayOf(v)))
    constructor(v: StringBuffer) : this(v.toString())
    constructor(v: StringBuilder) : this(v.toString())

    override fun yson(buf: StringBuilder) {
        buf.append("\"")
        escapeJsonTo(buf, data)
        buf.append("\"")
    }

    override fun equals(other: Any?): Boolean {
        if (other is YsonString) {
            return other.data == data
        }
        return false
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    override fun preferBufferSize(): Int {
        return data.length + 8
    }
}

class YsonBool(val data: Boolean) : YsonValue() {
    override fun yson(buf: StringBuilder) {
        if (data) {
            buf.append("true")
        } else {
            buf.append("false")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is YsonBool) {
            return other.data == data
        }
        return false
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    override fun preferBufferSize(): Int {
        return 8
    }

    companion object {
        val True: YsonBool = YsonBool(true)
        val False: YsonBool = YsonBool(false)
    }
}


class YsonBlob(val data: ByteArray) : YsonValue() {

    constructor(v: java.sql.Blob) : this(v.getBytes(1, v.length().toInt()))

    override fun yson(buf: StringBuilder) {
        buf.append("\"")
        buf.append(encoded)
        buf.append("\"")
    }

    val encoded: String get() = encode(data)

    override fun equals(other: Any?): Boolean {
        if (other is YsonBlob) {
            return other.data.contentEquals(data)
        }
        return false
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    override fun preferBufferSize(): Int {
        return data.size * 4 / 3 + 4
    }

    companion object {
        fun encode(data: ByteArray): String {
            return Base64.encodeToString(data, Base64.URL_SAFE)
//			val e = Base64.getUrlEncoder()
//			return e.encodeToString(data)
        }

        fun decode(s: String): ByteArray {
            return Base64.decode(s, Base64.URL_SAFE)
//			return Base64.getUrlDecoder().decode(s)
        }

    }

}

// 12 == \f
private val jsonEscChars: Set<Char> = setOf('\b', '\t', '\n', 12.toChar(), '\r', '\\', '\"')

fun escapeJsonTo(out: StringBuilder, value: String) {
    var i = 0
    val length = value.length
    while (i < length) {
        when (val c = value[i]) {
            '"', '\\', '/' -> out.append('\\').append(c)
            '\t' -> out.append("\\t")
            '\b' -> out.append("\\b")
            '\n' -> out.append("\\n")
            '\r' -> out.append("\\r")
            12.toChar() -> out.append("\\f")
            else -> if (c.code <= 0x1F) {
                out.append(String.format("\\u%04x", c.code))
            } else {
                out.append(c)
            }
        }
        i++
    }
}

