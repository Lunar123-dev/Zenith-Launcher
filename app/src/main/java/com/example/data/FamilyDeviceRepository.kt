package com.example.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import com.example.R
import com.example.data.model.DeviceRecord
import com.example.data.model.Family
import com.example.data.model.OperationType
import com.example.data.model.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.random.Random

class FamilyDeviceRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun getOrCreateDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences("zenith_device_prefs", Context.MODE_PRIVATE)
        var deviceId = prefs.getString("unique_device_id", null)
        if (deviceId.isNullOrBlank()) {
            deviceId = "dev_" + UUID.randomUUID().toString().replace("-", "").take(12)
            prefs.edit().putString("unique_device_id", deviceId).apply()
        }
        return deviceId
    }

    fun collectLocalTelemetry(context: Context, customDeviceName: String? = null): DeviceRecord {
        val deviceId = getOrCreateDeviceId(context)
        val uid = auth.currentUser?.uid ?: ""
        val email = auth.currentUser?.email ?: ""
        val userName = auth.currentUser?.displayName ?: "Người dùng"

        // Battery
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toLong() else 100L
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        // Memory
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val totalMb = memInfo.totalMem / (1024 * 1024)
        val availMb = memInfo.availMem / (1024 * 1024)
        val usedMb = (totalMb - availMb).coerceAtLeast(0)

        // Network
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(activeNetwork)
        val (netType, netName) = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "ETHERNET" to "Mạng dây LAN"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WIFI" to "Wi-Fi Gia Đình"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "CELLULAR" to "Dữ liệu di động"
            else -> "NONE" to "Ngoại tuyến"
        }

        // Device Model & Type
        val model = "${Build.MANUFACTURER} ${Build.MODEL}".trim()
        val isTv = context.packageManager.hasSystemFeature("android.software.leanback")
        val isTablet = context.resources.configuration.smallestScreenWidthDp >= 600
        val deviceType = when {
            isTv -> "TV"
            isTablet -> "TABLET"
            else -> "PHONE"
        }

        val name = customDeviceName?.ifBlank { null } ?: when (deviceType) {
            "TV" -> "Android TV ($model)"
            "TABLET" -> "Tablet ($model)"
            else -> "Điện thoại ($model)"
        }

        val prefs = context.getSharedPreferences("zenith_family_prefs", Context.MODE_PRIVATE)
        val familyId = prefs.getString("family_id", "") ?: ""
        val familyCode = prefs.getString("family_code", "") ?: ""
        val memberUids = listOfNotNull(uid.ifBlank { null })

        return DeviceRecord(
            id = deviceId,
            userId = uid,
            userEmail = email,
            userName = userName,
            familyId = familyId,
            familyCode = familyCode,
            memberUids = memberUids,
            deviceName = name,
            deviceModel = model,
            deviceType = deviceType,
            batteryLevel = batteryPct,
            isCharging = isCharging,
            ramUsedMb = usedMb,
            ramTotalMb = totalMb,
            networkType = netType,
            networkName = netName,
            isOnline = true,
            remoteMessage = "",
            remoteMessageSender = "",
            ringAlert = false
        )
    }

    suspend fun syncCurrentDevice(device: DeviceRecord) {
        val uid = requireUserId()
        val docRef = db.collection("devices").document(device.id)
        try {
            val existing = docRef.get().await()
            val existingMemberUids = existing.get("memberUids") as? List<*>
            val members = (existingMemberUids?.mapNotNull { it?.toString() } ?: device.memberUids).toMutableSet()
            members.add(uid)

            val payload = mutableMapOf<String, Any>(
                "id" to device.id,
                "userId" to uid,
                "userEmail" to device.userEmail,
                "userName" to device.userName,
                "familyId" to (existing.getString("familyId") ?: device.familyId),
                "familyCode" to (existing.getString("familyCode") ?: device.familyCode),
                "memberUids" to members.toList(),
                "deviceName" to (existing.getString("deviceName") ?: device.deviceName),
                "deviceModel" to device.deviceModel,
                "deviceType" to device.deviceType,
                "batteryLevel" to device.batteryLevel,
                "isCharging" to device.isCharging,
                "ramUsedMb" to device.ramUsedMb,
                "ramTotalMb" to device.ramTotalMb,
                "networkType" to device.networkType,
                "networkName" to device.networkName,
                "isOnline" to true,
                "remoteMessage" to (existing.getString("remoteMessage") ?: ""),
                "remoteMessageSender" to (existing.getString("remoteMessageSender") ?: ""),
                "ringAlert" to (existing.getBoolean("ringAlert") ?: false),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (!existing.exists() || existing.getTimestamp("createdAt") == null) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            } else {
                payload["createdAt"] = existing.getTimestamp("createdAt")!!
            }

            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    fun observeFamilyDevices(familyId: String): Flow<List<DeviceRecord>> = flow {
        val uid = requireUserId()
        val query = if (familyId.isNotBlank()) {
            db.collection("devices").whereEqualTo("familyId", familyId)
        } else {
            db.collection("devices").whereEqualTo("userId", uid)
        }

        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(DeviceRecord::class.java)
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, "devices")
                    }
                    throw error
                }
        )
    }

    fun observeCurrentDevice(deviceId: String): Flow<DeviceRecord?> = flow {
        val docRef = db.collection("devices").document(deviceId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) snapshot.toObject(DeviceRecord::class.java) else null
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun createFamily(context: Context, familyName: String): Family {
        val uid = requireUserId()
        val email = auth.currentUser?.email ?: ""
        val familyId = "fam_" + UUID.randomUUID().toString().replace("-", "").take(10)
        val code = "ZEN" + Random.nextInt(1000, 9999).toString()

        val family = Family(
            id = familyId,
            name = familyName.ifBlank { "Gia đình Zenith" },
            code = code,
            ownerId = uid,
            ownerEmail = email,
            memberUids = listOf(uid)
        )

        val docRef = db.collection("families").document(familyId)
        val payload = mapOf(
            "id" to family.id,
            "name" to family.name,
            "code" to family.code,
            "ownerId" to family.ownerId,
            "ownerEmail" to family.ownerEmail,
            "memberUids" to family.memberUids,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        docRef.set(payload).await()

        // Lưu vào SharedPreferences
        context.getSharedPreferences("zenith_family_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("family_id", familyId)
            .putString("family_code", code)
            .putString("family_name", family.name)
            .apply()

        // Cập nhật lại device hiện tại với familyId và memberUids
        val deviceId = getOrCreateDeviceId(context)
        val deviceDoc = db.collection("devices").document(deviceId)
        val devSnap = deviceDoc.get().await()
        if (devSnap.exists()) {
            deviceDoc.update(
                mapOf(
                    "familyId" to familyId,
                    "familyCode" to code,
                    "memberUids" to listOf(uid),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        return family
    }

    suspend fun joinFamilyByCode(context: Context, code: String): Boolean {
        val uid = requireUserId()
        val normalizedCode = code.trim().uppercase()
        val querySnap = db.collection("families")
            .whereEqualTo("code", normalizedCode)
            .get()
            .await()

        if (querySnap.isEmpty) {
            return false
        }

        val familyDoc = querySnap.documents.first()
        val familyId = familyDoc.id
        val familyName = familyDoc.getString("name") ?: "Gia đình Zenith"
        val existingMembers = (familyDoc.get("memberUids") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        val updatedMembers = (existingMembers + uid).distinct()

        familyDoc.reference.update(
            mapOf(
                "memberUids" to updatedMembers,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        // Cập nhật SharedPreferences
        context.getSharedPreferences("zenith_family_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("family_id", familyId)
            .putString("family_code", normalizedCode)
            .putString("family_name", familyName)
            .apply()

        // Cập nhật device của người dùng
        val deviceId = getOrCreateDeviceId(context)
        val deviceDoc = db.collection("devices").document(deviceId)
        val devSnap = deviceDoc.get().await()
        if (devSnap.exists()) {
            deviceDoc.update(
                mapOf(
                    "familyId" to familyId,
                    "familyCode" to normalizedCode,
                    "memberUids" to updatedMembers,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        return true
    }

    suspend fun sendRemoteMessage(targetDeviceId: String, message: String, senderName: String) {
        requireUserId()
        val docRef = db.collection("devices").document(targetDeviceId)
        docRef.update(
            mapOf(
                "remoteMessage" to message,
                "remoteMessageSender" to senderName,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun toggleRemoteRing(targetDeviceId: String, ring: Boolean) {
        requireUserId()
        val docRef = db.collection("devices").document(targetDeviceId)
        docRef.update(
            mapOf(
                "ringAlert" to ring,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun clearAlerts(deviceId: String) {
        requireUserId()
        val docRef = db.collection("devices").document(deviceId)
        docRef.update(
            mapOf(
                "remoteMessage" to "",
                "remoteMessageSender" to "",
                "ringAlert" to false,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }
}
