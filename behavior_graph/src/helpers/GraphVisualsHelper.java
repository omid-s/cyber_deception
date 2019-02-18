/**
 * 
 */
package helpers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import classes.AccessCall;
import classes.ResourceItem;
import classes.ResourceType;
import edu.uci.ics.jung.graph.Graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;

/**
 * @author omido
 *
 */
public class GraphVisualsHelper {

	/// get edge labels
	private Transformer<AccessCall, String> EdgeLabeler = new Transformer<AccessCall, String>() {
		public String transform(AccessCall e) {
			String Call = "";

			Call = e.Command + "(" + e.OccuranceFactor + ")" + "-" + e.sequenceNumber;

			return Call; // "Call ( "+ Call + " ) : "
							// +graph.getEndpoints(e).toString();
		}
	};

	public Transformer<AccessCall, String> getEdgeLabeler() {
		return EdgeLabeler;
	}

	// ------------------------
	private Transformer<AccessCall, String> EdgeToolTiper = new Transformer<AccessCall, String>() {
		public String transform(AccessCall e) {
			String Call = "";

			Call = e.toString(); // e.Command + "(" + e.OccuranceFactor + ")";

			return Call; // "Call ( "+ Call + " ) : "
							// +graph.getEndpoints(e).toString();
		}
	};

	public Transformer<AccessCall, String> getEdgeToolTip() {
		return EdgeToolTiper;
	}

	/// --------------------------

	Transformer<ResourceItem, String> VertexLabeler = new Transformer<ResourceItem, String>() {
		public String transform(ResourceItem e) {
			String ret = "";
			ret += e.Type.toString() + " | " + e.Number + " ("
					+ ((e.Type.equals(ResourceType.Process)) ? e.Title : e.Title) + ")";

			return ret;
		}
	};

	public Transformer<ResourceItem, String> getVertexLabler() {
		return VertexLabeler;
	}

	/// -------------------------------------------------------------------

	private Transformer<ResourceItem, Paint> colorifier = new Transformer<ResourceItem, Paint>() {
		public Paint transform(ResourceItem e) {
			Color[] colors = { Color.RED, Color.YELLOW, Color.blue, Color.LIGHT_GRAY, Color.green, Color.ORANGE,
					Color.PINK, Color.pink, Color.gray, Color.CYAN, Color.black, Color.WHITE, Color.DARK_GRAY };

			switch (e.Type) {
			case Process:
				return colors[0];
			case Thread:
				return colors[1];
			case NetworkIPV4:
				return colors[2];
			case NetworkIPV6:
				return colors[3];
			case Unix:
				return colors[4];
			case SignalFDs:
				return colors[5];
			case EventFDs:
				return colors[6];
			case iNotifyFDS:
				return colors[7];
			case TimerFDs:
				return colors[8];
			case File:
				return colors[9];
			case Pipe:
				return colors[10];
			case Activity:
				return colors[11];
			case Service:
				return colors[12];
			case UBSIUnit:
				return colors[6];
			}
			return Color.magenta;
		}
	};

	public Transformer<ResourceItem, Paint> getVerticeColorifier() {
		return colorifier;
	}
	/// --------------------------------------------------------------

	private Transformer<ResourceItem, String> toolTiper = new Transformer<ResourceItem, String>() {

		@Override
		public String transform(ResourceItem I) {
			// return String.valueOf(i*i + i );
			String ret = "";
			/*
			 * switch (i.) { case "1": ret = "Process \r\n Name : httpd , ID : 12"; break;
			 * case "2": ret = "Thread \r\n ID : 18 "; break; case "5": ret =
			 * "Thread \r\n ID : 19 "; break; case "3": ret =
			 * "Network : port : 233 , location : 127.0.0.1"; break; case "4": ret =
			 * "File \r\n Directory : /usr/omido/ Name : data.cnfg "; break; default: ret =
			 * "Resource  "; break; }
			 */
			return ret;
		}
	};

	public Transformer<ResourceItem, String> gettoolTipper() {
		return toolTiper;
	}

	// .---------------------------------------------------------------------------

	private Map<String, Color> SelectedColorForCommands = new HashMap<String, Color>();
	private ColorHelpers ColorHelperFactory = new ColorHelpers();

	private Transformer<AccessCall, Paint> EdgeColorizer = new Transformer<AccessCall, Paint>() {

		@Override
		public Color transform(AccessCall item) {
			// return String.valueOf(i*i + i );

			if (!SelectedColorForCommands.containsKey(item.Command))
				SelectedColorForCommands.put(item.Command, ColorHelperFactory.GenerateNewColor());

			return SelectedColorForCommands.get(item.Command);
		}
	};

	public Transformer<AccessCall, Paint> getEdgeColorizer() {
		return EdgeColorizer;
	}

	/// -----------------------------------------------------------

	private Graph<ResourceItem, AccessCall> tempGraph;

	private Transformer<AccessCall, Stroke> EdgeStroker = new Transformer<AccessCall, Stroke>() {
		public Stroke transform(AccessCall Item) {
			int theNumber = 1
					+ (Item.OccuranceFactor / (tempGraph.getEdgeCount() != 0 ? tempGraph.getEdgeCount() * 10 : 1));
			if (theNumber > 8)
				theNumber = 8;
			return new BasicStroke(theNumber);
			// 1 + (Item.OccuranceFactor / (Calls.size() * 10)));
		}
	};

	public Transformer<AccessCall, Stroke> getEdgeStroker(Graph<ResourceItem, AccessCall> tempGraph) {
		this.tempGraph = tempGraph;
		return EdgeStroker;
	}

	/// --------------------------------------------------------------------------------------
	private Transformer<ResourceItem, Shape> vertexShapeTransformer = new Transformer<ResourceItem, Shape>() {
		float size = 12;

		@Override
		public Shape transform(ResourceItem arg0) {
			Shape ret = null;
			switch (arg0.Type) {
			case Process:
				ret = new Ellipse2D.Float(-2 * size, -1 * size / 2, size * 4, size);
				break;
			case File:
			case Pipe:
				ret = new Rectangle2D.Float(-2 * size, -1 * size / 2, size * 4, size);
				break;
			case Thread:
				int x_1 = (int)Math.round( Math.cos(18) * 1*size );
				int x_2 = (int)Math.round( Math.sin(36) * 1*size );
				int y_1 = (int)Math.round( Math.sin(18) * 1*size );
				int y_2 = (int)Math.round( Math.cos(36) * 1*size );

				Polygon tp = new Polygon();
				tp.addPoint(0, -1 * (int) size);
				tp.addPoint(-1 * x_2, y_2);
				tp.addPoint(x_1, -1 * y_1);
				tp.addPoint(-1 * x_1, -1 * y_1);
				tp.addPoint(x_2, y_2);
				ret = tp;
				break;
			case NetworkIPV4:
			case NetworkIPV6:
				Polygon tp2 = new Polygon();
				tp2.addPoint(0, -1 * (int) size);
				tp2.addPoint(-2 * (int) size, 0);
				tp2.addPoint(0, 1 * (int) size);
				tp2.addPoint(2 * (int) size, 0);
				ret = tp2;
				break;
			default:
				ret = new Ellipse2D.Float(-1 * size / 2, -1 * size / 2, size, size);
				break;
			}

			return ret;
			// return new Ellipse2D.Float(-1 * size / 2, -1 * size / 2, size,
			// size);

			// return new Rectangle2D.Float(-1 * size / 2, -1 * size / 2, size,
			// size);

		}
	};

	public Transformer<ResourceItem, Shape> getVertexShapper() {
		return this.vertexShapeTransformer;
	}

}
