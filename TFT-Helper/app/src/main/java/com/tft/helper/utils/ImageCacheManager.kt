package com.tft.helper.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.security.MessageDigest

/**
 * 图片缓存管理器
 * 
 * 负责将网络图片下载并缓存到本地存储
 * 避免APK体积过大，同时支持离线查看图片
 */
class ImageCacheManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var instance: ImageCacheManager? = null

        fun getInstance(context: Context): ImageCacheManager {
            return instance ?: synchronized(this) {
                instance ?: ImageCacheManager(context.applicationContext).also { instance = it }
            }
        }

        // 英雄图片文件名前缀
        const val HERO_IMAGE_PREFIX = "hero_"
        // 阵容图片文件名前缀
        const val COMP_IMAGE_PREFIX = "comp_"
        // 图片格式
        const val IMAGE_FORMAT = ".png"
    }

    // 缓存目录
    private val cacheDir: File by lazy {
        File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "tft_cache").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * 获取英雄图片本地路径
     */
    fun getHeroImagePath(heroName: String): String? {
        val fileName = "$HERO_IMAGE_PREFIX${md5(heroName)}$IMAGE_FORMAT"
        val file = File(cacheDir, fileName)
        return if (file.exists()) file.absolutePath else null
    }

    /**
     * 获取阵容图片本地路径
     */
    fun getCompImagePath(compId: String): String? {
        val fileName = "$COMP_IMAGE_PREFIX${compId}$IMAGE_FORMAT"
        val file = File(cacheDir, fileName)
        return if (file.exists()) file.absolutePath else null
    }

    /**
     * 下载英雄图片到本地
     */
    suspend fun downloadHeroImage(heroName: String, imageUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "$HERO_IMAGE_PREFIX${md5(heroName)}$IMAGE_FORMAT"
            val file = File(cacheDir, fileName)
            
            // 如果已存在，直接返回路径
            if (file.exists()) {
                return@withContext file.absolutePath
            }

            // 使用Glide下载图片
            val bitmap = Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .submit()
                .get()

            // 保存到本地
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 下载阵容图片到本地
     */
    suspend fun downloadCompImage(compId: String, imageUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "$COMP_IMAGE_PREFIX${compId}$IMAGE_FORMAT"
            val file = File(cacheDir, fileName)
            
            if (file.exists()) {
                return@withContext file.absolutePath
            }

            val bitmap = Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .submit()
                .get()

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 预下载所有阵容和英雄图片
     */
    suspend fun preloadImages(comps: List<com.tft.helper.model.Comp>) = withContext(Dispatchers.IO) {
        comps.forEach { comp ->
            // 下载阵容图片
            comp.imageUrl?.let { url ->
                downloadCompImage(comp.id, url)
            }
            
            // 下载英雄图片
            comp.coreHeroes.forEach { compHero ->
                compHero.hero.imageUrl?.let { url ->
                    downloadHeroImage(compHero.hero.name, url)
                }
            }
        }
    }

    /**
     * 获取缓存大小（字节）
     */
    fun getCacheSize(): Long {
        return cacheDir.listFiles()?.sumOf { it.length() } ?: 0
    }

    /**
     * 清除所有缓存
     */
    fun clearCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }

    /**
     * MD5加密（用于文件名）
     */
    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
