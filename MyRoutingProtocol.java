package my_protocol;

import framework.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @version 12-03-2019
*
* Copyright University of Twente, 2013-2019
*
**************************************************************************
*                         Copyright notice                           *
*                                                                     *
*             This file may ONLY be distributed UNMODIFIED.              *
* In particular, a correct solution to the challenge must NOT be posted  *
* in public places, to preserve the learning effect for future students. *
**************************************************************************
*/
public class MyRoutingProtocol implements IRoutingProtocol {
   private LinkLayer linkLayer;
   private HashMap<Integer, MyRoute> myRoutingTable = new HashMap<>();
   private final int STARTTTL = 4;
   private final int INFINITY = 2000;


   // You can use this data structure to store your routing table.


   @Override
   public void init(LinkLayer linkLayer) {
       this.linkLayer = linkLayer;
       int myAddress = this.linkLayer.getOwnAddress();
   }


   @Override
   public void tick(PacketWithLinkCost[] packetsWithLinkCosts) {
       // Get the address of this node
       int myAddress = this.linkLayer.getOwnAddress();

       for (int dest: myRoutingTable.keySet()){
           MyRoute r = myRoutingTable.get(dest);
           if (r.cost != INFINITY) {
               r.ttl--;
           }
           if (r.ttl == 0){
               r.cost = INFINITY;
           }
           myRoutingTable.replace(dest, r);
       }
       for (int dest: myRoutingTable.keySet()){
           MyRoute r = myRoutingTable.get(dest);
           if (myRoutingTable.get(myRoutingTable.get(dest).nextHop).cost == INFINITY) {
               r.cost = INFINITY;
           }
           myRoutingTable.replace(dest, r);
       }

       System.out.println("tick; received " + packetsWithLinkCosts.length + " packets");
       int i;

       // first process the incoming packets; loop over them:
       for (i = 0; i < packetsWithLinkCosts.length; i++) {
           Packet packet = packetsWithLinkCosts[i].getPacket();
           int neighbour = packet.getSourceAddress();             // from whom is the packet?
           int linkcost = packetsWithLinkCosts[i].getLinkCost();  // what's the link cost from/to this neighbour?
           DataTable dt = packet.getDataTable();                  // other data contained in the packet
           System.out.printf("received packet from %d with %d rows and %d columns of data%n", neighbour, dt.getNRows(), dt.getNColumns());

           //example code for checking whether some destination is already in myRoutingTable, and accessing it:
           if (myRoutingTable.containsKey(neighbour)){
               MyRoute r = myRoutingTable.get(neighbour);
               if (r.cost > linkcost) {
                   r.cost = linkcost;
                   r.nextHop = neighbour;
                   r.ttl = STARTTTL;
                   myRoutingTable.replace(neighbour, r);
               }
           } else {
               MyRoute r = new MyRoute(neighbour, linkcost, STARTTTL);
               myRoutingTable.put(neighbour, r);
           }


           if (!packet.isRaw())
               for (int j=0; j < dt.getNRows(); j++){
                   int dest = dt.get(j,0);
                   int cost = dt.get(j, 1);
                   int next = dt.get(j, 2);
                   if (dest != myAddress && myRoutingTable.containsKey(dest)) {
                       MyRoute r = myRoutingTable.get(dest);
                       // do something with r.cost and r.nextHop; you can even modify them
                       if (r.cost > cost + linkcost && next != myAddress) {
                           r.cost = cost + linkcost;
                           r.nextHop = neighbour;
                           r.ttl = STARTTTL;
                       } else if(myRoutingTable.get(dest).nextHop == neighbour){
                           r.cost = cost + linkcost;
                       } else if (cost + linkcost == r.cost){
                           r.cost = INFINITY;
                       }
                       myRoutingTable.put(dest, r);
                   } else if (dest != myAddress){
                       MyRoute r = new MyRoute(neighbour, cost+linkcost, STARTTTL);
                       myRoutingTable.put(dest, r);
                   }

               }


       }
       sendRoutingTable();
   }

   public void sendRoutingTable(){
       DataTable rt = new DataTable(3);
       int row = -1;
       for (int dest: myRoutingTable.keySet()){
           row++;
           rt.set(row, 0, dest);
           rt.set(row, 1, myRoutingTable.get(dest).getCost());
           rt.set(row, 2, myRoutingTable.get(dest).getNextHop());
       }


       Packet pkt = new Packet(linkLayer.getOwnAddress(), 0, rt);
       linkLayer.transmit(pkt);
   }


   public Map<Integer, Integer> getForwardingTable() {
       // This code extracts from your routing table the forwarding table.
       // The result of this method is send to the server to validate and score your protocol.

       // <Destination, NextHop>
       HashMap<Integer, Integer> ft = new HashMap<>();

       for (Map.Entry<Integer, MyRoute> entry : myRoutingTable.entrySet()) {
           ft.put(entry.getKey(), entry.getValue().nextHop);
       }

       return ft;
   }
}
