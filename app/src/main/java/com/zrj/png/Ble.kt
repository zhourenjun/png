package com.zrj.png

import android.bluetooth.BluetoothGatt
import com.clj.fastble.BleManager
import com.clj.fastble.callback.*
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.clj.fastble.utils.HexUtil
import java.util.*


fun connect(bleDevice: BleDevice) {
    BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
        override fun onStartConnect() {

        }

        override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {

        }

        override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {

        }

        override fun onDisConnected(
            isActiveDisConnected: Boolean,
            bleDevice: BleDevice,
            gatt: BluetoothGatt,
            status: Int
        ) {

        }
    })
}

fun connect(mac: String) {
    BleManager.getInstance().connect(mac, object : BleGattCallback() {
        override fun onStartConnect() {

        }

        override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {

        }

        override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
            notify(bleDevice)
        }

        override fun onDisConnected(isActiveDisConnected: Boolean, bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {

        }
    })
}


fun scan() {
    BleManager.getInstance().scan(object : BleScanCallback() {
        override fun onScanStarted(success: Boolean) {

        }

        override fun onLeScan(bleDevice: BleDevice) {

        }

        override fun onScanning(bleDevice: BleDevice) {

        }

        override fun onScanFinished(scanResultList: List<BleDevice>) {

        }
    })
}

fun setMtu(bleDevice: BleDevice, mtu: Int) {
    BleManager.getInstance().setMtu(bleDevice, mtu, object : BleMtuChangedCallback() {
        override fun onSetMTUFailure(exception: BleException) {

        }

        override fun onMtuChanged(mtu: Int) {

        }
    })
}


fun write(bleDevice: BleDevice, data: ByteArray) {
    BleManager.getInstance().write(bleDevice, "uuid_service", "uuid_characteristic_write", data, object : BleWriteCallback() {
            override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {

            }

            override fun onWriteFailure(exception: BleException) {

            }
        })
}

fun notify(bleDevice: BleDevice) {
    BleManager.getInstance().notify(bleDevice, "uuid_service", "uuid_characteristic_notify",
        object : BleNotifyCallback() {
            override fun onNotifySuccess() {}
            override fun onNotifyFailure(exception: BleException) {}
            override fun onCharacteristicChanged(data: ByteArray) {
                HexUtil.formatHexString(data, true)
            }
        })
}


fun setScanRule() {
    val scanRuleConfig = BleScanRuleConfig.Builder()
        .setServiceUuids(arrayOf(UUID.fromString("uuids")))      // 只扫描指定的服务的设备，可选
        .setDeviceName(true, "names")   // 只扫描指定广播名的设备，可选
        .setDeviceMac("mac")                  // 只扫描指定mac的设备，可选
        .setAutoConnect(true)      // 连接时的autoConnect参数，可选，默认false
        .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
        .build();
    BleManager.getInstance().initScanRule(scanRuleConfig)
}

fun cancelScan() {
    BleManager.getInstance().cancelScan()
}