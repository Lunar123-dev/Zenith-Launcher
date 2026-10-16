package com.example.data.model

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import org.json.JSONArray
import org.json.JSONObject

@IgnoreExtraProperties
data class Family(
    @DocumentId val id: String = "",
    val name: String = "",
    val code: String = "",
    val ownerId: String = "",
    val ownerEmail: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class DeviceRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val familyId: String = "",
    val familyCode: String = "",
    val memberUids: List<String> = emptyList(),
    val deviceName: String = "",
    val deviceModel: String = "",
    val deviceType: String = "PHONE", // "TV", "PHONE", "TABLET"
    val batteryLevel: Long = 100L,
    val isCharging: Boolean = false,
    val ramUsedMb: Long = 0L,
    val ramTotalMb: Long = 0L,
    val networkType: String = "WIFI",
    val networkName: String = "",
    val isOnline: Boolean = true,
    val remoteMessage: String = "",
    val remoteMessageSender: String = "",
    val ringAlert: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String?): String {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", exception.message ?: exception.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}
