package org.jetbrains.dukat.model.commonLowerings

import org.jetbrains.dukat.astCommon.IdentifierEntity
import org.jetbrains.dukat.astCommon.NameEntity
import org.jetbrains.dukat.astCommon.toNameEntity
import org.jetbrains.dukat.astModel.*
import org.jetbrains.dukat.astModel.visitors.visitTopLevelModel

private fun ModuleModel.addStandardImportsAndAnnotations(qualifierName: NameEntity?) {

    qualifierName?.let { qualifier ->
        val hasTypeAliases = declarations.any { it is TypeAliasModel }

        // Can't put non-external declarations in file marked with JsQualifier annotation
        if (!hasTypeAliases) annotations.add(AnnotationModel("file:JsQualifier", listOf(qualifier)))
    }

    annotations.add(AnnotationModel("file:Suppress", listOf(
            "ABSTRACT_MEMBER_NOT_IMPLEMENTED",
            "VAR_TYPE_MISMATCH_ON_OVERRIDE",
            "INTERFACE_WITH_SUPERCLASS",
            "OVERRIDING_FINAL_MEMBER",
            "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
            "CONFLICTING_OVERLOADS",
            "EXTERNAL_DELEGATION",
            "PackageDirectoryMismatch"
    ).map { it.toNameEntity() }))

    imports.addAll(0,
            listOf(
                    "kotlin.js.*",
                    "kotlin.js.Json",
                    "org.khronos.webgl.*",
                    "org.w3c.dom.*",
                    "org.w3c.dom.events.*",
                    "org.w3c.dom.parsing.*",
                    "org.w3c.dom.svg.*",
                    "org.w3c.dom.url.*",
                    "org.w3c.fetch.*",
                    "org.w3c.files.*",
                    "org.w3c.notifications.*",
                    "org.w3c.performance.*",
                    "org.w3c.workers.*",
                    "org.w3c.xhr.*"
            ).map { ImportModel(it.toNameEntity()) }
    )
}

private fun InterfaceModel.hasNestedEntity(): Boolean {
    if ((companionObject?.members?.isNotEmpty() == true) || (companionObject?.parentEntities?.isNotEmpty() == true)) {
        return true
    }

    return members.any { (it is ClassLikeModel) }
}

class AddStandardImportsAndAnnotations(private val qualifierName: NameEntity?) : ModelLowering {
    override fun lower(module: ModuleModel): ModuleModel {
        module.visitTopLevelModel { topLevelModel ->
            when (topLevelModel) {
                is InterfaceModel -> {
                    if (topLevelModel.hasNestedEntity()) {
                        topLevelModel.annotations.add(AnnotationModel("Suppress", listOf(IdentifierEntity("NESTED_CLASS_IN_EXTERNAL_INTERFACE"))))
                    }
                }
                is ModuleModel -> {
                    topLevelModel.addStandardImportsAndAnnotations(qualifierName)
                }
            }
        }

        return module
    }
}
