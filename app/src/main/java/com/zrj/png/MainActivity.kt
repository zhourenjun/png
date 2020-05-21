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
import com.clj.fastble.data.BleDevice
import com.gyf.barlibrary.ImmersionBar
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import com.zrj.png.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.startActivity
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * 主页
 * zrj 2020/5/17
 */
@ExperimentalCoroutinesApi
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

        seekBar.setOnTouchListener { _, _ -> true }

        ctl_send.click {
            if (!isConnect){
                toast(R.string.device_not_connected)
                return@click
            }
            if (photoPath.isEmpty()) {
                toast(R.string.please_select_picture)
                return@click
            }
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.RGB_565
            val bm = BitmapFactory.decodeFile(photoPath, options)
            val byteArray = BitmapConverter.convert(bm)


        }

        runWithPermissions(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            options = quickPermissionsOption
        ) {
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

    private var isConnect = false

    override fun initData() {
        if (photoPath.isNotEmpty()) {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.RGB_565
            val bm = BitmapFactory.decodeFile(photoPath, options)
            iv.setImageBitmap(bm)
        }

        receive<BleDevice>(false, "onDisConnected") {
            isConnect = false
            tv_status.text = getString(R.string.not_connected)
        }

        receive<BleDevice>(false, "onConnectSuccess") {
            isConnect = true
            tv_status.text = getString(R.string.connected)
        }

        receiveTag("onStartConnect") {
            tv_status.text = getString(R.string.connecting)
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




//                try {
//                    // 存储文件名
//                    val filename = Environment.getExternalStorageDirectory().absolutePath + "/ble.bmp"
//                    val file = File(filename)
//                    if (!file.exists()) {
//                        file.createNewFile()
//                    }
//                    val fileos = FileOutputStream(filename)
//                    fileos.write(byteArray)
//                    fileos.flush()
//                    fileos.close()
//                } catch (e: FileNotFoundException) {
//                    e.printStackTrace()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
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

        if (mac.isNotEmpty()) {
            Ble.connect(mac)
        }
    }
}
