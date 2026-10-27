#!/usr/bin/env python3
"""Standalone technical model figures. Requires matplotlib; never controls Android."""
import csv
from pathlib import Path
from statistics import mean
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parents[1]
rows = list(csv.DictReader((ROOT / "docs/data/progression-v3-audit.csv").open()))
BG, PANEL, TEXT, MUTED = "#101629", "#19213a", "#f6f4ff", "#aeb8d0"
CYAN, PURPLE, GOLD, GRAY = "#77e5cf", "#b8a4ff", "#ffd795", "#8a95b0"
plt.rcParams.update({"font.family": "DejaVu Sans", "font.size": 10,
                     "svg.fonttype": "none", "axes.titleweight": "bold"})
fig, axes = plt.subplots(2, 2, figsize=(15, 9), facecolor=BG)
for ax in axes.flat:
    ax.set_facecolor(PANEL)
    ax.tick_params(colors=MUTED)
    ax.xaxis.label.set_color(MUTED)
    ax.yaxis.label.set_color(MUTED)
    ax.title.set_color(TEXT)
    for spine in ax.spines.values():
        spine.set_color("#34405a")
    ax.grid(axis="y", color="#34405a", linewidth=0.6, alpha=0.5)


def first_route(ax, family, count, title):
    selected = [row for row in rows if row["family"] == family and int(row["number"]) <= count]
    x = [int(row["number"]) for row in selected]
    ax.plot(x, [float(row["v2_exact_legal"]) * 100 for row in selected], color=GRAY,
            linewidth=1.4, alpha=0.8, label="Frozen V2 at the same position")
    ax.plot(x, [float(row["v3_exact_legal"]) * 100 for row in selected], color=CYAN,
            linewidth=1.7, label="V3 authored waves")
    keys = [row for row in selected if int(row["keys"]) > 0]
    ax.scatter([int(row["number"]) for row in keys],
               [float(row["v3_exact_legal"]) * 100 for row in keys],
               marker="D", color=GOLD, edgecolor=BG, linewidth=0.6, s=30, zorder=4,
               label="Key / seal board")
    ax.set(title=title, xlabel="Board number", ylabel="Exact naive legal success (%)", ylim=(-3, 105))
    legend = ax.legend(loc="upper right", fontsize=8, frameon=False)
    for text in legend.get_texts():
        text.set_color(TEXT)


first_route(axes[0, 0], "journey", 60, "A  Journey: teaching, practice, peaks and recovery")
first_route(axes[1, 0], "remix", 60, "C  Remix: dual gates with sparse keys")

journey = [row for row in rows if row["family"] == "journey"]
ax = axes[0, 1]
for beat, color, label in [("RECOVERY", CYAN, "V3 recovery"), ("PEAK", PURPLE, "V3 peak")]:
    points = []
    for start in range(1, 1001, 100):
        group = [row for row in journey if start <= int(row["number"]) < start + 100 and row["beat"] == beat]
        points.append((start + 49.5, mean(float(row["v3_exact_legal"]) for row in group) * 100))
    ax.plot([point[0] for point in points], [point[1] for point in points],
            color=color, marker="o", markersize=4, linewidth=2, label=label)
baseline = [(start + 49.5, mean(float(row["v2_exact_legal"]) for row in journey
                              if start <= int(row["number"]) < start + 100) * 100)
            for start in range(1, 1001, 100)]
ax.plot([point[0] for point in baseline], [point[1] for point in baseline],
        color=GRAY, linestyle="--", linewidth=1.4, label="V2 mean, all beats")
ax.set(title="B  Full Journey: 100-board windows", xlabel="Board number",
       ylabel="Mean exact naive legal success (%)", ylim=(0, 65))
legend = ax.legend(frameon=False, fontsize=9)
for text in legend.get_texts():
    text.set_color(TEXT)

ax = axes[1, 1]
labels = ["Practice", "Peak", "Recovery"]
beats = ["PRACTICE", "PEAK", "RECOVERY"]
x = list(range(3))
for offset, key, color, label in [(-0.18, "v2_exact_legal", GRAY, "V2 same positions"),
                                  (0.18, "v3_exact_legal", CYAN, "V3")]:
    values = [mean(float(row[key]) for row in journey if row["beat"] == beat) * 100 for beat in beats]
    bars = ax.bar([value + offset for value in x], values, width=0.34, color=color, label=label)
    for bar, value in zip(bars, values):
        ax.text(bar.get_x() + bar.get_width() / 2, value + 0.35, f"{value:.1f}%",
                ha="center", color=TEXT, fontsize=9)
ax.set_xticks(x, labels)
ax.set(title="D  Journey: measured separation of authored beats",
       ylabel="Mean exact naive legal success (%)", ylim=(0, 18))
legend = ax.legend(loc="upper left", frameon=False, fontsize=9)
for text in legend.get_texts():
    text.set_color(TEXT)

fig.suptitle("Luma Drift · V3 progression model", x=0.06, y=0.98, ha="left",
             color=TEXT, fontsize=22, fontweight="bold")
fig.text(0.06, 0.935, "1,000 Journey boards + 240 Remix boards · exact subset enumeration · deterministic content",
         color=MUTED, fontsize=11)
fig.text(0.06, 0.025,
         "Technical baseline only: uniform legal choices, no powers, undo, purchases or continuations. "
         "These curves do not measure human difficulty or retention.", color=MUTED, fontsize=9)
fig.subplots_adjust(left=0.065, right=0.98, top=0.88, bottom=0.10, hspace=0.42, wspace=0.24)
fig.savefig(ROOT / "docs/data/progression-v3-curves.svg", facecolor=BG)
fig.savefig(ROOT / "docs/data/progression-v3-curves.png", facecolor=BG, dpi=150)
print("Saved standalone progression-v3-curves.svg and .png")
