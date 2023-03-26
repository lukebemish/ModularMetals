package dev.lukebemish.modularmetals.client.variant

import dev.lukebemish.modularmetals.Constants
import dev.lukebemish.modularmetals.template.TemplateEngine
import dev.lukebemish.modularmetals.client.planner.BlockstatePlanner
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.data.variant.block.BlockVariant
import dev.lukebemish.modularmetals.data.variant.ItemVariant
import net.minecraft.resources.ResourceLocation

import java.util.concurrent.atomic.AtomicReference

class BlockClientVariantHandler extends ItemClientVariantHandler {
    @Override
    String getHeader() {
        return 'block'
    }

    @Override
    Map<String, Map> defaultModel(ResourceLocation fullLocation) {
        return Map.<String, Map> of('', (Map) ['parent': 'block/cube_all', 'textures': ['all': '${textures[""]}']], 'item', (Map) ['parent': "${fullLocation.namespace}:block/${fullLocation.path}" as String])
    }

    @Override
    protected void fillPlanners(Map<String, Map> models, ResourceLocation fullLocation, Map replacements, Metal metal, ItemVariant variant, ResourceLocation metalRl, ResourceLocation variantRl) {
        if (variant !instanceof BlockVariant) {
            return
        }

        super.fillPlanners(models, fullLocation, replacements, metal, variant, metalRl, variantRl)

        // Generate blockstates
        AtomicReference<ResourceLocation> mainModel = new AtomicReference<>(new ResourceLocation(fullLocation.namespace, "$header/${fullLocation.path}"))
        models = new HashMap<>(models)
        models.computeIfAbsent('item', {
            String key = models.keySet().sort().get(0)
            mainModel.set(new ResourceLocation(fullLocation.namespace, "$header/${fullLocation.path}${key == '' ? '' : "_$key"}"))
            return ['parent':mainModel.get().toString()]
        })

        Map map = variant.blockTexturing.blockstate.map {it.map}.orElseGet {->
            ['variants':['':[
                'model': mainModel.get().toString()
            ]]]}
        try {
            Map out = TemplateEngine.fillReplacements(map, replacements)
            BlockstatePlanner.instance.plan(fullLocation, out)
        } catch (Exception e) {
            Constants.LOGGER.error("Error writing blockstate for metal '${metalRl}', variant '${variantRl}':", e)
        }
    }
}
