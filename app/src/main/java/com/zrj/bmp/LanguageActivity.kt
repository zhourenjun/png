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
                iv_zh.setVisible(true)
                iv_en.setVisible(false)
            }
        }
        ctl_english.click {
            if (lang != "en") {
                iv_zh.setVisible(false)
                iv_en.setVisible(true)
            }
        }
        tv_save.click {
            lang = if (iv_zh.visibility == View.VISIBLE) "zh" else "en"
            updateLocale(if (iv_zh.visibility == View.VISIBLE) Locale.CHINESE else Locale.ENGLISH)
            val i = Intent(this, MainActivity::class.java)
            startActivity(i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    override fun initData() {
        iv_zh.setVisible(lang == "zh")
        iv_en.setVisible(lang != "zh")
    }
}
