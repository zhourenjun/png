package com.zrj.png

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.clj.fastble.BleManager
import com.gyf.barlibrary.ImmersionBar
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import com.zrj.png.utils.LogUtil
import com.zrj.png.utils.Preference
import com.zrj.png.utils.click
import com.zrj.png.utils.toast
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity


/**
 * 主页
 * zrj 2020/5/17
 */
class MainActivity : BaseActivity() {

    private val REQUEST_CODE_OPEN_GPS = 101
    private val REQUEST_CODE_ALBUM = 102
    private val REQUEST_CODE_CROP = 103

    private var photoPath: String by Preference("photoPath", "")
    private var mac: String by Preference("mac", "")

    override fun attachLayoutRes() = R.layout.activity_main

    override fun initView() {
        ImmersionBar.setTitleBar(this, toolbar)
        iv_setting.click { startActivity<SettingActivity>() }
        iv_menu.click { startActivity<ScanActivity>() }
        ctl_album.click {
            runWithPermissions(
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

        runWithPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, options = quickPermissionsOption) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.hint)
                    .setMessage(R.string.gpsNotifyMsg)
                    .setNegativeButton(R.string.cancel) { _, _ -> finish() }
                    .setPositiveButton(R.string.setting) { _, _ ->
                        startActivityForResult(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                            REQUEST_CODE_OPEN_GPS
                        )
                    }
                    .setCancelable(false)
                    .show()
            } else {
                checkBluetoothStatus()
            }
        }
    }

    override fun initData() {
        if (photoPath.isNotEmpty()) {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.RGB_565
            val bm = BitmapFactory.decodeFile(photoPath, options)
            iv.setImageBitmap(bm)
        }
    }

    private lateinit var cropImageUri: Uri

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_CODE_OPEN_GPS -> {
                if (checkGPSIsOpen()) {
                    checkBluetoothStatus()
                }
            }

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
                photoPath = cropImageUri.path ?: ""
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

    override fun onDestroy() {
        super.onDestroy()
        BleManager.getInstance().disconnectAllDevice()
        BleManager.getInstance().destroy()
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

    private fun checkGPSIsOpen(): Boolean {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    //检查蓝牙是否支持及打开
    private fun checkBluetoothStatus() {
        // 检查设备是否支持BLE4.0
        if (!BleManager.getInstance().isSupportBle) {
            toast("该设备不支持BLE蓝牙")
            finish()
        }

        if (!BleManager.getInstance().isBlueEnable) {
            BleManager.getInstance().enableBluetooth()
        }

        if (mac.isNotEmpty()){

        }
    }
}
