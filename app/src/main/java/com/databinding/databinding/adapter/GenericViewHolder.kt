package com.databinding.databinding.adapter

import android.view.View
import com.databinding.databinding.IView
import com.databinding.databinding.IData
import androidx.recyclerview.widget.RecyclerView
import android.widget.LinearLayout
import android.view.ViewGroup

/**
 * Class representing the only RecyclerView.ViewHolder needed. It simply holds the view and the
 * corresponding data.
 */
class GenericViewHolder(view: IView, data: IData) : RecyclerView.ViewHolder(view as View) {
    var data: IData
    var view: IView

    init {
        itemView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        this.data = data
        this.view = view
    }
}