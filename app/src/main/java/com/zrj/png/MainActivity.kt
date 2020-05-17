package com.zrj.png

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.gyf.barlibrary.ImmersionBar
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import com.zrj.png.utils.UriUtil
import com.zrj.png.utils.click
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import java.io.File


/**
 * 主页
 * zrj 2020/5/17
 */
class MainActivity : BaseActivity() {

    private val REQUEST_CODE_ALBUM = 102

    private val REQUEST_CODE_CROP = 103


    override fun attachLayoutRes() = R.layout.activity_main

    override fun initView() {
        ImmersionBar.setTitleBar(this, toolbar)
        iv_setting.click { startActivity<SettingActivity>() }
        iv_menu.click { startActivity<ScanActivity>() }
        ctl_album.click {
            runWithPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                options = quickPermissionsOption
            ) {
                val intent = Intent()
                intent.action = Intent.ACTION_GET_CONTENT
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_CODE_ALBUM)
            }
        }
        ctl_send.click {

        }
    }

    override fun initData() {

    }

    private var path = ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_CODE_ALBUM -> {
                path = File(
                    Environment.getExternalStorageDirectory(),
                    "${System.currentTimeMillis()}.png"
                ).path
                val intent = Intent("com.android.camera.action.CROP")
                intent.setDataAndType(
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        data?.data
                    } else {
                        FileProvider.getUriForFile(
                            this,
                            "com.zrj.png.fileProvider",
                            File(UriUtil.getPath(data?.data)?:"")
                        )
                    }, "image/*"
                )
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, path)
                intent.putExtra("crop", "true")
                intent.putExtra("aspectX", 1)
                intent.putExtra("aspectY", 1)
                intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG)
                intent.putExtra("outputX", 240)
                intent.putExtra("outputY", 240)
                intent.putExtra("scale", true)
                intent.putExtra("scaleUpIfNeeded", true)
                intent.putExtra("return-data", false)
                startActivityForResult(intent, REQUEST_CODE_CROP)
            }
            REQUEST_CODE_CROP -> {
                val cropBitmap = BitmapFactory.decodeFile(path)
                iv.setImageBitmap(cropBitmap)
            }
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private val quickPermissionsOption = QuickPermissionsOptions(permanentDeniedMethod = { req ->
        AlertDialog.Builder(this@MainActivity)
            .setTitle(R.string.hint)
            .setMessage(R.string.permission)
            .setPositiveButton(R.string.sure) { _: DialogInterface, _: Int ->
                req.openAppSettings()
            }.setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int ->

            }.show()
    })
}
