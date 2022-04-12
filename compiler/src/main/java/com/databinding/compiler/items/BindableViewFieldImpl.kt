package com.databinding.compiler.items

/**
 * Class which represents a @BindTo annotated view field.
 */
class BindableViewFieldImpl(// field name
    var fieldName: String, // path without class names (the parent class name is held in the map of bindableViewFields)
    var objectPath: String, // full class name of the view held in the parent view
    var fieldViewClassName: String, // simple class name of the object held in the parent data
    var fieldObjectClassName: String
)