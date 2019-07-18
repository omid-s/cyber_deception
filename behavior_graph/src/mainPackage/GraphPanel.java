package mainPackage;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author omido
 */
/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 */

import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.iterators.ArrayListIterator;

import classes.AccessCall;
import classes.ResourceItem;
import classes.ResourceType;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ConstantDirectionalEdgeValueTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;
import helpers.ColorHelpers;
import helpers.GraphVisualsHelper;

/**
 * Demonstrates jung support for drawing edge labels that can be positioned at
 * any point along the edge, and can be rotated to be parallel with the edge.
 * 
 * @author Tom Nelson
 * 
 *         This code has been modified by Omid Setayeshfar
 * 
 */
public class GraphPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6077157664507049647L;

	private boolean doPrint = true;

	public void setPrint(boolean print) {
		this.doPrint = print;
	}

	/**
	 * the graph
	 */
	Graph<ResourceItem, AccessCall> graph;

	/**
	 * the visual component and renderer for the graph
	 */
	public VisualizationViewer<ResourceItem, AccessCall> vv;

	/**
	 */
	VertexLabelRenderer vertexLabelRenderer;
	EdgeLabelRenderer edgeLabelRenderer;

	ScalingControl scaler = new CrossoverScalingControl();

	private ArrayList<AccessCall> Calls;
	private ArrayList<ResourceItem> Items;

	private boolean clickToReorder = false;

	public GraphPanel() {
		// TODO Auto-generated constructor stub
	}

	GraphZoomScrollPane panel;

	/**
	 * create an instance of a simple graph with controls to demo the label
	 * positioning features
	 * 
	 */
	@SuppressWarnings("serial")
	public GraphPanel(ArrayList<ResourceItem> Items, ArrayList<AccessCall> Calls) {
		this.Calls = Calls;
		this.Items = Items;
		// create a simple graph for the demo
		graph = new SparseMultigraph<ResourceItem, AccessCall>();

		Layout<ResourceItem, AccessCall> layout = new FRLayout<ResourceItem, AccessCall>(graph);
		java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
		vv = new VisualizationViewer<ResourceItem, AccessCall>(layout,
				new Dimension((int) tk.getScreenSize().getWidth() - 80, (int) tk.getScreenSize().getHeight() - 80));
		vv.setBackground(Color.white);

		vertexLabelRenderer = vv.getRenderContext().getVertexLabelRenderer();
		edgeLabelRenderer = vv.getRenderContext().getEdgeLabelRenderer();

		GraphVisualsHelper gvHelper = new GraphVisualsHelper();

		vv.getRenderContext().setEdgeLabelTransformer(gvHelper.getEdgeLabeler());
		vv.getRenderContext().setVertexLabelTransformer(gvHelper.getVertexLabler());
		vv.getRenderContext().setVertexFillPaintTransformer(gvHelper.getVerticeColorifier());
		vv.getRenderContext().setEdgeDrawPaintTransformer(gvHelper.getEdgeColorizer());
		vv.getRenderContext().setEdgeStrokeTransformer(gvHelper.getEdgeStroker(graph));
		vv.getRenderContext().setVertexShapeTransformer(gvHelper.getVertexShapper());
		vv.setVertexToolTipTransformer(gvHelper.gettoolTipper());

		// create a from to hold the graph
		panel = new GraphZoomScrollPane(vv);

		Container content = this;

		final DefaultModalGraphMouse<ResourceItem, AccessCall> graphMouse = new DefaultModalGraphMouse<ResourceItem, AccessCall>();
		vv.setGraphMouse(graphMouse);
		graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

		Box controls = Box.createHorizontalBox();

		JPanel modePanel = new JPanel(new GridLayout(2, 1));
		modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
		modePanel.add(graphMouse.getModeComboBox());

		controls.setSize(12, 200);
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == 3) {
					if (clickToReorder)
						graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
					else
						graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
					clickToReorder = !clickToReorder;

				}

			}
		});

		content.add(controls, BorderLayout.NORTH);
		content.add(panel, BorderLayout.AFTER_LAST_LINE);

	}

	public GraphPanel(Graph<ResourceItem, AccessCall> inp) {
		this(inp, true);
	}

	@SuppressWarnings("serial")
	public GraphPanel(Graph<ResourceItem, AccessCall> inp, boolean print ) {

		// create a simple graph for the demo
		graph = inp;

		if (print) {
			System.out.println("Number of Edges: " + graph.getEdgeCount());
			System.out.println("Number of Vertices: " + graph.getVertices().size());
		}
		Layout<ResourceItem, AccessCall> layout = new FRLayout<ResourceItem, AccessCall>(graph);
		java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
		vv = new VisualizationViewer<ResourceItem, AccessCall>(layout,
				new Dimension((int) tk.getScreenSize().getWidth() - 80, (int) tk.getScreenSize().getHeight() - 80));
		vv.setBackground(Color.white);

		vertexLabelRenderer = vv.getRenderContext().getVertexLabelRenderer();
		edgeLabelRenderer = vv.getRenderContext().getEdgeLabelRenderer();

		GraphVisualsHelper gvHelper = new GraphVisualsHelper();

		vv.getRenderContext().setEdgeLabelTransformer(gvHelper.getEdgeLabeler());
		vv.getRenderContext().setVertexLabelTransformer(gvHelper.getVertexLabler());
		vv.getRenderContext().setVertexFillPaintTransformer(gvHelper.getVerticeColorifier());
		vv.getRenderContext().setEdgeDrawPaintTransformer(gvHelper.getEdgeColorizer());
		vv.getRenderContext().setEdgeStrokeTransformer(gvHelper.getEdgeStroker(graph));
		vv.getRenderContext().setVertexShapeTransformer(gvHelper.getVertexShapper());
		vv.setVertexToolTipTransformer(gvHelper.gettoolTipper());
		vv.setEdgeToolTipTransformer(gvHelper.getEdgeToolTip());

		// create a from to hold the graph
		panel = new GraphZoomScrollPane(vv);

		Container content = this; // getContentPane();

		// content.add(panel);

		final DefaultModalGraphMouse<ResourceItem, AccessCall> graphMouse = new DefaultModalGraphMouse<ResourceItem, AccessCall>();
		vv.setGraphMouse(graphMouse);
		graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

		vv.addGraphMouseListener(new GraphMouseListener<ResourceItem>() {

			@Override
			public void graphReleased(ResourceItem arg0, MouseEvent arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void graphPressed(ResourceItem arg0, MouseEvent arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void graphClicked(ResourceItem arg0, MouseEvent arg1) {
				if (arg1.getButton() == 2) {
					if (clickToReorder)
						graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
					else
						graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
					clickToReorder = !clickToReorder;
				}

				if (arg0 != null) {
					ColorHelpers.PrintRed(graph.degree(arg0) + "\n");
					for (AccessCall x : graph.getOutEdges(arg0))
						System.out.println(x.toString());
				}
			}
		});

		Box controls = Box.createHorizontalBox();

		JPanel modePanel = new JPanel(new GridLayout(2, 1));
		modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
		modePanel.add(graphMouse.getModeComboBox());

		controls.setSize(12, 200);
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == 3) {
					if (clickToReorder)
						graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
					else
						graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
					clickToReorder = !clickToReorder;
				}
			}
		});

		content.add(controls, BorderLayout.NORTH);
		content.add(panel, BorderLayout.AFTER_LAST_LINE);
	}
}
