/* Shared schema and export helpers. No DOM, network, storage or dependencies. */
(function (root) {
  'use strict';
  var rates=['completionRate','blockedRate','modeledD1','modeledD7'];
  var measures=rates.concat(['meanLevelsCleared','meanActions','meanSessionMinutes','meanOverrunMinutes']);
  function object(value){return value!==null&&typeof value==='object'&&!Array.isArray(value);}
  function finite(value){return typeof value==='number'&&Number.isFinite(value);}
  function fail(){throw new Error('invalidSchema');}
  function finiteTree(value,depth,budget){if(depth>60||--budget.left<0)fail();if(typeof value==='number'&&!finite(value))fail();if(Array.isArray(value)){value.forEach(function(v){finiteTree(v,depth+1,budget);});}else if(object(value)){Object.keys(value).forEach(function(k){if(k==='__proto__'||k==='constructor'||k==='prototype')fail();finiteTree(value[k],depth+1,budget);});}}
  function label(value){return typeof value==='string'||object(value)&&Object.values(value).every(function(x){return typeof x==='string';});}
  function identifier(value){return typeof value==='string'&&value.length>0&&value.length<=200;}
  function rows(value,key){if(!object(value)||value.schemaVersion!==undefined&&value.schemaVersion!==1||!Array.isArray(value[key]))fail();return value[key];}
  function validate(input){
    if(!object(input)||input.schemaVersion!==undefined&&input.schemaVersion!==1||!object(input.summary)||input.summary.schemaVersion!==1)fail();
    finiteTree(input,0,{left:1000000});
    var summary=input.summary;if(!Array.isArray(summary.scenarios)||!summary.scenarios.length)throw new Error('noScenarios');
    if(summary.scope!==undefined){if(!object(summary.scope))fail();var scope=summary.scope;if(scope.modeled!==undefined&&scope.modeled!==true||scope.humanParticipants!==undefined&&scope.humanParticipants!==0||scope.clinicalAssessment!==undefined&&scope.clinicalAssessment!==false||scope.humanRetentionPrediction!==undefined&&scope.humanRetentionPrediction!==false||scope.iqCalibration!==undefined&&scope.iqCalibration!==false)fail();}
    if(summary.totals&&summary.totals.humanParticipants!==undefined&&summary.totals.humanParticipants!==0)fail();
    if(summary.scenarios.length>1000)fail();
    var ids=new Set();summary.scenarios.forEach(function(s){if(!object(s)||!identifier(s.id)||ids.has(s.id)||s.label!==undefined&&!label(s.label)||!Number.isSafeInteger(s.trajectories)||s.trajectories<0)fail();ids.add(s.id);measures.forEach(function(k){if(!finite(s[k])||s[k]<0||rates.includes(k)&&s[k]>1)fail();});});
    var profiles=input.profiles||{schemaVersion:1,profiles:[]},paired=input.paired||input.pairedComparisons||{schemaVersion:1,comparisons:[]},traces=input.traces||{schemaVersion:1,traces:[]};
    var profileIds=new Set();rows(profiles,'profiles').forEach(function(p){if(!object(p)||!identifier(p.id)||profileIds.has(p.id)||p.label!==undefined&&!label(p.label)||!object(p.parameters))fail();profileIds.add(p.id);Object.values(p.parameters).forEach(function(v){if(!finite(v)||v<0)fail();});if(p.iqLabelHypothesis!==undefined&&(!object(p.iqLabelHypothesis)||!finite(p.iqLabelHypothesis.value)||p.iqLabelHypothesis.calibrated!==false||p.iqLabelHypothesis.causal!==false))fail();});
    if(summary.profileResults!==undefined){if(!Array.isArray(summary.profileResults))fail();summary.profileResults.forEach(function(p){if(!object(p)||!identifier(p.profileId)||!ids.has(p.scenario)||profileIds.size&&!profileIds.has(p.profileId))fail();measures.forEach(function(k){if(p[k]!==undefined&&(!finite(p[k])||p[k]<0||rates.includes(k)&&p[k]>1))fail();});});}
    rows(paired,'comparisons').forEach(function(c){if(!object(c)||!ids.has(c.scenario)||!ids.has(c.baseline)||!object(c.metrics))fail();Object.values(c.metrics).forEach(function(m){if(!object(m)||!finite(m.meanDifference)||m.ci95!==undefined&&(!Array.isArray(m.ci95)||m.ci95.length!==2||m.ci95.some(function(x){return !finite(x);})||m.ci95[0]>m.ci95[1])||m.pairs!==undefined&&(!Number.isSafeInteger(m.pairs)||m.pairs<0))fail();});});
    rows(traces,'traces').forEach(function(t){if(!object(t)||!identifier(t.profileId)||!ids.has(t.scenario)||profileIds.size&&!profileIds.has(t.profileId)||!object(t.decision)||!object(t.observation)||!object(t.outcome))fail();if(t.decision.candidates!==undefined&&(!Array.isArray(t.decision.candidates)||t.decision.candidates.some(function(c){return !object(c)||!finite(c.value);})) )fail();});
    return {summary:summary,profiles:profiles,paired:paired,traces:traces};
  }
  function csvValue(value){var str=String(value===undefined||value===null?'':value);/* Prevent spreadsheet formula execution when users open downloaded CSV. */if(/^[=+@\-\t\r]/.test(str)&&typeof value!=='number')str="'"+str;return '"'+str.replace(/"/g,'""')+'"';}
  function makeCSV(rows){if(!rows.length)return '';var keys=Array.from(new Set(rows.reduce(function(result,row){return result.concat(Object.keys(row));},[])));return '\ufeff'+keys.map(csvValue).join(',')+'\r\n'+rows.map(function(row){return keys.map(function(k){return csvValue(typeof row[k]==='object'?JSON.stringify(row[k]):row[k]);}).join(',');}).join('\r\n')+'\r\n';}
  function barPercent(value,max){if(!finite(value)||!finite(max)||max<=0)return 0;return Math.max(0,Math.min(100,value/max*100));}
  function formatDuration(minutes,lang){if(!finite(minutes))return '—';var seconds=Math.abs(minutes)<1,value=seconds?minutes*60:minutes;var options=value!==0&&Math.abs(value)<0.01?{notation:'scientific',maximumSignificantDigits:2}:{maximumFractionDigits:2};return new Intl.NumberFormat(lang==='fr'?'fr-FR':'en-GB',options).format(value)+' '+(seconds?'s':'min');}
  var api={validate:validate,makeCSV:makeCSV,barPercent:barPercent,formatDuration:formatDuration};if(typeof module==='object'&&module.exports)module.exports=api;else root.LumaCognitiveCore=api;
}(typeof window==='object'?window:globalThis));
