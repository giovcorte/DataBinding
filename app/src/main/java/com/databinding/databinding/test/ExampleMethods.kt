package com.databinding.databinding.test

import android.widget.ImageView
import android.widget.TextView
import com.databinding.annotations.BindingMethod
import com.databinding.annotations.Data
import com.databinding.annotations.Inject
import com.databinding.annotations.View

object ExampleMethods {
    @JvmStatic
    @BindingMethod
    fun bindExampleView(
        @View view: ExampleView?,
        @Data model: ExampleModel?,
        @Inject exampleRepo: ExampleRepo?
    ) {
    }

    @JvmStatic
    @BindingMethod
    fun bindExampleView2(
        @View view: ExampleView?,
        @Data model: ExampleModel2?,
        @Inject exampleRepo: ExampleRepo?
    ) {
    }

    @JvmStatic
    @BindingMethod
    fun bindImageView(@View view: ImageView?, @Data url: String?) {

    }

    @JvmStatic
    @BindingMethod
    fun bindText(@View view: TextView?, @Data text: String?) {
        view?.text = text
    }

    @JvmStatic
    @BindingMethod
    fun bindText(@View view: TextView?, @Data text: ExampleModel?) {
        view?.text = text?.text
    }
}