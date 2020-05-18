package com.zrj.png

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import com.gyf.barlibrary.ImmersionBar
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import com.zrj.png.utils.LogUtil
import com.zrj.png.utils.click
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


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
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
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

    private lateinit var cropImageUri: Uri

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_CODE_ALBUM -> {
                cropImageUri =
                    Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().path + "/" + "${System.currentTimeMillis()}.png")

                val intent = Intent("com.android.camera.action.CROP")
                intent.setDataAndType(data?.data, "image/*")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri)
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
                LogUtil.e(cropImageUri.path.toString())
                LogUtil.e("${File(cropImageUri.path).length() / 1024}kb")

                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.RGB_565
                val bm = BitmapFactory.decodeFile(cropImageUri.path.toString(), options)
                iv.setImageBitmap(bm)
//                val fd = contentResolver.openFileDescriptor(cropImageUri, "r")
//                if (fd != null) {
//                    val bitmap = BitmapFactory.decodeFileDescriptor(fd.fileDescriptor)
//                	fd.close()
//                    iv.setImageBitmap(bitmap)
//                }
//                val path = "file://" + "/" + Environment.getExternalStorageDirectory().path + "/" + "compress.png"

//                val cropFile =  File(Environment.getExternalStorageDirectory(), "compress.png")
//                try {
//                    if (cropFile.exists()) {
//                        cropFile.delete()
//                    }
//                    cropFile.createNewFile()
//                } catch ( e: IOException) {
//                    e.printStackTrace()
//                }
//                val os = FileOutputStream(cropFile)
//                bm.compress(Bitmap.CompressFormat.PNG, 100, os)
//                LogUtil.e("${cropFile.length() / 1024}kb")
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
