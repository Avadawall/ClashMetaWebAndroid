package com.github.kr328.clash.service.util

import com.github.kr328.clash.core.model.CommonProfile
import com.github.kr328.clash.service.data.Imported
import com.github.kr328.clash.service.data.Pending


fun Pending.getCommon() : CommonProfile {
    return CommonProfile(this.uuid.toString(), this.name, this.type.ordinal,
        this.source, this.interval, true, "")
}

fun Imported.getCommon(): CommonProfile {
    return CommonProfile(this.uuid.toString(), this.name, this.type.ordinal,
        this.source, this.interval, true, "")

}