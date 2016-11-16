package hu.qgears.repocache;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.commons.UtilString;

public class Path {
	public List<String> pieces;
	public boolean folder;
	public Path(String path)
	{
		pieces=UtilString.split(path, "/");
		folder=path.endsWith("/");
	}
	public Path(Path p) {
		pieces=new ArrayList<>(p.pieces);
		folder=p.folder;
	}
	public String toStringPath() {
		return UtilString.concat(pieces, "/")+(folder?"/":"");
	}
	public Path add(String string) {
		pieces.add(string);
		folder=false;
		return this;
	}
	public Path remove(int i) {
		pieces.remove(i);
		return this;
	}
	public boolean eq(int i, String path) {
		return (pieces.size()>i)&&pieces.get(i).equals(path) ;
	}
	public Path setFolder(boolean b) {
		folder=b;
		return this;
	}
	@Override
	public String toString() {
		return "PATH: "+pieces+" "+folder;
	}
	public String getFileName() {
		if(pieces.size()==0)
		{
			return null;
		}
		return pieces.get(pieces.size()-1);
	}
}
