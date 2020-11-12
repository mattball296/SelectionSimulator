import javax.swing.*;
import java.awt.*;
import java.lang.Object.*;
import java.awt.event.*;
import javax.swing.Timer;
import java.util.*;
import javax.swing.SwingUtilities;
import java.util.Collections;
import java.util.Iterator;
import java.io.*;

public class SelectionSimulatorGraphical{
	
	public boolean useTimer;
	
	public final int size;
	public final int Max= 700;
	public int squareSize; 
	
	public boolean speedMutating;
	public boolean sizeMutating;
	public boolean senseMutating;
	
	public int nTypes;
	public int[][] defaultThingAttributes;
	public String[] defaultThingStrings;
	public ThingArrayList[] ThingTypeArrays;
	
	public int defaultEnergy = 800;
	
	public JFrame Window = new JFrame();
	public JPanel MainPanel = new JPanel(new BorderLayout());
	public Environment e;
	
	public int Generation = 1;
	public int Time = 0;
	
	public ArrayList<int[]> possibleXY = new ArrayList<int[]>();
	
	public int[][] SpeedSizeLimits = new int[][]{{0,20},{0,20},{-1,9}};
	public final int ThingSpeed = 0;
	public final int ThingSize = 1;
	public final int ThingSense = 2;
	
	public int minSpeed = SpeedSizeLimits[ThingSpeed][1]-1;
	
	public int maxThings;
	public int numFood;
	public int generationToStartReducingFood = 50;
	public int foodReduced = 0;
	public int minFood = 140;
	
	public int eatMargin = 2;
	
	public Color[] eatenColorArray = new Color[]{Color.RED,Color.ORANGE, Color.YELLOW};
	
	public int numThings;
	public ThingArrayList ThingsArray = new ThingArrayList(-1);
	public ArrayList<Thing> thingBabies = new ArrayList<Thing>();
	
	boolean doneSomething = true;
	
	public int timeStep = 1;
	
	
	
	public Timer Simulation = new Timer(timeStep,new ActionListener(){
		public void actionPerformed(ActionEvent e){
			Simulate();
		}
	});
	
	public ArrayList<int[][][]> data = new ArrayList<int[][][]>();
	public ArrayList<Integer> foodData = new ArrayList<Integer>();
	
	/*
	thingStats = {{thingSpeed, thingSize, thingSense},...,}
	*/
	
	public SelectionSimulatorGraphical(boolean useTimer, int numFood, int size, boolean[] isMutating,int[][] thingStats, String[] thingStrings, int denseRow, int denseCol){
		// System.out.println(01);
		try{ 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());  // sets look and feel
		}
		catch (Exception e) { 
			System.err.println(e.getMessage()); 
		}	
		
		this.useTimer = useTimer;
		
		this.speedMutating = isMutating[ThingSpeed];
		this.sizeMutating = isMutating[ThingSize];
		this.senseMutating = isMutating[ThingSense];
		
		this.defaultThingAttributes = thingStats;
		this.defaultThingStrings = thingStrings;
		
		nTypes = defaultThingAttributes.length;
		this.ThingTypeArrays = new ThingArrayList[nTypes];
		this.numThings = 0;
		for(int i=0; i<nTypes; i++){
			numThings+=defaultThingAttributes[i][0];
		}
	
		for(int m=-1; m<=1; m++){
			for(int n=-1; n<=1; n++){
				if(m!=0||n!=0){
					possibleXY.add(new int[]{m,n});
				}
			}	
		} 
		
		this.size = size;
		this.maxThings = (int)Math.pow(size-2,2);
		this.squareSize = (int)(Max/size);
		this.e = new Environment(numFood,denseRow,denseCol);
		this.numFood = numFood;
		for(int i=0; i<nTypes;i++){
			ThingTypeArrays[i] = new ThingArrayList(i);
			int[] stats = defaultThingAttributes[i];
			for(int j=0; j<stats[0]; j++){
				Thing newThing = new Thing(new int[]{stats[1],stats[2],stats[3]},thingStrings[i],i);
				ThingsArray.add(newThing);
				ThingTypeArrays[i].add(newThing);
			}
		}		
		e.placeThings(ThingsArray);
		e.placeFood(numFood);
		
		MainPanel.add(e,BorderLayout.CENTER);

		JButton Next = new JButton("Next");
		Next.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				System.out.println("NEXT PRESSED");
				Simulation.start();
				// Simulate();
			}
		});
		MainPanel.add(Next,BorderLayout.PAGE_END);
		
		addToData();
		
		Window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		Window.add(MainPanel);
		Window.pack();
		Window.setVisible(true);
	}
	
	public static void writeData(String s,String title){
		try{
			FileWriter fw = new FileWriter(title+".data");
			fw.write(s);
			fw.close();
		}		
		catch(Exception e){
			System.err.println("An error has occured. See below for details");
			e.printStackTrace();
		}
	}
	
	public void SimulateGenerations(int n){
		// System.out.println(02);
		for(int Gen=1; Gen<=n; Gen++){
			if(useTimer){
				Simulation.start();
				while(Simulation.isRunning()){}
				doneSomething = true;
			}
			else{
				while(doneSomething&&ThingsArray.size()!=0){
					Simulate();
				}
				doneSomething = true;
			}
			addToData();
		}
	}
	
	/*
		data = {speed size sense numThings numFood}
	*/
	
	public int[][] getGenAttributes(int thingType){
		ThingArrayList T_Arr = ThingTypeArrays[thingType];
		int[][] I_Arr = new int[T_Arr.size()][3];
		for(int id=0; id<T_Arr.size(); id++){
			I_Arr[id] = T_Arr.get(id).getThingAttributes();
		}
		return I_Arr;
	}
	
	public void addToData(){
		foodData.add(numFood);
		int[][][] i_arr3 = new int[nTypes][][];
		for(int type=0; type<nTypes; type++){
			int[][] i_arr = getGenAttributes(type);
			i_arr3[type] = i_arr;
		}
		data.add(i_arr3);
	}
	
	public String thingDataToString(int thingType){
		String s = "";
		for(int[][][] i_arr3 : data){
			int[][] i_arr2 = i_arr3[thingType];
			s+=toString(i_arr2);
		}
		return s;
	}	
	
	public static String toString(int[][] i_arr2){
		String s = "";
		for(int a=0; a<i_arr2.length; a++){
			for(int b=0; b<i_arr2[0].length; b++){
				s+=i_arr2[a][b]+" ";
			}
			s+="\n";
		}
		return s;
	}
	
	public void Simulate(){
		// System.out.println(03);
		doneSomething = false;
		moveThings(e);
		Time++;
		if(!doneSomething){
			Simulation.stop();
			generationOver();
			Generation++;
		}
	}
	
	public void moveThings(Environment E){
		// System.out.println(05);
		Collections.shuffle(ThingsArray);
		for(Thing T : ThingsArray){
			T.move(E);
		}
	}
	
	public void generationOver(){
		// System.out.println(07);
		e.clear();
		Collections.shuffle(ThingsArray);
		Iterator I = ThingsArray.iterator();
		ThingArrayList nextGeneration = new ThingArrayList(-1);
		thingBabies.clear();
		while(I.hasNext()){
			Thing T = (Thing)I.next();
			if(T.isDead){
				I.remove();
				ThingTypeArrays[T.thingType].remove(T);
				continue;
			}
		}
		for(Thing T : ThingsArray){
			nextGeneration.add(T);
			if(T.foodEaten>=2){
				Thing t = new Thing(T);
				if(!t.isDead){
					thingBabies.add(t);
				}
			}
			T.reset();
		}
		for(Thing t : thingBabies){
			if(nextGeneration.size()<maxThings){
				nextGeneration.add(t);
				ThingTypeArrays[t.thingType].add(t);
			}
		}
		for(ThingArrayList T_Arr : ThingTypeArrays){
			System.out.println(T_Arr.avgString());
		}
		System.out.println(Generation);
		numThings = nextGeneration.size();
		ThingsArray = nextGeneration;
		e.placeThings(ThingsArray);
		if(Generation>=generationToStartReducingFood){
			int newFood = numFood-foodReduced;
			numFood = Math.max(newFood,minFood);
		}
		int a = 0;
		int b= 0;
		for(Thing T : ThingsArray){
			a++;
		}
		System.out.println(a);
		for(int R=0; R<size; R++){
			for(int C=0; C<size; C++){
				if(e.GridSquares[R][C].hasThing){
					b++;
				}
			}
		}
		System.out.println(b);
		System.out.println(a==b);
		e.placeFood(numFood);
		
	}
	
	public boolean spaceOccupied(int r, int c, ArrayList<Thing> T_Arr){
		// System.out.println(81);
		for(Thing T : T_Arr){
			if(T.row==r&&T.col==c){
				return true;
			}
		}
		return false;
	}
	
	// public String ThingTypeDataString(int thingType){
		// double[][] D_Arr = ThingTypeData(thingType);
		// String S = "";
		// for(double[] d_Arr : D_Arr){
			// for(double d : d_Arr){
				// S+=d+" ";
			// }
			// S+="\n";
		// }
		// return S;
	// }
	
	class Thing{
		public double Energy = defaultEnergy;
		public int foodEaten = 0;
		public int Speed;
		public int Size;
		public int Sense;
		public boolean isMovable = true;
		public String thingString;
		public int thingType;
		public boolean isDead = false;
		public int deathReason = 0;
		
		public int row;
		public int col;
	
		public Thing(int[] defaultThingAttributes, String str, int i){
			// System.out.println(10);
			thingType = i;
			thingString = str;
			Speed = defaultThingAttributes[ThingSpeed];
			Size = defaultThingAttributes[ThingSize];
			Sense = defaultThingAttributes[ThingSense];
			// System.out.println(Sense);
			boolean RCset = false;
			while(!RCset){
				// System.out.println(1);
				int r = (int)(size*Math.random());
				int c = (int)(size*Math.random());
				if(!spaceOccupied(r,c,ThingsArray)){
					this.setRC(r,c);
					RCset = true;
				}
			}
		}
		
		public Thing(Thing t){
			// System.out.println(11);
			this.thingString = t.thingString;
			this.thingType = t.thingType;
			if(speedMutating){
				this.Speed = Mutated(t.Speed,ThingSpeed);
			}
			else{
				this.Speed = t.Speed;
			}
			if(sizeMutating){
				this.Size = Mutated(t.Size,ThingSize);
			}
			else{
				this.Size = t.Size;
			}
			if(senseMutating){
				this.Sense = Mutated(t.Sense,ThingSense);
			}
			else{
				this.Sense = t.Sense;
			}
			// System.out.println(this.Sense);
			// System.out.println("new thing born");
			boolean placedChild = false;
			int id = 0;
			while(!placedChild&&id<8){
				// System.out.println(2);
				Collections.shuffle(possibleXY);
				int x = possibleXY.get(id)[0];
				int y = possibleXY.get(id)[1];
				id++;
				if((0<=t.row+x&&t.row+x<=size-1)&&(0<=t.col+y&&t.col+y<=size-1)&&!spaceOccupied(t.row+x,t.col+y,thingBabies)&&!spaceOccupied(t.row+x,t.col+y,ThingsArray)){
					this.setRC(t.row+x,t.col+y);
					placedChild = true;
				}
			}
			if(!placedChild){
				this.isDead = true;
			}
		}
		
		public int Mutated(int s,int type){
			// System.out.println(12);
			double chance = Math.random();
			if(chance<0.1){
				int New = s-1;
				if(New>SpeedSizeLimits[type][0]){
					return New;
				}
				else{
					return s;
				}
			}
			else if(chance>0.9){
				int New = s+1;
				if(New<SpeedSizeLimits[type][1]){
					return New;
				}
				else{
					return s;
				}
			}
			else return s;
		}
		
		public int[] getThingAttributes(){
			return new int[]{this.Speed,this.Size,this.Sense,Generation};
		}

		public String getThingAttributesString(){
			String str = "";
			for(int i : this.getThingAttributes()){
				str+=i+" ";
			}
			return str;
		}
		
		public double energyUsed(){
			// System.out.println(13);
			double d = (double)(Size)*(Size)*(Size)*(Sense+1)*(Sense+1)*(double)1/((Speed)*(Speed));//(double)(Sense+1)*(Sense+1)*(double)(1/(Math.pow((Speed),2)));
			// System.out.println(d);
			return d;
		}
		
		public void setRC(int r, int c){
			// System.out.println(14);
			this.row = r;
			this.col = c;
		}
		
		public void move(Environment e){
			// System.out.println(15);
			boolean isMoved = false;
			if(isDead){
				return;
			}
			if(!isMovable){
				return;
			}
			if(Time%(this.Speed)!=0){
				doneSomething = true;
				return;
			}
			int idx = 0;
			ArrayList<Integer> impossibleIDX = new ArrayList<Integer>();
			Collections.shuffle(possibleXY);
			int r = 0;
			while(!isMoved){
				r++;
				if(impossibleIDX.size()==8){
					return;
				}
				if(!impossibleIDX.contains(idx%8)){
					int x = possibleXY.get(idx%8)[0];
					int y = possibleXY.get(idx%8)[1];
					if(idx<Sense){
						try{
							if(e.GridSquares[row+x][col+y].hasFood){
								isMoved = true;
								// System.out.println("HASFOOD");
								moveTo(x,y,e);
								return;
							}
							else if(e.GridSquares[row+x][col+y].hasThing){
								Thing T = e.GridSquares[row+x][col+y].T;
								if(this.Size-T.Size>=eatMargin){
									moveTo(x,y,e);
									return;
								}
								else{
									impossibleIDX.add(idx);
									idx++;
									continue;
								}
							}
							idx++;
							continue;
						}
						catch(ArrayIndexOutOfBoundsException aioobe){
							impossibleIDX.add(idx);
							idx++;
							continue;
						}
					}
					try{
						moveTo(x,y,e);
						isMoved = true;
					}
					catch(Exception EX){
						impossibleIDX.add(idx);
						idx++;
					}
				}
				else{
					idx++;
				}
			}
		}
		
		public void eat(Thing T){
			T.isDead = true;
			T.deathReason = 1;
			if(T.foodEaten==0){
				this.foodEaten++;
			}
			else{
				this.foodEaten+=2;
			}
		}
		
		public void runAway() throws RuntimeException{
			Collections.shuffle(possibleXY);
			for(int ix=0; ix<this.Sense; ix++){
				if(this.Energy<=0){
					throw new RuntimeException("");
				}
				int x = possibleXY.get(ix)[0];
				int y = possibleXY.get(ix)[1];
				this.Energy-=this.energyUsed();
				if(this.isOutOfBounds(x,y,e)||e.GridSquares[this.row+x][this.col+y].hasThing){
					continue;
				}
				else{
					e.GridSquares[this.row][this.col].removeThing();
					e.GridSquares[this.row][this.col].removeFood();
					e.GridSquares[this.row+x][this.col+y].addThing(this);
					this.row+=x;
					this.col+=y;
					return;
				}
			}
			throw new RuntimeException("");
		}
		
		public void moveTo(int x, int y, Environment e) throws RuntimeException{
			this.Energy-=this.energyUsed();
			// if(this.thingString.equals("C")){
				// System.out.println("e used = "+this.energyUsed());
				// System.out.println("e after = "+this.Energy);
			// }
			if(e.GridSquares[row+x][col+y].hasThing){
				Thing T = e.GridSquares[row+x][col+y].T;
				if(this.Size-T.Size>=eatMargin){
					try{
						T.runAway();
					}
					catch(RuntimeException rte){
						e.GridSquares[row+x][col+y].removeThing();
						this.eat(T);
					}
				}
				else if(T.Size-this.Size>=eatMargin){
					T.eat(this);
					e.GridSquares[row][col].removeThing();
					return;
				}
				else{
					// System.out.println("THINGHERE");
					throw new RuntimeException("");
				}
			}
			if(isOutOfBounds(x,y,e)){
				// System.out.println("OUTOFBOUNDS");
				throw new RuntimeException("");
			}
			doneSomething = true;
			if(e.GridSquares[row+x][col+y].hasFood){
				this.foodEaten++;
				e.GridSquares[row+x][col+y].removeFood();
			}
			e.GridSquares[row+x][col+y].addThing(this);
			e.GridSquares[row][col].removeThing();
			row+=x;
			col+=y;
			if(this.Energy<=0){
				if(foodEaten==0){
					deathReason = 2;
					isDead = true;
				}
				this.isMovable=false;
			}
			if(this.foodEaten>=2){
				this.isMovable=false;
			}
		}

		public boolean containedIn(int[] xy,ArrayList<int[]> i_Arr){
			// System.out.println(17);
			for(int[] i : i_Arr){
				if(i[0]==xy[0]&&i[1]==xy[1]){
					return true;
				}
			}
			return false;
		}
		
		public void reset(){
			// System.out.println(18);
			foodEaten = 0;
			Energy = defaultEnergy;
			isMovable = true;
			isDead = false;
		}
		
		public boolean isOutOfBounds(int x, int y, Environment e){
			// System.out.println(19);
			try{
				e.GridSquares[row+x][col+y].doNothing();
				return false;
			}
			catch(ArrayIndexOutOfBoundsException aioobe){
				return true;
			}
		}
	}	
	
	class EnvironmentSquare extends JTextField{
		
		public Thing T;
		public boolean hasFood;
		public boolean hasThing = false;
		
		
		public EnvironmentSquare(){
			super();
			// System.out.println(20);
			this.setFont(new Font("",Font.BOLD,squareSize/2));
			this.setPreferredSize(new Dimension(squareSize,squareSize));
			this.setEditable(false);
			this.setHorizontalAlignment(SwingConstants.CENTER);
			this.setFocusable(true);
			this.addMouseListener(new MouseAdapter(){
				public void mousePressed(MouseEvent m){
					if(hasThing){
						System.out.println(T.getThingAttributesString());
					}
					// System.out.println("GGGGG");
				}
			});
		}
		
		public void addFood(){
			// System.out.println(21);
			this.setBackground(Color.GREEN);
			this.hasFood = true;
			this.setText("F");
		}
		
		public void removeFood(){
			// System.out.println(22);
			this.setBackground(null);
			this.hasFood = false;
			this.setText("");
		}
		
		public void addThing(Thing t){
			// System.out.println(23);
			this.setBackground(Color.GRAY);
			this.hasFood = false;
			this.T = t;
			this.hasThing = true;
			this.setText(t.thingString);
			this.setFont(generateFont(t));
			this.setForeground(generateColor(t));
		}
		
		public void removeThing(){
			// System.out.println(24);
			this.T = null;
			this.setBackground(null);
			this.hasThing = false;
			this.setText("");
			this.setFont(new Font("",Font.BOLD,squareSize/2));
			this.setForeground(null);
		}
		
		public void doNothing(){}
		
		public Font generateFont(Thing t){
			int Range = SpeedSizeLimits[ThingSize][1] - SpeedSizeLimits[ThingSize][0];
			double g = (double)squareSize/Range;
			return new Font("",Font.BOLD,(int)(t.Size*g));
		}
		
		public Color generateColor(Thing t){
			// System.out.println(25);
			// System.out.println(12);
			int Range = SpeedSizeLimits[ThingSpeed][1] - SpeedSizeLimits[ThingSpeed][0];
			int gap = (int)((255*3)/Range);
			int c = 765-t.Speed*gap;
			if(c<=255){
				return new Color(c,0,0);
			}
			else if(255<c&&c<=510){
				return new Color(255,c%255,0);
			}
			else if(510<c&&c<=765){
				return new Color(255,255,c%255);
			}
			return null;
		}
	}
	
	class Environment extends JPanel{
		
		public EnvironmentSquare[][] GridSquares = new EnvironmentSquare[size][size];
		public int denseRow;
		public int denseCol;
		public int numFood;		
		
		public Environment(int F,int R, int C){
			super();
			denseRow = R;
			denseCol = C;
			// System.out.println(26);
			this.setLayout(new GridLayout(size,size,0,0));
			for(int r=0; r<size; r++){
				for(int c=0; c<size; c++){
					GridSquares[r][c] = new EnvironmentSquare();
					this.add(GridSquares[r][c]);
				}
			}
			this.numFood = F;
			this.setFocusable(true);
		}
		
		public void placeFood(int numFood){
			System.out.println(27);
			int f = 0;
			while(f<Math.min(numFood,(Math.pow(size,2)-numThings))){
				// System.out.println(5);
				int r,c;
				if(denseRow==-1||denseCol==-1){
					r = (int)(size*Math.random());
					c = (int)(size*Math.random());
				}
				else{			
					Random R = new Random();
					r = (int)(R.nextGaussian()*size/2+denseRow);
					c = (int)(R.nextGaussian()*size/2+denseCol);
				}
				try{
					if(!GridSquares[r][c].hasFood&&!GridSquares[r][c].hasThing){
						GridSquares[r][c].addFood();
						f++;
					}
				}
				catch(ArrayIndexOutOfBoundsException aioobe){
					continue;
				}
			}
		}
		
		public void placeThings(ArrayList<Thing> T_Arr){
			// System.out.println(28);
			for(Thing T : T_Arr){
				if(this.GridSquares[T.row][T.col].hasThing){
					System.out.println("ERR");
					System.out.println(T.row+" , "+T.col);
				}
				this.GridSquares[T.row][T.col].addThing(T);
			}
		}
		
		public void clear(){
			// System.out.println(28);
			for(int r=0; r<size; r++){
				for(int c=0; c<size; c++){
					GridSquares[r][c].removeFood();
					GridSquares[r][c].removeThing();
				}
			}
		}
	}
	
	class ThingArrayList extends ArrayList<Thing>{

		int Type;
		String[] Labels = new String[]{" avgSpeed = "," avgSize = "," avgSense = "};
		
		public ThingArrayList(int u){
			super();
			this.Type = u;
		}
		
		public double avg(int type){
			int sum = 0;
			for(Thing T: this){
				switch(type){
					case ThingSpeed:
						sum+=T.Speed;
						break;
					case ThingSize:
						sum+=T.Size;
						break;
					case ThingSense:
						sum+=T.Sense;
						break;
				}
			}
			try{
				return (double)sum/this.size();
			}
			catch(ArithmeticException ae){
				return 0.0;
			}
		}
		
		public String avgString(){
			String s = "";
			if(Type!=-1){
				s+=defaultThingStrings[Type];
			}
			else{
				s+="All";
			}
			for(int i=0; i<3; i++){
				s+=Labels[i]+this.avg(i);
			}
			s+=" "+this.size();
			return s;
		}
		
		public String genData(){
			String s = "";
			for(int i=0; i<3; i++){
				s+=" "+this.avg(i);
			}
			s+=" "+this.size();
			return s;
		}
	}
	
	public static void main(String[] args){
		boolean[] isMutating = new boolean[]{true,true,true};
		int[][] Things = new int[][]{{10,10,10,04},
									 {10,05,05,05},
									 {1,15,15,06}};
		String[] thingStrings = new String[]{"A","B","C",};
		SelectionSimulatorGraphical s = new SelectionSimulatorGraphical(false,500,50,isMutating,Things,thingStrings,0,0);
		s.SimulateGenerations(1000);
		
		// writeData(s.thingDataToString(0),"A");
		
		// String A_data = s.ThingTypeDataString(0);
		// System.out.println(A_data);
		// s.writeData(s.ThingTypeDataString(0),"A");
		// s.writeData(s.ThingTypeDataString(1),"B");
		// s.writeData(s.ThingTypeDataString(2),"C");
		// double[][]
		// writeData(dataStrings);
		// for(Thing T : s.ThingsArray){
			// System.out.println(T.thingString);
		// }
		// for(Thing T : s.ThingsArray){
			// System.out.println(T.Speed+" "+T.Sense);
		// }
		////System.out.println(8%101);
		// int[] PopSize = new int[11];
		// while(s.Generation<11){
			// s.Simulation.start();
			// PopSize[s.Generation-1] = s.numThings;
			////System.out.println(s.Generation);
		// }
		// for(int i=1; i<=PopSize.length;i++){
			////System.out.println("Population in the "+i+"th generation = "+PopSize[i-1]);
		// }
	}

}