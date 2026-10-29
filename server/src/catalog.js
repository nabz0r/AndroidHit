// Fixed prices are part of the paid-wallet contract. Local progression is not proof.
export const PRODUCTS = Object.freeze({ luma_gold_500: 500, luma_gold_1400: 1400, luma_gold_3600: 3600 });
const characters = { miko:160, orbit:220, pip:300, astra:420, zig:600, lune:180, fenn:240, hoot:280, buzz:320, tula:360, jelli:400, vela:440, nimbi:480, soli:520, mochi:560, nixi:620, prickly:680, quill:740, waddles:800, halo:860, emberling:920, bambu:1000, inky:1100 };
const skins = { lagoon:180, blossom:240, ember:300, midnight:360, aurora:450 };
const boosters = { hint:[45,3], vision:[40,3], flow:[60,2], shield:[70,2], focus:[80,2], trail:[90,2] };
export const SKUS = Object.freeze(Object.fromEntries([
  ...Object.entries(characters).map(([itemId,price]) => [`character:${itemId}`, { kind:'character', itemId, amount:1, price }]),
  ...Object.entries(skins).map(([itemId,price]) => [`skin:${itemId}`, { kind:'skin', itemId, amount:1, price }]),
  ...Object.entries(boosters).map(([itemId,[price,amount]]) => [`booster:${itemId}`, { kind:'booster', itemId, amount, price }]),
  ['continue_5', { kind:'continuation', itemId:'continue_5', amount:1, price:100 }],
  // One durable refill pack, redeemable for ten session units. No Coins price.
  ['energy_refill10', { kind:'energy', itemId:'energy_refill10', amount:10, price:50 }]
]));
