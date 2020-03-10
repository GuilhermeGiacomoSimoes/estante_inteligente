package dominando.android.estante_inteligente

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.fragment.app.FragmentManager

class BluetoothHelper(private var fragmentManager: FragmentManager) {

    private var lista = ArrayList<BluetoothDevice>()
    private lateinit var dispositivo: BluetoothAdapter

    companion object {
        fun getDefaultAdapteer(): BluetoothAdapter? {
            return BluetoothAdapter.getDefaultAdapter() //RETORNA NULO SE O TELEFONE NÃO TIVER BLUETOOTH
        }

        fun verificaBluetoothHabilitado(): Boolean {
            return BluetoothAdapter.getDefaultAdapter().isEnabled // VERIFICA SE O BLUETOOTH ESTA ATIVADO
        }
    }

    fun obterDevicesPareados(fragmentManager: FragmentManager): ArrayList<BluetoothDevice>{
        val bluetooth = BluetoothHelper(fragmentManager)

        lista = ArrayList()
        bluetooth.dispositivo = BluetoothAdapter.getDefaultAdapter()

        val pairedDevices = bluetooth.dispositivo.bondedDevices

        if(pairedDevices.isNotEmpty()){
            for(device in pairedDevices)
                lista.add(device)
        }

        return lista
    }
}