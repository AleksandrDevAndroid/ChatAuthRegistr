package ru.netology.nmedia.util

import android.os.Bundle
import ru.netology.nmedia.dto.Post
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object StringArg: ReadWriteProperty<Bundle, String?> {

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: String?) {
        thisRef.putString(property.name, value)
    }

    override fun getValue(thisRef: Bundle, property: KProperty<*>): String? =
        thisRef.getString(property.name)
}
