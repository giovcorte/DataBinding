package com.databinding.compiler.items

class BindableActionFieldImpl(// field name
    var fieldName: String, // path without class names (the parent class name is held in the map of bindableViewFields)
    var objectPath: String
)