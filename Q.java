/*

   Name: Adam Johnson
   Course: 421 - AI
   Assignment: 5 Q learning
   Due: 11/21/2016

*/

import java.io.*;
import java.util.*;

public class Q{
    public static void main(String[] args)throws InterruptedException{
    
        //first delete all the old result files
        int count = 0;
        boolean running = true;
        do{
            File del = new File("output/results" + count + ".txt");
            if(del.exists()){
                del.delete();
                //System.out.println("Deleted : " + del.getName());
                
            } else {
                running = false;
                //System.out.println("FFF");
            }
            count++;
        } while (running);
        File f;
        try{
            //take in the data file from args[0]
             f = new File(args[0]);
             //after reading in the file create the objects
            Agent a = processFile(f);
            //a.map().printMap(new MyPoint(0,0));
          
            //Movement is implemented in this run function
            //The run function handles movement as well as a set number of 
            //epochs currently the only way to change epochs is by changing the
            //hard coded number
            a.run();
            
        } catch (FileNotFoundException e){
            System.out.println("The file does not exists");
        }
        
        
        
        
    }
    
    public static Agent processFile(File f) throws FileNotFoundException{
        Scanner fc = new Scanner(f);
        //put the control variables into a string
        String variables = fc.nextLine();
        //put the escape location into a string
        String escape = fc.nextLine();
        //put the pony variables into a string
        String ponies = fc.nextLine();
        //put the series of obstacles into a string
        String obstacles = fc.nextLine();
        //put the series of trolls into a string
        String trolls = fc.nextLine();
        //open the scanner on variables
        fc = new Scanner(variables);
        
        //go through the variables
        int n = fc.nextInt();
        int nTrolls = fc.nextInt();
        int nPonies = fc.nextInt();
        
        
        //go through the escape 
        fc = new Scanner(escape);
        MyPoint escapeLoc = new MyPoint(fc.nextInt(), fc.nextInt());
        
        //go through the ponies
        fc = new Scanner(ponies);
        ArrayList<Pony> ponyLoc = new ArrayList<>();
        for(int i = 0; i < nPonies; i++){
            int x = fc.nextInt();
            int y = fc.nextInt();
            ponyLoc.add( new Pony(new MyPoint(x, y)));
            
        }
        
        //go through the obstacles
        fc = new Scanner(obstacles);
        ArrayList<Obstacle> obstacleLoc = new ArrayList<>();
        while(fc.hasNext()){
            int x = fc.nextInt();
            int y = fc.nextInt();
            MyPoint temp = new MyPoint(x,y);
            obstacleLoc.add( new Obstacle(temp));
        }
        
        //go through the trolls
        fc = new Scanner(trolls);
        ArrayList<Troll> trollLoc = new ArrayList<>();
        for(int i = 0; i < nTrolls; i++){
            int x = fc.nextInt();
            int y = fc.nextInt();
            MyPoint temp = new MyPoint(x,y);
            Troll newTroll = new Troll(temp);
            trollLoc.add( newTroll);
        }
        //gather the input parameters
        fc = new Scanner(System.in);
        
        
        
        //return a new worldmap
        WorldMap m =  new WorldMap(trollLoc, ponyLoc, obstacleLoc, n, escapeLoc,
        getAlpha(fc), getGamma(fc), getQ(fc));
        
        return new Agent(m);
    }
    
    private static double getAlpha(Scanner sc){
        System.out.print("Enter alpha: ");
        while(!sc.hasNextDouble()){
            System.out.print("Enter alpha: ");
            sc.next();
        }
        return sc.nextDouble();
    }
    private static double getGamma(Scanner sc){
        System.out.print("Enter gamma: ");
        while(!sc.hasNextDouble()){
            System.out.print("Enter gamma: ");
            sc.next();
        }
        return sc.nextDouble();
    }
    private static double getQ(Scanner sc){
        System.out.print("Enter q: ");
        while(!sc.hasNextDouble()){
            System.out.print("Enter q: ");
            sc.next();
        }
        return sc.nextDouble();
    }
    
}




class Troll{
    MyPoint location;
    
    public Troll(MyPoint p){
        location = p;
    }
    
    public boolean equals(Object other){
        if(other instanceof Troll){
            Troll i = (Troll) other;
            if((i.location.equals(location))){
                return true;
            }
        }
        return false;
    }
    public String toString(){
        return "T";
    }
}

class Pony {
    MyPoint location;
    public Pony(MyPoint p){
        location = p;
    }
    public boolean equals(Object other){
        if(other instanceof Pony){
            Pony i = (Pony) other;
            if((i.location.equals(location))){
                return true;
            }
        }
        return false;
    }
    public String toString(){
        return "P";
    }
}

class Obstacle {
    MyPoint location;
    public Obstacle(MyPoint p){
        location = p;
    }
    
    public boolean equals(Object other){
        if(other instanceof Obstacle){
            Obstacle i = (Obstacle) other;
            if((i.location.equals(location))){
                return true;
            }
        }
        return false;
    }
    public String toString(){
        return "#";
    }

}

class WorldMap{
    public static int n = 0;
    private ArrayList<Troll> trolls;
    private ArrayList<Pony> ponies;
    private ArrayList<Obstacle> obstacles;
    private int size;
    private MyPoint escape;
    private State[][] map;
    private double alpha;
    private double gamma;
    private double q;
    
    public WorldMap(ArrayList<Troll> t, ArrayList<Pony> p, ArrayList<Obstacle> a, int s, MyPoint esc ,double al, double g, double q){
        trolls = t;
        ponies = p;
        obstacles = a;
        size = s;
        map = new State[size][size];
        escape = esc;
        alpha = al;
        gamma = g; 
        this.q = q;
        initMap();
    }
    
    public State getStart(){
        Random rand = new Random();
        int x = rand.nextInt(size);
        int y = rand.nextInt(size);
        boolean run = false;
        for(Obstacle o : obstacles){
            if(o.location.equals(new MyPoint(x, y)))
                run = true;
        }
        while(run){
            x = rand.nextInt(size);
            y = rand.nextInt(size);
            run = false;
            for(Obstacle o : obstacles){
                if(o.location.equals(new MyPoint(x, y)))
                    run = true;
            }
        }
        map[x][y].setTaken();
        return map[x][y];
    }
    
    private void initMap(){

        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){

                map[i][j] = new State(new MyPoint(i, j));
            }
        }
        
        Action nw = null;
        Action n = null;
        Action ne = null;
        Action e = null;
        Action se = null;
        Action s = null;
        Action sw = null;
        Action w = null;
        
        //go through each location and assign actions to the states
        for(int i = 0; i < size; i++){
        
            for(int j = 0; j < size; j++){
                MyPoint mp = new MyPoint(i,j);
                //if we go out of bounds we self reference action
                if(i - 1 < 0 || j + 1 >= size){
                     
                    //nw = new Action(map[i][j]);
                } else {
                    Boolean b = true;
                    MyPoint op = new MyPoint( i - 1, j + 1);
                    //check if the x,y pair goes to an obstacle
                    for(Obstacle it : obstacles){
                        //if we find an obstacle at the point
                        
                        if(it.equals(new Obstacle(op))){
                            b = false;
                        }
                    }
                    if(b){
                        nw = new Action(map[i-1][j+1], alpha, gamma, q);
                    } else {
                        //nw = new Action(map[i][j]);
                    }
                }
                
                //setup north
                if(j + 1 >= size){
                     
                    //n = new Action(map[i][j]);
                } else {
                    Boolean b = true;
                    MyPoint op = new MyPoint( i , j + 1);
                    //check if the x,y pair goes to an obstacle
                    for(Obstacle it : obstacles){
                        //if we find an obstacle at the point

                        if(it.equals(new Obstacle(op))){

                            b = false;
                        }
                    }
                    
                    if(b){
                        
                        n = new Action(map[i][j+1], alpha, gamma, q);
                    } else {

                        //n = new Action(map[i][j]);
                    }
                }
                
                //setup northeast
                if(i + 1 >= size || j + 1 >= size){
                     
                   // ne = new Action(map[i][j]);
                } else {
                    Boolean b = true;
                    MyPoint op = new MyPoint( i + 1, j + 1);
                    //check if the x,y pair goes to an obstacle
                    for(Obstacle it : obstacles){
                        //if we find an obstacle at the point
                        
                        if(it.equals(new Obstacle(op))){
                            b = false;
                        }
                    }
                    if(b){
                        ne = new Action(map[i+1][j+1], alpha, gamma, q);
                    } else {
                       // ne = new Action(map[i][j]);
                    }
                }
                
                //setup east
                if(i + 1 >= size){
                     
                    //e = new Action(map[i][j]);
                } else {
                    Boolean b = true;
                    MyPoint op = new MyPoint( i + 1 , j);
                    //check if the x,y pair goes to an obstacle
                    for(Obstacle it : obstacles){
                        //if we find an obstacle at the point
                        
                        if(it.equals(new Obstacle(op))){
                            b = false;
                        }
                    }
                    if(b){
                        e = new Action(map[i+1][j], alpha, gamma, q);
                    } else {
                        //e = new Action(map[i][j]);
                    }
                }
                
                //setup southeast
                if(i + 1 >= size || j - 1 < 0){
                     
                    //se = new Action(map[i][j]);
                } else {
                    Boolean b = true;
                    MyPoint op = new MyPoint( i + 1, j - 1);
                    //check if the x,y pair goes to an obstacle
                    for(Obstacle it : obstacles){
                        //if we find an obstacle at the point
                        
                        if(it.equals(new Obstacle(op))){
                            b = false;
                        }
                    }
                    if(b){
                        se = new Action(map[i+1][j-1], alpha, gamma, q);
                    } else {
                        //se = new Action(map[i][j]);
                    }
                }
                
                //setup south
                if(j - 1 < 0){
                     
                    //s = new Action(map[i][j]);
                } else {
                    Boolean b = true;
                    MyPoint op = new MyPoint( i , j - 1);
                    //check if the x,y pair goes to an obstacle
                    for(Obstacle it : obstacles){
                        //if we find an obstacle at the point
                        
                        if(it.equals(new Obstacle(op))){
                            b = false;
                        }
                    }
                    if(b){
                        s = new Action(map[i][j-1], alpha, gamma, q);
                    } else {
                        //s = new Action(map[i][j]);
                    }
                }
                
                //setup southwest
                if(i - 1 < 0 || j -1 < 0){
                     
                    //sw = new Action(map[i][j]);
                } else {
                    Boolean b = true;
                    MyPoint op = new MyPoint( i - 1, j - 1);
                    //check if the x,y pair goes to an obstacle
                    for(Obstacle it : obstacles){
                        //if we find an obstacle at the point
                        
                        if(it.equals(new Obstacle(op))){
                            b = false;
                        }
                    }
                    if(b){
                        sw = new Action(map[i-1][j-1], alpha, gamma, q);
                    } else {
                        //sw = new Action(map[i][j]);
                    }
                }
                
                //setup west
                if(i - 1 < 0){
                     
                    //w = new Action(map[i][j]);
                } else {
                    Boolean b = true;
                    MyPoint op = new MyPoint( i - 1, j );
                    //check if the x,y pair goes to an obstacle
                    for(Obstacle it : obstacles){
                        //if we find an obstacle at the point
                        
                        if(it.equals(new Obstacle(op))){
                            b = false;
                        }
                    }
                    if(b){
                        w = new Action(map[i-1][j], alpha, gamma, q);
                    } else {
                       // w = new Action(map[i][j]);
                    }
                }
                
                //at this point all directions are setup
                ArrayList<Action> actions = new ArrayList<>();
                if(nw != null)
                    actions.add(nw);
                if(n != null)
                    actions.add(n);
                if(ne != null)
                    actions.add(ne);
                if(e != null)
                    actions.add(e);
                if(se != null)
                    actions.add(se);
                if(s != null)
                    actions.add(s);
                if(sw != null)    
                    actions.add(sw);
                if(w != null)
                    actions.add(w);
                map[i][j].initActions(actions);
                map[i][j].updateR();
                
                //reset the actions;
                nw = null;
                n = null;
                ne = null;
                e = null;
                se = null;
                s = null;
                sw = null;
                w = null;

            }
        }
        
        resetContains();
    }
    
    public void resetContains(){
        //set all taken to false
        for(int i = 0; i < size; i ++){
            for(int j = 0; j < size; j++){
                map[j][i].resetTaken();
            }
        }
        //go through each state and flip their contains
        for(Pony t : ponies){
            int x = t.location.x;
            int y = t.location.y;
            map[x][y].setPony(true);
        }
        
        for(Troll t : trolls){
            int x = t.location.x;
            int y = t.location.y;
            map[x][y].setTroll(true);
        }
        
        for(Obstacle t : obstacles){
            int x = t.location.x;
            int y = t.location.y;
            if(x != -1)
                map[x][y].setObstacle(true);
        }
        
        //set the escape
        map[escape.x][escape.y].setEscape(true);
        
    }
    
    public State getState(Action a){
        a.result.setTaken();
        return a.result;
    }

    public double getReward(State s){
        //find out what we landed on
        MyPoint loc = s.pointData();
        
        //check this point against trolls
        if(s.troll){
            return -15;
        }
        
        //check this point against ponies
        if(s.pony){
            s.setPony(false);
            return 10;
        }
        
        if(escape.equals(loc)){
            return 15;
        }
        
        
        return 2;
    }
    
    public State[][] getStateMap(){
        return map;
    }
    
    public void printMap(MyPoint agent){
        try{
            File writeTo = new File("output/results" + n + ".txt");
            
            //create the file if it does not exist
            if(!writeTo.exists()){
                writeTo.createNewFile();
            }
            
            //Here true is to append the content to file
            FileWriter fw = new FileWriter(writeTo,true);
            
            //BufferedWriter writer give better performance
            BufferedWriter bw = new BufferedWriter(fw);
            for(int i = size - 1; i >= 0; i--)
            bw.write("#");
            bw.write("##\n");
            for(int i = size - 1; i >= 0; i--){
                bw.write("#");
                for(int j = 0; j < size; j++){
                    if(!agent.equals(map[j][i].actualLocation)){
                        bw.write(getSymbol(map[j][i]));
                    } else {
                        bw.write("A");
                    }
                }
                bw.write("#\n");
            }
            bw.write("#");
            for(int i = 0; i <= size - 1; i++)
                bw.write("#");
            bw.write("#\n");
            bw.write("\n");
            
            //Closing BufferedWriter Stream
            bw.close();
        } catch (FileNotFoundException e){
            System.exit(1);
        } catch (IOException e2){
            System.exit(1);
        }
        
       
    }
    
    
    public void printEnd(double reward){
        try{
            File writeTo = new File("output/results" + n + ".txt");
            
            //create the file if it does not exist
            if(!writeTo.exists()){
                writeTo.createNewFile();
            }
            
            //Here true is to append the content to file
            FileWriter fw = new FileWriter(writeTo,true);
            
            //BufferedWriter writer give better performance
            BufferedWriter bw = new BufferedWriter(fw);
            //print out the total Reward
            bw.write("Total reward:" + reward);
            bw.write("\n Pony rescue ratio: " + (ponies.size() - notRescued() ) + "/" + ponies.size());
            
            //Closing BufferedWriter Stream
            bw.close();
            n++;
        } catch (FileNotFoundException e){
            System.exit(1);
        } catch (IOException e2){
            System.exit(1);
        }
    }
    
    private String getSymbol(State s){
        return s.symbol;
    }
    
    private int notRescued(){
        int r = 0;
        for(int i = 0; i <size; i ++){
            for(int j = 0; j <size; j ++){
                if(map[j][i].pony)
                    r++;
            }
        }
        return r;
    }
    
}
class State{
    ArrayList<Action> actions;
    MyPoint actualLocation;
    String symbol;
    boolean obstacle;
    boolean troll;
    boolean pony;
    boolean escape;
    boolean taken;
    double r;
    
    public State(MyPoint p){
        actualLocation = p;
        initialSetup();
    }
    
    private void initialSetup(){
        symbol = "-";
        troll = false;
        pony = false;
        escape = false;
        taken = false;
    }
    
    public void initActions(ArrayList<Action> a){
        actions = a;
    }
    
    public MyPoint pointData(){
        return actualLocation;
    }
    
    public String toString(){
        return actualLocation.toString();
    }
    
    public void setTroll(boolean b){
        troll = b;
        setSymbol();
    }
    
    public void setPony(boolean b){
        pony = b;
        setSymbol();
    }
           
    public void updateR(){
        double highest = actions.get(0).q;
        //go through each of the actions and determine the highest q
        for(Action a : actions){
            if(highest < a.q){
                highest = a.q;
            }
        }
        r = highest;
    }

    
    public void setObstacle(boolean b){
        obstacle = b;
        setSymbol();
    }
    
    public void setEscape(boolean b){
        escape = b;
        setSymbol();
    }
    
    public void setTaken(){
        taken = true;
        setSymbol();
    }
    
    public void resetTaken(){
        taken = false;
        setSymbol();
    }
    
    
    private void setSymbol(){
        //always want obstacle to be set first
        if(obstacle){
            symbol = "#";
            return;
        }
        
        //then we want troll on top no matter what
        if(troll){
            symbol = "T";
            return;
        }
        
        //then pony
        if(pony){
            symbol = "P";
            return;
        }
        
        //then taken
        if(taken){
            symbol = "X";
            return;
        }
        
        //then escape
        if(escape){
            symbol = "E";
            return;
        }
        
        //if nothing is here
        symbol = "-";
    }
    
}

class Action{
    State result;
    double q;
    double alpha;
    double gamma;
    
    public Action(State r){
        this(r, .5, .5, 0);
    }
    
    public Action(State r, double a, double g, double q){
        result = r;
        alpha = a;
        gamma = g;
        q = 0;
    }
    
    public String toString(){
        return result.pointData().toString();
    }
    
    
    public void update(Double d){
        q = q + alpha * ( d + (gamma * result.r) - q );
    }
}

class Agent {
    WorldMap wm;
    
    public Agent(WorldMap a){
        wm = a;
    }
    public void run() throws InterruptedException{
        //get start space
        int run = 0;
        int count = 0;
        double highest = 0;
        while(count != 100){
            //move(wm.getStart());
            State start = wm.getStart();
            double t = 0;           
            if(!start.troll)
                t = oneEpoch(start);
            else{
                wm.printMap(start.actualLocation);
                t = -15;
            }
            if(t > highest){
                highest = t;
                run = count;
            }
                
            count++;
            wm.printEnd(t);
            wm.resetContains();
            
            
        }
         
        System.out.println("The highest reward was : " + highest + " from run " +
                            run);
        
    }
  
    
    private double oneEpoch(State start) throws InterruptedException{
        //print the map before hand
        wm.printMap(start.actualLocation);
        //Thread.sleep(1000);
        //System.out.println("we got the start");
        double sum = 0;
        boolean running = true;
        while(running){
            Action picked = null;
            double denom = 0;
            //evaluate each action from the start
            for(Action a : start.actions){
                //get the total for each action
                denom += Math.exp(a.q/10000);
            }
            
            //determine if we explore or if we choose highest q
            if(Math.random() >= .9){
                //randomly pick an action
                double summ = 0;
                Random r = new Random();
                double p = r.nextDouble() * denom;
                boolean notPicked = true;
                for(Action a : start.actions){
                    //get the total for each action
                    summ += Math.exp(a.q/10000);
                    if(summ >= p && notPicked){
                        picked = a;
                        notPicked = false;
                    }
                }
            } else {
                Action highest = null;
                for(Action a : start.actions){
                    if(highest == null){
                        highest = a;
                        
                    } else if(a.q > highest.q){
                        highest = a;
                    }
                }
                picked = highest;
            }
            
            State temp = start;
            
            //send action to get a new state back to the mothership
            start = wm.getState(picked);
            
            
            //receive reward
            double reward = wm.getReward(start);
            //update the action taken with the reward value 
            picked.update(reward);
            sum+= reward;
            
            //after updating the Q value on the action go back to the previous
            //state and update the r value
            temp.updateR();
            //check if reward is -15 or 15
            if(reward == 15){
                //System.out.println("FOUND THE ESCAPE");
                running = false;
            } else if(reward == -15){
                //System.out.println("KILLED BY DANNY DIVITO");
                running = false;
            }
            //print out the map at the new start
            
            //Thread.sleep(1000);


        }
        //System.out.println(sum);
        wm.printMap(start.actualLocation);;
        return sum;
    }
    
    
    //making this for now
    public boolean Equals(Object other){
        return false;
    }
    
    public WorldMap map(){
        return wm;
    }
    
    //testing functions 
      
    private void move(State start){
        //print the map
        wm.printMap(start.actualLocation);
        boolean running = true;
        Scanner sc = new Scanner(System.in);
        int d;
        while(running){
            System.out.println("To move NW type 0");
            System.out.println("To move N type 1");
            System.out.println("To move NE type 2");
            System.out.println("To move E type 3");
            System.out.println("To move SE type 4");
            System.out.println("To move S type 5");
            System.out.println("To move SW type 6");
            System.out.println("To move W type 7");
            //we have a list of actions from the start state
            //get the required action
            do{
                System.out.print("Choose your move : ");
                while(!sc.hasNextInt()){
                    System.out.print("Enter an Int between 0-7 : ");
                    sc.next();
                }
                d = sc.nextInt();
            } while(d < 0 || d > 7);
            System.out.println("Move accepted!");
            Action picked = start.actions.get(d);
            
            //grab the move we made
            start = wm.getState(picked);
                
            //receive reward
            double reward = wm.getReward(start);
            //The reward for moving here was 
            System.out.println("The reward for you move was: " + reward);
            wm.printMap(start.actualLocation);
            
        }

    }
}

class MyPoint{
    int x;
    int y;
    
    public MyPoint(int a, int b){
        x = a;
        y = b;
    }
    
    public boolean equals(Object other){
        if(other instanceof MyPoint){
            MyPoint i = (MyPoint) other;
            if(i.x == this.x){
                if(i.y == this.y){
                    return true;
                }
            }
        }
        return false;
    }
    public String toString(){
        return x + ", " + y;
    }
}