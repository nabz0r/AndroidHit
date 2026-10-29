// Browser-only teaching model. It neither loads nor writes an Android account/save.
export const COPY_THRESHOLDS=Object.freeze([1,2,8,14,22,32,44,58,74,92]);
export const FORM_LEVELS=Object.freeze([1,3,6,10]);
export const ARRIVAL_PRICE=180;
function checkedLevel(level){if(!Number.isInteger(level)||level<1||level>1000)throw new RangeError('U level1..1000 required');return level;}
export function rewardMultiplier(level){return 1+Math.log1p(Math.max(0,checkedLevel(level)-10)/25)/Math.log1p(990/25);}
export function boostMultiplier(level){return 1+.4*Math.log1p(Math.max(0,checkedLevel(level)-10)/50)/Math.log1p(990/50);}
export function campaignReward(level,stars){if(!Number.isInteger(stars)||stars<0||stars>3)throw new RangeError('Stars0..3 required');checkedLevel(level);return stars===0?0:Math.round((25+10*stars)*rewardMultiplier(level));}
export function boostPrice(base,level){checkedLevel(level);return base<=0?0:Math.min(2147483647,Math.ceil(base*boostMultiplier(level)));}
export function undoPrice(level,paidIndex=1){if(!Number.isInteger(paidIndex)||paidIndex<1)throw new RangeError('Paid Undo index starts at1');return Math.min(40,Math.ceil(8+4*Math.log1p(checkedLevel(level)/25)+4*Math.log1p(paidIndex)));}
export function energyCapacity(level){return 10+Math.floor(6*Math.log1p(Math.max(0,checkedLevel(level)-10)/50)/Math.log1p(990/50));}
export const ARRIVAL_POOL=Object.freeze(['miko','orbit','pip','astra','zig','lune','fenn','hoot','buzz','tula','jelli','vela','nimbi','soli','mochi','nixi','prickly','quill','waddles','halo','emberling','bambu','inky'].map((id,index)=>Object.freeze({id,chance:index<10?600:index<18?375:200,duplicateCoins:index<10?45:index<18?75:120})));
export const INITIAL_TILES=Object.freeze([
  {id:1,row:0,col:0,dir:'up',shell:'ice',layers:2},
  {id:2,row:0,col:2,dir:'up',shell:'water',layers:1},
  {id:3,row:1,col:1,dir:'left',shell:'fire',layers:2},
  {id:4,row:1,col:3,dir:'right',shell:'',layers:0},
  {id:5,row:2,col:0,dir:'up',shell:'',layers:0},
  {id:6,row:2,col:2,dir:'right',shell:'ice',layers:2},
  {id:7,row:3,col:1,dir:'up',shell:'',layers:0},
  {id:8,row:3,col:3,dir:'down',shell:'',layers:0}
]);
export function levelForCopies(copies){return COPY_THRESHOLDS.reduce((level,threshold)=>copies>=threshold?level+1:level,0);}
export function formForCopies(copies){let form=0;FORM_LEVELS.forEach((level,index)=>{if(levelForCopies(copies)>=level)form=index;});return form;}
export function drawForRoll(roll){if(!Number.isInteger(roll)||roll<0||roll>=10000)throw new RangeError('Uniform roll 0..9999 required');for(const item of ARRIVAL_POOL){if(roll<item.chance)return item;roll-=item.chance;}throw new Error('Invalid source pool');}
export function createState(){return {view:'home',coins:180,xp:85,gold:null,avatar:'nova',copies:{nova:7,miko:1},tiles:INITIAL_TILES.map(tile=>({...tile})),exits:[],chain:0,corrections:0,charge:3,guideUsed:false,focusEarned:false,focusUsed:false,highlight:0,cleared:false,rewarded:false,expeditionRewarded:false,arrival:null,tab:'companions'};}
export function canExit(state,tile){const delta={up:[-1,0],right:[0,1],down:[1,0],left:[0,-1]}[tile.dir];let row=tile.row+delta[0],col=tile.col+delta[1];while(row>=0&&row<4&&col>=0&&col<4){if(state.tiles.some(other=>other.row===row&&other.col===col))return false;row+=delta[0];col+=delta[1];}return true;}
export function safeTile(state){return state.tiles.find(tile=>canExit(state,tile));}
export function tapTile(state,id){const tile=state.tiles.find(tile=>tile.id===id);if(!tile)return {kind:'missing'};state.highlight=0;if(tile.layers>0){tile.layers--;return {kind:tile.layers?'shell':'free',shell:tile.shell,left:tile.layers};}if(!canExit(state,tile)){state.corrections++;state.chain=0;return {kind:'blocked'};}state.tiles=state.tiles.filter(other=>other.id!==id);state.exits.push(id);state.chain++;state.charge=Math.min(6,state.charge+1);if(state.chain>=3&&!state.focusEarned)state.focusEarned=true;const result={kind:'exit',focus:state.focusEarned&&!state.focusUsed};if(state.tiles.length===0){state.cleared=true;if(!state.rewarded){state.rewarded=true;state.coins+=45;state.xp+=35;result.kind='win';}}return result;}
export function useGuide(state){if(state.guideUsed||state.charge<2||state.cleared)return false;const tile=safeTile(state);if(!tile)return false;state.charge-=2;state.guideUsed=true;state.highlight=tile.id;return true;}
export function useFocus(state){if(!state.focusEarned||state.focusUsed||state.cleared)return false;const tile=safeTile(state);if(!tile)return false;state.focusUsed=true;state.highlight=tile.id;return true;}
export function targetedCopy(state){if(state.expeditionRewarded)return false;state.expeditionRewarded=true;state.copies.nova=(state.copies.nova||0)+1;return true;}
export function arrive(state,roll){if(state.coins<ARRIVAL_PRICE)return null;const item=drawForRoll(roll),before=state.copies[item.id]||0;state.coins-=ARRIVAL_PRICE;state.copies[item.id]=before+1;if(before)state.coins+=item.duplicateCoins;state.arrival={...item,duplicate:before>0};return state.arrival;}
