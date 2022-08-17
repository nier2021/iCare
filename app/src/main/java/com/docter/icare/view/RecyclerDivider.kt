package com.docter.icare.view

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat.getDrawable
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.docter.icare.R

class RecyclerDivider(
    context: Context,
    private val color: Color
) : ItemDecoration() {

    private val appContext = context.applicationContext

    enum class Color(
        val dividerId: Int
    ) {
        PRIMARY(R.drawable.line_primary)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: State) {
        super.onDraw(c, parent, state)

        getDrawable(appContext, color.dividerId)?.let { drawable ->

            val dividerLeft = parent.paddingLeft
            val dividerRight = parent.width - parent.paddingRight
            val childCount = parent.childCount

            (0..childCount - 2).forEach {

                val child = parent.getChildAt(it)
                val params = child.layoutParams as LayoutParams
                val dividerTop = child.bottom + params.bottomMargin
                val dividerBottom = dividerTop + drawable.intrinsicHeight

                drawable.apply { setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom) }.draw(c)
            }
        }
    }
}