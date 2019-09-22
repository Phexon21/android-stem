package com.example.placeholder.data.helpers.android

class AndroidSourceSetWrapper(private val androidSourceSet: Any) {

    private val clazz = androidSourceSet.javaClass
    private val getResMethod by lazy { clazz.getMethod("getRes") }
    private val getNameMethod by lazy { clazz.getMethod("getName") }

    fun getRes(): AndroidSourceDirectorySetWrapper {
        return AndroidSourceDirectorySetWrapper(getResMethod.invoke(androidSourceSet))
    }

    fun getName(): String {
        return getNameMethod.invoke(androidSourceSet) as String
    }
}