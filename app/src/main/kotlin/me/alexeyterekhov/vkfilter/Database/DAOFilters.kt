package me.alexeyterekhov.vkfilter.Database

import com.activeandroid.Model
import com.activeandroid.query.Select
import me.alexeyterekhov.vkfilter.DataCache.Helpers.DataDepend
import me.alexeyterekhov.vkfilter.GUI.Mock.Mocker
import java.util.Vector

public object DAOFilters {
    val changeListeners = Vector<DataDepend>()

    fun loadVkFilters(): List<VkFilter> {
        if (Mocker.MOCK_MODE)
            return Mocker.mockFilters()
        return Select().all()
                    .from(javaClass<VkFilter>())
                    .orderBy("ListOrder")
                    .execute()
    }

    fun loadVkFilterById(id: Long): VkFilter {
        return Model.load(javaClass<VkFilter>(), id)
    }

    fun saveFilter(f: VkFilter) {
        f.save()
        for (l in changeListeners)
            l.onDataUpdate()
    }

    fun deleteFilter(f: VkFilter) {
        f.identifiers() forEach { it.delete() }
        f.delete()
    }
}