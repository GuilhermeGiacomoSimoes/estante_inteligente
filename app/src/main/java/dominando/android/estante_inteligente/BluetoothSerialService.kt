package dominando.android.estante_inteligente

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Method
import java.util.*

class BluetoothSerialService(context: Context, handler: Handler) {
    private val TAG = "BluetoothReadService"
    private val D = true

    private val SerialPortServiceClass_UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var mAdapter: BluetoothAdapter
    private var mHandler: Handler = handler
    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null
    private var mState: Int = 0

    private var mAllowInsecureConnections: Boolean = false

    private var mContext: Context = context

    private val STATE_NONE = 0
    private val STATE_CONNECTING = 2
    private val STATE_CONNECTED = 3

    init {
        mAdapter = BluetoothAdapter.getDefaultAdapter()
        mState = STATE_NONE
        mAllowInsecureConnections = true
    }

    @Synchronized
    private fun setState(state: Int) {
        if (D) Log.d(TAG, "setState() $mState -> $state")
        mState = state

        mHandler.obtainMessage(0, state, -1).sendToTarget()
    }

    @Synchronized
    fun getState(): Int {
        return mState
    }

    @Synchronized
    fun start() {
        if (D) Log.d(TAG, "start")

        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        setState(STATE_NONE)
    }

    @Synchronized
    fun connect(device: BluetoothDevice) {
        if (D) Log.d(TAG, "connect to: $device")

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread!!.cancel()
                mConnectThread = null
            }
        }

        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        mConnectThread = ConnectThread(device)
        mConnectThread!!.start()
        setState(STATE_CONNECTING)
    }

    @Synchronized
    fun connected(socket: BluetoothSocket, device: BluetoothDevice) {
        if (D) Log.d(TAG, "connected")

        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        mConnectedThread = ConnectedThread(socket)
        mConnectedThread!!.start()

        val msg = mHandler.obtainMessage(0)
        val bundle = Bundle()
        bundle.putString("", device.name)
        msg.data = bundle
        mHandler.sendMessage(msg)

        setState(STATE_CONNECTED)
    }

    @Synchronized
    fun stop() {
        if (D) Log.d(TAG, "stop")


        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        setState(STATE_NONE)
    }

    fun write(out: ByteArray) {
        val r: ConnectedThread?
        synchronized(this) {
            if (mState != STATE_CONNECTED)
                return

            r = mConnectedThread
        }

        r!!.write(out)
    }

    private fun connectionFailed() {
        setState(STATE_NONE)

        val msg = mHandler.obtainMessage(0)
        val bundle = Bundle()
        bundle.putString("", "Impossível se conectar")
        msg.data = bundle
        mHandler.sendMessage(msg)
    }

    private fun connectionLost() {
        setState(STATE_NONE)

        val msg = mHandler.obtainMessage(0)
        val bundle = Bundle()
        bundle.putString("", "Conectado com sucesso")
        msg.data = bundle
        mHandler.sendMessage(msg)
    }

    private inner class ConnectThread(private val mmDevice: BluetoothDevice) : Thread() {
        private val mmSocket: BluetoothSocket?

        init {
            var tmp: BluetoothSocket? = null

            try {
                if (mAllowInsecureConnections) {
                    val method: Method

                    method = mmDevice.javaClass.getMethod(
                        "createRfcommSocket",
                        *arrayOf<Class<*>>(Int::class.javaPrimitiveType!!)
                    )
                    tmp = method.invoke(mmDevice, 1) as BluetoothSocket
                } else {
                    tmp = mmDevice.createRfcommSocketToServiceRecord(SerialPortServiceClass_UUID)
                }
            } catch (e: Exception) {
                Log.e(TAG, "create() failed", e)
            }

            mmSocket = tmp
        }

        override fun run() {
            Log.i(TAG, "BEGIN mConnectThread")
            name = "ConnectThread"

            mAdapter.cancelDiscovery()

            try {
                mmSocket!!.connect()
            } catch (e: IOException) {
                connectionFailed()
                try {
                    mmSocket!!.close()
                } catch (e2: IOException) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2)
                }

                return
            }

            synchronized(this@BluetoothSerialService) {
                mConnectThread = null
            }

            connected(mmSocket, mmDevice)
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }

        }
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?


        init {
            Log.d(TAG, "create ConnectedThread")
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            try {
                tmpIn = mmSocket.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {
                Log.e(TAG, "temp sockets not created", e)
            }

            mmInStream = tmpIn
            mmOutStream = tmpOut
        }

        override fun run() {
            Log.i(TAG, "BEGIN mConnectedThread")
            val buffer = ByteArray(1024)
            var bytes: Int

            while (true) {
                try {
                    bytes = mmInStream!!.read(buffer)

                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                    connectionLost()
                    break
                }

            }
        }

        fun write(buffer: ByteArray) {
            try {
                mmOutStream!!.write(buffer)

                mHandler.obtainMessage(0, buffer.size, -1, buffer)
                    .sendToTarget()
            } catch (e: IOException) {
                Log.e(TAG, "Exception during write", e)
            }

        }

        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }

        }
    }
}