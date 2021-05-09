package com.dp.advancedgunnerycontrol.utils

import com.dp.advancedgunnerycontrol.typesandvalues.Suffixes
import com.dp.advancedgunnerycontrol.typesandvalues.defaultSuffixString
import com.dp.advancedgunnerycontrol.typesandvalues.suffixDescriptions

class SuffixSelector : CycleSelectorBase<Suffixes> {
    constructor() : super(Suffixes.values().toList())
    constructor(sfx: Suffixes?) : super(Suffixes.values().toList(), (sfx ?: Suffixes.NONE))
    override fun currentValueAsString(): String {
        return suffixDescriptions[currentValue] ?: defaultSuffixString
    }
}