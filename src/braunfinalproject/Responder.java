/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package braunfinalproject;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

/**
 *
 * @author braun1792
 */
public class Responder implements Runnable {
    
    //client socket
    private Socket requestHandler;
    //text scanner
    private Scanner requestReader;
    private Scanner pageReader;
    private String HTTPMessage;
    private String requestedFile;
    private DataOutputStream pageWriter;
    FileWriter writer;
    BufferedWriter bufferedWriter;
    String timeStamp;
    
    
    public Responder(){
        System.out.println("Warning this responder does not have a request handler");
    }
    
    public Responder(Socket requestHandler){
        //sets client socket
        setSocket(requestHandler);
        try{
        //instantiate writer here, so that it may be used in entire class
        writer = new FileWriter("MyLog.txt", true);
        //Writes text to a character-output stream, buffering characters so 
        //as to provide for the efficient writing of single characters, arrays, and strings
        bufferedWriter = new BufferedWriter(writer);
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }
    
    public void setSocket(Socket requestHandler){
        this.requestHandler = requestHandler;
    }
    
    public void run(){
        try{

            System.out.println("Page Requested: Request Header:");

            //reads incoming data from browser via requesthandler (socket)
            //inputstreamreader converts bytes to characters
            requestReader = new Scanner(new InputStreamReader(requestHandler.getInputStream()));
            
          int lineCount=0;

            do{ 

               lineCount++;
               //stores incoming characters as string
                HTTPMessage = requestReader.nextLine();

                //substring (begin index, end index)
                //finds the requested page name or service from within the HTTP header/message 
                if (lineCount==1){
                    requestedFile="WebRoot\\" + 
                    HTTPMessage.substring(5, HTTPMessage.indexOf("HTTP/1.1")-1);
                    
                    //trims empty spaces
                    requestedFile = requestedFile.trim();

                    //if .htm is not found append default.htm
                    //do not append .htm to a doService request
                    if(!requestedFile.contains(".htm") && !requestedFile.contains("doSERVICE")){
                        requestedFile = requestedFile + "default.htm";
                    }
                }

                System.out.println(HTTPMessage);
                //query example: GET /doSERVICE?Criteria=3000&Field=JobCode&submit=Run+Service HTTP/1.1
                //page example: GET /default.htm HTTP/1.1
     
                
            }while(HTTPMessage.length()!=0);
            
            //set current date/time for text log
            timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                
            //wrties request to log
            bufferedWriter.write("Requested file: "+requestedFile);
            bufferedWriter.newLine();
            bufferedWriter.write("Time: "+ timeStamp);
            bufferedWriter.newLine();
            
            //forces any buffered output bytes to be written out
            //not sure if this is necessary
            bufferedWriter.flush();
                
            
            //if/else block handles either a doService request (SQL query) or a page request
            if(requestedFile.contains("doSERVICE")){
                
                //get output stream from socket
                pageWriter = new DataOutputStream( requestHandler.getOutputStream());
                //pass output stream and request to SQL Service class
                Service s = new SQLSelectService(pageWriter,requestedFile);
                s.doWork();
                
            }else{

                try{
                    //reads text from file that contains html page
                    pageReader = new Scanner( new File(requestedFile));

                }catch(FileNotFoundException fnf){
                    
                    //if file is not found, read text from page not found file 
                    pageReader = new Scanner(new File("WebRoot\\Util\\PageNotFound.htm"));
                    //log that the file was not found
                    timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                    bufferedWriter.write("File not found: "+requestedFile);
                    bufferedWriter.newLine();
                    bufferedWriter.write("Time: "+ timeStamp);
                    bufferedWriter.newLine();
                }
                
                
                pageWriter = new DataOutputStream( requestHandler.getOutputStream());
                
                //reads entire file
                while(pageReader.hasNext()){
                    
                    //writes file by line, sending string as bytes via the output stream
                    String s=pageReader.nextLine();
                    pageWriter.writeBytes(s);
                }
                //we're done with this
                pageReader.close();
            }

            //Tells the Browser we’re done sending
            bufferedWriter.close();
            pageWriter.close();
            requestHandler.close();
            
            
        }
        catch(Exception e){
            
            System.out.println(e.toString());
            
            try{
                //logs any errors in the run method
                timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                bufferedWriter.write(e.toString());
                bufferedWriter.newLine();
                bufferedWriter.write("Time: "+ timeStamp);
                bufferedWriter.newLine();

                bufferedWriter.flush();
                
            }catch(Exception ex){
                System.out.println(e.toString());
            }
        }
    }
    
}
