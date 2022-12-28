package io.github.lukebemish.modularmetals.compat

import dev.lukebemish.excavatedvariants.api.ExcavatedVariantsListener
import dev.lukebemish.excavatedvariants.data.ModData

@ExcavatedVariantsListener
class ExcavatedVariantsModData implements IDataProvider {
    @Override
    ModData provide() {
        return null
    }
}
