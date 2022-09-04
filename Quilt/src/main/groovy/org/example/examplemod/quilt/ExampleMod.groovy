package org.example.examplemod.quilt

import org.example.examplemod.CommonClass
import org.example.examplemod.Constants
import org.quiltmc.qsl.tooltip.api.client.ItemTooltipCallback

Constants.LOG.info("Hello Quilt world!")
CommonClass.init()
//noinspection GroovyAssignabilityCheck
ItemTooltipCallback.EVENT << CommonClass::onItemTooltip
