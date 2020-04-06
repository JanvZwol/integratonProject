package my_protocol;
import framework.IMACProtocol;
import framework.MediumState;
import framework.TransmissionInfo;
import framework.TransmissionType;
import java.util.Random;
/**
* A fairly trivial Medium Access Control scheme.
*
* @author Jaco ter Braak, University of Twente
* @version 05-12-2013
*
* Copyright University of Twente, 2013-2019
*
**************************************************************************
*                         Copyright notice                               *
*                                                                        *
*             This file may ONLY be distributed UNMODIFIED.              *
* In particular, a correct solution to the challenge must NOT be posted  *
* in public places, to preserve the learning effect for future students. *
**************************************************************************
*/

//By  Jan van Zwol s2159732
//and Brand Hauser s2234823

public class MyProtocol implements IMACProtocol {
   //protocol variables
   public static final int INITTRANSMISSIONCHANCE = 25;
   public static final int NODES = 4;
   private static final int MAXPACKETNUMBER = 4;
   private static final int SECONDCHANCE = 5;
   private static final int WAITNUMBER = 1;
   public static final int QUEUETRANSMISSIONCHANCE = 100;
   
   public static final int DECREMENTCHANCE = 50;
   
   //requesting queue variable
   private int waitCount = 0;
   private boolean inQueue = false;
   private boolean waiting = true;
   private int cycled = 0;
   
   //queue variable
   private int packetCount = MAXPACKETNUMBER;
   private LinkedList queue = new LinkedList();
   private int queueChance = QUEUETRANSMISSIONCHANCE;
  
   //initialisation variables
   private boolean initialized = false;
   private boolean justSend    = false;
   private int IDNumber = -1;
   private int queueNumber = 0;
   private int initChance = 100 / (4 - queueNumber);
   
   //Test varaiables
   private int collision = 0;
   private int succes = 0;
   private int idle = 0;

   @Override
   public TransmissionInfo TimeslotAvailable(MediumState previousMediumState,
                                             int controlInformation, int localQueueLength) {
	   
       //Check if queue number is successful
       if (justSend && previousMediumState == MediumState.Succes) {
           IDNumber = controlInformation;
       }
       justSend = false;

       //Initialisation Algorithm
       if (!initialized) {
    	   //If the previous medium state was succes that means someone has aquired an ID
           if (previousMediumState == MediumState.Succes) {
               queueNumber = controlInformation + 1;
               if (queueNumber != 4) {
            	   //Set the new initTransmissionChance according to the amount of Nodes needing an ID
            	   initChance = 100 / (4 - queueNumber);
               }
               //If the amount of ID's acquired equals the number of Nodes the initialisation is finished
               if (queueNumber == NODES) {
                   initialized = true;
                   queueNumber = 0;
                   return new TransmissionInfo(TransmissionType.Silent, 0);
               }
           }
           //Follow ID acquirement protocol
           return initTransmission();
       }
       
       //sets the chance of a Node sending a message requesting a spot in the queue according to the previous amount of collisions
       if (previousMediumState == MediumState.Collision) {
    	   queueChance = Math.max(33, queueChance - DECREMENTCHANCE);
       } else if (controlInformation >= 300){
    	   queueChance = Math.min(queueChance + DECREMENTCHANCE, QUEUETRANSMISSIONCHANCE);
       }
       
        //Checking if transmitting is over according to the control information
        //200 means the Node is ready with sending and there will be slots for requesting a spot in the queue
        if (controlInformation == 200) {
     	   waitCount = 0;
     	   cycled = 0;
     	   waiting = true;
        //230 means the same as 200 but the sender wishes to be removed from the queue
        } else if (controlInformation == 230) {
    	    waitCount = 0;
    	   	queue.removeFirst();
    	    waiting = true;
    	//210 means the sender is ready with sending and will pass the turn to the next one in the queue
        } else if (controlInformation == 210) {
    	   queue.cycle();
    	   cycled++;
        }
       
        //This will check whether the window where in nodes can request a place in the queue is over
        if (waitCount == WAITNUMBER) {
    	    waiting = false;
    	    queue.cycle();
        }
       
       //If there was a collision the current time slot doesn't count for the requesting window to improve fairness
       if (previousMediumState != MediumState.Collision) {
    	   waitCount++;
       }
       
       //If there is no one in the queue the requesting window does not stop
       if (queue.size() == 0){
    	   waitCount = 0;
       }
       
       
                    
       //If anyone send a message with a controlInformation between 300 and 399 the last to digits of the message will be added
       //to the queue if it is not already in the queue
       if (controlInformation < 400 && controlInformation >= 300 && controlInformation - 300 < NODES && !queue.checkID(controlInformation - 300)) {
    	   queue.addToQueue(controlInformation - 300);
    	   waiting = false;
    	   queue.cycle();
       }
       
        //There is a random chance of 80% or lower depending on the amount of previous collisions that a node with data will send 
        //a request for getting in the queue
        if (!queue.checkID(IDNumber) && localQueueLength != 0 && waiting) {
    	    if (new Random().nextInt(100) <= queueChance) {
    	    	//If the node has only one piece of data he will send a message with control information
    	    	//400 + IDNumber meaning he has send data but does not wish to enter the queue
                if (localQueueLength == 1) {
                	return new TransmissionInfo(TransmissionType.Data, 400 + IDNumber);
                } 
                // If he has more data it will use control information 300 + IDNumber indicating it wants to enter the queue
                else {
                	return new TransmissionInfo(TransmissionType.Data, 300 + IDNumber);
                }
            }
        }
        
        //This checks if the node is the first node in the queue and it is not a requesting window if so it can send data
        if (!waiting && queue.size() > 0 && IDNumber == queue.first()) {
        	//If the node will send a packet after this one it sends code 100 + IDNumber indicating no one but him should be sending
    	    if (packetCount > 1 && localQueueLength >1) {
    		    packetCount--;
    		    return new TransmissionInfo(TransmissionType.Data, 100 + IDNumber);
    	    }
    	    //If the node will end its transmission after this and secedes his turn to the next one in the queue it will send 200
    	    else if (packetCount == 1 && localQueueLength > 1 && cycled == 1) {
    		    packetCount = MAXPACKETNUMBER;
    		    return new TransmissionInfo(TransmissionType.Data, 200);
       	    }
    	    //If the node will end its transmission after this and will open a requesting window it will send 210
    	    else if (packetCount == 1 && localQueueLength > 1 && cycled == 0) {
       	    	if (queue.size() > 1) {
       	    		packetCount = MAXPACKETNUMBER;
       	    	} else {
       	    		packetCount = SECONDCHANCE;
       	    	}
    		    return new TransmissionInfo(TransmissionType.Data, 210);
    		//If the node will end its transmission after this and wishes to be taken out of the queue it will send 230
       	    } else {
    	 	   packetCount = MAXPACKETNUMBER;
    	 	   return new TransmissionInfo(TransmissionType.Data, 230);
    	    }
        }
        
        //If nothing has to be send by this node it will not send anything
        return new TransmissionInfo(TransmissionType.Silent, 0);
   }

   //initialisation protocol
   public TransmissionInfo initTransmission() {
	   //If it has an ID it will do nothing
       if (IDNumber != -1) {
           return new TransmissionInfo(TransmissionType.Silent, 0);
       }
       //If it has no ID it uses the ALOHA model with reducing chance according to the amount of unclaimed ID's
       else if (new Random().nextInt(100) <= initChance) {
           justSend = true;
           return new TransmissionInfo(TransmissionType.NoData, queueNumber);
       } else {
           return new TransmissionInfo(TransmissionType.Silent, 0);
       }
   }
}
