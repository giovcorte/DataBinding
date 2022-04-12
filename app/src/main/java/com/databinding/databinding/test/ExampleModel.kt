package com.databinding.databinding.test

import com.databinding.annotations.BindableObject
import com.databinding.databinding.IData
import com.databinding.databinding.IViewAction

@BindableObject(view = ExampleView::class)
class ExampleModel : IData {

    var text: String? = null
    var action: IViewAction? = null

    override fun name(): String {
        return "ExampleModel"
    }
}