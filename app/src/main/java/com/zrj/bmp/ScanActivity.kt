package com.zrj.bmp

import android.bluetooth.BluetoothDevice
import android.widget.ProgressBar
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.clj.fastble.data.BleDevice
import com.gyf.barlibrary.ImmersionBar
import com.zrj.bmp.utils.*
import kotlinx.android.synthetic.main.activity_scan.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.textColor

/**
 * 扫描
 * zrj 2020/5/21
 */
@ExperimentalCoroutinesApi
class ScanActivity : BaseActivity() {

    override fun attachLayoutRes() = R.layout.activity_scan

    private val deviceAdapter: DeviceAdapter by lazy { DeviceAdapter() }
    private var mac: String by Preference("mac", "")
    private var scan = false

    override fun initView() {
        ImmersionBar.setTitleBar(this, toolbar)
        iv_back.click { finish() }

        tv.click {
            if (scan) {
                tv.text = getString(R.string.scan)
                Ble.cancelScan()
            } else {
                tv.text = getString(R.string.stop)
                Ble.scan()
            }
            scan = !scan
            refresh.isRefreshing = scan
        }

        refresh.onRefresh { refresh.isRefreshing = false }

        deviceAdapter.apply {
            bindToRecyclerView(rv)
            setOnItemChildClickListener { _, _, position ->
                val device = this.data[position]
                device.isConnecting = true
                notifyItemChanged(position)
                if (device.connected) {
                    mac = ""
                    Ble.disconnect(device)
                } else {
                    mac = device.mac
                    Ble.connect(device)
                }
            }
        }
    }

    override fun initData() {

        receiveTag("onScanFinished") {
            refresh.isRefreshing = false
            tv.text = getString(R.string.scan)
            if (deviceAdapter.data.isEmpty()) {
                toast(R.string.no_device)
            }
        }

        receive<BleDevice>(true, "onScanning") {
            val device = deviceAdapter.data.find { myBleDevice -> myBleDevice.device.address == it.mac }
            if (device == null) {
                deviceAdapter.addData(MyBleDevice(it.device))
            }
        }

        receive<BleDevice>(true, "onDisConnected") {
            deviceAdapter.data.forEachWithIndex { i, myBleDevice ->
                if (myBleDevice.mac == it.device.address) {
                    myBleDevice.connected = false
                    myBleDevice.isConnecting = false
                    deviceAdapter.notifyItemChanged(i)
                    return@forEachWithIndex
                }
            }
        }

        receive<BleDevice>(true, "onConnectSuccess") {
            deviceAdapter.data.forEachWithIndex { i, myBleDevice ->
                if (myBleDevice.mac == it.device.address) {
                    myBleDevice.connected = true
                    myBleDevice.isConnecting = false
                    deviceAdapter.notifyItemChanged(i)
                    return@forEachWithIndex
                }
            }
        }
    }
}

class MyBleDevice(device: BluetoothDevice) : BleDevice(device) {
    var connected = false
    var isConnecting = false
}


class DeviceAdapter : BaseQuickAdapter<MyBleDevice, BaseViewHolder>(R.layout.item_device_list) {

    override fun convert(helper: BaseViewHolder, item: MyBleDevice) {
        helper.setText(R.id.tv_name, item.name)
            .addOnClickListener(R.id.tv_connect)

        val progress = helper.getView<ProgressBar>(R.id.progress)
        progress.setVisible(item.isConnecting)

        val tvConnect = helper.getView<TextView>(R.id.tv_connect)
        tvConnect.setVisible(!item.isConnecting)
        tvConnect.text = mContext.getString(if (item.connected) R.string.connected else R.string.not_connected)
        tvConnect.textColor = mContext.colorCompat(if (item.connected) R.color.white else R.color.tv_b2b2b2)
        tvConnect.background = mContext.drawable(if (item.connected) R.drawable.bg_red_radius_gradient2 else R.drawable.bg_gray_line2)
    }
}