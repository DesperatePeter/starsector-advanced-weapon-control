package com.dp.advancedgunnerycontrol.weaponais.test.mocks

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.loading.MissileSpecAPI
import com.fs.starfarer.api.loading.ProjectileSpawnType
import com.fs.starfarer.api.loading.ProjectileSpecAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import org.json.JSONObject
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

class MissileMock : MissileAPI {
    override fun getLocation(): Vector2f {
        TODO("Not yet implemented")
    }

    override fun getVelocity(): Vector2f {
        TODO("Not yet implemented")
    }

    override fun getFacing(): Float {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun setOwner(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun getCollisionRadius(): Float {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
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

    override fun getDamageType(): DamageType {
        TODO("Not yet implemented")
    }

    override fun getDamageAmount(): Float {
        TODO("Not yet implemented")
    }

    override fun getBaseDamageAmount(): Float {
        TODO("Not yet implemented")
    }

    override fun getEmpAmount(): Float {
        TODO("Not yet implemented")
    }

    override fun setDamageAmount(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getWeapon(): WeaponAPI {
        TODO("Not yet implemented")
    }

    override fun didDamage(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getDamageTarget(): CombatEntityAPI {
        TODO("Not yet implemented")
    }

    override fun getProjectileSpecId(): String {
        TODO("Not yet implemented")
    }

    override fun getSource(): ShipAPI {
        TODO("Not yet implemented")
    }

    override fun setSource(p0: ShipAPI?) {
        TODO("Not yet implemented")
    }

    override fun isFading(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getSpawnType(): ProjectileSpawnType {
        TODO("Not yet implemented")
    }

    override fun getElapsed(): Float {
        TODO("Not yet implemented")
    }

    override fun getDamage(): DamageAPI {
        TODO("Not yet implemented")
    }

    override fun isFromMissile(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setFromMissile(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun removeDamagedAlready(p0: CombatEntityAPI?) {
        TODO("Not yet implemented")
    }

    override fun addDamagedAlready(p0: CombatEntityAPI?) {
        TODO("Not yet implemented")
    }

    override fun getMoveSpeed(): Float {
        TODO("Not yet implemented")
    }

    override fun getSpawnLocation(): Vector2f {
        TODO("Not yet implemented")
    }

    override fun getProjectileSpec(): ProjectileSpecAPI {
        TODO("Not yet implemented")
    }

    override fun getBrightness(): Float {
        TODO("Not yet implemented")
    }

    override fun getTailEnd(): Vector2f {
        TODO("Not yet implemented")
    }

    override fun getDamagedAlready(): MutableList<CombatEntityAPI> {
        TODO("Not yet implemented")
    }

    override fun isFizzling(): Boolean {
        TODO("Not yet implemented")
    }

    override fun flameOut() {
        TODO("Not yet implemented")
    }

    override fun getEngineController(): ShipEngineControllerAPI {
        TODO("Not yet implemented")
    }

    override fun setMissileAI(p0: MissileAIPlugin?) {
        TODO("Not yet implemented")
    }

    override fun getMissileAI(): MissileAIPlugin {
        TODO("Not yet implemented")
    }

    override fun giveCommand(p0: ShipCommand?) {
        TODO("Not yet implemented")
    }

    override fun isFlare(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getSpriteAPI(): SpriteAPI {
        TODO("Not yet implemented")
    }

    override fun getAcceleration(): Float {
        TODO("Not yet implemented")
    }

    override fun getMaxSpeed(): Float {
        TODO("Not yet implemented")
    }

    override fun getMaxTurnRate(): Float {
        TODO("Not yet implemented")
    }

    override fun getTurnAcceleration(): Float {
        TODO("Not yet implemented")
    }

    override fun getMaxFlightTime(): Float {
        TODO("Not yet implemented")
    }

    override fun getFlightTime(): Float {
        TODO("Not yet implemented")
    }

    override fun setFlightTime(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun isGuided(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isArmed(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getArmingTime(): Float {
        TODO("Not yet implemented")
    }

    override fun setArmingTime(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun setArmedWhileFizzling(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isArmedWhileFizzling(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setEmpResistance(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun getEmpResistance(): Int {
        TODO("Not yet implemented")
    }

    override fun decrEMPResistance() {
        TODO("Not yet implemented")
    }

    override fun interruptContrail() {
        TODO("Not yet implemented")
    }

    override fun fadeOutThenIn(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getTimeSinceFizzling(): Float {
        TODO("Not yet implemented")
    }

    override fun setTimeSinceFizzling(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun isMine(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setMine(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setMineExplosionRange(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun isMinePrimed(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getMineExplosionRange(): Float {
        TODO("Not yet implemented")
    }

    override fun setMinePrimed(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getUntilMineExplosion(): Float {
        TODO("Not yet implemented")
    }

    override fun setUntilMineExplosion(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun setJitter(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float) {
        TODO("Not yet implemented")
    }

    override fun setJitter(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float, p5: Float) {
        TODO("Not yet implemented")
    }

    override fun getCurrentBaseAlpha(): Float {
        TODO("Not yet implemented")
    }

    override fun getGlowRadius(): Float {
        TODO("Not yet implemented")
    }

    override fun setGlowRadius(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun isRenderGlowAbove(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setRenderGlowAbove(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setShineBrightness(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun isMirv(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getMirvWarheadDamage(): Float {
        TODO("Not yet implemented")
    }

    override fun getMirvWarheadEMPDamage(): Float {
        TODO("Not yet implemented")
    }

    override fun getMirvNumWarheads(): Int {
        TODO("Not yet implemented")
    }

    override fun getMirvWarheadDamageType(): DamageType {
        TODO("Not yet implemented")
    }

    override fun getBehaviorSpecParams(): JSONObject {
        TODO("Not yet implemented")
    }

    override fun isDecoyFlare(): Boolean {
        TODO("Not yet implemented")
    }

    override fun resetEngineGlowBrightness() {
        TODO("Not yet implemented")
    }

    override fun getECCMChance(): Float {
        TODO("Not yet implemented")
    }

    override fun getWeaponSpec(): WeaponSpecAPI {
        TODO("Not yet implemented")
    }

    override fun setWeaponSpec(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun getUnwrappedMissileAI(): MissileAIPlugin {
        TODO("Not yet implemented")
    }

    override fun getParamAboutToApplyDamage(): Any {
        TODO("Not yet implemented")
    }

    override fun setParamAboutToApplyDamage(p0: Any?) {
        TODO("Not yet implemented")
    }

    override fun getSpec(): MissileSpecAPI {
        TODO("Not yet implemented")
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        TODO("Not yet implemented")
    }

    override fun isForceAlwaysArmed(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setForceAlwaysArmed(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isNoMineFFConcerns(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setNoMineFFConcerns(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getEccmChanceOverride(): Float {
        TODO("Not yet implemented")
    }

    override fun setEccmChanceOverride(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getEccmChanceBonus(): Float {
        TODO("Not yet implemented")
    }

    override fun setEccmChanceBonus(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getSourceAPI(): ShipAPI {
        TODO("Not yet implemented")
    }

    override fun isNoFlameoutOnFizzling(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setNoFlameoutOnFizzling(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun explode(): DamagingProjectileAPI {
        TODO("Not yet implemented")
    }

    override fun getMaxRange(): Float {
        TODO("Not yet implemented")
    }

    override fun setMaxRange(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun setMaxFlightTime(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getSpriteAlphaOverride(): Float {
        TODO("Not yet implemented")
    }

    override fun setSpriteAlphaOverride(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getStart(): Vector2f {
        TODO("Not yet implemented")
    }

    override fun setStart(p0: Vector2f?) {
        TODO("Not yet implemented")
    }
}