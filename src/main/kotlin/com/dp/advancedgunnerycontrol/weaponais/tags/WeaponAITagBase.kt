package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.assignShipMode
import com.dp.advancedgunnerycontrol.typesandvalues.hasCustomAI
import com.dp.advancedgunnerycontrol.typesandvalues.getCustomShipAI
import com.dp.advancedgunnerycontrol.weaponais.isPD
import com.dp.advancedgunnerycontrol.weaponais.shipais.ShipCommandWrapper
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

abstract class WeaponAITagBase(protected val weapon: WeaponAPI) {
    open fun isValidTarget(entity: CombatEntityAPI) : Boolean{
        if(entity is MissileAPI){
            return isPD(weapon)
        }
        return true
    }
    abstract fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f) : Float
    abstract fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f) : Boolean
    abstract fun isBaseAiOverridable() : Boolean
    abstract fun avoidDebris() : Boolean

    open fun isBaseAiValid(entity: CombatEntityAPI) : Boolean{
        return isValidTarget(entity)
    }

    open fun isValid() : Boolean {
        return !Settings.weaponBlacklist.contains(weapon.id)
    }
    open fun forceFire(entity: CombatEntityAPI?, predictedLocation: Vector2f?) : Boolean = false
    open fun advance(){}
    // Note: if true, advance will be called every frame, even if the weapon group is not set to autofire!
    open val advanceWhenTurnedOff : Boolean = false

    companion object{
        fun forceAutofire(ship: ShipAPI, groupIndex: Int){
            if(ship.shipAI == null) return // mode is only applicable for AI-controlled ships
            if( !hasCustomAI(ship)){
                assignShipMode(listOf(), ship, true)
            }
            val ai = getCustomShipAI(ship) ?: return
            if(ship.weaponGroupsCopy?.getOrNull(groupIndex)?.isAutofiring == false){
                if(!ai.containsFleetingCommand(ShipCommand.TOGGLE_AUTOFIRE, groupIndex)){
                    ai.addFleetingCommand(ShipCommandWrapper(ShipCommand.TOGGLE_AUTOFIRE, null, groupIndex))
                }
            }
            if(ship.selectedGroupAPI ==  ship.weaponGroupsCopy?.getOrNull(groupIndex)){
                if(!ai.containsFleetingCommand(ShipCommand.SELECT_GROUP)){
                    ai.addFleetingCommand(ShipCommandWrapper(ShipCommand.SELECT_GROUP, null, (ship.weaponGroupsCopy?.size ?: 0)))
                }
            }
        }
    }
}