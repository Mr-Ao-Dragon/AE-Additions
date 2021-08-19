package com.the9grounds.aeadditions.api.chemical

import appeng.api.storage.data.IAEStack
import mekanism.api.chemical.Chemical
import mekanism.api.chemical.ChemicalStack
import mekanism.api.chemical.IChemicalHandler
import java.util.*

interface IAEChemicalStack : IAEStack<IAEChemicalStack>, Comparable<IAEChemicalStack> {
    /**
     * @return [ChemicalStack]
     */
    fun getChemicalStack(): ChemicalStack<*>

    /**
     * @return [Chemical]
     */
    fun getChemical(): Chemical<*>
    
    fun getCapabilityForChemical(): Optional<IChemicalHandler<*, *>>
}