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

val nodes : Int = 5

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
}