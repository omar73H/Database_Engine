package BPTree;

import java.io.FileReader;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

import recordsPointer.Ref;



public class BPTree<T extends Comparable<T>> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int order;
	private BPTreeNode<T> root;

	private String strTableName; // may be used without
	private String strColName;
	
	/**
	 * Creates an empty B+ tree
	 * @param order the maximum number of keys in the nodes of the tree
	 */
	public BPTree(String strTableName,String strColName) 
	{
		this.strTableName = strTableName;
		this.strColName = strColName;
		order = getNodeSize();
		root = new BPTreeLeafNode<T>(this.order,this.strTableName,this.strColName);
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
	public void insert(T key, Ref recordReference)
	{
		PushUp<T> pushUp = root.insert(key, recordReference, null, -1);
		if(pushUp != null)
		{
			BPTreeInnerNode<T> newRoot = new BPTreeInnerNode<T>(order);
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
	public String search(T key)
	{
		return root.search(key);
	}
	
	/**
	 * Delete a key and its associated record from the tree.
	 * @param key the key to be deleted
	 * @return a boolean to indicate whether the key is successfully deleted or it was not in the tree
	 */
	public boolean delete(T key)
	{
		boolean done = root.delete(key, null, -1);
		//go down and find the new root in case the old root is deleted
		while(root instanceof BPTreeInnerNode && !root.isRoot())
			root = ((BPTreeInnerNode<T>) root).getFirstChild();
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
		Queue<BPTreeNode<T>> cur = new LinkedList<BPTreeNode<T>>(), next;
		cur.add(root);
		while(!cur.isEmpty())
		{
			next = new LinkedList<BPTreeNode<T>>();
			while(!cur.isEmpty())
			{
				BPTreeNode<T> curNode = cur.remove();
				System.out.print(curNode);
				if(curNode instanceof BPTreeLeafNode)
					System.out.print("->");
				else
				{
					System.out.print("{");
					BPTreeInnerNode<T> parent = (BPTreeInnerNode<T>) curNode;
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
	
	public BPTreeLeafNode getSmallestLeafNode()
	{
		return root.getSmallestLeafNode();
	}
	
	public BPTreeLeafNode searchKey(T key)
	{
		return root.searchLeafNode(key);
	}
}