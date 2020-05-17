package com.example.reminder;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;


import com.allyants.notifyme.NotifyMe;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static android.Manifest.permission.RECORD_AUDIO;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static final String TAG = "MainActivity";

    static int stage_variable=0;
    static String values = "";

    ArrayList<Reminder_detail> reminders = new ArrayList<Reminder_detail>();

    List<item> mlist = new ArrayList<item>();

    ArrayList<String> listKeys = new ArrayList<>();


    public TextToSpeech texttoSpeech;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    String rdesc;
    String rdate;
    String rtime;
    static String message;
    Calendar now = Calendar.getInstance();
    int second =1;

    LocalDateTime fdate;

    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rv_list);

        //microphone permission
        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);

        //text to speech
        texttoSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

            }
        });

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //VoiceInput
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                values = "";
                startVoiceInput();
            }
        });

        //read n add cards
        addValueListener();
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Do you want to add a reminder?");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            texttoSpeech.speak("Do you want to add a reminder?", TextToSpeech.QUEUE_FLUSH,null,null);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stage_variable=1;

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
        }

    }


    private void add_reminder() {


        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What's the reminder?");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            texttoSpeech.speak("What's the reminder?", TextToSpeech.QUEUE_FLUSH, null, null);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stage_variable=2;
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }


    private void add_date() {


        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();


       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            texttoSpeech.speak("I did not get that. Please select a date.", TextToSpeech.QUEUE_FLUSH, null, null);
        }*/




        Log.i(TAG, "Date called");


    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        now.set(Calendar.YEAR, year);
        now.set(Calendar.MONTH, month);
        now.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        // add date to database
        String format_date = year + "-" + (month+1) + "-" + dayOfMonth;

        SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        SimpleDateFormat ddMMMyyyyEEE = new SimpleDateFormat("dd-MMM-yyyy EEE", Locale.US);
        String d = "";
        d = parseDate(format_date, ymdFormat, ddMMMyyyyEEE);
        rdate = d.replaceAll("-", " ");
        add_time();
    }


    public static String parseDate(String inputDateString, SimpleDateFormat inputDateFormat, SimpleDateFormat outputDateFormat) {
        Date date = null;
        String outputDateString = null;
        try {
            date = inputDateFormat.parse(inputDateString);
            outputDateString = outputDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return outputDateString;
    }


    private void add_time() {

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                true
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            texttoSpeech.speak("Please select the time.", TextToSpeech.QUEUE_FLUSH, null, null);
        }

        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        now.set(Calendar.HOUR_OF_DAY, hourOfDay);
        now.set(Calendar.MINUTE, minute);
        now.set(Calendar.SECOND,second);

        Log.i(TAG,"NOW" + now);

        String hour = String.valueOf(hourOfDay);
        String min = String.valueOf(minute);

        // to convert to hh:mm format
        if(hour.length()==1)
        {
            hour = '0' + hour;
        }
        if(min.length()==1) {

            min = '0' + min;
        }

        rtime = hour + ":" + min;



        stage_variable = 4;
        add_to_database();

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (stage_variable == 1) {
                        String choice = result.get(0);
                        if (choice.equals("yes")) {
                            add_reminder();
                        }
                        else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                texttoSpeech.speak("Okay. Press this button anytime to add a reminder", TextToSpeech.QUEUE_FLUSH, null, null);
                            }
                        }

                    }
                    else if (stage_variable == 2) {
                        rdesc = result.get(0);
                        /*Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "When do you want to be reminded?");*/

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            texttoSpeech.speak("When do you want to be reminded?", TextToSpeech.QUEUE_FLUSH, null, null);
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        stage_variable=3;
                        add_date();
                        /*try {
                            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
                        } catch (ActivityNotFoundException a) {

                        }*/

                    }
                    else if(stage_variable == 3){

                        add_date();
                        /*String input_date = result.get(0);
                        String[] date_values = input_date.split(" ");
                        if (date_values.length == 3 ){
                            String monthval = find_month(date_values[1]);
                            String format_date = date_values[2] + "-" + monthval + "-" + date_values[0];

                            now.set(Calendar.YEAR, Integer.parseInt(date_values[2]));
                            now.set(Calendar.MONTH, Integer.parseInt(monthval));
                            now.set(Calendar.DAY_OF_MONTH,Integer.parseInt(date_values[0]));
                            SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                            SimpleDateFormat ddMMMyyyyEEE = new SimpleDateFormat("dd-MMM-yyyy EEE", Locale.US);
                            String d = "";
                            d = parseDate(format_date, ymdFormat, ddMMMyyyyEEE);
                            rdate = d.replaceAll("-", " ");

                            if(Integer.parseInt(date_values[2] ) > 2020 ){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    texttoSpeech.speak("I hope I'm still with you then! Haha!", TextToSpeech.QUEUE_FLUSH, null, null);
                                }
                            }

                            add_time();
                        }
                        else {

                            add_date();
                        }*/
                    }



                    }
                break;
            }

        }
    }


    public void add_to_database(){

        String key = databaseReference.push().getKey();

        databaseReference.child(key).child("reminder_desc").setValue(rdesc);
        databaseReference.child(key).child("reminder_date").setValue(rdate);
        databaseReference.child(key).child("reminder_time").setValue(rtime);
        mlist.clear();
        listKeys.clear();





        Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);

        NotifyMe notifyMe = new NotifyMe.Builder(getApplicationContext())
                .title("Remainder")
                .content(rdesc)
                .color(255,0,0,255)
                .led_color(255,255,255,255)
                .time(now)
                .addAction(intent1, "Dismiss", false, false)
                .key("test")
                .large_icon(R.mipmap.ic_launcher_round)
                .build();

        if (stage_variable == 4) {

            String repeat = "You have set the reminder for " + rdesc + " on " + rdate + " at time " + rtime;
            System.out.println(repeat);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                texttoSpeech.speak(repeat, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }



        addValueListener();

    }

    public void addValueListener(){
        ValueEventListener vl = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.i(TAG, "CHILD ADDED");

                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {


                    Reminder_detail rd = new Reminder_detail();
                    rd.reminder_desc = snapshot.child("reminder_desc").getValue(String.class);
                    rd.reminder_date =  snapshot.child("reminder_date").getValue(String.class);
                    rd.reminder_time =  snapshot.child("reminder_time").getValue(String.class);
                    rd.reminder_key = snapshot.getKey();

                    int c_date = Integer.parseInt(rd.reminder_date.substring(0, 2));
                    int c_year = Integer.parseInt(rd.reminder_date.substring(7, 11));
                    String c_month = rd.reminder_date.substring(3, 6);
                    int c_hour = Integer.parseInt(rd.reminder_time.substring(0,2)) ;
                    int c_min = Integer.parseInt(rd.reminder_time.substring(3,5));

                    // convert string month into type int and then to type Month.
                    Date date = null;
                    try {
                        date = new SimpleDateFormat("MMM", Locale.ENGLISH).parse(c_month);
                    }
                    catch (ParseException e) {
                        System.out.println("month error");
                        e.printStackTrace();
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    int month = cal.get(Calendar.MONTH);

                    Month m = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        m = Month.of(month + 1);
                    }

                    System.out.println(c_date);
                    System.out.println(c_year);
                    System.out.println(c_month);
                    System.out.println(m);
                    System.out.println(c_hour);
                    System.out.println(c_min);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        fdate = LocalDateTime.of(c_year, m, c_date, c_hour, c_min);
                    }
                    System.out.println("fdate:"+fdate);

                    rd.reminder_f_date = fdate;

                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
                    LocalDateTime today = LocalDateTime.now();


                    if( rd.reminder_f_date.compareTo(today) < 0)
                    {
                        //delete the reminder which is old
                        snapshot.getRef().removeValue();
                        continue;
                    }



                    reminders.add(rd);


                    i++;

                }

                if (reminders.size() > 1) {
                    Collections.sort(reminders, new Comparator<Reminder_detail>() {
                        public int compare(Reminder_detail o1, Reminder_detail o2) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                return o1.getReminder_f_date().compareTo(o2.getReminder_f_date());
                            }
                            else
                                return 0;
                        }
                    });
                }

                Reminder_detail sorted = new Reminder_detail();

                for(int j=0;j<reminders.size();j++){

                    sorted = reminders.get(j);

                    // System.out.println("----j----"+sorted.getReminder_date()+ sorted.getReminder_desc() + sorted.getReminder_time() + sorted.getReminder_f_date());

                    String c_day = sorted.reminder_date.substring(12,15);
                    String c_date = sorted.reminder_date.substring(0,2);
                    String c_month = sorted.reminder_date.substring(3,6);

                    mlist.add(new item(c_date,c_month,c_day,sorted.reminder_desc,sorted.reminder_time, sorted.reminder_key));

                    System.out.println(c_date);
                    System.out.println(c_day);
                    System.out.println(c_month);
                }

                reminders.clear();

                add_to_card();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReference.addListenerForSingleValueEvent(vl);
    }


    private void add_to_card() {


        adapter _adapter = new adapter(MainActivity.this,mlist,listKeys);
        recyclerView.setAdapter(_adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



    }





   /* public String find_month(String mon){
        String ret_mon = "";
        switch (mon){
            case "January": {
                ret_mon = "1";
                break;
            }
            case "February": {
                ret_mon = "2";
                break;
            }
            case "March": {
                ret_mon = "3";
                break;
            }
            case "April": {
                ret_mon = "4";
                break;
            }
            case "May": {
                ret_mon = "5";
                break;
            }
            case "June": {
                ret_mon = "6";
                break;
            }
            case "July": {
                ret_mon = "7";
                break;
            }
            case "August": {
                ret_mon = "8";
                break;
            }
            case "September": {
                ret_mon = "9";
                break;
            }
            case "October": {
                ret_mon = "10";
                break;
            }
            case "November": {
                ret_mon = "11";
                break;
            }
            case "December": {
                ret_mon = "12";
                break;
            }
            case "january": {
                ret_mon = "1";
                break;
            }
            case "february": {
                ret_mon = "2";
                break;
            }
            case "march": {
                ret_mon = "3";
                break;
            }
            case "april": {
                ret_mon = "4";
                break;
            }
            case "may": {
                ret_mon = "5";
                break;
            }
            case "june": {
                ret_mon = "6";
                break;
            }
            case "july": {
                ret_mon = "7";
                break;
            }
            case "august": {
                ret_mon = "8";
                break;
            }
            case "september": {
                ret_mon = "9";
                break;
            }
            case "october": {
                ret_mon = "10";
                break;
            }
            case "november": {
                ret_mon = "11";
                break;
            }
            case "december": {
                ret_mon = "12";
                break;
            }


        }


        return ret_mon;
    }*/
}


