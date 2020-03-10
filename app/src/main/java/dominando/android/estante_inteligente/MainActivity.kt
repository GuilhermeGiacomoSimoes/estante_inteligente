package dominando.android.estante_inteligente

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity(), DialogInterface.OnClickListener{

    private var UM = true
    private var DOIS = true
    private var TRES = true
    private var QUATRO = true
    private var CINCO = true
    private var SEIS = true

    private lateinit var alertDialog: AlertDialog

    private var card = 0

    private var BT_ACTIVITY = 101
    private var BT_VISIBILITY = 102

    lateinit var servie: BluetoothSerialService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(BluetoothHelper.getDefaultAdapteer() == null)
            Toast.makeText(this, "Smartphone sem adaptador bluetooth", Toast.LENGTH_SHORT).show()

        else if(!BluetoothHelper.verificaBluetoothHabilitado()){
            val it = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(it, BT_ACTIVITY)
        }

        tornarVisivel()

        DialogBluetooths.build(supportFragmentManager, listarBluetoothsPareados())

        if(UM)
            txtUm.alpha = 1f
        else
            txtUm.alpha = .5f

        if(DOIS)
            txtDois.alpha = 1f
        else
            txtDois.alpha = .5f

        if(TRES)
            txtTres.alpha = 1f
        else
            txtTres.alpha = .5f

        if(QUATRO)
            txtQuatro.alpha = 1f
        else
            txtQuatro.alpha = .5f

        if(CINCO)
            txtCinco.alpha = 1f
        else
            txtCinco.alpha = .5f

        if(SEIS)
            txtSeis.alpha = 1f
        else
            txtSeis.alpha = .5f

        this.alertDialog = criaAlertDialog()

    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when(which){
            0 -> { // COLOCAR PEÇA
                mandarMensagem("A")
                when (card) {
                    1 -> {
                        if(UM)// TEM PEÇA NA ESTANTE
                            Toast.makeText(this, "Já tem peça nessa posição da estante", Toast.LENGTH_SHORT).show()

                        else { // NÃO TEM PEÇA NA ESTANTE
                            UM = !UM
                            txtUm.alpha = 1f
                        }
                    }

                    2 -> {
                        if(DOIS)// TEM PEÇA NA ESTANTE
                            Toast.makeText(this, "Já tem peça nessa posição da estante", Toast.LENGTH_SHORT).show()

                        else { // NÃO TEM PEÇA NA ESTANTE
                            DOIS = !DOIS
                            txtDois.alpha = 1f
                        }
                    }

                    3 -> {
                        if(TRES)// TEM PEÇA NA ESTANTE
                            Toast.makeText(this, "Já tem peça nessa posição da estante", Toast.LENGTH_SHORT).show()

                        else { // NÃO TEM PEÇA NA ESTANTE
                            TRES = !TRES
                            txtTres.alpha = 1f
                        }
                    }

                    4 -> {
                        if(QUATRO)// TEM PEÇA NA ESTANTE
                            Toast.makeText(this, "Já tem peça nessa posição da estante", Toast.LENGTH_SHORT).show()

                        else { // NÃO TEM PEÇA NA ESTANTE
                            QUATRO = !QUATRO
                            txtQuatro.alpha = 1f
                        }
                    }

                    5 -> {
                        if(CINCO)// TEM PEÇA NA ESTANTE
                            Toast.makeText(this, "Já tem peça nessa posição da estante", Toast.LENGTH_SHORT).show()

                        else { // NÃO TEM PEÇA NA ESTANTE
                            CINCO = !CINCO
                            txtCinco.alpha = 1f
                        }
                    }

                    6 -> {
                        if (SEIS)// TEM PEÇA NA ESTANTE
                            Toast.makeText(
                                this,
                                "Já tem peça nessa posição da estante",
                                Toast.LENGTH_SHORT
                            ).show()
                        else { // NÃO TEM PEÇA NA ESTANTE
                            SEIS = !SEIS
                            txtSeis.alpha = 1f
                        }
                    }
                }
            }

            1 -> { // TIRAR PEÇA
                mandarMensagem("A")

                when(card){
                    1 -> {
                        if(UM){
                            UM = !UM
                            txtUm.alpha = .3f
                        }
                        else
                            Toast.makeText(this, "Não tem peça para ser tirada da estante", Toast.LENGTH_SHORT).show()
                    }

                    2->{
                        if(DOIS){
                            DOIS = !DOIS
                            txtDois.alpha = .3f
                        }
                        else
                            Toast.makeText(this, "Não tem peça para ser tirada da estante", Toast.LENGTH_SHORT).show()
                    }

                    3 -> {
                        if(TRES){
                            TRES = !TRES
                            txtTres.alpha = .3f
                        }
                        else
                            Toast.makeText(this, "Não tem peça para ser tirada da estante", Toast.LENGTH_SHORT).show()
                    }

                    4 -> {
                        if(QUATRO){
                            QUATRO = !QUATRO
                            txtQuatro.alpha = .3f
                        }
                        else
                            Toast.makeText(this, "Não tem peça para ser tirada da estante", Toast.LENGTH_SHORT).show()
                    }

                    5 -> {
                        if(CINCO){
                            CINCO = !CINCO
                            txtCinco.alpha = .3f
                        }
                        else
                            Toast.makeText(this, "Não tem peça para ser tirada da estante", Toast.LENGTH_SHORT).show()
                    }

                    6 -> {
                        if(SEIS){
                            SEIS = !SEIS
                            txtSeis.alpha = .3f
                        }
                        else
                            Toast.makeText(this, "Não tem peça para ser tirada da estante", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        alertDialog.dismiss()
    }

    private fun mandarMensagem(msg: String){
        Thread {
            try {
                servie.write(obtemByteArray(msg))

            }catch (e: Exception){
                Toast.makeText(this, "Erro ao mandar mensagem bluetooth: $e", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun obtemByteArray(string:String) : ByteArray{
        val charset = Charsets.UTF_8
        val byteArray = string.toByteArray(charset)

        return byteArray
    }

    private fun listarBluetoothsPareados(): ArrayList<BluetoothDevice> {
        val b = BluetoothHelper(supportFragmentManager)
        val l = b.obterDevicesPareados(supportFragmentManager)

        return l
    }

    private fun criaAlertDialog(): AlertDialog {
        val items = arrayOf("colocar peça", "tirar peça")

        val builder = AlertDialog.Builder(this)
        builder.setItems(items, this)

        return builder.create()
    }

    fun chamaDialog(v: View){
        when(v.id){
            R.id.txtUm -> card = 1
            R.id.txtDois -> card = 2
            R.id.txtTres -> card = 3
            R.id.txtQuatro -> card = 4
            R.id.txtCinco -> card = 5
            R.id.txtSeis -> card = 6
        }

        this.alertDialog.show()
    }

    private fun tornarVisivel(){
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120)
        startActivityForResult(discoverableIntent, BT_VISIBILITY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == BT_ACTIVITY){
            if(resultCode == Activity.RESULT_OK)
                tornarVisivel()

            else
                Toast.makeText(this, "Precisamos que seu bluetooth seja ativado", Toast.LENGTH_SHORT).show()
        }
    }

    fun vaiPraCasa(view: View){
        mandarMensagem("home")
    }
}
