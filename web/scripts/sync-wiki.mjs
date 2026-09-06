import { readFile, writeFile, copyFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import path from 'node:path';
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const pages = [
  ['camera', 'Nothing Cameraの解析', '16.0.01.26.00のシャッター音制御と、Notrashのフック。'],
  ['essential-recorder', 'Essential Recorderの解析', '16.0.60のVoice本体と、OSのキーボード連携。'],
  ['essential-space', 'Essential Spaceの解析', '2.1.7の構成、AIサービス、Voiceとの関係。']
];
const routes = { '../README.md':'/docs/overview', 'camera.md':'/wiki/camera', 'essential-recorder.md':'/wiki/essential-recorder', 'essential-space.md':'/wiki/essential-space' };
for (const [slug,title,description] of pages) {
  let body = await readFile(path.join(root, '..', 'docs', slug + '.md'), 'utf8');
  body = body.replace(/^# .*\r?\n/, '').trim();
  body = body.replace(/\[([^\]]+)\]\(([^)]+)\)/g, (match,label,url) => {
    if (routes[url]) return `[${label}](${routes[url]})`;
    if (url.startsWith('../verification/') && url.endsWith('.png')) return `[${label}](/images/${path.basename(url)})`;
    if (url.startsWith('../app/')) return '`' + label + '`';
    return match;
  });
  await writeFile(path.join(root, 'wiki', slug + '.mdx'), `---\ntitle: "${title}"\ndescription: "${description}"\nkeywords: [Notrash, Wiki, ${slug}]\n---\n\n${body}\n`);
}
for (const name of ['camera-on.png','keyboard-on.png','keyboard-off.png']) {
  await copyFile(path.join(root,'..','verification',name), path.join(root,'images',name));
}
console.log('Synced 3 wiki pages from the Android project documentation.');
