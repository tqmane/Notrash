import { readFile, stat } from 'node:fs/promises';
import assert from 'node:assert/strict';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const config=JSON.parse(await readFile(path.join(root,'docs.json'),'utf8'));
const pages=config.navigation.groups.flatMap(group=>group.pages);
for (const page of pages) {
  const text=await readFile(path.join(root,page+'.mdx'),'utf8');
  for (const field of ['title','description','keywords']) assert(new RegExp('^'+field+':','m').test(text),`${page}: missing ${field}`);
  if(page.startsWith('docs/')) assert(!/ユーザーの許可|今回の作業|202\d年\d+月\d+日/.test(text),`${page}: work-log language in user guide`);
  for (const [,target] of text.matchAll(/\]\(([^)]+)\)/g)) {
    if(!target.startsWith('/')) { assert(/^https?:/.test(target),`${page}: relative link ${target}`); continue; }
    const route=decodeURI(target.split('#')[0]).replace(/^\//,'') || 'index';
    const file=route.match(/\.(png|svg|apk|otf)$/) ? route : route+'.mdx';
    await stat(path.join(root,file));
  }
}
assert(pages.length===11,'Review navigation after changing the page count');
console.log(`PASS: ${pages.length} pages, metadata, links, guide copy`);
