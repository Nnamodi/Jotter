package com.roland.android.jotter.util

import android.util.Log
import android.view.MotionEvent
import android.view.View

class TouchListener : View.OnTouchListener {
    private var initialX = 0f
    private var initialY = 0f
    private var startX = 0f
    private var startY = 0f

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                initialX = view.x
                initialY = view.y
                view.isPressed = true
                startX = event.rawX - initialX
                startY = event.rawY - initialY
            }
            MotionEvent.ACTION_MOVE -> {
                view.apply {
                    // This calculation makes the view move from its current position instead of jumping onMove.
                    x = event.rawX - startX
                    y = event.rawY - startY
                }
            }
            MotionEvent.ACTION_UP -> {
                if (view.x.equals(initialX) && view.y.equals(initialY)) {
                    view.performClick()
                } else {
                    view.apply {
                        x = initialX
                        y = initialY
                        isPressed = false
                    }
                }
            }
        }
        Log.d("TouchInfo", "RawX, Y = ${event.rawX}, ${event.rawY}| View.x, .y = ${view.x}, ${view.y}| StartX, Y = $startX, $startY.")
        return true
    }
}