/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Thomas
 */
public class Programming {
    private String name;
    private String date;
    private String hour;
    private String url;
    private String avail;
    private String type;
    private String service;
    private Map<String,Midia> listMidia;
 //   private NCLDocument nclDocument;
    
    Programming(){
        listMidia = new HashMap<>();
       
    }
    
    //----------------------------GET-------------------------------------------
    public String getName(){
        return name;
    }
    
    public String getDate(){
        return date;
    }
    
    public String getHour(){
        return hour;
    }
    
    public String getUrl(){
        return url;
    }
    
    public Map getListMidia(){
        return listMidia;
    }
    
    public String getAvailable(){
        return avail;
    }
    
    public String getType(){
        return type;
    }
    
    public String getService(){
    	return service;
    }
    
    public Midia getMidia(String str){
        return listMidia.get(str);
    }
    
 /*   public NCLDocument getNCLDocument(){
        return nclDocument;
    }
   */ 
    //-----------------------------SET------------------------------------------
    public void setName(String str){
        name = str;
    }
    
    public void setDate(String str){
        date = str;
    }
    
    public void setHour(String str){
        hour = str;
    }
    
    public void setUrl(String str){
        url = str;
    }
    
    public void setAvailable(String d){
        avail = d;
    }
    
    public void setType(String t){
        type = t;
    }
    
    public void setService(String str){
    	service = str;
    }
    
    public void addMidia(String str, Midia midia){
        listMidia.put(str, midia);
    }
    
  /*  public void setNCLDocument(NCLDocument ncl){
        nclDocument = new NCLDocument(null, null);
        nclDocument = ncl;
    }*/
}
