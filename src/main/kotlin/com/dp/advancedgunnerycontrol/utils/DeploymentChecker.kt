package com.dp.advancedgunnerycontrol.utils

import com.fs.starfarer.api.combat.CombatEngineAPI

/**
 * checks every frameInterval frames whether new Ships were deployed (roughly speaking)
 */
class DeploymentChecker(private var engine: CombatEngineAPI?) {
    private var wasInDeplyomentScreen = false
    private var selectedShips: List<String>? = null

    fun checkDeployment(): List<String>? {
        if (wasInDeplyomentScreen) { // check for selected ships
            if (engine?.combatUI?.isShowingDeploymentDialog == false) {
                wasInDeplyomentScreen = false
                return selectedShips
            }
            selectedShips = engine?.combatUI?.currentlySelectedInFleetDeploymentDialog
                ?.mapNotNull { it.id }?.ifEmpty { selectedShips }
            return null
        }

        wasInDeplyomentScreen = (engine?.combatUI?.isShowingDeploymentDialog == true)
        return null
    }
}