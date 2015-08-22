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
public class SyncTV {
    
    private String url;
    private Map<String,Channel> listChannel;
    
    
    SyncTV(){
        listChannel= new HashMap<>();
    }
    
    //-------------------------------GET----------------------------------------
    public String getURL(){
        return url;
    }
    
    public Map getListChannel(){
        return listChannel;
    }
    
    public Channel getChannel(String str){
        return listChannel.get(str);
    }
    
    //-------------------------------SET----------------------------------------
    public void setUrl(String str){
        url = str;
    }
    
    public void addChannel(String str, Channel channel){
        listChannel.put(str, channel);
    }
}
