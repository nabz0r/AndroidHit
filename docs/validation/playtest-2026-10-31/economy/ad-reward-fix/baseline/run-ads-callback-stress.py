#!/usr/bin/env python3
"""Compile actual AdsController against bounded fake SDK callbacks; no SDK network calls."""
import pathlib,subprocess,tempfile,json,hashlib,datetime,os
R=pathlib.Path(__file__).resolve().parents[2]
stubs={
'android/app/Activity.java':'package android.app;public abstract class Activity extends android.content.Context{public boolean isFinishing(){return false;}public boolean isDestroyed(){return false;}}',
'android/os/Looper.java':'package android.os;public class Looper{static final Looper MAIN=new Looper();public static Looper getMainLooper(){return MAIN;}public static Looper myLooper(){return MAIN;}}',
'android/os/Handler.java':'package android.os;public class Handler{final Looper l;public Handler(Looper l){this.l=l;}public Looper getLooper(){return l;}public boolean post(Runnable r){r.run();return true;}}',
'com/google/android/libraries/ads/mobile/sdk/MobileAds.java':'package com.google.android.libraries.ads.mobile.sdk;public class MobileAds{public static void initialize(android.content.Context c,Object config,java.util.function.Consumer<Object> callback){callback.accept(new Object());}}',
'com/google/android/libraries/ads/mobile/sdk/common/AdLoadCallback.java':'package com.google.android.libraries.ads.mobile.sdk.common;public abstract class AdLoadCallback<T>{public abstract void onAdLoaded(T ad);public abstract void onAdFailedToLoad(LoadAdError error);}',
'com/google/android/libraries/ads/mobile/sdk/common/AdRequest.java':'package com.google.android.libraries.ads.mobile.sdk.common;public class AdRequest{public static class Builder{public Builder(String id){}public AdRequest build(){return new AdRequest();}}}',
'com/google/android/libraries/ads/mobile/sdk/common/AdValue.java':'package com.google.android.libraries.ads.mobile.sdk.common;public class AdValue{public long getValueMicros(){return 0;}public String getCurrencyCode(){return "test";}public Object getPrecisionType(){return "fixture";}}',
'com/google/android/libraries/ads/mobile/sdk/common/FullScreenContentError.java':'package com.google.android.libraries.ads.mobile.sdk.common;public class FullScreenContentError{}',
'com/google/android/libraries/ads/mobile/sdk/common/LoadAdError.java':'package com.google.android.libraries.ads.mobile.sdk.common;public class LoadAdError{}',
'com/google/android/libraries/ads/mobile/sdk/initialization/InitializationConfig.java':'package com.google.android.libraries.ads.mobile.sdk.initialization;public class InitializationConfig{public static class Builder{public Builder(String id){}public Builder disableSdkCrashReporting(){return this;}public InitializationConfig build(){return new InitializationConfig();}}}',
'com/google/android/libraries/ads/mobile/sdk/rewarded/RewardedAdEventCallback.java':'package com.google.android.libraries.ads.mobile.sdk.rewarded;import com.google.android.libraries.ads.mobile.sdk.common.*;public class RewardedAdEventCallback{public void onAdShowedFullScreenContent(){}public void onAdDismissedFullScreenContent(){}public void onAdFailedToShowFullScreenContent(FullScreenContentError e){}public void onAdImpression(){}public void onAdPaid(AdValue v){}}',
'com/google/android/libraries/ads/mobile/sdk/rewarded/RewardedAd.java':'''package com.google.android.libraries.ads.mobile.sdk.rewarded;import com.google.android.libraries.ads.mobile.sdk.common.*;public class RewardedAd{public static volatile int loads;static volatile AdLoadCallback<RewardedAd> pending;public RewardedAdEventCallback callbacks;java.util.function.Consumer<Object> reward;public int shows,destroyed;public static void load(AdRequest request,AdLoadCallback<RewardedAd> cb){pending=cb;loads++;}public static RewardedAd deliver(){RewardedAd ad=new RewardedAd();pending.onAdLoaded(ad);pending=null;return ad;}public void setAdEventCallback(RewardedAdEventCallback c){callbacks=c;}public void show(android.app.Activity owner,java.util.function.Consumer<Object> cb){shows++;reward=cb;callbacks.onAdShowedFullScreenContent();}public void earn(){reward.accept(new Object());}public void dismiss(){callbacks.onAdDismissedFullScreenContent();}public void destroy(){destroyed++;}}''',
'com/google/android/ump/ConsentInformation.java':'''package com.google.android.ump;public class ConsentInformation{public enum PrivacyOptionsRequirementStatus{REQUIRED,NOT_REQUIRED}public boolean canRequestAds(){return true;}public PrivacyOptionsRequirementStatus getPrivacyOptionsRequirementStatus(){return PrivacyOptionsRequirementStatus.NOT_REQUIRED;}public void requestConsentInfoUpdate(android.app.Activity a,ConsentRequestParameters p,Runnable ok,java.util.function.Consumer<Object> fail){ok.run();}}''',
'com/google/android/ump/ConsentRequestParameters.java':'package com.google.android.ump;public class ConsentRequestParameters{public static class Builder{public Builder setTagForUnderAgeOfConsent(boolean b){return this;}public ConsentRequestParameters build(){return new ConsentRequestParameters();}}}',
'com/google/android/ump/UserMessagingPlatform.java':'package com.google.android.ump;public class UserMessagingPlatform{public static ConsentInformation getConsentInformation(android.app.Activity a){return new ConsentInformation();}public static void loadAndShowConsentFormIfRequired(android.app.Activity a,java.util.function.Consumer<Object> cb){cb.accept(null);}public static void showPrivacyOptionsForm(android.app.Activity a,java.util.function.Consumer<Object> cb){cb.accept(null);}}'
}
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 import argparse
 p=argparse.ArgumentParser();p.add_argument('--output',required=True,type=pathlib.Path);p.add_argument('--sessions',type=int,default=10000);a=p.parse_args();out=a.output.resolve();out.mkdir(parents=True,exist_ok=False)
 java=pathlib.Path(os.environ.get('JAVA_HOME','/opt/homebrew/opt/openjdk@17'))/'bin'
 sources=sorted([*R.glob('scripts/test-support/android/annotation/*.java'),*R.glob('scripts/test-support/android/content/*.java'),*R.glob('scripts/test-support/android/graphics/*.java'),*R.glob('app/src/main/java/com/androidhit/lumadrift/data/*.java'),*R.glob('app/src/main/java/com/androidhit/lumadrift/engine/*.java'),R/'scripts/playtest-2026-10-31/EconomyStateStress.java',R/'scripts/playtest-2026-10-31/AdsCallbackStress.java',R/'app/src/main/java/com/androidhit/lumadrift/commerce/AdsController.java',R/'app/src/main/java/com/androidhit/lumadrift/commerce/CommerceConfig.java',R/'app/build/generated/source/buildConfig/production/release/com/androidhit/lumadrift/BuildConfig.java'])
 receipt={'status':'RUNNING','scope':'Controlled host callback test, no actual ads or consent/network.','networkRequested':False,'recordedAtUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'sourceInputs':{str(p.relative_to(R)):sha(p) for p in sources},'sdkStubSources':stubs,'runnerSha256':sha(pathlib.Path(__file__))}
 try:
  with tempfile.TemporaryDirectory(prefix='luma123-ads-callback-')as t:
   tmp=pathlib.Path(t);generated=[]
   for name,s in stubs.items():f=tmp/name;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(s);generated.append(f)
   for name,cmd in [('compile',[str(java/'javac'),'-Xlint:all','-d',str(tmp/'classes'),*map(str,sources+generated)]),('run',[str(java/'java'),'-Xmx512m','-cp',str(tmp/'classes'),'AdsCallbackStress',str(out/'result'),str(a.sessions)])]:
    q=subprocess.run(cmd,cwd=R,capture_output=True);(out/(name+'.stdout.log')).write_bytes(q.stdout);(out/(name+'.stderr.log')).write_bytes(q.stderr)
    if q.returncode:raise RuntimeError(f'{name} exit {q.returncode}; originals preserved')
    print(q.stdout.decode(),end='')
  assert all(sha(R/p)==h for p,h in receipt['sourceInputs'].items()),'source drift'
  receipt['status']='PASS_WITH_FINDING'
 except BaseException as e:receipt['status']='FAIL';receipt['error']=str(e);raise
 finally:
  receipt['outputs']={str(p.relative_to(out)):{'sha256':sha(p),'bytes':p.stat().st_size} for p in sorted(out.rglob('*')) if p.is_file()};(out/'receipt.json').write_text(json.dumps(receipt,indent=2)+'\n')
if __name__=='__main__':main()
