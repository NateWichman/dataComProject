import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.nio.file.Path;
import java.nio.file.Files;
/*************************************************************************************
UDP client using Datagram Sockets. Requests the Server for a file, then recieves
said file via the sliding window model.
@Author Nathan Wichman
@Version september 2018
*************************************************************************************/
class udpclient{
    public static void main(String args[]){
	try{
	    /** Receiving user input for port number and iPadress **/
	    Scanner scnr = new Scanner(System.in);
	    System.out.println("Enter a port number: ");
	    int portNumber = scnr.nextInt();
	    System.out.println("Enter an IP address: ");
	    scnr.nextLine();
	    String ipAddress = scnr.nextLine();
	    System.out.println("IpReceived: " + ipAddress);
	    
	    /** Creating a datagram channel **/
	    DatagramChannel sc = DatagramChannel.open();
	    Console cons = System.console();

	    /** Sending the file name to the inputted ip and port number **/
	    String fileName  = cons.readLine("Enter a File Name: ");
	    ByteBuffer buf = ByteBuffer.wrap(fileName.getBytes());
	    sc.send(buf, new InetSocketAddress((ipAddress),portNumber));
            
	    /** Just for debugging purposes, sending a 1 to check connection **/
	    String example = "1";
	    ByteBuffer buffer = ByteBuffer.wrap(example.getBytes());
	    sc.send(buffer, new InetSocketAddress((ipAddress),portNumber));

	    /** Just for debugging as well, receiving a "check" string from the server **/
	    ByteBuffer buf3 = ByteBuffer.allocate(5000);
	    sc.receive(buf3);
	    buf3.flip();
	    byte[] b = new byte[buf3.remaining()];
	    String out = new String(buf3.array());
	    System.out.println("Test: " + out);
	    out = out.replaceAll("\0+","");
	    if(out.equals("check")){
		    System.out.println("Test Passes");
	    }else{
		    System.out.println("Test Failed");
	    }
	    
	    /** Setting up the output file to write too **/
	    File outputFile = new File("output.txt");
	    FileOutputStream outStream = new FileOutputStream("output.txt");
	    
	    /** Byte array to hold the received data **/
	    byte[] receivedData = {};
	  
	    int acknowledgment = 0;
	    while(true){
		    /** Receiving packets from Server **/
		    ByteBuffer buf2 = ByteBuffer.allocate(5000);
		    sc.receive(buf2);
		    buf2.flip();
		  //  byte[] a = new byte[buf2.remaining()];
		  //  buf2.get(a);

		    /** Converting to string to test for termination code **/
		    String receivedString = new String(buf2.array());
		    /** Removing null charachters from string **/
		    receivedString = receivedString.replaceAll("\0+$", "");
		   
		    if(receivedString  == null){
			    System.out.println("null");
		    }
		    else if(receivedString.equals("done")){
			    System.out.println("done");
			    break;
		    }
	           
		    buf2.rewind();
		    int ackNum =  buf2.getInt();
		    buf2.slice();
		    byte[] a = new byte[buf2.remaining()];
		    buf2.get(a); 
		    
		    /** Adding new packet to the total byte array (ReceivedData[]) **/
		    byte[] combo = new byte[a.length + receivedData.length];
		    System.arraycopy(receivedData, 0, combo,0, receivedData.length);
		    System.arraycopy(a, 0, combo, receivedData.length, a.length);
		    receivedData = combo;
		    System.out.println("Bytes Received: " + a);
		    
		    
		    /** Sending acknoledgment **/
		    String ack = "c" + Integer.toString(ackNum);
	  	 //   String ack = "c" + Integer.toString(acknowledgment); //Every acknowledgment begins with c
		    //I had issues checking if the string was null on the server end, so instead I check that
		    //it begins with c then remove the c leaving just the integer on the server end.
		    ByteBuffer buf4 = ByteBuffer.wrap(ack.getBytes());
		    sc.send(buf4, new InetSocketAddress((ipAddress), portNumber));
		    acknowledgment++;

	    }

    	    /** Writing File **/
	    if(receivedData != null){
	  	  outStream.write(receivedData);
	    }else{
		    System.out.println("ReceivedData was empty");
	    } 

	    /** Closing Datagram Socket Channel **/
	    sc.close();

	}catch(IOException e){
	    System.out.println("Error happened\n");
	}
    }
}
