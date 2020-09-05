package RTree;

import java.io.FileReader;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;


import RASQL.comparablePolygon;
import recordsPointer.Ref;

public class RTree implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int order;
	private RTreeNode root;

	private String strTableName; // may be used without
	private String strColName;
	
	/**
	 * Creates an empty B+ tree
	 * @param order the maximum number of keys in the nodes of the tree
	 */
	public RTree(String strTableName,String strColName) 
	{
		this.strTableName = strTableName;
		this.strColName = strColName;
		order = getNodeSize();
		root = new RTreeLeafNode(this.order,this.strTableName,this.strColName);
		root.setRoot(true);
	}
	
	/**
	 * 
	 * @return: the node size (maximum number of keys that the node can hold) according to what specified in
	 * 			the properties file 
	 */
	private static int getNodeSize(){
		try 
		{
			FileReader reader = new FileReader("config/DBApp.properties");
			Properties properties = new Properties();
			properties.load(reader);
			return Integer.parseInt(properties.getProperty("NodeSize"));			
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
		return 15;
	}
	
	/**
	 * Inserts the specified key associated with the given record in the B+ tree
	 * @param key the key to be inserted
	 * @param recordReference the reference of the record associated with the key
	 */
	public void insert(comparablePolygon key, Ref recordReference)
	{
		PushUp pushUp = root.insert(key, recordReference, null, -1);
		if(pushUp != null)
		{
			RTreeInnerNode newRoot = new RTreeInnerNode(order);
			newRoot.insertLeftAt(0, pushUp.key, root);
			newRoot.setChild(1, pushUp.newNode);
			root.setRoot(false);
			root = newRoot;
			root.setRoot(true);
		}
	}
	
	
	/**
	 * Looks up for the record that is associated with the specified key
	 * @param key the key to find its record
	 * @return String the "Over Flow page" name associated with this key 
	 */
	public String search(comparablePolygon key)
	{
		return root.search(key);
	}
	
	/**
	 * Delete a key and its associated record from the tree.
	 * @param key the key to be deleted
	 * @return a boolean to indicate whether the key is successfully deleted or it was not in the tree
	 */
	public boolean delete(comparablePolygon key)
	{
		boolean done = root.delete(key, null, -1);
		//go down and find the new root in case the old root is deleted
		while(root instanceof RTreeInnerNode && !root.isRoot())
			root = ((RTreeInnerNode) root).getFirstChild();
		return done;
	}
	
	
	
	/**
	 * Returns a string representation of the B+ tree.
	 */
	public String toString()
	{	
		
		//	<For Testing>
		// node :  (id)[k1|k2|k3|k4]{P1,P2,P3,}
		String s = "";
		Queue<RTreeNode> cur = new LinkedList<RTreeNode>(), next;
		cur.add(root);
		while(!cur.isEmpty())
		{
			next = new LinkedList<RTreeNode>();
			while(!cur.isEmpty())
			{
				RTreeNode curNode = cur.remove();
				System.out.print(curNode);
				if(curNode instanceof RTreeLeafNode)
					System.out.print("->");
				else
				{
					System.out.print("{");
					RTreeInnerNode parent = (RTreeInnerNode) curNode;
					for(int i = 0; i <= parent.numberOfKeys; ++i)
					{
						System.out.print(parent.getChild(i).index+",");
						next.add(parent.getChild(i));
					}
					System.out.print("} ");
				}
				
			}
			System.out.println();
			cur = next;
		}	
		//	</For Testing>
		return s;
	}
	
	public RTreeLeafNode getSmallestLeafNode()
	{
		return root.getSmallestLeafNode();
	}
	
	public RTreeLeafNode searchKey(comparablePolygon key)
	{
		return root.searchLeafNode(key);
	}
}