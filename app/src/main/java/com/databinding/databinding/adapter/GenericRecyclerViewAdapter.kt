package com.databinding.databinding.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.databinding.databinding.IAdapterDataBinding
import com.databinding.databinding.IData
import com.databinding.databinding.IViewFactory

/**
 * The only adapter needed for bind any ViewConfiguration object into an Android RecyclerView.
 */
@Suppress("unused")
class GenericRecyclerViewAdapter : RecyclerView.Adapter<GenericViewHolder> {

    private val dataBinding: IAdapterDataBinding
    private val viewFactory: IViewFactory
    private val items: MutableList<IData>

    /**
     * Constructor.
     *
     * @param dataBinding IAdapterDataBinding dataBinding.
     * @param items List of IData objects.
     */
    constructor(
        dataBinding: IAdapterDataBinding,
        viewFactory: IViewFactory,
        items: MutableList<IData>
    ) {
        this.dataBinding = dataBinding
        this.viewFactory = viewFactory
        this.items = items
    }

    /**
     * Constructor.
     *
     * @param dataBinding IAdapterDataBinding dataBinding.
     */
    constructor(
        dataBinding: IAdapterDataBinding,
        viewFactory: IViewFactory
    ) {
        this.dataBinding = dataBinding
        this.viewFactory = viewFactory
        items = ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder {
        val data = items[viewType]
        val view = viewFactory.build(data)
        return GenericViewHolder(view, data)
    }

    override fun onBindViewHolder(holder: GenericViewHolder, position: Int) {
        dataBinding.bind(holder.view, holder.data)
    }

    /**
     * Adds a list of UI.ViewConfiguration annotated object to the current recycler view.
     *
     * @param newItems Generic list of IData objects
     */
    fun addItems(newItems: List<IData>?) {
        items.addAll(newItems!!)
        notifyItemRangeInserted(0, items.size)
    }

    /**
     * Appends a new child to the root configuration and displays it, if the key
     * is admissible by the adapter filter.
     *
     * @param data new IData object.
     */
    @Synchronized
    fun addItem(data: IData) {
        items.add(data)
        notifyItemInserted(items.size)
    }

    /**
     * Removes the item in the specified position.
     *
     * @param position integer for the position desired.
     */
    @Synchronized
    fun removeItem(position: Int) {
        if (position >= 0 && position < items.size) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /**
     * Finds and returns the position of the first element which has the specified name.
     *
     * @param key String representing the name for the given IData element.
     * @return integer for the position.
     */
    fun itemPositionByKey(key: String): Int {
        var position = 0
        var found = false
        for (data in items) {
            if (data.name() == key) {
                found = true
                break
            }
            position++
        }
        return if (found) position else -1
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    /**
     * Access to the displayed collection.
     *
     * @return the current children displayed.
     */
    fun getItems(): List<IData> {
        return items
    }

    /**
     * Access to the IData at the specified index.
     *
     * @param index integer representing the position of the desired element.
     * @return the IData at the given index.
     */
    fun getItem(index: Int): IData {
        return items[index]
    }

    /**
     * Access to the IData at the specified index.
     *
     * @param index integer representing the position of the desired element.
     * @param type class of the returned object.
     * @return the T object at the given index.
     */
    fun <T> getItem(index: Int, type: Class<out T>): T? {
        val data = getItem(index)
        return try {
            type.cast(data)
        } catch (e: ClassCastException) {
            null
        }
    }

    /**
     * Clears all the children and stops the tasks in each binder.
     */
    fun clearItems() {
        val oldSize = items.size
        items.clear()
        notifyItemRangeRemoved(0, oldSize)
    }
}