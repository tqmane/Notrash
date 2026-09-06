import { readFile, stat, glob } from 'node:fs/promises';
import assert from 'node:assert/strict';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const contentRoot=path.join(root,'src/content/docs');
let count=0;
for await (const page of glob('**/*.mdx',{cwd:contentRoot})) {
  const text=await readFile(path.join(contentRoot,page),'utf8');
  count++;
  for (const field of ['title','description','keywords']) assert(new RegExp('^'+field+':','m').test(text),`${page}: missing ${field}`);
  if(page.split(path.sep)[0]==='docs') assert(!/ユーザーの許可|今回の作業|202\d年\d+月\d+日/.test(text),`${page}: work-log language in user guide`);
  for (const match of text.matchAll(/\]\(([^)]+)\)|href="([^"]+)"/g)) {
    const target=match[1] || match[2];
    if(!target.startsWith('/')) { assert(/^https?:/.test(target),`${page}: relative link ${target}`); continue; }
    const route=decodeURI(target.split('#')[0]).replace(/^\//,'');
    const file=!route ? path.join(root,'src/pages/index.astro')
      : path.extname(route) ? path.join(root,'public',route) : path.join(contentRoot,route+'.mdx');
    await stat(file);
  }
}
assert(count>0,'No documentation pages found');
console.log(`PASS: ${count} documentation pages, metadata, links, guide copy`);
