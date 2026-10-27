# J8 exact trap reproduction

Commit 245f9f59f01ca1f5a4226b14ab8e8c707bcaf896. Coordinates one-based; tile IDs zero-based.

Identity: `LD1-J-8-313284BB`; pieces 10; base 14; margin 4; initial gates 13.

Initial legal IDs [0, 1, 3, 8]; safe [0, 1, 3, 8]; doomed [].

|ID|Row|Column|Direction|SwitchMask|
|---:|---:|---:|---|---:|
|0|2|2|UP|0|
|1|2|1|LEFT|0|
|2|2|3|LEFT|0|
|3|5|1|DOWN|2|
|4|3|1|DOWN|0|
|5|2|4|LEFT|4|
|6|3|2|LEFT|0|
|7|3|4|LEFT|4|
|8|5|3|DOWN|0|
|9|4|3|DOWN|0|

Full completion: [{id:0,row:2,col:2,exit:UP}, {id:1,row:2,col:1,exit:LEFT}, {id:2,row:2,col:3,exit:LEFT}, {id:3,row:5,col:1,exit:DOWN}, {id:4,row:3,col:1,exit:DOWN}, {id:5,row:2,col:4,exit:LEFT}, {id:6,row:3,col:2,exit:LEFT}, {id:7,row:3,col:4,exit:LEFT}, {id:8,row:5,col:3,exit:DOWN}, {id:9,row:4,col:3,exit:DOWN}].

Trap: [{id:0,row:2,col:2,exit:UP}, {id:1,row:2,col:1,exit:LEFT}, {id:2,row:2,col:3,exit:LEFT}, {id:5,row:2,col:4,exit:LEFT}]

RemainingMask 984; gates 9; legal remaining []; remaining actions 10; remaining pieces 6; undos to recovery 1; recovery+finish actions 8; fits true.

Before fatal choice: gates 13; legal [3, 5, 8]; safe [3, 8]; risky [5].

