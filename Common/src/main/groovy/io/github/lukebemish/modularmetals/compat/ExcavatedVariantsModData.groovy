package io.github.lukebemish.modularmetals.compat

import io.github.lukebemish.excavated_variants.api.ExcavatedVariantsListener
import io.github.lukebemish.excavated_variants.api.IDataProvider
import io.github.lukebemish.excavated_variants.data.ModData

@ExcavatedVariantsListener
class ExcavatedVariantsModData implements IDataProvider {
    @Override
    ModData provide() {
        return null
    }
}
