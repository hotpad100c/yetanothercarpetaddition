/*
 * This file is part of the Yet Another Carpet Addition project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  Ryan100c and contributors
 *
 * Yet Another Carpet Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Yet Another Carpet Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Yet Another Carpet Addition.  If not, see <https://www.gnu.org/licenses/>.
 */

package mypals.ml.features.treeGrowthStats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class TreeStatsExporter {

    private static final String OUTPUT_FILE = "tree-growth-stats.html";

    private TreeStatsExporter() {
    }

    public static Path outputPath() {
        return FabricLoader.getInstance().getGameDir().resolve(OUTPUT_FILE);
    }

    public static Path export() throws IOException {
        Path out = outputPath();
        Files.write(out, buildHtml().getBytes(StandardCharsets.UTF_8));
        return out;
    }

    private static JsonObject buildData() {
        JsonObject root = new JsonObject();
        root.addProperty("generatedAt",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        root.addProperty("totalTrees", TreeGrowthStatistics.totalTreeCount());

        JsonObject speciesOut = new JsonObject();
        List<String> keys = TreeGrowthStatistics.speciesKeys();
        for (String key : keys) {
            TreeGrowthStatistics.SpeciesData data = TreeGrowthStatistics.snapshot().get(key);
            if (data == null) {
                continue;
            }
            JsonObject s = new JsonObject();
            s.addProperty("treeCount", data.treeCount);

            long logs = data.totalOf(TreeGrowthStatistics::isLog);
            long leaves = data.totalOf(TreeGrowthStatistics::isLeaves);
            long beehives = data.totalOf(b -> "beehive".equals(TreeGrowthStatistics.categoryOf(b)));
            long blocks = data.totals.values().stream().mapToLong(Long::longValue).sum();
            s.addProperty("totalLogs", logs);
            s.addProperty("totalLeaves", leaves);
            s.addProperty("totalBeehives", beehives);
            s.addProperty("totalBlocks", blocks);

            JsonArray blocksOut = new JsonArray();
            for (Map.Entry<Block, Long> e
                    : TreeGrowthStatistics.sortedTotals(data)) {
                JsonObject b = new JsonObject();
                b.addProperty("id", TreeGrowthStatistics.blockId(e.getKey()));
                b.addProperty("category", TreeGrowthStatistics.categoryOf(e.getKey()));
                b.addProperty("count", e.getValue());
                b.addProperty("perTree", data.treeCount == 0 ? 0d : (double) e.getValue() / data.treeCount);
                blocksOut.add(b);
            }
            s.add("blocks", blocksOut);

            TreeMap<Integer, List<JsonObject>> layers = new TreeMap<>();
            int minX = 0, maxX = 0, minZ = 0, maxZ = 0, minY = 0, maxY = 0;
            boolean first = true;

            for (Map.Entry<Integer, Map<Block, Integer>> entry
                    : data.posBlocks.entrySet()) {
                int packed = entry.getKey();
                int dx = TreeGrowthStatistics.unpackX(packed);
                int dy = TreeGrowthStatistics.unpackY(packed);
                int dz = TreeGrowthStatistics.unpackZ(packed);
                Map<Block, Integer> counts = entry.getValue();
                if (counts == null || counts.isEmpty()) {
                    continue;
                }
                List<Map.Entry<Block, Integer>> sorted =
                        new ArrayList<>(counts.entrySet());
                sorted.sort(Comparator.<Map.Entry<Block, Integer>>
                        comparingInt(Map.Entry::getValue).reversed());

                JsonObject cell = new JsonObject();
                cell.addProperty("x", dx);
                cell.addProperty("z", dz);
                cell.addProperty("block", TreeGrowthStatistics.blockId(sorted.get(0).getKey()));
                cell.addProperty("category", TreeGrowthStatistics.categoryOf(sorted.get(0).getKey()));
                cell.addProperty("count", sorted.get(0).getValue());
                cell.addProperty("prob", data.treeCount == 0 ? 0d
                        : (double) sorted.get(0).getValue() / data.treeCount);

                JsonArray others = new JsonArray();
                for (Map.Entry<Block, Integer> c : sorted) {
                    JsonObject o = new JsonObject();
                    o.addProperty("block", TreeGrowthStatistics.blockId(c.getKey()));
                    o.addProperty("category", TreeGrowthStatistics.categoryOf(c.getKey()));
                    o.addProperty("count", c.getValue());
                    o.addProperty("prob", data.treeCount == 0 ? 0d : (double) c.getValue() / data.treeCount);
                    others.add(o);
                }
                cell.add("all", others);

                layers.computeIfAbsent(dy, k -> new ArrayList<>()).add(cell);

                if (first) {
                    minX = maxX = dx;
                    minZ = maxZ = dz;
                    minY = maxY = dy;
                    first = false;
                } else {
                    minX = Math.min(minX, dx);
                    maxX = Math.max(maxX, dx);
                    minZ = Math.min(minZ, dz);
                    maxZ = Math.max(maxZ, dz);
                    minY = Math.min(minY, dy);
                    maxY = Math.max(maxY, dy);
                }
            }

            JsonObject bounds = new JsonObject();
            bounds.addProperty("minX", minX);
            bounds.addProperty("maxX", maxX);
            bounds.addProperty("minZ", minZ);
            bounds.addProperty("maxZ", maxZ);
            bounds.addProperty("minY", minY);
            bounds.addProperty("maxY", maxY);
            s.add("bounds", bounds);

            JsonArray layersOut = new JsonArray();
            for (Map.Entry<Integer, List<JsonObject>> layer : layers.entrySet()) {
                JsonObject l = new JsonObject();
                l.addProperty("y", layer.getKey());
                JsonArray cells = new JsonArray();
                for (JsonObject c : layer.getValue()) {
                    cells.add(c);
                }
                l.add("cells", cells);
                layersOut.add(l);
            }
            s.add("layers", layersOut);

            speciesOut.add(key, s);
        }
        root.add("species", speciesOut);

        JsonArray order = new JsonArray();
        for (String k : keys) {
            order.add(k);
        }
        root.add("speciesOrder", order);
        return root;
    }

    private static String buildHtml() {
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(buildData()).replace("</", "<\\/");

        return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>树苗生长统计 / Sapling Growth Statistics</title>
<style>
  :root { --bg:#14161a; --panel:#1c1f26; --line:#2c313a; --text:#e6e8eb; --dim:#9aa4b2; --accent:#2f6feb; }
  * { box-sizing:border-box; }
  body { margin:0; background:var(--bg); color:var(--text);
         font-family:system-ui,-apple-system,"Segoe UI","Noto Sans SC",sans-serif; }
  header { padding:16px 24px; background:var(--panel); border-bottom:1px solid var(--line); }
  h1 { margin:0 0 4px; font-size:18px; font-weight:600; }
  header .sub { color:var(--dim); font-size:13px; }
  main { padding:20px 24px 60px; max-width:1400px; margin:0 auto; }
  h2 { font-size:13px; font-weight:600; color:var(--dim); text-transform:uppercase;
       letter-spacing:.06em; margin:0 0 12px; }
  h2 .hint { float:right; text-transform:none; letter-spacing:0; font-weight:400; font-size:12px; }
  .panel { background:var(--panel); border:1px solid var(--line); border-radius:12px; padding:16px; margin-bottom:18px; }
  .tabs { display:flex; gap:8px; flex-wrap:wrap; margin-bottom:18px; }
  .tab { padding:7px 14px; border-radius:999px; background:#23272f; border:1px solid var(--line);
         cursor:pointer; font-size:13px; }
  .tab.active { background:var(--accent); border-color:var(--accent); color:#fff; }
  .cards { display:grid; grid-template-columns:repeat(auto-fit,minmax(140px,1fr)); gap:12px; margin-bottom:18px; }
  .card { background:var(--panel); border:1px solid var(--line); border-radius:10px; padding:12px 14px; }
  .card .k { color:var(--dim); font-size:12px; margin-bottom:6px; }
  .card .v { font-size:20px; font-weight:600; font-variant-numeric:tabular-nums; }
  .legend { display:flex; flex-wrap:wrap; gap:8px; }
  .chip { display:flex; align-items:center; gap:7px; padding:6px 11px; border-radius:999px;
          border:1px solid var(--line); background:#23272f; cursor:pointer; font-size:12px; user-select:none; }
  .chip.off { opacity:.35; text-decoration:line-through; }
  .chip.hl { border-color:var(--accent); background:#22304d; box-shadow:0 0 0 1px var(--accent); }
  .chip .n { color:var(--dim); font-variant-numeric:tabular-nums; }
  .chip .lbl { max-width:200px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
  .chip .eye { opacity:.85; padding:3px 10px; border-radius:7px; font-size:12px;
               border:1px solid var(--line); background:#2a2f38; margin-left:4px;
               line-height:1.25; white-space:nowrap; }
  .chip .eye:hover { opacity:1; background:#343a45; border-color:var(--accent); color:#fff; }
  .sw { width:11px; height:11px; border-radius:3px; display:inline-block; flex:none; }
  canvas { width:100%; height:560px; display:block; background:#0f1114; border-radius:10px;
           cursor:grab; touch-action:none; }
  canvas.drag { cursor:grabbing; }
  .viewwrap { position:relative; }
  .pickinfo { position:absolute; top:12px; right:12px; max-width:320px; display:none;
              background:rgba(13,15,18,.95); border:1px solid var(--line); border-radius:10px;
              padding:10px 12px; font-size:12px; box-shadow:0 8px 24px rgba(0,0,0,.5); }
  .pickinfo .hdr { font-weight:600; margin-bottom:6px; }
  .pickinfo table { border-collapse:collapse; font-size:12px; }
  .pickinfo th, .pickinfo td { padding:2px 7px; text-align:left; }
  .pickinfo td.num { text-align:right; font-variant-numeric:tabular-nums; }
  .row { display:flex; align-items:center; gap:18px; flex-wrap:wrap; margin-bottom:14px;
         font-size:13px; color:var(--dim); }
  input[type=range] { width:220px; vertical-align:middle; }
  button { background:#23272f; color:var(--text); border:1px solid var(--line); border-radius:8px;
           padding:6px 12px; font-size:12px; cursor:pointer; }
  button:hover { border-color:var(--accent); }
  .profilewrap { overflow:auto; }
  table.profile { border-collapse:collapse; font-size:11px; }
  table.profile th { color:var(--dim); font-weight:500; padding:4px 7px; white-space:nowrap; }
  table.profile th.yh { text-align:right; position:sticky; left:0; background:var(--panel); z-index:2; }
  table.profile td { padding:0; }
  .cell { width:56px; height:30px; border-radius:5px; margin:1px; display:flex; flex-direction:column;
          align-items:center; justify-content:center; line-height:1.05;
          box-shadow:inset 0 0 0 1px rgba(255,255,255,.14); }
  .cell.has { cursor:crosshair; }
  td .cell:not(.has) { background:#20242b; }
  .cell .b { font-size:10px; font-weight:600; }
  .cell .p { font-size:9px; opacity:.75; font-variant-numeric:tabular-nums; }
  .axis { color:var(--dim); font-size:11px; margin:0 0 8px; }
  #tooltip { position:fixed; pointer-events:none; background:#0d0f12; border:1px solid var(--line);
             border-radius:8px; padding:10px 12px; font-size:12px; max-width:340px; display:none; z-index:20;
             box-shadow:0 8px 24px rgba(0,0,0,.55); }
  #tooltip table { border-collapse:collapse; font-size:12px; }
  #tooltip th, #tooltip td { padding:2px 7px; text-align:left; }
  #tooltip td.num { text-align:right; font-variant-numeric:tabular-nums; }
  .empty { color:var(--dim); padding:20px; border:1px dashed var(--line); border-radius:10px; font-size:13px; }
  code { background:#23272f; padding:1px 6px; border-radius:4px; font-size:12px; }
</style>
</head>
<body>
<header>
  <h1>树苗生长统计 / Sapling Growth Statistics</h1>
  <div class="sub" id="meta"></div>
</header>
<main><div id="app"></div></main>
<div id="tooltip"></div>
<script>
const DATA = __DATA__;

const BASE_COLOR = { log:'#c98a4b', leaves:'#5faa3c', beehive:'#e0b040', sapling:'#8bc34a', dirt:'#8d6e52', other:'#7f8ea3' };

function colorOf(id, cat) {
  if (cat && cat !== 'other' && BASE_COLOR[cat]) return BASE_COLOR[cat];
  let h = 0;
  for (let i = 0; i < id.length; i++) h = (h * 31 + id.charCodeAt(i)) % 360;
  return 'hsl(' + h + ',36%,63%)';
}

function shortName(id) {
  const p = id.includes(':') ? id.split(':')[1] : id;
  const parts = p.split('_');
  const last = parts[parts.length - 1];
  const map = { log:'木', wood:'木', stem:'柄', hyphae:'柄', leaves:'叶', planks:'板',
                sapling:'苗', propagule:'苗', beehive:'巢', bee_nest:'巢', vine:'藤',
                dirt:'土', podzol:'灰', moss_block:'苔', cocoa:'可', shroomlight:'光',
                wart_block:'疣', roots:'根', mud:'泥' };
  if (map[last]) return map[last];
  return last.slice(0, 3);
}

let lastFaces = null;

const state = { species:null, hidden:new Set(), highlight:null, yaw:0.7, pitch:0.32, dist:30,
                panX:0, panY:0, z:0, dispLayer:null, onlyLayer:false };

function list() { return (DATA.speciesOrder || []).filter(k => DATA.species[k]); }

function render() {
  const app = document.getElementById('app');
  const keys = list();
  document.getElementById('meta').textContent =
    '生成于 ' + DATA.generatedAt + ' · 总样本 ' + DATA.totalTrees + ' 棵树 · 树种 ' + keys.length + ' 个';

  if (!keys.length) {
    app.innerHTML = '<div class="empty">还没有数据。开启规则 <code>saplingGrowthStatistics</code>，' +
      '让树苗长成树后再执行 <code>/treeStats export</code>。</div>';
    return;
  }
  if (!state.species || !DATA.species[state.species]) { state.species = keys[0]; state.hidden.clear(); state.highlight = null; }
  const sp = DATA.species[state.species];

  app.innerHTML =
    '<div class="tabs" id="tabs"></div>' +
    '<div class="cards">' +
      card('样本（树）', sp.treeCount) +
      card('总原木', sp.totalLogs) +
      card('总树叶', sp.totalLeaves) +
      card('总蜂巢', sp.totalBeehives) +
      card('方块总数', sp.totalBlocks) +
      card('平均原木/棵', sp.treeCount ? (sp.totalLogs / sp.treeCount).toFixed(2) : '0') +
    '</div>' +
    '<div class="panel"><h2>方块显示 <span class="hint">点击切换显示 / 隐藏，三维与剖面表联动</span></h2>' +
      '<div class="legend" id="legend"></div></div>' +
    '<div class="panel"><h2>三维视图 <span class="hint">左键拖拽旋转 · 滚轮缩放 · 右键拖拽平移 · 点击方块看信息</span></h2>' +
      '<div class="row"><span>显示层级：</span><input type="range" id="layerRange">' +
      '<span id="layerText"></span>' +
      '<label><input type="checkbox" id="onlyLayer"' + (state.onlyLayer ? ' checked' : '') +
      '> 只看该层（不勾选则显示到该层为止）</label></div>' +
      '<div class="viewwrap"><canvas id="view3d"></canvas><div id="pickInfo" class="pickinfo"></div></div>' +
      '<div class="row" style="margin-top:12px"><button id="resetView">重置视角</button>' +
      '<span id="viewInfo"></span></div></div>' +
    '<div class="panel"><h2>纵向剖面表 <span class="hint">横轴 = 到树苗的水平距离，纵轴 = 高度</span></h2>' +
      '<div class="row">' +
        '<span>Z 切面：</span><input type="range" id="zSlider"><span id="zLabel"></span>' +
        '<span>（树苗中心对称，只显示 X ≥ 0 一侧）</span>' +
      '</div>' +
      '<div class="profilewrap" id="profileWrap"></div></div>';

  const tabs = document.getElementById('tabs');
  for (const k of keys) {
    const el = document.createElement('div');
    el.className = 'tab' + (k === state.species ? ' active' : '');
    el.textContent = k + ' (' + DATA.species[k].treeCount + ')';
    el.onclick = () => { state.species = k; state.hidden.clear(); state.highlight = null;
                         state.z = 0; state.dispLayer = null; render(); };
    tabs.appendChild(el);
  }

  const zs = new Set();
  for (const layer of sp.layers) for (const c of layer.cells) zs.add(c.z);
  const zList = [...zs].sort((a, b) => a - b);
  if (!zList.includes(state.z)) state.z = zList.includes(0) ? 0 : zList[0];
  const zSlider = document.getElementById('zSlider');
  zSlider.min = 0; zSlider.max = Math.max(0, zList.length - 1);
  zSlider.value = Math.max(0, zList.indexOf(state.z));
  const zLabel = document.getElementById('zLabel');
  const applyZ = () => {
    state.z = zList[parseInt(zSlider.value, 10)];
    zLabel.textContent = 'Z = ' + state.z;
    drawProfile();
    draw3D();
  };
  zSlider.oninput = applyZ;
  zLabel.textContent = 'Z = ' + state.z;

  renderLegend(sp);
  applyZ();

  if (state.dispLayer === null || state.dispLayer > sp.bounds.maxY || state.dispLayer < sp.bounds.minY) {
    state.dispLayer = sp.bounds.maxY;
  }
  const lr = document.getElementById('layerRange');
  lr.min = sp.bounds.minY;
  lr.max = sp.bounds.maxY;
  lr.value = state.dispLayer;
  const applyLayer = () => {
    state.dispLayer = parseInt(lr.value, 10);
    document.getElementById('layerText').textContent =
      (state.onlyLayer ? 'Y = ' : 'Y ≤ ') + state.dispLayer;
    draw3D();
  };
  lr.oninput = applyLayer;
  document.getElementById('onlyLayer').onchange = e => {
    state.onlyLayer = e.target.checked;
    applyLayer();
  };
  applyLayer();

  const canvas = document.getElementById('view3d');
  canvas.addEventListener('mousedown', ev => {
    ev.preventDefault();
    canvas.classList.add('drag');
    let lx = ev.clientX, ly = ev.clientY, moved = 0;
    const right = ev.button === 2;
    const move = e => {
      const dx = e.clientX - lx, dy = e.clientY - ly;
      lx = e.clientX; ly = e.clientY;
      moved += Math.abs(dx) + Math.abs(dy);
      if (right) { state.panX += dx; state.panY += dy; }
      else { state.yaw += dx * 0.01; state.pitch = Math.max(-1.45, Math.min(1.45, state.pitch + dy * 0.01)); }
      draw3D();
    };
    const up = e => { canvas.classList.remove('drag');
      window.removeEventListener('mousemove', move); window.removeEventListener('mouseup', up);
      if (!right && moved < 4) handleCanvasClick(e); };
    window.addEventListener('mousemove', move);
    window.addEventListener('mouseup', up);
  });
  canvas.addEventListener('contextmenu', e => e.preventDefault());
  canvas.addEventListener('wheel', e => {
    e.preventDefault();
    state.dist = Math.max(6, Math.min(140, state.dist * (e.deltaY > 0 ? 1.1 : 0.9)));
    draw3D();
  }, { passive:false });
  document.getElementById('resetView').onclick = () => {
    state.yaw = 0.7; state.pitch = 0.32; state.dist = 30; state.panX = 0; state.panY = 0; draw3D();
  };
  window.addEventListener('resize', draw3D);
}

function card(k, v) {
  return '<div class="card"><div class="k">' + k + '</div><div class="v">' + v + '</div></div>';
}

function renderLegend(sp) {
  const box = document.getElementById('legend');
  box.innerHTML = '';
  for (const b of sp.blocks) {
    const el = document.createElement('div');
    el.className = 'chip' + (state.hidden.has(b.id) ? ' off' : '') +
                   (state.highlight === b.id ? ' hl' : '');
    el.innerHTML = '<span class="sw" style="background:' + colorOf(b.id, b.category) + '"></span>' +
      '<span class="lbl">' + b.id + '</span> <span class="n">' + b.count + '</span>' +
      '<span class="eye" title="点击切换该类方块的显示 / 隐藏">' +
      (state.hidden.has(b.id) ? '显示' : '隐藏') + '</span>';
    el.onclick = ev => {
      if (ev.target.classList.contains('eye')) {
        if (state.hidden.has(b.id)) state.hidden.delete(b.id); else state.hidden.add(b.id);
      } else {
        state.highlight = state.highlight === b.id ? null : b.id;
      }
      renderLegend(sp); draw3D(); drawProfile();
    };
    box.appendChild(el);
  }
}

function pick(cell) {
  if (!cell || !cell.all) return null;
  for (const o of cell.all) if (!state.hidden.has(o.block)) return o;
  return null;
}

function drawProfile() {
  const sp = DATA.species[state.species];
  const b = sp.bounds;
  const wrap = document.getElementById('profileWrap');
  const map = new Map();
  for (const layer of sp.layers) {
    for (const c of layer.cells) {
      if (c.z !== state.z) continue;
      map.set(c.x + ',' + layer.y, c);
    }
  }
  const xMax = Math.max(0, b.maxX);
  const xMin = 0;
  const yTop = b.maxY, yBot = b.minY;

  if (!map.size) {
    wrap.innerHTML = '<div class="empty">Z = ' + state.z + ' 这个切面上没有数据，换个 Z 试试。</div>';
    return;
  }

  let html = '<div class="axis">该切面共 ' + map.size + ' 个位置；格子颜色 = 主导方块，' +
             '百分比 = 该方块在此位置的出现率（出现次数 / 样本数）</div>';
  html += '<table class="profile"><thead><tr><th class="yh">Y \\ X</th>';
  for (let x = xMin; x <= xMax; x++) html += '<th>' + x + '</th>';
  html += '</tr></thead><tbody>';

  for (let y = yTop; y >= yBot; y--) {
    html += '<tr><th class="yh">' + (y > 0 ? '+' + y : y) + '</th>';
    for (let x = xMin; x <= xMax; x++) {
      const cell = map.get(x + ',' + y);
      const top = pick(cell);
      if (!top) { html += '<td><div class="cell"></div></td>'; continue; }
      const meta = cell.all.find(o => o.block === top.block) || {};
      let bg = mixRgb([26, 31, 38], cssToRgb(colorOf(top.block, meta.category)),
                      0.55 + 0.45 * Math.min(1, top.prob));
      const isHl = state.highlight && !state.hidden.has(state.highlight) &&
                   cell.all.some(o => o.block === state.highlight);
      if (isHl) bg = mixRgb(bg, [255, 255, 255], 0.2);
      const ring = isHl ? ';outline:2px solid #ffce00;outline-offset:-1px' : '';
      const lum = 0.299 * bg[0] + 0.587 * bg[1] + 0.114 * bg[2];
      const fg = lum > 132 ? '#0b0d10' : '#eef1f4';
      const pct = (top.prob * 100).toFixed(2);
      html += '<td><div class="cell has" data-x="' + x + '" data-y="' + y + '" ' +
              'style="background:rgb(' + bg[0] + ',' + bg[1] + ',' + bg[2] + ');color:' + fg + ring + '">' +
              '<span class="b">' + shortName(top.block) + '</span>' +
              '<span class="p">' + pct + '%</span></div></td>';
    }
    html += '</tr>';
  }
  html += '</tbody></table>';
  wrap.innerHTML = html;

  wrap.querySelectorAll('.cell.has').forEach(el => {
    el.onmousemove = ev => {
      const cell = map.get(el.dataset.x + ',' + el.dataset.y);
      if (cell) showTip(ev, '相对坐标 (' + cell.x + ', ' + el.dataset.y + ', ' + state.z + ')', cell.all);
    };
    el.onmouseleave = hideTip;
  });
}

function showTip(ev, title, all) {
  const tip = document.getElementById('tooltip');
  let rows = '';
  for (const o of all) {
    const dim = state.hidden.has(o.block) ? ' style="opacity:.35"' : '';
    rows += '<tr' + dim + '><td><span class="sw" style="background:' + colorOf(o.block, null) + '"></span>' +
      o.block + '</td><td class="num">' + o.count + '</td><td class="num">' +
      (o.prob * 100).toFixed(2) + '%</td></tr>';
  }
  tip.innerHTML = '<b>' + title + '</b><table><thead><tr><th>方块</th><th class="num">次数</th>' +
    '<th class="num">出现率</th></tr></thead><tbody>' + rows + '</tbody></table>';
  tip.style.display = 'block';
  const pad = 14, w = tip.offsetWidth, h = tip.offsetHeight;
  tip.style.left = Math.min(ev.clientX + pad, window.innerWidth - w - 8) + 'px';
  tip.style.top = Math.min(ev.clientY + pad, window.innerHeight - h - 8) + 'px';
}

function hideTip() { document.getElementById('tooltip').style.display = 'none'; }

const FACES = [
  { n:[0,1,0],  v:[[0,1,0],[1,1,0],[1,1,1],[0,1,1]], s:1.00 },
  { n:[0,0,1],  v:[[0,0,1],[1,0,1],[1,1,1],[0,1,1]], s:0.78 },
  { n:[1,0,0],  v:[[1,0,0],[1,0,1],[1,1,1],[1,1,0]], s:0.88 },
  { n:[0,0,-1], v:[[0,0,0],[0,1,0],[1,1,0],[1,0,0]], s:0.62 },
  { n:[-1,0,0], v:[[0,0,0],[0,0,1],[0,1,1],[0,1,0]], s:0.52 },
  { n:[0,-1,0], v:[[0,0,0],[1,0,0],[1,0,1],[0,0,1]], s:0.40 }
];

const EDGES = [[0,1],[2,3],[4,5],[6,7],[0,2],[1,3],[4,6],[5,7],[0,4],[1,5],[2,6],[3,7]];

function rotate(x, y, z) {
  const cy = Math.cos(state.yaw), sy = Math.sin(state.yaw);
  const x1 = x * cy - z * sy, z1 = x * sy + z * cy;
  const cp = Math.cos(state.pitch), sp = Math.sin(state.pitch);
  return { x:x1, y:y * cp - z1 * sp, z:y * sp + z1 * cp };
}

function projectCube(x, y, z, env) {
  const corners = [];
  let depth = 0;
  for (let i = 0; i < 8; i++) {
    const px = (x - env.cxm) + (i & 1), py = (y - env.cym) + ((i >> 1) & 1), pz = (z - env.czm) + ((i >> 2) & 1);
    const r = rotate(px, py, pz);
    const f = env.fit * 14 / (env.dist + r.z);
    corners.push({ x:env.cx + r.x * f, y:env.cy - r.y * f, z:r.z });
    depth += r.z;
  }
  return { corners:corners, depth:depth / 8 };
}

function pointInQuad(px, py, quad) {
  let sign = 0;
  for (let i = 0; i < 4; i++) {
    const a = quad[i], b = quad[(i + 1) % 4];
    const cross = (b.x - a.x) * (py - a.y) - (b.y - a.y) * (px - a.x);
    if (Math.abs(cross) < 1e-6) continue;
    const s = cross > 0 ? 1 : -1;
    if (sign === 0) sign = s; else if (s !== sign) return false;
  }
  return true;
}

function pickCube(px, py) {
  if (!lastFaces) return null;
  for (let i = lastFaces.length - 1; i >= 0; i--) {
    const item = lastFaces[i];
    if (!item.faces) continue;
    for (const quad of item.faces) {
      if (pointInQuad(px, py, quad)) return item.cube;
    }
  }
  return null;
}

function findCell(x, y, z) {
  const sp = DATA.species[state.species];
  for (const layer of sp.layers) {
    if (layer.y !== y) continue;
    for (const c of layer.cells) if (c.x === x && c.z === z) return c;
  }
  return null;
}

function hidePickInfo() {
  const box = document.getElementById('pickInfo');
  if (box) box.style.display = 'none';
}

function showPickInfo(cube) {
  const box = document.getElementById('pickInfo');
  const cell = findCell(cube.x, cube.y, cube.z);
  if (!box || !cell) { hidePickInfo(); return; }
  let rows = '';
  for (const o of cell.all) {
    const dim = state.hidden.has(o.block) ? ' style="opacity:.4"' : '';
    rows += '<tr' + dim + '><td><span class="sw" style="background:' +
            colorOf(o.block, o.category) + '"></span>' + o.block + '</td>' +
            '<td class="num">' + o.count + '</td>' +
            '<td class="num">' + (o.prob * 100).toFixed(2) + '%</td></tr>';
  }
  box.innerHTML = '<div class="hdr">相对坐标 (' + cube.x + ', ' + cube.y + ', ' + cube.z + ')' +
    ' · 样本 ' + DATA.species[state.species].treeCount + ' 棵</div>' +
    '<table><thead><tr><th>方块</th><th class="num">次数</th><th class="num">出现率</th></tr></thead>' +
    '<tbody>' + rows + '</tbody></table>';
  box.style.display = 'block';
}

function handleCanvasClick(ev) {
  const canvas = document.getElementById('view3d');
  const rect = canvas.getBoundingClientRect();
  const hit = pickCube(ev.clientX - rect.left, ev.clientY - rect.top);
  if (hit) showPickInfo(hit); else hidePickInfo();
}

function projectPoint(x, y, z, env) {
  const r = rotate(x - env.cxm, y - env.cym, z - env.czm);
  const f = env.fit * 14 / (env.dist + r.z);
  return { x:env.cx + r.x * f, y:env.cy - r.y * f };
}

function draw3D() {
  const canvas = document.getElementById('view3d');
  if (!canvas) return;
  const dpr = window.devicePixelRatio || 1;
  const w = canvas.clientWidth, h = canvas.clientHeight;
  if (canvas.width !== Math.round(w * dpr)) { canvas.width = Math.round(w * dpr); canvas.height = Math.round(h * dpr); }
  const g = canvas.getContext('2d');
  g.setTransform(dpr, 0, 0, dpr, 0, 0);
  g.clearRect(0, 0, w, h);

  const sp = DATA.species[state.species];
  const cubes = [];
  for (const layer of sp.layers) {
    if (state.onlyLayer ? layer.y !== state.dispLayer : layer.y > state.dispLayer) continue;
    for (const c of layer.cells) {
      const top = pick(c);
      if (!top) continue;
      cubes.push({ x:c.x, y:layer.y, z:c.z, block:top.block, prob:top.prob, cat:top.category });
    }
  }
  document.getElementById('viewInfo').textContent = cubes.length + ' 个方块（已按开关过滤）';
  if (!cubes.length) return;

  const env = {
    cxm: (sp.bounds.minX + sp.bounds.maxX) / 2,
    cym: (sp.bounds.minY + sp.bounds.maxY) / 2,
    czm: (sp.bounds.minZ + sp.bounds.maxZ) / 2,
    fit: Math.min(w, h) / (state.dist * 0.11 + 6),
    cx: w / 2 + state.panX,
    cy: h / 2 + state.panY,
    dist: state.dist
  };

  const pts = [];
  for (const cube of cubes) {
    const p = projectCube(cube.x, cube.y, cube.z, env);
    pts.push({ cube:cube, corners:p.corners, depth:p.depth });
  }
  pts.sort((a, b) => b.depth - a.depth);
  lastFaces = pts;

  {
    const op = projectCube(0, 0, 0, env);
    g.save();
    g.setLineDash([4, 3]);
    g.lineWidth = 2;
    g.strokeStyle = 'rgba(130,255,190,.95)';
    g.beginPath();
    for (const e of EDGES) {
      const a = op.corners[e[0]], b = op.corners[e[1]];
      g.moveTo(a.x, a.y);
      g.lineTo(b.x, b.y);
    }
    g.stroke();
    g.restore();
    const lbl = projectPoint(0, 1.5, 0, env);
    g.save();
    g.fillStyle = 'rgba(150,255,200,.95)';
    g.font = '12px system-ui,sans-serif';
    g.textAlign = 'center';
    g.fillText('树苗 (0,0,0)', lbl.x, lbl.y);
    g.restore();
  }

  g.save();
  g.lineWidth = 2;
  g.strokeStyle = 'rgba(120,200,255,.8)';
  g.fillStyle = 'rgba(170,220,255,.95)';
  g.font = '13px system-ui,sans-serif';
  g.textAlign = 'center';
  g.textBaseline = 'middle';
  const org = projectPoint(0, 0, 0, env);
  const span = Math.max(sp.bounds.maxX - sp.bounds.minX, sp.bounds.maxZ - sp.bounds.minZ, 4);
  const DL = span / 2 + 1.5;
  for (const d of [[1, 0, '东'], [-1, 0, '西'], [0, 1, '南'], [0, -1, '北']]) {
    const tip = projectPoint(d[0] * DL, 0, d[1] * DL, env);
    g.beginPath();
    g.moveTo(org.x, org.y);
    g.lineTo(tip.x, tip.y);
    g.stroke();
    g.beginPath();
    g.arc(tip.x, tip.y, 3, 0, Math.PI * 2);
    g.fill();
    g.fillText(d[2], tip.x + (tip.x - org.x) * 0.12, tip.y + (tip.y - org.y) * 0.12);
  }
  g.beginPath();
  g.arc(org.x, org.y, 3.5, 0, Math.PI * 2);
  g.fillStyle = 'rgba(255,255,255,.9)';
  g.fill();
  g.restore();

  for (const item of pts) {
    const rgb = cssToRgb(colorOf(item.cube.block, item.cube.cat));
    const isHl = state.highlight === item.cube.block;
    const probK = (0.78 + 0.22 * Math.min(1, item.cube.prob)) * (isHl ? 1.12 : 1);
    item.faces = [];
    for (const face of FACES) {
      const rn = rotate(face.n[0], face.n[1], face.n[2]);
      if (rn.z > -0.02) continue;
      const quad = [];
      for (let i = 0; i < 4; i++) {
        const vi = face.v[i][0] | (face.v[i][1] << 1) | (face.v[i][2] << 2);
        quad.push(item.corners[vi]);
      }
      item.faces.push(quad);
      g.beginPath();
      for (let i = 0; i < 4; i++) {
        const p = quad[i];
        if (i === 0) g.moveTo(p.x, p.y); else g.lineTo(p.x, p.y);
      }
      g.closePath();
      const k = face.s * probK;
      g.fillStyle = 'rgb(' + Math.round(rgb[0] * k) + ',' + Math.round(rgb[1] * k) + ',' +
                    Math.round(rgb[2] * k) + ')';
      g.fill();
      if (!isHl) {
        g.strokeStyle = 'rgba(0,0,0,.28)';
        g.lineWidth = 0.5;
        g.stroke();
      }
    }
  }

  if (state.highlight && !state.hidden.has(state.highlight)) {
    const hlSet = new Set();
    const hlCells = [];
    for (const layer of sp.layers) {
      if (state.onlyLayer ? layer.y !== state.dispLayer : layer.y > state.dispLayer) continue;
      for (const c of layer.cells) {
        if (!c.all.some(o => o.block === state.highlight)) continue;
        hlSet.add(c.x + ',' + layer.y + ',' + c.z);
        hlCells.push({ x:c.x, y:layer.y, z:c.z });
      }
    }
    const edgePlanes = new Map();
    for (const h of hlCells) {
      for (const face of FACES) {
        const nx = h.x + face.n[0], ny = h.y + face.n[1], nz = h.z + face.n[2];
        if (hlSet.has(nx + ',' + ny + ',' + nz)) continue;
        const plane = face.n.join(',') + ':' +
          (face.n[0] !== 0 ? h.x + (face.n[0] > 0 ? 1 : 0)
           : face.n[1] !== 0 ? h.y + (face.n[1] > 0 ? 1 : 0)
           : h.z + (face.n[2] > 0 ? 1 : 0));
        const vs = face.v.map(v => (h.x + v[0]) + ',' + (h.y + v[1]) + ',' + (h.z + v[2]));
        for (let i = 0; i < 4; i++) {
          const a = vs[i], b = vs[(i + 1) % 4];
          const ek = a < b ? a + '|' + b : b + '|' + a;
          let m = edgePlanes.get(ek);
          if (!m) { m = new Map(); edgePlanes.set(ek, m); }
          m.set(plane, (m.get(plane) || 0) + 1);
        }
      }
    }
    g.save();
    g.lineWidth = 2;
    g.strokeStyle = 'rgba(255,206,0,.95)';
    g.shadowColor = 'rgba(255,206,0,.85)';
    g.shadowBlur = 6;
    g.beginPath();
    for (const entry of edgePlanes) {
      let internal = false;
      for (const cnt of entry[1].values()) {
        if (cnt >= 2) { internal = true; break; }
      }
      if (internal) continue;
      const parts = entry[0].split('|');
      const a = parts[0].split(',').map(Number);
      const b = parts[1].split(',').map(Number);
      const pa = projectPoint(a[0], a[1], a[2], env);
      const pb = projectPoint(b[0], b[1], b[2], env);
      g.moveTo(pa.x, pa.y);
      g.lineTo(pb.x, pb.y);
    }
    g.stroke();
    g.restore();
  }
}

function mixRgb(a, b, t) {
  t = Math.max(0, Math.min(1, t));
  return [Math.round(a[0] + (b[0] - a[0]) * t),
          Math.round(a[1] + (b[1] - a[1]) * t),
          Math.round(a[2] + (b[2] - a[2]) * t)];
}

function cssToRgb(s) {
  if (s.startsWith('#')) {
    const v = s.slice(1);
    const n = v.length === 3 ? v.split('').map(c => c + c).join('') : v;
    return [parseInt(n.slice(0,2),16), parseInt(n.slice(2,4),16), parseInt(n.slice(4,6),16)];
  }
  const open = s.indexOf('('), close = s.indexOf(')');
  if (open < 0 || close < 0) return [120,120,120];
  const parts = s.substring(open + 1, close).split(',');
  if (parts.length < 3) return [120,120,120];
  const hh = (parseFloat(parts[0]) || 0) / 360;
  const ss = Math.max(0, Math.min(1, (parseFloat(parts[1]) || 0) / 100));
  const ll = Math.max(0, Math.min(1, (parseFloat(parts[2]) || 0) / 100));
  const q = ll < 0.5 ? ll * (1 + ss) : ll + ss - ll * ss;
  const p = 2 * ll - q;
  const f = t => { t = (t + 1) % 1;
    if (t < 1/6) return p + (q - p) * 6 * t;
    if (t < 1/2) return q;
    if (t < 2/3) return p + (q - p) * (2/3 - t) * 6;
    return p; };
  return [Math.round(f(hh + 1/3) * 255), Math.round(f(hh) * 255), Math.round(f(hh - 1/3) * 255)];
}

render();
</script>
</body>
</html>
""".replace("__DATA__", json);
    }
}
