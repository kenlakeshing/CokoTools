package com.yorick.cokotools.util

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.documentfile.provider.DocumentFile
import com.yorick.cokotools.cokoApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.zhanghai.android.appiconloader.AppIconLoader
import java.io.File
import java.io.IOException

object PackageUtils {

    private const val TAG = "PackageUtils"

    class AppInfo(val app: ApplicationInfo, val label: String)

    var appList by mutableStateOf(listOf<AppInfo>())
        private set

    @SuppressLint("StaticFieldLeak")
    private val iconLoader = AppIconLoader(
        cokoApplication.resources.getDimensionPixelSize(android.R.dimen.app_icon_size),
        false,
        cokoApplication
    )
    private val appIcon = mutableMapOf<String, ImageBitmap>()

    suspend fun fetchAppList() {
        withContext(Dispatchers.IO) {
            val pm = cokoApplication.packageManager
            val collection = mutableListOf<AppInfo>()
            pm.getInstalledApplications(PackageManager.GET_META_DATA).forEach {
                val label = pm.getApplicationLabel(it)
                collection.add(AppInfo(it, label.toString()))
                appIcon[it.packageName] = iconLoader.loadIcon(it).asImageBitmap()
            }

            appList = collection
        }
    }

    @Suppress("DEPRECATION")
    fun getAppInfo(packageName: String): String {
        return try {
            val pm = cokoApplication.packageManager
            val applicationInfo = pm.getApplicationInfo(
                packageName, PackageManager.MATCH_UNINSTALLED_PACKAGES
            )
            pm.getApplicationLabel(applicationInfo).toString()

        } catch (e: Exception) {
            e.printStackTrace()
            "Null"
        }
    }

    suspend fun cleanTmpApkDir() {
        withContext(Dispatchers.IO) {
            cokoApplication.tmpApkDir.listFiles()?.forEach(File::delete)
        }
    }

    suspend fun getAppInfoFromApks(apks: List<Uri>): Result<AppInfo> {
        return withContext(Dispatchers.IO) {
            runCatching {
                var primary: ApplicationInfo? = null
                val splits = apks.mapNotNull { uri ->
                    val src = DocumentFile.fromSingleUri(cokoApplication, uri)
                        ?: throw IOException("DocumentFile is null")
                    val dst = cokoApplication.tmpApkDir.resolve(src.name!!)
                    val input = cokoApplication.contentResolver.openInputStream(uri)
                        ?: throw IOException("InputStream is null")
                    input.use {
                        dst.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (primary == null) {
                        primary = cokoApplication.packageManager.getPackageArchiveInfo(
                            dst.absolutePath,
                            0
                        )?.applicationInfo
                        primary?.let {
                            it.sourceDir = dst.absolutePath
                            return@mapNotNull null
                        }
                    }
                    dst.absolutePath
                }

                // TODO: Check selected apks are from the same app
                if (primary == null) throw IllegalArgumentException("No primary apk")
                val label = cokoApplication.packageManager.getApplicationLabel(primary!!).toString()
                if (splits.isNotEmpty()) primary!!.splitSourceDirs = splits.toTypedArray()
                AppInfo(primary!!, label)
            }.recoverCatching { t ->
                cleanTmpApkDir()
                Log.e(TAG, "Failed to load apks", t)
                throw t
            }
        }
    }

}