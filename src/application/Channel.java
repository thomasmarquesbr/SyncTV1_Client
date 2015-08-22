/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Thomas
 */
public class Channel {
    private String name;
    private Map<String,Programming> listProgramming;
    
    Channel(){
        listProgramming = new HashMap<>();
    }
    
    //------------------------------GET----------------------------------------
    public String getName(){
        return name;
    }
    
    public Map getListProgramming(){
        return listProgramming;
    }
    
    public Programming getProgramming(String str){
        return listProgramming.get(str);
    }
    
    public ArrayList<Programming> getListProgVoD(){
    	ArrayList<Programming> listProgVoD = new ArrayList<>();
    	
    	Set<String> keyset = listProgramming.keySet();
    	for (String string : keyset) {
    		if(listProgramming.get(string).getService().equals("VoD")){
    			listProgVoD.add(listProgramming.get(string));
    		}
    	}
    	
    	return listProgVoD;
    	/*Map<String,Programming> listProgVoD = new HashMap<>();
    	
    	Set<String> keyset = listProgramming.keySet();
    	for (String string : keyset) {
    		if(listProgramming.get(string).getService().equals("VoD")){
    			listProgVoD.put(listProgramming.get(string).getName(), listProgramming.get(string));
    		}
    	}
    	return listProgVoD;*/
    }
    
    //-------------------------------SET----------------------------------------
    public void setName (String str){
        name = str;
    }
    
    public void addProgramming(String str, Programming prog){
        listProgramming.put(str, prog);
    }
}
