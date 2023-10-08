package com.dp.advancedgunnerycontrol.utils
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.ui.UIPanelAPI

fun UIPanelAPI.getChildren(): List<UIComponentAPI>{
    return invokeMethodByName("getChildrenCopy", this) as List<UIComponentAPI>
}

