package com.zrj.png

import android.bluetooth.BluetoothDevice
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.clj.fastble.data.BleDevice
import com.gyf.barlibrary.ImmersionBar
import com.zrj.png.utils.*
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
                if (device.isConnect) {
                    Ble.disconnect(device)
                } else {
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
            deviceAdapter.addData(MyBleDevice(it.device))
        }

        receive<BleDevice>(true, "onDisConnected") {
            deviceAdapter.data.forEachWithIndex { i, myBleDevice ->
                if (myBleDevice.mac == it.device.address) {
                    myBleDevice.isConnect = false
                    deviceAdapter.notifyItemChanged(i)
                    return@forEachWithIndex
                }
            }
        }

        receive<BleDevice>(true, "onConnectSuccess") {
            mac = it.mac
            deviceAdapter.data.forEachWithIndex { i, myBleDevice ->
                if (myBleDevice.mac == it.device.address) {
                    myBleDevice.isConnect = true
                    deviceAdapter.notifyItemChanged(i)
                    return@forEachWithIndex
                }
            }
        }
    }
}

class MyBleDevice(device: BluetoothDevice) : BleDevice(device) {
    var isConnect = false
}


class DeviceAdapter : BaseQuickAdapter<MyBleDevice, BaseViewHolder>(R.layout.item_device_list) {

    override fun convert(helper: BaseViewHolder, item: MyBleDevice) {
        helper.setText(R.id.tv_name, item.name)
            .addOnClickListener(R.id.tv_connect)
        val tvConnect = helper.getView<TextView>(R.id.tv_connect)
        tvConnect.text =  mContext.getString(if (item.isConnect) R.string.connected else R.string.not_connected)
        tvConnect.textColor = mContext.colorCompat(if (item.isConnect) R.color.white else R.color.bg_gray)
        tvConnect.background = mContext.drawable(if (item.isConnect) R.drawable.bg_red_radius_gradient2 else R.drawable.bg_gray_line2)
    }
}