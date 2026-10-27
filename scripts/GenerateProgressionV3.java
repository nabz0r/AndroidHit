import com.androidhit.lumadrift.engine.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Deterministic offline authoring: frozen G/H layouts, sparse certified seals, fixed waves. */
public final class GenerateProgressionV3 {
    static final class Source {
        final int number;final Level level;final double probability;
        Source(int number,Level level,double probability){this.number=number;this.level=level;this.probability=probability;}
    }
    static boolean keys(int number,boolean remix) {
        return remix?number==9||number>=17&&(number-17)%8==0
                :number==12||number==14||number==17||number>=22&&(number-22)%6==0;
    }
    // Enum ordinal contract: NORMAL,HARD,EXPERT; INTRODUCTION,PRACTICE,PEAK,RECOVERY.
    static int difficulty(int number,boolean remix) {
        int beat=(number-1)%10;
        if(number<=9||!remix&&(number==12||number==14||number==17))return 0;
        if(beat==9&&number>=40)return 2;
        return beat==3||beat==6||beat==8||beat==9?1:0;
    }
    static int beat(int number,boolean remix) {
        if(number<=3||!remix&&(number==4||number==12)||remix&&number==9)return 0;
        if(!remix&&(number==5||number==6||number==7||number==9||number==14||number==17))return 1;
        int slot=(number-1)%10;
        return slot==0||slot==4||slot==7?3:slot==3||slot==6||slot==9?2:1;
    }
    static double target(int number,boolean remix) {
        double base=number<=30?.60:number<=120?.48*Math.exp(-.6*(number-30)/90.0)
                :.25*Math.exp(-1.05*(number-120)/880.0);
        if(remix)base*=.75;
        double[] wave={1.65,1.12,1.0,.50,1.6,1.0,.46,1.45,.55,.28};
        if(number<=3)return 1;
        if(!remix&&number<=9)return .70;
        if(!remix&&number==10)return .50;
        if(!remix&&number==12)return 1;
        if(!remix&&(number==14||number==17))return .75;
        return Math.min(.95,base*wave[(number-1)%10]);
    }
    static Level decorate(Source source,int number,boolean remix) {
        Level old=source.level;
        if(!remix&&number==12) {
            ArrayList<Tile> ordinary=new ArrayList<>();
            for(Tile tile:old.tiles)ordinary.add(new Tile(tile.id,tile.row,tile.col,tile.direction,0));
            old=new Level(old.number,old.chapter,old.size,15,ordinary,old.solution,false,old.seed);
        }
        int wanted=number<60?1:number<180?2:number<480?2:3;
        int wantedKeys=number<180?1:2;
        ArrayList<Barrier> walls=new ArrayList<>();
        int[] rank=new int[old.tiles.size()];for(int i=0;i<old.solution.length;i++)rank[old.solution[i]]=i;
        Random random=new Random(0x4A4B5345414C5633L^old.seed^((long)number*0x9E3779B97F4A7C15L));
        ArrayList<int[]> candidates=new ArrayList<>();
        for(int row=0;row<old.size;row++)for(int col=0;col<old.size;col++) {
            boolean occupied=false;for(Tile tile:old.tiles)if(tile.row==row&&tile.col==col)occupied=true;
            if(occupied)continue;
            for(Tile key:old.tiles) {
                if(rank[key.id]>old.tiles.size()/2)continue;
                if(!remix&&number<=17&&key.switchMask!=0)continue;
                int firstBlocked=100,affected=0;boolean initiallyFree=false;
                Board board=new Board(old);
                if(number>17)for(int id:old.solution){if(id==key.id)break;board.move(id);}
                for(Tile tile:old.tiles)if(ProgressionAnalysis.onRay(tile,row,col)) {
                    firstBlocked=Math.min(firstBlocked,rank[tile.id]);affected++;
                    if(board.canMove(tile.id))initiallyFree=true;
                }
                if(affected==0||firstBlocked<=rank[key.id]||!initiallyFree)continue;
                candidates.add(new int[]{row,col,key.id});
            }
        }
        Collections.shuffle(candidates,random);
        boolean found=false;
        for(int amount=wanted;amount>=1&&!found;amount--)
            for(int keyCount=Math.min(amount,wantedKeys);keyCount>=1&&!found;keyCount--){walls.clear();found=chooseWalls(candidates,0,amount,keyCount,walls);}
        if(!found)return null;
        Level level=new Level(number,(number-1)/30+1,old.size,old.initialGateMask,old.tiles,old.solution,false,
                old.seed^0x56334A4B00000000L^number,walls);
        Board board=new Board(level);for(int id:level.solution)if(!board.move(id).success)return null;
        return board.isSolved()?level:null;
    }
    static boolean chooseWalls(List<int[]> candidates,int start,int wanted,int wantedKeys,List<Barrier> walls) {
        HashSet<Integer> keys=new HashSet<>();for(Barrier wall:walls)keys.add(wall.keyTileId);
        if(walls.size()==wanted)return keys.size()==wantedKeys;
        if(keys.size()>wantedKeys||keys.size()+wanted-walls.size()<wantedKeys)return false;
        for(int i=start;i<candidates.size();i++) {
            int[] candidate=candidates.get(i);boolean occupied=false;
            for(Barrier wall:walls)if(wall.row==candidate[0]&&wall.col==candidate[1])occupied=true;
            if(occupied||keys.size()==wantedKeys&&!keys.contains(candidate[2]))continue;
            walls.add(new Barrier(candidate[0],candidate[1],candidate[2]));
            if(chooseWalls(candidates,i+1,wanted,wantedKeys,walls))return true;
            walls.remove(walls.size()-1);
        }
        return false;
    }
    static String encode(Source source,Level level,int number,boolean remix) {
        int allowance=number<=3?5:beat(number,remix)==0?5:beat(number,remix)==3?4:difficulty(number,remix)==2?4:3;
        StringBuilder row=new StringBuilder().append(source.number).append('|').append(allowance).append('|')
                .append(difficulty(number,remix)).append('|').append(beat(number,remix)).append('|');
        for(Barrier wall:level.barriers)row.append(wall.row).append(',').append(wall.col).append(',').append(wall.keyTileId).append(';');
        row.append('|').append(!remix&&number==12?1:0);
        return row.toString();
    }
    public static void main(String[] args)throws Exception {
        Path root=Paths.get(args.length==0?".":args[0]);
        HashMap<String,Double> probabilities=new HashMap<>();
        for(String row:Files.readAllLines(root.resolve("docs/data/progression-v2-selection.csv"))) {
            String[] fields=row.split(",");if(fields[0].equals("family"))continue;
            probabilities.put(fields[0]+":"+fields[1],Double.parseDouble(fields[8]));
        }
        StringBuilder java=new StringBuilder("package com.androidhit.lumadrift.engine;\n\n/** Frozen V3 J/K maps: G/H source|margin|difficulty|beat|seals|neutral-gate lesson. */\nfinal class ProgressionV3Layouts {\n    private ProgressionV3Layouts() { }\n");
        StringBuilder csv=new StringBuilder("family,number,source,keys,seals,target_exact,selected_exact,difficulty,beat,margin\n");
        for(boolean remix:new boolean[]{false,true}) {
            int total=remix?240:1000;HashSet<String> unique=new HashSet<>();
            java.append("    static final String[] ").append(remix?"REMIX":"JOURNEY").append(" = {\n");
            for(int bucket=0;bucket<total;bucket+=30) {
                ArrayList<Source> pool=new ArrayList<>();
                for(int source=bucket+1;source<=Math.min(total,bucket+30);source++) {
                    Level level=remix?BalancedCatalog.remixV2(source):BalancedCatalog.getV2(source);
                    pool.add(new Source(source,level,probabilities.get((remix?"remix:":"journey:")+source)));
                }
                for(int number=bucket+1;number<=Math.min(total,bucket+30);number++) {
                    final double goal=target(number,remix);
                    pool.sort(Comparator.comparingDouble((Source source)->Math.abs(Math.log(source.probability/goal))).thenComparingInt(source->source.number));
                    Source picked=null;Level selected=null;double selectedP=0,bestLoss=Double.POSITIVE_INFINITY;
                    for(Source candidate:pool) {
                        if(number<=6&&candidate.number!=number)continue;
                        if(!remix&&number==12&&candidate.level.tiles.size()>9)continue;
                        Level level=keys(number,remix)?decorate(candidate,number,remix):candidate.level;
                        if(level==null)continue;
                        if(!unique.add(ProgressionAnalysis.canonical(level)))continue;
                        unique.remove(ProgressionAnalysis.canonical(level));
                        double p=level.barriers.isEmpty()?candidate.probability:new ProgressionAnalysis(level).probability;
                        double loss=Math.abs(Math.log(p/goal));
                        if(number<=6||loss<bestLoss){picked=candidate;selected=level;selectedP=p;bestLoss=loss;}
                        if(number<=6||!keys(number,remix)||loss<.05)break;
                    }
                    if(picked==null)throw new AssertionError("No selection for "+number+" remix="+remix);
                    pool.remove(picked);unique.add(ProgressionAnalysis.canonical(selected));
                    String encoded=encode(picked,selected,number,remix);
                    java.append("        \"").append(encoded).append("\"").append(number==total?"\n":",\n");
                    HashSet<Integer> keys=new HashSet<>();for(Barrier wall:selected.barriers)keys.add(wall.keyTileId);
                    csv.append(remix?"remix":"journey").append(',').append(number).append(',').append(picked.number)
                            .append(',').append(keys.size()).append(',').append(selected.barriers.size()).append(',')
                            .append(goal).append(',').append(selectedP).append(',').append(difficulty(number,remix)).append(',')
                            .append(beat(number,remix)).append(',').append(encoded.split("\\|")[1]).append('\n');
                }
                System.out.println((remix?"Remix":"Journey")+" frozen through "+Math.min(total,bucket+30));
            }
            java.append("    };\n");
        }
        java.append("}\n");
        Files.write(root.resolve("app/src/main/java/com/androidhit/lumadrift/engine/ProgressionV3Layouts.java"),java.toString().getBytes(StandardCharsets.UTF_8));
        Files.write(root.resolve("docs/data/progression-v3-selection.csv"),csv.toString().getBytes(StandardCharsets.UTF_8));
    }
}
