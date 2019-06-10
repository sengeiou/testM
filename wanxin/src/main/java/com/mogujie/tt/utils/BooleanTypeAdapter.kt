package com.mogujie.tt.utils

import android.util.Log
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

/**
 * Created by wr
 * Date: 2019/6/10  14:39
 * mail: 1902065822@qq.com
 * describe:
 */
class BooleanTypeAdapter : TypeAdapter<Boolean>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Boolean?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    @Throws(IOException::class)
    override fun read(jsonReader: JsonReader): Boolean? {
        val peek = jsonReader.peek()
        when (peek) {
            JsonToken.BOOLEAN -> return jsonReader.nextBoolean()
            JsonToken.NULL -> {
                jsonReader.nextNull()
                return null
            }
            JsonToken.NUMBER ->{
                //如果为1则返回true，其他返回false
                Log.e("BooleanTypeAdapter","gson int 转 Boolean")
                return jsonReader.nextInt() == 1
            }
            else -> {
                Log.e("BooleanTypeAdapter","格式不对")
                return null
            }
        }
    }
}
