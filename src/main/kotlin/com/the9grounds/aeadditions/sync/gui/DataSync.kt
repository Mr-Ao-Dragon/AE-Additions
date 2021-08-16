package com.the9grounds.aeadditions.sync.gui

import com.the9grounds.aeadditions.Logger
import com.the9grounds.aeadditions.network.AEAPacketBuffer
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

/**
 * Again, taken from AE2 for more control.
 */
class DataSync<T>(val host: Any) {
    val fields = mutableMapOf<String, Field<*, T>>()
    
    init {
        collectMutableProperties(host, host::class)
    }
    
    fun collectMutableProperties(host: Any, clazz: KClass<*>) {
        for (property in clazz.memberProperties) {
            if (property is KMutableProperty1<*, *>) {
                if (property.hasAnnotation<GuiSync>()) {
                    val annotation = property.findAnnotation<GuiSync>()!!
                    
                    val value = annotation.key
                    
                    if (fields.containsKey(value)) {
                        throw IllegalStateException("Class ${host.javaClass} declares the same sync key twice: ${value}")
                    }
                    
                    fields[value] = Field.create<T>(host, property as KMutableProperty1<T, *>)
                }
            }
        }
    }

    fun hasChanges(): Boolean {
        for (value in fields.values) {
            if (value.hasChanges()) {
                return true
            }
        }
        return false
    }

    fun writeFull(data: AEAPacketBuffer) {
        writeFields(data, true)
    }
    
    fun writeUpdate(data: AEAPacketBuffer) {
        writeFields(data, false)
    }
    
    private fun writeFields(data: AEAPacketBuffer, includeUnchanged: Boolean) {
        for (item in fields) {
            if (includeUnchanged || item.value.hasChanges()) {
                data.writeString(item.key)
                item.value.write(data)
            }
        }
        
        data.writeString("-1")
    }
    
    fun readUpdate(data: AEAPacketBuffer) {
        var key = data.readString()
        var i = 0
        
        while (key != "-1") {
            val field = fields.get(key)
            
            if (field == null) {
                Logger.warn("Server sent update for GUI field %d, which we don't know.", key)
                
                key = data.readString()
                i++
                continue
            }
            
            field.read(data)
            
            key = data.readString()
            i++
        }
    }
    
    fun hasFields(): Boolean {
        return !fields.isEmpty()
    }
}