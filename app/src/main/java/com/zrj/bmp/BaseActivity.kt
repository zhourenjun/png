package com.zrj.bmp

import android.os.Bundle
import com.gyf.barlibrary.ImmersionBar
import com.zrj.bmp.utils.locale.LocaleAwareCompatActivity

abstract class BaseActivity : LocaleAwareCompatActivity() {
    protected abstract fun attachLayoutRes(): Int
    abstract fun initView()
    abstract fun initData()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(attachLayoutRes())
        //初始化沉浸式
        initImmersionBar()
        initView()
        initData()
    }

    open fun initImmersionBar() {
        ImmersionBar.with(this).navigationBarColor(R.color.colorPrimary).init()
    }

    override fun onDestroy() {
        super.onDestroy()
        ImmersionBar.with(this).destroy()
    }
}