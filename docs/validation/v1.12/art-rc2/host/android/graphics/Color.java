package android.graphics;
public class Color {
    public static int parseColor(String s){return (int)(Long.parseLong(s.substring(1),16)|(s.length()==7?0xff000000L:0));}
    public static int rgb(int r,int g,int b){return 0xff000000|(r<<16)|(g<<8)|b;}
    public static int red(int c){return (c>>>16)&255;}
    public static int green(int c){return (c>>>8)&255;}
    public static int blue(int c){return c&255;}
}
