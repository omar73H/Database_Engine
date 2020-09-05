package RASQL;
import java.awt.Dimension;
import java.awt.Polygon;
import java.util.Arrays;

public class comparablePolygon extends java.awt.Polygon implements Comparable<comparablePolygon>{
	public comparablePolygon(int[]xpoints,int[]ypoints,int npoints) {
		super(xpoints, ypoints, npoints);
	}
	
	
	public static void main(String[] args) {
		int[]x1= {1,2,3},y1= {7,5,6};
		comparablePolygon p = new comparablePolygon(x1, y1, 3), p2 = new comparablePolygon(x1, new int[]{4,5,6}, 3) ;
		System.out.println(p.compareTo(p2));
	}

	@Override
	public int compareTo(comparablePolygon o) {
		Dimension thisDim=getBounds().getSize();
		Dimension dim=o.getBounds().getSize();
		
		long thisArea=thisDim.height*1l*thisDim.width;
		long area=dim.width*1l*dim.height;
		return Long.compare(thisArea, area);
	}
	
	public boolean equals(Object o) {
		comparablePolygon polygon = (comparablePolygon) o;
		if(this.npoints != polygon.npoints)
			return false;
				
		int[][]thisPoints=new int[npoints][2];
		int[][]oPoints=new int[npoints][2];
		
		int idx =0;
		while(idx<npoints)
		{
			thisPoints[idx]=new int[] {xpoints[idx],ypoints[idx]};
			oPoints[idx]=new int[] {polygon.xpoints[idx],polygon.ypoints[idx]};
			
			idx++;
		}
		Arrays.sort(thisPoints,(x,y)->x[0]==y[0]?x[1]-y[1]:x[0]-y[0]);
		Arrays.sort(oPoints,(x,y)->x[0]==y[0]?x[1]-y[1]:x[0]-y[0]);
		
		idx =0;
		while(idx<npoints)
		{
			if(thisPoints[idx][0]!=oPoints[idx][0] || thisPoints[idx][1]!=oPoints[idx][1])
				return false;
			idx++;
		}
		return true;
	}
	public boolean equals(comparablePolygon polygon) {
		if(this.npoints != polygon.npoints)
			return false;
				
		int[][]thisPoints=new int[npoints][2];
		int[][]oPoints=new int[npoints][2];
		
		int idx =0;
		while(idx<npoints)
		{
			thisPoints[idx]=new int[] {xpoints[idx],ypoints[idx]};
			oPoints[idx]=new int[] {polygon.xpoints[idx],polygon.ypoints[idx]};
			
			idx++;
		}
		Arrays.sort(thisPoints,(x,y)->x[0]==y[0]?x[1]-y[1]:x[0]-y[0]);
		Arrays.sort(oPoints,(x,y)->x[0]==y[0]?x[1]-y[1]:x[0]-y[0]);
		
		idx =0;
		while(idx<npoints)
		{
			if(thisPoints[idx][0]!=oPoints[idx][0] || thisPoints[idx][1]!=oPoints[idx][1])
				return false;
			idx++;
		}
		return true;
	}
	
	public String toString() {
		StringBuilder s=new StringBuilder();
		
		for(int i=0;i<npoints;i++) {
			s.append("("+xpoints[i]+","+ypoints[i]+")");
			if(i!=npoints-1) {
				s.append(",");
			}
		}
		return s.toString();
	}
}
