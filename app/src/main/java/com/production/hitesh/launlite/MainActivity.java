package com.production.hitesh.launlite;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private View bottomSheet;
    private int i,dockViewNo;
    private boolean canAddDock,fromHome;
    private String leftS,midS,rightS,dockPackage,name;
    private boolean fromDrawer=false;
    private int position;

    private RelativeLayout remove;
    private RelativeLayout dock;


    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;


    private Drawable drawable;
    private ImageView swipeUp;
    private Button setting,widget,wallpaper;
    private Button removeButton,info;
    private EditText search;

    private ImageButton left,mid,right;
    private TextClock time;
    private TextView dateView;

    private CoordinatorLayout mainScreen;
    private PopupWindow popupWindow;


    private PackageManager manager;
    private List<AppDetail> apps;
    private GridView appsGrid;
    private ArrayList<AppDetail> homeApp;
    private GridView homeGrid;
    private ArrayAdapter<AppDetail> adapter;



    private SharedPreferences dockPreferences;
    private SharedPreferences home;
    private ApplicationInfo ai;


    @Override
    public void onBackPressed() {
        if(BottomSheetBehavior.from(bottomSheet).getState()==BottomSheetBehavior.STATE_EXPANDED) {
            appsGrid.setSelection(0);
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);

        }
        if(popupWindow.isShowing()){
            popupWindow.dismiss();
        }


        return;
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance

            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getSupportActionBar().hide();
        }


        //App drawer
        appsGrid = findViewById(R.id.AppsGrid);
        search=findViewById(R.id.search);
        search.setInputType(InputType.TYPE_NULL);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Starting Google", Toast.LENGTH_SHORT).show();
                Intent intent = manager.getLaunchIntentForPackage("com.google.android.googlequicksearchbox");
                MainActivity.this.startActivity(intent);

            }
        });



        //Initializing views
        swipeUp = findViewById(R.id.SwipeIndicator);
        mainScreen = findViewById(R.id.MainScreen);


        //Dock views
        left = findViewById(R.id.left);
        mid = findViewById(R.id.mid);
        right = findViewById(R.id.right);
        dock = findViewById(R.id.hotSeat);


        //Homescreen
        remove=findViewById(R.id.remove);
        removeButton=findViewById(R.id.redmoveButton);
        info=findViewById(R.id.info);

        homeGrid=findViewById(R.id.homeGrid);
        homeApp = new ArrayList<AppDetail>();



        time=findViewById(R.id.time);

        if(android.text.format.DateFormat.is24HourFormat(this))
            time.setFormat24Hour("HH:mm");


       dateView=findViewById(R.id.dateText);
       setDate();


       //To update date at midnight
       Handler handler = new Handler();
       Runnable runnable = new Runnable() {
           @Override
           public void run() {
               if(time.getText().toString().equals("12:00 AM") || time.getText().toString().equals("00:00"));
                  setDate();
           }
       };
       handler.postDelayed(runnable,60000);
       runnable.run();




















        //Getting setup dock data
        dockPreferences = getSharedPreferences("Dock", MODE_PRIVATE);
        final SharedPreferences.Editor dockEditor = dockPreferences.edit();

        if (dockPreferences.contains("Init")) {
            try {
                if (dockPreferences.contains("left")) {
                    leftS = dockPreferences.getString("left", "");

                    left.setImageDrawable(getPackageManager().getApplicationIcon(leftS));
                }

                if(dockPreferences.contains("right")){
                    rightS=dockPreferences.getString("right","");
                    right.setImageDrawable(getPackageManager().getApplicationIcon(rightS));
                }
                if(dockPreferences.contains("mid")){
                    midS=dockPreferences.getString("mid","");
                    mid.setImageDrawable(getPackageManager().getApplicationIcon(midS));
                }

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();

            }
            }




        //Getting and setting home shortcuts
         home=getSharedPreferences("home",MODE_PRIVATE);
         final SharedPreferences.Editor homeEditor = home.edit();

         if(home.getInt("size",0)>0){
             for(int i=0;i<home.getInt("size",0);i++){
                try{ AppDetail app = new AppDetail();
                 app.packagename = home.getString("pack_"+i,"");
                 app.icon=getPackageManager().getApplicationIcon(home.getString("pack_"+i,""));
                 ai=getPackageManager().getApplicationInfo(home.getString("pack_"+i,""),0);
                 app.name=getPackageManager().getApplicationLabel(ai).toString();
                 homeApp.add(app);
                 loadGridView(homeGrid,homeApp);
                 } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
             }

         }












            LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View customView = layoutInflater.inflate(R.layout.home_popup, null);

            popupWindow = new PopupWindow(customView, 400 /*LinearLayout.LayoutParams.WRAP_CONTENT*/, 300 /*LinearLayout.LayoutParams.WRAP_CONTENT*/);




            //Getting and displaying apps
            GetApps();
            loadGridView(appsGrid,apps);




            //Implementing bottom sheets for appdrawer
            bottomSheet = findViewById(R.id.AppDrawer);
            final BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull final View bottomSheet, int newState) {
                    i = bottomSheet.getDrawingCacheBackgroundColor();

                   //For fixing conflict between bottomsheet and Gridview scrolling
                    if(appsGrid.canScrollVertically(-1)) {
                        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                    }

                    switch (newState) {
                        case   BottomSheetBehavior.STATE_DRAGGING: {
                            if (popupWindow.isShowing()) {
                                popupWindow.dismiss();

                            }



                            break;
                        }
                        case BottomSheetBehavior.STATE_SETTLING: {
                            break;

                        }
                        case BottomSheetBehavior.STATE_EXPANDED: {
                            bottomSheet.setBackgroundColor(getResources().getColor(R.color.White));
                            bottomSheet.getBackground().setAlpha(200);


                            break;
                        }
                        case BottomSheetBehavior.STATE_HIDDEN: {
                            Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_HIDDEN");
                            break;
                        }
                        case BottomSheetBehavior.STATE_COLLAPSED: {
                            bottomSheet.setBackgroundColor(i);
                            mainScreen.setBackgroundColor(i);
                            appsGrid.setSelection(0);
                            search.setText("");
                            search.clearFocus();
                            break;
                        }

                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    bottomSheet.setBackgroundColor(getResources().getColor(R.color.White));
                    float i = slideOffset * 200;
                    bottomSheet.getBackground().setAlpha((int) i);


    //For hiding dock when bottomsheet is expanded

          if(slideOffset<0.8) {
    if (dockPreferences.contains("left"))
        left.getDrawable().setAlpha((int) (250 - slideOffset * 250));

    if (dockPreferences.contains("mid"))
        mid.getDrawable().setAlpha((int) (250 - slideOffset * 250));

    if (dockPreferences.contains("right"))
        right.getDrawable().setAlpha((int) (250 - slideOffset * 250));
    dock.setVisibility(View.VISIBLE);
     }else{
    if (dockPreferences.contains("left"))
        left.getDrawable().setAlpha(250);

    if (dockPreferences.contains("mid"))
        mid.getDrawable().setAlpha(250);

    if (dockPreferences.contains("right"))
        right.getDrawable().setAlpha(250);
    dock.setVisibility(View.INVISIBLE);

    }

                }


            });




            //For launching app
            appsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = manager.getLaunchIntentForPackage(apps.get(i).packagename.toString());
                    MainActivity.this.startActivity(intent);
                }
            });

        GridLayout g = new GridLayout(this);
        g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });




            //Dock functionality
// Dragging item to homeScreen
            appsGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    view.findViewById(R.id.Applabel).setVisibility(View.GONE);
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                    view.startDrag(data, shadowBuilder, view, 0);
                    view.findViewById(R.id.Applabel).setVisibility(View.VISIBLE);

                    dockPackage = (apps.get(i).packagename).toString();
                    ImageView img = view.findViewById(R.id.AppIcon);
                    drawable = img.getDrawable();
                    name = apps.get(i).name;
                    position=i;

                    appsGrid.setSelection(0);
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);

                    remove.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_down));
                    remove.setVisibility(View.VISIBLE);

                    fromDrawer=true;
                    return true;
                }
            });





            //DragListener
            View.OnDragListener onDragListener = new View.OnDragListener() {
                @Override
                public boolean onDrag(View view, DragEvent dragEvent) {
                    ImageButton v = null;
                    if (view == left) {
                        v = left;
                        dockViewNo = 1;
                    }
                    if (view == right) {
                        v = right;
                        dockViewNo = 2;
                    }
                    if (view == mid) {
                        v = mid;
                        dockViewNo = 3;
                    }


                    if (dragEvent.getAction() == DragEvent.ACTION_DRAG_ENTERED) {

                        if (v.getDrawable() == null) {
                            drawable.setAlpha(100);
                            v.setImageDrawable(drawable);
                            canAddDock = true;
                            return true;
                        } else {
                            Toast.makeText(MainActivity.this, "You can't add here !", Toast.LENGTH_SHORT).show();
                            canAddDock = false;
                            return false;
                        }

                    }

                    if (dragEvent.getAction() == DragEvent.ACTION_DRAG_EXITED && canAddDock) {
                        v.setImageDrawable(null);
                    }

                    if (dragEvent.getAction() == DragEvent.ACTION_DROP && canAddDock) {
                        drawable.setAlpha(250);
                        v.setImageDrawable(drawable);
                        if (dockViewNo == 1)
                            leftS = dockPackage;
                        dockEditor.putString("left", leftS);
                        if (dockViewNo == 2)
                            rightS = dockPackage;
                        dockEditor.putString("right", rightS);
                        if (dockViewNo == 3)
                            midS = dockPackage;
                        dockEditor.putString("mid", midS);

                        dockEditor.putBoolean("Init", true);

                        remove.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_up));
                        remove.setVisibility(View.GONE);

                        dockEditor.commit();

                    }

                    return true;
                }
            };

            left.setOnDragListener(onDragListener);
            right.setOnDragListener(onDragListener);
            mid.setOnDragListener(onDragListener);




            //Dock clickListener
            final View.OnClickListener dockClick = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String launch = null;
                    if (view == left)
                        launch = leftS;
                    if (view == right)
                        launch = rightS;
                    if (view == mid)
                        launch = midS;

                    try
                    {Intent intent = manager.getLaunchIntentForPackage(launch);
                    MainActivity.this.startActivity(intent);
                    }
                    catch (Exception e){

                    }

                }
            };


            //Click event
            left.setOnClickListener(dockClick);
            right.setOnClickListener(dockClick);
            mid.setOnClickListener(dockClick);




        //Home shortcuts dragging
        View.OnLongClickListener removeClick = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(data, shadowBuilder, view, 0);
                view.setVisibility(View.INVISIBLE);

                remove.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_down));
                remove.setVisibility(View.VISIBLE);


                return true;
            }
        };

        if(left.getDrawable()!=null)
        left.setOnLongClickListener(removeClick);

        if (mid.getDrawable()!=null)
        mid.setOnLongClickListener(removeClick);

        if(right.getDrawable()!=null)
        right.setOnLongClickListener(removeClick);







        //Removing shortcut from home and getting info about the installed applications
        View.OnDragListener removeDrag = new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                View v = (View) dragEvent.getLocalState();
                if(view==removeButton){
                    if(dragEvent.getAction()==DragEvent.ACTION_DROP){
                        if (v==left){
                            left.setImageDrawable(null);
                            dockEditor.remove("left");
                            leftS="";

                        }
                        if(v==right){
                            right.setImageDrawable(null);
                            dockEditor.remove("right");
                            rightS="";
                        }
                        if(v==mid){
                            mid.setImageDrawable(null);
                            dockEditor.remove("mid");
                            midS="";

                        }
                        if(fromHome){
                            fromHome=false;
                            homeApp.remove(position);
                            adapter.notifyDataSetChanged();
                            homeEditor.clear();
                            homeEditor.commit();
                            homeEditor.putInt("size",homeApp.size());
                            if(homeApp.size()>0){
                                for(int i=0;i<homeApp.size();i++){
                                    homeEditor.putString("pack_"+i,homeApp.get(i).packagename);
                                }
                                homeEditor.commit();
                            }


                        }

                       dockEditor.commit();
                        v.setVisibility(View.VISIBLE);
                       remove.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_up));
                       remove.setVisibility(View.GONE);



                    }
                    if(dragEvent.getAction()==DragEvent.ACTION_DRAG_ENDED){
                        v.setVisibility(View.VISIBLE);

                    remove.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_up));
                    remove.setVisibility(View.GONE);}


                }
                else if (view==info){
                    if(dragEvent.getAction()==DragEvent.ACTION_DROP){
                        String appInfo=null;

                        if (v==left){
                            appInfo=dockPreferences.getString("left","");

                        }
                        if(v==right){
                            appInfo=dockPreferences.getString("right","");

                        }
                        if(v==mid){
                            appInfo=dockPreferences.getString("mid","");


                        }
                        else if (fromHome){
                            fromHome=false;
                          appInfo=  homeApp.get(position).packagename;


                        }
                        else{
                            appInfo=apps.get(position).packagename;
                        }
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + appInfo));
                        startActivity(intent);


                    }
                    if(dragEvent.getAction()==DragEvent.ACTION_DRAG_ENDED){
                        v.setVisibility(View.VISIBLE);

                        remove.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_up));
                        remove.setVisibility(View.GONE);

                    }



                }
                return true;

            }
        };


        removeButton.setOnDragListener(removeDrag);
        info.setOnDragListener(removeDrag);









        //Adding shortcuts to homescreen
        homeGrid.setOnDragListener(new View.OnDragListener() {

            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                View v = (View) dragEvent.getLocalState();
                if(dragEvent.getAction()==DragEvent.ACTION_DRAG_ENTERED){


                }
                if(dragEvent.getAction()==DragEvent.ACTION_DROP && fromDrawer){
                    Log.v("capacity",String.valueOf(homeApp.size()));
                    fromDrawer=false;
                    AppDetail app = new AppDetail();
                    if(homeApp.isEmpty()){
                    app.icon=drawable;
                    app.packagename=dockPackage;
                    app.name=name;
                    homeEditor.remove("pack_"+homeApp.size());
                    homeEditor.putString("pack_"+homeApp.size(),dockPackage);
                    homeApp.add(app);
                    loadGridView(homeGrid,homeApp);
                    homeEditor.putInt("size",homeApp.size());

                    }
                    else{
                        app.icon=drawable;
                        app.packagename=dockPackage;
                        app.name=name;
                        homeEditor.remove("pack_"+homeApp.size());
                        homeEditor.putString("pack_"+homeApp.size(),dockPackage);
                        homeApp.add(app);
                        adapter.notifyDataSetChanged();
                        homeEditor.putInt("size",homeApp.size());
                    }


                   homeEditor.commit();

                }

                return true;
            }
        });




        //Dragging shortcuts from homescreen
        homeGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(data, shadowBuilder, view, 0);
                view.setVisibility(View.INVISIBLE);
                position = i;
                remove.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_down));
                remove.setVisibility(View.VISIBLE);

                fromHome=true;


                return true;
            }
        });



        //launching installed application from homescreen shortcuts
        homeGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = manager.getLaunchIntentForPackage(homeApp.get(i).packagename.toString());
                MainActivity.this.startActivity(intent);

            }
        });










        //home Options to change settings of launcher
            mainScreen.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View customView = layoutInflater.inflate(R.layout.home_popup, null);


                    //instantiate popup window
                    popupWindow = new PopupWindow(customView, 400 /*LinearLayout.LayoutParams.WRAP_CONTENT*/, 300 /*LinearLayout.LayoutParams.WRAP_CONTENT*/);

                    //display the popup window
                    popupWindow.setAnimationStyle(R.style.Animation);
                    popupWindow.showAtLocation(mainScreen, Gravity.CENTER, 0, 0);

                    setting = customView.findViewById(R.id.settings);
                    widget = customView.findViewById(R.id.widgets);
                    wallpaper = customView.findViewById(R.id.wallpaper);


                    //NOTE -- settings and widget support is deprecated for now
                    //Will be added in future builds



                    setting.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });


                    widget.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });


                    wallpaper.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            popupWindow.dismiss();
                            Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
                            startActivity(Intent.createChooser(intent, "Select Wallpaper"));

                        }
                    });


                    return true;
                }
            });





            mainScreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(popupWindow.isShowing())
                        popupWindow.dismiss();
                }
            });
        }










    //Getting all the applications icon and name to display in appdrawer
    private void GetApps(){
    manager = getPackageManager();
    apps = new ArrayList<AppDetail>();

    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);

    for(ResolveInfo ri:availableActivities){
        AppDetail app = new AppDetail();
        app.name = ri.loadLabel(manager).toString();
        app.icon =ri.activityInfo.loadIcon(manager);

        app.packagename=ri.activityInfo.packageName;
        apps.add(app);
    }
    Collections.sort(apps, new Comparator<AppDetail>() {
        @Override
        public int compare(AppDetail appDetail, AppDetail t1) {
            return appDetail.name.toString().compareToIgnoreCase(t1.name.toString());
        }
    });

    }










    //Setting up contents of Gridview
    private void loadGridView(final GridView grid, final List<AppDetail> app){


         adapter = new ArrayAdapter<AppDetail>(this,
                R.layout.app_column,
                app) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.app_column, null);
                }

                ImageView appIcon = (ImageView)convertView.findViewById(R.id.AppIcon);
                appIcon.setImageDrawable(app.get(position).icon);



                TextView appName = (TextView)convertView.findViewById(R.id.Applabel);
                appName.setText(app.get(position).name);
                if(grid==homeGrid)
                    appName.setTextColor(getResources().getColor(R.color.White));


                return convertView;
            }
        };

        grid.setAdapter(adapter);


    }



    public void setDate(){
        SimpleDateFormat day = new SimpleDateFormat("EEEE");
        Date d = new Date();
        String dayOfTheWeek = day.format(d);

        SimpleDateFormat mon = new SimpleDateFormat("MMM");

        Calendar cal = Calendar.getInstance();
        String month = mon.format(cal.getTime());

        int dayofmonth = cal.get(Calendar.DAY_OF_MONTH);
        dateView.setText(dayOfTheWeek +", "+String.valueOf(dayofmonth)+" "+month);


    }




}
