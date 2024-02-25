package com.dp.advancedgunnerycontrol.weaponais

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs
import kotlin.math.sqrt

typealias V = Vector2f

val w = V(0f, 0f)
val ap = V(0f, 10f)

class AGCUtilsKtTest {

    data class OverlapTypeTest(val entity: FPair, val obstruction: FPair, val expectedList: List<FPair>)
    @Test
    fun overlapType(){
        mapOf(
            PolarEntityInWeaponCone.Companion.OverlapType.NO_OVERLAP to OverlapTypeTest(FPair(0f, 1f), FPair(2f, 3f), listOf(FPair(0f, 1f))),
            PolarEntityInWeaponCone.Companion.OverlapType.ERROR to OverlapTypeTest(FPair(2f, 1f), FPair(2f, 3f), emptyList()),
            PolarEntityInWeaponCone.Companion.OverlapType.CENTER to OverlapTypeTest(FPair(0f, 5f), FPair(2f, 3f), listOf(FPair(0f, 2f), FPair(3f, 5f))),
            PolarEntityInWeaponCone.Companion.OverlapType.COMPLETE_BLOCK to OverlapTypeTest(FPair(0f, 5f), FPair(0f, 5f), emptyList()),
            PolarEntityInWeaponCone.Companion.OverlapType.PARTIAL_LEFT to OverlapTypeTest(FPair(1f, 3f), FPair(0f, 2f), listOf(FPair(2f, 3f))),
            PolarEntityInWeaponCone.Companion.OverlapType.PARTIAL_RIGHT to OverlapTypeTest(FPair(1f, 3f), FPair(2f, 4f), listOf(
                FPair(1f, 2f)
            )),
        ).forEach{
            assertEquals(it.key, PolarEntityInWeaponCone.obstructionType(it.value.entity, it.value.obstruction))
            val res = PolarEntityInWeaponCone.obstruct(it.value.entity, it.value.obstruction)
            assertEquals(res.size, it.value.expectedList.size)
            res.forEachIndexed { index, pair ->
                assertEquals(pair.first, it.value.expectedList[index].first)
                assertEquals(pair.second, it.value.expectedList[index].second)
            }
        }
    }
    data class Params(val wl: V, val aimPoint: V, val spreadDeg: Float, val tLoc: V, val collRad: Float, val expectedRes: Float)

    @Test
    fun testComputeWeaponConeExposureRad() {


        mapOf(
            "limited coll rad" to Params(w, ap, 180f, ap, 10f, 1f),
            "outside of cone" to Params(w, ap, 60f, V(10f, 0f), 10f, 0f),
            "at edge of spread" to Params(w, ap, 90f, V(10f, 10f), 10f, 0.5f / sqrt(2f)),
            "limited by spread" to Params(w, ap, 90f, ap, 100f, 90f * degToRad)
        ).forEach {
            val p = it.value
            val res = computeWeaponConeExposureRad(p.wl, p.aimPoint, p.spreadDeg, p.tLoc, p.collRad)
            assertTrue(abs(res - p.expectedRes) <= 0.1f, "${it.key}: result = $res, expected = ${p.expectedRes}, params = $p")
        }
    }

    @Test
    fun testEclipsingExposure(){

        var res = 0f
        var expectedRes = 0f
        var name = ""

        fun check(){
            assertTrue(abs(res - expectedRes) <= 0.01f, "res = $res, expect = $expectedRes, name = $name")
        }

        res = computeWeaponConeExposureRadWithEclipsingEntity(w, ap, 90f, ap, 5f, ap, 1f)
        expectedRes = 0.4f
        name = "1"
        check()

        res = computeWeaponConeExposureRadWithEclipsingEntity(w, ap, 90f, ap, 5f, ap, 5f)
        expectedRes = 0.0f
        name = "2"
        check()

        res = computeWeaponConeExposureRadWithEclipsingEntity(w, ap, 90f, ap, 5f, V(10f, 0f), 10f)
        expectedRes = 0.5f
        name = "3"
        check()

    }
}