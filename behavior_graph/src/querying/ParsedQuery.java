/**
 * 
 */
package querying;

import java.util.ArrayList;

import classes.AccessCall;
import classes.ResourceItem;
import classes.ResourceType;
import edu.uci.ics.jung.graph.Graph;
import querying.parsing.Criteria;

/**
 * @author omid
 *
 *         This class will hold representation of the parsed query This will be
 *         passed among different parts of the program
 */
public class ParsedQuery {

	private ArrayList<ResourceType> verticeTypes = null;
	private ArrayList<String> edgeTypes = null;
	private boolean isVerbose = false;
	private boolean isBackTracked = false;
	private boolean isForwardTracked = false;
	private ArrayList<Criteria> criterias = null;
	private Graph<ResourceItem, AccessCall> originalGraph = null;
	private String queryString = null;
	private boolean doesAppend = false;

	

	public ParsedQuery(ArrayList<ResourceType> verticeTypes, ArrayList<String> edgeTypes, boolean iVerbose,
			boolean isBackTracked, boolean isForwardTracked, ArrayList<Criteria> criterias,
			Graph<ResourceItem, AccessCall> originalGraph , boolean doesAppend) {

		this(verticeTypes, edgeTypes, iVerbose, isBackTracked, isForwardTracked, criterias, originalGraph, "", doesAppend);
	}

	public ParsedQuery(ArrayList<ResourceType> verticeTypes, ArrayList<String> edgeTypes, boolean iVerbose,
			boolean isBackTracked, boolean isForwardTracked, ArrayList<Criteria> criterias,
			Graph<ResourceItem, AccessCall> originalGraph, String queryString, boolean doesAppend) {
		super();
		this.verticeTypes = verticeTypes;
		this.edgeTypes = edgeTypes;
		this.isVerbose = iVerbose;
		this.isBackTracked = isBackTracked;
		this.isForwardTracked = isForwardTracked;
		this.criterias = criterias;
		this.originalGraph = originalGraph;
		this.queryString = queryString;
		this.doesAppend = doesAppend;
	}

	
	public boolean isDoesAppend() {
		return doesAppend;
	}

	public void setDoesAppend(boolean doesAppend) {
		this.doesAppend = doesAppend;
	}
	
	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public ArrayList<ResourceType> getVerticeTypes() {
		return verticeTypes;
	}

	public void setVerticeTypes(ArrayList<ResourceType> verticeTypes) {
		this.verticeTypes = verticeTypes;
	}

	public ArrayList<String> getEdgeTypes() {
		return edgeTypes;
	}

	public void setEdgeTypes(ArrayList<String> edgeTypes) {
		this.edgeTypes = edgeTypes;
	}

	public boolean isVerbose() {
		return isVerbose;
	}

	public void setVerbose(boolean isVerbose) {
		this.isVerbose = isVerbose;
	}

	public boolean isBackTracked() {
		return isBackTracked;
	}

	public void setBackTracked(boolean isBackTracked) {
		this.isBackTracked = isBackTracked;
	}

	public boolean isForwardTracked() {
		return isForwardTracked;
	}

	public void setForwardTracked(boolean isForwardTracked) {
		this.isForwardTracked = isForwardTracked;
	}

	public ArrayList<Criteria> getCriterias() {
		return criterias;
	}

	public void setCriterias(ArrayList<Criteria> criterias) {
		this.criterias = criterias;
	}

	public Graph<ResourceItem, AccessCall> getOriginalGraph() {
		return originalGraph;
	}

	public void setOriginalGraph(Graph<ResourceItem, AccessCall> originalGraph) {
		this.originalGraph = originalGraph;
	}

}
