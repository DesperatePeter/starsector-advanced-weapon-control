package com.dp.advancedgunnerycontrol.weaponais.test.mocks

import com.fs.starfarer.api.combat.ShieldAPI
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class ShieldMock : ShieldAPI {
    var _type = ShieldAPI.ShieldType.FRONT
    var _facing = 0f
    var _arc = 0f
    var _activeArc = 0f
    var _isOn = true
    var _unfoldTime = 1f
    override fun setType(p0: ShieldAPI.ShieldType?) {
        _type = p0 ?: _type
    }

    override fun getType(): ShieldAPI.ShieldType {
        return _type
    }

    override fun getFacing(): Float {
        return _facing
    }

    override fun getArc(): Float = _arc

    override fun getActiveArc(): Float = _activeArc

    override fun setActiveArc(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getRadius(): Float {
        TODO("Not yet implemented")
    }

    override fun isOn(): Boolean {
        return _isOn
    }

    override fun isOff(): Boolean {
        return !_isOn
    }

    override fun getLocation(): Vector2f {
        TODO("Not yet implemented")
    }

    override fun isWithinArc(p0: Vector2f?): Boolean {
        TODO("Not yet implemented")
    }

    override fun toggleOff() {
        TODO("Not yet implemented")
    }

    override fun getFluxPerPointOfDamage(): Float {
        TODO("Not yet implemented")
    }

    override fun setArc(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun setInnerColor(p0: Color?) {
        TODO("Not yet implemented")
    }

    override fun setRingColor(p0: Color?) {
        TODO("Not yet implemented")
    }

    override fun getInnerColor(): Color {
        TODO("Not yet implemented")
    }

    override fun getRingColor(): Color {
        TODO("Not yet implemented")
    }

    override fun getUpkeep(): Float {
        TODO("Not yet implemented")
    }

    override fun forceFacing(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun setRadius(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun setRadius(p0: Float, p1: String?, p2: String?) {
        TODO("Not yet implemented")
    }

    override fun toggleOn() {
        TODO("Not yet implemented")
    }

    override fun getUnfoldTime(): Float = _unfoldTime

    override fun setCenter(p0: Float, p1: Float) {
        TODO("Not yet implemented")
    }

    override fun getInnerRotationRate(): Float {
        TODO("Not yet implemented")
    }

    override fun setInnerRotationRate(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getRingRotationRate(): Float {
        TODO("Not yet implemented")
    }

    override fun setRingRotationRate(p0: Float) {
        TODO("Not yet implemented")
    }
}