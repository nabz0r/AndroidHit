package cognitive_lab;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;

final class Json {
    static Map<String,Object> obj(Object... args){Map<String,Object> out=new LinkedHashMap<>();for(int i=0;i<args.length;i+=2)out.put((String)args[i],args[i+1]);return out;}
    static String encode(Object v){StringBuilder out=new StringBuilder();append(out,v);return out.toString();}
    private static void append(StringBuilder out,Object v){
        if(v==null){out.append("null");return;}if(v instanceof Number){double d=((Number)v).doubleValue();if(!Double.isFinite(d))throw new IllegalArgumentException("Nonfinite number");out.append(v);return;}
        if(v instanceof Boolean){out.append(v);return;}if(v instanceof Map){out.append('{');boolean first=true;for(Object entry:((Map<?,?>)v).entrySet()){Map.Entry<?,?> e=(Map.Entry<?,?>)entry;if(!first)out.append(',');first=false;append(out,e.getKey().toString());out.append(':');append(out,e.getValue());}out.append('}');return;}
        if(v instanceof Iterable){out.append('[');boolean first=true;for(Object value:(Iterable<?>)v){if(!first)out.append(',');first=false;append(out,value);}out.append(']');return;}
        if(v instanceof double[]){out.append('[');double[] a=(double[])v;for(int i=0;i<a.length;i++){if(i>0)out.append(',');append(out,a[i]);}out.append(']');return;}
        if(v instanceof int[]){out.append('[');int[] a=(int[])v;for(int i=0;i<a.length;i++){if(i>0)out.append(',');append(out,a[i]);}out.append(']');return;}
        out.append('"');for(char c:v.toString().toCharArray()){switch(c){case '"':out.append("\\\"");break;case '\\':out.append("\\\\");break;case '\n':out.append("\\n");break;case '\r':out.append("\\r");break;case '\t':out.append("\\t");break;default:if(c<32)out.append(String.format("\\u%04x",(int)c));else out.append(c);}}out.append('"');
    }
    static void write(Path path,Object v)throws IOException{Files.writeString(path,encode(v)+"\n",StandardCharsets.UTF_8);}
}
