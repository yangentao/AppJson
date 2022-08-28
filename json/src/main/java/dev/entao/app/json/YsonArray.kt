@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.app.json

import dev.entao.app.basic.ITextConvert
import dev.entao.app.basic.TextConvert


class YsonArray(val data: ArrayList<YsonValue> = ArrayList(16)) : YsonValue(), List<YsonValue> by data {

    constructor(capcity: Int) : this(ArrayList<YsonValue>(capcity))

    constructor(json: String) : this() {
        val v = YsonParser(json).parse(true)
        if (v is YsonArray) {
            data.addAll(v.data)
        }
    }

    override fun yson(buf: StringBuilder) {
        buf.append("[")
        for (i in data.indices) {
            if (i != 0) {
                buf.append(",")
            }
            data[i].yson(buf)
        }
        buf.append("]")
    }

    override fun preferBufferSize(): Int {
        return data.sumOf { it.preferBufferSize() }
    }

    override fun toString(): String {
        return yson()
    }

    fun toBoolArray(): BooleanArray {
        return this.map { (it as YsonBool).data }.toBooleanArray()
    }

    fun toByteArray(): ByteArray {
        return this.map { (it as YsonNum).data.toByte() }.toByteArray()
    }

    fun toShortArray(): ShortArray {
        return this.map { (it as YsonNum).data.toShort() }.toShortArray()
    }

    fun toIntArray(): IntArray {
        return this.map { (it as YsonNum).data.toInt() }.toIntArray()
    }

    fun toLongArray(): LongArray {
        return this.map { (it as YsonNum).data.toLong() }.toLongArray()
    }

    fun toFloatArray(): FloatArray {
        return this.map { (it as YsonNum).data.toFloat() }.toFloatArray()
    }

    fun toDoubleArray(): DoubleArray {
        return this.map { (it as YsonNum).data.toDouble() }.toDoubleArray()
    }

    fun toCharArray(): CharArray {
        return this.map { (it as YsonString).data.first() }.toCharArray()
    }

    fun toStringArray(): Array<String> {
        return this.map { (it as YsonString).data }.toTypedArray()
    }

    fun toByteList(): List<Byte> {
        return this.map { (it as YsonNum).data.toByte() }
    }

    fun toShortList(): List<Short> {
        return this.map { (it as YsonNum).data.toShort() }
    }

    fun toIntList(): List<Int> {
        return this.map { (it as YsonNum).data.toInt() }
    }

    fun toLongList(): List<Long> {
        return this.map { (it as YsonNum).data.toLong() }
    }

    fun toFloatList(): List<Float> {
        return this.map { (it as YsonNum).data.toFloat() }
    }

    fun toDoubleList(): List<Double> {
        return this.map { (it as YsonNum).data.toDouble() }
    }

    fun toCharList(): List<Char> {
        return this.map { (it as YsonString).data.first() }
    }

    fun toStringList(): List<String> {
        return this.map { (it as YsonString).data }
    }

    fun toObjectList(): List<YsonObject> {
        return this.map { it as YsonObject }
    }

    inline fun <reified T : Any> toList(): List<T> {
        return this.mapNotNull {
            YsonDecoder.decodeT(it)
        }
    }

    fun add(value: String?) {
        if (value == null) {
            data.add(YsonNull.inst)
        } else {
            data.add(YsonString(value))
        }
    }

    fun add(value: Boolean?) {
        if (value == null) {
            data.add(YsonNull.inst)
        } else {
            data.add(YsonBool(value))
        }
    }

    fun add(value: Int?) {
        if (value == null) {
            data.add(YsonNull.inst)
        } else {
            data.add(YsonNum(value))
        }
    }

    fun add(value: Long?) {
        if (value == null) {
            data.add(YsonNull.inst)
        } else {
            data.add(YsonNum(value))
        }
    }

    fun add(value: Float?) {
        add(value?.toDouble())
    }

    fun add(value: Double?) {
        if (value == null) {
            data.add(YsonNull.inst)
        } else {
            data.add(YsonNum(value))
        }
    }

    fun addBlob(value: ByteArray?) {
        if (value == null) {
            data.add(YsonNull.inst)
        } else {
            data.add(YsonBlob(value))
        }
    }

    fun add(value: YsonValue?) {
        if (value == null) {
            data.add(YsonNull.inst)
        } else {
            data.add(value)
        }
    }

    fun addAny(value: Any?) {
        data.add(from(value))
    }

    inline fun eachObject(block: (YsonObject) -> Unit) {
        for (item in this) {
            block(item as YsonObject)
        }
    }

    inline fun <R> mapObject(transform: (YsonObject) -> R): ArrayList<R> {
        val ls = ArrayList<R>(this.size)
        for (yo in this) {
            ls += transform(yo as YsonObject)
        }
        return ls
    }

    fun addObject(block: YsonObject.() -> Unit) {
        val yo = YsonObject()
        this.add(yo)
        yo.block()
    }

    companion object {
        init {
            TextConvert.putConvert(YsonArray::class, YsonArrayTextConvert)
        }
    }
}

object YsonArrayTextConvert : ITextConvert {
    override val defaultValue: Any = YsonArray()
    override fun fromText(text: String): Any {
        return YsonArray(text)
    }
}

fun ysonArray(values: Collection<Any?>): YsonArray {
    val arr = YsonArray()
    for (v in values) {
        arr.addAny(v)
    }
    return arr
}

fun ysonArray(vararg values: Any): YsonArray {
    val arr = YsonArray()
    for (v in values) {
        arr.addAny(v)
    }
    return arr
}

fun <T> ysonArray(values: Collection<T>, block: (T) -> Any?): YsonArray {
    val arr = YsonArray()
    for (v in values) {
        arr.addAny(block(v))
    }
    return arr
}

fun ysonArray(block: YsonArray.() -> Unit): YsonArray {
    val ya = YsonArray()
    ya.block()
    return ya
}