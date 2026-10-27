# J7 exact trap reproduction

Commit 245f9f59f01ca1f5a4226b14ab8e8c707bcaf896. Coordinates one-based; tile IDs zero-based.

Identity: `LD1-J-7-A18D992A`; pieces 9; base 12; margin 3; initial gates 13.

Initial legal IDs [0, 1, 2, 3, 4]; safe [0, 1, 2, 3, 4]; doomed [].

|ID|Row|Column|Direction|SwitchMask|
|---:|---:|---:|---|---:|
|0|3|1|LEFT|0|
|1|1|2|LEFT|0|
|2|4|4|DOWN|2|
|3|4|5|DOWN|0|
|4|2|2|LEFT|0|
|5|4|3|RIGHT|8|
|6|3|2|UP|8|
|7|3|3|LEFT|0|
|8|2|3|DOWN|0|

Full completion: [{id:0,row:3,col:1,exit:LEFT}, {id:1,row:1,col:2,exit:LEFT}, {id:2,row:4,col:4,exit:DOWN}, {id:3,row:4,col:5,exit:DOWN}, {id:4,row:2,col:2,exit:LEFT}, {id:5,row:4,col:3,exit:RIGHT}, {id:6,row:3,col:2,exit:UP}, {id:7,row:3,col:3,exit:LEFT}, {id:8,row:2,col:3,exit:DOWN}].

Trap: [{id:2,row:4,col:4,exit:DOWN}, {id:3,row:4,col:5,exit:DOWN}, {id:5,row:4,col:3,exit:RIGHT}]

RemainingMask 467; gates 7; legal remaining []; remaining actions 9; remaining pieces 6; undos to recovery 1; recovery+finish actions 8; fits true.

Before fatal choice: gates 15; legal [0, 1, 4, 5]; safe [0, 1, 4]; risky [5].

