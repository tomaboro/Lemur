package pl.kit.context_aware.lemur.Editor;


import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.LinkedList;

import pl.kit.context_aware.lemur.Editor.RuleExpressions.ALSVExpression;
import pl.kit.context_aware.lemur.Editor.RuleExpressions.ActionExpression;
import pl.kit.context_aware.lemur.Editor.RuleExpressions.DecisionExpression;
import pl.kit.context_aware.lemur.Editor.Xtypes.Xattr;
import pl.kit.context_aware.lemur.Editor.Xtypes.Xrule;
import pl.kit.context_aware.lemur.Editor.Xtypes.Xtype;
import pl.kit.context_aware.lemur.Editor.Xtypes.Xschm;

/**
 * Created by Krzysiek on 2016-12-16.
 */
public class ModelCreator implements Serializable {
    private String modelName;
    private String path;
    private LinkedList<Xtype> types = new LinkedList<>();
    private LinkedList<Xattr> attributes = new LinkedList<>();
    private LinkedList<Xschm>  schemes = new LinkedList<>();
    private LinkedList<Xrule> rules = new LinkedList<>();

    public ModelCreator(String modelName, String path){
        this.modelName = modelName;
        //TODO
        this.path = path;
    }

    public void setModelName(String modelName){
        this.modelName = modelName;
    }
    public void addType(Xtype xtype){
        types.add(xtype);
    }
    public void addAttribute(Xattr xattr){
        attributes.add(xattr);
    }
    public void addScheme(Xschm xschm){
        schemes.add(xschm);
    }
    public void addRule(Xrule xrule){
        rules.add(xrule);
    }

    public Xattr getAttribute(String attrName){
        for(Xattr attribute : attributes){
            if(attrName.equals(attribute.getName())){
                return attribute;
            }
        }
        return null; //TODO exception or sth
    }

    /**
     * Method prints current model in console
     */
    public void printModel(){
        for (Xtype i : types) {
            System.out.println(i.returnStringForModel());
        }
        for (Xattr i : attributes) {
            System.out.println(i.returnStringForModel());
        }
        for (Xschm i : schemes) {
            System.out.println(i.returnStringForModel());
        }
        for (Xrule i : rules) {
            System.out.println(i.returnStringForModel());
        }
    }

    /**
     * Method saves current model in 2 files:
     * 1) modelName.hmr - model which is used by HearTDROID
     * 2) modelName.ser - serialized model which is used in loading models to application
     */
    public void saveModel(){
        PrintWriter out = null;
        File file = new File(this.path + "/" + modelName + ".hmr");
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            out = new PrintWriter(file);
            for (Xtype i : types) {
                out.println(i.returnStringForModel());
            }
            for (Xattr i : attributes) {
                out.println(i.returnStringForModel());
            }
            for (Xschm i : schemes) {
                out.println(i.returnStringForModel());
            }
            for (Xrule i : rules) {
                out.println(i.returnStringForModel());
            }
            fos = new FileOutputStream(this.path + "/" + modelName + ".ser");
            oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
        } catch (IOException e){

        } finally {
            out.close();
          try {
              if (fos != null) fos.close();
          } catch (IOException e) {}
          try {
              if (fos != null) oos.close();
          } catch (IOException e) {}
        }
    }

    /**
     * Static which loads ModelCreator from file and returns it
     * @return ModelCreator loaded from file modelName.ser
     */
    public ModelCreator loadModel(){
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        ModelCreator model = null;
        try{
            fis = new FileInputStream(this.path + "/" + modelName + ".ser");
            ois = new ObjectInputStream(fis);
            model = (ModelCreator) ois.readObject();

        } catch (IOException e){

        } catch (ClassNotFoundException e) {

        } finally {
            try {
                if (fis != null) fis.close();
            } catch (IOException e) {}
            try {
                if (ois != null) ois.close();
            } catch (IOException e) {}
        }
        return model;
    }
    public static ModelCreator createBasicModel(String modelName,Context mContext) {
        // We will use only these types in our model, user cant add his own types
        final Xtype hour_type = new Xtype("hour_type", "numeric", "[0 to 23]");
        final Xtype minute_type = new Xtype("minute_type", "numeric", "[0 to 59");
        final Xtype time_type = new Xtype("time_type", "numeric", "[0.0000 to 23.0000]");
        final Xtype day_type = new Xtype("day_type", "symbolic", "[mon/1,tue/2,wen/3,thu/4,fri/5,sat/6,sun/7]", "yes");
        final Xtype longitude_type = new Xtype("longitude_type", "numeric", "[-180.0000000 to 180.0000000]");
        final Xtype latitude_type = new Xtype("latitude_type", "numeric", "[-90.0000000 to 90.0000000]");
        final Xtype sound_type = new Xtype("sound_type", "symbolic", "[on,off,vibration]");

        //We will use only these arguments in our model, user can't add his own arguments
        final Xattr hour = new Xattr(hour_type, "hour", "hour1", "in", "");
        final Xattr minute = new Xattr(minute_type, "minute", "minute1", "in", "");
        final Xattr time = new Xattr(time_type, "time", "time1", "in", "pl.kit.conext_aware.lemur.HeartDROID.callbacks.getTime");
        final Xattr day = new Xattr(day_type, "day", "day1", "in", "pl.kit.conext_aware.lemur.HeartDROID.callbacks.getDayOfAWeek");
        final Xattr longitude = new Xattr(longitude_type, "longitude", "longitude1", "in", "pl.kit.conext_aware.lemur.HeartDROID.callbacks.getLongitude");
        final Xattr latitude = new Xattr(latitude_type, "latitude", "latitude1", "in", "pl.kit.conext_aware.lemur.HeartDROID.callbacks.getLatitude");
        final Xattr sound = new Xattr(sound_type, "sound", "sound1", "inter", "");

        // Creating model by adding everything into ModelCreator and saving it
        ModelCreator model = new ModelCreator(modelName,mContext.getFilesDir().toString());
        model.addType(hour_type);
        model.addType(minute_type);
        model.addType(time_type);
        model.addType(day_type);
        model.addType(longitude_type);
        model.addType(latitude_type);
        model.addType(sound_type);
        model.addAttribute(hour);
        model.addAttribute(minute);
        model.addAttribute(time);
        model.addAttribute(day);
        model.addAttribute(longitude);
        model.addAttribute(latitude);
        model.addAttribute(sound);

        return model;
    }
}