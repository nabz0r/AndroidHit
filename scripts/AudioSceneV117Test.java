package com.androidhit.lumadrift.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.SoundPool;
import android.os.Handler;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/** Actual PCM exports and a controlled JVM Android lifecycle model; no speaker playback claim. */
public final class AudioSceneV117Test {
    private static int checks;
    private static final float EFFECT_GAIN = .72f; // AudioEngine's initial SFX gain.
    private static final int PREVIEW_SECONDS = 10;
    private static final class Metrics {
        final double rms, dc;
        final int peak, seam, largestStep;
        Metrics(short[] pcm) {
            double energy=0,sum=0;int peak=0,step=0;
            for(int i=0;i<pcm.length;i++){
                int sample=pcm[i];sum+=sample;energy+=sample*(double)sample;
                peak=Math.max(peak,Math.abs(sample));if(i>0)step=Math.max(step,Math.abs(sample-pcm[i-1]));
            }
            this.rms=Math.sqrt(energy/pcm.length)/32767;this.dc=sum/pcm.length;
            this.peak=peak;this.seam=Math.abs(pcm[0]-pcm[pcm.length-1]);this.largestStep=step;
        }
    }
    public static void main(String[] args)throws Exception {
        if(args.length>1&&args[1].equals("--lifecycle-only")){int maximum=lifecycle();System.out.println("PASS "+checks+" scene lifecycle/concurrency model checks only; maximum live tracks="+maximum);return;}
        Path out=Path.of(args.length==0?"docs/validation/v1.17/audio/scenes":args[0]);Files.createDirectories(out);
        int dspBefore=checks;dsp(out);int dspChecks=checks-dspBefore;
        int lifecycleBefore=checks;int maximum=lifecycle();int lifecycleChecks=checks-lifecycleBefore;
        Files.writeString(out.resolve("scope.txt"),"PASS "+checks+" checks: "+dspChecks+" DSP and "+lifecycleChecks+" lifecycle/concurrency checks.\n"
                +"50 distinct original scene/biome PCM loops: 10 historical puzzle themes and 40 new place arrangements.\n"
                +"Four 10-second scene previews use biome0 and actual default music gain "+AudioEngine.DEFAULT_MUSIC_VOLUME+", with the engine's 900ms smooth entrance and a preview-only 450ms ending.\n"
                +"Three earcons use initial SFX gain0.72. All WAVs are mono PCM16 at "+Synth.RATE+" Hz, rendered from the app synthesizer.\n"
                +"Historical ten puzzle-theme hashes and all eleven historical effect hashes are checked against frozen v1.7 records.\n"
                +"JVM model: late/obsolete PCM writes, 1000 combined biome/scene requests, return to current scene, pause/focus, music/SFX mute independence, resume, failed output and release; maximum live AudioTracks="+maximum+".\n"
                +"No physical speaker recording, subjective listening result or Android native allocation/performance measurement is implied.\n");
        System.out.println("PASS "+checks+" audio scene checks ("+dspChecks+" DSP, "+lifecycleChecks+" lifecycle); 50 loops, 4 scene previews, 3 earcons; maximum live tracks="+maximum);
    }
    private static void dsp(Path out)throws Exception {
        check(AudioEngine.Scene.values().length==5,"Five stable scene identities");
        check(AudioEngine.Scene.PUZZLE.ordinal()==0&&AudioEngine.Scene.ATELIER.ordinal()==4,"Saved scene-independent puzzle arrangement remains scene0");
        Map<Integer,String> historical=new LinkedHashMap<>();List<String> baseline=Files.readAllLines(Path.of("docs/validation/v1.7/audio/audio-biome-metrics.csv"));
        for(String line:baseline.subList(1,baseline.size()))if(!line.isBlank()){String[] cells=line.split(",");historical.put(Integer.parseInt(cells[0]),cells[cells.length-1]);}
        check(historical.size()==10,"Ten frozen historical puzzle-theme witnesses");
        StringBuilder legacy=new StringBuilder("AMBIENT ").append(hash(Synth.ambientLoop())).append('\n');
        for(AudioEngine.Sound sound:java.util.EnumSet.range(AudioEngine.Sound.TAP,AudioEngine.Sound.LOSE))legacy.append(sound.name()).append(' ').append(hash(Synth.effect(sound.name()))).append('\n');
        check(legacy.toString().equals(Files.readString(Path.of("docs/validation/v1.7/audio/legacy-pcm.sha256"))),"All eleven historical effects and original puzzle theme preserve exact PCM");
        Set<String> hashes=new HashSet<>();StringBuilder csv=new StringBuilder("biome,scene,seconds,frames,pcm_bytes,rms,peak,dc_samples,seam_step,max_adjacent_step,sha256\n");
        int biggest=0;double largestDefaultMusicPeak=0;
        for(int biome=0;biome<10;biome++)for(AudioEngine.Scene scene:AudioEngine.Scene.values()){
            short[] pcm=Synth.sceneLoop(biome,scene.ordinal());String digest=hash(pcm);Metrics m=new Metrics(pcm);
            check(hashes.add(digest),"Distinct scene/biome PCM: "+biome+"/"+scene);
            check(Arrays.equals(pcm,Synth.sceneLoop(biome,scene.ordinal())),"Deterministic scene/biome rendering: "+biome+"/"+scene);
            if(scene==AudioEngine.Scene.PUZZLE)check(digest.equals(historical.get(biome)),"Historical puzzle theme byte identity: "+biome);
            double seconds=pcm.length/(double)Synth.RATE;
            check(seconds>=20&&seconds<=30,"Bounded eight-bar arrangement duration: "+biome+"/"+scene);
            check(m.rms>=.10&&m.rms<=.145,"Audible consistent RMS: "+biome+"/"+scene+" rms="+m.rms);
            check(m.peak<(scene==AudioEngine.Scene.PUZZLE?.621:.581)*32767,"Music leaves mix headroom: "+biome+"/"+scene);
            check(Math.abs(m.dc)<1,"No material music DC offset: "+biome+"/"+scene);
            check(m.seam<=m.largestStep&&m.seam<.012*32767,"Wrapped loop seam is bounded: "+biome+"/"+scene);
            check(pcm.length*2<1_300_000,"Bounded single-loop PCM buffer");
            for(int second=0;second<(int)seconds;second++){
                double energy=0;for(int i=second*Synth.RATE;i<(second+1)*Synth.RATE;i++)energy+=pcm[i]*(double)pcm[i];
                check(Math.sqrt(energy/Synth.RATE)/32767>.035,"No silent one-second gap: "+biome+"/"+scene+" second"+second);
            }
            biggest=Math.max(biggest,pcm.length*2);largestDefaultMusicPeak=Math.max(largestDefaultMusicPeak,m.peak/32767.0*AudioEngine.DEFAULT_MUSIC_VOLUME);
            csv.append(String.format(Locale.ROOT,"%d,%s,%.6f,%d,%d,%.6f,%.6f,%.6f,%d,%d,%s\n",biome,scene,seconds,pcm.length,pcm.length*2,m.rms,m.peak/32767.0,m.dc,m.seam,m.largestStep,digest));
            if(biome==0&&scene!=AudioEngine.Scene.PUZZLE){
                short[] preview=new short[PREVIEW_SECONDS*Synth.RATE];
                for(int i=0;i<preview.length;i++){double t=i/(double)Synth.RATE;double fade=smooth(Math.min(1,t/.9))*smooth(Math.min(1,(PREVIEW_SECONDS-t)/.45));preview[i]=(short)Math.round(pcm[i%pcm.length]*AudioEngine.DEFAULT_MUSIC_VOLUME*fade);}
                wave(out.resolve("scene-"+scene.name().toLowerCase(Locale.ROOT)+"-preview.wav"),preview);
                check(new Metrics(preview).rms>.018,"Default-gain preview retains audible program energy");
            }
        }
        check(hashes.size()==50,"All ten puzzle themes and forty scene arrangements are distinct");
        StringBuilder effects=new StringBuilder("earcon,seconds,rms,peak,dc_samples,sha256,preview_gain\n");Set<String> earconHashes=new HashSet<>();
        for(AudioEngine.Sound sound:new AudioEngine.Sound[]{AudioEngine.Sound.SCENE,AudioEngine.Sound.FABRIC,AudioEngine.Sound.REVEAL}){
            short[] pcm=Synth.effect(sound.name());Metrics m=new Metrics(pcm);String digest=hash(pcm);
            check(earconHashes.add(digest)&&Arrays.equals(pcm,Synth.effect(sound.name())),"Distinct deterministic earcon: "+sound);
            check(pcm[0]==0&&pcm[pcm.length-1]==0,"Quiet effect endpoints: "+sound);
            check(m.rms>.003&&m.peak<.8*32767,"Non-silent unclipped earcon: "+sound);
            check(Math.abs(m.dc)<10,"No material earcon DC offset: "+sound+" dc="+m.dc);
            check(largestDefaultMusicPeak+m.peak/32767.0*EFFECT_GAIN<.90,"Default music plus one earcon retains mix headroom");
            short[] preview=new short[pcm.length];for(int i=0;i<pcm.length;i++)preview[i]=(short)Math.round(pcm[i]*EFFECT_GAIN);
            wave(out.resolve("earcon-"+sound.name().toLowerCase(Locale.ROOT)+".wav"),preview);
            effects.append(String.format(Locale.ROOT,"%s,%.6f,%.6f,%.6f,%.6f,%s,%.2f\n",sound,pcm.length/(double)Synth.RATE,m.rms,m.peak/32767.0,m.dc,digest,EFFECT_GAIN));
        }
        for(int[] invalid:new int[][]{{-1,0},{10,0},{0,-1},{0,5},{Integer.MAX_VALUE,4}}){try{Synth.sceneLoop(invalid[0],invalid[1]);throw new AssertionError("Invalid scene accepted");}catch(IllegalArgumentException expected){checks++;}}
        Files.writeString(out.resolve("audio-scene-metrics.csv"),csv);Files.writeString(out.resolve("audio-earcon-metrics.csv"),effects);
        Files.writeString(out.resolve("memory-scope.txt"),"Largest PCM16 loop="+biggest+" bytes. One double[] mix and one resulting short[] are synthesized per requested arrangement. No 50-loop array cache is retained by AudioEngine. Native lifecycle model checks at most two AudioTracks; actual device allocations require profiling.\n");
    }
    private static int lifecycle()throws Exception {
        AudioTrack.resetTestState();AudioManager manager=new AudioManager();AudioEngine audio=new AudioEngine(new Context(Files.createTempDirectory("luma-scene-audio").toFile(),manager));
        audio.resume();AudioTrack current=playing(null,0,AudioEngine.Scene.PUZZLE);Handler.advanceBy(1000);
        for(AudioEngine.Sound earcon:new AudioEngine.Sound[]{AudioEngine.Sound.SCENE,AudioEngine.Sound.FABRIC,AudioEngine.Sound.REVEAL}){int before=SoundPool.lastCreated.played;audio.play(earcon);check(SoundPool.lastCreated.played==before+1,"New earcon loaded and routed through SoundPool: "+earcon);}
        for(AudioEngine.Scene scene:new AudioEngine.Scene[]{AudioEngine.Scene.ADVENTURE,AudioEngine.Scene.HOME,AudioEngine.Scene.COLLECTION,AudioEngine.Scene.ATELIER,AudioEngine.Scene.PUZZLE}){
            AudioTrack prior=current;audio.setScene(scene);check(audio.getScene()==scene,"Requested scene visible synchronously");Handler.advanceBy(250);current=playing(prior,0,scene);await(()->prior.released,"Previous scene output released");Handler.advanceBy(1000);check(current.volume>.26f,"Replacement scene fades to default gain");
            int before=AudioTrack.createdCount;for(int i=0;i<100;i++)audio.setScene(scene);Thread.sleep(20);check(AudioTrack.createdCount==before,"Repeated scene selection creates no redundant player");check(AudioTrack.liveCount==1&&AudioTrack.maximumLiveCount<=2,"Scene switching has bounded players");
        }
        holdWrite();audio.setScene(AudioEngine.Scene.HOME);AudioTrack stale=blocked();AudioTrack prior=current;
        for(int i=0;i<1000;i++){audio.setBiome(i%10);audio.setScene(AudioEngine.Scene.values()[i%5]);}audio.setBiome(2);audio.setScene(AudioEngine.Scene.HOME);check(audio.getBiome()==2&&audio.getScene()==AudioEngine.Scene.HOME,"Combined requests expose the last desired pair");Handler.advanceBy(350);int before=AudioTrack.createdCount;releaseWrite();current=playing(stale,2,AudioEngine.Scene.HOME);await(()->stale.released&&prior.released,"Obsolete candidate and previous scene both released");check(stale.plays==0&&AudioTrack.createdCount==before+1,"1000 intermediate scene/biome requests coalesce into one last pair");Handler.advanceBy(1000);
        holdWrite();audio.setScene(AudioEngine.Scene.ATELIER);AudioTrack returning=blocked();AudioTrack kept=current;audio.setScene(AudioEngine.Scene.HOME);releaseWrite();await(()->returning.released,"Returning to current scene discards pending replacement");Handler.advanceBy(1000);check(!kept.released&&kept.getPlayState()==AudioTrack.PLAYSTATE_PLAYING&&returning.plays==0,"Current scene is reused after cancelled excursion");
        holdWrite();audio.setScene(AudioEngine.Scene.COLLECTION);AudioTrack background=blocked();audio.pause();releaseWrite();await(()->kept.released&&background.looped,"Paused replacement prepares and releases old output");manager.dispatch(AudioManager.AUDIOFOCUS_GAIN);Handler.advanceBy(1500);check(background.plays==0&&!manager.held&&Handler.pending()==0,"Late scene completion/focus cannot play in background");audio.resume();await(()->background.getPlayState()==AudioTrack.PLAYSTATE_PLAYING,"Resume starts requested prepared scene");Handler.advanceBy(1000);
        holdWrite();audio.setScene(AudioEngine.Scene.ADVENTURE);AudioTrack muted=blocked();audio.setMusicEnabled(false);Handler.advanceBy(350);releaseWrite();await(()->background.released&&muted.looped,"Muted scene replaces old output without playing");Handler.advanceBy(1000);check(muted.plays==0,"Music-off survives scene generation");before=SoundPool.lastCreated.played;audio.play(AudioEngine.Sound.FABRIC);check(SoundPool.lastCreated.played==before+1,"Effects remain independent of music mute");audio.setSfxEnabled(false);before=SoundPool.lastCreated.played;audio.play(AudioEngine.Sound.REVEAL);check(SoundPool.lastCreated.played==before,"Effects mute suppresses a new earcon");audio.setSfxEnabled(true);audio.setMusicEnabled(true);await(()->muted.getPlayState()==AudioTrack.PLAYSTATE_PLAYING,"Music toggle resumes requested scene");Handler.advanceBy(1000);
        AudioTrack.failNextWrite.set(true);audio.setScene(AudioEngine.Scene.ATELIER);await(()->AudioTrack.lastCreated!=muted&&AudioTrack.lastCreated.released,"Short scene PCM write releases its candidate");Handler.advanceBy(350);check(AudioTrack.liveCount==1,"Failed scene write leaks no player");audio.setScene(AudioEngine.Scene.HOME);AudioTrack recovered=playing(muted,2,AudioEngine.Scene.HOME);check(muted.released,"Later scene request recovers after failed output");Handler.advanceBy(1000);
        try{audio.setScene(null);throw new AssertionError("Null scene accepted");}catch(IllegalArgumentException expected){checks++;}check(audio.getScene()==AudioEngine.Scene.HOME,"Invalid scene leaves requested pair unchanged");
        holdWrite();audio.setScene(AudioEngine.Scene.COLLECTION);AudioTrack late=blocked();audio.release();audio.release();audio.resume();audio.setScene(AudioEngine.Scene.ATELIER);audio.setMusicEnabled(true);check(recovered.released&&!manager.held,"Release clears current scene output and focus");releaseWrite();await(()->late.released&&AudioTrack.liveCount==0,"Late scene PCM after destruction is released");Handler.advanceBy(1500);check(late.plays==0&&Handler.pending()==0,"No fade/candidate restarts after destruction");int maximum=AudioTrack.maximumLiveCount;check(maximum<=2,"All scene lifecycle scenarios retain at most two native-track models");
        AudioTrack.resetTestState();holdWrite();AudioEngine early=new AudioEngine(new Context(Files.createTempDirectory("luma-scene-early").toFile(),new AudioManager()));early.resume();AudioTrack initial=blocked();early.setBiome(9);early.setScene(AudioEngine.Scene.ATELIER);releaseWrite();AudioTrack finalTrack=playing(initial,9,AudioEngine.Scene.ATELIER);check(initial.released&&initial.plays==0,"A scene requested during constructor preparation supersedes initial music");early.release();check(finalTrack.released&&AudioTrack.liveCount==0&&Handler.pending()==0,"Initial-scene race frees outputs and callbacks");return Math.max(maximum,AudioTrack.maximumLiveCount);
    }
    private static AudioTrack playing(AudioTrack previous,int biome,AudioEngine.Scene scene)throws Exception {
        int frames=Synth.sceneLoop(biome,scene.ordinal()).length;
        await(()->AudioTrack.lastCreated!=null&&AudioTrack.lastCreated!=previous&&AudioTrack.lastCreated.pcmLength==frames&&AudioTrack.lastCreated.looped&&AudioTrack.lastCreated.getPlayState()==AudioTrack.PLAYSTATE_PLAYING,"Requested scene ready: "+biome+"/"+scene);
        await(()->Handler.pending()>0,"Entrance fade scheduled");return AudioTrack.lastCreated;
    }
    private static void holdWrite(){AudioTrack.writeEntered=new CountDownLatch(1);AudioTrack.allowWrite=new CountDownLatch(1);AudioTrack.blockNextWrite.set(true);}
    private static AudioTrack blocked()throws Exception{check(AudioTrack.writeEntered.await(8,TimeUnit.SECONDS),"Controlled scene PCM write is in flight");return AudioTrack.lastCreated;}
    private static void releaseWrite(){AudioTrack.allowWrite.countDown();}
    private static void await(BooleanSupplier ready,String message)throws Exception{long until=System.nanoTime()+8_000_000_000L;while(!ready.getAsBoolean()&&System.nanoTime()<until)Thread.sleep(10);check(ready.getAsBoolean(),message);}
    private static void check(boolean good,String message){checks++;if(!good)throw new AssertionError(message);}
    private static double smooth(double x){return x*x*(3-2*x);}
    private static String hash(short[] pcm)throws Exception{MessageDigest digest=MessageDigest.getInstance("SHA-256");for(short sample:pcm){digest.update((byte)sample);digest.update((byte)(sample>>>8));}StringBuilder result=new StringBuilder();for(byte value:digest.digest())result.append(String.format(Locale.ROOT,"%02x",value));return result.toString();}
    private static void wave(Path path,short[] pcm)throws Exception{ByteBuffer wav=ByteBuffer.allocate(44+pcm.length*2).order(ByteOrder.LITTLE_ENDIAN);wav.put("RIFF".getBytes(StandardCharsets.US_ASCII)).putInt(36+pcm.length*2).put("WAVEfmt ".getBytes(StandardCharsets.US_ASCII)).putInt(16).putShort((short)1).putShort((short)1).putInt(Synth.RATE).putInt(Synth.RATE*2).putShort((short)2).putShort((short)16).put("data".getBytes(StandardCharsets.US_ASCII)).putInt(pcm.length*2);for(short sample:pcm)wav.putShort(sample);Files.write(path,wav.array());}
}
