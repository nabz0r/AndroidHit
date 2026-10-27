# Version 2 progression: reproducible before/after audit

Journey has 1,000 frozen G boards; Remix has 240 frozen H boards. C/R and B/Q retain their original 240-per-family layouts. Every solved board needs exactly N exits; the displayed allowance is N plus a fixed per-board mistake margin. Both policies allow free restart. No spending, ad, failure or individual player signal changes content or allowance.

The exact baseline chooses uniformly among legal exits, with no undo, powers or continuations. It is a deliberately naive technical policy, not human difficulty or a prediction of completion/retention. A separate seeded blind-tap simulation chooses uniformly among all present pieces and counts blocked taps against each version's own allowance (100 runs per board per version). Old/new comparisons cover the same first 240 positions; later Journey boards have no historical equivalent.

## Journey

| Chapter | Old legal-random success | V2 legal-random success | Old blind success | V2 blind success | V2 pieces | V2 budget | V2 margin | V2 mean choice points |
|---|---:|---:|---:|---:|---|---|---|---:|
|1|100.00%|57.85%|16.60%|11.90%|5–12|9–14|2–4|2.07|
|2|100.00%|51.83%|0.90%|0.20%|11–13|13–15|2–3|2.77|
|3|100.00%|38.09%|0.50%|0.07%|12–14|14–17|2–3|4.10|
|4|92.45%|25.85%|0.10%|0.03%|13–15|15–18|2–3|4.30|
|5|86.55%|23.82%|0.00%|0.00%|14–16|16–19|2–3|5.17|
|6|79.03%|18.31%|0.00%|0.00%|15–17|17–20|2–3|5.67|
|7|74.09%|13.30%|0.00%|0.00%|16–18|18–21|2–3|6.23|
|8|80.19%|11.08%|0.00%|0.00%|17–19|19–22|2–3|5.97|
|9|—|10.87%|—|0.00%|17–20|20–23|2–3|6.37|
|10|—|10.97%|—|0.00%|17–20|19–23|2–3|6.23|
|11|—|9.65%|—|0.00%|17–20|20–23|2–3|6.23|
|12|—|8.27%|—|0.00%|17–20|20–23|3–3|6.70|
|13|—|8.85%|—|0.00%|17–20|20–23|2–3|6.67|
|14|—|8.93%|—|0.00%|17–20|20–23|2–3|5.67|
|15|—|6.89%|—|0.00%|17–20|20–23|3–3|7.57|
|16|—|7.33%|—|0.00%|17–20|20–23|2–3|6.57|
|17|—|7.19%|—|0.00%|17–20|20–23|3–3|7.17|
|18|—|6.97%|—|0.00%|17–20|20–23|2–3|6.93|
|19|—|5.98%|—|0.00%|17–20|20–23|3–3|7.13|
|20|—|5.96%|—|0.00%|17–20|20–23|3–3|7.30|
|21|—|5.97%|—|0.00%|17–20|20–23|3–3|7.27|
|22|—|5.61%|—|0.00%|17–20|20–23|3–3|6.57|
|23|—|5.76%|—|0.00%|17–20|20–23|3–3|6.80|
|24|—|5.16%|—|0.00%|17–20|20–23|3–3|7.40|
|25|—|5.16%|—|0.00%|17–20|20–23|3–3|7.27|
|26|—|4.65%|—|0.00%|17–20|20–23|3–3|6.80|
|27|—|4.16%|—|0.00%|17–20|20–23|3–3|6.83|
|28|—|4.27%|—|0.00%|17–20|20–23|3–3|8.27|
|29|—|3.69%|—|0.00%|17–20|20–23|3–3|8.00|
|30|—|4.17%|—|0.00%|17–20|20–23|3–3|6.73|
|31|—|3.74%|—|0.00%|17–20|20–23|3–3|7.67|
|32|—|3.69%|—|0.00%|17–20|20–23|3–3|7.40|
|33|—|3.46%|—|0.00%|17–20|20–23|3–3|7.77|
|34|—|3.52%|—|0.00%|17–18|20–21|3–3|7.80|

Matched first 240 positions: exact legal-random success **89.04% → 30.02%**; blind taps **2.26% → 1.53%**.

Full V2 route: **1000 distinct layouts even after canonicalizing all eight rotations/reflections, translations, arrows, switch directions and gate states**. Mean exact legal-random success **11.96%**; blind-tap success **0.37%**.

## Remix

| Chapter | Old legal-random success | V2 legal-random success | Old blind success | V2 blind success | V2 pieces | V2 budget | V2 margin | V2 mean choice points |
|---|---:|---:|---:|---:|---|---|---|---:|
|1|79.75%|50.26%|0.43%|0.57%|10–13|13–16|2–4|2.67|
|2|86.87%|35.44%|0.00%|0.00%|13–15|15–18|2–3|3.23|
|3|75.78%|27.27%|0.00%|0.00%|14–16|16–19|2–3|4.67|
|4|52.34%|21.01%|0.00%|0.00%|15–17|17–20|2–3|5.13|
|5|46.61%|16.40%|0.00%|0.00%|16–18|18–21|2–3|5.87|
|6|45.69%|13.62%|0.00%|0.00%|17–19|19–22|2–3|6.07|
|7|33.66%|9.35%|0.00%|0.00%|18–20|20–23|2–3|6.93|
|8|42.99%|7.01%|0.00%|0.00%|19–20|22–23|3–3|6.80|

Matched first 240 positions: exact legal-random success **57.96% → 22.55%**; blind taps **0.05% → 0.07%**.

Full V2 route: **240 distinct layouts even after canonicalizing all eight rotations/reflections, translations, arrows, switch directions and gate states**. Mean exact legal-random success **22.55%**; blind-tap success **0.07%**.

Legacy attempts restore legacyGet/legacyRemix with MoveBudget.restoreLegacy and policy 1 until completion/abandonment. New attempts use get/remix and policy 2. Earned progression and inventory are not reset. G/H codes prevent historical B/Q shares from silently opening different boards. Exact model measurements do not establish player enjoyment or a calibrated human difficulty curve.
