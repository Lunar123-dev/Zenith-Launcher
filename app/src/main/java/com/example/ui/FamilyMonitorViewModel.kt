package com.example.ui

import android.app.Application
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FamilyDeviceRepository
import com.example.data.model.DeviceRecord
import com.example.data.model.Family
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FamilyMonitorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FamilyDeviceRepository(application.applicationContext)
    private val auth: FirebaseAuth = Firebase.auth

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _familyId = MutableStateFlow("")
    val familyId: StateFlow<String> = _familyId.asStateFlow()

    private val _familyCode = MutableStateFlow("")
    val familyCode: StateFlow<String> = _familyCode.asStateFlow()

    private val _familyName = MutableStateFlow("Gia đình của tôi")
    val familyName: StateFlow<String> = _familyName.asStateFlow()

    private val _devices = MutableStateFlow<List<DeviceRecord>>(emptyList())
    val devices: StateFlow<List<DeviceRecord>> = _devices.asStateFlow()

    private val _currentDevice = MutableStateFlow<DeviceRecord?>(null)
    val currentDevice: StateFlow<DeviceRecord?> = _currentDevice.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _activeRemoteMessage = MutableStateFlow<Pair<String, String>?>(null) // sender to message
    val activeRemoteMessage: StateFlow<Pair<String, String>?> = _activeRemoteMessage.asStateFlow()

    private val _isRinging = MutableStateFlow(false)
    val isRinging: StateFlow<Boolean> = _isRinging.asStateFlow()

    private var heartbeatJob: Job? = null
    private var observeDevicesJob: Job? = null
    private var observeCurrentJob: Job? = null

    init {
        // Load SharedPreferences cached family details
        val context = application.applicationContext
        val prefs = context.getSharedPreferences("zenith_family_prefs", Context.MODE_PRIVATE)
        _familyId.value = prefs.getString("family_id", "") ?: ""
        _familyCode.value = prefs.getString("family_code", "") ?: ""
        _familyName.value = prefs.getString("family_name", "Gia đình của tôi") ?: "Gia đình của tôi"

        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
            if (firebaseAuth.currentUser != null) {
                onUserLoggedIn()
            } else {
                onUserLoggedOut()
            }
        }

        if (auth.currentUser != null) {
            onUserLoggedIn()
        }
    }

    private fun onUserLoggedIn() {
        startTelemetrySync()
        startObservingDevices()
        startObservingCurrentDevice()
    }

    private fun onUserLoggedOut() {
        heartbeatJob?.cancel()
        observeDevicesJob?.cancel()
        observeCurrentJob?.cancel()
        _devices.value = emptyList()
        _currentDevice.value = null
    }

    fun startTelemetrySync() {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            while (isActive) {
                if (auth.currentUser != null) {
                    try {
                        val localTelemetry = repository.collectLocalTelemetry(context)
                        _currentDevice.value = localTelemetry
                        repository.syncCurrentDevice(localTelemetry)
                    } catch (e: Exception) {
                        // ignore or log
                    }
                }
                delay(30_000L) // Cập nhật nhịp tim thiết bị mỗi 30 giây
            }
        }
    }

    private fun startObservingDevices() {
        observeDevicesJob?.cancel()
        observeDevicesJob = viewModelScope.launch {
            val famId = _familyId.value
            repository.observeFamilyDevices(famId)
                .catch { error ->
                    _errorMessage.value = error.localizedMessage
                }
                .collect { deviceList ->
                    _devices.value = deviceList
                }
        }
    }

    private fun startObservingCurrentDevice() {
        observeCurrentJob?.cancel()
        val context = getApplication<Application>().applicationContext
        val deviceId = repository.getOrCreateDeviceId(context)

        observeCurrentJob = viewModelScope.launch {
            repository.observeCurrentDevice(deviceId)
                .catch { /* ignore */ }
                .collect { dev ->
                    if (dev != null) {
                        _currentDevice.value = dev
                        // Kiểm tra nếu có tin nhắn từ xa gửi tới màn hình TV / điện thoại
                        if (dev.remoteMessage.isNotBlank()) {
                            _activeRemoteMessage.value = Pair(dev.remoteMessageSender.ifBlank { "Thành viên gia đình" }, dev.remoteMessage)
                        }
                        // Kiểm tra nếu có lệnh rung / chuông tìm thiết bị
                        if (dev.ringAlert && !_isRinging.value) {
                            triggerLocalRinging()
                        }
                    }
                }
        }
    }

    fun createFamily(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val context = getApplication<Application>().applicationContext
                val fam = repository.createFamily(context, name)
                _familyId.value = fam.id
                _familyCode.value = fam.code
                _familyName.value = fam.name
                startObservingDevices()
            } catch (e: Exception) {
                _errorMessage.value = "Tạo nhóm gia đình thất bại: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun joinFamily(code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val context = getApplication<Application>().applicationContext
                val success = repository.joinFamilyByCode(context, code)
                if (success) {
                    val prefs = context.getSharedPreferences("zenith_family_prefs", Context.MODE_PRIVATE)
                    _familyId.value = prefs.getString("family_id", "") ?: ""
                    _familyCode.value = prefs.getString("family_code", "") ?: ""
                    _familyName.value = prefs.getString("family_name", "Gia đình Zenith") ?: "Gia đình Zenith"
                    startObservingDevices()
                } else {
                    _errorMessage.value = "Mã gia đình không tồn tại hoặc không hợp lệ"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gia nhập gia đình thất bại: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendRemoteMessage(targetDeviceId: String, message: String) {
        viewModelScope.launch {
            try {
                val myName = auth.currentUser?.displayName ?: "Gia đình"
                repository.sendRemoteMessage(targetDeviceId, message, myName)
            } catch (e: Exception) {
                _errorMessage.value = "Không thể gửi tin nhắn: ${e.localizedMessage}"
            }
        }
    }

    fun ringTargetDevice(targetDeviceId: String) {
        viewModelScope.launch {
            try {
                repository.toggleRemoteRing(targetDeviceId, true)
            } catch (e: Exception) {
                _errorMessage.value = "Không thể phát chuông: ${e.localizedMessage}"
            }
        }
    }

    private fun triggerLocalRinging() {
        _isRinging.value = true
        val context = getApplication<Application>().applicationContext
        try {
            // Rung
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500, 200, 500), -1))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                v?.vibrate(1500)
            }
            // Chuông báo thức / thông báo
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone?.play()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun dismissRemoteMessage() {
        _activeRemoteMessage.value = null
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val deviceId = repository.getOrCreateDeviceId(context)
            try {
                repository.clearAlerts(deviceId)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun dismissRingAlert() {
        _isRinging.value = false
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val deviceId = repository.getOrCreateDeviceId(context)
            try {
                repository.clearAlerts(deviceId)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
