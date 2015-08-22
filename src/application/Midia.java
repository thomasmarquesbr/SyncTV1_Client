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
public class Midia {
    private String name;
    private int offset;
    private int dur;
    private String type;
    private String url;
    private Map<String, String> listMidia;
    
    Midia(){
    	listMidia = new HashMap<>();
    }
    
//----------------------------------GET-----------------------------------------    
    public String getName(){
        return name;
    }
    
    public int getOffset(){
        return offset;
    }
    
    public int getDur(){
        return dur;
    }
    
    public String getType(){
        return type;
    }
    
    public String getUrl(){
        return url;
    }
    
    public String getUrlMidia(String str){
    	return listMidia.get(str);
    }
    
//----------------------------------SET-----------------------------------------
    public void setName(String str){
        name = str;
    }
    
    public void setOffset(int str){
        offset = str;
    }
    
    public void setDur(int str){
        dur = str;
    }
    
    public void setType(String str){
        type = str;
    }
    
    public void setUrl(String str){
        url = str;
    }
    
    public void addUrlMidia(String res, String url){
    	listMidia.put(res, url);
    }
       
}
