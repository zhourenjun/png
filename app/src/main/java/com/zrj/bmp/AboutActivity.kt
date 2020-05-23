package com.zrj.bmp

import android.annotation.SuppressLint
import com.google.zxing.BarcodeFormat
import com.gyf.barlibrary.ImmersionBar
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.zrj.bmp.utils.click
import kotlinx.android.synthetic.main.activity_about.*

/**
 * 关于我们
 * zrj 2020/5/19
 */
class AboutActivity : BaseActivity() {

    override fun attachLayoutRes() = R.layout.activity_about

    override fun initView() {
        ImmersionBar.setTitleBar(this, toolbar)
        iv_back.click { finish() }
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap("content", BarcodeFormat.QR_CODE, 400, 400)
            iv_qr_code.setImageBitmap(bitmap)
        } catch (e: Exception) {
        }

        tv_version.text = "V${packageManager.getPackageInfo(packageName, 0).versionName}"
    }

}
