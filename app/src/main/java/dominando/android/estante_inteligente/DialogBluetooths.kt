package dominando.android.estante_inteligente

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.dialog_bluetooths.*
import java.lang.Exception

class DialogBluetooths: DialogFragment() {

    private var listaBluetooth = arrayListOf<BluetoothDevice>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_bluetooths, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        inflarLista()
        listaBluetoothView.setOnItemClickListener{ parent, view, position, id ->
            conectar(position)
        }
    }

    private fun inflarLista(){
        listaBluetoothView.adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, nameBluetooth())
    }

    private fun conectar(position: Int){
        try {
            val device = listaBluetooth[position]

            val mHandlerBT = Handler()
            val mSerialService = BluetoothSerialService(activity!!, mHandlerBT)

            mSerialService.connect(device)

            (activity as MainActivity).servie = mSerialService

            dismiss()
        }catch (e:Exception){

        }
    }

    private fun nameBluetooth(): ArrayList<String>{
        val lista = arrayListOf<String>()

        for(b in listaBluetooth)
            lista.add(b.name)

        return lista
    }

    companion object {
        private fun getInstance() = DialogBluetooths()

        fun build(fragmentManager: FragmentManager, listaBluetooth: ArrayList<BluetoothDevice>){
            with(getInstance()){
                this.listaBluetooth = listaBluetooth
                isCancelable = false
                show(fragmentManager, "")
            }
        }
    }
}