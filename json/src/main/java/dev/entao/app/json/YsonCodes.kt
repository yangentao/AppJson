@file:Suppress("unused", "MemberVisibilityCanBePrivate", "RemoveRedundantQualifierName")

package dev.entao.app.json

import dev.entao.app.basic.MyDate
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Blob
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import kotlin.reflect.KClass

object YsonCoders {
    val map: HashMap<KClass<out Any>, YsonConverter> = HashMap(16)

    operator fun get(cls: KClass<*>): YsonConverter? {
        return map[cls] ?: globalMap[cls]
    }

    operator fun set(cls: KClass<*>, c: YsonConverter) {
        map[cls] = c
    }

    private val globalMap: HashMap<KClass<out Any>, YsonConverter> = HashMap(64)

    init {
        globalMap[Boolean::class] = BoolYsonConverter
        globalMap[Byte::class] = ByteYsonConverter
        globalMap[Short::class] = ShortYsonConverter
        globalMap[Int::class] = IntYsonConverter
        globalMap[Long::class] = LongYsonConverter
        globalMap[BigInteger::class] = BigIntegerYsonConverter
        globalMap[Float::class] = FloatYsonConverter
        globalMap[Double::class] = DoubleYsonConverter
        globalMap[BigDecimal::class] = BigDecimalYsonConverter
        globalMap[Char::class] = CharYsonConverter
        globalMap[String::class] = StringYsonConverter
        globalMap[StringBuffer::class] = StringBufferYsonConverter
        globalMap[StringBuilder::class] = StringBuilderYsonConverter
        globalMap[java.util.Date::class] = DateYsonConverter
        globalMap[java.sql.Date::class] = SQLDateYsonConverter
        globalMap[java.sql.Time::class] = SQLTimeYsonConverter
        globalMap[java.sql.Timestamp::class] = SQLTimestampYsonConverter
        globalMap[java.sql.Blob::class] = BlobYsonConverter
        globalMap[ByteArray::class] = ByteArrayYsonConverter
        globalMap[BooleanArray::class] = BoolArrayYsonConverter
        globalMap[ShortArray::class] = ShortArrayYsonConverter
        globalMap[IntArray::class] = IntArrayYsonConverter
        globalMap[LongArray::class] = LongArrayYsonConverter
        globalMap[FloatArray::class] = FloatArrayYsonConverter
        globalMap[DoubleArray::class] = DoubleArrayYsonConverter
        globalMap[CharArray::class] = CharArrayYsonConverter
        globalMap[YsonObject::class] = ObjectYsonConverter
        globalMap[YsonArray::class] = YsonArrayYsonConverter
    }

}


interface YsonConverter {
    fun toYsonValue(v: Any): YsonValue
    fun fromYsonValue(yv: YsonValue): Any?
}

object ObjectYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): YsonObject? {
        return when (yv) {
            is YsonObject -> yv
            is YsonString -> YsonObject(yv.data)
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return v as YsonObject
    }
}

object YsonArrayYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): YsonArray? {
        return when (yv) {
            is YsonArray -> yv
            is YsonString -> YsonArray(yv.data)
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return v as YsonArray
    }
}

object BoolYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): Boolean? {
        return when (yv) {
            is YsonBool -> yv.data
            is YsonNum -> yv.data.toInt() == 1
            is YsonString -> yv.data == "true"
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonBool(v as Boolean)
    }
}

object ByteYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): Byte? {
        return when (yv) {
            is YsonNum -> yv.data.toByte()
            is YsonString -> yv.data.toByteOrNull()
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonNum(v as Byte)
    }
}

object ShortYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): Short? {
        return when (yv) {
            is YsonNum -> yv.data.toShort()
            is YsonString -> yv.data.toShortOrNull()
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonNum(v as Short)
    }
}

object IntYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): Int? {
        return when (yv) {
            is YsonNum -> yv.data.toInt()
            is YsonString -> yv.data.toIntOrNull()
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonNum(v as Int)
    }
}

object LongYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): Long? {
        return when (yv) {
            is YsonNum -> yv.data.toLong()
            is YsonString -> yv.data.toLongOrNull()
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonNum(v as Long)
    }
}


object FloatYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): Float? {
        return when (yv) {
            is YsonNum -> yv.data.toFloat()
            is YsonString -> yv.data.toFloatOrNull()
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonNum(v as Float)
    }
}

object DoubleYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): Double? {
        return when (yv) {
            is YsonNum -> yv.data.toDouble()
            is YsonString -> yv.data.toDoubleOrNull()
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonNum(v as Double)
    }
}

object BigDecimalYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): BigDecimal? {
        return when (yv) {
            is YsonNum -> BigDecimal(yv.data.toDouble())
            is YsonString -> yv.data.toBigDecimalOrNull()
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonNum(v as BigDecimal)
    }
}

object CharYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): Char? {
        return when (yv) {
            is YsonString -> yv.data.firstOrNull()
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonString(v as Char)
    }
}

object StringYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): String? {
        return when (yv) {
            is YsonString -> yv.data
            is YsonBlob -> yv.encoded
            is YsonBool -> yv.data.toString()
            is YsonNum -> yv.data.toString()
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonString(v as String)
    }
}

object StringBufferYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): StringBuffer? {
        return when (yv) {
            is YsonString -> StringBuffer(yv.data)
            is YsonBlob -> StringBuffer(yv.encoded)
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonString(v as StringBuffer)
    }
}

object StringBuilderYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): StringBuilder? {
        return when (yv) {
            is YsonString -> StringBuilder(yv.data)
            is YsonBlob -> StringBuilder(yv.encoded)
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonString(v as StringBuilder)
    }
}

object DateYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): java.util.Date? {
        return when (yv) {
            is YsonString -> MyDate.parseDateTime(yv.data)?.toDate
            is YsonNum -> java.util.Date(yv.data.toLong())
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonNum((v as java.util.Date).time)
    }
}

object SQLDateYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): java.sql.Date? {
        return when (yv) {
            is YsonString -> MyDate.parseDate(yv.data)?.toDateSQL
            is YsonNum -> java.sql.Date(yv.data.toLong())
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonString((v as Date).toString())
    }
}

object SQLTimeYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): java.sql.Time? {
        return when (yv) {
            is YsonString -> MyDate.parseTime(yv.data)?.toTimeSQL
            is YsonNum -> java.sql.Time(yv.data.toLong())
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonString((v as Time).toString())
    }
}

object SQLTimestampYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): java.sql.Timestamp? {
        return when (yv) {
            is YsonString -> MyDate.parseTimestamp(yv.data)?.toTimestamp
            is YsonNum -> java.sql.Timestamp(yv.data.toLong())
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonString((v as Timestamp).toString())
    }
}

object BigIntegerYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): BigInteger? {
        return when (yv) {
            is YsonNum -> BigInteger(yv.data.toString())
            is YsonString -> yv.data.toBigIntegerOrNull()
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonNum(v as BigInteger)
    }
}

object BlobYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): ByteArray? {
        return when (yv) {
            is YsonBlob -> yv.data
            is YsonString -> YsonBlob.decode(yv.data)
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonBlob(v as Blob)
    }
}

object BoolArrayYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): BooleanArray? {
        return (yv as? YsonArray)?.toBoolArray()
    }

    override fun toYsonValue(v: Any): YsonValue {
        v as BooleanArray
        val arr = YsonArray(v.size)
        for (b in v) {
            arr.add(YsonBool(b))
        }
        return arr
    }
}

object ByteArrayYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): ByteArray? {
        return when (yv) {
            is YsonBlob -> yv.data
            is YsonString -> YsonBlob.decode(yv.data)
            is YsonArray -> yv.toByteArray()
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        return YsonBlob(v as ByteArray)
    }
}

object ShortArrayYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): ShortArray? {
        return (yv as? YsonArray)?.toShortArray()
    }

    override fun toYsonValue(v: Any): YsonValue {
        v as ShortArray
        val arr = YsonArray(v.size)
        for (b in v) {
            arr.add(YsonNum(b))
        }
        return arr
    }
}

object IntArrayYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): IntArray? {
        return (yv as? YsonArray)?.toIntArray()
    }

    override fun toYsonValue(v: Any): YsonValue {
        v as IntArray
        val arr = YsonArray(v.size)
        for (b in v) {
            arr.add(YsonNum(b))
        }
        return arr
    }
}

object LongArrayYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): LongArray? {
        return (yv as? YsonArray)?.toLongArray()
    }

    override fun toYsonValue(v: Any): YsonValue {
        v as LongArray
        val arr = YsonArray(v.size)
        for (b in v) {
            arr.add(YsonNum(b))
        }
        return arr
    }
}

object FloatArrayYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): FloatArray? {
        return (yv as? YsonArray)?.toFloatArray()
    }

    override fun toYsonValue(v: Any): YsonValue {
        v as FloatArray
        val arr = YsonArray(v.size)
        for (b in v) {
            arr.add(YsonNum(b))
        }
        return arr
    }
}

object DoubleArrayYsonConverter : YsonConverter {
    override fun fromYsonValue(yv: YsonValue): DoubleArray? {
        return (yv as? YsonArray)?.toDoubleArray()
    }

    override fun toYsonValue(v: Any): YsonValue {
        v as DoubleArray
        val arr = YsonArray(v.size)
        for (b in v) {
            arr.add(YsonNum(b))
        }
        return arr
    }
}

object CharArrayYsonConverter : YsonConverter {

    override fun fromYsonValue(yv: YsonValue): Any? {
        return when (yv) {
            is YsonArray -> yv.toCharArray()
            is YsonString -> yv.data.toCharArray()
            else -> null
        }
    }

    override fun toYsonValue(v: Any): YsonValue {
        v as CharArray
        val arr = YsonArray(v.size)
        for (b in v) {
            arr.add(YsonString(b))
        }
        return arr
    }
}






