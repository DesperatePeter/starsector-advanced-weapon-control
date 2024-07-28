package com.dp.advancedgunnerycontrol.gui.refitscreen

import com.dp.advancedgunnerycontrol.utils.generateUniversalFleetMemberId
import com.fs.starfarer.api.combat.ShipAPI

class ModuleIdManager {
    companion object{
        private var moduleIds = listOf<String>()
        private var parentId = ""

        fun getUniversalIdIfApplicable(ship: ShipAPI): String?{
            if(!RefitScreenHandler.isRefit) return null
            if(ship.variant?.hullVariantId in moduleIds) {
                return generateUniversalFleetMemberId(parentId, moduleIds.indexOf(ship.variant.hullVariantId))
            }
            return null
        }
    }


    fun update(ship: ShipAPI){
        var shipId = generateUniversalFleetMemberId(ship)
        if(ship.variant?.stationModules?.isEmpty() != true){
            moduleIds = ship.variant?.stationModules?.values?.toList() ?: emptyList()
            parentId = shipId
        }else if(ship.variant?.hullVariantId !in moduleIds){
            moduleIds = emptyList()
            parentId = ""
        }
    }

}