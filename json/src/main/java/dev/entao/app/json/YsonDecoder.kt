@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.app.json


import dev.entao.app.basic.genericArgs
import dev.entao.app.basic.isGeneric
import dev.entao.app.basic.isPublic
import dev.entao.app.basic.userName
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

class YsonDecoderConfig {
    val map: HashMap<KClass<*>, YsonConverter> = HashMap()

    operator fun get(cls: KClass<*>): YsonConverter? {
        return map[cls]
    }
}

object YsonDecoder {

    inline fun <reified T : Any> decodeT(yson: YsonValue): T {
        return decodeByClass(yson, T::class, null) as T
    }

    //IntArray, Array<T>, Map
    fun decodeByClass(yson: YsonValue, cls: KClass<*>, config: YsonDecoderConfig?): Any? {
        if (yson is YsonNull) {
            return if (cls == YsonNull::class) {
                YsonNull.inst
            } else {
                null
            }
        }
        if (yson::class == cls) {
            return yson
        }

        val d = config?.get(cls) ?: YsonCoders[cls]
        if (d != null) {
            return d.fromYsonValue(yson)
        }
        val model: Any = cls.createInstance()
        val yo: YsonObject = yson as? YsonObject ?: throw YsonError("期望是YsonObject")
        val mems: List<KMutableProperty1<*, *>> = cls.memberProperties.filter { it.isPublic && it is KMutableProperty1 && !it.isAbstract && !it.isConst }.map { (it as KMutableProperty1) }
        for ((k, v) in yo) {
            val p = mems.find { it.userName == k } ?: continue
            val pt = p.returnType
            val pcls = pt.classifier as KClass<*>
            val pv = if (pt.isGeneric) {
                decodeByType(v, pt, config)
            } else {
                val d2 = config?.get(pcls) ?: YsonCoders[pcls]
                if (d2 != null) {
                    d2.fromYsonValue(v)
                } else {
                    decodeByClass(v, pcls, config)
                }
            }
            if (pv != null) {
                p.setter.call(model, pv)
            } else {
                if (pt.isMarkedNullable) {
                    p.setter.call(model, pv)
                }
                //else  use default value

            }
        }
        return model
    }

    fun decodeByType(yson: YsonValue, ktype: KType, config: YsonDecoderConfig?): Any? {
        val cls = ktype.classifier as KClass<*>
        if (!ktype.isGeneric) {
            return decodeByClass(yson, cls, config)
        }
        if (yson is YsonNull) {
            return null
        }

        if (cls.java.isArray) {
            throw YsonError("不支持泛型数组Array<T>,请用ArrayList<T>代替")
        }
        val argList = ktype.genericArgs
        val inst = cls.createInstance()
        if (inst is MutableCollection<*>) {
            val yarr: YsonArray = when (yson) {
                is YsonArray -> yson
                is YsonString -> {
                    Yson.parseArray(yson.data) ?: throw YsonError("类型不匹配")
                }
                else -> throw YsonError("类型不匹配")
            }
            val addFun = cls.memberFunctions.find { it.name == "add" && it.parameters.size == 2 } ?: throw YsonError("没有add 方法")
            val arg = argList.first()
            val argType = arg.type ?: throw YsonError("type是null")
            val argCls = argType.classifier as KClass<*>
            val canNull = arg.type!!.isMarkedNullable
            for (yv in yarr) {
                val v = if (argType.isGeneric) {
                    decodeByType(yv, argType, config)
                } else {
                    decodeByClass(yv, argCls, config)
                }
                if (v != null) {
                    addFun.call(inst, v)
                } else {
                    if (canNull) {
                        addFun.call(inst, null)
                    }
                }
            }
        } else if (inst is MutableMap<*, *>) {
            val yobj: YsonObject = when (yson) {
                is YsonObject -> yson
                is YsonString -> {
                    Yson.parseObject(yson.data) ?: throw YsonError("类型不匹配")
                }
                else -> throw YsonError("类型不匹配")
            }
            val argKey = argList.first()
            if (argKey.type?.classifier != String::class) {
                throw YsonError("MutableMap的键必须是String")
            }
            val typeVal = argList[1].type ?: throw YsonError("类型不匹配")
            val putFun = cls.memberFunctions.find { it.name == "put" && it.parameters.size == 3 } ?: throw YsonError("没有put 方法")
            for ((key, yv) in yobj) {
                val v = decodeByType(yv, typeVal, config)
                if (v != null) {
                    putFun.call(inst, key, v)
                } else {
                    if (typeVal.isMarkedNullable) {
                        putFun.call(inst, key, null)
                    }
                }
            }
        }
        return inst
    }

}


