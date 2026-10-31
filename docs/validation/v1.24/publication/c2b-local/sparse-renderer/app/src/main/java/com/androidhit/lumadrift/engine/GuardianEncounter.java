package com.androidhit.lumadrift.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Small reversible encounter engine with real layer hits and one-cell movement. */
public final class GuardianEncounter {
    public static final int SAVE_VERSION = 1;
    public enum BlockReason { NONE, BLOCKED_PIECE, EXHAUSTED, MISSING_PIECE }

    /** Immutable initial or current piece view; IDs remain stable after removal. */
    public static final class Piece {
        public final int id, row, col, layers;
        public final Direction direction;
        public final boolean cellwise, present;
        Piece(int id, int row, int col, Direction direction, int layers, boolean cellwise, boolean present) {
            this.id=id;this.row=row;this.col=col;this.direction=direction;
            this.layers=layers;this.cellwise=cellwise;this.present=present;
        }
    }

    public static final class Snapshot {
        public final int number, size, totalWork, remainingWork, used, remainingActions;
        public final boolean solved;
        public final List<Piece> pieces;
        private Snapshot(GuardianEncounter encounter) {
            number=encounter.definition.number;size=encounter.definition.size;
            totalWork=encounter.definition.totalWork;remainingWork=encounter.remainingWork();
            used=encounter.used;remainingActions=encounter.remainingActions();solved=encounter.isSolved();
            pieces=Collections.unmodifiableList(new ArrayList<>(encounter.pieces));
        }
    }

    public static final class MoveResult {
        public final boolean success, blocked, solved, removed, peeled, stepped;
        public final int pieceId, remainingWork, remainingActions;
        public final BlockReason reason;
        private MoveResult(GuardianEncounter encounter,int id,BlockReason reason,boolean removed,boolean peeled,boolean stepped) {
            this.success=reason==BlockReason.NONE;this.blocked=reason==BlockReason.BLOCKED_PIECE;
            this.solved=encounter.isSolved();this.removed=removed;this.peeled=peeled;this.stepped=stepped;
            this.pieceId=id;this.reason=reason;this.remainingWork=encounter.remainingWork();this.remainingActions=encounter.remainingActions();
        }
    }

    public final GuardianCatalog.Definition definition;
    private final ArrayList<Piece> pieces=new ArrayList<>();
    private final ArrayList<Integer> history=new ArrayList<>();
    private int used;

    private GuardianEncounter(GuardianCatalog.Definition definition) {
        this.definition=definition;restart();
    }
    public static GuardianEncounter start(int number) { return new GuardianEncounter(GuardianCatalog.get(number)); }
    public Snapshot snapshot() { return new Snapshot(this); }
    public int[] history() { int[] result=new int[history.size()];for(int i=0;i<result.length;i++)result[i]=history.get(i);return result; }
    public int remainingActions() { return definition.baseBudget-used; }
    public boolean canAct() { return !isSolved()&&remainingActions()>0; }
    public boolean canUndo() { return !history.isEmpty(); }
    public boolean isSolved() { return remainingWork()==0; }
    public int remainingWork() {
        int work=0;for(Piece piece:pieces)if(piece.present)work+=piece.layers-1+(piece.cellwise?GuardianCatalog.distanceToExit(definition.size,piece):1);return work;
    }
    public Piece piece(int id) { return id>=0&&id<pieces.size()?pieces.get(id):null; }
    public boolean canMove(int id) { return physicalReason(id)==BlockReason.NONE; }

    private BlockReason physicalReason(int id) {
        Piece piece=piece(id);if(piece==null||!piece.present)return BlockReason.MISSING_PIECE;
        if(piece.layers>1)return BlockReason.NONE;
        int row=piece.row+piece.direction.dr,col=piece.col+piece.direction.dc;
        while(row>=0&&row<definition.size&&col>=0&&col<definition.size){
            for(Piece other:pieces)if(other.present&&other.id!=id&&other.row==row&&other.col==col)return BlockReason.BLOCKED_PIECE;
            if(piece.cellwise)break;
            row+=piece.direction.dr;col+=piece.direction.dc;
        }
        return BlockReason.NONE;
    }

    /** Missing/empty taps do not spend actions; a real blocked piece attempt does. */
    public MoveResult move(int id) {
        BlockReason reason=physicalReason(id);
        if(reason==BlockReason.MISSING_PIECE)return new MoveResult(this,id,reason,false,false,false);
        if(!canAct())return new MoveResult(this,id,BlockReason.EXHAUSTED,false,false,false);
        used++;
        if(reason!=BlockReason.NONE)return new MoveResult(this,id,reason,false,false,false);
        Piece old=pieces.get(id);boolean peeled=old.layers>1,removed=false,stepped=false;
        if(peeled)pieces.set(id,new Piece(id,old.row,old.col,old.direction,old.layers-1,old.cellwise,true));
        else if(!old.cellwise){removed=true;pieces.set(id,new Piece(id,old.row,old.col,old.direction,1,false,false));}
        else {
            int row=old.row+old.direction.dr,col=old.col+old.direction.dc;
            removed=row<0||row>=definition.size||col<0||col>=definition.size;stepped=!removed;
            pieces.set(id,new Piece(id,removed?old.row:row,removed?old.col:col,old.direction,1,true,!removed));
        }
        history.add(id);return new MoveResult(this,id,BlockReason.NONE,removed,peeled,stepped);
    }

    /** Refund exactly the successful action reversed, even when the budget is exhausted. */
    public boolean undo() {
        if(history.isEmpty())return false;int[] old=history();int previousUsed=used;
        restart();for(int i=0;i<old.length-1;i++)if(!move(old[i]).success)throw new IllegalStateException("Invalid encounter history");
        used=previousUsed-1;return true;
    }
    public void restart() { pieces.clear();pieces.addAll(definition.pieces);history.clear();used=0; }

    /** Every successful action reduces work by one, so any legal productive plan is shortest. */
    public int[] optimalPlan() {
        GuardianEncounter copy=start(definition.number);for(int id:history())if(!copy.move(id).success)throw new IllegalStateException("Invalid encounter history");
        copy.used=0;ArrayList<Integer> plan=new ArrayList<>();int guard=definition.totalWork+1;
        while(!copy.isSolved()&&guard-->0){boolean found=false;
            for(Piece piece:copy.pieces)if(copy.canMove(piece.id)){plan.add(piece.id);copy.move(piece.id);found=true;break;}
            if(!found)throw new IllegalStateException("Encounter dependency cycle");
        }
        if(!copy.isSolved())throw new IllegalStateException("Encounter work did not decrease");
        int[] result=new int[plan.size()];for(int i=0;i<result.length;i++)result[i]=plan.get(i);return result;
    }

    public String encode() {
        StringBuilder save=new StringBuilder("G1|").append(definition.number).append('|').append(used).append('|');
        for(int i=0;i<history.size();i++){if(i>0)save.append(',');save.append(history.get(i));}return save.toString();
    }
    /** Canonical bounded save. Invalid records return null and never mutate a live encounter. */
    public static GuardianEncounter restore(String save) {
        if(save==null||save.length()>1024)return null;
        try {
            String[] fields=save.split("\\|",-1);if(fields.length!=4||!fields[0].equals("G1"))return null;
            GuardianEncounter encounter=start(Integer.parseInt(fields[1]));int used=Integer.parseInt(fields[2]);
            String[] ids=fields[3].isEmpty()?new String[0]:fields[3].split(",",-1);
            if(ids.length>encounter.definition.totalWork||used<ids.length||used>encounter.definition.baseBudget)return null;
            for(String id:ids)if(!encounter.move(Integer.parseInt(id)).success)return null;
            encounter.used=used;return encounter.encode().equals(save)?encounter:null;
        }catch(IllegalArgumentException invalid){return null;}
    }
}
