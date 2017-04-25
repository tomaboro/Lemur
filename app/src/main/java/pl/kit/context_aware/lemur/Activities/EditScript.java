package pl.kit.context_aware.lemur.Activities;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import pl.kit.context_aware.lemur.ArrayAdapters.ActionsArrayAdapter;
import pl.kit.context_aware.lemur.ArrayAdapters.DaysArrayAdapter;
import pl.kit.context_aware.lemur.ArrayAdapters.LocationsArrayAdapter;
import pl.kit.context_aware.lemur.ArrayAdapters.TimesArrayAdapter;
import pl.kit.context_aware.lemur.DialogFragments.ActionPickerFragment;
import pl.kit.context_aware.lemur.DialogFragments.DayOfWeekPickerFragment;
import pl.kit.context_aware.lemur.DialogFragments.TimePickerFragment;
import pl.kit.context_aware.lemur.Editor.ModelCreator;
import pl.kit.context_aware.lemur.Editor.RuleExpressions.ALSVExpression;
import pl.kit.context_aware.lemur.Editor.RuleExpressions.ActionExpression;
import pl.kit.context_aware.lemur.Editor.RuleExpressions.DecisionExpression;
import pl.kit.context_aware.lemur.Editor.Xtypes.Xattr;
import pl.kit.context_aware.lemur.Editor.Xtypes.Xrule;
import pl.kit.context_aware.lemur.Editor.Xtypes.Xschm;
import pl.kit.context_aware.lemur.FilesOperations.FilesOperations;
import pl.kit.context_aware.lemur.ListItems.ActionItem;
import pl.kit.context_aware.lemur.ListItems.DayItem;
import pl.kit.context_aware.lemur.ListItems.LocationItem;
import pl.kit.context_aware.lemur.R;
import pl.kit.context_aware.lemur.ListItems.TimeItem;

public class EditScript extends AppCompatActivity implements DayOfWeekPickerFragment.NoticeDialogDOWPFListener
        , ActionPickerFragment.NoticeDialogAPFListener, TimePickerFragment.NoticeDialogTPFListener {

    String rememberedModelName; //used in deleting old models
    private int hour; //hour selected or loaded from file
    private int minute; //minute selected or loaded from file
    private double time; //time calculated with hour and minute
    private Double latitude; //latitude selected or loaded from file
    private Double longitude; //longitude selected or loaded from file
    private String notificationNumber; //used in deleting old files, when we edit model
    private String notificationTitle;
    private String notificationMessage; //TODO
    private String smsNumber; //used in deleting old files, when we edit model
    private String smsPhoneNumber;
    private String smsMessage; //TODO
    private LinkedList<Integer> daysCyclical = new LinkedList<>(); //list of daysCyclical selected or loaded from file
    private LinkedList<Integer> actions = new LinkedList<>(); //list of actions selected or loaded from file
    private static final int PLACE_PICKER_REQUEST = 1; //variable needed for place picker
    private String scriptNameToLoad; // ??
    EditText scriptName; //Edit text field with script name

    private ListView listDays;
    private ListView listTime;
    private ListView listLocation;
    private ListView listAction;

    ArrayList<TimeItem> times;
    ArrayList<DayItem> days;
    ArrayList<LocationItem> locations;
    ArrayList<ActionItem> actionss;

    TimesArrayAdapter timeAdapter;
    DaysArrayAdapter daysAdapter;
    LocationsArrayAdapter locationsAdapter;
    ActionsArrayAdapter actionsAdapter;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /**
     * Action for the Set Time Clicked
     * Opens TimePickerFragment with previously selected items checked
     * @param v current View
     */
    public void SetTimeOnClick(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        ((TimePickerFragment) newFragment).setPosition(-1);
        newFragment.show(getFragmentManager(), "Time Picker");

    }

    /**
     * Action for the Set Day Clicked
     * Opens DayOfWeekPickerFragment with previously selected items checked
     * @param v current View
     */
    public void SetDayOnClick(View v) {
        DialogFragment newFragment = new DayOfWeekPickerFragment();
        ((DayOfWeekPickerFragment) newFragment).setPosition(-1);
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

    public void SetDateOnClick(View v) {
        Toast.makeText(this,"TO DO",Toast.LENGTH_SHORT).show();
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
            int PLACE_PICKER_REQUEST = -1;
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            try {
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }
        else{
            Toast.makeText(this,this.getResources().getString(R.string.pl_fine_location),Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method running at Activity start
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_script);
        Intent intent = getIntent();

        listDays = (ListView)findViewById(R.id.es_lv_days);
        listTime = (ListView)findViewById(R.id.es_lv_time);
        listLocation = (ListView)findViewById(R.id.es_lv_location);
        listAction = (ListView)findViewById(R.id.es_lv_actions);



        days = new ArrayList<DayItem>();
        daysAdapter = new DaysArrayAdapter(this, days);
        listDays.setAdapter(daysAdapter);
        ListUtils.setDynamicHeight(listDays);

        times = new ArrayList<TimeItem>();
        for(int i=0;i<3;i++){
            times.add(new TimeItem(i,i));
        }
        timeAdapter = new TimesArrayAdapter(this, times);
        listTime.setAdapter(timeAdapter);
        ListUtils.setDynamicHeight(listTime);

        locations = new ArrayList<LocationItem>();
        for(int i=0;i<3;i++){
            locations.add(new LocationItem(1.1,i+0.05));
        }
        locationsAdapter = new LocationsArrayAdapter(this,locations);
        listLocation.setAdapter(locationsAdapter);
        ListUtils.setDynamicHeight(listLocation);
        /*
        actionss = new ArrayList<ActionItem>();
        for(int i=0;i<3;i++){
            actionss.add(new ActionItem("MAIN","SUB",i));
        }

        actionsAdapter = new ActionsArrayAdapter(this,actionss);
        listAction.setAdapter(actionsAdapter);
        ListUtils.setDynamicHeight(listAction);
        */
        String scriptNameToEdit = intent.getExtras().getString("eFileName");
        rememberedModelName = scriptNameToEdit;

        hour = -1;
        minute = -1;
        time = -1;
        latitude = null;
        longitude = null;
        notificationNumber = "";
        notificationTitle = "tytul test";
        notificationMessage = "zobaczymy czy dziala"; //TODO
        smsNumber = "";
        smsPhoneNumber = "";
        smsMessage = ""; // TODO
        //if we are creating new model
        if(scriptNameToEdit.isEmpty()) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.edit_script_toolbar);
            scriptName = (EditText) findViewById(R.id.es_set_tiitle_sub);
            //toolbar.setTitle(getResources().getString(R.string.es_Script));
            setSupportActionBar(toolbar);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // ATTENTION: This was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        }
        //if we are editing already existing model
        else {
            scriptNameToLoad = this.getFilesDir() + "/" + scriptNameToEdit +".ser";
            ModelCreator loadedModel = ModelCreator.loadModel(scriptNameToLoad);

            //reading values of previus attributes from model
            if(!loadedModel.getAttribute("minute").getValues().isEmpty()) {
                minute = Integer.valueOf(loadedModel.getAttribute("minute").getValues().getFirst());
            }
            if(!loadedModel.getAttribute("hour").getValues().isEmpty()){
                hour = Integer.valueOf(loadedModel.getAttribute("hour").getValues().getFirst());
            }
            if(!loadedModel.getAttribute("time").getValues().isEmpty()){
                time = Double.valueOf(loadedModel.getAttribute("time").getValues().getFirst());
            }
            if(!loadedModel.getAttribute("latitude").getValues().isEmpty()){
                latitude = Double.valueOf(loadedModel.getAttribute("latitude").getValues().getFirst());
            }
            if(!loadedModel.getAttribute("longitude").getValues().isEmpty()){
                longitude = Double.valueOf(loadedModel.getAttribute("longitude").getValues().getFirst());
            }

            if(!loadedModel.getAttribute("notification").getValues().isEmpty()){ // TODO
                notificationNumber = loadedModel.getAttribute("notificationNumber").getValues().pop();
                notificationTitle = loadedModel.getAttribute("notification").getValues().getFirst();
                notificationMessage = loadedModel.getAttribute("notification").getValues().getLast();
            }
            if(!loadedModel.getAttribute("sms").getValues().isEmpty()){ // TODO
                smsNumber = loadedModel.getAttribute("smsNumber").getValues().pop();
                smsPhoneNumber = loadedModel.getAttribute("sms").getValues().getFirst();
                smsMessage = loadedModel.getAttribute("sms").getValues().getLast();
            }

            final String[] daysOfWeekArray = {"mon", "tue", "wed", "thu", "fri", "sat", "sun"};

            for (String day : loadedModel.getAttribute("day").getValues()){
                for(int i = 0; i<daysOfWeekArray.length;++i){
                    if(daysOfWeekArray[i].equals(day)){
                        daysCyclical.add(i);
                    }
                }
            }


            final String[] argumentsArray = {"bluetooth","wifi","sound","notification","sms"}; //TODO message, it will be different when we implement setting action differently
            final String[] stateArray = {"off", "on", "vibration"};
            for(int j = 0; j < argumentsArray.length; ++j) {
                for (String action : loadedModel.getAttribute(argumentsArray[j]).getValues()) {
                    for (int i = 0; i < stateArray.length; ++i) {
                        if (stateArray[i].equals(action)) {
                            actions.add(i + j*2);
                        }
                    }
                    if (j == 3) {
                        actions.add(7); //TODO
                        break;
                    }
                    else if (j == 4){
                        actions.add(8); //TODO
                        break;
                    }
                }
            }

            Toolbar toolbar = (Toolbar) findViewById(R.id.edit_script_toolbar);



            //filling fields with loaded attributes
            scriptName = (EditText) findViewById(R.id.es_set_tiitle_sub);
            scriptName.setText(scriptNameToEdit);
            TextView timeTV = (TextView) this.findViewById(R.id.es_set_time_sub);
            if(time == -1) {
                timeTV.setText("---");
            }
            else{
                timeTV.setText(hour + ":" + minute);
            }

            TextView daysTV = (TextView) this.findViewById(R.id.es_set_day_sub);
            daysTV.setText("");
            if(daysCyclical.isEmpty()){
                daysTV.setText("---");
            }
            else {
                for (int i = 0; i < daysCyclical.size(); i++) {
                    Resources res = this.getResources();
                    daysTV.setText(daysTV.getText().toString() + res.getStringArray(R.array.days)[daysCyclical.get(i)] + ",");
                }
            }

            TextView LocSubTV = (TextView) findViewById(R.id.es_set_location_sub);
            if(latitude == null){
                LocSubTV.setText("---");
            }
            else{
                LocSubTV.setText( Double.toString(latitude) + "  " + Double.toString(longitude));
            }

            TextView actionsTV = (TextView) this.findViewById(R.id.es_set_action_sub);
            actionsTV.setText("");
            if(actions.isEmpty()){
                actionsTV.setText("---");
            }
            else {
                for (int i = 0; i < actions.size(); i++) {
                    Resources res = this.getResources();
                    actionsTV.setText(actionsTV.getText().toString() + res.getStringArray(R.array.actions)[actions.get(i)] + ",");
                }
            }
            //TODO!!!
            if(notificationMessage.equals("")){
                //notificationTitleTV.setText("");
                //notificationMessageTV.setText("");
            }
            else {
                //notificationTitleTV.setText(notificationTitle);
                //notificationMessageTV.setText(notificationMessage);
            }
            if(smsMessage.equals("")){
                //smsNumberTV.setText("");
                //smsMessageTV.setText("");
            }
            else {
                //smsNumberTV.setText(smsNumber);
                //smsNumberTV.setText(smsMessage);
            }


            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // ATTENTION: This was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

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
            case R.id.action_save_script:
                
                //Creating basic model containing all needed types and attributes
                ModelCreator newModel = ModelCreator.createBasicModel(scriptName.getText().toString(), this);

                //Creating lists which are needed in Scheme
                LinkedList<Xattr> attributesList = new LinkedList<>();
                LinkedList<Xattr> attributesToSetList = new LinkedList<>();

                //Creating lists which are needed in Rule
                LinkedList<ALSVExpression> ALSVList = new LinkedList<>();
                LinkedList<DecisionExpression> decisionList = new LinkedList<>();
                LinkedList<ActionExpression> actionList = new LinkedList<>();

                //Creating expressions which will be added to lists
                ALSVExpression alsv = null;
                DecisionExpression decision = null;
                ActionExpression action = null;

                //Creatung list of rules
                LinkedList<Xrule> rulesList = new LinkedList<>();

                //TODO
                //Adding appropriate arguments to attributesToSetList which is used in creating Xschm
                if (!actions.isEmpty()) {
                    for(Integer actionNumber : actions){ // TODO still need some changes
                        if(actionNumber < 2) attributesToSetList.add(newModel.getAttribute("bluetooth"));
                        else if(actionNumber > 1 && actionNumber < 4) attributesToSetList.add(newModel.getAttribute("wifi"));
                        else if(actionNumber > 3 && actionNumber < 7) attributesToSetList.add(newModel.getAttribute("sound"));
                        else if(actionNumber==7) {
                            attributesToSetList.add(newModel.getAttribute("notification"));
                            attributesToSetList.add(newModel.getAttribute("notificationNumber"));
                        }
                        else if(actionNumber==8){
                            attributesToSetList.add(newModel.getAttribute("sms"));
                            attributesToSetList.add(newModel.getAttribute("smsNumber"));
                        }
                    }

                    //Filling decisionList and actionList with appropriate content
                    final String[] actionsArray = {"off", "on", "vibration"};
                    for (Integer actionNumber : actions) {
                        if(actionNumber < 2){
                            newModel.getAttribute("bluetooth").addValue(actionsArray[actionNumber]);
                            decision = new DecisionExpression(newModel.getAttribute("bluetooth"), actionsArray[actionNumber]);
                            decisionList.add(decision);
                            action = new ActionExpression("pl.kit.context_aware.lemur.HeartDROID.actions.setBluetooth");
                            actionList.add(action);
                        }
                        if(actionNumber > 1 && actionNumber < 4){
                            newModel.getAttribute("wifi").addValue(actionsArray[actionNumber-2]);
                            decision = new DecisionExpression(newModel.getAttribute("wifi"), actionsArray[actionNumber-2]);
                            decisionList.add(decision);
                            action = new ActionExpression("pl.kit.context_aware.lemur.HeartDROID.actions.setWifi");
                            actionList.add(action);
                        }
                        if(actionNumber > 3 && actionNumber < 7){
                            newModel.getAttribute("sound").addValue(actionsArray[actionNumber-4]);
                            decision = new DecisionExpression(newModel.getAttribute("sound"), actionsArray[actionNumber-4]);
                            decisionList.add(decision);
                            action = new ActionExpression("pl.kit.context_aware.lemur.HeartDROID.actions.setSound");
                            actionList.add(action);
                        }
                        if(actionNumber == 7){
                            newModel.getAttribute("notification").addValue(notificationMessage);
                            decision = new DecisionExpression(newModel.getAttribute("notification"), "sent");
                            decisionList.add(decision);
                            action = new ActionExpression("pl.kit.context_aware.lemur.HeartDROID.actions.sendNotification");
                            actionList.add(action);

                            if(!notificationNumber.equals("")){
                                FilesOperations.deleteNotification(this,notificationNumber); //deleting old notification before we create new one
                            }
                            notificationNumber = FilesOperations.createNotification(this,notificationTitle,notificationMessage);
                            newModel.getAttribute("notificationNumber").addValue(notificationNumber);
                            decision = new DecisionExpression(newModel.getAttribute("notificationNumber"), notificationNumber);
                            decisionList.add(decision);

                        }
                        if(actionNumber == 8){
                            newModel.getAttribute("sms").addValue(smsMessage);
                            decision = new DecisionExpression(newModel.getAttribute("sms"), "sent");
                            decisionList.add(decision);
                            action = new ActionExpression("pl.kit.context_aware.lemur.HeartDROID.actions.sendSMS");
                            actionList.add(action);

                            if (!smsNumber.equals("")) {
                                FilesOperations.deleteSMS(this,smsNumber); //deleting old sms before we create new one
                            }
                            smsNumber = FilesOperations.createSMS(this,smsPhoneNumber,smsMessage);
                            newModel.getAttribute("smsNumber").addValue(smsNumber);
                            decision = new DecisionExpression(newModel.getAttribute("smsNumber"), smsNumber);
                            decisionList.add(decision);
                        }
                    } //TODO actions for sending messages
                }

                //Adding messages to model for later loading
                if (!notificationMessage.equals("")){ //TODO
                    newModel.getAttribute("notification").addValue(notificationTitle);
                    newModel.getAttribute("notification").addValue(notificationMessage);
                }
            /*
                if (!smsMessage.equals("")){
                    newModel.getAttribute("sms").addValue(smsPhoneNumber);
                    newModel.getAttribute("sms").addValue(smsMessage);
                }
                */

                //Adding appropriate arguments to attributesList which is used in creating Xschm
                //Adding attribites which are needed in inference to ALSVlist
                //Setting values of Attributes, needed in loading models
                if (!(times.isEmpty())) {
                    attributesList.add(newModel.getAttribute("time"));
                    LinkedList<String> timesList = new LinkedList<>();

                    for(TimeItem time : times){
                        newModel.getAttribute("hour").addValue(String.valueOf(time.getHours()));
                        newModel.getAttribute("minute").addValue(String.valueOf(time.getMinutes()));
                        String timeString =  String.valueOf(((double)time.getHours() + ((double)time.getMinutes() / 60)));
                        newModel.getAttribute("time").addValue(timeString);

                        timesList.add(timeString); // Adding times to list, which will be used in ALSV expression
                    }

                    alsv = new ALSVExpression(newModel.getAttribute("time"), timesList);
                    ALSVList.add(alsv);
                }
                if (!daysCyclical.isEmpty()) {
                    attributesList.add(newModel.getAttribute("day"));

                    final String[] daysOfWeekArray = {"mon", "tue", "wed", "thu", "fri", "sat", "sun"};
                    LinkedList<String> selectedDays = new LinkedList<>();
                    for (Integer day : daysCyclical) {
                        newModel.getAttribute("day").addValue(daysOfWeekArray[day]);
                        selectedDays.add(daysOfWeekArray[day]);
                    }
                    alsv = new ALSVExpression(newModel.getAttribute("day"), selectedDays);
                    ALSVList.add(alsv);
                }
                if (!days.isEmpty()){
                    attributesList.add(newModel.getAttribute("dayFromCalendar"));

                    LinkedList<String> selectedDays = new LinkedList<>();

                    for(DayItem day : days){
                        String selectedDay = String.format("%4d",day.getYear()) + String.format("%02d",day.getMonth()) + String.format("%02d",day.getDay());
                        newModel.getAttribute("dayFromCalendar").addValue(selectedDay);
                        selectedDays.add(selectedDay);
                    }
                    alsv = new ALSVExpression(newModel.getAttribute("dayFromCalendar"), selectedDays);
                    ALSVList.add(alsv);
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

                        alsv = new ALSVExpression(newModel.getAttribute("latitude"), String.valueOf(location.getLatitude()));
                        ALSVList.add(alsv);
                        alsv = new ALSVExpression(newModel.getAttribute("longitude"), String.valueOf(location.getLongitude()));
                        ALSVList.add(alsv);

                        Xrule rule = new Xrule(scheme, numberOFRule, ALSVList, decisionList, actionList);
                        rulesList.add(rule);

                        ALSVList.remove();
                        ALSVList.remove();
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
                newModel.saveModel();

                //deleting old models
                if(!rememberedModelName.equals(scriptName.getText().toString())){
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
        if(((DayOfWeekPickerFragment) dialog).getPosition() == -1) {
            days.add(new DayItem(((DayOfWeekPickerFragment) dialog).getDays(),1));
        }else{
            days.get(((DayOfWeekPickerFragment) dialog).getPosition()).setDayOfWeek(((DayOfWeekPickerFragment) dialog).getDays());
        }
        daysAdapter.notifyDataSetChanged();
        ListUtils.setDynamicHeight(listDays);
    }

    /**
     * DayOfWeekPickerFragment listener for negative button clicked
     * Saves selected items to private linked list.
     * @param dialog DayOfWeekPickerFragment dialog object
     */
    @Override
    public void onDialogDOWPFNegativeClick(DialogFragment dialog) {

    }

    /**
     * ActionPickerFragment listener for positive button clicked
     * Saves selected items to private linked list.
     * @param dialog ActionPickerFragment dialog object
     */
    @Override
    public void onDialogAPFPositiveClick(DialogFragment dialog) {
        actions = (LinkedList<Integer>) ((ActionPickerFragment) dialog).getActions().clone();
    }

    /**
     * ActionPickerFragment listener for negative button clicked
     * Saves selected items to private linked list.
     * @param dialog ActionPickerFragment dialog object
     */
    @Override
    public void onDialogAPFNegativeClick(DialogFragment dialog) {

    }

    /**
     * TimePickerFragment listener for positive button clicked
     * Saves selected items to private linked list.
     * @param dialog TimePickerFragment dialog object
     */
    @Override
    public void onDialogTPFPositiveClick(DialogFragment dialog) {
        if(((TimePickerFragment) dialog).getPosition() == -1) {
            times.add(new TimeItem(((TimePickerFragment) dialog).getHour(),((TimePickerFragment) dialog).getMinute()));
        }else{
            times.get(((TimePickerFragment) dialog).getPosition()).setHours(((TimePickerFragment) dialog).getHour());
            times.get(((TimePickerFragment) dialog).getPosition()).setMinutes(((TimePickerFragment) dialog).getMinute());
            Log.d("APPP",Integer.toString(((TimePickerFragment) dialog).getHour()));
        }
        timeAdapter.notifyDataSetChanged();
        ListUtils.setDynamicHeight(listTime);
    }

    /**
     * TimePickerFragment listener for negative button clicked
     * Saves selected items to private linked list.
     * @param dialog TimePickerFragment dialog object
     */
    @Override
    public void onDialogTPFNegativeClick(DialogFragment dialog) {

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("EditScript Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    /**
     * PlacePicker listener for result of the activity
     * Saves selected items to private double pools;
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == RESULT_OK) {
                double latitude,longitude;
                Place place = PlacePicker.getPlace(data, this);
                latitude = place.getLatLng().latitude;
                latitude = ((double)Math.round(latitude*1000)) / 1000;
                longitude = place.getLatLng().longitude;
                longitude = ((double)Math.round(longitude*1000)) / 1000;

                if(requestCode == -1) locations.add(new LocationItem(latitude,longitude));
                if(requestCode != -1){
                    locations.get(requestCode).setLatitude(latitude);
                    locations.get(requestCode).setLongitude(longitude);
                }
                locationsAdapter.notifyDataSetChanged();
                ListUtils.setDynamicHeight(listLocation);
            }

    }

    public static class ListUtils {
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
