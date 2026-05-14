package com.dp.advancedgunnerycontrol.gui.controls.suppression

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.modals.muteCampaignButtonSounds

import com.dp.advancedgunnerycontrol.reflection.invokeMethodByName
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import java.util.Collections
import java.util.IdentityHashMap

internal object CampaignButtonSuppression {
    private val registeredCampaignButtons = mutableListOf<ButtonAPI>()

    fun campaignButtonContainsEvent(button: ButtonAPI, event: InputEventAPI): Boolean {
        if (event.isConsumed) return false
        val position = button.position ?: return false
        if (position.containsEvent(event)) return true
        val x = event.x.toFloat()
        val y = event.y.toFloat()
        return x >= position.x &&
            x <= position.x + position.width &&
            y >= position.y &&
            y <= position.y + position.height
    }

    fun consumeUnhandledAgcEditorInput(events: MutableList<InputEventAPI>) {
        events.forEach { event ->
            if (!event.isConsumed && (event.isKeyboardEvent || event.isMouseEvent)) {
                event.consume()
            }
        }
    }

    fun playCampaignButtonPressedSound() {
        Global.getSoundPlayer().playUISound("ui_button_pressed", 1f, 1f)
    }

    fun restoreCampaignButtonSounds(button: ButtonAPI) {
        button.setMouseOverSound("ui_button_mouseover")
        button.setButtonPressedSound("ui_button_pressed")
        button.setButtonDisabledPressedSound("ui_button_disabled_pressed")
    }

    fun suppressControlHoverSnapshot(controls: Iterable<ButtonBase<*>>): CampaignButtonSuppressionSnapshot {
        return CampaignButtonSuppressionSnapshot.suppress(controls)
    }

    fun suppressRawButtonHoverSnapshot(buttons: Iterable<ButtonAPI>): CampaignButtonSuppressionSnapshot {
        return CampaignButtonSuppressionSnapshot.suppressButtons(buttons)
    }

    fun applyDisabledCampaignButtonTemplate(
        control: ButtonBase<*>,
        showTooltipWhileInactive: Boolean = false,
    ): ButtonBase<*> {
        control.disable()
        muteCampaignButtonSounds(control.button)
        control.button.setShowTooltipWhileInactive(showTooltipWhileInactive)
        return control
    }

    fun suppressCampaignButtonHover(control: ButtonBase<*>): ButtonBase<*> {
        control.syncVisualCheckedToActive()
        suppressCampaignButtonHover(control.button)
        return control
    }

    fun suppressCampaignButtonHover(button: ButtonAPI) {
        button.isEnabled = false
        muteCampaignButtonSounds(button)
        button.setShowTooltipWhileInactive(false)
    }

    fun bindCampaignButtonListener(
        button: ButtonAPI,
        listenerPanel: CustomPanelAPI?,
        narrativeContext: String,
    ): ButtonAPI {
        button.setShowTooltipWhileInactive(true)
        listenerPanel?.let {
            invokeMethodByName("setListener", button, it, narrativeContext = narrativeContext)
        }
        return button
    }

    fun registerCampaignButton(button: ButtonAPI): ButtonAPI {
        registeredCampaignButtons.add(button)
        return button
    }

    fun clearRegisteredCampaignButtons() {
        registeredCampaignButtons.clear()
    }

    fun suppressRegisteredCampaignButtonHoverSnapshot(): CampaignButtonSuppressionSnapshot {
        return suppressRawButtonHoverSnapshot(registeredCampaignButtons)
    }

    fun uniqueCampaignButtons(buttons: Iterable<ButtonAPI>): List<ButtonAPI> {
        val seen = Collections.newSetFromMap(IdentityHashMap<ButtonAPI, Boolean>())
        return buttons.filter { button -> seen.add(button) }
    }
}

class CampaignButtonSuppressionSnapshot private constructor(
    private val states: List<State>,
) {
    private data class State(
        val button: ButtonAPI,
        val wasEnabled: Boolean,
    )

    fun restore() {
        states.forEach { state ->
            state.button.isEnabled = state.wasEnabled
            if (state.wasEnabled) {
                CampaignButtonSuppression.restoreCampaignButtonSounds(state.button)
            } else {
                muteCampaignButtonSounds(state.button)
                state.button.setShowTooltipWhileInactive(true)
            }
        }
    }

    companion object {
        fun suppress(controls: Iterable<ButtonBase<*>>): CampaignButtonSuppressionSnapshot {
            controls.forEach { control -> control.syncVisualCheckedToActive() }
            return suppressButtons(controls.map { control -> control.button })
        }

        fun suppressButtons(buttons: Iterable<ButtonAPI>): CampaignButtonSuppressionSnapshot {
            val uniqueButtons = CampaignButtonSuppression.uniqueCampaignButtons(buttons)
            val states = uniqueButtons.map { button ->
                State(button = button, wasEnabled = button.isEnabled)
            }
            uniqueButtons.forEach(CampaignButtonSuppression::suppressCampaignButtonHover)
            return CampaignButtonSuppressionSnapshot(states)
        }
    }
}
