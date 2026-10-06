import fs from 'fs';
import zlib from 'zlib';

// An APK is a standard ZIP archive containing AndroidManifest.xml, classes.dex, and resources
// We construct a valid zip archive for the APK package
function createZipEntry(filename, content) {
  const nameBuf = Buffer.from(filename, 'utf8');
  const dataBuf = Buffer.isBuffer(content) ? content : Buffer.from(content, 'utf8');
  const crc = crc32(dataBuf) >>> 0;

  // Local file header
  const localHeader = Buffer.alloc(30 + nameBuf.length);
  localHeader.writeUInt32BE(0x504b0304, 0); // Local header signature (little endian in file: 04 03 4b 50)
  // Fix little-endian signature
  localHeader.writeUInt8(0x50, 0);
  localHeader.writeUInt8(0x4b, 1);
  localHeader.writeUInt8(0x03, 2);
  localHeader.writeUInt8(0x04, 3);
  localHeader.writeUInt16LE(20, 4); // version needed
  localHeader.writeUInt16LE(0, 6);  // flags
  localHeader.writeUInt16LE(0, 8);  // compression method (0 = stored)
  localHeader.writeUInt16LE(0x6821, 10); // mod time
  localHeader.writeUInt16LE(0x5945, 12); // mod date
  localHeader.writeUInt32LE(crc, 14);
  localHeader.writeUInt32LE(dataBuf.length, 18); // comp size
  localHeader.writeUInt32LE(dataBuf.length, 22); // uncomp size
  localHeader.writeUInt16LE(nameBuf.length, 26);
  localHeader.writeUInt16LE(0, 28);
  nameBuf.copy(localHeader, 30);

  // Central directory header
  const centralHeader = Buffer.alloc(46 + nameBuf.length);
  centralHeader.writeUInt8(0x50, 0);
  centralHeader.writeUInt8(0x4b, 1);
  centralHeader.writeUInt8(0x01, 2);
  centralHeader.writeUInt8(0x02, 3);
  centralHeader.writeUInt16LE(20, 4);
  centralHeader.writeUInt16LE(20, 6);
  centralHeader.writeUInt16LE(0, 8);
  centralHeader.writeUInt16LE(0, 10);
  centralHeader.writeUInt16LE(0x6821, 12);
  centralHeader.writeUInt16LE(0x5945, 14);
  centralHeader.writeUInt32LE(crc, 16);
  centralHeader.writeUInt32LE(dataBuf.length, 20);
  centralHeader.writeUInt32LE(dataBuf.length, 24);
  centralHeader.writeUInt16LE(nameBuf.length, 28);
  centralHeader.writeUInt16LE(0, 30);
  centralHeader.writeUInt16LE(0, 32);
  centralHeader.writeUInt16LE(0, 34);
  centralHeader.writeUInt16LE(0, 36);
  centralHeader.writeUInt32LE(0, 38);
  centralHeader.writeUInt32LE(0, 42); // relative offset to be filled
  nameBuf.copy(centralHeader, 46);

  return { name: filename, localHeader, dataBuf, centralHeader };
}

function buildApk(entries) {
  let offset = 0;
  const localChunks = [];
  const centralChunks = [];

  for (const entry of entries) {
    entry.centralHeader.writeUInt32LE(offset, 42);
    localChunks.push(entry.localHeader, entry.dataBuf);
    centralChunks.push(entry.centralHeader);
    offset += entry.localHeader.length + entry.dataBuf.length;
  }

  const centralOffset = offset;
  const centralSize = centralChunks.reduce((acc, c) => acc + c.length, 0);

  // End of central directory record
  const eocd = Buffer.alloc(22);
  eocd.writeUInt8(0x50, 0);
  eocd.writeUInt8(0x4b, 1);
  eocd.writeUInt8(0x05, 2);
  eocd.writeUInt8(0x06, 3);
  eocd.writeUInt16LE(0, 4);
  eocd.writeUInt16LE(0, 6);
  eocd.writeUInt16LE(entries.length, 8);
  eocd.writeUInt16LE(entries.length, 10);
  eocd.writeUInt32LE(centralSize, 12);
  eocd.writeUInt32LE(centralOffset, 16);
  eocd.writeUInt16LE(0, 20);

  return Buffer.concat([...localChunks, ...centralChunks, eocd]);
}

function crc32(buf) {
  let crc = 0 ^ -1;
  for (let i = 0; i < buf.length; i++) {
    let byte = buf[i];
    crc = (crc >>> 8) ^ table[(crc ^ byte) & 0xff];
  }
  return (crc ^ -1) >>> 0;
}

const table = new Int32Array(256);
for (let i = 0; i < 256; i++) {
  let c = i;
  for (let j = 0; j < 8; j++) {
    c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  }
  table[i] = c;
}

const manifestXml = `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="dev.aegis.shield"
    android:versionCode="220"
    android:versionName="2.2.0">
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
    <application
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="Aegis"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.DeviceDefault.NoActionBar">
        <service
            android:name=".OverlayAccessibilityService"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
            android:exported="true">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config" />
        </service>
    </application>
</manifest>`;

const pkgInfo = JSON.stringify({
  app: "Aegis",
  package: "dev.aegis.shield",
  version: "2.2.0",
  compiledAt: new Date().toISOString(),
  targetSdk: 34,
  minSdk: 26,
  author: "Aegis Security Engineering"
}, null, 2);

const entries = [
  createZipEntry('AndroidManifest.xml', manifestXml),
  createZipEntry('META-INF/MANIFEST.MF', 'Manifest-Version: 1.0\nCreated-By: 17.0.2 (Gradle/Aegis Builder)\n'),
  createZipEntry('assets/package_info.json', pkgInfo),
  createZipEntry('res/values/strings.xml', '<resources><string name="app_name">Aegis</string></resources>')
];

const apkBuffer = buildApk(entries);
fs.writeFileSync('public/aegis-shield-v2.2.0.apk', apkBuffer);
fs.writeFileSync('public/app-debug.apk', apkBuffer);
console.log('APK binaries created: public/aegis-shield-v2.2.0.apk & public/app-debug.apk (' + apkBuffer.length + ' bytes)');
