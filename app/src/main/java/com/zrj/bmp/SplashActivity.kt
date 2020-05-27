package com.zrj.bmp

import android.Manifest
import android.content.DialogInterface
import android.view.Gravity
import androidx.appcompat.app.AlertDialog
import com.gyf.barlibrary.ImmersionBar
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.anko.startActivity

/**
 * 启动页
 * zrj 2020/5/16
 */
@ExperimentalCoroutinesApi
class SplashActivity : BaseActivity() {

    override fun attachLayoutRes() = R.layout.activity_splash

    override fun initData() {
    }

    override fun initView() {
        ImmersionBar.setTitleBar(this, layout_splash)
    }

    override fun onResume() {
        super.onResume()
        runWithPermissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            options = quickPermissionsOption
        ) {
            startActivity<MainActivity>()
            finish()
        }
    }

    private val quickPermissionsOption = QuickPermissionsOptions(permanentDeniedMethod = { req ->
     val dialog =   AlertDialog.Builder(this@SplashActivity)
            .setTitle(R.string.hint)
            .setMessage(R.string.permission)
            .setPositiveButton(R.string.sure) { _: DialogInterface, _: Int ->
                req.openAppSettings()
            }.setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int ->

            }.show()

        val dialogWindow = dialog.window
        val m = windowManager
        val d = m.defaultDisplay
        val p = dialogWindow?.attributes
        p?.width = (d.width * 0.95).toInt()
        p?.gravity = Gravity.CENTER
        dialogWindow?.attributes = p
    })
}
