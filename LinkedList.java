package my_protocol;

//Part of Challenge 3
//By  Jan van Zwol s2159732
//and Brand Hauser s2234823

public class LinkedList<Element> {
	
	private int size;
	private Node first;
	
	public LinkedList () {
		size = 0;
		first = null;
	}
	
	//addsnode to the back
	public void addNode(Node newNode) {
		if (size == 0) {
			first = newNode;
			newNode.next = newNode;
		} else {
			lastNode().next = newNode;
			newNode.next = first;
		}
		
		size++;
	}
	
	//Returns size
	public int size() {
		return size;
	}
	
	// returns last node
	public Node lastNode() {
		Node currentNode = first;
		for (int i = 0; i < size - 1; i++) {
			currentNode = currentNode.next;
		}
		return currentNode;
	}
	
	//Gets the first value of the queue
	public int first () {
		return first.getValue();
	}
	
	//Cycle the queue. Second becomes first. First becomes last.
	public void cycle() {
		first = first.next;
	}
	
	//Add a new value to the back of the queue.
	public void addToQueue(int value) {
    	Node newNode = new Node(value);
    	addNode(newNode);
    }
	
	//Removes the first node from the queue. Second becomes first.
	public void removeFirst() {
		lastNode().next = first.next;
		first = first.next;
		size--;
	}
	
	//check whether id is in list
	public boolean checkID(int id) {
		Node currentNode = first;
		for (int i = 0; i < size; i++) {
			if (id == currentNode.getValue()) {
				return true;
			}
		currentNode = currentNode.next;
		}
		return false;
	}
}
