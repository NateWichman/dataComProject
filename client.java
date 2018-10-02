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
	    
	    /** Setting up the output file to write too **/
	    File outputFile = new File("output.txt");
	    FileOutputStream outStream = new FileOutputStream("output.txt");
	    
	    /** Byte array to hold the received data **/
	    byte[] receivedData = {};
	  
	    int timer = 0;
	    int fileReceivedTimer = 0;
	    int acknowledgment = 0;

	    ArrayList<Packet> packets = new ArrayList<Packet>();
	    int currentPacketNumber = 0;
	    while(true){
		    /** Receiving packets from Server **/
		    ByteBuffer buf2 = ByteBuffer.allocate(5000);
		    sc.receive(buf2);
		    buf2.flip();
		 
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
		    

		    
		    String ack = "c" + Integer.toString(ackNum);

		    System.out.println(ackNum);
		    
		    boolean CanAddPacket = true;
		    for(Packet pac: packets){
			    if(pac.getNumber() == ackNum){
				    System.out.println("Packet Already Received");
				    CanAddPacket = false;
				    break;
			    }
		    }
		    if(CanAddPacket){
			    packets.add(new Packet(ackNum, a));
		    }

		    Iterator<Packet> iter2 = packets.iterator();
		    boolean missingNextPacket = true;
		    while(iter2.hasNext()){
			    Packet pac = iter2.next();

			    if(pac.getNumber() < currentPacketNumber){
				    System.out.println("Packet " + pac.getNumber() + " is less than the current number");
				    System.out.println("Removing Packer from saved array");
				    iter2.remove();
			    }

			    if(pac.getNumber() == currentPacketNumber){
				     byte[] combo = new byte[pac.getData().length + receivedData.length];
				     System.arraycopy(receivedData, 0, combo, 0, receivedData.length);
				     System.arraycopy(pac.getData(), 0, combo, receivedData.length, pac.getData().length);
				     receivedData = combo;
				     System.out.println("Adding packet: " + pac.getNumber() + " to the output");
				     iter2.remove();
				     currentPacketNumber++;
				     missingNextPacket = false;
			    }
		    }

		    if(missingNextPacket){	
			    if(timer >= 20){
			    for(int j = 0; j < 10; j++){
			   	   String resendAck = "c" + Integer.toString(currentPacketNumber - 10 + j);
			  	   ByteBuffer buf5 = ByteBuffer.wrap(resendAck.getBytes());
			 	   System.out.println("Resending Ack for packet " + (currentPacketNumber - 10 + j));
			 	   sc.send(buf5, new InetSocketAddress((ipAddress), portNumber));
				   timer = 0;
			      }
			    }else{
				    timer ++;
			    }
		    }else{
			   
		    ByteBuffer buf4 = ByteBuffer.wrap(ack.getBytes());
		    sc.send(buf4, new InetSocketAddress((ipAddress), portNumber));
		    acknowledgment++;
		    timer = 0;
		    }
			
		    /** This block determines if the first packet, the file name was lost. If after 100 loops, we have
		     * received no packets, the file name is resent to the server **/
		    if(currentPacketNumber == 0){
			    fileReceivedTimer++;
			    System.out.println("fileReceivedTimer: " + fileReceivedTimer);
			    if(fileReceivedTimer >= 100){
				    sc.send(buf, new InetSocketAddress((ipAddress), portNumber));
			    }
		    }
				    

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
