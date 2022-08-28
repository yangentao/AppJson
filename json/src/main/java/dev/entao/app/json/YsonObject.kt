@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.app.json

import dev.entao.app.basic.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

class YsonObject(val data: LinkedHashMap<String, YsonValue> = LinkedHashMap(32)) : YsonValue(), Map<String, YsonValue> by data {
    var caseLess = false

    constructor(capcity: Int) : this(LinkedHashMap<String, YsonValue>(capcity))

    constructor(json: String) : this() {
        val p = YsonParser(json)
        val v = p.parse(true)
        if (v is YsonObject) {
            data.putAll(v.data)
        }
    }

    override fun yson(buf: StringBuilder) {
        buf.append("{")
        var first = true
        for ((k, v) in data) {
            if (!first) {
                buf.append(",")
            }
            first = false
            buf.append("\"")
            escapeJsonTo(buf, k)
            buf.append("\":")
            v.yson(buf)
        }
        buf.append("}")
    }

    override fun preferBufferSize(): Int {
        return 256
    }

    override fun toString(): String {
        return yson()
    }

    private val _changedProperties = ArrayList<KMutableProperty<*>>(8)
    private var gather: Boolean = false

    @Synchronized
    fun gather(block: () -> Unit): ArrayList<KMutableProperty<*>> {
        this.gather = true
        this._changedProperties.clear()
        block()
        val ls = ArrayList<KMutableProperty<*>>(_changedProperties)
        this.gather = false
        return ls
    }

    operator fun <V> setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        this.data[property.userName] = Yson.toYson(value)
        if (this.gather) {
            if (property is KMutableProperty) {
                if (property !in this._changedProperties) {
                    this._changedProperties.add(property)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <V> getValue(thisRef: Any?, property: KProperty<*>): V {
        val retType = property.returnType
        val v = if (caseLess) {
            this[property.userName] ?: this[property.userName.lowercase()]
        } else {
            this[property.userName]
        } ?: YsonNull.inst

        if (v !is YsonNull) {
            if (v::class == retType.classifier) {
                return v as V
            }
            val pv = YsonDecoder.decodeByType(v, retType, null)
            if (pv != null || retType.isMarkedNullable) {
                return pv as V
            }
        }
        if (retType.isMarkedNullable) {
            return null as V
        }
        val defVal = property.userDefaultValueText
        if (defVal != null) {
            return TextConvert.textToValue(defVal, property)
        }
        return TextConvert.defaultValueOf(property)
    }

    fun removeProperty(p: KProperty<*>) {
        this.data.remove(p.userName)
    }

    fun removeKey(key: String) {
        this.data.remove(key)
    }

    fun setString(key: String, value: String?) {
        if (value == null) {
            data[key] = YsonNull.inst
        } else {
            data[key] = YsonString(value)
        }
    }

    fun getString(key: String): String? {
        return when (val v = get(key)) {
            null -> null
            is YsonString -> v.data
            is YsonBool -> v.data.toString()
            is YsonNum -> v.data.toString()
            is YsonNull -> null
            is YsonObject -> v.toString()
            is YsonArray -> v.toString()
            else -> v.toString()
        }
    }

    fun setInt(key: String, value: Int?) {
        if (value == null) {
            data[key] = YsonNull.inst
        } else {
            data[key] = YsonNum(value)
        }
    }

    fun getInt(key: String): Int? {
        return when (val v = get(key)) {
            is YsonNum -> v.data.toInt()
            is YsonString -> v.data.toIntOrNull()
            else -> null
        }
    }

    fun setLong(key: String, value: Long?) {
        if (value == null) {
            data[key] = YsonNull.inst
        } else {
            data[key] = YsonNum(value)
        }
    }

    fun getLong(key: String): Long? {
        return when (val v = get(key)) {
            is YsonNum -> v.data.toLong()
            is YsonString -> v.data.toLongOrNull()
            else -> null
        }
    }

    fun setReal(key: String, value: Double?) {
        if (value == null) {
            data[key] = YsonNull.inst
        } else {
            data[key] = YsonNum(value)
        }
    }

    fun getReal(key: String): Double? {
        return when (val v = get(key)) {
            is YsonNum -> v.data.toDouble()
            is YsonString -> v.data.toDoubleOrNull()
            else -> null
        }
    }

    fun setBool(key: String, value: Boolean?) {
        if (value == null) {
            data[key] = YsonNull.inst
        } else {
            data[key] = YsonBool(value)
        }
    }

    fun getBool(key: String): Boolean? {
        val v = get(key) ?: return null
        return BoolYsonConverter.fromYsonValue(v)
    }

    fun setObject(key: String, value: YsonObject?) {
        if (value == null) {
            data[key] = YsonNull.inst
        } else {
            data[key] = value
        }
    }

    fun setObject(key: String, block: YsonObject.() -> Unit) {
        val yo = YsonObject()
        data[key] = yo
        yo.block()
    }

    fun getObject(key: String): YsonObject? {
        return get(key) as? YsonObject
    }

    fun setArray(key: String, value: YsonArray?) {
        if (value == null) {
            data[key] = YsonNull.inst
        } else {
            data[key] = value
        }
    }

    fun getArray(key: String): YsonArray? {
        return get(key) as? YsonArray
    }

    fun setArray(key: String, block: YsonArray.() -> Unit): YsonArray {
        val ls = YsonArray()
        data[key] = ls
        ls.block()
        return ls
    }


    fun setAny(key: String, value: Any?) {
        data[key] = from(value)
    }

    fun getAny(key: String): Any? {
        return get(key)
    }

    fun setNull(key: String) {
        data[key] = YsonNull.inst
    }

    infix fun <V> String.TO(value: V) {
        setAny(this, value)
    }


    infix fun String.TO(value: YsonObject) {
        setObject(this, value)
    }

    infix fun String.TO(value: YsonArray) {
        setArray(this, value)
    }

    companion object {
        init {
            TextConvert.putConvert(YsonObject::class, YsonObjectTextConvert)
        }
    }
}

object YsonObjectTextConvert : ITextConvert {
    override val defaultValue: Any = YsonObject()
    override fun fromText(text: String): Any {
        return YsonObject(text)
    }
}


fun <T : Any> KClass<T>.createYsonModel(argValue: YsonObject): T {
//    return this.createInstance(YsonObject::class, argValue)
    val c = this.constructors.first { it.parameters.size == 1 && it.parameters.first().type.classifier == YsonObject::class }
    return c.call(argValue)
}

fun ysonObject(block: YsonObject.() -> Unit): YsonObject {
    val b = YsonObject()
    b.block()
    return b
}

// data/user/name
// data/items/0/name
fun YsonObject.path(path: String, delimiter: Char): YsonValue {
    val ls = path.split(delimiter)
    var yv: YsonValue? = this
    if (yv is YsonNull) return YsonNull.inst

    ls.forEachIndexed { n, s ->
        yv = when (yv) {
            is YsonObject -> (yv as YsonObject)[s]
            is YsonArray -> {
                val idx = s.toIntOrNull() ?: return YsonNull.inst
                val yar = yv as YsonArray
                if (idx !in yar.indices) {
                    return YsonNull.inst
                }
                yar[idx]
            }
            else -> return YsonNull.inst
        }
        if (n == ls.size - 1) {
            return yv ?: YsonNull.inst
        }
    }
    return YsonNull.inst
}
