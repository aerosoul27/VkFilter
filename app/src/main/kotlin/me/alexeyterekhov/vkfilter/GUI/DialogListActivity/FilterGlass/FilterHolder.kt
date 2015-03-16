package me.alexeyterekhov.vkfilter.GUI.DialogListActivity.FilterGlass

import android.support.v7.widget.RecyclerView
import android.view.View
import me.alexeyterekhov.vkfilter.R
import android.widget.TextView
import me.alexeyterekhov.vkfilter.GUI.Common.TripleSwitchView


class FilterHolder(val v: View): RecyclerView.ViewHolder(v) {
    val filterName = v.findViewById(R.id.filterName) as TextView
    val avatarList = v.findViewById(R.id.memberList) as RecyclerView
    val switch = v.findViewById(R.id.filterStateSwitch) as TripleSwitchView
}