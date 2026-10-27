package com.androidhit.lumadrift.onboarding;

import com.androidhit.lumadrift.engine.*;
import java.util.*;

/**
 * Small, guided practice boards. This class knows no Android, account, economy or campaign API.
 * Only the requested real board action can advance a phase; Next cannot solve a lesson.
 * Persist encode() after every changed result, including previews, skip and replay.
 */
public final class TutorialSession {
    public static final int LESSON_COUNT = 7;
    public static final String COMPLETED_SAVE = "T1|7|0|COMPLETE|";
    public enum RequiredAction { MOVE, TRY_BLOCKED, PREVIEW_SWITCH, PREVIEW_GATE, OPEN_GATE, UNDO, HINT, DONE, FINISHED }
    private enum State { ACTIVE, SKIPPED, COMPLETE }

    /** accepted means the teaching action matched, including an intentionally blocked attempt. */
    public static final class Action {
        public final boolean accepted, changed;
        public final String feedbackKey;
        /** Null for a hint, preview, Undo or navigation action. Inspect success before animating an exit. */
        public final Board.MoveResult move;
        public final Board.ActionPreview preview;
        /** Piece ID in these lessons, or -1. A stock charge is never reserved or consumed. */
        public final int hintTileId;
        private Action(boolean accepted, boolean changed, String key, Board.MoveResult move,
                       Board.ActionPreview preview, int hint) {
            this.accepted=accepted;this.changed=changed;feedbackKey=key;this.move=move;this.preview=preview;hintTileId=hint;
        }
    }
    private static final class Step {
        final RequiredAction action;final int target;final String instruction,feedback;
        Step(RequiredAction action,int target,String instruction,String feedback){this.action=action;this.target=target;this.instruction=instruction;this.feedback=feedback;}
    }
    private static Step step(RequiredAction action,int target,String instruction,String feedback){return new Step(action,target,instruction,feedback);}
    private static final String[] LESSON_KEYS={"exit","blocker","key","gate","switch","undo","hint"};
    private static final Step[][] STEPS={
        {step(RequiredAction.MOVE,0,"exit_move","exit_success")},
        {step(RequiredAction.TRY_BLOCKED,0,"blocker_try","blocker_seen"),step(RequiredAction.MOVE,1,"blocker_clear","blocker_cleared"),step(RequiredAction.MOVE,0,"blocker_finish","lesson_success")},
        {step(RequiredAction.TRY_BLOCKED,0,"key_try","key_seen"),step(RequiredAction.MOVE,1,"key_exit","key_removed"),step(RequiredAction.MOVE,0,"key_finish","lesson_success")},
        {step(RequiredAction.PREVIEW_GATE,64,"gate_preview","gate_previewed"),step(RequiredAction.OPEN_GATE,64,"gate_open","gate_opened"),step(RequiredAction.MOVE,0,"gate_exit","lesson_success")},
        {step(RequiredAction.PREVIEW_SWITCH,0,"switch_preview","switch_previewed"),step(RequiredAction.MOVE,0,"switch_exit","switch_changed"),step(RequiredAction.MOVE,1,"switch_finish","lesson_success")},
        {step(RequiredAction.MOVE,0,"undo_move","undo_moved"),step(RequiredAction.UNDO,-1,"undo_return","undo_returned"),step(RequiredAction.MOVE,0,"undo_again","undo_moved_again"),step(RequiredAction.MOVE,1,"undo_finish","lesson_success")},
        {step(RequiredAction.HINT,-1,"hint_press","hint_ready"),step(RequiredAction.MOVE,1,"hint_follow","hint_followed"),step(RequiredAction.MOVE,0,"hint_finish","lesson_success")}
    };

    private int lesson,phase;private State state;private Board board;
    private TutorialSession(){lesson=0;phase=0;state=State.ACTIVE;board=new Board(level(0));}
    public static TutorialSession start(){return new TutorialSession();}
    public int lessonIndex(){return lesson;}
    public int lessonCount(){return LESSON_COUNT;}
    public int phase(){return phase;}
    public int phaseCount(){return complete()?0:STEPS[lesson].length;}
    /** The renderer may read this board; mutations must go through this session. Null after finishing. */
    public Board board(){return board;}
    public String lessonKey(){return complete()?"complete":LESSON_KEYS[lesson];}
    public boolean complete(){return state==State.COMPLETE;}
    public boolean skipped(){return state==State.SKIPPED;}
    public boolean lessonComplete(){return !complete()&&phase==STEPS[lesson].length;}
    public RequiredAction requiredAction(){return complete()?RequiredAction.FINISHED:lessonComplete()?RequiredAction.DONE:STEPS[lesson][phase].action;}
    public String instructionKey(){return complete()?"complete_note":skipped()?"paused_note":lessonComplete()?"lesson_done":STEPS[lesson][phase].instruction;}
    public int expectedTileId(){if(complete()||lessonComplete())return -1;Step s=STEPS[lesson][phase];return s.action==RequiredAction.MOVE||s.action==RequiredAction.TRY_BLOCKED||s.action==RequiredAction.PREVIEW_SWITCH?s.target:-1;}
    public Direction expectedGate(){if(complete()||lessonComplete())return null;Step s=STEPS[lesson][phase];return s.action==RequiredAction.PREVIEW_GATE||s.action==RequiredAction.OPEN_GATE?Direction.UP:null;}
    /** Undo already removes its preceding action: there is no additional tutorial action cost. */
    public int spentActions(){return board==null?0:board.history().length;}
    /** The currently confirmed preview survives rotation/restart while waiting for the real action. */
    public Board.ActionPreview currentPreview(){
        return !complete()&&phase==1&&(lesson==3||lesson==4)
                ?board.previewAction(lesson==3?Board.gateAction(Direction.UP):0):null;
    }
    /** Restored hints keep the same exact safe piece highlighted after activity recreation. */
    public int highlightedHintId(){return lesson==6&&phase==1&&!complete()?1:-1;}

    public Action move(int id){return move(id,null);}
    /** A null direction is a tap; a swipe must match the piece's actual arrow. */
    public Action move(int id,Direction swiped){
        if(!active())return rejected();Step expected=STEPS[lesson][phase];
        if(expected.action!=RequiredAction.MOVE&&expected.action!=RequiredAction.TRY_BLOCKED||id!=expected.target)return reject("try_highlighted");
        Tile tile=board.level.tile(id);if(swiped!=null&&(tile==null||tile.direction!=swiped))return reject("wrong_direction");
        Board.MoveResult moved=board.move(id);
        if(expected.action==RequiredAction.TRY_BLOCKED){
            Board.BlockReason wanted=lesson==1?Board.BlockReason.BLOCKED_PATH:Board.BlockReason.LOCKED_BARRIER;
            if(moved.success||moved.reason!=wanted)throw new IllegalStateException("Tutorial blocked fixture changed");
        }else if(!moved.success)return new Action(false,false,"try_highlighted",moved,null,-1);
        return progressed(expected,moved,null,-1);
    }
    public Action previewSwitch(int id){
        if(!active())return rejected();Step expected=STEPS[lesson][phase];
        if(expected.action!=RequiredAction.PREVIEW_SWITCH||id!=expected.target)return reject("try_highlighted");
        Board.ActionPreview preview=board.previewAction(id);
        if(!preview.legal||!board.level.tile(id).isSwitch())throw new IllegalStateException("Tutorial switch fixture changed");
        return progressed(expected,null,preview,-1);
    }
    public Action previewGate(Direction direction){
        if(!active())return rejected();Step expected=STEPS[lesson][phase];
        if(expected.action!=RequiredAction.PREVIEW_GATE||direction!=Direction.UP)return reject("try_gate");
        Board.ActionPreview preview=board.previewAction(Board.gateAction(direction));
        if(!preview.legal)throw new IllegalStateException("Tutorial gate fixture changed");
        return progressed(expected,null,preview,-1);
    }
    public Action openGate(Direction direction){
        if(!active())return rejected();Step expected=STEPS[lesson][phase];
        if(expected.action!=RequiredAction.OPEN_GATE||direction!=Direction.UP)return reject("try_gate");
        Board.MoveResult moved=board.performAction(Board.gateAction(direction));
        if(!moved.success)throw new IllegalStateException("Tutorial gate fixture changed");
        return progressed(expected,moved,null,-1);
    }
    public Action undo(){
        if(!active())return rejected();Step expected=STEPS[lesson][phase];
        if(expected.action!=RequiredAction.UNDO)return reject("try_highlighted");
        if(!board.undo())throw new IllegalStateException("Tutorial Undo fixture changed");
        return progressed(expected,null,null,-1);
    }
    public Action hint(){
        if(!active())return rejected();Step expected=STEPS[lesson][phase];
        if(expected.action!=RequiredAction.HINT)return reject("try_highlighted");
        int id=board.hint();if(id!=1||!board.isSafeMove(id))throw new IllegalStateException("Tutorial hint fixture changed");
        return progressed(expected,null,null,id);
    }
    private boolean active(){return state==State.ACTIVE&&!lessonComplete();}
    private Action rejected(){return reject(skipped()?"paused_note":complete()?"complete_note":"lesson_done");}
    private static Action reject(String key){return new Action(false,false,key,null,null,-1);}
    private Action progressed(Step s,Board.MoveResult moved,Board.ActionPreview preview,int hint){phase++;return new Action(true,true,s.feedback,moved,preview,hint);}
    /** Only a genuinely solved and completed lesson can proceed. This method never plays a piece. */
    public Action advance(){
        if(state!=State.ACTIVE||!lessonComplete()||!board.isSolved())return reject("finish_action");
        lesson++;phase=0;if(lesson==LESSON_COUNT){state=State.COMPLETE;board=null;return new Action(true,true,"complete_note",null,null,-1);}
        board=new Board(level(lesson));return new Action(true,true,"next_lesson",null,null,-1);
    }
    public Action restartLesson(){if(complete())return reject("complete_note");phase=0;state=State.ACTIVE;board=new Board(level(lesson));return new Action(true,true,"restarted",null,null,-1);}
    /** Keeps the exact phase/history for a later return; skipping never counts as completion. */
    public Action skip(){if(state!=State.ACTIVE)return rejected();state=State.SKIPPED;return new Action(true,true,"paused_note",null,null,-1);}
    public Action resume(){if(state!=State.SKIPPED)return reject("try_highlighted");state=State.ACTIVE;return new Action(true,true,"resumed",null,null,-1);}
    public Action replay(){lesson=0;phase=0;state=State.ACTIVE;board=new Board(level(0));return new Action(true,true,"restarted",null,null,-1);}

    /** Single bounded record includes phase, mode and the engine's typed action history. */
    public String encode(){
        if(complete())return COMPLETED_SAVE;
        StringBuilder out=new StringBuilder("T1|").append(lesson).append('|').append(phase).append('|').append(state.name()).append('|');
        int[] history=board.history();for(int i=0;i<history.length;i++){if(i>0)out.append(',');out.append(history[i]);}return out.toString();
    }
    /** Invalid, future, mismatched or noncanonical saves safely restart practice, without a reward. */
    public static TutorialSession restore(String encoded){
        TutorialSession result=start();if(encoded==null||encoded.isEmpty())return result;
        try{
            if(encoded.length()>256)throw new IllegalArgumentException();String[] fields=encoded.split("\\|",-1);
            if(fields.length!=5||!fields[0].equals("T1"))throw new IllegalArgumentException();
            int lesson=Integer.parseInt(fields[1]),phase=Integer.parseInt(fields[2]);State state=State.valueOf(fields[3]);
            if(state==State.COMPLETE){if(!COMPLETED_SAVE.equals(encoded))throw new IllegalArgumentException();result.lesson=LESSON_COUNT;result.phase=0;result.state=State.COMPLETE;result.board=null;return result;}
            if(lesson<0||lesson>=LESSON_COUNT||phase<0||phase>STEPS[lesson].length)throw new IllegalArgumentException();
            result.lesson=lesson;result.board=new Board(level(lesson));
            for(int i=0;i<phase;i++){Step s=STEPS[lesson][i];Action action;
                switch(s.action){case MOVE:case TRY_BLOCKED:action=result.move(s.target);break;case PREVIEW_SWITCH:action=result.previewSwitch(s.target);break;case PREVIEW_GATE:action=result.previewGate(Direction.UP);break;case OPEN_GATE:action=result.openGate(Direction.UP);break;case UNDO:action=result.undo();break;case HINT:action=result.hint();break;default:throw new IllegalArgumentException();}
                if(!action.accepted)throw new IllegalArgumentException();
            }
            result.state=state;if(!result.encode().equals(encoded))throw new IllegalArgumentException();return result;
        }catch(RuntimeException invalid){return start();}
    }
    public static boolean isValidSave(String encoded){return encoded!=null&&!encoded.isEmpty()&&restore(encoded).encode().equals(encoded);}

    private static Tile tile(int id,int row,int col,Direction direction){return new Tile(id,row,col,direction,0);}
    private static Level legacy(int number,List<Tile> tiles,int[] solution,List<Barrier> barriers,int gates){return new Level(number,0,3,gates,tiles,solution,true,18000+number,barriers);}
    private static Level level(int lesson){
        switch(lesson){
            case 0:return legacy(1,Arrays.asList(tile(0,1,1,Direction.RIGHT)),new int[]{0},Collections.emptyList(),Direction.ALL_OPEN);
            case 1:return legacy(2,Arrays.asList(tile(0,1,0,Direction.RIGHT),tile(1,1,2,Direction.UP)),new int[]{1,0},Collections.emptyList(),Direction.ALL_OPEN);
            case 2:return legacy(3,Arrays.asList(tile(0,1,0,Direction.RIGHT),tile(1,0,0,Direction.UP)),new int[]{1,0},Arrays.asList(new Barrier(1,2,1)),Direction.ALL_OPEN);
            case 3:return new Level(4,0,3,Direction.RIGHT.bit|Direction.DOWN.bit|Direction.LEFT.bit,Arrays.asList(tile(0,1,1,Direction.UP)),new int[]{0},true,18004,Collections.emptyList(),4,2,new int[]{64,0});
            case 4:return new Level(5,0,3,Direction.DOWN.bit|Direction.RIGHT.bit|Direction.LEFT.bit,Arrays.asList(new Tile(0,1,1,Direction.DOWN,Direction.UP.bit|Direction.RIGHT.bit),tile(1,0,0,Direction.UP)),new int[]{0,1},true,18005,Collections.emptyList(),4,2,new int[]{0,1});
            case 5:return new Level(6,0,3,Direction.ALL_OPEN,Arrays.asList(tile(0,0,0,Direction.UP),tile(1,2,2,Direction.DOWN)),new int[]{0,1},true,18006,Collections.emptyList(),4,2,new int[]{0,1});
            case 6:return legacy(7,Arrays.asList(tile(0,1,0,Direction.RIGHT),tile(1,1,2,Direction.UP)),new int[]{1,0},Collections.emptyList(),Direction.ALL_OPEN);
            default:throw new IllegalArgumentException("Unknown tutorial lesson");
        }
    }
}
