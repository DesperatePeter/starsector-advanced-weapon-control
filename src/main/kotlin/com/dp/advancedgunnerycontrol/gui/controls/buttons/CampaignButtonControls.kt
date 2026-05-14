package com.dp.advancedgunnerycontrol.gui.controls.buttons

import com.dp.advancedgunnerycontrol.gui.modals.RenderedCampaignConfirmationModal

import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.ButtonAPI

class CampaignMomentaryButton(
    button: ButtonAPI,
    private val callback: () -> Unit,
) : ButtonBase<Unit>(Unit, button, false) {
    override fun executeCallbackIfChecked(): Boolean {
        if (!button.isEnabled) {
            button.isChecked = false
            active = false
            return false
        }
        if (!button.isChecked) return false
        callback()
        button.isChecked = false
        active = false
        return true
    }

    override fun onActivate() {}
}

internal object CampaignButtonControls {
    fun addRenderedConfirmationModalButtons(
        buttons: MutableList<ButtonBase<*>>,
        modal: RenderedCampaignConfirmationModal,
        onConfirm: () -> Unit,
        onCancel: () -> Unit,
    ) {
        modal.backdropButtons.forEach { backdropButton ->
            addControl(buttons, button = backdropButton) {}
        }
        addControl(buttons, button = modal.confirmButton) { onConfirm() }
        addControl(buttons, button = modal.cancelButton) { onCancel() }
    }

    fun clearRenderedConfirmationModalButtons(
        buttons: MutableList<ButtonBase<*>>,
        buttonStartIndex: Int,
        buttonEndIndex: Int,
    ) {
        val start = buttonStartIndex.coerceIn(0, buttons.size)
        val end = buttonEndIndex.coerceIn(start, buttons.size)
        buttons.subList(start, end).clear()
    }

    fun addBidirectionalMomentaryButton(
        buttons: MutableList<ButtonBase<*>>,
        button: ButtonAPI,
        onLeftClick: () -> Unit,
        onRightClick: () -> Unit,
    ): ButtonBase<*> {
        val control = addControl(buttons, button = button) { onLeftClick() }
            .onRightClick { onRightClick() }
        return control
    }

    fun processCallbacks(buttons: List<ButtonBase<*>>): Boolean {
        return processCampaignButtonCallbacksFrom(buttons)
    }

    fun processRightClickInput(
        buttons: MutableList<ButtonBase<*>>,
        events: MutableList<InputEventAPI>?,
    ): Boolean {
        events?.forEach { event ->
            if (processRightClickEvent(buttons, event)) return true
        }
        return false
    }

    fun processRightClickEvent(buttons: MutableList<ButtonBase<*>>, event: InputEventAPI): Boolean {
        val initialSize = buttons.size
        var index = 0
        while (index < initialSize && index < buttons.size) {
            if (buttons[index].processRightClickEvent(event)) return true
            index++
        }
        return false
    }

    private fun processCampaignButtonCallbacksFrom(buttons: List<ButtonBase<*>>): Boolean {
        val initialSize = buttons.size
        var index = 0
        while (index < initialSize && index < buttons.size) {
            if (buttons[index].executeCallbackIfChecked()) return true
            index++
        }
        return false
    }

    fun addControl(
        buttons: MutableList<ButtonBase<*>>,
        button: ButtonAPI,
        active: Boolean = false,
        stateful: Boolean = false,
        callback: () -> Unit,
    ): ButtonBase<*> {
        button.isChecked = active
        val control = if (stateful) {
            CampaignStateButton(button, active) { callback() }
        } else {
            CampaignMomentaryButton(button) { callback() }
        }
        buttons.add(control)
        return control
    }
}

class CampaignStateButton(
    button: ButtonAPI,
    initiallyChecked: Boolean,
    private val callback: () -> Unit,
) : ButtonBase<Unit>(Unit, button, false) {
    init {
        setActiveChecked(initiallyChecked)
    }

    override fun executeCallbackIfChecked(): Boolean {
        if (!button.isEnabled) {
            syncButtonCheckedToActive()
            return false
        }
        if (button.isChecked == active) return false
        callback()
        // Action rows rebuild after callbacks; keep the visual state stable for
        // the remainder of this frame so checked rows do not flicker off.
        syncButtonCheckedToActive()
        return true
    }

    override fun onActivate() {}
}

class CampaignButtonCallbackPoller(
    private val buttons: MutableList<ButtonBase<*>>,
) {
        companion object {
            private const val CLICK_SETTLE_POLL_FRAMES = 3
        }

        private var pollFramesRemaining = CLICK_SETTLE_POLL_FRAMES
        private var targetedPollButtons: List<ButtonBase<*>>? = null

        fun requestPoll() {
            pollFramesRemaining = CLICK_SETTLE_POLL_FRAMES
            targetedPollButtons = null
        }

        fun requestPollFromInput(events: MutableList<InputEventAPI>?) {
            val clickEvents = events?.filter(::isCampaignLeftClickPollEvent).orEmpty()
            if (clickEvents.isEmpty()) return
            val candidates = buttons.filter { button ->
                clickEvents.any(button::containsEvent)
            }
            pollFramesRemaining = CLICK_SETTLE_POLL_FRAMES
            targetedPollButtons = candidates.ifEmpty { null }
        }

        fun clearTargetedPoll() {
            targetedPollButtons = null
        }

        fun processIfRequested(): Boolean {
            if (pollFramesRemaining <= 0) return false
            pollFramesRemaining--
            val handled = processCurrentTarget()
            if (handled || pollFramesRemaining <= 0) {
                targetedPollButtons = null
            }
            return handled
        }

        private fun processCurrentTarget(): Boolean {
            return liveTargetedButtons()
                ?.let(CampaignButtonControls::processCallbacks)
                ?: CampaignButtonControls.processCallbacks(buttons)
        }

        private fun liveTargetedButtons(): List<ButtonBase<*>>? {
            return targetedPollButtons?.takeIf { candidates ->
                candidates.all { candidate -> buttons.contains(candidate) }
            }
        }

        private fun isCampaignLeftClickPollEvent(event: InputEventAPI): Boolean {
            return !event.isConsumed && (event.isLMBDownEvent || event.isLMBUpEvent)
        }
}
