package my_protocol;

//Part of Challenge 3
//By  Jan van Zwol s2159732
//and Brand Hauser s2234823

public class Node {
	
	private int value;
	public Node next;
	
	public Node(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
}
