package com.movtery.pojavzh.feature.mod.translate

import com.movtery.pojavzh.feature.log.Logging
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.Tools
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

abstract class TranslateManager(private val classify: TranslateClassify) {
    private val infos: MutableList<TranslateInfoItem> = ArrayList()

    init {
        runCatching {
            val context = PojavApplication.getContext()
            CheckTranslate.check(context, classify, object : CheckTranslate.CheckListener {
                override fun onEnd(infoFile: File?) {
                    var input: InputStream? = null
                    infoFile?.let {
                        input = FileInputStream(infoFile)
                        Logging.i("${classify.name} Translate Manager", "The local cache database is successfully read!")
                    }
                    Utils.identificationData(input ?: context.assets.open(classify.fileName), infos)
                }
            })
        }.getOrElse { e ->
            Logging.e("${classify.name} Translate Manager", Tools.printToString(e))
        }
    }

    /**
     * 通过原始名称得到一个完全匹配的中文翻译名
     */
    fun searchToChinese(origin: String): String? {
        if (infos.isEmpty()) return null
        return Utils.searchBestMatch(infos, origin, matchBy = { it.originName }, returnBy = { it.chineseName }, true)
    }

    /**
     * 通过中文名称得到一个匹配度最高的原始名
     */
    fun searchToOrigin(chinese: String): String? {
        if (infos.isEmpty()) return null
        return Utils.searchBestMatch(infos, chinese, matchBy = { it.chineseName }, returnBy = { it.originName })
    }
}