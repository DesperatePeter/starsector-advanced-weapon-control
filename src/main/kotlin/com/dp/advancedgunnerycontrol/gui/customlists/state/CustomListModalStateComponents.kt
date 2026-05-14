package com.dp.advancedgunnerycontrol.gui.customlists.state

import com.dp.advancedgunnerycontrol.gui.customlists.edit.CustomListEditLifecycleController
import com.dp.advancedgunnerycontrol.gui.customlists.modal.CustomListModalSessionController
import com.dp.advancedgunnerycontrol.gui.session.CustomListModalBindings

internal data class CustomListModalStateComponents(
    val tagStaging: CustomTagStagingController,
    val shipModeStaging: CustomShipModeStagingController,
    val expansionState: CustomListExpansionStateController,
    val editLifecycle: CustomListEditLifecycleController,
    val debugColorDrafts: CustomDebugColorDraftController,
    val loadoutRenameDrafts: CustomLoadoutRenameDraftController,
    val session: CustomListModalSessionController,
) {
    companion object {
        fun create(
            bindings: CustomListModalBindings,
            changeHandler: CustomListModalChangeHandler,
        ): CustomListModalStateComponents {
            val draftStore = CustomListDraftStore(
                valuesProvider = bindings.draftValuesProvider,
                valuesUpdater = bindings.onDraftValuesUpdate,
                onChanged = changeHandler::onCustomListModalStateChanged,
            )
            val editLifecycle = CustomListEditLifecycleController(
                draftStore = draftStore,
                onDraftDefinitionIdUpdate = bindings.onDraftDefinitionIdUpdate,
                onDraftValuesUpdate = bindings.onDraftValuesUpdate,
                onEditSourceGroupUpdate = bindings.onEditSourceGroupUpdate,
                onEditSourceTagUpdate = bindings.onEditSourceTagUpdate,
                onModalModeUpdate = bindings.onModalModeUpdate,
                onChanged = changeHandler::onCustomListModalStateChanged,
            )
            return CustomListModalStateComponents(
                tagStaging = CustomTagStagingController(
                    draftStore = draftStore,
                    markedForRemovalProvider = bindings.markedForRemovalProvider,
                    markedForRemovalUpdater = bindings.onMarkedForRemovalUpdate,
                    onChanged = changeHandler::onCustomListModalStateChanged,
                ),
                shipModeStaging = CustomShipModeStagingController(
                    draftStore = draftStore,
                    onChanged = changeHandler::onCustomListModalStateChanged,
                ),
                expansionState = CustomListExpansionStateController(
                    draftStore = draftStore,
                ),
                editLifecycle = editLifecycle,
                debugColorDrafts = CustomDebugColorDraftController(
                    draftStore = draftStore,
                ),
                loadoutRenameDrafts = CustomLoadoutRenameDraftController(
                    draftStore = draftStore,
                ),
                session = CustomListModalSessionController(
                    modalModeProvider = bindings.modalModeProvider,
                    draftValuesProvider = bindings.draftValuesProvider,
                    onModalModeUpdate = bindings.onModalModeUpdate,
                    onDraftDefinitionIdUpdate = bindings.onDraftDefinitionIdUpdate,
                    onDraftValuesUpdate = bindings.onDraftValuesUpdate,
                    onManagerScrollOffsetUpdate = bindings.onManagerScrollOffsetUpdate,
                    onChangeReviewScrollOffsetUpdate = bindings.onChangeReviewScrollOffsetUpdate,
                    onMarkedForRemovalUpdate = bindings.onMarkedForRemovalUpdate,
                    clearEditSource = editLifecycle::clearEditSource,
                    onChanged = changeHandler::onCustomListModalStateChanged,
                ),
            )
        }
    }
}
