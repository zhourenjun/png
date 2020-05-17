package com.zrj.png.utils.locale

import java.util.*

object Locales {
     val mAllLanguages = object : HashMap<String, Locale>(15) {
        init {
            put("en", Locale.ENGLISH)  //English
            put("zh", Locale.CHINESE)
            put("ar", Locale("ar")) //阿拉伯
            put("cs", Locale("cs")) //捷克
            put("de", Locale.GERMANY)  //德国
            put("es", Locale("es")) //西班牙语
            put("fi", Locale("fi")) //芬兰
            put("fr", Locale.FRANCE)   //法
            put("it", Locale.ITALY)   //意大利
            put("ja", Locale.JAPAN)   //日本語
            put("nl", Locale("nl")) //荷兰
            put("no", Locale("no")) //挪威
            put("pt", Locale("pt")) //葡萄牙
            put("ru", Locale("ru")) //俄
            put("sv", Locale("sv")) //瑞典
        }
    }

    val RTL: Set<String> by lazy { hashSetOf("ar", "dv", "fa", "ha", "he", "iw", "ji", "ps", "sd", "ug", "ur", "yi") }
}