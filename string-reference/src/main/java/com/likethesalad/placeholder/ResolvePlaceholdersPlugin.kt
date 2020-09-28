package com.likethesalad.placeholder

import com.likethesalad.placeholder.data.helpers.AndroidProjectHelper
import com.likethesalad.placeholder.di.AppInjector
import com.likethesalad.placeholder.models.PlaceholderExtension
import com.likethesalad.placeholder.tasks.GatherRawStringsTask
import com.likethesalad.placeholder.tasks.GatherTemplatesTask
import com.likethesalad.placeholder.tasks.ResolvePlaceholdersTask
import com.likethesalad.placeholder.utils.TaskActionProvider
import org.gradle.api.Plugin
import org.gradle.api.Project

class ResolvePlaceholdersPlugin : Plugin<Project> {

    companion object {
        const val RESOLVE_PLACEHOLDERS_TASKS_GROUP_NAME = "resolver"
        const val EXTENSION_NAME = "stringXmlReference"
    }

    override fun apply(project: Project) {
        project.plugins.withId("com.android.application") {
            val extension = project.extensions.create(EXTENSION_NAME, PlaceholderExtension::class.java)
            val projectHelper = AndroidProjectHelper(project)
            project.afterEvaluate {
                val taskActionProviderFactory = AppInjector.getTaskActionProviderFactory()
                val variantDataExtractorFactory = AppInjector.getVariantDataExtractorFactory()

                projectHelper.androidExtension.getApplicationVariants().forEach {
                    createResolvePlaceholdersTaskForVariant(
                        project,
                        taskActionProviderFactory.create(variantDataExtractorFactory.create(it)),
                        extension.resolveOnBuild
                    )
                }
            }
        }
    }

    private fun createResolvePlaceholdersTaskForVariant(
        project: Project,
        taskActionProvider: TaskActionProvider,
        resolveOnBuild: Boolean
    ) {
        val androidVariantHelper = taskActionProvider.androidVariantHelper

        val gatherStringsTask = project.tasks.create(
            androidVariantHelper.tasksNames.gatherRawStringsName,
            GatherRawStringsTask::class.java
        ) {
            it.group = RESOLVE_PLACEHOLDERS_TASKS_GROUP_NAME
            it.gatherRawStringsAction = taskActionProvider.gatherRawStringsAction
            it.dependenciesRes = taskActionProvider.androidConfigHelper.librariesResDirs
            it.gradleGeneratedStrings = androidVariantHelper.generateResValuesTask.outputs.files
        }

        val gatherTemplatesTask = project.tasks.create(
            androidVariantHelper.tasksNames.gatherStringTemplatesName,
            GatherTemplatesTask::class.java
        ) {
            it.group = RESOLVE_PLACEHOLDERS_TASKS_GROUP_NAME
            it.dependsOn(gatherStringsTask)

            it.gatherTemplatesAction = taskActionProvider.gatherTemplatesAction
        }

        val resolvePlaceholdersTask = project.tasks.create(
            androidVariantHelper.tasksNames.resolvePlaceholdersName,
            ResolvePlaceholdersTask::class.java
        ) {
            it.group = RESOLVE_PLACEHOLDERS_TASKS_GROUP_NAME
            it.dependsOn(gatherTemplatesTask)

            it.resolvePlaceholdersAction = taskActionProvider.resolvePlaceholdersAction
        }

        if (resolveOnBuild) {
            androidVariantHelper.mergeResourcesTask.dependsOn(resolvePlaceholdersTask)
        }
    }
}