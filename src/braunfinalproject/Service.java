/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package braunfinalproject;

import java.io.DataOutputStream;

/**
 *
 * @author braun1792
 */
public abstract class Service {
    private DataOutputStream responseWriter;
    
    public Service(DataOutputStream responseWriter){this.responseWriter = responseWriter;}
    
    //allows access to the responseWriter in other classes
    public DataOutputStream getResponseWriter(){return this.responseWriter;}
    
    public abstract void doWork();
}
