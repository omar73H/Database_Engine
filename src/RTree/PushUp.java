package RTree;

import RASQL.comparablePolygon;

public class PushUp {

	/**
	 * This class is used for push keys up to the inner nodes in case
	 * of splitting at a lower level
	 */
	RTreeNode newNode;
	comparablePolygon key;
	
	public PushUp(RTreeNode newNode, comparablePolygon key)
	{
		this.newNode = newNode;
		this.key = key;
	}
}