import fs from 'fs';
import zlib from 'zlib';

function createPNG(width, height, r, g, b, a = 255) {
  const signature = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);

  // IHDR chunk
  const ihdrData = Buffer.alloc(13);
  ihdrData.writeUInt32BE(width, 0);
  ihdrData.writeUInt32BE(height, 4);
  ihdrData.writeUInt8(8, 8); // 8 bit depth
  ihdrData.writeUInt8(6, 9); // RGBA color type
  ihdrData.writeUInt8(0, 10);
  ihdrData.writeUInt8(0, 11);
  ihdrData.writeUInt8(0, 12);
  const ihdrChunk = makeChunk('IHDR', ihdrData);

  // Raw image scanlines
  const rowLength = width * 4 + 1;
  const rawData = Buffer.alloc(height * rowLength);
  for (let y = 0; y < height; y++) {
    const rowOffset = y * rowLength;
    rawData.writeUInt8(0, rowOffset); // filter type 0
    for (let x = 0; x < width; x++) {
      const pxOffset = rowOffset + 1 + x * 4;
      // Draw dark slate background with neon gradient center
      const dx = x - width / 2;
      const dy = y - height / 2;
      const dist = Math.sqrt(dx * dx + dy * dy);
      const isShieldCenter = dist < width * 0.3;

      if (isShieldCenter) {
        rawData.writeUInt8(61, pxOffset);     // R
        rawData.writeUInt8(255, pxOffset + 1); // G
        rawData.writeUInt8(196, pxOffset + 2); // B
        rawData.writeUInt8(255, pxOffset + 3); // A
      } else {
        rawData.writeUInt8(r, pxOffset);
        rawData.writeUInt8(g, pxOffset + 1);
        rawData.writeUInt8(b, pxOffset + 2);
        rawData.writeUInt8(a, pxOffset + 3);
      }
    }
  }

  const compressedData = zlib.deflateSync(rawData);
  const idatChunk = makeChunk('IDAT', compressedData);
  const iendChunk = makeChunk('IEND', Buffer.alloc(0));

  return Buffer.concat([signature, ihdrChunk, idatChunk, iendChunk]);
}

function makeChunk(type, data) {
  const len = data.length;
  const chunk = Buffer.alloc(len + 12);
  chunk.writeUInt32BE(len, 0);
  chunk.write(type, 4, 4, 'ascii');
  data.copy(chunk, 8);

  const crcData = chunk.subarray(4, len + 8);
  const crc = crc32(crcData) >>> 0;
  chunk.writeUInt32BE(crc, len + 8);
  return chunk;
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

// Generate files
if (!fs.existsSync('public')) {
  fs.mkdirSync('public');
}

fs.writeFileSync('public/pwa-192x192.png', createPNG(192, 192, 14, 22, 36));
fs.writeFileSync('public/pwa-512x512.png', createPNG(512, 512, 14, 22, 36));
fs.writeFileSync('public/pwa-maskable-512x512.png', createPNG(512, 512, 14, 22, 36));
fs.writeFileSync('public/apple-touch-icon.png', createPNG(180, 180, 14, 22, 36));
console.log('PNG assets successfully generated in /public');
