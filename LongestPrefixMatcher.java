/**
 * LongestPrefixMatcher.java
 *
 *   Version: 2019-07-10
 * Copyright: University of Twente, 2015-2019
 *
 **************************************************************************
 *                            Copyright notice                            *
 *                                                                        *
 *             This file may ONLY be distributed UNMODIFIED.              *
 * In particular, a correct solution to the challenge must NOT be posted  *
 * in public places, to preserve the learning effect for future students. *
 **************************************************************************
 */

//Jan van Zwol s2159732
//Brand Hauser s2234823

package lpm;

import java.util.HashMap;
import java.util.Map;

public class LongestPrefixMatcher {
	public static final int SHORTESTPREFIX = 8;
	public static final int LONGESTPREFIX = 32;
	private Map[] portTree = new Map[LONGESTPREFIX - SHORTESTPREFIX + 1];
	
  /**
   * You can use this function to initialize variables.
   */
    public LongestPrefixMatcher() {
    	for (int i = 0; i <= LONGESTPREFIX - SHORTESTPREFIX; i++) {
        	portTree[i] = new HashMap<Integer, Integer>();
        }
    }
    
    /**
     * Looks up an IP address in the routing tables
     * @param ip The IP address to be looked up in integer representation
     * @return The port number this IP maps to
     */
    public int lookup(int ip) {
    	int treeLength = portTree.length;
        for (int i = 0; i < treeLength; i++) {
        	if (portTree[treeLength - i - 1].containsKey(ip >> i)) {
        		return (int) portTree[treeLength - i - 1].get(ip >> i);
       		}
        }
        return -1;
    }

    /**
     * Adds a route to the routing tables
     * @param ip The IP the block starts at in integer representation
     * @param prefixLength The number of bits indicating the network part
     *                     of the address range (notation ip/prefixLength)
     * @param portNumber The port number the IP block should route to
     */
    public void addRoute(int ip, byte prefixLength, int portNumber) {
      	int prefix = ip >> (32 - prefixLength);
    	portTree[prefixLength - SHORTESTPREFIX].put(prefix, portNumber);
    }

    /**
     * This method is called after all routes have been added.
     * You don't have to use this method but can use it to sort or otherwise
     * organize the routing information, if your datastructure requires this.
     */
    public void finalizeRoutes() {
    }

    /**
     * Converts an integer representation IP to the human readable form
     * @param ip The IP address to convert
     * @return The String representation for the IP (as xxx.xxx.xxx.xxx)
     */
    private String ipToHuman(int ip) {
        return Integer.toString(ip >> 24 & 0xff) + "." +
                Integer.toString(ip >> 16 & 0xff) + "." +
                Integer.toString(ip >> 8 & 0xff) + "." +
                Integer.toString(ip & 0xff);
    }

    /**
     * Parses an IP
     * @param ipString The IP address to convert
     * @return The integer representation for the IP
     */
    private int parseIP(String ipString) {
        String[] ipParts = ipString.split("\\.");

        int ip = 0;
        for (int i = 0; i < 4; i++) {
        	ip |= Integer.parseInt(ipParts[i]) << (24 - (8 * i));           
        }

        return ip;
    }
}


