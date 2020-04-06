package my_protocol;

/**
* Simple object which describes a route entry in the forwarding table.
* Can be extended to include additional data.
*
* Copyright University of Twente, 2013-2019
*
**************************************************************************
*                            Copyright notice                            *
*                                                                        *
*             This file may ONLY be distributed UNMODIFIED.              *
* In particular, a correct solution to the challenge must NOT be posted  *
* in public places, to preserve the learning effect for future students. *
**************************************************************************
*/
public class MyRoute {
   public int nextHop;
   public int cost;
   public int ttl;

   public MyRoute(int nextHop, int cost, int ttl) {
       this.nextHop = nextHop;
       this.cost    = cost;
       this.ttl = 1;
   }

   public int getCost() {
       return cost;
   }

   public int getNextHop() {
       return nextHop;
   }

   public int geTttl(){ return this.ttl;}

   public void setTtl(int newTtl){
       this.ttl = newTtl;
   }
}
