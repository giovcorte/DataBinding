# DataBinding
Android view data binding made simple!
    
A more user friendly docu is coming! This library doesn't use any external library, so it's light weight and dependencies free.

Example usages:

Example model classes:

```kotlin
data class User {

    val name: String

    val photo: Photo
    
    val action: IViewAction

}

data class Photo {

    val url: String

}
```

View class: annotate the subviews in order to specify, for each model you want to bind, a path to find the parameter to bind. 

```kotlin
@BindableView
@BindAction(["User.action"])
class UserView: ConstraintLayout {

    @BindWith(paths = ["User.text:String"])
    val text: TextView
    @BindAction(["User.action])
    @BindWith(paths = ["User.photo.url:String"])
    val image: ImageView

    // Constructors...
    init(@NonNull Context context) {
        ...
    }

}
```

Everywhere you want, define a static method to bind a custom view - data model pair. You can annotate parameters with @Inject in order to provide dependency to your components, it will be injected in the DataBinding constructor.

```kotlin
@BindingMethod
@JvmStatic
fun bindUser(@View userView: UserView?, @Data user: User?, @Inject sharedPreferences: SharedPreferences?) {
    // do whatever you want
}

@BindingMethod
public static void bindText(@View view: TextView?, @Data text: String?) {
    // do whatever you want, for example view.setText(text);
}
```

To bind all yopu view tree now you only have to get a DataBinding instance and call bind(..) with your parameters, using the overloading. The library also enables to display different objects in RecyclerViews. Annotate your custom views with @BindableView, the objects to display with @BindableObject(CustomView.class), and in both implement respectively IView and IData interfaces, returning the simple class name of each. Then use GenericRecyclerViewAdapter with the AdapterDatabinding and ViewFactory generated classes.
To enable compatibility with Kotlin, if you use this library with Java you must implement getter methods for all yur views and data model. In Kotlin these are by default availables.
