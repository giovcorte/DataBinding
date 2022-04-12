package com.databinding.databinding.test

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.databinding.databinding.IView
import com.databinding.annotations.BindWith
import android.widget.TextView
import com.databinding.annotations.BindAction
import com.databinding.annotations.BindableView

@BindableView
@BindAction(paths = ["ExampleModel.action"])
class ExampleView(context: Context?) : View(context), IView {
    @BindWith(paths = ["ExampleModel.text:String", "ExampleModel2.obj.text:String"])
    var text: TextView? = null

    @BindWith(paths = ["ExampleModel.text:String"])
    var image: ImageView? = null

    override fun name(): String {
        return "ExampleView"
    }
}