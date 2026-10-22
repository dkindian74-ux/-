package com.example.util

import com.example.model.AttendanceStatus

object AttendanceSerializer {

    /**
     * Serializes day -> AttendanceStatus map to compact string: "1:P,2:H,3:A..."
     */
    fun serialize(map: Map<Int, AttendanceStatus>): String {
        return map.entries.sortedBy { it.key }
            .joinToString(separator = ",") { "${it.key}:${it.value.code}" }
    }

    /**
     * Deserializes attendance string to day -> AttendanceStatus map.
     * Any missing dates default to AttendanceStatus.PRESENT.
     */
    fun deserialize(serialized: String?, totalDays: Int): Map<Int, AttendanceStatus> {
        val result = (1..totalDays).associateWith { AttendanceStatus.PRESENT }.toMutableMap()
        if (serialized.isNullOrBlank()) return result

        val parts = serialized.split(",")
        for (part in parts) {
            val sub = part.split(":")
            if (sub.size == 2) {
                val day = sub[0].toIntOrNull()
                if (day != null && day in 1..totalDays) {
                    result[day] = AttendanceStatus.fromCode(sub[1])
                }
            }
        }
        return result
    }
}
