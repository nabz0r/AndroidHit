package com.androidhit.lumadrift.ui;

import com.androidhit.lumadrift.data.Catalog;

/**
 * Anatomical anchors for the original full-body portraits, in a 3.15-unit standing height.
 * Hands and whole-boot centres are measured in the selected source rectangles, then mapped
 * by the same aspect-preserving full-height fit as CharacterSprites (not equal atlas cells).
 */
public final class CompanionRig {
    public static final float BODY_HEIGHT = 3.15f, BODY_WIDTH = 3.8f;
    public final float torsoX, hatY;
    public final Hand leftHand, rightHand;
    public final Foot leftFoot, rightFoot;

    public static final class Hand {
        public final float x,y,angle;
        private Hand(float x,float y,float angle){this.x=x;this.y=y;this.angle=angle;}
    }
    public static final class Foot {
        public final float x,bottom;
        private Foot(float x,float bottom){this.x=x;this.bottom=bottom;}
    }
    private CompanionRig(float torsoX,float hatY,float lx,float ly,float la,float rx,float ry,float ra,float lf,float lb,float rf,float rb){
        this.torsoX=torsoX;this.hatY=hatY;
        leftHand=new Hand(lx,ly,la);rightHand=new Hand(rx,ry,ra);
        leftFoot=new Foot(lf,lb);rightFoot=new Foot(rf,rb);
    }
    private static final CompanionRig[] ALL={
        new CompanionRig( .08f,.20f,-.61f,.23f,65,.93f,.64f,-35,-.28f,1.63f,.54f,1.62f), // Nova
        new CompanionRig( .04f,.15f,-.68f,.21f,65,.49f,.64f,-38,-.40f,1.63f,.40f,1.62f), // Miko
        new CompanionRig( .04f,.12f,-.80f,.45f,65,.86f,.92f,-35,-.38f,1.63f,.48f,1.62f), // Orbit
        new CompanionRig( .06f,.25f,-.47f,.25f,58,.93f,.21f,-35,-.43f,1.61f,.39f,1.57f), // Pip
        new CompanionRig(-.02f,.18f,-.52f,.34f,62,1.03f,.59f,-35,-.28f,1.63f,.71f,1.59f), // Astra
        new CompanionRig( .06f,.12f,-.70f,.02f,62,.95f,.57f,-38,-.49f,1.61f,.70f,1.60f), // Zig
        new CompanionRig( .20f,.10f,-.50f,.67f,60,.50f,.19f,-15,-.11f,1.62f,.30f,1.63f), // Lune
        new CompanionRig( .25f,.16f,-.47f,.46f,65,.59f,.47f,-30,-.38f,1.62f,.48f,1.62f), // Fenn
        new CompanionRig( .00f,.06f,-.66f,.58f,65,.61f,.14f,-20,-.30f,1.62f,.34f,1.60f), // Hoot
        new CompanionRig( .02f,.20f,-1.12f,.20f,65,.44f,.47f,-45,-.48f,1.63f,.17f,1.64f), // Buzz
        new CompanionRig( .25f,.18f, .01f,.34f,20,.92f,.27f,-12,-.08f,1.62f,.57f,1.60f), // Tula
        new CompanionRig( .03f,.08f,-.74f,-.01f,62,.62f,.38f,-35,-.09f,1.54f,.28f,1.34f), // Jelli
        new CompanionRig( .06f,.24f,-.73f,.49f,65,.77f,.52f,-65,-.21f,1.64f,.37f,1.61f), // Vela
        new CompanionRig( .00f,.13f,-.81f,.61f,65,.68f,.32f,-10,-.32f,1.63f,.20f,1.61f), // Nimbi
        new CompanionRig(-.04f,.16f,-.74f,.62f,65,.82f,.55f,-35,-.40f,1.62f,.38f,1.61f), // Soli
        new CompanionRig( .42f,.24f, .03f,.53f,58,1.28f,.63f,-42, .05f,1.61f,.80f,1.57f), // Mochi
        new CompanionRig( .06f,.18f,-.54f,.23f,65,.99f,.34f,-60, .02f,1.62f,.46f,1.57f), // Nixi
        new CompanionRig(-.04f,.17f,-.88f,.47f,65,.93f,.33f,-35,-.36f,1.62f,.39f,1.59f), // Prickly
        new CompanionRig( .25f,.16f,-.62f,.54f,65,.96f,.66f,-35,-.38f,1.63f,.60f,1.58f), // Quill
        new CompanionRig( .04f,.08f,-1.08f,.50f,70,.95f,.45f,-60,-.57f,1.64f,.46f,1.61f), // Waddles
        new CompanionRig( .06f,.00f,-.56f,.54f,62,.93f,.70f,-35,-.24f,1.62f,.34f,1.58f), // Halo
        new CompanionRig( .38f,.23f,-.44f,.38f,62,1.10f,.60f,-35,-.23f,1.63f,.56f,1.51f), // Emberling
        new CompanionRig( .10f,.16f,-.44f,.60f,15,.98f,-.05f,-15,-.35f,1.64f,.43f,1.51f), // Bambu
        new CompanionRig( .10f,.12f,-.72f,.30f,65,.82f,.36f,-45,-.28f,1.64f,.24f,1.57f)  // Inky
    };

    public static CompanionRig forCharacter(Catalog.CharacterItem character){
        int index=character==null?0:character.shape;
        return ALL[index>=0&&index<ALL.length?index:0];
    }
}
