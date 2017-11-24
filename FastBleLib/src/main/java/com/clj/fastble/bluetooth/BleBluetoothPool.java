package com.clj.fastble.bluetooth;


import com.clj.fastble.data.BleConfig;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.data.BleConnectState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BleBluetoothPool {

    private final BleLruHashMap<String, BleBluetooth> bleLruHashMap;

    public BleBluetoothPool() {
        bleLruHashMap = new BleLruHashMap<>(BleConfig.getInstance().getMaxConnectCount());
    }

    public BleBluetoothPool(int BleBluetoothSize) {
        bleLruHashMap = new BleLruHashMap<>(BleBluetoothSize);
    }

    /**
     * 添加设备镜像
     *
     * @param bleBluetooth
     */
    public synchronized void addBleBluetooth(BleBluetooth bleBluetooth) {
        if (bleBluetooth == null) {
            return;
        }
        if (!bleLruHashMap.containsKey(bleBluetooth.getDeviceKey())) {
            bleLruHashMap.put(bleBluetooth.getDeviceKey(), bleBluetooth);
        }
    }

    /**
     * 删除设备镜像
     *
     * @param bleBluetooth
     */
    public synchronized void removeBleBluetooth(BleBluetooth bleBluetooth) {
        if (bleBluetooth == null) {
            return;
        }
        if (bleLruHashMap.containsKey(bleBluetooth.getDeviceKey())) {
            bleLruHashMap.remove(bleBluetooth.getDeviceKey());
        }
    }

    /**
     * 判断是否包含设备镜像
     */
    public synchronized boolean isContainDevice(BleBluetooth bleBluetooth) {
        if (bleBluetooth == null || !bleLruHashMap.containsKey(bleBluetooth.getDeviceKey())) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否包含设备镜像
     */
    public synchronized boolean isContainDevice(BleDevice bleDevice) {
        if (bleDevice == null || !bleLruHashMap.containsKey(bleDevice.getKey())) {
            return false;
        }
        return true;
    }

    /**
     * 获取连接池中该设备镜像的连接状态，如果没有连接则返回CONNECT_DISCONNECT。
     */
    public synchronized BleConnectState getConnectState(BleDevice bleDevice) {
        BleBluetooth bleBluetooth = getBleBluetooth(bleDevice);
        if (bleBluetooth != null) {
            return bleBluetooth.getConnectState();
        }
        return BleConnectState.CONNECT_DISCONNECT;
    }

    /**
     * 获取连接池中的设备镜像，如果没有连接则返回空
     */
    public synchronized BleBluetooth getBleBluetooth(BleDevice bleDevice) {
        if (bleDevice != null) {
            if (bleLruHashMap.containsKey(bleDevice.getKey())) {
                return bleLruHashMap.get(bleDevice.getKey());
            }
        }
        return null;
    }

    /**
     * 断开连接池中某一个设备
     */
    public synchronized void disconnect(BleDevice bleDevice) {
        if (isContainDevice(bleDevice)) {
            getBleBluetooth(bleDevice).disconnect();
        }
    }

    /**
     * 断开连接池中所有设备
     */
    public synchronized void disconnect() {
        for (Map.Entry<String, BleBluetooth> stringBleBluetoothEntry : bleLruHashMap.entrySet()) {
            stringBleBluetoothEntry.getValue().disconnect();
        }
        bleLruHashMap.clear();
    }

    /**
     * 清除连接池
     */
    public synchronized void clear() {
        for (Map.Entry<String, BleBluetooth> stringBleBluetoothEntry : bleLruHashMap.entrySet()) {
            stringBleBluetoothEntry.getValue().closeBluetoothGatt();
        }
        bleLruHashMap.clear();
    }

    /**
     * 获取连接池设备镜像Map集合
     *
     * @return
     */
    public Map<String, BleBluetooth> getBleBluetoothMap() {
        return bleLruHashMap;
    }

    /**
     * 获取连接池设备镜像List集合
     *
     * @return
     */
    public synchronized List<BleBluetooth> getBleBluetoothList() {
        final List<BleBluetooth> BleBluetooths = new ArrayList<>(bleLruHashMap.values());
        Collections.sort(BleBluetooths, new Comparator<BleBluetooth>() {
            @Override
            public int compare(final BleBluetooth lhs, final BleBluetooth rhs) {
                return lhs.getDeviceKey().compareToIgnoreCase(rhs.getDeviceKey());
            }
        });
        return BleBluetooths;
    }

    /**
     * 获取连接池设备详细信息List集合
     *
     * @return
     */
    public synchronized List<BleDevice> getDeviceList() {
        final List<BleDevice> deviceList = new ArrayList<>();
        for (BleBluetooth BleBluetooth : getBleBluetoothList()) {
            if (BleBluetooth != null) {
                deviceList.add(BleBluetooth.getDevice());
            }
        }
        return deviceList;
    }

}