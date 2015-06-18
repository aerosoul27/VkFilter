package me.alexeyterekhov.vkfilter.GUI.EditFilterActivity.IconList

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import me.alexeyterekhov.vkfilter.Database.FilterIcons
import me.alexeyterekhov.vkfilter.R
import me.alexeyterekhov.vkfilter.Util.AppContext


class IconAdapter(): RecyclerView.Adapter<IconHolder>() {
    private var selectedIcon: Int = 0

    private val normalColor = AppContext.instance.getResources().getColor(R.color.my_gray_new)
    private val selectedColor = AppContext.instance.getResources().getColor(R.color.my_green)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): IconHolder? {
        val inflater = LayoutInflater.from(AppContext.instance)
        val view = inflater.inflate(R.layout.item_icon, parent, false)
        return IconHolder(view)
    }

    override fun onBindViewHolder(holder: IconHolder, position: Int) {
        with (holder) {
            icon.setImageResource(FilterIcons.resourceById(position + 1))
            icon.setBackgroundColor(
                    if (position == selectedIcon)
                        selectedColor
                    else
                        normalColor
            )

            v setOnClickListener {
                selectedIcon = position
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount() = FilterIcons.count()

    fun getSelectedIconId() = selectedIcon + 1
    fun setSelectedIconId(id: Int) { selectedIcon = id }
}