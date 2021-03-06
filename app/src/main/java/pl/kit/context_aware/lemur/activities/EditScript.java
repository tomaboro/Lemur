package pl.kit.context_aware.lemur.activities;

import android.Manifest;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.CastRemoteDisplay;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import pl.kit.context_aware.lemur.arrayAdapters.ActionsArrayAdapter;
import pl.kit.context_aware.lemur.arrayAdapters.DaysArrayAdapter;
import pl.kit.context_aware.lemur.arrayAdapters.LocationsArrayAdapter;
import pl.kit.context_aware.lemur.arrayAdapters.TimesArrayAdapter;
import pl.kit.context_aware.lemur.dialogFragments.ActionPickerFragment;
import pl.kit.context_aware.lemur.dialogFragments.DatePickerFragment;
import pl.kit.context_aware.lemur.dialogFragments.DayOfWeekPickerFragment;
import pl.kit.context_aware.lemur.dialogFragments.NotificationMessageDetailsFragment;
import pl.kit.context_aware.lemur.dialogFragments.SMSMessageDetailsFragment;
import pl.kit.context_aware.lemur.dialogFragments.TimePickerFragment;
import pl.kit.context_aware.lemur.editor.ModelCreator;
import pl.kit.context_aware.lemur.editor.ruleExpressions.ALSVExpression;
import pl.kit.context_aware.lemur.editor.ruleExpressions.ActionExpression;
import pl.kit.context_aware.lemur.editor.ruleExpressions.DecisionExpression;
import pl.kit.context_aware.lemur.editor.xtypes.Xattr;
import pl.kit.context_aware.lemur.editor.xtypes.Xrule;
import pl.kit.context_aware.lemur.editor.xtypes.Xschm;
import pl.kit.context_aware.lemur.filesOperations.FilesOperations;
import pl.kit.context_aware.lemur.listItems.ActionItem;
import pl.kit.context_aware.lemur.listItems.DayItem;
import pl.kit.context_aware.lemur.listItems.LocationItem;
import pl.kit.context_aware.lemur.R;
import pl.kit.context_aware.lemur.listItems.TimeItem;
import pl.kit.context_aware.lemur.readers.ReadTime;


public class EditScript extends AppCompatActivity implements DayOfWeekPickerFragment.NoticeDialogDOWPFListener
        , ActionPickerFragment.NoticeDialogAPFListener, TimePickerFragment.NoticeDialogTPFListener, SMSMessageDetailsFragment.NoticeSMSMessageDetailsFragmentListener
        , NotificationMessageDetailsFragment.NoticeNotificationMessageDetailsFragmentListener, DatePickerFragment.NoticeDialogDPFListener{

    String rememberedModelName; //used in deleting old models
    private String notificationNumber; //used in deleting old files, when we edit model
    private String smsNumber; //used in deleting old files, when we edit model
    private LinkedList<Integer> daysCyclical = new LinkedList<>(); //list of daysCyclical selected or loaded from file
    private String scriptNameToLoad; // name of the loading script

    //Layout pools variables
    private EditText ETScriptName;
    private TextView TVDaysCyclical;
    private TimeItem tmpTimeInterval;
    private ListView listDays;
    private ListView listTime;
    private ListView listLocation;
    private ListView listAction;


    //Lists and adapters
    ArrayList<TimeItem> times;
    ArrayList<DayItem> days;
    ArrayList<LocationItem> locations;
    ArrayList<ActionItem> actions;
    TimesArrayAdapter timeAdapter;
    DaysArrayAdapter daysAdapter;
    LocationsArrayAdapter locationsAdapter;
    ActionsArrayAdapter actionsAdapter;

    /**
     * Action for the Set Time Clicked
     * Opens TimePickerFragment with previously selected items checked or current time if ocreating new one
     * @param v current View
     */
    public void SetTimeOnClick(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        ((TimePickerFragment) newFragment).setPosition(-1);
        ((TimePickerFragment) newFragment).setHour(ReadTime.ReadHour());
        ((TimePickerFragment) newFragment).setMinute(ReadTime.ReadMinute());
        newFragment.show(getFragmentManager(), "Time Picker");

    }

    /**
     * Action for the Set Time Interval Clicked
     * Opens TimePickerFragment with previously selected items checked
     * @param v current View
     */
    public void SetTimeIntervalOnClick(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        ((TimePickerFragment) newFragment).setPosition(-1);
        ((TimePickerFragment) newFragment).setTypeInterval(2);
        newFragment.show(getFragmentManager(), "Time Picker");
        DialogFragment newFragment1 = new TimePickerFragment();
        ((TimePickerFragment) newFragment1).setPosition(-1);
        ((TimePickerFragment) newFragment1).setTypeInterval(1);
        newFragment1.show(getFragmentManager(), "Time Picker");

    }

    /**
     * Action for the Set Day Clicked
     * Opens DayOfWeekPickerFragment with previously selected items checked
     * @param v current View
     */
    public void SetDayOnClick(View v) {
        DialogFragment newFragment = new DayOfWeekPickerFragment();
        ((DayOfWeekPickerFragment) newFragment).setPosition(-1);
        ((DayOfWeekPickerFragment) newFragment).setDaysOfWeek(daysCyclical);
        newFragment.show(getFragmentManager(), "DayOfWeek Picker");
    }

    /**
     * Action for the Set Action Clicked
     * Opens SetActionPickerFragment with previously selected items checked
     * @param v current View
     */
    public void SetActionOnClick(View v) {
        DialogFragment newFragment = new ActionPickerFragment();
        ((ActionPickerFragment)newFragment).setPosition(-1);
        newFragment.show(getFragmentManager(), "Action Picker");
    }

    /**
     * Action for Set Date Clicked
     * Opens DataPickerFragment with previously selected date or current date if adding new one
     * @param v current view
     */
    public void SetDateOnClick(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        ((DatePickerFragment)newFragment).setPosition(-1);
        ((DatePickerFragment) newFragment).setDay(ReadTime.ReadDayofMonth());
        ((DatePickerFragment) newFragment).setMonth(ReadTime.ReadMonth()+1);
        ((DatePickerFragment) newFragment).setYear(ReadTime.ReadYear());
        newFragment.show(getFragmentManager(), "Date Picker");
    }

    /**
     * Action for the Set Action Clicked
     * Opens PlacePicker with current location in center
     * @param v current View
     */
    public void SetLocationOnClick(View v) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(this), 1);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }
        else{
            Toast.makeText(this,this.getResources().getString(R.string.pl_fine_location),Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
        }
    }

    /**
     * Method running when Activity start
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_script);

        if(getFilesDir().listFiles().length == 0){
            AlertDialog.Builder infoBuilder = new AlertDialog.Builder(this);
            infoBuilder.setTitle(getText(R.string.es_InfoTitle))
                    .setMessage(getText(R.string.es_InfoMess))
                    .setPositiveButton(R.string.tp_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                        }});
            infoBuilder.show();
        }

        //initializing lists
        daysCyclical = new LinkedList<Integer>();
        days = new ArrayList<DayItem>();
        times = new ArrayList<TimeItem>();
        locations = new ArrayList<LocationItem>();
        actions = new ArrayList<ActionItem>();

        //loading data from model to variables
        this.loadDataFromModel();

        //initializing layout objects
        listDays = (ListView)findViewById(R.id.es_lv_days);
        listTime = (ListView)findViewById(R.id.es_lv_time);
        listLocation = (ListView)findViewById(R.id.es_lv_location);
        listAction = (ListView)findViewById(R.id.es_lv_actions);
        TVDaysCyclical = (TextView) findViewById(R.id.es_repeating_days);
        ETScriptName = (EditText) findViewById(R.id.es_set_tiitle_sub);

        //connecting adapters with lists and lists with listViews
        //refreshing height of the listViewx
        daysAdapter = new DaysArrayAdapter(this, days);
        listDays.setAdapter(daysAdapter);
        ListUtils.setDynamicHeight(listDays);
        locationsAdapter = new LocationsArrayAdapter(this,locations);
        listLocation.setAdapter(locationsAdapter);
        ListUtils.setDynamicHeight(listLocation);
        actionsAdapter = new ActionsArrayAdapter(this, actions);
        listAction.setAdapter(actionsAdapter);
        ListUtils.setDynamicHeight(listAction);
        timeAdapter = new TimesArrayAdapter(this, times);
        listTime.setAdapter(timeAdapter);
        ListUtils.setDynamicHeight(listTime);

        //setting other layout pools
        ETScriptName.setText(rememberedModelName);

        Collections.sort(daysCyclical);

        if(daysCyclical.size() == 7){
            TVDaysCyclical.setText(getText(R.string.es_Everyday));
        }else {
            String[] daysStr = this.getResources().getStringArray(R.array.days_short);
            TVDaysCyclical.setText("");
            for (int i = 0; i < daysCyclical.size(); i++) {
                if (TVDaysCyclical.getText().equals(""))
                    TVDaysCyclical.setText(daysStr[daysCyclical.get(i)]);
                else
                    TVDaysCyclical.setText(TVDaysCyclical.getText() + "," + daysStr[daysCyclical.get(i)]);
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_script_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Function loading data from Heart model to variables
     */
    public void loadDataFromModel(){
        String scriptNameToEdit = getIntent().getExtras().getString("eFileName");
        rememberedModelName = scriptNameToEdit;
        notificationNumber = "";
        smsNumber = "";
        //if we are creating new model
        if(scriptNameToEdit.isEmpty()) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.edit_script_toolbar);
            //toolbar.setTitle(getResources().getString(R.string.es_Script));
            setSupportActionBar(toolbar);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            for(int i=0;i<7;i++) {
                daysCyclical.add(i);
            }
        }
        //if we are editing already existing model
        else {
            scriptNameToLoad = this.getFilesDir() + "/" + scriptNameToEdit +".ser";
            ModelCreator loadedModel = ModelCreator.loadModel(scriptNameToLoad);

            //reading values of previous attributes from model
            if(!loadedModel.getAttribute("hour").getValues().isEmpty()){
                LinkedList<String> hoursList = loadedModel.getAttribute("hour").getValues();
                LinkedList<String> minutesList = loadedModel.getAttribute("minute").getValues();
                for(int i = 0; i < hoursList.size();++i ){
                    times.add(new TimeItem(Integer.valueOf(hoursList.get(i)),Integer.valueOf(minutesList.get(i))));
                }
            }

            if(!loadedModel.getAttribute("hourRange").getValues().isEmpty()){
                LinkedList<String> hoursList = loadedModel.getAttribute("hourRange").getValues();
                LinkedList<String> minutesList = loadedModel.getAttribute("minuteRange").getValues();
                for(int i = 0; i < hoursList.size();i+=2 ){
                    times.add(new TimeItem(Integer.valueOf(hoursList.get(i)),Integer.valueOf(minutesList.get(i)),Integer.valueOf(hoursList.get(i+1)),Integer.valueOf(minutesList.get(i+1))));
                }
            }

            final String[] daysOfWeekArray = {"mon", "tue", "wed", "thu", "fri", "sat", "sun"};
            for (String day : loadedModel.getAttribute("day").getValues()){
                for(int i = 0; i<daysOfWeekArray.length;++i){
                    if(daysOfWeekArray[i].equals(day)){
                        daysCyclical.add(i);
                    }
                }
            }

            if(!loadedModel.getAttribute("dayFromCalendar").getValues().isEmpty()){
                LinkedList<String> datesList = loadedModel.getAttribute("dayFromCalendar").getValues();
                for(String day : datesList){
                    days.add(new DayItem(Integer.valueOf(day.substring(6,8)),Integer.valueOf(day.substring(4,6)),Integer.valueOf(day.substring(0,4))));
                }
            }

            if(!loadedModel.getAttribute("latitude").getValues().isEmpty()){
                LinkedList<String> latitudesList = loadedModel.getAttribute("latitude").getValues();
                LinkedList<String> longitudesList = loadedModel.getAttribute("longitude").getValues();
                for(int i = 0; i < latitudesList.size();++i){
                    locations.add(new LocationItem(Double.valueOf(latitudesList.get(i)),Double.valueOf(longitudesList.get(i))));
                }
            }

            final String[] argumentsArray = {"bluetooth","wifi","sound"};
            final String[] stateArray = {"on", "off", "vibration"};
            for(int i = 0; i < argumentsArray.length; ++i) {
                for (String action : loadedModel.getAttribute(argumentsArray[i]).getValues()) {
                    for (int j = 0; j < stateArray.length; ++j) {
                        if (stateArray[j].equals(action)) {
                            actions.add(new ActionItem("","",i*2 + j));
                        }
                    }
                }
            }

            if(!loadedModel.getAttribute("notification").getValues().isEmpty()){
                notificationNumber = loadedModel.getAttribute("notificationNumber").getValues().pop();
                LinkedList<String>  notificationsList = loadedModel.getAttribute("notification").getValues();
                for(int i = 0; i < notificationsList.size(); i+=2){
                    ActionItem action = new ActionItem("",notificationsList.get(i) + "\n" + notificationsList.get(i+1),ActionItem.ACTION_SEND_NOTIFICATION);
                    action.setNotificationTitle(notificationsList.get(i));
                    action.setNotificationMessage(notificationsList.get(i+1));
                    actions.add(action);
                }
            }

            if(!loadedModel.getAttribute("sms").getValues().isEmpty()){
                smsNumber = loadedModel.getAttribute("smsNumber").getValues().pop();
                LinkedList<String>  smsList = loadedModel.getAttribute("sms").getValues();
                for(int i = 0; i < smsList.size(); i+=2){
                    ActionItem action = new ActionItem("",smsList.get(i) + "\n" + smsList.get(i+1),ActionItem.ACTION_SEND_SMS);
                    action.setSmsPhoneNumber(smsList.get(i));
                    action.setSmsMessage(smsList.get(i+1));
                    actions.add(action);
                }
            }
        }
    }


    /**
     * Method creating toolbar
     * @param menu
     * @return true if created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_script_toolbar, menu);
        return true;
    }

    /**
     * Method operating toolbar buttons clicked actions
     * @param item selected(clicked) item
     * @return true if button clicked
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info_script:
                AlertDialog.Builder infoBuilder = new AlertDialog.Builder(this);
                infoBuilder.setTitle(getText(R.string.es_InfoTitle))
                        .setMessage(getText(R.string.es_InfoMess))
                        .setPositiveButton(R.string.tp_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                }});
                infoBuilder.show();
                return true;
            case R.id.action_save_script:

                if(ETScriptName.getText().toString().isEmpty()){
                    Toast.makeText(this,getText(R.string.es_NameError),Toast.LENGTH_LONG).show();
                    return false;
                }
                if(actions.isEmpty()){
                    Toast.makeText(this,getText(R.string.es_EmptyScriptError1),Toast.LENGTH_LONG).show();
                    return false;
                }
                if(days.isEmpty() && daysCyclical.isEmpty()){
                    Toast.makeText(this,getText(R.string.es_EmptyScriptError2),Toast.LENGTH_LONG).show();
                    return false;
                }

                if((!days.isEmpty() || !daysCyclical.isEmpty()) && times.isEmpty() && locations.isEmpty()){
                    Toast.makeText(this,getText(R.string.es_EmptyScriptError3),Toast.LENGTH_LONG).show();
                    return false;
                }

                //Creating basic model containing all needed types and attributes
                ModelCreator newModel = ModelCreator.createBasicModel(ETScriptName.getText().toString(), this);

                //Creating lists which are needed in Scheme
                LinkedList<Xattr> attributesList = new LinkedList<>();
                LinkedList<Xattr> attributesToSetList = new LinkedList<>();

                //Creating lists which are needed in Rule
                LinkedList<ALSVExpression> ALSVList = new LinkedList<>();
                LinkedList<DecisionExpression> decisionList = new LinkedList<>();
                LinkedList<ActionExpression> actionList = new LinkedList<>();

                //Creating expressions which will be added to lists
                ALSVExpression alsvExpression = null;
                DecisionExpression decisionExpression = null;
                ActionExpression actionExpression = null;

                //Creatung list of rules
                LinkedList<Xrule> rulesList = new LinkedList<>();

                //TODO: Devide into smoller functions and add some comments
                if(!actions.isEmpty()){
                    boolean notificationToSend = false;
                    boolean smsToSend = false;
                    for(ActionItem action : actions){
                        int actionType = action.getActionType();
                        switch(actionType){
                            case ActionItem.ACTION_BLUETOOTH_ON:
                                attributesToSetList.add(newModel.getAttribute("bluetooth"));
                                newModel.getAttribute("bluetooth").addValue("on");
                                decisionExpression = new DecisionExpression(newModel.getAttribute("bluetooth"), "on");
                                decisionList.add(decisionExpression);
                                actionExpression = new ActionExpression("pl.kit.context_aware.lemur.heartDROID.actions.setBluetooth");
                                actionList.add(actionExpression);
                                break;
                            case ActionItem.ACTION_BLUETOOTH_OFF:
                                attributesToSetList.add(newModel.getAttribute("bluetooth"));
                                newModel.getAttribute("bluetooth").addValue("off");
                                decisionExpression = new DecisionExpression(newModel.getAttribute("bluetooth"), "off");
                                decisionList.add(decisionExpression);
                                actionExpression = new ActionExpression("pl.kit.context_aware.lemur.heartDROID.actions.setBluetooth");
                                actionList.add(actionExpression);
                                break;
                            case ActionItem.ACTION_WIFI_ON:
                                attributesToSetList.add(newModel.getAttribute("wifi"));
                                newModel.getAttribute("wifi").addValue("on");
                                decisionExpression = new DecisionExpression(newModel.getAttribute("wifi"), "on");
                                decisionList.add(decisionExpression);
                                actionExpression = new ActionExpression("pl.kit.context_aware.lemur.heartDROID.actions.setWifi");
                                actionList.add(actionExpression);
                                break;
                            case ActionItem.ACTION_WIFI_OFF:
                                attributesToSetList.add(newModel.getAttribute("wifi"));
                                newModel.getAttribute("wifi").addValue("off");
                                decisionExpression = new DecisionExpression(newModel.getAttribute("wifi"), "off");
                                decisionList.add(decisionExpression);
                                actionExpression = new ActionExpression("pl.kit.context_aware.lemur.heartDROID.actions.setWifi");
                                actionList.add(actionExpression);
                                break;
                            case ActionItem.ACTION_SOUND_OFF:
                                attributesToSetList.add(newModel.getAttribute("sound"));
                                newModel.getAttribute("sound").addValue("off");
                                decisionExpression = new DecisionExpression(newModel.getAttribute("sound"), "off");
                                decisionList.add(decisionExpression);
                                actionExpression = new ActionExpression("pl.kit.context_aware.lemur.heartDROID.actions.setSound");
                                actionList.add(actionExpression);
                                break;
                            case ActionItem.ACTION_SOUND_ON:
                                attributesToSetList.add(newModel.getAttribute("sound"));
                                newModel.getAttribute("sound").addValue("on");
                                decisionExpression = new DecisionExpression(newModel.getAttribute("sound"), "on");
                                decisionList.add(decisionExpression);
                                actionExpression = new ActionExpression("pl.kit.context_aware.lemur.heartDROID.actions.setSound");
                                actionList.add(actionExpression);
                                break;
                            case ActionItem.ACTION_VIBRATIONS_MODE:
                                attributesToSetList.add(newModel.getAttribute("sound"));
                                newModel.getAttribute("sound").addValue("vibration");
                                decisionExpression = new DecisionExpression(newModel.getAttribute("sound"), "vibration");
                                decisionList.add(decisionExpression);
                                actionExpression = new ActionExpression("pl.kit.context_aware.lemur.heartDROID.actions.setSound");
                                actionList.add(actionExpression);
                                break;
                            case ActionItem.ACTION_SEND_NOTIFICATION:
                                notificationToSend = true;
                                newModel.getAttribute("notification").addValue(action.getNotificationTitle());
                                newModel.getAttribute("notification").addValue(action.getNotificationMessage());
                                break;
                            case ActionItem.ACTION_SEND_SMS:
                                smsToSend = true;
                                newModel.getAttribute("sms").addValue(action.getSmsPhoneNumber());
                                newModel.getAttribute("sms").addValue(action.getSmsMessage());
                                break;
                        }
                    }
                    if(notificationToSend){
                        attributesToSetList.add(newModel.getAttribute("notification"));
                        attributesToSetList.add(newModel.getAttribute("notificationNumber"));
                        decisionExpression = new DecisionExpression(newModel.getAttribute("notification"), "sent");
                        decisionList.add(decisionExpression);
                        actionExpression = new ActionExpression("pl.kit.context_aware.lemur.heartDROID.actions.sendNotification");
                        actionList.add(actionExpression);

                        if(!notificationNumber.equals("")){
                            FilesOperations.deleteNotification(this,notificationNumber); //deleting old notification before we create new one
                        }
                        notificationNumber = FilesOperations.createNotification(this,newModel.getAttribute("notification").getValues());
                        newModel.getAttribute("notificationNumber").addValue(notificationNumber);
                        decisionExpression = new DecisionExpression(newModel.getAttribute("notificationNumber"), notificationNumber);
                        decisionList.add(decisionExpression);
                    }
                    if(smsToSend){
                        attributesToSetList.add(newModel.getAttribute("sms"));
                        attributesToSetList.add(newModel.getAttribute("smsNumber"));
                        decisionExpression = new DecisionExpression(newModel.getAttribute("sms"), "sent");
                        decisionList.add(decisionExpression);
                        actionExpression = new ActionExpression("pl.kit.context_aware.lemur.heartDROID.actions.sendSMS");
                        actionList.add(actionExpression);

                        if(!smsNumber.equals("")){
                            FilesOperations.deleteSMS(this,smsNumber); //deleting old notification before we create new one
                        }
                        smsNumber = FilesOperations.createSMS(this,newModel.getAttribute("sms").getValues());
                        newModel.getAttribute("smsNumber").addValue(smsNumber);
                        decisionExpression = new DecisionExpression(newModel.getAttribute("smsNumber"), smsNumber);
                        decisionList.add(decisionExpression);
                    }

                }
                //Adding appropriate arguments to attributesList which is used in creating Xschm
                //Adding attribites which are needed in inference to ALSVlist
                //Setting values of Attributes, needed in loading models
                if (!(times.isEmpty())) {
                    attributesList.add(newModel.getAttribute("time"));
                    LinkedList<String> timesList = new LinkedList<>();

                    for(TimeItem time : times){
                        if(!time.isIntervalType()) {
                            newModel.getAttribute("hour").addValue(String.valueOf(time.getHours()));
                            newModel.getAttribute("minute").addValue(String.valueOf(time.getMinutes()));
                            String timeString = String.valueOf(((double) time.getHours() + ((double) time.getMinutes() / 60)));

                            timesList.add(timeString); // Adding times to list, which will be used in ALSV
                        }
                        else{
                            newModel.getAttribute("hourRange").addValue(String.valueOf(time.getHours()));
                            newModel.getAttribute("hourRange").addValue(String.valueOf(time.getHoursEnd()));
                            newModel.getAttribute("minuteRange").addValue(String.valueOf(time.getMinutes()));
                            newModel.getAttribute("minuteRange").addValue(String.valueOf(time.getMinutesEnd()));
                            String timeString = String.valueOf((double) time.getHours() + ((double) time.getMinutes() / 60)) + " to " + String.valueOf((double) time.getHoursEnd() + ((double) time.getMinutesEnd() / 60));

                            timesList.add(timeString); // Adding times to list, which will be used in ALSV
                        }
                    }
                    alsvExpression = new ALSVExpression(newModel.getAttribute("time"), timesList);
                    ALSVList.add(alsvExpression);
                }
                if (!daysCyclical.isEmpty()) {
                    attributesList.add(newModel.getAttribute("day"));


                    final String[] daysOfWeekArray = {"mon", "tue", "wed", "thu", "fri", "sat", "sun"};
                    LinkedList<String> selectedDays = new LinkedList<>();
                    for (Integer day : daysCyclical) {
                        newModel.getAttribute("day").addValue(daysOfWeekArray[day]);
                        selectedDays.add(daysOfWeekArray[day]);
                    }
                    alsvExpression = new ALSVExpression(newModel.getAttribute("day"), selectedDays);
                    ALSVList.add(alsvExpression);
                }
                if (!days.isEmpty()){
                    attributesList.add(newModel.getAttribute("dayFromCalendar"));

                    LinkedList<String> selectedDays = new LinkedList<>();

                    for(DayItem day : days){
                        String selectedDay = String.format("%4d",day.getYear()) + String.format("%02d",day.getMonth()) + String.format("%02d",day.getDay());
                        newModel.getAttribute("dayFromCalendar").addValue(selectedDay);
                        selectedDays.add(selectedDay);
                    }
                    alsvExpression = new ALSVExpression(newModel.getAttribute("dayFromCalendar"), selectedDays);
                    ALSVList.add(alsvExpression);
                }

                if (!locations.isEmpty()) {
                    attributesList.add(newModel.getAttribute("latitude"));
                    attributesList.add(newModel.getAttribute("longitude"));

                    // Creating and adding scheme to model
                    Xschm scheme = new Xschm("SetEverything", attributesList, attributesToSetList);
                    newModel.addScheme(scheme);
                    int numberOFRule = 1;
                    for(LocationItem location : locations){
                        newModel.getAttribute("latitude").addValue(String.valueOf(location.getLatitude()));
                        newModel.getAttribute("longitude").addValue(String.valueOf(location.getLongitude()));

                        alsvExpression = new ALSVExpression(newModel.getAttribute("latitude"), String.valueOf(location.getLatitude()));
                        ALSVList.add(alsvExpression);
                        alsvExpression = new ALSVExpression(newModel.getAttribute("longitude"), String.valueOf(location.getLongitude()));
                        ALSVList.add(alsvExpression);

                        Xrule rule = new Xrule(scheme, numberOFRule, ALSVList, decisionList, actionList);
                        rulesList.add(rule);

                        ALSVList.removeLast();
                        ALSVList.removeLast();
                        numberOFRule++;
                    }
                }
                else{
                    // Creating and adding scheme to model
                    Xschm scheme = new Xschm("SetEverything", attributesList, attributesToSetList);
                    newModel.addScheme(scheme);
                    //Creating only one rule if no locations are selected
                    Xrule rule = new Xrule(scheme, 1, ALSVList, decisionList, actionList);
                    rulesList.add(rule);
                }

                //Adding rules to the model
                for(Xrule rule : rulesList){
                    newModel.addRule(rule);
                }
                //Saving the model
                newModel.saveModel();

                //deleting old models
                if(!rememberedModelName.equals(ETScriptName.getText().toString())){
                    File fileSer = new File(this.getFilesDir() + "/" + rememberedModelName +".ser");
                    File fileHmr = new File(this.getFilesDir() + "/" + rememberedModelName +".hmr");
                    fileSer.delete();
                    fileHmr.delete();
                }
                Toast.makeText(this, "Script added successfullly!", Toast.LENGTH_SHORT).show();
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    /**
     * DayOfWeekPickerFragment listener for positive button clicked
     * Saves selected items to private linked list.
     * @param dialog DayOfWeekPickerFragment dialog object
     */
    @Override
    public void onDialogDOWPFPositiveClick(DialogFragment dialog) {
        String [] daysStr = this.getResources().getStringArray(R.array.days_short);
        daysCyclical = ((DayOfWeekPickerFragment) dialog).getDays();
        Collections.sort(daysCyclical);
        if(daysCyclical.size() == 7){
            TVDaysCyclical.setText(getText(R.string.es_Everyday));
            return;
        }
        TVDaysCyclical.setText("");
        for(int i=0;i<daysCyclical.size();i++){
            if(TVDaysCyclical.getText().equals("")) TVDaysCyclical.setText(daysStr[daysCyclical.get(i)]);
            else TVDaysCyclical.setText(TVDaysCyclical.getText()+","+daysStr[daysCyclical.get(i)]);
        }

    }

    /**
     * ActionPickerFragment listener for positive button clicked
     * Saves selected items to private linked list.
     * @param dialog ActionPickerFragment dialog object
     */
    @Override
    public void onDialogAPFPositiveClick(DialogFragment dialog) {

        int tmpAction = ((ActionPickerFragment)dialog).getActions();
        if((actions.contains(new ActionItem("","",ActionItem.ACTION_BLUETOOTH_ON)) || actions.contains(new ActionItem("","",ActionItem.ACTION_BLUETOOTH_OFF)))
            && (tmpAction == ActionItem.ACTION_BLUETOOTH_ON || tmpAction == ActionItem.ACTION_BLUETOOTH_OFF)){
            Toast.makeText(this,getText(R.string.es_BloutoothError),Toast.LENGTH_LONG).show();
            return;
        }

        if((actions.contains(new ActionItem("","",ActionItem.ACTION_WIFI_OFF)) || actions.contains(new ActionItem("","",ActionItem.ACTION_WIFI_ON)))
                && (tmpAction == ActionItem.ACTION_WIFI_OFF || tmpAction == ActionItem.ACTION_WIFI_ON)){
            Toast.makeText(this,getText(R.string.es_WiFiError),Toast.LENGTH_LONG).show();
            return;
        }

        if((actions.contains(new ActionItem("","",ActionItem.ACTION_SOUND_ON)) || actions.contains(new ActionItem("","",ActionItem.ACTION_SOUND_OFF)) || actions.contains(new ActionItem("","",ActionItem.ACTION_VIBRATIONS_MODE))
                && (tmpAction == ActionItem.ACTION_SOUND_ON || tmpAction == ActionItem.ACTION_SOUND_OFF || tmpAction == ActionItem.ACTION_VIBRATIONS_MODE))){
            Toast.makeText(this,getText(R.string.es_SoundError),Toast.LENGTH_LONG).show();
            return;
        }

        if(((ActionPickerFragment)dialog).getPosition() == -1){
            actions.add(new ActionItem("","",((ActionPickerFragment)dialog).getActions()));
        } else{
            actions.get(((ActionPickerFragment)dialog).getPosition()).setActionType(((ActionPickerFragment)dialog).getActions());
        }
        actionsAdapter.notifyDataSetChanged();
        ListUtils.setDynamicHeight(listAction);
    }

    /**
     * TimePickerFragment listener for positive button clicked
     * Saves selected items to private linked list.
     * @param dialog TimePickerFragment dialog object
     */
    @Override
    public void onDialogTPFPositiveClick(DialogFragment dialog) {
        if(((TimePickerFragment) dialog).getPosition() == -1) {
            if (((TimePickerFragment) dialog).getTypeInterval() == 0)
                times.add(new TimeItem(((TimePickerFragment) dialog).getHour(), ((TimePickerFragment) dialog).getMinute(), ((TimePickerFragment) dialog).getHour(), ((TimePickerFragment) dialog).getMinute()));
            else if (((TimePickerFragment) dialog).getTypeInterval() == 1) {
                tmpTimeInterval = new TimeItem(((TimePickerFragment) dialog).getHour(), ((TimePickerFragment) dialog).getMinute(), -1, -1);
            } else if (((TimePickerFragment) dialog).getTypeInterval() == 2) {
                tmpTimeInterval.setHoursEnd(((TimePickerFragment) dialog).getHour());
                tmpTimeInterval.setMinutesEnd(((TimePickerFragment) dialog).getMinute());
                if(tmpTimeInterval.isRightIntervalType()) times.add(tmpTimeInterval);
                else Toast.makeText(getBaseContext(),getBaseContext().getResources().getString(R.string.es_WrongInterval),Toast.LENGTH_LONG).show();
            }
        }else{
            if(((TimePickerFragment)dialog).getTypeInterval() == 0) {
                times.get(((TimePickerFragment) dialog).getPosition()).setHours(((TimePickerFragment) dialog).getHour());
                times.get(((TimePickerFragment) dialog).getPosition()).setMinutes(((TimePickerFragment) dialog).getMinute());
                times.get(((TimePickerFragment) dialog).getPosition()).setHoursEnd(((TimePickerFragment) dialog).getHour());
                times.get(((TimePickerFragment) dialog).getPosition()).setMinutesEnd(((TimePickerFragment) dialog).getMinute());
            }else if(((TimePickerFragment)dialog).getTypeInterval() == 1) {
                tmpTimeInterval = new TimeItem(times.get(((TimePickerFragment) dialog).getPosition()));
                times.get(((TimePickerFragment) dialog).getPosition()).setHours(((TimePickerFragment) dialog).getHour());
                times.get(((TimePickerFragment) dialog).getPosition()).setMinutes(((TimePickerFragment) dialog).getMinute());
            }
            else if(((TimePickerFragment)dialog).getTypeInterval() == 2) {
                times.get(((TimePickerFragment) dialog).getPosition()).setHoursEnd(((TimePickerFragment) dialog).getHour());
                times.get(((TimePickerFragment) dialog).getPosition()).setMinutesEnd(((TimePickerFragment) dialog).getMinute());
                if(!times.get(((TimePickerFragment) dialog).getPosition()).isRightIntervalType()){
                    times.get(((TimePickerFragment) dialog).getPosition()).setHours(tmpTimeInterval.getHours());
                    times.get(((TimePickerFragment) dialog).getPosition()).setMinutes(tmpTimeInterval.getMinutes());
                    times.get(((TimePickerFragment) dialog).getPosition()).setHoursEnd(tmpTimeInterval.getHoursEnd());
                    times.get(((TimePickerFragment) dialog).getPosition()).setMinutesEnd(tmpTimeInterval.getMinutesEnd());
                    Toast.makeText(this,this.getResources().getString(R.string.es_WrongInterval),Toast.LENGTH_LONG).show();
                }
            }
        }
        timeAdapter.notifyDataSetChanged();
        ListUtils.setDynamicHeight(listTime);
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * PlacePicker listener for result of the activity
     * Saves selected items to private double pools;
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == RESULT_OK) {
                double latitude,longitude;

                latitude = PlacePicker.getPlace(this,data).getLatLng().latitude;
                latitude = ((double)Math.round(latitude*1000)) / 1000;
                longitude = PlacePicker.getPlace(this,data).getLatLng().longitude;
                longitude = ((double)Math.round(longitude*1000)) / 1000;

                if(requestCode == 1) locations.add(new LocationItem(latitude,longitude));
                if(requestCode != 1){
                    locations.get(requestCode-2).setLatitude(latitude);
                    locations.get(requestCode-2).setLongitude(longitude);
                }
                locationsAdapter.notifyDataSetChanged();
                ListUtils.setDynamicHeight(listLocation);
            }

    }

    /**
     * SMSMessageDetails listener for positive button clicked
     * saves number and message and refreshes list
     * @param dialog SMSMessageDetailsFragment object
     */
    @Override
    public void onSMSMessageDetailsFragmentPositiveClick(DialogFragment dialog) {
        if(((SMSMessageDetailsFragment)dialog).getPosition() == -1){
            ActionItem sms = new ActionItem();
            sms.setSmsPhoneNumber(((SMSMessageDetailsFragment)dialog).getPhoneNo());
            sms.setSmsMessage(((SMSMessageDetailsFragment)dialog).getMessage());
            sms.setActionType(ActionItem.ACTION_SEND_SMS);
            sms.setSubText(sms.getSmsPhoneNumber()+":\n"+sms.getSmsMessage());
            actions.add(sms);
        }else{
            actions.get(((SMSMessageDetailsFragment)dialog).getPosition()).setSmsMessage(((SMSMessageDetailsFragment)dialog).getMessage());
            actions.get(((SMSMessageDetailsFragment)dialog).getPosition()).setSmsPhoneNumber(((SMSMessageDetailsFragment)dialog).getPhoneNo());
            actions.get(((SMSMessageDetailsFragment)dialog).getPosition()).setSubText(((SMSMessageDetailsFragment)dialog).getPhoneNo()+":\n"+((SMSMessageDetailsFragment)dialog).getMessage());
        }
        actionsAdapter.notifyDataSetChanged();
        ListUtils.setDynamicHeight(listAction);
    }

    /**
     * NotificationMessageDetails listener for positive button clicked
     * Saves title and message and refreshes the list
     * @param dialog NotificationMessageDetailsFragment object
     */
    @Override
    public void onNotificationMessageDetailsFragmentPositiveClick(DialogFragment dialog) {
        if(((NotificationMessageDetailsFragment)dialog).getPosition() == -1){
            ActionItem noti = new ActionItem();
            noti.setNotificationMessage(((NotificationMessageDetailsFragment)dialog).getMessage());
            noti.setNotificationTitle(((NotificationMessageDetailsFragment)dialog).getnotiTitle());
            noti.setActionType(ActionItem.ACTION_SEND_NOTIFICATION);
            noti.setSubText(noti.getNotificationTitle()+"\n"+noti.getNotificationMessage());
            actions.add(noti);
        }else{
            actions.get(((NotificationMessageDetailsFragment)dialog).getPosition()).setNotificationMessage(((NotificationMessageDetailsFragment)dialog).getMessage());
            actions.get(((NotificationMessageDetailsFragment)dialog).getPosition()).setNotificationTitle(((NotificationMessageDetailsFragment)dialog).getnotiTitle());
            actions.get(((NotificationMessageDetailsFragment)dialog).getPosition()).setSubText(((NotificationMessageDetailsFragment)dialog).getnotiTitle() +"\n"+((NotificationMessageDetailsFragment)dialog).getMessage());
        }
        actionsAdapter.notifyDataSetChanged();
        ListUtils.setDynamicHeight(listAction);
    }

    /**
     * DataPicker listner for positive button clicked
     * Saves selected data and refreshes list
     * @param dialog DPF Fragment
     */
    @Override
    public void onDialogDPFPositiveClick(DialogFragment dialog) {
        if(((DatePickerFragment)dialog).getPosition() == -1){
            days.add(new DayItem(((DatePickerFragment)dialog).getDay(),((DatePickerFragment)dialog).getMonth(),((DatePickerFragment)dialog).getYear()));
        }else{
            days.get(((DatePickerFragment)dialog).getPosition()).setDay(((DatePickerFragment)dialog).getDay());
            days.get(((DatePickerFragment)dialog).getPosition()).setMonth(((DatePickerFragment)dialog).getMonth());
            days.get(((DatePickerFragment)dialog).getPosition()).setYear(((DatePickerFragment)dialog).getYear());
        }
        daysAdapter.notifyDataSetChanged();
        ListUtils.setDynamicHeight(listDays);
    }


    /**
     * Inner class used to refresh height of listViews
     */
    public static class ListUtils {
        /**
         * Calculates height of the list view
         * @param mListView ListView to calculate height
         */
        public static void setDynamicHeight(ListView mListView) {
            ListAdapter mListAdapter = mListView.getAdapter();
            if (mListAdapter == null) {
                // when adapter is null
                return;
            }
            int height = 0;
            int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
            for (int i = 0; i < mListAdapter.getCount(); i++) {
                View listItem = mListAdapter.getView(i, null, mListView);
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                height += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
            mListView.setLayoutParams(params);
            mListView.requestLayout();
        }
    }
}
