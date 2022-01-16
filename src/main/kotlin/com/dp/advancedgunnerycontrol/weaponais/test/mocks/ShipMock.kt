package com.dp.advancedgunnerycontrol.weaponais.test.mocks

import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.CombatListenerManagerAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.loading.WeaponSlotAPI
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

class ShipMock(public var _shield: ShieldAPI) : ShipAPI {
    var _fluxLevel = 0f
    var _location = Vector2f(0f, 0f)
    var _velocity = Vector2f(0f, 0f)
    var _shipTarget : ShipAPI? = null
    var _facing = 0f
    var _owner = 0
    var _collisionRadius = 0f

    override fun getLocation(): Vector2f {
        return _location
    }

    override fun getVelocity(): Vector2f {
        return _velocity
    }

    override fun getFacing(): Float {
        return _facing
    }

    override fun setFacing(p0: Float) {
        _facing = p0
    }

    override fun getAngularVelocity(): Float = 0f
    override fun setAngularVelocity(p0: Float) {
    }

    override fun getOwner(): Int = _owner

    override fun setOwner(p0: Int) {
        _owner = p0
    }

    override fun getCollisionRadius(): Float {
        return _collisionRadius
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

    override fun getShield(): ShieldAPI = _shield

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

    override fun getFleetMemberId(): String {
        TODO("Not yet implemented")
    }

    override fun getMouseTarget(): Vector2f {
        TODO("Not yet implemented")
    }

    override fun isShuttlePod(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isDrone(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isFighter(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isFrigate(): Boolean = false

    override fun isDestroyer(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isCruiser(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isCapital(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getHullSize(): ShipAPI.HullSize {
        TODO("Not yet implemented")
    }

    override fun getShipTarget(): ShipAPI? = _shipTarget

    override fun setShipTarget(p0: ShipAPI?) {
        _shipTarget = p0
    }

    override fun getOriginalOwner(): Int {
        TODO("Not yet implemented")
    }

    override fun setOriginalOwner(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun resetOriginalOwner() {
        TODO("Not yet implemented")
    }

    override fun getMutableStats(): MutableShipStatsAPI {
        TODO("Not yet implemented")
    }

    override fun isHulk(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAllWeapons(): MutableList<WeaponAPI> {
        TODO("Not yet implemented")
    }

    override fun getPhaseCloak(): ShipSystemAPI {
        TODO("Not yet implemented")
    }

    override fun getSystem(): ShipSystemAPI {
        TODO("Not yet implemented")
    }

    override fun getTravelDrive(): ShipSystemAPI {
        TODO("Not yet implemented")
    }

    override fun toggleTravelDrive() {
        TODO("Not yet implemented")
    }

    override fun setShield(p0: ShieldAPI.ShieldType?, p1: Float, p2: Float, p3: Float) {
        TODO("Not yet implemented")
    }

    override fun getHullSpec(): ShipHullSpecAPI {
        TODO("Not yet implemented")
    }

    override fun getVariant(): ShipVariantAPI {
        TODO("Not yet implemented")
    }

    override fun useSystem() {
        TODO("Not yet implemented")
    }

    override fun getFluxTracker(): FluxTrackerAPI {
        TODO("Not yet implemented")
    }

    override fun getWingMembers(): MutableList<ShipAPI> {
        TODO("Not yet implemented")
    }

    override fun getWingLeader(): ShipAPI {
        TODO("Not yet implemented")
    }

    override fun isWingLeader(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getWing(): FighterWingAPI {
        TODO("Not yet implemented")
    }

    override fun getDeployedDrones(): MutableList<ShipAPI> {
        TODO("Not yet implemented")
    }

    override fun getDroneSource(): ShipAPI {
        TODO("Not yet implemented")
    }

    override fun getWingToken(): Any {
        TODO("Not yet implemented")
    }

    override fun getArmorGrid(): ArmorGridAPI {
        TODO("Not yet implemented")
    }

    override fun setRenderBounds(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setCRAtDeployment(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getCRAtDeployment(): Float {
        TODO("Not yet implemented")
    }

    override fun getCurrentCR(): Float {
        TODO("Not yet implemented")
    }

    override fun setCurrentCR(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getWingCRAtDeployment(): Float {
        TODO("Not yet implemented")
    }

    override fun getTimeDeployedForCRReduction(): Float {
        TODO("Not yet implemented")
    }

    override fun getFullTimeDeployed(): Float {
        TODO("Not yet implemented")
    }

    override fun losesCRDuringCombat(): Boolean {
        TODO("Not yet implemented")
    }

    override fun controlsLocked(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setControlsLocked(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setShipSystemDisabled(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getDisabledWeapons(): MutableSet<WeaponAPI> {
        TODO("Not yet implemented")
    }

    override fun getNumFlameouts(): Int {
        TODO("Not yet implemented")
    }

    override fun getHullLevelAtDeployment(): Float {
        TODO("Not yet implemented")
    }

    override fun setSprite(p0: String?, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun getSpriteAPI(): SpriteAPI {
        TODO("Not yet implemented")
    }

    override fun getEngineController(): ShipEngineControllerAPI {
        TODO("Not yet implemented")
    }

    override fun giveCommand(p0: ShipCommand?, p1: Any?, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun setShipAI(p0: ShipAIPlugin?) {
        TODO("Not yet implemented")
    }

    override fun getShipAI(): ShipAIPlugin {
        TODO("Not yet implemented")
    }

    override fun resetDefaultAI() {
        TODO("Not yet implemented")
    }

    override fun turnOnTravelDrive() {
        TODO("Not yet implemented")
    }

    override fun turnOnTravelDrive(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun turnOffTravelDrive() {
        TODO("Not yet implemented")
    }

    override fun isRetreating(): Boolean {
        TODO("Not yet implemented")
    }

    override fun abortLanding() {
        TODO("Not yet implemented")
    }

    override fun beginLandingAnimation(p0: ShipAPI?) {
        TODO("Not yet implemented")
    }

    override fun isLanding(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isFinishedLanding(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isAlive(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isInsideNebula(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setInsideNebula(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isAffectedByNebula(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setAffectedByNebula(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getDeployCost(): Float {
        TODO("Not yet implemented")
    }

    override fun removeWeaponFromGroups(p0: WeaponAPI?) {
        TODO("Not yet implemented")
    }

    override fun applyCriticalMalfunction(p0: Any?) {
        TODO("Not yet implemented")
    }

    override fun applyCriticalMalfunction(p0: Any?, p1: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getBaseCriticalMalfunctionDamage(): Float {
        TODO("Not yet implemented")
    }

    override fun getEngineFractionPermanentlyDisabled(): Float {
        TODO("Not yet implemented")
    }

    override fun getCombinedAlphaMult(): Float {
        TODO("Not yet implemented")
    }

    override fun getLowestHullLevelReached(): Float {
        TODO("Not yet implemented")
    }

    override fun getAIFlags(): ShipwideAIFlags {
        TODO("Not yet implemented")
    }

    override fun getWeaponGroupsCopy(): MutableList<WeaponGroupAPI> {
        TODO("Not yet implemented")
    }

    override fun isHoldFire(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isHoldFireOneFrame(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setHoldFireOneFrame(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isPhased(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isAlly(): Boolean = false

    override fun setWeaponGlow(p0: Float, p1: Color?, p2: EnumSet<WeaponAPI.WeaponType>?) {
        TODO("Not yet implemented")
    }

    override fun setVentCoreColor(p0: Color?) {
        TODO("Not yet implemented")
    }

    override fun setVentFringeColor(p0: Color?) {
        TODO("Not yet implemented")
    }

    override fun getVentCoreColor(): Color {
        TODO("Not yet implemented")
    }

    override fun getVentFringeColor(): Color {
        TODO("Not yet implemented")
    }

    override fun getHullStyleId(): String {
        TODO("Not yet implemented")
    }

    override fun getWeaponGroupFor(p0: WeaponAPI?): WeaponGroupAPI {
        TODO("Not yet implemented")
    }

    override fun setCopyLocation(p0: Vector2f?, p1: Float, p2: Float) {
        TODO("Not yet implemented")
    }

    override fun getCopyLocation(): Vector2f {
        TODO("Not yet implemented")
    }

    override fun setAlly(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getId(): String {
        TODO("Not yet implemented")
    }

    override fun getName(): String {
        TODO("Not yet implemented")
    }

    override fun setJitter(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float) {
        TODO("Not yet implemented")
    }

    override fun setJitter(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float, p5: Float) {
        TODO("Not yet implemented")
    }

    override fun setJitterUnder(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float) {
        TODO("Not yet implemented")
    }

    override fun setJitterUnder(p0: Any?, p1: Color?, p2: Float, p3: Int, p4: Float, p5: Float) {
        TODO("Not yet implemented")
    }

    override fun getTimeDeployedUnderPlayerControl(): Float {
        TODO("Not yet implemented")
    }

    override fun getSmallTurretCover(): SpriteAPI {
        TODO("Not yet implemented")
    }

    override fun getSmallHardpointCover(): SpriteAPI {
        TODO("Not yet implemented")
    }

    override fun getMediumTurretCover(): SpriteAPI {
        TODO("Not yet implemented")
    }

    override fun getMediumHardpointCover(): SpriteAPI {
        TODO("Not yet implemented")
    }

    override fun getLargeTurretCover(): SpriteAPI {
        TODO("Not yet implemented")
    }

    override fun getLargeHardpointCover(): SpriteAPI {
        TODO("Not yet implemented")
    }

    override fun isDefenseDisabled(): Boolean = false

    override fun setDefenseDisabled(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setPhased(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setExtraAlphaMult(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun setApplyExtraAlphaToEngines(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setOverloadColor(p0: Color?) {
        TODO("Not yet implemented")
    }

    override fun resetOverloadColor() {
        TODO("Not yet implemented")
    }

    override fun getOverloadColor(): Color {
        TODO("Not yet implemented")
    }

    override fun isRecentlyShotByPlayer(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getMaxSpeedWithoutBoost(): Float {
        TODO("Not yet implemented")
    }

    override fun getHardFluxLevel(): Float {
        TODO("Not yet implemented")
    }

    override fun fadeToColor(p0: Any?, p1: Color?, p2: Float, p3: Float, p4: Float) {
        TODO("Not yet implemented")
    }

    override fun isShowModuleJitterUnder(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setShowModuleJitterUnder(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun addAfterimage(
        p0: Color?,
        p1: Float,
        p2: Float,
        p3: Float,
        p4: Float,
        p5: Float,
        p6: Float,
        p7: Float,
        p8: Float,
        p9: Boolean,
        p10: Boolean,
        p11: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun getCaptain(): PersonAPI {
        TODO("Not yet implemented")
    }

    override fun getStationSlot(): WeaponSlotAPI {
        TODO("Not yet implemented")
    }

    override fun setStationSlot(p0: WeaponSlotAPI?) {
        TODO("Not yet implemented")
    }

    override fun getParentStation(): ShipAPI {
        TODO("Not yet implemented")
    }

    override fun setParentStation(p0: ShipAPI?) {
        TODO("Not yet implemented")
    }

    override fun getFixedLocation(): Vector2f {
        TODO("Not yet implemented")
    }

    override fun setFixedLocation(p0: Vector2f?) {
        TODO("Not yet implemented")
    }

    override fun hasRadarRibbonIcon(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isTargetable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setStation(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isSelectableInWarroom(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isShipWithModules(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setShipWithModules(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getChildModulesCopy(): MutableList<ShipAPI> {
        TODO("Not yet implemented")
    }

    override fun isPiece(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getVisualBounds(): BoundsAPI {
        TODO("Not yet implemented")
    }

    override fun getRenderOffset(): Vector2f {
        TODO("Not yet implemented")
    }

    override fun splitShip(): ShipAPI {
        TODO("Not yet implemented")
    }

    override fun getNumFighterBays(): Int {
        TODO("Not yet implemented")
    }

    override fun isPullBackFighters(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setPullBackFighters(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun hasLaunchBays(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getLaunchBaysCopy(): MutableList<FighterLaunchBayAPI> {
        TODO("Not yet implemented")
    }

    override fun getFighterTimeBeforeRefit(): Float {
        TODO("Not yet implemented")
    }

    override fun setFighterTimeBeforeRefit(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getAllWings(): MutableList<FighterWingAPI> {
        TODO("Not yet implemented")
    }

    override fun getSharedFighterReplacementRate(): Float {
        TODO("Not yet implemented")
    }

    override fun areSignificantEnemiesInRange(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getUsableWeapons(): MutableList<WeaponAPI> {
        TODO("Not yet implemented")
    }

    override fun getModuleOffset(): Vector2f {
        TODO("Not yet implemented")
    }

    override fun getMassWithModules(): Float {
        TODO("Not yet implemented")
    }

    override fun getOriginalCaptain(): PersonAPI {
        TODO("Not yet implemented")
    }

    override fun isRenderEngines(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setRenderEngines(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getSelectedGroupAPI(): WeaponGroupAPI {
        TODO("Not yet implemented")
    }

    override fun setHullSize(p0: ShipAPI.HullSize?) {
        TODO("Not yet implemented")
    }

    override fun ensureClonedStationSlotSpec() {
        TODO("Not yet implemented")
    }

    override fun setMaxHitpoints(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun setDHullOverlay(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun isStation(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isStationModule(): Boolean {
        TODO("Not yet implemented")
    }

    override fun areAnyEnemiesInRange(): Boolean {
        TODO("Not yet implemented")
    }

    override fun blockCommandForOneFrame(p0: ShipCommand?) {
        TODO("Not yet implemented")
    }

    override fun getMaxTurnRate(): Float {
        TODO("Not yet implemented")
    }

    override fun getTurnAcceleration(): Float {
        TODO("Not yet implemented")
    }

    override fun getTurnDeceleration(): Float {
        TODO("Not yet implemented")
    }

    override fun getDeceleration(): Float {
        TODO("Not yet implemented")
    }

    override fun getAcceleration(): Float {
        TODO("Not yet implemented")
    }

    override fun getMaxSpeed(): Float {
        TODO("Not yet implemented")
    }

    override fun getFluxLevel(): Float {
        return _fluxLevel
    }

    override fun getCurrFlux(): Float {
        TODO("Not yet implemented")
    }

    override fun getMaxFlux(): Float {
        TODO("Not yet implemented")
    }

    override fun getMinFluxLevel(): Float {
        TODO("Not yet implemented")
    }

    override fun getMinFlux(): Float {
        TODO("Not yet implemented")
    }

    override fun setLightDHullOverlay() {
        TODO("Not yet implemented")
    }

    override fun setMediumDHullOverlay() {
        TODO("Not yet implemented")
    }

    override fun setHeavyDHullOverlay() {
        TODO("Not yet implemented")
    }

    override fun isJitterShields(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setJitterShields(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isInvalidTransferCommandTarget(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setInvalidTransferCommandTarget(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun clearDamageDecals() {
        TODO("Not yet implemented")
    }

    override fun syncWithArmorGridState() {
        TODO("Not yet implemented")
    }

    override fun syncWeaponDecalsWithArmorDamage() {
        TODO("Not yet implemented")
    }

    override fun isDirectRetreat(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setRetreating(p0: Boolean, p1: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isLiftingOff(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setVariantForHullmodCheckOnly(p0: ShipVariantAPI?) {
        TODO("Not yet implemented")
    }

    override fun getShieldCenterEvenIfNoShield(): Vector2f {
        TODO("Not yet implemented")
    }

    override fun getShieldRadiusEvenIfNoShield(): Float {
        TODO("Not yet implemented")
    }

    override fun getFleetMember(): FleetMemberAPI {
        TODO("Not yet implemented")
    }

    override fun getShieldTarget(): Vector2f {
        TODO("Not yet implemented")
    }

    override fun setShieldTargetOverride(p0: Float, p1: Float) {
        TODO("Not yet implemented")
    }

    override fun getListenerManager(): CombatListenerManagerAPI {
        TODO("Not yet implemented")
    }

    override fun addListener(p0: Any?) {
        TODO("Not yet implemented")
    }

    override fun removeListener(p0: Any?) {
        TODO("Not yet implemented")
    }

    override fun removeListenerOfClass(p0: Class<*>?) {
        TODO("Not yet implemented")
    }

    override fun hasListener(p0: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasListenerOfClass(p0: Class<*>?): Boolean {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> getListeners(p0: Class<T>?): MutableList<T> {
        TODO("Not yet implemented")
    }

    override fun getParamAboutToApplyDamage(): Any {
        TODO("Not yet implemented")
    }

    override fun setParamAboutToApplyDamage(p0: Any?) {
        TODO("Not yet implemented")
    }

    override fun getFluxBasedEnergyWeaponDamageMultiplier(): Float {
        TODO("Not yet implemented")
    }

    override fun setName(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun setHulk(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setCaptain(p0: PersonAPI?) {
        TODO("Not yet implemented")
    }

    override fun getShipExplosionRadius(): Float {
        TODO("Not yet implemented")
    }

    override fun setCircularJitter(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getExtraAlphaMult(): Float {
        TODO("Not yet implemented")
    }

    override fun setAlphaMult(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getAlphaMult(): Float {
        TODO("Not yet implemented")
    }

    override fun setAnimatedLaunch() {
        TODO("Not yet implemented")
    }

    override fun setLaunchingShip(p0: ShipAPI?) {
        TODO("Not yet implemented")
    }

    override fun isNonCombat(p0: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun findBestArmorInArc(p0: Float, p1: Float): Float {
        TODO("Not yet implemented")
    }

    override fun getAverageArmorInSlice(p0: Float, p1: Float): Float {
        TODO("Not yet implemented")
    }

    override fun setHoldFire(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun cloneVariant() {
        TODO("Not yet implemented")
    }

    override fun setTimeDeployed(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun setFluxVentTextureSheet(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun getFluxVentTextureSheet(): String {
        TODO("Not yet implemented")
    }

    override fun getAimAccuracy(): Float {
        TODO("Not yet implemented")
    }

    override fun getForceCarrierTargetTime(): Float {
        TODO("Not yet implemented")
    }

    override fun setForceCarrierTargetTime(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getForceCarrierPullBackTime(): Float {
        TODO("Not yet implemented")
    }

    override fun setForceCarrierPullBackTime(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getForceCarrierTarget(): ShipAPI {
        TODO("Not yet implemented")
    }

    override fun setForceCarrierTarget(p0: ShipAPI?) {
        TODO("Not yet implemented")
    }

    override fun setWing(p0: FighterWingAPI?) {
        TODO("Not yet implemented")
    }

    override fun getExplosionScale(): Float {
        TODO("Not yet implemented")
    }

    override fun setExplosionScale(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getExplosionFlashColorOverride(): Color {
        TODO("Not yet implemented")
    }

    override fun setExplosionFlashColorOverride(p0: Color?) {
        TODO("Not yet implemented")
    }

    override fun getExplosionVelocityOverride(): Vector2f {
        TODO("Not yet implemented")
    }

    override fun setExplosionVelocityOverride(p0: Vector2f?) {
        TODO("Not yet implemented")
    }

    override fun setNextHitHullDamageThresholdMult(p0: Float, p1: Float) {
        TODO("Not yet implemented")
    }

    override fun isEngineBoostActive(): Boolean {
        TODO("Not yet implemented")
    }

    override fun makeLookDisabled() {
        TODO("Not yet implemented")
    }

    override fun setExtraAlphaMult2(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun getExtraAlphaMult2(): Float {
        TODO("Not yet implemented")
    }
}