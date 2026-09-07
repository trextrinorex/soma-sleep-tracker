package com.somna.sleeptracker.data.local.entity

enum class SystemEventType {
    SCREEN_OFF,
    SCREEN_ON,
    USER_PRESENT,            // Device unlocked
    POWER_CONNECTED_AC,
    POWER_CONNECTED_WIRELESS,
    POWER_DISCONNECTED,
    APP_CATEGORY_ACCESSED
}
