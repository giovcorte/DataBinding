package com.databinding.databinding

interface IViewFactory {
    fun build(data: IData): IView
}