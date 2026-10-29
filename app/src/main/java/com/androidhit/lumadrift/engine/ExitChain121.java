package com.androidhit.lumadrift.engine;

import java.util.HashSet;
import java.util.Set;

/** Three distinct manual exits earn one explicit free Focus per attempt. Undo never farms it. */
public final class ExitChain121 {
    private final Set<Integer> credited=new HashSet<>();
    private int streak;
    private boolean earned,spent;
    public boolean exit(int id){
        if(id<0||id>63||!credited.add(id))return false;
        if(!earned){streak=Math.min(3,streak+1);if(streak==3)earned=true;}
        return ready();
    }
    public void correction(){if(!earned)streak=0;}
    public int progress(){return streak;}
    public boolean ready(){return earned&&!spent;}
    public boolean use(){if(!ready())return false;spent=true;return true;}
    public boolean spent(){return spent;}
    public String encode(){
        StringBuilder b=new StringBuilder("1:").append(streak).append(':').append(earned?1:0).append(':').append(spent?1:0).append(':');
        for(int i=0;i<=63;i++)if(credited.contains(i)){if(b.charAt(b.length()-1)!=':')b.append(',');b.append(i);}
        return b.toString();
    }
    public static ExitChain121 decode(String value,Level level){
        ExitChain121 fresh=new ExitChain121();
        if(value==null||value.isEmpty())return fresh;
        try{
            String[] p=value.split(":",-1);if(p.length!=5||!p[0].equals("1"))return null;
            int n=Integer.parseInt(p[1]);if(n<0||n>3||!p[2].matches("[01]")||!p[3].matches("[01]"))return null;
            fresh.streak=n;fresh.earned=p[2].equals("1");fresh.spent=p[3].equals("1");
            if(fresh.earned!=(n==3)||fresh.spent&&!fresh.earned)return null;
            if(!p[4].isEmpty())for(String id:p[4].split(",",-1)){
                int item=Integer.parseInt(id);if(item<0||item>63||level.tile(item)==null||!fresh.credited.add(item))return null;
            }
            if(fresh.credited.size()<n||!fresh.encode().equals(value))return null;
            return fresh;
        }catch(RuntimeException bad){return null;}
    }
}
