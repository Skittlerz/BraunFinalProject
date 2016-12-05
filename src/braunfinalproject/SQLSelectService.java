/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package braunfinalproject;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 *
 * @author braun1792
 */
public class SQLSelectService extends Service {
    
    private String requestString;
    private String SQLCommand;
    FileWriter writer;
    BufferedWriter bufferedWriter;
    String timeStamp;
    
    
    //This constructor will be called from the run method of a 
    //Responder.  It passes the HTTP request info, and the output 
    //object to the service. 
    public SQLSelectService(DataOutputStream responseWriter, String requestString){
		
	super(responseWriter);
	this.requestString=requestString;
         try{
        writer = new FileWriter("MyLog.txt", true);
        bufferedWriter = new BufferedWriter(writer);
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }
    
    public void setSQLCommand(){
        String field;
        String criteria;
        
        //searches request string for field value and stores it (SQL column name)
        field = requestString.substring(requestString.indexOf("Field=")+6,requestString.indexOf("&submit"));
        //stores user search criteria
        criteria = requestString.substring(requestString.indexOf("Criteria=")+9,requestString.indexOf("&Field"));
        
        //if field is empid or jobcode (type int) do not surround criteria with ''
        //else surround criteria with '' and use lower() function to ignore case
        if (field.contains("empid") || field.contains("JobCode")){
            SQLCommand = "SELECT EmpId, SIN, LastName, FirstName, Street, City, Province, PostalCode, JobCode, IncomeTax,"+
                    "to_char(birthDate, 'Month dd yyyy'), to_char(hireDate, 'Month dd yyyy'), to_char(jobCodeDate, 'Month dd yyyy')" +
                    "FROM EMPLOYEE WHERE "+field+" = "+criteria;
        }else{
            SQLCommand = "SELECT EmpId, SIN, LastName, FirstName, Street, City, Province, PostalCode, JobCode, IncomeTax,"+
                    "to_char(birthDate, 'Month dd yyyy'), to_char(hireDate, 'Month dd yyyy'), to_char(jobCodeDate, 'Month dd yyyy')" +
                    "FROM EMPLOYEE WHERE lower("+field+") = lower('"+criteria+"')";
        }
        
        try{
        //log sql query
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        bufferedWriter.write(SQLCommand);
        bufferedWriter.newLine();
        bufferedWriter.write("Time: "+ timeStamp);
        bufferedWriter.newLine();
        bufferedWriter.flush();
        }catch(Exception e){
            System.out.println(e.toString());
        }
        
        System.out.println(SQLCommand);
    }
    
    public void doWork(){
        
        //parse SQL query from request string
        setSQLCommand();
        
        //executes SQL statement and returns result set
        Statement stmt=null; 
        //stores table of data (database result set)
        ResultSet rset=null; 
        //stores info about the types and properties of columns
        ResultSetMetaData rsmd=null;
        //session with specific database
        Connection conn=null;
        
        try {
            //set up connection to database
            //oracledriver implements driver interface, allows it to use registerDriver()
            //JDBC drivers must be registered before you can establish a connection
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

            //parameters: JDBC driver, name of database, userID, password
            conn = DriverManager.getConnection
            ("jdbc:oracle:thin:@bisoracle.siast.sk.ca:1521:ACAD","cistu002","databasefun");
            
            
            stmt = conn.createStatement();
            rset = stmt.executeQuery(SQLCommand);
            rsmd = rset.getMetaData();
            
            //get column count from the resultset metadata
            int columnCount = rsmd.getColumnCount();
            
            //Set up html page, including css styling and table to insert data
            //writeBytes() sends string data as bytes
            getResponseWriter().writeBytes("<html><head><title>test</title>");
            getResponseWriter().writeBytes("<style>table td{border:1px solid black;text-align:center;padding:5px;}</style></head><body><a href=\"http://localhost:12346/query.htm\">Back to Query Page</a>");
            getResponseWriter().writeBytes("<table><tr style=\"background-color:#ccffff\"><td>EmpID</td><td>SIN</td><td>Last Name</td>"
                    + "<td>First Name</td><td>Address</td><td>City</td><td>Province</td><td>Postal Code</td>"+
                    "<td>Job Code</td><td>Income Tax</td><td>BirthDate</td><td>HireDate</td><td>JobCode Date</td>");
            
            //parse through the entire result set
            while (rset.next()){
                
                getResponseWriter().writeBytes("<tr>");
                
                //reads each column from each row
        	for(int i = 0; i< columnCount; i++ ){
                    
                    getResponseWriter().writeBytes("<td>"+rset.getString(i+1)+"</td>"); 
                    
                }
                //once all columns are read, close html table trow
                getResponseWriter().writeBytes("<tr />");
            }
            //close table, body, and html document
            getResponseWriter().writeBytes("</table></body></html>");
            
            //close all open connections and what not, no memory leaks here!
            rset.close();
            stmt.close();
            conn.close();
            bufferedWriter.close();
            
        }catch(Exception e){
            
            System.out.println(e.toString());
            
            try{
                //logs any errors in the do work method
                timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                bufferedWriter.write(e.toString());
                bufferedWriter.newLine();
                bufferedWriter.write("Time: "+ timeStamp);
                bufferedWriter.newLine();

                bufferedWriter.close();
                
            }catch (Exception ex){
                System.out.println(ex.toString());
            }
        }
    }

}
