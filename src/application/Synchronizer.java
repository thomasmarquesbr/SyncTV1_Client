/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Thomas
 */
public class Synchronizer extends Thread{
    private Main syncTVApp;
    private SyncTV synctv;
  //  private Controller controller = new Controller();
    private String optChannel;
    private String pathFolderUpload;
    private String pathMainXml;
    private boolean keepRunning;
   // private int durProg = 7200;
    
    Synchronizer(String strPathMainXml, String strPathFolderUpload,Main main){
    	this.pathMainXml = strPathMainXml;
    	this.pathFolderUpload = strPathFolderUpload;
    	this.syncTVApp = main;
    }

    public String getPathFolderUpload(){
    	return pathFolderUpload;
    }
    
    public boolean loadSynctv(){
        synctv = new SyncTV();
                    
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
	        Document doc = db.parse(pathMainXml);
	        
	        Element elem = doc.getDocumentElement();
	        //System.out.println("synctv -> url="+elem.getAttribute("url"));
	        synctv.setUrl(elem.getAttribute("url"));
	        
	        NodeList listchannel = elem.getElementsByTagName( "channel" );
	        for(int i=0; i <  listchannel.getLength(); i++){
	        	Element channel = (Element)listchannel.item(i);
	        	Channel c = new Channel();
	        	c.setName(channel.getAttribute("name"));
	        	
	        	NodeList listprog = channel.getElementsByTagName("programming");
	            for(int j=0; j < listprog.getLength(); j++){//varrendo elementos <programming>
		            Element prog = (Element)listprog.item(j);
		            Programming p = new Programming();
		            p.setName(prog.getAttribute("name"));
	                p.setDate(prog.getAttribute("date"));
	                p.setHour(prog.getAttribute("hour"));
	                p.setUrl(prog.getAttribute("url"));
	                p.setAvailable(prog.getAttribute("available"));
	                p.setType(prog.getAttribute("type"));
	                p.setService( prog.getAttribute("service"));
	            
	                if(p.getType().equals("synctv")){//baixa programação synctv
	                	
	                	//Analisando cada um dos xml`s referentes a cada programação
	                	DocumentBuilderFactory dbf2 = DocumentBuilderFactory.newInstance();
	                    DocumentBuilder db2 = dbf2.newDocumentBuilder();
	                    Document doc2 = db2.parse( pathFolderUpload+prog.getAttribute("name")+"/"+prog.getAttribute("name")+".xml" );
	
	                    Element prog1 = doc2.getDocumentElement();//pegando elemento root <programming> referente a cada programação            
	                    NodeList listmidia = prog1.getElementsByTagName( "media" );
	                    for(int k=0; k < listmidia.getLength(); k++){// varrendo elementos de midia <media>
	                    	Element midia = (Element)listmidia.item(k);
	                    	Midia m = new Midia();
	                        m.setName(midia.getAttribute("name"));
	                        m.setType(midia.getAttribute("type"));
	                        m.setUrl(midia.getAttribute("url"));
	                        //verifica se há valores de offset e dur para salvar
	                        if (!"".equals(midia.getAttribute("offset")))
	                            m.setOffset(Integer.parseInt(midia.getAttribute("offset")));
	                        if (!"".equals(midia.getAttribute("dur")))
	                            m.setDur(Integer.parseInt(midia.getAttribute("dur")));
	                        
	                        //Verifica os formatos de vídeo
	                    	if(m.getType().equals("video")){                   		
	                    		NodeList propList = midia.getElementsByTagName("properties");
	                    		for(int w=0; w<propList.getLength(); w++){
	                    			Element prop = (Element)propList.item(w);
	                    			m.addUrlMidia(prop.getAttribute("resolution"), prop.getAttribute("url"));
	                    		}
	                    	}
	                        p.addMidia(m.getName(), m);
	                    }
	                }else{
	                	if(p.getType().equals("ncl")){//baixa programação ncl
	                		//loadNCL2(p);
	                		System.out.println("loadNCL");
	                	}
	                }
	                c.addProgramming(p.getName(), p);
	            }
	        	synctv.addChannel(c.getName(), c);
	        }    
	        return true;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			return false;
		}
    }
    
    public SyncTV getSyncTV(){
    	return synctv;
    }
    
    public void stopIt(){
    	this.keepRunning = false;
    }
    
    @Override
    public void run(){
    	keepRunning = true;
        while(keepRunning){
        	
            Set<String> keys = synctv.getChannel(optChannel).getListProgramming().keySet();
            for(final String key : keys){//varrendo programações
                String dtProg = synctv.getChannel(optChannel).getProgramming(key).getDate();
                String hrProg = synctv.getChannel(optChannel).getProgramming(key).getHour(); 
                String availProg = synctv.getChannel(optChannel).getProgramming(key).getAvailable();
                
                Calendar calProg =  Calendar.getInstance();
                calProg.set(Integer.parseInt(dtProg.substring(6,10)), Integer.parseInt(dtProg.substring(3,5)) ,Integer.parseInt(dtProg.substring(0,2)) ,Integer.parseInt(hrProg.substring(0,2)) ,Integer.parseInt(hrProg.substring(3,5)) ,0);
                
                
                
                Calendar calCurrent = Calendar.getInstance();
                                
                dtProg = calCurrent.get(Calendar.DAY_OF_MONTH)+"/"+calCurrent.get(Calendar.MONTH)+"/"+calCurrent.get(Calendar.YEAR) ;
                String dtCurrent = calCurrent.get(Calendar.DAY_OF_MONTH)+"/"+calCurrent.get(Calendar.MONTH)+"/"+calCurrent.get(Calendar.YEAR);
                //verifica se a data da programação coincide com a data atual
                if(dtProg.equals(dtCurrent)){
                    int hrCurrentInSec = (calCurrent.get(Calendar.HOUR_OF_DAY))*3600+(calCurrent.get(Calendar.MINUTE))*60+(calCurrent.get(Calendar.SECOND));
                    int hrProgInSec = (calProg.get(Calendar.HOUR_OF_DAY))*3600+(calProg.get(Calendar.MINUTE))*60+(calProg.get(Calendar.SECOND));
                    int availInSec = ((Integer.parseInt(availProg.substring(0, 2)))*3600 + (Integer.parseInt(availProg.substring(3, 5)))*60);
                    
                   //hora atual coincide com hora da prog && menor que o tempo de disponibilidade da prog
                   if((hrCurrentInSec >= hrProgInSec)&&(hrCurrentInSec <= (hrProgInSec+availInSec))){
                   
                       
                        Set<String> keysMedia= synctv.getChannel(optChannel).getProgramming(key).getListMidia().keySet();
                        for(final String keyMedia: keysMedia){//Varrendo Imagens
                            
                            
                            int offset = synctv.getChannel(optChannel).getProgramming(key).getMidia(keyMedia).getOffset();
                            //hora atual coincide com hora da prog + offset do item de midia
                            if(hrCurrentInSec == (hrProgInSec + offset)){
                                                                
                               // if(hrCurrentInSec >= (hrProgInSec + offset))//verifica se ja passou a hora da exibição e add na lista de midias do historico
                                    //if(!syncTVApp.getListProg().containsKey(key))
                                        //syncTVApp.addMediaList(synctv.getChannel(optChannel).getProgramming(key), synctv.getChannel(optChannel).getProgramming(key).getMidia(keyImg));
                                    //syncTVApp.addMediaList(synctv.getChannel(optChannel).getProgramming(key).getMidia(keyImg));
                                System.out.println(synctv.getChannel(optChannel).getProgramming(key).getMidia(keyMedia).getName());    
                                
										syncTVApp.showMedia(synctv.getChannel(optChannel).getProgramming(key).getMidia(keyMedia));
										try {
											Thread.sleep(1000);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										//syncTVApp.stopShowMedia(synctv.getChannel(optChannel).getProgramming(key).getMidia(keyMedia));
									
                                
                             /*  if(!controller.showingMidia()){
                                    controller = new Controller();
                                    controller.setSyncTVApp(syncTVApp);
                                    controller.setMidia(synctv.getChannel(optChannel).getProgramming(key).getMidia(keyImg));
                                    controller.start();
                                    
                            	    syncTVApp.showMedia(synctv.getChannel(optChannel).getProgramming(key).getMidia(keyImg));
                                    
                               }else{
                                   // syncTVApp.showWarning(synctv.getChannel(optChannel).getProgramming(key).getMidia(keyImg));
                                }*/
                                
                                //syncTVApp.showMidia(synctv, optChannel, key, keyImg)
                                //System.out.println("Ok");
                                
                                //delay de 1/2 segundo para reduzir processamento sem perder a sincronização
                               /* try { sleep(500); } catch (InterruptedException ex) { Logger.getLogger(Synchronizer.class.getName()).log(Level.SEVERE, null, ex); }
                            */}
                        }
                   }
                }
                
                
                
            }
            
            try {    
                sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(Synchronizer.class.getName()).log(Level.SEVERE, null, ex);
            }
    
        }
        
    }
    
    public void setOptChannel(String str){
        optChannel = str;
    }
    /*
    public SyncTV getSyncTV(){
        return synctv;
    }
    
    public void loadNCL(Programming p) throws ParserConfigurationException, SAXException, IOException{
        
        DocumentBuilderFactory docBuilderFac = DocumentBuilderFactory.newInstance();
        DocumentBuilder DocBuilder = docBuilderFac.newDocumentBuilder();
        Document document = DocBuilder.parse(p.getUrl());
        
        //<NCL>
        Element ncl = document.getDocumentElement();
        NCLDocument nclDocument = new NCLDocument(ncl.getAttribute("id"), ncl.getAttribute("xmlns"));
       
        
            //<HEAD>
            NodeList listhead = ncl.getElementsByTagName("head");
            Element elemHead = (Element)listhead.item(0);
            Head objHead = new Head();          
        
                //<connectorBase>
                NodeList listConnBase = elemHead.getElementsByTagName("connectorBase");
                Element elemConnBase = (Element)listConnBase.item(0);
                ConnectorBase objConnectorBase = new ConnectorBase();
                	
                	//<importBase>
                	NodeList listImportBase = elemConnBase.getElementsByTagName("importBase");
                	for(int i=0; i < listImportBase.getLength(); i++){
                		Element elemImportBase = (Element)listImportBase.item(i);
                		ImportBase objImportBase = new ImportBase(elemImportBase.getAttribute("documentURI"), elemImportBase.getAttribute("alias"));     
                		
                		if(elemImportBase.hasAttribute("region"))
                			objImportBase.setRegion(elemImportBase.getAttribute("region"));
                		
                		//System.out.println(objImportBase.getDocumentURI());
                		objConnectorBase.addImportBase(objImportBase);
                	}//</importBase>
                
                    //<causalConnector>                    
                    NodeList listCConnector = elemConnBase.getElementsByTagName("causalConnector");
                    for(int i=0; i < listCConnector.getLength(); i++){
                        Element elemCConnector = (Element)listCConnector.item(i);
                        CausalConnector objCConnector = new CausalConnector(elemCConnector.getAttribute("id"));
                        
                        //<connectorParam>
                        NodeList listConnParam = elemCConnector.getElementsByTagName("connectorParam");
                        for(int j = 0; j < listConnParam.getLength(); j++){
                            Element elemConnParam = (Element)listConnParam.item(j);
                            ConnectorParam objConnParam = new ConnectorParam(elemConnParam.getAttribute("name"));
                            
                            if(elemConnParam.hasAttribute("type"))
                                objConnParam.setType(elemConnParam.getAttribute("type"));
                            
                            //System.out.println(objConnParam.getName());
                            objCConnector.addConnectorParam(objConnParam);
                        }//</connectorParam>
                        
                        //<simpleCondition>
                        NodeList listSimpCond = elemCConnector.getElementsByTagName("simpleCondition");
                        for(int j = 0; j < listSimpCond.getLength(); j++){
                            Element elemSimpCondition = (Element)listSimpCond.item(j);
                            SimpleCondition objSimpCondition = new SimpleCondition(elemSimpCondition.getAttribute("role"));
                            
                            if(elemSimpCondition.hasAttribute("delay"))
                                objSimpCondition.setDelay(elemSimpCondition.getAttribute("delay"));
                            if(elemSimpCondition.hasAttribute("eventType"))
                                objSimpCondition.setEventType(elemSimpCondition.getAttribute("eventType"));
                            if(elemSimpCondition.hasAttribute("key"))
                                objSimpCondition.setKey(elemSimpCondition.getAttribute("key"));
                            if(elemSimpCondition.hasAttribute("max"))
                                objSimpCondition.setMax(elemSimpCondition.getAttribute("max"));
                            if(elemSimpCondition.hasAttribute("min"))
                                objSimpCondition.setMin(elemSimpCondition.getAttribute("min"));
                            if(elemSimpCondition.hasAttribute("qualifier"))
                                objSimpCondition.setQualifier(elemSimpCondition.getAttribute("qualifier"));
                            if(elemSimpCondition.hasAttribute("transition"))
                                objSimpCondition.setTransition(elemSimpCondition.getAttribute("transition"));
                            
                           // System.out.println(objSimpCondition.getEventType());
                            objCConnector.setSimpleCondition(objSimpCondition);
                        }//</simpleCondition>
                        
                        //<simpleAction>
                        NodeList listSimpAction = elemCConnector.getElementsByTagName("simpleAction");
                        for(int j=0; j < listSimpAction.getLength(); j++){
                            Element elemSimpAction = (Element)listSimpAction.item(j);
                            SimpleAction objSimpAction = new SimpleAction(elemSimpAction.getAttribute("role"));
                            
                            if(elemSimpAction.hasAttribute("actionType"))
                                objSimpAction.setActionType(elemSimpAction.getAttribute("actionType"));
                            if(elemSimpAction.hasAttribute("by"))
                                objSimpAction.setBy(elemSimpAction.getAttribute("by"));
                            if(elemSimpAction.hasAttribute("delay"))
                                objSimpAction.setDelay(elemSimpAction.getAttribute("delay"));
                            if(elemSimpAction.hasAttribute("duration"))
                                objSimpAction.setDuration(elemSimpAction.getAttribute("duration"));
                            if(elemSimpAction.hasAttribute("eventType"))
                                objSimpAction.setEventType(elemSimpAction.getAttribute("eventType"));
                            if(elemSimpAction.hasAttribute("key"))
                                objSimpAction.setKey(elemSimpAction.getAttribute("key"));
                            if(elemSimpAction.hasAttribute("max"))
                                objSimpAction.setMax(elemSimpAction.getAttribute("max"));
                            if(elemSimpAction.hasAttribute("min"))
                                objSimpAction.setMin(elemSimpAction.getAttribute("min"));
                            if(elemSimpAction.hasAttribute("qualifier"))
                                objSimpAction.setQualifier(elemSimpAction.getAttribute("qualifier"));
                            if(elemSimpAction.hasAttribute("repeat"))
                                objSimpAction.setRepeat(elemSimpAction.getAttribute("repeat"));
                            if(elemSimpAction.hasAttribute("repeadDelay"))
                                objSimpAction.setRepeatDelay(elemSimpAction.getAttribute("repeatDelay"));
                            if(elemSimpAction.hasAttribute("transition"))
                                objSimpAction.setTransition(elemSimpAction.getAttribute("transition"));
                            
                            //System.out.println(listSimpAction.getLength());
                            objCConnector.setSimpleAction(objSimpAction);
                        }//</simpleAction>
                        
                        //<compoundCondition>
                        NodeList listCompCondition = elemCConnector.getElementsByTagName("compoundCondition");
                        for(int j=0; j < listCompCondition.getLength(); j++){
                            Element elemCompCondition = (Element)listCompCondition.item(j);
                            CompoundCondition objCompCondition = new CompoundCondition(elemCompCondition.getAttribute("operator"));
                            
                            if(elemCompCondition.hasAttribute("delay"))
                                objCompCondition.setDelay(elemCompCondition.getAttribute("delay"));
                            
                            //<simpleCondition>
                            NodeList listSimpCondition = elemCompCondition.getElementsByTagName("simpleCondition");
                            for(int k = 0; k < listSimpCondition.getLength(); k++){
                                Element elemSimpCondition = (Element)listSimpCondition.item(k);
                                SimpleCondition objSimpCondition = new SimpleCondition(elemSimpCondition.getAttribute("role"));
                                
                                if(elemSimpCondition.hasAttribute("delay"))
                                    objSimpCondition.setDelay(elemSimpCondition.getAttribute("delay"));
                                if(elemSimpCondition.hasAttribute("eventType"))
                                    objSimpCondition.setEventType(elemSimpCondition.getAttribute("eventType"));
                                if(elemSimpCondition.hasAttribute("key"))
                                    objSimpCondition.setKey(elemSimpCondition.getAttribute("key"));
                                if(elemSimpCondition.hasAttribute("max"))
                                    objSimpCondition.setMax(elemSimpCondition.getAttribute("max"));
                                if(elemSimpCondition.hasAttribute("min"))
                                    objSimpCondition.setMin(elemSimpCondition.getAttribute("min"));
                                if(elemSimpCondition.hasAttribute("qualifier"))
                                    objSimpCondition.setQualifier(elemSimpCondition.getAttribute("qualifier"));
                                if(elemSimpCondition.hasAttribute("transition"))
                                    objSimpCondition.setTransition(elemSimpCondition.getAttribute("transition"));
                                
                                
                                //System.out.println(listSimpCondition.getLength());
                                objCompCondition.addSimpleCondition(objSimpCondition);                                        
                            }//</simpleCondition>
                            
                            //<assessmentStatement>
                            NodeList listAssessmentStatement = elemCompCondition.getElementsByTagName("assessmentStatement");
                            for(int k = 0; k < listAssessmentStatement.getLength(); k++){
                            	Element elemAssessmentStatement = (Element)listAssessmentStatement.item(k);
                            	AssessmentStatement objAssessmentStatement = new AssessmentStatement(elemAssessmentStatement.getAttribute("comparator"));
                            	
                            	//<attributeAssessment>
                            	NodeList listAttributeAssessment = elemAssessmentStatement.getElementsByTagName("attributeAssessment");
                            	for(int w = 0; w < listAttributeAssessment.getLength(); w++){
                            		Element elemAttributeAssessment = (Element)listAttributeAssessment.item(w);
                            		AttributeAssessment objAttributeAssessment = new AttributeAssessment(elemAttributeAssessment.getAttribute("role"), elemAttributeAssessment.getAttribute("eventType"));
                            		
                            		if(elemAttributeAssessment.hasAttribute("key"))
                            			objAttributeAssessment.setKey(elemAttributeAssessment.getAttribute("key"));
                            		if(elemAttributeAssessment.hasAttribute("attributeType"))
                            			objAttributeAssessment.setAttributeType(elemAttributeAssessment.getAttribute("attributeType"));
                            		if(elemAttributeAssessment.hasAttribute("offset"))
                            			objAttributeAssessment.setOffset(elemAttributeAssessment.getAttribute("offset"));
                            		
                            		//System.out.println(objAttributeAssessment.getAttributeType());
                            		objAssessmentStatement.setAttributeAssessment(objAttributeAssessment);
                            	}//</attributeAssessment>
                            	
                            	//<valueAssessment>
                            	NodeList listValueAssessment = elemAssessmentStatement.getElementsByTagName("valueAssessment");
                            	for(int w = 0; w < listValueAssessment.getLength(); w++){
                            		Element elemValueAssessment = (Element)listValueAssessment.item(w);
                            		ValueAssessment objValueAssessment = new ValueAssessment(elemValueAssessment.getAttribute("value"));
       
                            		//System.out.println(objValueAssessment.getValue());
                            		objAssessmentStatement.setValueAssessment(objValueAssessment);
                            	}//</valueAssessment>
                            	
                            	//System.out.println(objAssessmentStatement.getComparator());
                            	objCompCondition.setAssessementStatement(objAssessmentStatement);
                            }//</assessmentStatement>
                            
                            //<compoundStatement>
                            NodeList listCompoundStatement = elemCompCondition.getElementsByTagName("compoundStatement");
                            for(int t = 0; t < listCompoundStatement.getLength(); t++){
                            	Element elemCompoundStatement = (Element)listCompoundStatement.item(t);
                            	CompoundStatement objCompoundStatement = new CompoundStatement(elemCompoundStatement.getAttribute("operator"));
                            	
                            	if(elemCompoundStatement.hasAttribute("isNegated"))
                            		objCompoundStatement.setIsNegated(elemCompoundStatement.getAttribute("isNegated"));
                            	
                            	//<assessmentStatement>2
                                NodeList listAssessmentStatement2 = elemCompoundStatement.getElementsByTagName("assessmentStatement");
                                for(int k = 0; k < listAssessmentStatement2.getLength(); k++){
                                	Element elemAssessmentStatement2 = (Element)listAssessmentStatement2.item(k);
                                	AssessmentStatement objAssessmentStatement2 = new AssessmentStatement(elemAssessmentStatement2.getAttribute("comparator"));
                                	
                                	//<attributeAssessment>2
                                	NodeList listAttributeAssessment2 = elemAssessmentStatement2.getElementsByTagName("attributeAssessment");
                                	for(int w = 0; w < listAttributeAssessment2.getLength(); w++){
                                		Element elemAttributeAssessment2 = (Element)listAttributeAssessment2.item(w);
                                		AttributeAssessment objAttributeAssessment2 = new AttributeAssessment(elemAttributeAssessment2.getAttribute("role"), elemAttributeAssessment2.getAttribute("eventType"));
                                		
                                		if(elemAttributeAssessment2.hasAttribute("key"))
                                			objAttributeAssessment2.setKey(elemAttributeAssessment2.getAttribute("key"));
                                		if(elemAttributeAssessment2.hasAttribute("attributeType"))
                                			objAttributeAssessment2.setAttributeType(elemAttributeAssessment2.getAttribute("attributeType"));
                                		if(elemAttributeAssessment2.hasAttribute("offset"))
                                			objAttributeAssessment2.setOffset(elemAttributeAssessment2.getAttribute("offset"));
                                		
                                		//System.out.println(objAttributeAssessment2.getRole());
                                		objAssessmentStatement2.setAttributeAssessment(objAttributeAssessment2);
                                	}//</attributeAssessment>2
                                	
                                	//<valueAssessment>2
                                	NodeList listValueAssessment2 = elemAssessmentStatement2.getElementsByTagName("valueAssessment");
                                	for(int w = 0; w < listValueAssessment2.getLength(); w++){
                                		Element elemValueAssessment2 = (Element)listValueAssessment2.item(w);
                                		ValueAssessment objValueAssessment2 = new ValueAssessment(elemValueAssessment2.getAttribute("value"));
           
                                		//System.out.println(objValueAssessment2.getValue());
                                		objAssessmentStatement2.setValueAssessment(objValueAssessment2);
                                	}//</valueAssessment>2
                                	
                                	//System.out.println(objAssessmentStatement2.getComparator());
                                	objCompCondition.setAssessementStatement(objAssessmentStatement2);
                                }//</assessmentStatement>2
                            	
                                //System.out.println(objCompoundStatement.getOperator());
                            	objCompCondition.setCompoundStatement(objCompoundStatement);
                            }//</compoundStatement>
                            
                            //System.out.println(listSimpCondition.getLength());
                            objCConnector.setCompoundCondition(objCompCondition);
                        } //</compoundCondition>
                        
                        //<compoundAction>
                        NodeList listCompoundAction = elemCConnector.getElementsByTagName("compoundAction");
                        for (int j = 0; j < listCompoundAction.getLength(); j++){
                        	Element elemCompoundAction = (Element)listCompoundAction.item(j);
                        	CompoundAction objCompoundAction = new CompoundAction(elemCompoundAction.getAttribute("operator"));
                        	
                        	if(elemCompoundAction.hasAttribute("delay"))
                        		objCompoundAction.setDelay(elemCompoundAction.getAttribute("delay"));
                        	
                        	//<simpleAction>
                            NodeList listSimpAction2 = elemCompoundAction.getElementsByTagName("simpleAction");
                            for(int k=0; k < listSimpAction2.getLength(); k++){
                                Element elemSimpAction2 = (Element)listSimpAction2.item(k);
                                SimpleAction objSimpAction2 = new SimpleAction(elemSimpAction2.getAttribute("role"));
                                
                                if(elemSimpAction2.hasAttribute("actionType"))
                                    objSimpAction2.setActionType(elemSimpAction2.getAttribute("actionType"));
                                if(elemSimpAction2.hasAttribute("by"))
                                    objSimpAction2.setBy(elemSimpAction2.getAttribute("by"));
                                if(elemSimpAction2.hasAttribute("delay"))
                                    objSimpAction2.setDelay(elemSimpAction2.getAttribute("delay"));
                                if(elemSimpAction2.hasAttribute("duration"))
                                    objSimpAction2.setDuration(elemSimpAction2.getAttribute("duration"));
                                if(elemSimpAction2.hasAttribute("eventType"))
                                    objSimpAction2.setEventType(elemSimpAction2.getAttribute("eventType"));
                                if(elemSimpAction2.hasAttribute("key"))
                                    objSimpAction2.setKey(elemSimpAction2.getAttribute("key"));
                                if(elemSimpAction2.hasAttribute("max"))
                                    objSimpAction2.setMax(elemSimpAction2.getAttribute("max"));
                                if(elemSimpAction2.hasAttribute("min"))
                                    objSimpAction2.setMin(elemSimpAction2.getAttribute("min"));
                                if(elemSimpAction2.hasAttribute("qualifier"))
                                    objSimpAction2.setQualifier(elemSimpAction2.getAttribute("qualifier"));
                                if(elemSimpAction2.hasAttribute("repeat"))
                                    objSimpAction2.setRepeat(elemSimpAction2.getAttribute("repeat"));
                                if(elemSimpAction2.hasAttribute("repeadDelay"))
                                    objSimpAction2.setRepeatDelay(elemSimpAction2.getAttribute("repeatDelay"));
                                if(elemSimpAction2.hasAttribute("transition"))
                                    objSimpAction2.setTransition(elemSimpAction2.getAttribute("transition"));
                                
                                //System.out.println(objSimpAction2.getRole());
                                objCompoundAction.addSimpleAction(objSimpAction2);
                            }//</simpleAction>
                        	
                        	//System.out.println(objCompoundAction.getDelay());
                        	objCConnector.setCompoundAction(objCompoundAction);
                        }//</compoundAction>
                        
                        //System.out.println(objCConnector.getId());
                        objConnectorBase.addCausalConnector(objCConnector.getId(), objCConnector);
                    }//</causalConnector> 
                
                //</connectorBase>
                objHead.setConnectorBase(objConnectorBase);
                
            //</HEAD>
            nclDocument.setHead(objHead);
            
            //<BODY>
            NodeList listBody = ncl.getElementsByTagName("body");
            Element elemBody = (Element)listBody.item(0);
            Body objBody = new Body();
            
            if(elemBody.hasAttribute("id"))
            	objBody.setId(elemBody.getAttribute("id"));
            
            	//<port>
            	NodeList listPort = elemBody.getElementsByTagName("port");
            	for(int i=0; i < listPort.getLength(); i++){
            		Element elemPort = (Element)listPort.item(i);
            		Port objPort = new Port(elemPort.getAttribute("id"), elemPort.getAttribute("component"));
            		
            		if(elemPort.hasAttribute("interface"))
            			objPort.setInterface(elemPort.getAttribute("interface"));
            		
            		//System.out.println(objPort.getComponent());
            		objBody.addPort(objPort.getId(), objPort);
            	}//</port>
            	
            	//<media>
            	NodeList listMedia = elemBody.getElementsByTagName("media");
            	for(int i=0; i < listMedia.getLength(); i++){
            		Element elemMedia = (Element)listMedia.item(i);
            		MediaNCL objMedia = new MediaNCL(elemMedia.getAttribute("id"));
            		
            		if(elemMedia.hasAttribute("src"))
            			objMedia.setSrc(elemMedia.getAttribute("src"));
            		if(elemMedia.hasAttribute("refer"))
            			objMedia.setRefer(elemMedia.getAttribute("refer"));
            		if(elemMedia.hasAttribute("instance"))
            			objMedia.setInstance(elemMedia.getAttribute("instance"));
            		if(elemMedia.hasAttribute("type"))
            			objMedia.setType(elemMedia.getAttribute("type"));
            		if(elemMedia.hasAttribute("descriptor"))
            			objMedia.setDescriptor(elemMedia.getAttribute("descriptor"));
            		
            		//<area>
            		NodeList listArea = elemMedia.getElementsByTagName("area");
            		for(int j=0; j < listArea.getLength(); j++){
            			Element elemArea = (Element)listArea.item(j);
            			Area objArea = new Area(elemArea.getAttribute("id"));
            			
            			if(elemArea.hasAttribute("coords"))
            				objArea.setCoords(elemArea.getAttribute("coords"));
            			if(elemArea.hasAttribute("begin"))
            				objArea.setBegin(elemArea.getAttribute("begin"));
            			if(elemArea.hasAttribute("end"))
            				objArea.setEnd(elemArea.getAttribute("end"));
            			if(elemArea.hasAttribute("text"))
            				objArea.setText(elemArea.getAttribute("text"));
            			if(elemArea.hasAttribute("position"))
            				objArea.setPosition(elemArea.getAttribute("position"));
            			if(elemArea.hasAttribute("first"))
            				objArea.setFirst(elemArea.getAttribute("first"));
            			if(elemArea.hasAttribute("last"))
            				objArea.setLast(elemArea.getAttribute("last"));
            			if(elemArea.hasAttribute("label"))
            				objArea.setLabel(elemArea.getAttribute("label"));
            			if(elemArea.hasAttribute("clip"))
            				objArea.setClip(elemArea.getAttribute("clip"));
            			if(elemArea.hasAttribute("beginOffset"))
            				objArea.setBeginOffset(elemArea.getAttribute("beginOffset"));
            			if(elemArea.hasAttribute("endOffset"))
            				objArea.setEndOffset(elemArea.getAttribute("endOffset"));
            			
            			//System.out.println(objArea.getId());
            			objMedia.addArea(objArea.getId(), objArea);
            		}//</area>
            		
            		//<property>
            		NodeList listProperty2 = elemMedia.getElementsByTagName("property");
            		for(int j=0; j < listProperty2.getLength(); j++){
            			Element elemProperty2 = (Element)listProperty2.item(j);
            			Property objProperty2 = new Property(elemProperty2.getAttribute("name"), elemProperty2.getAttribute("value"));
            			
            			//System.out.println(objProperty2.getName());
            			objMedia.addProperty(objProperty2);
            		}//</property>
            		
            		//System.out.println(objMedia.getId());
            		objBody.addMediaNCL(objMedia.getId(), objMedia);
            		objMedia = null;
            	}//</media>
            	
            	
            	//<link>
            	NodeList listLink = elemBody.getElementsByTagName("link");
            	for(int i=0; i < listLink.getLength(); i++){
            		Element elemLink = (Element)listLink.item(i);
            		Link objLink = new Link(elemLink.getAttribute("xconnector"));
            		
            		if(elemLink.hasAttribute("id"))
            			objLink.setId(elemLink.getAttribute("id"));
            		
            		//<bind>
            		NodeList listBind = elemLink.getElementsByTagName("bind");
            		for(int j=0; j < listBind.getLength(); j++){
            			Element elemBind = (Element)listBind.item(j);
            			Bind objBind = new Bind(elemBind.getAttribute("role"), elemBind.getAttribute("component"));
            			
            			if(elemBind.hasAttribute("descriptor"))
            				objBind.setDescriptor(elemBind.getAttribute("descriptor"));
            			if(elemBind.hasAttribute("interface"))
            				objBind.setInferface(elemBind.getAttribute("interface"));
            			
            			//<bindParam>
            			NodeList listBindParam = elemBind.getElementsByTagName("bindParam");
            			for(int k=0; k < listBindParam.getLength(); k++){
            				Element elemBindParam = (Element)listBindParam.item(k);
            				BindParam objBindParam = new BindParam(elemBindParam.getAttribute("name"), elemBindParam.getAttribute("value"));
            				
            				//System.out.println(objBindParam.getName());
            				objBind.addBindParam(objBindParam);
            			}
            			//</bindParam>
            			
            			//System.out.println(objBind.getComponent());
            			objLink.addBind(objBind);
            		}//</bind>
            		
            		//<linkParam>
            		NodeList listLinkParam = elemLink.getElementsByTagName("linkParam");
            		for (int j=0; j < listLinkParam.getLength(); j++){
            			Element elemLinkParam = (Element)listLinkParam.item(j);
            			LinkParam objLinkParam = new LinkParam(elemLinkParam.getAttribute("name"), elemLinkParam.getAttribute("value"));
            			
            			//System.out.println(objLinkParam.getName());
            			objLink.addLinkParam(objLinkParam);
            		}//</linkParam>
            		
            		//System.out.println(objLink.getXConnector());
            		objBody.addLink(objLink);
            	}//</link>
            	
            	//<property>
            	NodeList listProperty = elemBody.getElementsByTagName("property");
            	for(int i=0; i < listProperty.getLength(); i++){
            		Element elemProperty = (Element)listProperty.item(i);
            		Property objProperty = new Property(elemProperty.getAttribute("name"), elemProperty.getAttribute("value"));
            	
            		//System.out.println(objProperty.getName()); //ta mostrando os property internos tbm
            		objBody.addProperty(objProperty);
            	}//</property>
            	
            	//<switch>
            	NodeList listSwitch = elemBody.getElementsByTagName("switch");
            	for(int i=0; i < listSwitch.getLength(); i++){
            		Element elemSwitch = (Element)listSwitch.item(i);
            		Switch objSwitch = new Switch(elemSwitch.getAttribute("id"));
            		
            		if(elemSwitch.hasAttribute("refer")) 
            			objSwitch.setRefer(elemSwitch.getAttribute("refer"));
            		
            		//<switchPort>
            		NodeList listSwitchPort = elemSwitch.getElementsByTagName("switchPort");
            		for(int j=0; j < listSwitchPort.getLength(); j++){
            			Element elemSwitchPort = (Element)listSwitchPort.item(j);
            			SwitchPort objSwitchPort = new SwitchPort(elemSwitchPort.getAttribute("id"));
            			
            			//<mapping>
            			NodeList listMapping = elemSwitchPort.getElementsByTagName("mapping");
            			for(int k=0; j < listMapping.getLength(); j++){
            				Element elemMapping = (Element)listMapping.item(k);
            				Mapping objMapping = new Mapping(elemMapping.getAttribute("component"));
            				
            				if(elemMapping.hasAttribute("interface"))
            					objMapping.setInterface(elemMapping.getAttribute("interface"));
            				
            				//System.out.println(objMapping.getComponent());
            				objSwitchPort.addMapping(objMapping);
            			}//</mapping>
            			
            			//System.out.println(objSwitchPort.getId());
            			objSwitch.addSwitchPort(objSwitchPort.getId(), objSwitchPort);
            		}//</switchPort>
            		
            		//<bindRule>
            		NodeList listBindRule = elemSwitch.getElementsByTagName("bindRule");
            		for(int j=0; j < listBindRule.getLength(); j++){
            			Element elemBindRule = (Element)listBindRule.item(j);
            			BindRule objBindRule = new BindRule(elemBindRule.getAttribute("constituent"), elemBindRule.getAttribute("rule"));
            			
            			//System.out.println(objBindRule.getConstituent());
            			objSwitch.addBindRule(objBindRule);
            		}//</bindRule>
            		
            		//<defaultComponent>
            		NodeList listDefaultComponent = elemSwitch.getElementsByTagName("defaultComponent");
            		for(int j=0; j < listDefaultComponent.getLength(); j++){
            			Element elemDefaultComponent = (Element)listDefaultComponent.item(j);
            			DefaultComponent objDefaultComponent = new DefaultComponent(elemDefaultComponent.getAttribute("component"));
            			
            			//System.out.println(objDefaultComponent.getComponent());
            			objSwitch.setDefaultComponent(objDefaultComponent);
            		}//</defaultComponent>
            		
            		//<media>
            		NodeList listMedia2 = elemBody.getElementsByTagName("media");
                	for(int q=0; q < listMedia2.getLength(); q++){
                		Element elemMedia2 = (Element)listMedia2.item(q);
                		//if(elemMedia2.getParentNode().getNodeName().compareTo("body") != 0)
                    		//continue;
                		//if(elemMedia2.getParentNode() != elemBody)
                			//continue;
                		MediaNCL objMedia2 = new MediaNCL(elemMedia2.getAttribute("id"));
                		
                		if(elemMedia2.hasAttribute("src"))
                			objMedia2.setSrc(elemMedia2.getAttribute("src"));
                		if(elemMedia2.hasAttribute("refer"))
                			objMedia2.setRefer(elemMedia2.getAttribute("refer"));
                		if(elemMedia2.hasAttribute("instance"))
                			objMedia2.setInstance(elemMedia2.getAttribute("instance"));
                		if(elemMedia2.hasAttribute("type"))
                			objMedia2.setType(elemMedia2.getAttribute("type"));
                		if(elemMedia2.hasAttribute("descriptor"))
                			objMedia2.setDescriptor(elemMedia2.getAttribute("descriptor"));
                		
                		//<area>
                		NodeList listArea2 = elemMedia2.getElementsByTagName("area");
                		for(int w=0; w < listArea2.getLength(); w++){
                			Element elemArea2 = (Element)listArea2.item(w);
                			Area objArea2 = new Area(elemArea2.getAttribute("id"));
                			
                			if(elemArea2.hasAttribute("coords"))
                				objArea2.setCoords(elemArea2.getAttribute("coords"));
                			if(elemArea2.hasAttribute("begin"))
                				objArea2.setBegin(elemArea2.getAttribute("begin"));
                			if(elemArea2.hasAttribute("end"))
                				objArea2.setEnd(elemArea2.getAttribute("end"));
                			if(elemArea2.hasAttribute("text"))
                				objArea2.setText(elemArea2.getAttribute("text"));
                			if(elemArea2.hasAttribute("position"))
                				objArea2.setPosition(elemArea2.getAttribute("position"));
                			if(elemArea2.hasAttribute("first"))
                				objArea2.setFirst(elemArea2.getAttribute("first"));
                			if(elemArea2.hasAttribute("last"))
                				objArea2.setLast(elemArea2.getAttribute("last"));
                			if(elemArea2.hasAttribute("label"))
                				objArea2.setLabel(elemArea2.getAttribute("label"));
                			if(elemArea2.hasAttribute("clip"))
                				objArea2.setClip(elemArea2.getAttribute("clip"));
                			if(elemArea2.hasAttribute("beginOffset"))
                				objArea2.setBeginOffset(elemArea2.getAttribute("beginOffset"));
                			if(elemArea2.hasAttribute("endOffset"))
                				objArea2.setEndOffset(elemArea2.getAttribute("endOffset"));
                			
                			//System.out.println(objArea.getId());
                			objMedia2.addArea(objArea2.getId(), objArea2);
                		}//</area>
                		
                		//<property>
                		NodeList listProperty3 = elemMedia2.getElementsByTagName("property");
                		for(int j=0; j < listProperty3.getLength(); j++){
                			Element elemProperty3 = (Element)listProperty3.item(j);
                			Property objProperty3 = new Property(elemProperty3.getAttribute("name"), elemProperty3.getAttribute("value"));
                			
                			//System.out.println(objProperty2.getName());
                			objMedia2.addProperty(objProperty3);
                		}//</property>
                		
                		
                		//System.out.println(objMedia2.getId());
                		objSwitch.addMediaNCL(objMedia2.getId(), objMedia2);
                	}//</media>
            		
            		//<context>
            		//</context>
            		
            		//<switch>
            		//</switch>
            		
            		//System.out.println(objSwitch.getId());
            		objBody.addSwitch(objSwitch.getId(), objSwitch);
            	}//</switch>
            
            	//<context>
            	//</context>
            	
            	//<meta>
            	NodeList listMeta = elemBody.getElementsByTagName("meta");
            	for(int i=0; i < listMeta.getLength(); i++){
            		Element elemMeta = (Element)listMeta.item(i);
            		Meta objMeta = new Meta(elemMeta.getAttribute("content"),elemMeta.getAttribute("name"));
            		
            		//System.out.println("<meta name=\""+objMeta.getName()+"\"/");
            		objBody.addMeta(objMeta);
            	}//</meta>
            	
            //System.out.println(objBody.getId());
            nclDocument.setBody(objBody);
            //</BODY>
        
        //</NCL>
        p.setNCLDocument(nclDocument);
       // System.out.println(nclDocument.getBody().getListMediaNCL().size());
    }

    public void loadNCL2(Programming p) throws ParserConfigurationException, SAXException, IOException{
    	
    	DocumentBuilderFactory docBuilderFac = DocumentBuilderFactory.newInstance();
        DocumentBuilder DocBuilder = docBuilderFac.newDocumentBuilder();
        Document document = DocBuilder.parse(p.getUrl());
    	
        //<NCL>
        Element elemNCL = document.getDocumentElement();
        NCLDocument nclDocument = new NCLDocument(elemNCL.getAttribute("id"), elemNCL.getAttribute("xmlns"));
        nclDocument.load(elemNCL);
        //</NCL>
        
        p.setNCLDocument(nclDocument);
    }
    
    public void setMidiaAvailable(){
        Set<String> keys = synctv.getChannel(optChannel).getListProgramming().keySet();
        for(String key : keys){//varrendo programações
            String dtProg = synctv.getChannel(optChannel).getProgramming(key).getDate();
            String hrProg = synctv.getChannel(optChannel).getProgramming(key).getHour(); 
            String availProg = synctv.getChannel(optChannel).getProgramming(key).getAvailable();

            Calendar calProg =  Calendar.getInstance();
            calProg.set(Integer.parseInt(dtProg.substring(6,10)), Integer.parseInt(dtProg.substring(3,5)) ,Integer.parseInt(dtProg.substring(0,2)) ,Integer.parseInt(hrProg.substring(0,2)) ,Integer.parseInt(hrProg.substring(3,5)) ,0);

            Calendar calCurrent = Calendar.getInstance();

            dtProg = calCurrent.get(Calendar.DAY_OF_MONTH)+"/"+calCurrent.get(Calendar.MONTH)+"/"+calCurrent.get(Calendar.YEAR) ;
            String dtCurrent = calCurrent.get(Calendar.DAY_OF_MONTH)+"/"+calCurrent.get(Calendar.MONTH)+"/"+calCurrent.get(Calendar.YEAR);
            //verifica se a data da programação coincide com a data atual
            if(dtProg.equals(dtCurrent)){
                int hrCurrentInSec = (calCurrent.get(Calendar.HOUR_OF_DAY))*3600+(calCurrent.get(Calendar.MINUTE))*60+(calCurrent.get(Calendar.SECOND));
                int hrProgInSec = (calProg.get(Calendar.HOUR_OF_DAY))*3600+(calProg.get(Calendar.MINUTE))*60+(calProg.get(Calendar.SECOND));
                int availInSec = ((Integer.parseInt(availProg.substring(0, 2)))*3600 + (Integer.parseInt(availProg.substring(3, 5)))*60);
                
                
               //hora atual coincide com hora da prog 
               if((hrCurrentInSec >= hrProgInSec)&&(hrCurrentInSec <= (hrProgInSec+availInSec))){

                    Set<String> keysImg= synctv.getChannel(optChannel).getProgramming(key).getListMidia().keySet();
                    for(String keyImg: keysImg){//Varrendo Imagens

                        int offset = synctv.getChannel(optChannel).getProgramming(key).getMidia(keyImg).getOffset();
                        //hora atual coincide com hora da prog + offset do item de midia
                        if(hrCurrentInSec >= (hrProgInSec + offset)){
                            syncTVApp.addMediaList(synctv.getChannel(optChannel).getProgramming(key), synctv.getChannel(optChannel).getProgramming(key).getMidia(keyImg));
                        }
                    }
               }
            }   
        }
    }
*/
}
