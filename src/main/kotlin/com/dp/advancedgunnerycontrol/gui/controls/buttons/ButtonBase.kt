package com.dp.advancedgunnerycontrol.gui.controls.buttons

import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppression
import com.dp.advancedgunnerycontrol.gui.controls.toggles.CampaignToggleVisualState
import com.dp.advancedgunnerycontrol.gui.modals.muteCampaignButtonSounds

import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.ButtonAPI


abstract class ButtonBase<T>(
    var associatedValue: T,
    var button: ButtonAPI,
    protected var isRadio: Boolean = true,
    protected val useMomentaryVisualState: Boolean = false,
) {
    protected var active = false
    protected var sameGroupButtons: List<ButtonBase<T>> = emptyList()
        set(value) {
            field = value.filter { it.associatedValue != this.associatedValue }
        }
    private var rightClickCallback: (() -> Unit)? = null

    fun connectSameGroupButtons(buttons: List<ButtonBase<T>>) {
        sameGroupButtons = buttons
    }

    fun onRightClick(callback: () -> Unit): ButtonBase<T> {
        rightClickCallback = callback
        return this
    }

    fun processRightClickInput(events: MutableList<InputEventAPI>?): Boolean {
        events?.forEach { event ->
            if (processRightClickEvent(event)) return true
        }
        return false
    }

    fun processRightClickEvent(event: InputEventAPI): Boolean {
        val callback = rightClickCallback ?: return false
        if (event.isConsumed || !button.isEnabled || !event.isRMBDownEvent || !containsEvent(event)) return false
        event.consume()
        CampaignButtonSuppression.playCampaignButtonPressedSound()
        callback()
        return true
    }

    open fun containsEvent(event: InputEventAPI): Boolean {
        return CampaignButtonSuppression.campaignButtonContainsEvent(button, event)
    }

    open fun executeCallbackIfChecked(): Boolean {
        if (!active && button.isChecked) {
            check()
            button.isChecked = active
            return true
        }
        button.isChecked = active
        return false
    }

    protected fun check() {
        callback()
        setActiveChecked(true)
    }

    fun disable() {
        button.isEnabled = false
    }

    fun enable() {
        button.isEnabled = true
    }

    protected fun disableAsUnavailable(visualState: CampaignToggleVisualState) {
        disable()
        muteCampaignButtonSounds(button)
        button.setShowTooltipWhileInactive(true)
        clearActiveChecked()
        visualState.applyUnavailable(button)
    }

    protected fun enableWithToggleVisualState(
        visualState: CampaignToggleVisualState,
        force: Boolean = false,
    ) {
        enable()
        CampaignButtonSuppression.restoreCampaignButtonSounds(button)
        visualState.apply(button, force)
    }

    protected fun uncheck() {
        setActiveChecked(false)
    }

    protected fun setActiveChecked(checked: Boolean) {
        active = checked
        button.isChecked = checked
    }

    protected fun setActiveWithoutCheckedVisual(checked: Boolean) {
        active = checked
        button.isChecked = false
    }

    protected fun setActiveForConfiguredVisualState(checked: Boolean) {
        if (useMomentaryVisualState) {
            setActiveWithoutCheckedVisual(checked)
        } else {
            setActiveChecked(checked)
        }
    }

    protected fun setCheckedFromPersistenceVisual(
        checked: Boolean,
        visualState: CampaignToggleVisualState,
    ) {
        setActiveForConfiguredVisualState(checked)
        if (!useMomentaryVisualState) {
            visualState.apply(button, force = true)
        }
    }

    protected fun setAvailableForConfiguredVisualState(visualState: CampaignToggleVisualState) {
        if (useMomentaryVisualState) {
            enable()
            CampaignButtonSuppression.restoreCampaignButtonSounds(button)
        } else {
            enableWithToggleVisualState(visualState)
        }
    }

    protected fun <S> executeMomentaryVisualToggleIfClicked(
        currentState: () -> S,
        isUnavailable: (S) -> Boolean = { false },
        rejectUnavailable: () -> Unit = {},
        updatedState: (currentlyActive: Boolean, currentState: S) -> S,
        saveState: (S) -> Unit,
        afterStateChanged: (S) -> Unit,
    ): Boolean {
        if (!button.isChecked) return false
        val state = currentState()
        if (!button.isEnabled || isUnavailable(state)) {
            button.isChecked = false
            rejectUnavailable()
            return false
        }

        val updated = updatedState(active, state)
        saveState(updated)
        setActiveWithoutCheckedVisual(!active)
        afterStateChanged(updated)
        button.isChecked = false
        return true
    }

    protected fun clearActiveChecked() {
        setActiveChecked(false)
    }

    protected open fun syncButtonCheckedToActive() {
        button.isChecked = active
    }

    open fun syncVisualCheckedToActive() {
        if (useMomentaryVisualState) {
            button.isChecked = false
        } else {
            syncButtonCheckedToActive()
        }
    }

    private fun callback() {
        if (isRadio) {
            sameGroupButtons.forEach { it.uncheck() }
        }
        onActivate()
    }

    abstract fun onActivate()
}
