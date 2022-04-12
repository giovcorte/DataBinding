package com.databinding.databinding

import android.view.View

object DataBindingHelper {

    @JvmStatic
    fun bindAction(view: View?, action: IViewAction?) {
        view?.setOnClickListener { action?.onClick() }
    }

}