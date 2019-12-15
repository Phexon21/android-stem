package com.likethesalad.placeholder.data

import com.likethesalad.placeholder.utils.ValuesNameUtils
import com.likethesalad.placeholder.utils.XmlUtils
import java.io.File

class VariantValuesFolders(
    variantName: String,
    private val resDirs: Set<File>
) {

    val valuesStringFiles: Map<String, ValuesStringFiles> by lazy {
        val valuesFilesMap = mutableMapOf<String, ValuesStringFiles>()
        val valuesFolderNames = ValuesNameUtils.getUniqueValuesDirName(resDirs)
        for (valuesFolderName in valuesFolderNames) {
            val filesInValuesFolder = getFilesFromValuesFolders(valuesFolderName)
            if (filesInValuesFolder.isNotEmpty()) {
                valuesFilesMap[valuesFolderName] = ValuesStringFiles(filesInValuesFolder.toSet())
            }
        }

        valuesFilesMap
    }

    private fun getFilesFromValuesFolders(
        valuesFolderName: String
    ): List<File> {
        val filesInValuesFolder = mutableListOf<File>()

        for (resDir in resDirs) {
            getValuesFolderFromDir(resDir, valuesFolderName)?.let {
                filesInValuesFolder.addAll(it.listFiles { _, name ->
                    XmlUtils.isValidRawXmlFileName(name)
                } ?: emptyArray())
            }
        }

        return filesInValuesFolder
    }

    private fun getValuesFolderFromDir(dir: File, valuesFolderName: String): File? {
        val valuesFolder = File(dir, valuesFolderName)
        if (valuesFolder.exists()) {
            return valuesFolder
        }
        return null
    }

}