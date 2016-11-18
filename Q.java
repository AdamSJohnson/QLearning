import java.io.*;
import java.util.*;

public class Q{
    public static void main(String[] args){
        File f;
        try{
            //take in the data file from args[0]
             f = new File(args[0]);
             //after reading in the file create the objects
            Agent a = processFile(f);
            a.map().printMap();
            /*
            Scanner derp = new Scanner(System.in);
            //evaluate all states and all the state action sets
            for(int i = 0; i < 7; i++){
                for(int j = 0; j < 7; j++){
                    State s = a.map().getStateMap()[i][j];
                    //print out all actions 
                    for(int k = 0; k < s.actions.size(); k++){
                        System.out.println(k + " action leads from " + i + "," + j + " to " + s.actions.get(k));
                        String ssdfasdfasdf = derp.nextLine();
                    }
                }
            }
            */
            //Now I need to implement agent movement
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
        
        //prepare the escape
        for(Obstacle o : obstacleLoc){
            System.out.print(o.location+" | ");
        }

        //return a new worldmap
        WorldMap m =  new WorldMap(trollLoc, ponyLoc, obstacleLoc, n, escapeLoc);
        
        return new Agent(m);
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
    private ArrayList<Troll> trolls;
    private ArrayList<Pony> ponies;
    private ArrayList<Obstacle> obstacles;
    private int size;
    private MyPoint escape;
    private State[][] map;
    
    public WorldMap(ArrayList<Troll> t, ArrayList<Pony> p, ArrayList<Obstacle> a, int s, MyPoint esc){
        trolls = t;
        ponies = p;
        obstacles = a;
        size = s;
        map = new State[size][size];
        escape = esc;
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
                     
                    nw = new Action(map[i][j]);
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
                        nw = new Action(map[i-1][j+1]);
                    } else {
                        nw = new Action(map[i][j]);
                    }
                }
                
                //setup north
                if(j + 1 >= size){
                     
                    n = new Action(map[i][j]);
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
                        
                        n = new Action(map[i][j+1]);
                    } else {

                        n = new Action(map[i][j]);
                    }
                }
                
                //setup northeast
                if(i + 1 >= size || j + 1 >= size){
                     
                    ne = new Action(map[i][j]);
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
                        ne = new Action(map[i+1][j+1]);
                    } else {
                        ne = new Action(map[i][j]);
                    }
                }
                
                //setup east
                if(i + 1 >= size){
                     
                    e = new Action(map[i][j]);
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
                        e = new Action(map[i+1][j]);
                    } else {
                        e = new Action(map[i][j]);
                    }
                }
                
                //setup southeast
                if(i + 1 >= size || j - 1 < 0){
                     
                    se = new Action(map[i][j]);
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
                        se = new Action(map[i+1][j-1]);
                    } else {
                        se = new Action(map[i][j]);
                    }
                }
                
                //setup south
                if(j - 1 < 0){
                     
                    s = new Action(map[i][j]);
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
                        s = new Action(map[i][j-1]);
                    } else {
                        s = new Action(map[i][j]);
                    }
                }
                
                //setup southwest
                if(i - 1 < 0 || j -1 < 0){
                     
                    sw = new Action(map[i][j]);
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
                        sw = new Action(map[i-1][j-1]);
                    } else {
                        sw = new Action(map[i][j]);
                    }
                }
                
                //setup west
                if(i - 1 < 0){
                     
                    w = new Action(map[i][j]);
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
                        w = new Action(map[i-1][j]);
                    } else {
                        w = new Action(map[i][j]);
                    }
                }
                
                //at this point all directions are setup
                ArrayList<Action> actions = new ArrayList<>();
                actions.add(nw);
                actions.add(n);
                actions.add(ne);
                actions.add(e);
                actions.add(se);
                actions.add(s);
                actions.add(sw);
                actions.add(w);
                map[i][j].initActions(actions);
                
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
    }
    
    public State getState(Action a){
        return a.result;
    }

    public double getReward(State s){
        //find out what we landed on
        MyPoint loc = s.pointData();
        
        //check this point against trolls
        for(int i = 0; i < trolls.size(); i++){
            
            if(trolls.get(i).location.equals(loc)){
                return -15;
            }
        }
        
        //check this point against ponies
        for(int i = 0; i < ponies.size(); i++){
            
            if(ponies.get(i).location.equals(loc)){
                //ponies.remove(i);
                return 10;
            }
        }
        
        if(escape.equals(loc)){
            return 15;
        }
        
        return 2;
    }
    
    public State[][] getStateMap(){
        return map;
    }
    
    public void printMap(){
        for(int i = size - 1; i >= 0; i--)
            System.out.print("#");
        System.out.println("##");
        for(int i = size - 1; i >= 0; i--){
            System.out.print("#");
            for(int j = 0; j < size; j++){
                System.out.print(getSymbol(map[j][i]));
            }
             System.out.println("#");
        }
        System.out.print("#");
        for(int i = 0; i <= size - 1; i++)
            System.out.print("#");
        System.out.println("#");
       
    }
    
    private String getSymbol(State s){
        MyPoint position = s.pointData();
        //System.out.print(position+" | ");
        //check if the position is a troll
        for(Troll t : trolls){
            //System.out.print((new Troll(position)).location+ " ");
            //System.out.print(t.location+" " );
            if(t.equals(new Troll(position))){
               return "T"; 
            }
        }
        for(Obstacle it : obstacles){
            
            if(it.equals(new Obstacle(position))){
               return "#"; 
            }
        }
        for(Pony p : ponies){

            if(p.equals(new Pony(position))){
               return "P"; 
            }
        }
        
        if(escape.equals(position)){
            return "E";
        }
        return "-";
    }
    
}
class State{
    ArrayList<Action> actions;
    MyPoint actualLocation;
    public State(MyPoint p){
        actualLocation = p;
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
    
}

class Action{
    State result;
    double q;
    double alpha;
    
    public Action(State r){
        result = r;
        alpha = .1;
        q = 20;
    }
    
    public String toString(){
        return result.pointData().toString();
    }
    
    public void update(Double d){
        q = q + alpha * ( d - q );
    }
}

class Agent {
    WorldMap wm;
    
    public Agent(WorldMap a){
        wm = a;
    }
    public void run(){
        //get start space
        int count = 0;
        double highest = 0;
        while(count != 1000){
            double t = oneEpoch(wm.getStart());
            if(t > highest)
                highest = t;
                
            count++;
            System.out.println(count);
        }
        System.out.println(highest);
        
    }
    
    private double oneEpoch(State start){
        //System.out.println("we got the start");
        double sum = 0;
        boolean running = true;
        while(running){
            Action picked = null;
            double denom = 0;
            //evaluate each action from the start
            for(Action a : start.actions){
                //get the total for each action
                denom += Math.exp(a.q/10);
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
                    summ += Math.exp(a.q/10);
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
            
            //send action to get a new state back to the mothership
            start = wm.getState(picked);
            
            //receive reward
            double reward = wm.getReward(start);
            //update the action taken with the reward value 
            picked.update(reward);
            sum+= reward;
            //check if reward is -15 or 15
            if(reward == 15){
                //System.out.println("FOUND THE ESCAPE");
                running = false;
            } else if(reward == -15){
                //System.out.println("KILLED BY DANNY DIVITO");
                running = false;
            }
            


        }
        //System.out.println(sum);
        return sum;
    }
    
    private Action move(){
        return null;
    }
    
    //making this for now
    public boolean Equals(Object other){
        return false;
    }
    
    public WorldMap map(){
        return wm;
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