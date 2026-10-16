package com.example.data

import androidx.test.core.app.ApplicationProvider
import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.DeviceRecord
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyDeviceRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun unauthenticatedUser_failsSyncDevice() {
        val repo = FamilyDeviceRepository(firestore)
        val dummy = DeviceRecord(
            id = "test_dev_1",
            userId = "anon",
            userEmail = "anon@test.com",
            userName = "Anon",
            familyId = "fam_1",
            familyCode = "FAM111",
            memberUids = listOf("anon"),
            deviceName = "TV Test",
            deviceModel = "TestModel",
            deviceType = "TV",
            batteryLevel = 100L,
            isCharging = true,
            ramUsedMb = 1000L,
            ramTotalMb = 4000L,
            networkType = "WIFI",
            networkName = "TestNet",
            isOnline = true
        )

        assertThrows(IllegalStateException::class.java) {
            runBlocking {
                repo.syncCurrentDevice(dummy)
            }
        }
    }

    @Test
    fun authenticatedUser_canSyncAndReadDevice() = runBlocking {
        val uid = signInTestUser("tester@example.com")
        val repo = FamilyDeviceRepository(firestore)

        val device = DeviceRecord(
            id = "dev_auth_$uid",
            userId = uid,
            userEmail = "tester@example.com",
            userName = "Tester",
            familyId = "fam_alpha",
            familyCode = "ALP999",
            memberUids = listOf(uid),
            deviceName = "Android TV Pro",
            deviceModel = "Nexus Player",
            deviceType = "TV",
            batteryLevel = 100L,
            isCharging = true,
            ramUsedMb = 1200L,
            ramTotalMb = 3000L,
            networkType = "ETHERNET",
            networkName = "LAN",
            isOnline = true
        )

        repo.syncCurrentDevice(device)

        val doc = firestore.collection("devices").document(device.id).get().await()
        assertTrue(doc.exists())
        assertEquals("Android TV Pro", doc.getString("deviceName"))
        assertEquals("TV", doc.getString("deviceType"))
    }
}
