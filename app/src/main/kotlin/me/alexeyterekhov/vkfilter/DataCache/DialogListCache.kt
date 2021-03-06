package me.alexeyterekhov.vkfilter.DataCache

import me.alexeyterekhov.vkfilter.DataCache.Common.DataDepend
import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.Data.Dialog
import me.alexeyterekhov.vkfilter.GUI.DialogsActivity.Data.DialogListSnapshot
import me.alexeyterekhov.vkfilter.GUI.Mock.Mocker
import java.util.*

object DialogListCache {
    public val listeners: Vector<DataDepend> = Vector()

    private var snapshot = emptySnapshot()

    infix fun updateSnapshot(snap: DialogListSnapshot) {
        snapshot = snap
        for (l in listeners) l.onDataUpdate()
    }

    fun getSnapshot(): DialogListSnapshot {
        return if (Mocker.MOCK_MODE)
            Mocker.mockDialogSnapshot()
        else
            snapshot
    }

    fun clear() {
        snapshot = emptySnapshot()
    }

    fun emptySnapshot() = DialogListSnapshot(
            0,
            Vector<Dialog>()
    )
}