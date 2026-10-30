package cognitive_lab;

import java.util.*;

/** Synthetic dimensions, independently stratified. No human calibration is claimed. */
final class Profile {
    final String id,label;
    final int planningDepth,searchCap,workingMemory;final double iq;
    final double attentionLapse,learningRate,forgettingRate,fatigueRate,frustrationSensitivity;
    final double timeAvailabilityMinutes,rewardSensitivity,puzzleInterest,collectionInterest,explorationInterest;
    Profile(int index,double[] u,double iq) {
        id=String.format(Locale.ROOT,"P%03d",index+1);label="Synthetic profile "+(index+1);this.iq=iq;
        planningDepth=1+(int)(u[0]*5);searchCap=12+(int)(u[1]*149);workingMemory=2+(int)(u[2]*11);
        attentionLapse=.015+.22*u[3];learningRate=.035+.23*u[4];forgettingRate=.008+.16*u[5];
        fatigueRate=.014+.055*u[6];frustrationSensitivity=.35+1.25*u[7];timeAvailabilityMinutes=4+22*u[8];
        rewardSensitivity=.05+.95*u[9];puzzleInterest=.15+.85*u[10];collectionInterest=u[11];explorationInterest=u[12];
    }
    static List<Profile> generate(int count,long seed) {
        Random random=new Random(seed);double[][] units=new double[count][13];
        for(int d=0;d<13;d++) {List<Integer> bins=new ArrayList<>();for(int i=0;i<count;i++)bins.add(i);Collections.shuffle(bins,random);
            for(int i=0;i<count;i++)units[i][d]=(bins.get(i)+random.nextDouble())/count;}
        List<Double> labels=new ArrayList<>();for(int i=0;i<count;i++)labels.add(70+69d*i/Math.max(1,count-1));Collections.shuffle(labels,random);
        List<Profile> result=new ArrayList<>();for(int i=0;i<count;i++)result.add(new Profile(i,units[i],labels.get(i)));return result;
    }
    Map<String,Object> json() {return Json.obj("id",id,"label",label,"parameters",Json.obj(
        "planningDepth",planningDepth,"searchCap",searchCap,"workingMemory",workingMemory,"attentionLapse",attentionLapse,
        "learningRate",learningRate,"forgettingRate",forgettingRate,"fatigueRate",fatigueRate,"frustrationSensitivity",frustrationSensitivity,
        "timeAvailabilityMinutes",timeAvailabilityMinutes,"rewardSensitivity",rewardSensitivity,"puzzleInterest",puzzleInterest,
        "collectionInterest",collectionInterest,"explorationInterest",explorationInterest),
        "iqLabelHypothesis",Json.obj("value",iq,"calibrated",false,"causal",false,"description","Independent fictional label, never read by the policy; not a psychometric measurement"));}
}
