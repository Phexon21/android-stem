package com.likethesalad.placeholder.modules.templateStrings

import com.likethesalad.android.templates.common.tasks.templates.TemplatesConstants
import com.likethesalad.android.templates.common.tasks.templates.data.TemplateItem
import com.likethesalad.android.templates.common.tasks.templates.data.TemplateItemsSerializer
import com.likethesalad.placeholder.modules.common.helpers.android.AndroidVariantContext
import com.likethesalad.placeholder.modules.templateStrings.models.StringsTemplatesModel
import com.likethesalad.tools.resource.api.android.data.AndroidResourceType
import com.likethesalad.tools.resource.api.android.environment.Language
import com.likethesalad.tools.resource.api.android.modules.string.StringAndroidResource
import com.likethesalad.tools.resource.api.collection.ResourceCollection
import com.likethesalad.tools.resource.locator.android.extension.configuration.data.ResourcesProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.io.File

class GatherTemplatesAction @AssistedInject constructor(
    @Assisted private val androidVariantContext: AndroidVariantContext,
    private val templateItemsSerializer: TemplateItemsSerializer
) {

    @AssistedFactory
    interface Factory {
        fun create(androidVariantContext: AndroidVariantContext): GatherTemplatesAction
    }

    private val resourcesHandler = androidVariantContext.androidResourcesHandler

    fun gatherTemplateStrings(
        outputDir: File,
        commonResources: ResourcesProvider,
        templateIdsContainer: File
    ) {
        val commonHandler = commonResources.resources
        val templateIds = getTemplateIds(templateIdsContainer)
        for (language in commonHandler.listLanguages()) {
            val allResources = asStringResources(commonHandler.getMergedResourcesForLanguage(language))
            val templates = getTemplatesFromResources(templateIds, allResources)
            val resources = allResources.minus(templates)
            resourcesHandler.saveTemplates(outputDir, gatheredStringsToTemplateStrings(language, resources, templates))
        }
    }

    private fun getTemplatesFromResources(
        templateIds: List<TemplateItem>,
        resources: List<StringAndroidResource>
    ): List<StringAndroidResource> {
        val templateNames = templateIds.map { it.name }
        return resources.filter {
            it.name() in templateNames
        }
    }

    private fun getTemplateIds(templateIdsContainer: File): List<TemplateItem> {
        return templateItemsSerializer.deserialize(templateIdsContainer.readText())
    }

    private fun gatheredStringsToTemplateStrings(
        language: Language,
        stringResources: List<StringAndroidResource>,
        stringTemplates: List<StringAndroidResource>
    ): StringsTemplatesModel {
        val placeholdersResolved = getPlaceholdersResolved(stringResources, stringTemplates)

        return StringsTemplatesModel(
            language,
            stringTemplates,
            placeholdersResolved
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun asStringResources(resources: ResourceCollection): List<StringAndroidResource> {
        return resources.getResourcesByType(AndroidResourceType.StringType) as List<StringAndroidResource>
    }

    private fun getPlaceholdersResolved(
        strings: List<StringAndroidResource>,
        templates: List<StringAndroidResource>
    ): Map<String, String> {
        val stringsMap = stringResourcesToMap(strings)
        val placeholders = templates.map { TemplatesConstants.PLACEHOLDER_REGEX.findAll(it.stringValue()) }
            .flatMap { it.toList().map { m -> m.groupValues[1] } }.toSet()

        val placeholdersResolved = mutableMapOf<String, String>()

        for (it in placeholders) {
            placeholdersResolved[it] = stringsMap.getValue(it)
        }

        return placeholdersResolved
    }

    private fun stringResourcesToMap(list: Collection<StringAndroidResource>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (it in list) {
            map[it.name()] = it.stringValue()
        }
        return map
    }
}