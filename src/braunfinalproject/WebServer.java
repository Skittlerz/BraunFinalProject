/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package braunfinalproject;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author braun1792
 */
public class WebServer {
    
    private ServerSocket requestListener;
    private static int HTTP_PORT = 12346;
    private ExecutorService responses;
    
    public WebServer(){
        try{
        //waits for request to come in over network on specified port
        requestListener = new ServerSocket(HTTP_PORT);
        //creates a thread pool of 100, threads are a program's path of execution
        responses = Executors.newFixedThreadPool(100);
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }
    
    public void start(){
        
        //continuously waits for connections
        while(true){
            try{
                System.out.println("Waiting for connection");
                
                //accepts the connection and passes to the responder class
                //requestListener.accept() returns new socket
                Responder r = new Responder( requestListener.accept() );
                System.out.println("Connection accepted");
                
                //responders run method handles analyzing requests and answering them
                responses.execute(r);
                System.out.println("Connection executed");
            }catch(Exception e){
                System.out.println(e.toString());
            }
        }
    }
    
    public static void main(String[] args){
        
        WebServer web = new WebServer();
        web.start();
    }
    
}
