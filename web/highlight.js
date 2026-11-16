// A small Kotlin highlighter for the Code tab: comments, strings, annotations, keywords, numbers,
// calls and types. It only colours tokens; the text is always the exact source.
(function () {
  const KEYWORDS = new Set(
    (
      "package import fun val var if else when for while do return in is as " +
      "class object interface enum data private internal public override true false null this it " +
      "break continue throw try catch finally by get set"
    ).split(" "),
  );
  const esc = (s) => s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
  const TOKEN =
    /(\/\/[^\n]*|\/\*[\s\S]*?\*\/)|("(?:\\.|[^"\\\n])*")|(@[A-Za-z_][\w.]*)|(\b0x[0-9A-Fa-f]+L?\b|\b\d+(?:\.\d+)?(?:[fFL])?\b)|([A-Za-z_]\w*)(?=\s*\()|([A-Za-z_]\w*)/g;
  function highlightKotlin(src) {
    let out = "";
    let last = 0;
    src.replace(TOKEN, (m, com, str, ann, num, call, word, idx) => {
      out += esc(src.slice(last, idx));
      last = idx + m.length;
      if (com) out += `<span class="tok-com">${esc(com)}</span>`;
      else if (str) out += `<span class="tok-str">${esc(str)}</span>`;
      else if (ann) out += `<span class="tok-ann">${esc(ann)}</span>`;
      else if (num) out += `<span class="tok-num">${esc(num)}</span>`;
      else if (call)
        out += KEYWORDS.has(call) ? `<span class="tok-kw">${call}</span>` : `<span class="tok-fn">${call}</span>`;
      else if (word) {
        if (KEYWORDS.has(word)) out += `<span class="tok-kw">${word}</span>`;
        else if (/^[A-Z]/.test(word)) out += `<span class="tok-type">${word}</span>`;
        else out += word;
      }
      return m;
    });
    return out + esc(src.slice(last));
  }
  function highlightDiff(src) {
    return src
      .split("\n")
      .map((line) => {
        const e = esc(line);
        if (line.startsWith("+++") || line.startsWith("---")) return `<span class="diff-hunk">${e}</span>`;
        if (line.startsWith("@@")) return `<span class="diff-hunk">${e}</span>`;
        if (line.startsWith("+")) return `<span class="diff-add">${e}</span>`;
        if (line.startsWith("-")) return `<span class="diff-del">${e}</span>`;
        return e;
      })
      .join("\n");
  }
  window.RcHighlight = { kotlin: highlightKotlin, diff: highlightDiff, escape: esc };
})();
