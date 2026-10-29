package com.androidhit.lumadrift.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** An accessible action carried by an illustrated world object and free-standing text. */
public final class SceneActionView extends TextView {
    public static final int BACKGROUND=0xfffff6ec, INK=ScenicUi.INK, MUTED=0xff6c667b;
    private final ScenicUi scene=new ScenicUi();
    private final RectF art=new RectF();
    private ScenicUi.Kind kind=ScenicUi.Kind.LANTERN;
    private int accent=ScenicUi.TEAL;

    public SceneActionView(Context context) {
        super(context);
        setBackground(null);setGravity(Gravity.CENTER_VERTICAL);setMaxLines(3);
        setTextSize(15);setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        setTextColor(new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled},new int[]{}},new int[]{0xff918597,INK}));
        setMinHeight(dp(60));setMinimumHeight(dp(60));setMinimumWidth(dp(48));
        setPadding(dp(66),dp(9),dp(8),dp(9));setCompoundDrawablePadding(dp(8));
        setClickable(true);setFocusable(true);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    public static SceneActionView create(Context context,CharSequence text,ScenicUi.Kind kind,int accent,Runnable action) {
        SceneActionView view=new SceneActionView(context);view.setText(text);view.setObjectKind(kind);view.setAccent(accent);
        if(action!=null)view.setOnClickListener(v->action.run());
        return view;
    }

    public void setObjectKind(ScenicUi.Kind value){kind=value==null?ScenicUi.Kind.LANTERN:value;invalidate();}
    public void setAccent(int color){accent=color;invalidate();}
    @Override protected void drawableStateChanged(){super.drawableStateChanged();invalidate();}
    @Override public boolean performClick(){return isEnabled()&&super.performClick();}
    @Override public CharSequence getAccessibilityClassName(){return "android.widget.Button";}
    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(info);info.setClassName("android.widget.Button");info.setClickable(isClickable());}

    @Override protected void onDraw(Canvas canvas) {
        float size=Math.min(dp(58),Math.max(dp(40),getHeight()-dp(4)));
        art.set(dp(1),(getHeight()-size)*.5f,dp(1)+size,(getHeight()+size)*.5f);
        int saved=canvas.save();
        if(!isEnabled())canvas.saveLayerAlpha(art,105);
        if(hasFocus())scene.selector(canvas,art,accent,true,kind);else scene.object(canvas,art,accent,isPressed(),kind);
        canvas.restoreToCount(saved);
        super.onDraw(canvas);
    }

    /** Explicit consent remains a real checkable control, carried by a lamp rather than a slider pill. */
    public static final class Toggle extends CompoundButton {
        private final ScenicUi scene=new ScenicUi();private final RectF lamp=new RectF(),state=new RectF();
        public Toggle(Context context){
            super(context);setButtonDrawable((Drawable)null);setBackground(null);setGravity(Gravity.CENTER_VERTICAL);
            setTextSize(15);setTextColor(INK);setMaxLines(4);setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));
            setPadding(dp(context,66),dp(context,10),dp(context,8),dp(context,10));setMinHeight(dp(context,64));setMinimumWidth(dp(context,48));
            setClickable(true);setFocusable(true);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
        @Override public CharSequence getAccessibilityClassName(){return "android.widget.Switch";}
        @Override protected void drawableStateChanged(){super.drawableStateChanged();invalidate();}
        @Override protected void onDraw(Canvas canvas){
            float unit=getResources().getDisplayMetrics().density,size=54*unit;
            lamp.set(unit,(getHeight()-size)*.5f,unit+size,(getHeight()+size)*.5f);
            scene.object(canvas,lamp,isChecked()?ScenicUi.GOLD:0xffa49bb1,isPressed(),ScenicUi.Kind.LANTERN);
            state.set(lamp.right-22*unit,lamp.bottom-20*unit,lamp.right+unit,lamp.bottom+3*unit);
            scene.object(canvas,state,isChecked()?ScenicUi.TEAL:ScenicUi.CORAL,false,isChecked()?ScenicUi.Kind.CONFIRM:ScenicUi.Kind.CANCEL);
            super.onDraw(canvas);
        }
    }

    public static final class Choice {
        final String label;final ScenicUi.Kind kind;final Runnable action;final int accent;
        public Choice(String label,ScenicUi.Kind kind,Runnable action){this(label,kind,ScenicUi.TEAL,action);}
        public Choice(String label,ScenicUi.Kind kind,int accent,Runnable action){this.label=label;this.kind=kind;this.accent=accent;this.action=action;}
    }

    /** Explicit confirmations keep their separate affirmative/cancel actions, without stock dialog rows. */
    public static Dialog showChoices(Activity activity,String title,CharSequence message,Choice... choices) {
        Dialog dialog=new Dialog(activity);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        FrameLayout sheet=new FrameLayout(activity);sheet.setBackground(paper());
        LinearLayout content=new LinearLayout(activity);content.setOrientation(LinearLayout.VERTICAL);
        int pad=dp(activity,24);content.setPadding(pad,dp(activity,25),pad,dp(activity,24));
        TextView heading=new TextView(activity);heading.setText(title);heading.setTextSize(23);heading.setTextColor(INK);heading.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));heading.setPadding(0,0,0,dp(activity,14));
        if(android.os.Build.VERSION.SDK_INT>=28)heading.setAccessibilityHeading(true);content.addView(heading);
        TextView body=new TextView(activity);body.setText(message);body.setTextColor(MUTED);body.setTextSize(15);body.setLineSpacing(dp(activity,3),1);body.setPadding(0,0,0,dp(activity,16));content.addView(body);
        for(Choice choice:choices) {
            SceneActionView action=create(activity,choice.label,choice.kind,choice.accent,()->{dialog.dismiss();if(choice.action!=null)choice.action.run();});
            content.addView(action,new LinearLayout.LayoutParams(-1,-2));
        }
        ScrollView scroll=new ScrollView(activity);scroll.setFillViewport(false);scroll.addView(content);
        sheet.addView(scroll,new FrameLayout.LayoutParams(-1,-2));dialog.setContentView(sheet);
        dialog.setCanceledOnTouchOutside(true);dialog.show();
        Window window=dialog.getWindow();
        if(window!=null){
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams attributes=window.getAttributes();attributes.dimAmount=.42f;window.setAttributes(attributes);
            int width=Math.min(activity.getResources().getDisplayMetrics().widthPixels-dp(activity,28),dp(activity,520));
            window.setLayout(width,WindowManager.LayoutParams.WRAP_CONTENT);
            int maximum=Math.round(activity.getResources().getDisplayMetrics().heightPixels*.86f);
            sheet.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(maximum,View.MeasureSpec.AT_MOST));
            if(sheet.getMeasuredHeight()>=maximum)window.setLayout(width,maximum);
        }
        return dialog;
    }

    /** Warm scene atmosphere shared by optional native account, social and wallet surfaces. */
    public static Drawable atmosphere(){return new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xfffff8e9,0xfff4eaf8,0xffe7f5ed});}

    /** One hanging fabric sheet; not a repeated table-cell or action background. */
    public static Drawable paper(){return new Drawable(){
        private final ScenicUi drawing=new ScenicUi();private final RectF box=new RectF();
        @Override public void draw(Canvas c){box.set(getBounds().left+2,getBounds().top+2,getBounds().right-2,getBounds().bottom-8);drawing.windowModal(c,box,ScenicUi.VIOLET);}
        @Override public void setAlpha(int alpha){}
        @Override public void setColorFilter(ColorFilter filter){}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    };}

    /** A quiet wooden divider under a section of objects; no enclosing card. */
    public static Drawable shelf(){return new Drawable(){
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c){paint.setColor(0xffd9bcaa);paint.setStrokeWidth(2);float y=getBounds().bottom-2;c.drawLine(getBounds().left+9,y,getBounds().right-9,y,paint);paint.setColor(0x70fff9e9);c.drawLine(getBounds().left+13,y-2,getBounds().right-13,y-2,paint);}
        @Override public void setAlpha(int alpha){paint.setAlpha(alpha);}
        @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    };}

    private int dp(float value){return dp(getContext(),value);}
    private static int dp(Context context,float value){return Math.round(value*context.getResources().getDisplayMetrics().density);}
}
