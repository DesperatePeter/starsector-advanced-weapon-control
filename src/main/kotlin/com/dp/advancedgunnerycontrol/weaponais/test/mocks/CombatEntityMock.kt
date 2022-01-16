package com.dp.advancedgunnerycontrol.weaponais.test.mocks

import com.fs.starfarer.api.combat.*
import org.lwjgl.util.vector.Vector2f

class CombatEntityMock(var ship: ShipAPI) : CombatEntityAPI {
    override fun getLocation(): Vector2f {
        return ship.location
    }

    override fun getVelocity(): Vector2f {
        return ship.velocity
    }

    override fun getFacing(): Float {
        return ship.facing
    }

    override fun setFacing(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getAngularVelocity(): Float {
        TODO("Not yet implemented")
    }

    override fun setAngularVelocity(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getOwner(): Int {
        return ship.owner
    }

    override fun setOwner(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun getCollisionRadius(): Float {
        return ship.collisionRadius
    }

    override fun getCollisionClass(): CollisionClass {
        TODO("Not yet implemented")
    }

    override fun setCollisionClass(p0: CollisionClass?) {
        TODO("Not yet implemented")
    }

    override fun getMass(): Float {
        TODO("Not yet implemented")
    }

    override fun setMass(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getExactBounds(): BoundsAPI {
        TODO("Not yet implemented")
    }

    override fun getShield(): ShieldAPI {
        return ship.shield
    }

    override fun getHullLevel(): Float {
        TODO("Not yet implemented")
    }

    override fun getHitpoints(): Float {
        TODO("Not yet implemented")
    }

    override fun getMaxHitpoints(): Float {
        TODO("Not yet implemented")
    }

    override fun setCollisionRadius(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getAI(): Any {
        TODO("Not yet implemented")
    }

    override fun isExpired(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setCustomData(p0: String?, p1: Any?) {
        TODO("Not yet implemented")
    }

    override fun removeCustomData(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun getCustomData(): MutableMap<String, Any> {
        TODO("Not yet implemented")
    }

    override fun setHitpoints(p0: Float) {
        TODO("Not yet implemented")
    }
}