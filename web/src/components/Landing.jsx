import { useState } from 'react';

export const Landing = () => {
  const [mode, setMode] = useState(0);
  const modes = ["ダーク・高コントラスト", "ダーク・通常", "ライト・高コントラスト", "ライト・通常"];
  const labels = ["ダーク / 高", "ダーク / 通常", "ライト / 高", "ライト / 通常"];
  return (
    <div className="notrash-landing">
      <nav className="lp-navbar" aria-label="サイトナビゲーション">
        <a className="lp-nav-brand" href="/" aria-label="Notrash ホーム">NOTRASH<span /></a>
        <div><a href="/docs/overview">Docs</a><a href="/wiki/essential-recorder">Wiki <span aria-hidden="true">↗</span></a></div>
      </nav>
      <section className="lp-hero" aria-labelledby="lp-title">
        <div className="lp-hero-copy">
          <p className="lp-release"><span /> Nothing OS extension · v1.0.2</p>
          <h1 id="lp-title">NOTRASH<span className="lp-period">.</span></h1>
          <p className="lp-statement">Nothingを、<br />自分の設定で。</p>
          <p className="lp-lede">カメラのシャッター音から、Essential Voiceまで。<br className="lp-desktop-break" />使いたい機能だけを選ぶ、Nothing OS拡張モジュール。</p>
          <div className="lp-actions">
            <a className="lp-button lp-button-primary" href="/docs/install">導入ガイドを読む <span aria-hidden="true">↗</span></a>
            <a className="lp-text-link" href="#features">できることを見る <span aria-hidden="true">↓</span></a>
          </div>
        </div>
        <div className="lp-object-stage" aria-hidden="true">
          <div className="lp-dot-field" />
          <div className="lp-orbit lp-orbit-one" /><div className="lp-orbit lp-orbit-two" />
          <div className="lp-module">
            <span className="lp-screw lp-screw-a" /><span className="lp-screw lp-screw-b" />
            <span className="lp-module-top">( N )</span>
            <div className="lp-module-ring"><img src="/images/mark.svg" alt="" width="180" height="194" /></div>
            <span className="lp-module-name">NOTRASH</span>
            <span className="lp-screw lp-screw-c" /><span className="lp-screw lp-screw-d" />
          </div>
        </div>
      </section>
      <div className="lp-facts" aria-label="動作環境の要点">
        <span>Nothing OS</span><span>Vector / API 102</span><span>機能は初期OFF</span>
      </div>
      <section className="lp-features" id="features" aria-labelledby="lp-features-title">
        <div className="lp-section-intro"><h2 id="lp-features-title">いつもの操作に、<br />選択肢を。</h2><p>純正の使い心地を活かして、<br />必要なところだけを変える。</p></div>
        <article className="lp-feature-row">
          <div><p className="lp-feature-name">Camera</p><h3>シャッター音も、<br />あなたの設定で。</h3><p>地域によって隠れるシャッター音設定を表示。マナーモード中も、純正カメラのスイッチから切り替えられます。</p><a className="lp-text-link" href="/docs/camera">カメラの使い方 <span aria-hidden="true">↗</span></a></div>
          <div className="lp-shutter" aria-hidden="true"><div className="lp-shutter-outer"><div className="lp-shutter-inner"><span /></div></div><span className="lp-shutter-tick" /></div>
        </article>
        <article className="lp-feature-row lp-feature-reverse">
          <div><p className="lp-feature-name">Essential Voice</p><h3>話す、を<br />いつもの入力に。</h3><p>機種による制限を解除し、純正の設定入口とキーボードのVoiceボタンを利用可能に。対応するNothing OSのシステム機能が必要です。</p><a className="lp-text-link" href="/docs/essential-voice">Essential Voiceの使い方 <span aria-hidden="true">↗</span></a></div>
          <div className="lp-voice-art" aria-hidden="true"><div className="lp-wave">{[16,28,48,74,100,60,36,82,120,90,52,28,60,94,44,24,12].map((height,i) => <i key={i} style={{height: height + "px"}} />)}</div><span className="lp-voice-dot" /></div>
        </article>
      </section>
      <section className="lp-appearance" aria-labelledby="lp-appearance-title">
        <div className="lp-preview-area">
          <div className="lp-app-preview" data-mode={mode} aria-label={modes[mode] + "の外観プレビュー"}>
            <div className="lp-preview-brand">NOTRASH<span /></div>
            <p className="lp-preview-caption">Nothing OS customization</p>
            <p className="lp-preview-section">カメラ</p>
            <div className="lp-preview-row"><div><strong>シャッター音</strong><span>純正のスイッチで切り替える</span></div><i className="lp-preview-toggle" aria-hidden="true" /></div>
            <p className="lp-preview-section">Essential Voice</p>
            <div className="lp-preview-row"><div><strong>機種制限の解除</strong><span>音声入力をキーボードから</span></div><i className="lp-preview-toggle" aria-hidden="true" /></div>
            <p className="lp-preview-section">表示</p>
            <div className="lp-preview-row lp-preview-mode"><span>{mode < 2 ? "ダーク" : "ライト"}</span><span>{mode % 2 === 0 ? "高コントラスト" : "通常"}</span></div>
          </div>
          <p className="lp-preview-note">外観プレビュー。端末の設定は変更されません。</p>
        </div>
        <div className="lp-appearance-copy"><h2 id="lp-appearance-title">黒。白。赤。<br />選べる、4つの表情。</h2><p>Nothingらしいモノクロの操作部に、<br />必要なアクセントだけを。ライトとダーク、<br />2つのコントラストから選べます。</p>
          <div className="lp-mode-picker" role="group" aria-label="プレビューの配色">{modes.map((label,i) => <button type="button" key={label} aria-label={label} aria-pressed={mode === i} onClick={() => setMode(i)}><span className={"lp-mode-swatch lp-mode-swatch-" + i} />{labels[i]}</button>)}</div>
          <a className="lp-text-link" href="/docs/appearance">表示設定を詳しく <span aria-hidden="true">↗</span></a>
        </div>
      </section>
      <section className="lp-docs" aria-labelledby="lp-docs-title">
        <div><h2 id="lp-docs-title">Docs <span>&</span> Wiki</h2><p>最初の一歩から、動く仕組みまで。</p></div>
        <div className="lp-docs-list">
          <a href="/docs/install"><div><h3>はじめる</h3><p>ダウンロード、対応環境、初期設定。</p></div><span aria-hidden="true">↗</span></a>
          <a href="/docs/troubleshooting"><div><h3>困ったとき</h3><p>有効なのに使えない。ボタンが出ない。</p></div><span aria-hidden="true">↗</span></a>
          <a href="/wiki/essential-recorder"><div><h3>実装を読む</h3><p>純正3アプリの解析と、Notrashの仕組み。</p></div><span aria-hidden="true">↗</span></a>
        </div>
      </section>
      <footer className="lp-footer"><span>NOTRASH<span className="lp-period">.</span></span><p>Nothing OS向けの非公式拡張モジュール。</p><a href="/docs/compatibility">対応環境を確認 ↗</a></footer>
    </div>
  );
};
