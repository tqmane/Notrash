import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { gzipSync, gunzipSync } from 'node:zlib';

const directory = new URL('../public/fonts/', import.meta.url);
const fonts = {
  NTYPE_FONT_GZIP_BASE64: 'ntype82_regular.otf',
  NDOT_FONT_GZIP_BASE64: 'ndot_55.otf',
};

function validate(font, name) {
  if (font.length > 1024 * 1024 || font.subarray(0, 4).toString() !== 'OTTO') {
    throw new Error(`${name}: a valid OpenType (.otf) font of up to 1 MiB is required.`);
  }
  return font;
}

if (process.argv[2] === 'encode') {
  const key = process.argv[3];
  if (!Object.hasOwn(fonts, key)) throw new Error(`Choose ${Object.keys(fonts).join(' or ')}.`);
  const font = validate(await readFile(new URL(fonts[key], directory)), fonts[key]);
  process.stdout.write(gzipSync(font).toString('base64'));
} else {
  for (const [key, name] of Object.entries(fonts)) {
    const file = new URL(name, directory);
    const existing = await readFile(file).catch(error => {
      if (error.code !== 'ENOENT') throw error;
      return null;
    });
    const value = process.env[key]?.trim();
    if (!value) {
      if (!existing) throw new Error(`Provide public/fonts/${name} locally or set ${key} in Vercel.`);
      validate(existing, name);
      continue;
    }
    const compressed = Buffer.from(value, 'base64');
    if (compressed.toString('base64') !== value) throw new Error(`${key}: invalid Base64.`);
    const font = validate(gunzipSync(compressed, { maxOutputLength: 1024 * 1024 }), name);
    if (existing) {
      if (!existing.equals(font)) throw new Error(`${name}: local font differs from ${key}; refusing to overwrite it.`);
    } else {
      await mkdir(directory, { recursive: true });
      await writeFile(file, font, { flag: 'wx' });
    }
  }
  console.log('Fonts ready.');
}
