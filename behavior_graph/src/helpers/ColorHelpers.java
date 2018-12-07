package helpers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

public class ColorHelpers {
	private ArrayList<Color> selectedColors = new ArrayList<Color>();

	public Color GenerateNewColor() {
		Color ret = Color.green;

		Random randomizer = new Random();
		while ( true) {
			int red = randomizer.nextInt(256);
			int green = randomizer.nextInt(256);
			int blue = randomizer.nextInt(256);
			ret = new Color(red, green, blue);
			
			if(selectedColors.stream().anyMatch(
					x -> GetDeltaColor(x, red,green, blue) < 10))
				continue ; 
			else 
				break;
			
		} ;

		return ret;

	}

	private int GetDeltaColor(Color a,  int r , int g, int b) {
		double Dr = a.getRed() - r;
		Dr *= Dr;

		double Dg = a.getGreen() - g;
		Dg *= Dg;

		double Db = a.getBlue() - b;
		Db *= Db;

		return (int) Math.sqrt(Dr + Dg + Db);
	}
	
	public static void PrintBlue ( String inp ){
		if( !System.getProperty("os.name").toLowerCase().contains("windows"))
			System.out.print("\u001B[34m" +inp+ "\u001B[0m");
		else
			System.out.print(inp);
		
	}
	public static void PrintGreen ( String inp ){
		if( !System.getProperty("os.name").toLowerCase().contains("windows"))
			System.out.print("\u001B[32m" +inp+ "\u001B[0m");
		else
			System.out.print(inp);
		
	}
	public static void PrintRed ( String inp ){
		if( !System.getProperty("os.name").toLowerCase().contains("windows"))
			System.out.print("\u001B[31m" +inp+ "\u001B[0m");
		else
			System.out.print(inp);
		
	}
}
