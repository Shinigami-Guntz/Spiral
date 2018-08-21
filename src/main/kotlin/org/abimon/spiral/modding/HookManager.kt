package org.abimon.spiral.modding

import ch.qos.logback.classic.Level
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import org.abimon.spiral.core.*
import org.abimon.spiral.core.archives.IArchive
import org.abimon.spiral.core.data.PatchOperation
import org.abimon.spiral.core.data.SpiralData
import java.io.File
import java.io.InputStream

object HookManager {
    val BEFORE_OPERATING_CHANGE: BeforeHookChangeMutableList<File?> = ArrayList()
    val ON_OPERATING_CHANGE: OnHookChangeMutableList<File?> = ArrayList()

    val BEFORE_FILE_OPERATING_CHANGE: BeforeHookChangeMutableList<File?> = ArrayList()
    val ON_FILE_OPERATING_CHANGE: OnHookChangeMutableList<File?> = ArrayList()

    val BEFORE_SCOPE_CHANGE: BeforeHookChangeMutableList<GurrenScope> = ArrayList()
    val ON_SCOPE_CHANGE: OnHookChangeMutableList<GurrenScope> = ArrayList()

    val BEFORE_LOGGER_LEVEL_CHANGE: BeforeHookChangeMutableList<Level> = ArrayList()
    val ON_LOGGER_LEVEL_CHANGE: OnHookChangeMutableList<Level> = ArrayList()

    val BEFORE_CACHE_ENABLED_CHANGE: BeforeHookChangeMutableList<Boolean> = ArrayList()
    val ON_CACHE_ENABLED_CHANGE: OnHookChangeMutableList<Boolean> = ArrayList()

    val BEFORE_CONCURRENT_OPERATIONS_CHANGE: BeforeHookChangeMutableList<Int> = ArrayList()
    val ON_CONCURRENT_OPERATIONS_CHANGE: OnHookChangeMutableList<Int> = ArrayList()

    val BEFORE_AUTO_CONFIRM_CHANGE: BeforeHookChangeMutableList<Boolean> = ArrayList()
    val ON_AUTO_CONFIRM_CHANGE: OnHookChangeMutableList<Boolean> = ArrayList()

    val BEFORE_PURGE_CACHE_CHANGE: BeforeHookChangeMutableList<Boolean> = ArrayList()
    val ON_PURGE_CACHE_CHANGE: OnHookChangeMutableList<Boolean> = ArrayList()

    val BEFORE_PATCH_OPERATION_CHANGE: BeforeHookChangeMutableList<PatchOperation?> = ArrayList()
    val ON_PATCH_OPERATION_CHANGE: OnHookChangeMutableList<PatchOperation?> = ArrayList()

    val BEFORE_PATCH_FILE_CHANGE: BeforeHookChangeMutableList<File?> = ArrayList()
    val ON_PATCH_FILE_CHANGE: OnHookChangeMutableList<File?> = ArrayList()

    val BEFORE_ATTEMPT_FINGERPRINT_CHANGE: BeforeHookChangeMutableList<Boolean> = ArrayList()
    val ON_ATTEMPT_FINGERPRINT_CHANGE: OnHookChangeMutableList<Boolean> = ArrayList()

    val BEFORE_PRINT_EXTRACT_CHANGE: BeforeHookChangeMutableList<Boolean> = ArrayList()
    val ON_PRINT_EXTRACT_CHANGE: OnHookChangeMutableList<Boolean> = ArrayList()

    val BEFORE_PRINT_COMPILE_CHANGE: BeforeHookChangeMutableList<Boolean> = ArrayList()
    val ON_PRINT_COMPILE_CHANGE: OnHookChangeMutableList<Boolean> = ArrayList()

    val BEFORE_NO_FLUFF_CHANGE: BeforeHookChangeMutableList<Boolean> = ArrayList()
    val ON_NO_FLUFF_CHANGE: OnHookChangeMutableList<Boolean> = ArrayList()

    val BEFORE_MULTITHREADED_SIMPLE_CHANGE: BeforeHookChangeMutableList<Boolean> = ArrayList()
    val ON_MULTITHREADED_SIMPLE_CHANGE: OnHookChangeMutableList<Boolean> = ArrayList()

    val BEFORE_TABLE_OUTPUT_CHANGE: BeforeHookChangeMutableList<Boolean> = ArrayList()
    val ON_TABLE_OUTPUT_CHANGE: OnHookChangeMutableList<Boolean> = ArrayList()

    val BEFORE_EXTRACT: MutableList<Pair<IPlugin, (IArchive, File, List<Pair<String, () -> InputStream>>, Boolean) -> Boolean>> = ArrayList()
    val ON_EXTRACT: MutableList<Pair<IPlugin, (IArchive, File, List<Pair<String, () -> InputStream>>) -> Unit>> = ArrayList()
    val DURING_EXTRACT: MutableList<Pair<IPlugin, (IArchive, File, List<Pair<String, () -> InputStream>>, Pair<String, () -> InputStream>) -> Unit>> = ArrayList()
    val AFTER_EXTRACT: MutableList<Pair<IPlugin, (IArchive, File, List<Pair<String, () -> InputStream>>) -> Unit>> = ArrayList()

    fun beforeOperatingChange(old: File?, new: File?): Boolean =
            beforeChange(old, new, BEFORE_OPERATING_CHANGE)

    fun beforeFileOperatingChange(old: File?, new: File?): Boolean =
            beforeChange(old, new, BEFORE_FILE_OPERATING_CHANGE)


    fun beforeScopeChange(old: Pair<String, String>, new: Pair<String, String>): Boolean
            = beforeChange(old, new, BEFORE_SCOPE_CHANGE)

    fun beforeLoggerLevelChange(old: Level, new: Level): Boolean
            = beforeChange(old, new, BEFORE_LOGGER_LEVEL_CHANGE)

    fun beforeCacheEnabledChange(old: Boolean, new: Boolean): Boolean
            = beforeChange(old, new, BEFORE_CACHE_ENABLED_CHANGE)

    fun beforeConcurrentOperationsChange(old: Int, new: Int): Boolean
            = beforeChange(old, new, BEFORE_CONCURRENT_OPERATIONS_CHANGE)

    fun beforeAutoConfirmChange(old: Boolean, new: Boolean): Boolean
            = beforeChange(old, new, BEFORE_AUTO_CONFIRM_CHANGE)

    fun beforePurgeCacheChange(old: Boolean, new: Boolean): Boolean
            = beforeChange(old, new, BEFORE_PURGE_CACHE_CHANGE)

    fun beforePatchOperationChange(old: PatchOperation?, new: PatchOperation?): Boolean
            = beforeChange(old, new, BEFORE_PATCH_OPERATION_CHANGE)

    fun beforePatchFileChange(old: File?, new: File?): Boolean
            = beforeChange(old, new, BEFORE_PATCH_FILE_CHANGE)

    fun beforeAttemptFingerprintChange(old: Boolean, new: Boolean): Boolean
            = beforeChange(old, new, BEFORE_ATTEMPT_FINGERPRINT_CHANGE)

    fun beforePrintExtractChange(old: Boolean, new: Boolean): Boolean
            = beforeChange(old, new, BEFORE_PRINT_EXTRACT_CHANGE)

    fun beforePrintCompileChange(old: Boolean, new: Boolean): Boolean
            = beforeChange(old, new, BEFORE_PRINT_COMPILE_CHANGE)

    fun beforeNoFluffChange(old: Boolean, new: Boolean): Boolean
            = beforeChange(old, new, BEFORE_NO_FLUFF_CHANGE)

    fun beforeMultithreadedSimpleChange(old: Boolean, new: Boolean): Boolean
            = beforeChange(old, new, BEFORE_MULTITHREADED_SIMPLE_CHANGE)

    fun beforeTableOutputChange(old: Boolean, new: Boolean): Boolean = beforeChange(old, new, BEFORE_TABLE_OUTPUT_CHANGE)

    fun afterOperatingChange(old: File?, new: File?): Unit =
            afterChange(old, new, ON_OPERATING_CHANGE)

    fun afterFileOperatingChange(old: File?, new: File?): Unit =
            afterChange(old, new, ON_FILE_OPERATING_CHANGE)

    fun afterScopeChange(old: Pair<String, String>, new: Pair<String, String>): Unit
            = afterChange(old, new, ON_SCOPE_CHANGE)

    fun afterLoggerLevelChange(old: Level, new: Level): Unit
            = afterChange(old, new, ON_LOGGER_LEVEL_CHANGE)

    fun afterCacheEnabledChange(old: Boolean, new: Boolean): Unit
            = afterChange(old, new, ON_CACHE_ENABLED_CHANGE)

    fun afterConcurrentOperationsChange(old: Int, new: Int): Unit
            = afterChange(old, new, ON_CONCURRENT_OPERATIONS_CHANGE)

    fun afterAutoConfirmChange(old: Boolean, new: Boolean): Unit
            = afterChange(old, new, ON_AUTO_CONFIRM_CHANGE)

    fun afterPurgeCacheChange(old: Boolean, new: Boolean): Unit
            = afterChange(old, new, ON_PURGE_CACHE_CHANGE)

    fun afterPatchOperationChange(old: PatchOperation?, new: PatchOperation?): Unit
            = afterChange(old, new, ON_PATCH_OPERATION_CHANGE)

    fun afterPatchFileChange(old: File?, new: File?): Unit
            = afterChange(old, new, ON_PATCH_FILE_CHANGE)

    fun afterAttemptFingerprintChange(old: Boolean, new: Boolean): Unit
            = afterChange(old, new, ON_ATTEMPT_FINGERPRINT_CHANGE)

    fun afterPrintExtractChange(old: Boolean, new: Boolean): Unit
            = afterChange(old, new, ON_PRINT_EXTRACT_CHANGE)

    fun afterPrintCompileChange(old: Boolean, new: Boolean): Unit
            = afterChange(old, new, ON_PRINT_COMPILE_CHANGE)

    fun afterNoFluffChange(old: Boolean, new: Boolean): Unit
            = afterChange(old, new, ON_NO_FLUFF_CHANGE)

    fun afterMultithreadedSimpleChange(old: Boolean, new: Boolean): Unit
            = afterChange(old, new, ON_MULTITHREADED_SIMPLE_CHANGE)

    fun afterTableOutputChange(old: Boolean, new: Boolean): Unit = afterChange(old, new, ON_TABLE_OUTPUT_CHANGE)

    fun shouldExtract(archive: IArchive, folder: File, files: List<Pair<String, () -> InputStream>>): Boolean
            = BEFORE_EXTRACT
            .filter { (plugin) -> plugin == SpiralData.BASE_PLUGIN || PluginManager.loadedPlugins.values.any { (_, _, c) -> plugin == c } }
            .fold(true) { state, (_, hook) -> hook(archive, folder, files, state) }

    fun extracting(archive: IArchive, folder: File, files: List<Pair<String, () -> InputStream>>): Unit
            = ON_EXTRACT
            .filter { (plugin) -> plugin == SpiralData.BASE_PLUGIN || PluginManager.loadedPlugins.values.any { (_, _, c) -> plugin == c } }
            .forEach { (_, hook) -> hook(archive, folder, files) }

    var prevExtractJob: Job? = null
    fun extractingFile(archive: IArchive, folder: File, files: List<Pair<String, () -> InputStream>>, extracting: Pair<String, () -> InputStream>): Unit {
        val prev: Job? = prevExtractJob
        prevExtractJob = launch(CommonPool) {
            prev?.join()

            DURING_EXTRACT
                    .filter { (plugin) -> plugin == SpiralData.BASE_PLUGIN || PluginManager.loadedPlugins.values.any { (_, _, c) -> plugin == c } }
                    .forEach { (_, hook) -> hook(archive, folder, files, extracting) }
        }
    }

    fun finishedExtraction(archive: IArchive, folder: File, files: List<Pair<String, () -> InputStream>>): Unit
            = AFTER_EXTRACT
            .filter { (plugin) -> plugin == SpiralData.BASE_PLUGIN || PluginManager.loadedPlugins.values.any { (_, _, c) -> plugin == c } }
            .forEach { (_, hook) -> hook(archive, folder, files) }

    fun <T : Any?> beforeChange(old: T, new: T, beforeChanges: BeforeHookChangeList<T>): Boolean
            = beforeChanges
            .filter { (plugin) -> plugin == SpiralData.BASE_PLUGIN || PluginManager.loadedPlugins.values.any { (_, _, c) -> plugin == c } }
            .fold(true) { state, (_, hook) -> hook(old, new, state) }

    fun <T : Any?> afterChange(old: T, new: T, afterChanges: OnHookChangeList<T>): Unit
            = afterChanges
            .filter { (plugin) -> plugin == SpiralData.BASE_PLUGIN || PluginManager.loadedPlugins.values.any { (_, _, c) -> plugin == c } }
            .forEach { (_, hook) -> hook(old, new) }
}