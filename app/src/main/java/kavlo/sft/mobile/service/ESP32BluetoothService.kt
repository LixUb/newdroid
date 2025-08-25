package kavlo.sft.mobile.service

import android.bluetooth.*
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.*
import java.util.*
import com.google.gson.Gson
import kavlo.sft.mobile.data.SensorData
import kavlo.sft.mobile.data.ConnectionStatus

class ESP32BluetoothService(private val context: Context) {
    companion object {
        private const val TAG = "ESP32Service"
        private const val SERVICE_UUID = "12345678-1234-1234-1234-123456789abc"
        private const val CHARACTERISTIC_UUID = "87654321-4321-4321-4321-cba987654321"
        private const val RECONNECT_DELAY = 5000L
        private const val MAX_RECONNECT_ATTEMPTS = 5
    }
    
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private val gson = Gson()
    private var reconnectAttempts = 0
    private var isReconnecting = false
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // State flows
    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData
    
    private val _connectionStatus = MutableStateFlow(ConnectionStatus())
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, ERROR, RECONNECTING
    }
    
    init {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        
        if (bluetoothAdapter == null) {
            _errorMessage.value = "Bluetooth not supported on this device"
        }
    }
    
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d(TAG, "Connection state changed: status=$status, newState=$newState")
            
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "Connected to GATT server")
                    _connectionStatus.value = _connectionStatus.value.copy(
                        isConnected = true,
                        deviceName = gatt?.device?.name ?: "Unknown"
                    )
                    _errorMessage.value = null
                    reconnectAttempts = 0
                    isReconnecting = false
                    
                    serviceScope.launch {
                        delay(1000)
                        gatt?.discoverServices()
                    }
                }
                
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Disconnected from GATT server")
                    _connectionStatus.value = _connectionStatus.value.copy(isConnected = false)
                    
                    if (!isReconnecting && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                        scheduleReconnect()
                    } else {
                        _errorMessage.value = if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                            "Failed to reconnect after $MAX_RECONNECT_ATTEMPTS attempts"
                        } else {
                            "Disconnected from ESP32"
                        }
                    }
                }
            }
        }
        
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Log.d(TAG, "Services discovered: status=$status")
            
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt?.getService(UUID.fromString(SERVICE_UUID))
                if (service != null) {
                    val characteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
                    if (characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, true)
                        
                        val descriptor = characteristic.getDescriptor(
                            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                        )
                        descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(descriptor)
                        
                        Log.d(TAG, "Notifications enabled for characteristic")
                    } else {
                        _errorMessage.value = "Characteristic not found"
                    }
                } else {
                    _errorMessage.value = "Service not found"
                }
            } else {
                _errorMessage.value = "Service discovery failed"
            }
        }
        
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            characteristic?.let {
                val data = String(it.value)
                Log.d(TAG, "Received data: $data")
                
                try {
                    val sensorData = gson.fromJson(data, SensorData::class.java)
                    _sensorData.value = sensorData.copy(timestamp = System.currentTimeMillis())
                    _connectionStatus.value = _connectionStatus.value.copy(
                        lastUpdate = System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse sensor data: ${e.message}")
                    _errorMessage.value = "Invalid data format received"
                }
            }
        }
        
        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val signalStrength = convertRssiToPercentage(rssi)
                _connectionStatus.value = _connectionStatus.value.copy(
                    signalStrength = signalStrength
                )
            }
        }
    }
    
    private fun convertRssiToPercentage(rssi: Int): Int {
        return when {
            rssi >= -50 -> 100
            rssi >= -60 -> 80
            rssi >= -70 -> 60
            rssi >= -80 -> 40
            rssi >= -90 -> 20
            else -> 0
        }
    }
    
    private fun scheduleReconnect() {
        if (isReconnecting) return
        
        isReconnecting = true
        reconnectAttempts++
        
        serviceScope.launch {
            delay(RECONNECT_DELAY)
            if (reconnectAttempts <= MAX_RECONNECT_ATTEMPTS) {
                bluetoothGatt?.connect()
            } else {
                isReconnecting = false
                _errorMessage.value = "Unable to reconnect to ESP32"
            }
        }
    }
    
    fun connectToDevice(deviceAddress: String): Boolean {
        if (bluetoothAdapter == null) {
            _errorMessage.value = "Bluetooth adapter not available"
            return false
        }
        
        try {
            _errorMessage.value = null
            reconnectAttempts = 0
            isReconnecting = false
            
            val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
            bluetoothGatt = device?.connectGatt(context, false, gattCallback)
            
            Log.d(TAG, "Attempting to connect to $deviceAddress")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect: ${e.message}")
            _errorMessage.value = "Connection failed: ${e.message}"
            return false
        }
    }
    
    fun disconnect() {
        isReconnecting = false
        reconnectAttempts = MAX_RECONNECT_ATTEMPTS
        
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        
        _connectionStatus.value = ConnectionStatus()
        _errorMessage.value = null
    }
    
    fun cleanup() {
        serviceScope.cancel()
        disconnect()
    }
}