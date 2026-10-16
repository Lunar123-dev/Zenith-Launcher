const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read devices", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("devices").get());
});

test("Unauthenticated user: cannot read families", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("families").get());
});

test("Authenticated user: cannot read another user's isolated device", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("devices").doc("bob_device").set({
      id: "bob_device",
      userId: BOB_UID,
      userEmail: "bob@example.com",
      userName: "Bob",
      familyId: "bob_family",
      familyCode: "BOB123",
      memberUids: [BOB_UID],
      deviceName: "Bob Phone",
      deviceModel: "Pixel 8",
      deviceType: "PHONE",
      batteryLevel: 80,
      isCharging: false,
      ramUsedMb: 2048,
      ramTotalMb: 8192,
      networkType: "WIFI",
      networkName: "BobWifi",
      isOnline: true,
      remoteMessage: "",
      remoteMessageSender: "",
      ringAlert: false,
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("devices").doc("bob_device").get());
});

test("Authenticated family member: CAN read device in same family", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("devices").doc("family_tv").set({
      id: "family_tv",
      userId: BOB_UID,
      userEmail: "bob@example.com",
      userName: "Bob",
      familyId: "fam_shared",
      familyCode: "FAM123",
      memberUids: [BOB_UID, ALICE_UID],
      deviceName: "Living Room TV",
      deviceModel: "Android TV",
      deviceType: "TV",
      batteryLevel: 100,
      isCharging: true,
      ramUsedMb: 1500,
      ramTotalMb: 4096,
      networkType: "ETHERNET",
      networkName: "HomeLAN",
      isOnline: true,
      remoteMessage: "",
      remoteMessageSender: "",
      ringAlert: false,
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("devices").doc("family_tv").get());
});

test("Authenticated user: can create their own device with valid schema", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("devices").doc("alice_device").set({
      id: "alice_device",
      userId: ALICE_UID,
      userEmail: "alice@example.com",
      userName: "Alice",
      familyId: "fam_alice",
      familyCode: "ALC123",
      memberUids: [ALICE_UID],
      deviceName: "Alice Phone",
      deviceModel: "Galaxy S24",
      deviceType: "PHONE",
      batteryLevel: 95,
      isCharging: true,
      ramUsedMb: 3000,
      ramTotalMb: 8192,
      networkType: "WIFI",
      networkName: "HomeWiFi",
      isOnline: true,
      remoteMessage: "",
      remoteMessageSender: "",
      ringAlert: false,
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

test("Authenticated user: CANNOT create device with mismatched userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("devices").doc("spoofed_device").set({
      id: "spoofed_device",
      userId: BOB_UID, // Spoofed!
      userEmail: "bob@example.com",
      userName: "Bob",
      familyId: "fam_alice",
      familyCode: "ALC123",
      memberUids: [BOB_UID],
      deviceName: "Fake Device",
      deviceModel: "Fake",
      deviceType: "PHONE",
      batteryLevel: 95,
      isCharging: false,
      ramUsedMb: 1000,
      ramTotalMb: 4000,
      networkType: "WIFI",
      networkName: "FakeWiFi",
      isOnline: true,
      remoteMessage: "",
      remoteMessageSender: "",
      ringAlert: false,
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});
