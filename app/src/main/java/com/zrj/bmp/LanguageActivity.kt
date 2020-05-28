package com.zrj.bmp

import android.content.Intent
import android.view.View
import com.gyf.barlibrary.ImmersionBar
import com.zrj.bmp.utils.Preference
import com.zrj.bmp.utils.click
import com.zrj.bmp.utils.setVisible
import kotlinx.android.synthetic.main.activity_language.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

/**
 * 设置
 * zrj 2020/5/18
 */
@ExperimentalCoroutinesApi
class LanguageActivity : BaseActivity() {

    private var lang: String by Preference("language", "")

    override fun attachLayoutRes() = R.layout.activity_language

    override fun initView() {
        ImmersionBar.setTitleBar(this, toolbar)
        iv_back.click { finish() }
        ctl_chinese.click {
            if (lang != "zh") {
                lang =  "zh"
                gotoMain( Locale.CHINESE)
            }
        }
        ctl_english.click {
            if (lang != "en") {
                lang = "en"
                gotoMain(Locale.ENGLISH)
            }
        }
    }

    private fun gotoMain(locale: Locale){
        updateLocale(locale)
        val i = Intent(this, MainActivity::class.java)
        startActivity(i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override fun initData() {
        iv_zh.setVisible(lang == "zh")
        iv_en.setVisible(lang != "zh")
    }
}
