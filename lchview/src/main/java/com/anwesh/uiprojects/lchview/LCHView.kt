package com.anwesh.uiprojects.lchview

/**
 * Created by anweshmishra on 17/07/18.
 */

import android.app.Activity
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

    private val renderer : Renderer = Renderer(this)

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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

    data class LCHNode(var i : Int, val state : State = State()) {

        var prev : LCHNode? = null

        var next : LCHNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = LCHNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(stopcb : (Int, Float) -> Unit) {
            state.update {
                stopcb(i, it)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : LCHNode {
            var curr : LCHNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedCircHighlighter(var i : Int) {

        private var curr : LCHNode = LCHNode(0)

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint: Paint) {
            curr.draw(canvas, paint)
        }

        fun update(stopcb : (Int, Float) -> Unit) {
            curr.update {j,scale ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                stopcb(j, scale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : LCHView) {

        private val lch : LinkedCircHighlighter = LinkedCircHighlighter(0)

        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            lch.draw(canvas, paint)
            animator.animate {
                lch.update {j, scale ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            lch.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : LCHView {
            val view : LCHView = LCHView(activity)
            activity.setContentView(view)
            return view
        }
    }
}