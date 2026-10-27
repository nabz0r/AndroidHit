# J13 exact trap reproduction

Commit 245f9f59f01ca1f5a4226b14ab8e8c707bcaf896. Coordinates one-based; tile IDs zero-based.

Identity: `LD1-J-D-6881B9AC`; pieces 10; base 13; margin 3; initial gates 7.

Initial legal IDs [0, 1, 4, 6]; safe [0, 1, 4, 6]; doomed [].

|ID|Row|Column|Direction|SwitchMask|
|---:|---:|---:|---|---:|
|0|5|3|DOWN|0|
|1|5|1|DOWN|8|
|2|5|2|LEFT|0|
|3|5|4|LEFT|0|
|4|1|3|UP|0|
|5|1|5|LEFT|0|
|6|2|5|RIGHT|0|
|7|4|5|UP|1|
|8|4|4|DOWN|1|
|9|4|3|RIGHT|0|

Full completion: [{id:0,row:5,col:3,exit:DOWN}, {id:1,row:5,col:1,exit:DOWN}, {id:2,row:5,col:2,exit:LEFT}, {id:3,row:5,col:4,exit:LEFT}, {id:4,row:1,col:3,exit:UP}, {id:5,row:1,col:5,exit:LEFT}, {id:6,row:2,col:5,exit:RIGHT}, {id:7,row:4,col:5,exit:UP}, {id:8,row:4,col:4,exit:DOWN}, {id:9,row:4,col:3,exit:RIGHT}].

Trap: [{id:0,row:5,col:3,exit:DOWN}, {id:1,row:5,col:1,exit:DOWN}, {id:2,row:5,col:2,exit:LEFT}, {id:3,row:5,col:4,exit:LEFT}, {id:8,row:4,col:4,exit:DOWN}]

RemainingMask 752; gates 14; legal remaining [6]; remaining actions 8; remaining pieces 5; undos to recovery 1; recovery+finish actions 7; fits true.

Before fatal choice: gates 15; legal [4, 6, 8]; safe [4, 6]; risky [8].

Trap: [{id:0,row:5,col:3,exit:DOWN}, {id:1,row:5,col:1,exit:DOWN}, {id:2,row:5,col:2,exit:LEFT}, {id:3,row:5,col:4,exit:LEFT}, {id:8,row:4,col:4,exit:DOWN}]

RemainingMask 752; gates 14; legal remaining [6]; remaining actions 8; remaining pieces 5; undos to recovery 1; recovery+finish actions 7; fits true.

Before fatal choice: gates 15; legal [4, 6, 8]; safe [4, 6]; risky [8].

