package com.zrj.bmp

import com.gyf.barlibrary.ImmersionBar
import com.zrj.bmp.utils.Preference
import com.zrj.bmp.utils.click
import com.zrj.bmp.utils.setVisible
import kotlinx.android.synthetic.main.activity_language.*
import java.util.*

/**
 * 设置
 * zrj 2020/5/18
 */
class LanguageActivity : BaseActivity() {

    private var lang: String by Preference("language", "en")

    override fun attachLayoutRes() = R.layout.activity_language

    override fun initView() {
        ImmersionBar.setTitleBar(this, toolbar)
        iv_back.click { finish() }
        ctl_chinese.click {
            if (lang != "zh") {
                updateLocale(Locale.CHINESE)
                lang = "zh"
            }
        }
        ctl_english.click {
            if (lang != "en") {
                updateLocale(Locale.ENGLISH)
                lang = "en"
            }
        }
    }

    override fun initData() {
        iv_zh.setVisible(lang == "zh")
        iv_en.setVisible(lang == "en")
    }
}
