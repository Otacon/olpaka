package org.cyanotic.olpaka.core

import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

interface StringResources {

    suspend fun get(resource: StringResource) : String
}

class StringResourcesDefault : StringResources {

    override suspend fun get(resource: StringResource) = getString(resource)

}