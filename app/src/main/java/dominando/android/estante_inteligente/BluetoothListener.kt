package dominando.android.estante_inteligente

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice

interface BluetoothListener {
    companion object {
        var ACTION_DISCOVERY_STARTED = BluetoothAdapter.ACTION_DISCOVERY_STARTED
        var ACTION_FOUND = BluetoothDevice.ACTION_FOUND
        var ACTION_DISCOVERY_FINISHED = BluetoothAdapter.ACTION_DISCOVERY_FINISHED
    }

    fun action(action:String)
}