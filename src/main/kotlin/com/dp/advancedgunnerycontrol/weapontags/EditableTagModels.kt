package com.dp.advancedgunnerycontrol.weapontags

enum class EditableTagParameterKind {
    TEXT,
    TOGGLE,
    CHOICE,
    NUMBER,
    DECIMAL
}

data class TagParameterOption(
    val id: String,
    val label: String
)

sealed class EditableTagParameterDefinition(
    open val id: String,
    open val label: String,
    val kind: EditableTagParameterKind,
    val useHiddenChangeMarker: Boolean = false,
)

data class TextParameter(
    override val id: String,
    override val label: String,
    val defaultValue: String = "",
    val maxLength: Int? = null,
    val markHiddenChange: Boolean = false,
) : EditableTagParameterDefinition(id, label, EditableTagParameterKind.TEXT, markHiddenChange)

data class ToggleParameter(
    override val id: String,
    override val label: String,
    val defaultValue: Boolean = false,
    val markHiddenChange: Boolean = false,
) : EditableTagParameterDefinition(id, label, EditableTagParameterKind.TOGGLE, markHiddenChange)

data class ChoiceParameter(
    override val id: String,
    override val label: String,
    val options: List<TagParameterOption>,
    val defaultOptionId: String,
    val markHiddenChange: Boolean = false,
) : EditableTagParameterDefinition(id, label, EditableTagParameterKind.CHOICE, markHiddenChange)

data class NumberParameter(
    override val id: String,
    override val label: String,
    val minValue: Int,
    val maxValue: Int,
    val defaultValue: Int,
    val suffix: String = "",
    val markHiddenChange: Boolean = false,
) : EditableTagParameterDefinition(id, label, EditableTagParameterKind.NUMBER, markHiddenChange)

data class DecimalParameter(
    override val id: String,
    override val label: String,
    val minValue: Float,
    val maxValue: Float,
    val defaultValue: Float,
    val minorStep: Float = 0.1f,
    val majorStep: Float = 1f,
    val decimalPlaces: Int = 2,
    val suffix: String = "",
    val markHiddenChange: Boolean = false,
) : EditableTagParameterDefinition(id, label, EditableTagParameterKind.DECIMAL, markHiddenChange)

data class EditableTagValidationError(
    val parameterId: String,
    val message: String
)

data class EditableTagBuildResult(
    val canonicalTag: String?,
    val errors: List<EditableTagValidationError>
) {
    val isValid: Boolean
        get() = canonicalTag != null && errors.isEmpty()
}

data class ParsedEditableWeaponTag(
    val canonicalTag: String,
    val definitionId: String,
    val family: String,
    val templateTag: String,
    val parameterValues: Map<String, String>,
    val exclusivityKeys: Set<String>
)

data class EditableWeaponTagDefinition(
    val id: String,
    val family: String,
    val templateTag: String,
    val acceptedTemplateTags: Set<String> = setOf(templateTag),
    val parameters: List<EditableTagParameterDefinition>,
    val defaultValues: Map<String, String>,
    private val parser: (String) -> ParsedEditableWeaponTag?,
    private val builder: (Map<String, String>) -> EditableTagBuildResult
) {
    fun parse(canonicalTag: String): ParsedEditableWeaponTag? = parser(canonicalTag)

    fun buildCanonicalTag(parameterValues: Map<String, String>): EditableTagBuildResult = builder(parameterValues)
}
