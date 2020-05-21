package com.zrj.png

import com.gyf.barlibrary.ImmersionBar
import com.zrj.png.utils.click
import kotlinx.android.synthetic.main.activity_setting.*
import org.jetbrains.anko.startActivity

/**
 * 设置
 * zrj 2020/5/19
 */
class SettingActivity : BaseActivity() {

    override fun attachLayoutRes() = R.layout.activity_setting

    override fun initView() {
        ImmersionBar.setTitleBar(this, toolbar)
        iv_back.click { finish() }
        ctl_lang.click { startActivity<LanguageActivity>() }
        ctl_about.click { startActivity<AboutActivity>() }
    }

    override fun initData() {

    }
}
