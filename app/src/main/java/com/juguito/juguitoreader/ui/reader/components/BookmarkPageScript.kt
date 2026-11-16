package com.juguito.juguitoreader.ui.reader.components

import com.juguito.juguitoreader.domain.model.Bookmark
import org.json.JSONArray

internal fun jumpToBookmarkScript(bookmark: Bookmark): String {
    val payload = JSONArray()
        .put(bookmark.quote)
        .put(bookmark.prefix)
        .put(bookmark.suffix)
        .put(bookmark.imageSrc.orEmpty())
        .put(bookmark.scrollPosition.toDouble())
        .toString()

    return """
        setTimeout(function() {
            var payload = $payload;
            var quote = payload[0];
            var imageSrc = payload[3];
            var scroll = payload[4];
            var found = false;

            if (imageSrc) {
                var imgs = document.getElementsByTagName('img');
                for (var i = 0; i < imgs.length; i++) {
                    if (imgs[i].getAttribute('src') === imageSrc) {
                        imgs[i].scrollIntoView({ block: 'start' });
                        found = true;
                        break;
                    }
                }
            }

            if (!found && quote) {
                var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
                var node;
                while ((node = walker.nextNode())) {
                    var text = node.textContent.replace(/\u00ad/g, '');
                    if (text.indexOf(quote) !== -1) {
                        (node.parentElement || node).scrollIntoView({ block: 'start' });
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                var scrollableHeight = document.body.scrollHeight - window.innerHeight;
                if (scroll > 1.1) {
                    window.scrollTo(0, scroll);
                } else if (scrollableHeight > 0) {
                    window.scrollTo(0, scrollableHeight * scroll);
                }
            }

            if (typeof currentScrollPercent === 'function' && typeof chapterIndex !== 'undefined') {
                AndroidBridge.reportScrollPosition(currentScrollPercent(), chapterIndex);
            }
            AndroidBridge.reportBookmarkJumpFinished();
        }, 150);
    """.trimIndent()
}

internal fun bookmarkPayload(bookmarks: List<Bookmark>): String {
    val array = JSONArray()
    bookmarks.forEach { mark ->
        array.put(
            JSONArray()
                .put(mark.id)
                .put(mark.kind.name)
                .put(mark.quote)
                .put(mark.prefix)
                .put(mark.suffix)
                .put(mark.imageSrc.orEmpty())
                .put(mark.color?.name.orEmpty())
                .put(mark.noteText.orEmpty())
        )
    }
    return array.toString()
}

internal fun bookmarkEngineScript(): String = """
    function paintBookmarks(payload) {
        ensureMarkStyle();
        clearMarks();
        for (var i = 0; i < payload.length; i++) {
            var mark = payload[i];
            var id = mark[0];
            var kind = mark[1];
            var quote = mark[2];
            var imageSrc = mark[5];
            var color = (mark[6] || 'AMBER').toLowerCase();
            var note = mark[7] || '';
            if (kind === 'IMAGE') {
                placePin(findImage(imageSrc), id);
            } else if (kind === 'POSITION') {
                if (imageSrc) placeIcon(findImage(imageSrc));
                else placeIcon(findQuoteStart(quote));
            } else {
                wrapQuote(id, quote, color);
                if (note) attachNoteFlag(id, note);
            }
        }
        setActiveMark(activeMarkId, activeMarkColor);
        ensureNoteDismiss();
        watchImages();
    }

    var activeMarkId = null;
    var activeMarkColor = '#FF7F50';

    function setActiveMark(id, color) {
        activeMarkId = id;
        if (color) activeMarkColor = color;
        var nodes = document.querySelectorAll('.jr-mark');
        for (var i = 0; i < nodes.length; i++) {
            var on = id != null && nodes[i].getAttribute('data-mark-id') === String(id);
            nodes[i].classList.toggle('jr-active', on);
            nodes[i].style.boxShadow = on ? 'inset 0 -2px 0 ' + activeMarkColor : '';
        }
    }

    function captureBookmarkHere() {
        var x = window.innerWidth / 2;
        var y = 16;
        var imageSrc = '';
        var quote = '';
        var prefix = '';
        var suffix = '';
        var el = document.elementFromPoint(x, y);
        var node = el;
        while (node && node !== document.body) {
            if (node.tagName === 'IMG') {
                imageSrc = node.getAttribute('src') || '';
                break;
            }
            node = node.parentElement;
        }
        if (!imageSrc && document.caretRangeFromPoint) {
            var caret = document.caretRangeFromPoint(x, y);
            if (caret && caret.startContainer && caret.startContainer.nodeType === 3) {
                var text = caret.startContainer.textContent.replace(/\u00ad/g, '').replace(/\s+/g, ' ');
                var start = Math.min(caret.startOffset, text.length);
                quote = text.substring(start, start + 120).trim();
                prefix = text.substring(Math.max(0, start - 48), start);
            }
        }
        AndroidBridge.reportBookmarkCapture(quote, prefix, suffix, imageSrc, currentScrollPercent(), chapterIndex);
    }

    function ensureMarkStyle() {
        if (document.getElementById('jr-mark-style')) return;
        var style = document.createElement('style');
        style.id = 'jr-mark-style';
        style.textContent = '.jr-amber{background-color:rgba(255,179,71,0.45);}.jr-green{background-color:rgba(46,125,50,0.45);}.jr-blue{background-color:rgba(101,175,255,0.45);}.jr-pink{background-color:rgba(244,143,177,0.45);}.jr-lilac{background-color:rgba(206,147,216,0.45);}.jr-mark.jr-active{filter:saturate(1.25);}.jr-bookmark-icon{float:right;display:inline-block;width:0.72em;height:1.15em;margin:0.05em 0 0 0.35em;line-height:0;pointer-events:none;vertical-align:text-top;}.jr-bookmark-icon svg{display:block;width:100%;height:100%;filter:drop-shadow(0 1px 1px rgba(0,0,0,.22));}.jr-image-pin{float:right;display:inline-block;width:2.2em;height:3.1em;margin:0.1em 0 0 0.45em;line-height:0;}.jr-image-pin svg{display:block;width:100%;height:100%;filter:drop-shadow(0 1px 1px rgba(0,0,0,.25));}.jr-note-flag{display:inline-block;width:0.55em;height:0.55em;margin-left:0.15em;border-radius:50%;background:#FF7F50;box-shadow:0 0 0 1.5px var(--jr-bg,#fff);vertical-align:super;}.jr-note-bubble{position:absolute;z-index:20;max-width:220px;padding:8px 10px;border-radius:12px;background:var(--jr-bg,#fff);color:var(--jr-text,#1A1C1E);border:1px solid var(--jr-text,#1A1C1E);box-shadow:0 2px 8px rgba(0,0,0,0.16);font-size:0.85em;line-height:1.35;text-align:left;}';
        document.head.appendChild(style);
    }

    function clearMarks() {
        var bubble = document.getElementById('jr-note-bubble');
        if (bubble) bubble.remove();
        var flags = document.querySelectorAll('.jr-note-flag');
        for (var f = flags.length - 1; f >= 0; f--) flags[f].remove();
        var icons = document.querySelectorAll('.jr-bookmark-icon, .jr-image-pin');
        for (var i = icons.length - 1; i >= 0; i--) icons[i].remove();
        var marks = document.querySelectorAll('.jr-mark');
        for (var j = marks.length - 1; j >= 0; j--) {
            var span = marks[j];
            var parent = span.parentNode;
            while (span.firstChild) {
                var child = span.firstChild;
                if (child.classList && child.classList.contains('jr-note-flag')) child.remove();
                else parent.insertBefore(child, span);
            }
            parent.removeChild(span);
        }
    }

    function watchImages() {
        if (window.jrImageWatch) return;
        window.jrImageWatch = true;
        document.addEventListener('touchstart', function(e) {
            var img = e.target && e.target.closest ? e.target.closest('img') : null;
            if (!img || !img.getAttribute('src')) return;
            var src = img.getAttribute('src');
            var alt = img.getAttribute('alt') || '';
            var timer = setTimeout(function() {
                AndroidBridge.consumeReaderTap();
                AndroidBridge.reportImagePinned(src, alt, currentScrollPercent(), chapterIndex);
            }, 500);
            var cancel = function() { clearTimeout(timer); };
            img.addEventListener('touchend', cancel, { once: true });
            img.addEventListener('touchmove', cancel, { once: true });
            img.addEventListener('touchcancel', cancel, { once: true });
        }, true);
    }

    function findImage(src) {
        var imgs = document.getElementsByTagName('img');
        for (var i = 0; i < imgs.length; i++) {
            if (imgs[i].getAttribute('src') === src) return imgs[i];
        }
        return null;
    }

    function findQuoteStart(quote) {
        var located = locateQuote(quote);
        return located ? located.start.node : null;
    }

    function placePin(img, id) {
        if (!img || !img.parentNode) return;
        var pin = document.createElement('span');
        pin.className = 'jr-image-pin';
        pin.setAttribute('data-image-id', String(id));
        var svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        svg.setAttribute('viewBox', '0 0 24 24');
        svg.setAttribute('aria-hidden', 'true');
        var head = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        head.setAttribute('fill', '#FF7F50');
        head.setAttribute('d', 'M12 1.5a4.2 4.2 0 1 0 0.01 0z');
        var needle = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        needle.setAttribute('fill', '#FF7F50');
        needle.setAttribute('d', 'M10.7 6.8h2.6L12 22.5z');
        svg.appendChild(head);
        svg.appendChild(needle);
        pin.appendChild(svg);
        img.parentNode.insertBefore(pin, img);
        pin.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            AndroidBridge.consumeReaderTap();
            AndroidBridge.reportPinnedImageTapped(id);
        });
    }

    function placeIcon(anchor) {
        if (!anchor || !anchor.parentNode) return;
        var icon = document.createElement('span');
        icon.className = 'jr-bookmark-icon';
        icon.setAttribute('aria-hidden', 'true');
        var svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        svg.setAttribute('viewBox', '0 0 16 24');
        svg.setAttribute('aria-hidden', 'true');
        var ribbon = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        ribbon.setAttribute('fill', '#FF7F50');
        ribbon.setAttribute('d', 'M1 1h14v21l-7-6-7 6V1z');
        svg.appendChild(ribbon);
        icon.appendChild(svg);
        anchor.parentNode.insertBefore(icon, anchor);
    }

    function wrapQuote(id, quote, color) {
        var located = locateQuote(quote);
        if (!located) return;
        var start = located.start;
        var end = located.end;
        if (start.node === end.node) {
            var node = start.node;
            var from = start.offset;
            var to = end.offset;
            if (from > 0) node = node.splitText(from);
            if (node.length > to - from) node.splitText(to - from);
            surround(node, id, color);
            return;
        }
        var first = start.node;
        if (start.offset > 0 && start.offset < first.length) first = first.splitText(start.offset);
        var last = end.node;
        if (end.offset > 0 && end.offset < last.length) last.splitText(end.offset);
        var nodes = textNodes();
        var startIndex = nodes.indexOf(first);
        var endIndex = nodes.indexOf(last);
        if (startIndex < 0 || endIndex < startIndex) return;
        for (var i = endIndex; i >= startIndex; i--) surround(nodes[i], id, color);
    }

    function surround(node, id, color) {
        if (!node || !node.parentNode || !node.textContent) return;
        var span = document.createElement('span');
        span.className = 'jr-mark jr-' + color;
        span.setAttribute('data-mark-id', String(id));
        node.parentNode.insertBefore(span, node);
        span.appendChild(node);
        span.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            if (e.target && e.target.classList && e.target.classList.contains('jr-note-flag')) return;
            AndroidBridge.consumeReaderTap();
            var rect = span.getBoundingClientRect();
            AndroidBridge.reportMarkTapped(id, rect.left, rect.top, rect.right, rect.bottom);
        });
    }

    function attachNoteFlag(id, note) {
        var nodes = document.querySelectorAll('.jr-mark[data-mark-id="' + id + '"]');
        if (!nodes.length) return;
        var host = nodes[nodes.length - 1];
        var flag = document.createElement('span');
        flag.className = 'jr-note-flag';
        flag.setAttribute('data-note-id', String(id));
        host.appendChild(flag);
        flag.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            AndroidBridge.consumeReaderTap();
            showNoteBubble(id, note, flag);
        });
    }

    function showNoteBubble(id, note, anchor) {
        var existing = document.getElementById('jr-note-bubble');
        if (existing) {
            var same = existing.getAttribute('data-note-id') === String(id);
            existing.remove();
            if (same) return;
        }
        var bubble = document.createElement('div');
        bubble.id = 'jr-note-bubble';
        bubble.className = 'jr-note-bubble';
        bubble.setAttribute('data-note-id', String(id));
        bubble.textContent = note;
        document.body.appendChild(bubble);
        var rect = anchor.getBoundingClientRect();
        var width = bubble.offsetWidth;
        var height = bubble.offsetHeight;
        var left = rect.left + window.scrollX;
        var maxLeft = window.scrollX + window.innerWidth - width - 12;
        if (left > maxLeft) left = maxLeft;
        if (left < window.scrollX + 12) left = window.scrollX + 12;
        var top = rect.bottom + window.scrollY + 8;
        if (rect.bottom + height + 12 > window.innerHeight) top = rect.top + window.scrollY - height - 8;
        bubble.style.left = left + 'px';
        bubble.style.top = top + 'px';
    }

    function ensureNoteDismiss() {
        if (window.jrNoteDismiss) return;
        window.jrNoteDismiss = true;
        document.addEventListener('click', function(e) {
            var bubble = document.getElementById('jr-note-bubble');
            if (!bubble) return;
            if (e.target.closest && (e.target.closest('.jr-note-flag') || e.target.closest('.jr-note-bubble'))) return;
            bubble.remove();
        }, true);
    }

    function locateQuote(quote) {
        if (!quote) return null;
        var nodes = textNodes();
        var norm = '';
        var map = [];
        var lastSpace = false;
        for (var n = 0; n < nodes.length; n++) {
            var text = nodes[n].textContent.replace(/\u00ad/g, '');
            for (var i = 0; i < text.length; i++) {
                var ch = text.charAt(i);
                if (/\s/.test(ch)) {
                    if (!lastSpace) {
                        norm += ' ';
                        map.push({ node: nodes[n], offset: i });
                        lastSpace = true;
                    }
                } else {
                    norm += ch;
                    map.push({ node: nodes[n], offset: i });
                    lastSpace = false;
                }
            }
        }
        var at = norm.indexOf(quote);
        if (at < 0 || at + quote.length > map.length) return null;
        var start = map[at];
        var endPoint = map[at + quote.length - 1];
        return {
            start: start,
            end: { node: endPoint.node, offset: endPoint.offset + 1 }
        };
    }
""".trimIndent()
