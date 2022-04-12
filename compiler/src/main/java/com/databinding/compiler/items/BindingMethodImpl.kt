package com.databinding.compiler.items

class BindingMethodImpl(
    var enclosingClass: String,
    var methodName: String,
    var viewClass: String,
    var dataClass: String,
    var dependencies: MutableList<String>
)