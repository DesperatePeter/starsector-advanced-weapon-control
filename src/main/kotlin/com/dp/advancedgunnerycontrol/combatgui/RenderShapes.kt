package com.dp.advancedgunnerycontrol.combatgui

import com.fs.starfarer.api.Global
import org.lazywizard.lazylib.opengl.DrawUtils
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.BufferUtils
import java.nio.FloatBuffer
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.nio.Buffer

/**
 * data class defining position, radius and opacity of a circle to highlight things
 */
data class Highlight(val x: Float, val y: Float, val r: Float, var a: Float)
val defaultHighlightColor: Color = Color.GREEN
val defaultTextBoxColor: Color = Color.BLACK
val defaultFrameColor: Color = Color.WHITE
private const val CIRCLE_POINTS = 50
private const val RADIUS_MULTIPLIER = 0.6f

private fun preRender(){
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
    GL11.glMatrixMode(GL11.GL_PROJECTION)
    GL11.glPushMatrix()
    GL11.glLoadIdentity()
    GL11.glMatrixMode(GL11.GL_MODELVIEW)
    GL11.glPushMatrix()
    GL11.glLoadIdentity()
    GL11.glDisable(GL11.GL_TEXTURE_2D)
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    GL11.glViewport(0,0, Display.getWidth(), Display.getHeight())
    GL11.glOrtho(0.0, Display.getWidth().toDouble(),0.0, Display.getHeight().toDouble(),-1.0, 1.0)
    GL11.glTranslatef(0.01f, 0.01f, 0f)
    GL11.glEnable(GL11.GL_LINE_SMOOTH)
    GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
    GL11.glLineWidth(1.5f)
}

private fun postRender(){
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glPopMatrix()
    GL11.glMatrixMode(GL11.GL_PROJECTION)
    GL11.glPopMatrix()
    GL11.glPopAttrib()
}

/**
 * Render circles in given color.
 * You can call this in your render or onHover methods to e.g. visualize what a given button affects
 * @param highlights list of highlights (filled circles) to render
 * @param viewMult get from viewport
 * @param color alpha value is unused (defined via Highlight instead)
 */
fun renderHighlights(highlights: List<Highlight>, viewMult: Float, color: Color = defaultHighlightColor){
    val uiMult = Global.getSettings()?.screenScaleMult ?: 1f
    preRender()
    highlights.forEach{
        GL11.glColor4f(color.red.toFloat()/255f, color.green.toFloat()/255f, color.blue.toFloat()/255f, it.a)
        DrawUtils.drawCircle(it.x, it.y, it.r * RADIUS_MULTIPLIER / viewMult * uiMult , CIRCLE_POINTS, true)
    }
    postRender()
}

/**
 * Render a textbox for given string at given position
 * this gets used by buttons internally, so unless you want to manually display text, you won't need to use this
 */
fun renderTextbox(text: LazyFont.DrawableString, xPos: Float, yPos: Float, buffer: Float, color: Color = defaultTextBoxColor, frameColor: Color = defaultFrameColor){
    val x = xPos - buffer/2f
    val y = yPos + buffer/2f
    val w = text.width + buffer
    val h = text.height + buffer
    // assume TOP_LEFT anchor for now, move later
    val vertices = arrayOf(x, y, // top left
        x + w, y, // top right
        x + w, y - h, // bottom right
        x, y - h).toFloatArray() // bottom left

    fun adjustForAnchor(){
        fun moveVertices(dx: Float, dy: Float){
            for (i in 0 until vertices.size / 2 ){
                vertices[2*i] += dx
                vertices[2*i + 1] += dy
            }
        }
        when(text.anchor){
            LazyFont.TextAnchor.TOP_CENTER -> moveVertices(-w/2f, 0f)
            LazyFont.TextAnchor.TOP_RIGHT -> moveVertices(-w, 0f)
            LazyFont.TextAnchor.CENTER_LEFT -> moveVertices(0f, h/2f)
            LazyFont.TextAnchor.CENTER -> moveVertices(-w/2f, h/2f)
            LazyFont.TextAnchor.CENTER_RIGHT -> moveVertices(-w, h/2f)
            LazyFont.TextAnchor.BOTTOM_LEFT -> moveVertices(0f, h)
            LazyFont.TextAnchor.BOTTOM_CENTER -> moveVertices(-w/2f, h)
            LazyFont.TextAnchor.BOTTOM_RIGHT -> moveVertices(-w, h)
            else -> moveVertices(0f, 0f)
        }
    }
    adjustForAnchor()

    fun createFrameVertices(vertices: FloatArray, offset: Float) : FloatArray
    {
        val v = vertices
        return arrayOf(v[0] - offset, v[1] + offset,
            v[2] + offset, v[3] + offset,
            v[4] + offset, v[5] - offset,
            v[6] -offset, v[7] -offset).toFloatArray()
    }

    val vertexBuffer: FloatBuffer = BufferUtils.createFloatBuffer(vertices.size)
    vertexBuffer.put(vertices)
    // don't ask me why the cast is necessary, some weird Java stuff...
    (vertexBuffer as Buffer).flip()

    val frameVertices = createFrameVertices(vertices, 1f)
    val frameVertexBuffer: FloatBuffer = BufferUtils.createFloatBuffer(frameVertices.size)
    frameVertexBuffer.put(frameVertices)
    (frameVertexBuffer as Buffer).flip()

    preRender()

    GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT)
    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY)
    GL11.glColor4f(color.red.toFloat()/255f, color.green.toFloat()/255f, color.blue.toFloat()/255f, color.alpha.toFloat()/255f)
    GL11.glVertexPointer(2, 0, vertexBuffer)
    GL11.glDrawArrays(GL11.GL_TRIANGLE_FAN, 0, vertices.size / 2)
    GL11.glColor4f(frameColor.red.toFloat()/255f, frameColor.green.toFloat()/255f, frameColor.blue.toFloat()/255f, frameColor.alpha.toFloat()/255f)
    GL11.glVertexPointer(2, 0, frameVertexBuffer)
    GL11.glDrawArrays(GL11.GL_LINE_LOOP, 0, vertices.size / 2)
    GL11.glPopClientAttrib()
    postRender()

}