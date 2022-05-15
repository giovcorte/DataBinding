package com.databinding.compiler.items

class BindingMethodImpl(
    var enclosingClassName: String,
    var methodName: String,
    var viewClassName: String,
    var dataClassName: String,
    var dependencies: MutableList<String>
)