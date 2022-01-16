package com.dp.advancedgunnerycontrol.weaponais.test.mocks

import com.fs.starfarer.api.AnimationAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.loading.MuzzleFlashSpec
import com.fs.starfarer.api.loading.WeaponSlotAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import org.lwjgl.util.vector.Vector2f

class WeaponMock (private val ship: ShipAPI) : WeaponAPI {
    override fun getId(): String {
        TODO("Not yet implemented")
    }

    override fun getType(): WeaponAPI.WeaponType {
        TODO("Not yet implemented")
    }

    override fun getSize(): WeaponAPI.WeaponSize {
        TODO("Not yet implemented")
    }

    override fun setPD(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun distanceFromArc(p0: Vector2f?): Float {
        TODO("Not yet implemented")
    }

    override fun isAlwaysFire(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getCurrSpread(): Float {
        TODO("Not yet implemented")
    }

    override fun getCurrAngle(): Float {
        TODO("Not yet implemented")
    }

    override fun getArcFacing(): Float {
        TODO("Not yet implemented")
    }

    override fun getArc(): Float {
        TODO("Not yet implemented")
    }

    override fun setCurrAngle(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getRange(): Float {
        TODO("Not yet implemented")
    }

    override fun getDisplayArcRadius(): Float {
        TODO("Not yet implemented")
    }

    override fun getChargeLevel(): Float {
        TODO("Not yet implemented")
    }

    override fun getTurnRate(): Float {
        TODO("Not yet implemented")
    }

    override fun getProjectileSpeed(): Float {
        TODO("Not yet implemented")
    }

    override fun getDisplayName(): String {
        TODO("Not yet implemented")
    }

    override fun getAmmo(): Int {
        TODO("Not yet implemented")
    }

    override fun getMaxAmmo(): Int {
        TODO("Not yet implemented")
    }

    override fun setMaxAmmo(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun resetAmmo() {
        TODO("Not yet implemented")
    }

    override fun getCooldownRemaining(): Float {
        TODO("Not yet implemented")
    }

    override fun getCooldown(): Float {
        TODO("Not yet implemented")
    }

    override fun setRemainingCooldownTo(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun isBeam(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isBurstBeam(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isPulse(): Boolean {
        TODO("Not yet implemented")
    }

    override fun requiresFullCharge(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getLocation(): Vector2f {
        TODO("Not yet implemented")
    }

    override fun isFiring(): Boolean {
        TODO("Not yet implemented")
    }

    override fun usesAmmo(): Boolean {
        TODO("Not yet implemented")
    }

    override fun usesEnergy(): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasAIHint(p0: WeaponAPI.AIHints?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getProjectileCollisionClass(): CollisionClass {
        TODO("Not yet implemented")
    }

    override fun beginSelectionFlash() {
        TODO("Not yet implemented")
    }

    override fun getFluxCostToFire(): Float {
        TODO("Not yet implemented")
    }

    override fun getMaxHealth(): Float {
        TODO("Not yet implemented")
    }

    override fun getCurrHealth(): Float {
        TODO("Not yet implemented")
    }

    override fun isDisabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getDisabledDuration(): Float {
        TODO("Not yet implemented")
    }

    override fun isPermanentlyDisabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getDamageType(): DamageType {
        TODO("Not yet implemented")
    }

    override fun getShip(): ShipAPI {
        return ship
    }

    override fun getDerivedStats(): WeaponAPI.DerivedWeaponStatsAPI {
        TODO("Not yet implemented")
    }

    override fun setAmmo(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun getAnimation(): AnimationAPI {
        TODO("Not yet implemented")
    }

    override fun getSprite(): SpriteAPI {
        TODO("Not yet implemented")
    }

    override fun getUnderSpriteAPI(): SpriteAPI {
        TODO("Not yet implemented")
    }

    override fun getBarrelSpriteAPI(): SpriteAPI {
        TODO("Not yet implemented")
    }

    override fun renderBarrel(p0: SpriteAPI?, p1: Vector2f?, p2: Float) {
        TODO("Not yet implemented")
    }

    override fun isRenderBarrelBelow(): Boolean {
        TODO("Not yet implemented")
    }

    override fun disable() {
        TODO("Not yet implemented")
    }

    override fun disable(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun repair() {
        TODO("Not yet implemented")
    }

    override fun getSpec(): WeaponSpecAPI {
        TODO("Not yet implemented")
    }

    override fun getSlot(): WeaponSlotAPI {
        TODO("Not yet implemented")
    }

    override fun getEffectPlugin(): EveryFrameWeaponEffectPlugin {
        TODO("Not yet implemented")
    }

    override fun getMissileRenderData(): MutableList<MissileRenderDataAPI> {
        TODO("Not yet implemented")
    }

    override fun getDamage(): DamageAPI {
        TODO("Not yet implemented")
    }

    override fun getProjectileFadeRange(): Float {
        TODO("Not yet implemented")
    }

    override fun isDecorative(): Boolean {
        TODO("Not yet implemented")
    }

    override fun ensureClonedSpec() {
        TODO("Not yet implemented")
    }

    override fun getAmmoPerSecond(): Float {
        TODO("Not yet implemented")
    }

    override fun setPDAlso(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setCurrHealth(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getMuzzleFlashSpec(): MuzzleFlashSpec {
        TODO("Not yet implemented")
    }

    override fun getBeams(): MutableList<BeamAPI> {
        TODO("Not yet implemented")
    }

    override fun getFirePoint(p0: Int): Vector2f {
        TODO("Not yet implemented")
    }

    override fun setTurnRateOverride(p0: Float?) {
        TODO("Not yet implemented")
    }

    override fun getGlowSpriteAPI(): SpriteAPI {
        TODO("Not yet implemented")
    }

    override fun getAmmoTracker(): AmmoTrackerAPI {
        TODO("Not yet implemented")
    }

    override fun setRefireDelay(p0: Float) {
        TODO("Not yet implemented")
    }
}