package com.anwesh.uiprojects.lchview

/**
 * Created by anweshmishra on 17/07/18.
 */

import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.RectF
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Color

val nodes : Int = 5

fun Canvas.drawAtMid(cb : () -> Unit) {
    save()
    translate(width.toFloat()/2, height.toFloat()/2)
    cb()
    restore()
}

fun Canvas.drawHighlightingArc(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = (Math.min(w, h) / 2.2f) / nodes
    val r : Float = gap * (i + 1)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = Math.min(w, h) / 50
    paint.strokeCap = Paint.Cap.ROUND
    for (j in 0..3) {
        save()
        rotate(j * 90f)
        paint.color = Color.parseColor("#9E9E9E")
        drawArc(RectF(-r, -r, r, r), 0f, 90f, false, paint)
        paint.color = Color.parseColor("#EEEEEE")
        drawArc(RectF(-r, -r, r, r), 0f, 90f * scale, false, paint)
        restore()
    }
}

fun Canvas.drawNode(i : Int, scale : Float, paint : Paint) {
    drawAtMid {
        drawHighlightingArc(i, scale, paint)
    }
}

class LCHView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {
        fun update(stopcb : (Float) -> Unit) {
            scale += 0.1f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                stopcb(prevScale)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {
        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }
    }
}