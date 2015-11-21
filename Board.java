/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package flowsolver;

import java.util.Arrays;
import java.util.ArrayList;

/**
 *
 * @author timothyjohnson
 */
public class Board {
    
    public int size;
    public int numColors;
    public int[] boardArr;
    public int[] colorStarts;
    public int[] colorEnds;
    
    public Board(int size, int colors, int[] boardArr)
    {
        System.out.println("Creating new board.");
        this.size = size;
        this.numColors = colors;
        this.boardArr = boardArr;
        
        this.colorStarts = new int[colors];
        this.colorEnds = new int[colors];
        
        for(int i = 0; i < colors; i++)
        {
            colorStarts[i] = -1;
            colorEnds[i] = -1;
        }
        
        for(int i = 0; i < size*size; i++)
        {
            if(boardArr[i] > 0)
            {
                int color = boardArr[i];
                if(color > colors)
                    System.out.println("Error! Too many colors.");
                if(colorStarts[color - 1] < 0)
                    colorStarts[color - 1] = i;
                else if(colorEnds[color - 1] < 0)
                    colorEnds[color - 1] = i;
                else
                    System.out.println("Error! More than two points of same color: " + color);
            }
        }
    }
    
    public void PrintBoard()
    {
        for(int i = 0; i < size; i++)
        {
            for(int j = 0; j < size; j++)
                System.out.format("%4d", boardArr[i*size + j]);
            
            System.out.println();
        }
    }
    
    public void PrintEqs(int[][] eqs)
    {
        for(int i = 0; i < eqs.length; i++)
        {
            for(int j = 0; j < eqs[i].length; j++)
                System.out.print(eqs[i][j] + " ");
            System.out.println();
        }
    }
    
    public void PrintPaths(int[][][] paths)
    {
        for(int i = 0; i < paths.length; i++)
        {
            System.out.println("Color: " + (i + 1));
            for(int j = 0; j < paths[i].length; j++)
            {
                System.out.println("Path: " + j);
                for(int k = 0; k < size; k++)
                {
                    for(int l = 0; l < size; l++)
                    {
                        System.out.print(paths[i][j][size*k + l] + ",");
                    }
                    System.out.println();
                }
                System.out.println();
            }    
        }
    }
    
    public ArrayList<Integer> adjEmpty(int board[], int loc)
    {
        ArrayList<Integer> adjSquares = new ArrayList<Integer>();
        if(loc >= size && board[loc - size] == 0) //Move up
            adjSquares.add(loc - size);
        
        if(loc < size*(size - 1) && board[loc + size] == 0) //Move down
            adjSquares.add(loc + size);
        
        if(loc%size > 0 && board[loc - 1] == 0) //Move left
            adjSquares.add(loc - 1);
        
        if(loc%size < size - 1 && board[loc + 1] == 0) //Move right
            adjSquares.add(loc + 1);
        
        return adjSquares;
    }
    
    public boolean isAdjacent(int loc1, int loc2)
    {
        int row1 = loc1/size;
        int row2 = loc2/size;
        int col1 = loc1%size;
        int col2 = loc2%size;
        int dist = Math.abs(row1 - row2) + Math.abs(col1 - col2);
        
        return(dist == 1);
    }
    
    public void Solve()
    {
        Preprocess();
        System.out.println("Processed board: ");
        PrintBoard();
        int[][][] paths = this.FindPaths();
        PrintPaths(paths);
        
        int[] numPathsColor = new int[numColors]; //The number of paths for each color.
        System.out.print("Path choices: ");
        for(int i = 0; i < numColors; i++)
            System.out.print(paths[i].length + " ");
        System.out.println();
        
        int[][] eqs = CreateEqs(paths); //Creates the matrix of linear equations to solve.
        //PrintEqs(eqs);
        int[] pathInds = new int[numColors];
        boolean solutionCheck = SolveEqs(eqs, pathInds, numColors - 1); //Finds a 0/1 solution of our system of equations.
        if(solutionCheck)
            DisplaySolution(pathInds, paths); //Displays the board with the solution discovered.
        else
            System.out.println("Error! No solution found.");
    }
    
    //We first preprocess the board, to identify the squares near the start
    //and end points for each color that can be determined with certainty.
    //Then we shift the new start and end points.
    public void Preprocess()
    {
        boolean changeMade = true;
        while(changeMade)
        {
            //System.out.println("New board:");
            //PrintBoard();
            changeMade = false;
            for(int i = 0; i < numColors; i++)
            {
                if(colorStarts[i] != colorEnds[i])
                    changeMade |= extendPath(colorStarts[i], i, true);
                
                if(colorStarts[i] != colorEnds[i])
                    changeMade |= extendPath(colorEnds[i], i, false);
            }
        }
    }
    
    //Attempts to extend a path, and returns true if successful and false otherwise.
    public boolean extendPath(int loc, int currColor, boolean start)
    {
        //start is true if this method has been called by the start location,
        //and false if this method has been called by the end location.
        //currColor is the index, which is 1 less than the label on the board.
        //System.out.println("loc: " + loc + ", currColor: " + currColor + " start: " + start);
        int empty = 0;
        int nextLoc = -1;
        if(loc >= size && checkExtend(loc - size, currColor, start)) //Move up
        {
            nextLoc = loc - size;
            empty++;
        }
        
        if(loc < size*(size - 1) && checkExtend(loc + size, currColor, start)) //Move down
        {
            nextLoc = loc + size;
            empty++;
        }
        
        if(loc%size > 0 && checkExtend(loc - 1, currColor, start)) //Move left
        {
            nextLoc = loc - 1;
            empty++;
        }
        
        if(loc%size < size - 1 && checkExtend(loc + 1, currColor, start)) //Move right
        {
            nextLoc = loc + 1;
            empty++;
        }
        
        if(empty == 1)
        {
            //Extend path to new location
            boardArr[nextLoc] = currColor + 1;
            if(start)
                colorStarts[currColor] = nextLoc;
            else
                colorEnds[currColor] = nextLoc;
            return true;
        }
        else return false;
    }
    
    //Checks whether we can extend a path. This is true whenever that square is
    //currently empty, or when the new edge connects the two ends of our path
    //for the current color.
    public boolean checkExtend(int newLoc, int currColor, boolean start)
    {
        if(boardArr[newLoc] == 0)
            return true;
        //Add 1 to the index to get the color label on the board
        else if(boardArr[newLoc] == currColor + 1)
        {
            if(start && (newLoc == colorEnds[currColor]))
                return true;
            if(!start && (newLoc == colorStarts[currColor]))
                return true;
        }
        return false;
    }
    
    //maxAvail gives the number of empty squares each path is allowed to use.
    public int[][][] FindPaths()
    {
        int paths[][][] = new int[numColors][][];
        
        for(int i = 0; i < numColors; i++)
        {
            int[] boardCopy = Arrays.copyOf(boardArr, size*size);
            boardCopy[colorEnds[i]] = 0;
            int[][] nextPaths = FindColoredPaths(i + 1, colorStarts[i], colorEnds[i], boardCopy);
            
            //We check each path for conflicts with each previous color.
            //There will be many paths for the first color, since these are not checked.
            //But by the time we choose one of those paths, most will have been
            //eliminated, since we work backwards through the list of colors.
            System.out.println("Color " + Integer.toString(i + 1) + " : " + Integer.toString(nextPaths.length) + " paths found!");
            nextPaths = ValidPaths(nextPaths, paths, i);
            System.out.println("Color " + Integer.toString(i + 1) + " : " + Integer.toString(nextPaths.length) + " valid paths found!");
            paths[i] = nextPaths;
        }
        
        return paths;
    }
    
    //Recursively finds the possible paths for our current color from start to
    //to finish in a partially filled board.
    public int[][] FindColoredPaths(int color, int currLoc, int end, int[] board)
    {   
        /*System.out.println("\nFinding paths for color " + color);
        for(int i = 0; i < size*size; i++)
        {
            System.out.print(board[i] + " ");
            if(i%size == size - 1)
                System.out.println();
        }
        System.out.println();*/
        
        int[][] paths;
        if(currLoc == end)
        {
            //System.out.println("New path found!");
            board[currLoc] = color;
            paths = new int[1][];
            paths[0] = Arrays.copyOf(board,size*size);
            board[currLoc] = 0;
            return paths;
        }
        else
        {
            int nextLoc;
            int[] nextBoard = Arrays.copyOf(board, size*size);
            int[][] nextPaths;
            int[] nextLocs = {-1, -1, -1, -1};
            paths = new int[0][];
            

            if(currLoc >= size)
                nextLocs[0] = currLoc - size; //Move up
            
            if(currLoc < size*(size - 1))
                nextLocs[1] = currLoc + size; //Move down
            
            if(currLoc%size > 0)
                nextLocs[2] = currLoc - 1; //Move left
            
            if(currLoc%size < size - 1)
                nextLocs[3] = currLoc + 1; //Move right
            
            for(int i = 0; i < 4; i++)
            {
                nextLoc = nextLocs[i];
                if(nextLoc >= 0 && nextBoard[nextLoc] == 0)
                {
                    nextBoard[nextLoc] = color;
                    colorStarts[color - 1] = nextLoc; //Decrease by 1 to get index
                    if(BoardCheck(nextBoard, color, nextLoc))
                    {
                        nextPaths = FindColoredPaths(color, nextLoc, end, nextBoard);
                        paths = MergeArrays(paths, nextPaths);
                    }
                    nextBoard[nextLoc] = 0;
                    colorStarts[color - 1] = currLoc;
                }                
            }
            if(paths.length > 10000)
                System.out.println("Color " + color + " : " + paths.length + " paths found");
            return paths;
        }
    }
    
    public boolean BoardCheck(int[] board, int currColor, int currLoc)
    {
        /*for(int i = 0; i < size*size; i++)
            System.out.print(Integer.toString(board[i]) + " ");
        System.out.println();*/
        
        //We check that the maximum flow is equal to the number of colors.
        Graph g = FlowGraph(board, colorStarts, colorEnds, currColor);
        //g.Print();
        int flow = g.calcFlow(size*size, size*size + 1);
        //System.out.println("The max flow is " + Integer.toString(flow));
        if(flow != numColors)
        {
            //System.out.println("The max flow is only " + Integer.toString(flow));
            return false;
        }
        
        //We check that there is a path for each color.
        int start, end;
        boolean[] searched;
        for(int i = 0; i < numColors; i++)
        {
            searched = new boolean[size*size];
            if(i != currColor - 1)
                start = colorStarts[i];
            else
                start = currLoc;
            end = colorEnds[i];
            
            //Our search method requires setting the end square to be 0, so that
            //we can move to it. But if we are checking the current color, this
            //will already have been done.
            if(i != currColor - 1)
                board[end] = 0;
            //System.out.println("Color, (start, end): " + (i + 1) + ", (" + start + ", " + end + ")");
            if(!Connected(board, searched, start, end))
                return false;
            
            if(i != currColor - 1)
                board[end] = i + 1;
            //System.out.println(check);
        }
        
        //We also check that there are no orphaned empty squares.
        boolean[] goal = new boolean[size*size];
        for(int i = 0; i < size*size; i++)
            goal[i] = false;
        for(int i = 0; i < numColors; i++)
        {
            assert(colorStarts[i] == currLoc);
            if(i != currColor - 1)
                goal[colorStarts[i]] = true;
            else
                goal[currLoc] = true;
            goal[colorEnds[i]] = true;
        }
        
        searched = new boolean[size*size];
        for(int i = 0; i < size*size; i++)
            searched[i] = false;
        for(int i = 0; i < size*size; i++)
        {
            if(board[i] == 0 && !searched[i] && !ConnectedZeros(board, i, searched, goal))
                return false;
        }
        
        //Finally, we check that there are no vertices of degree 1, since any
        //path that goes into them will then get stuck.
        boolean[] endpoints = new boolean[size*size];
        for(int i = 0; i < size*size; i++)
            endpoints[i] = false;
        for(int i = 0; i < numColors; i++)
        {
            if(i != currColor - 1)
            {
                if(colorStarts[i] != colorEnds[i])
                {
                    endpoints[colorStarts[i]] = true;
                    endpoints[colorEnds[i]] = true;
                }
            }
            else
            {
                if(currLoc != colorEnds[i])
                {
                    endpoints[currLoc] = true;
                    endpoints[colorEnds[i]] = true;
                }
            }
            
        }
        for(int i = 0; i < size*size; i++)
        {
            if(board[i] == 0 && numEmpty(i, board, endpoints) == 1 && i != colorEnds[currColor - 1])
            {
                //System.out.println("Degree 1 square: " + Integer.toString(i));
                return false;
            }
            else if(board[i] == 0 && numEmpty(i, board, endpoints) == 0)
                System.out.println("THIS SHOULD NEVER HAPPEN!!");
        }
        
        return true;
    }
    
    public Graph FlowGraph(int[] board, int[] colorStarts, int[] colorEnds, int currColor)
    {
        Graph g = new Graph(size*size + 2);
        /*for(int i = 0; i < numColors; i++)
        {
            System.out.print("Color " + Integer.toString(i + 1) + ": ");
            System.out.println(Integer.toString(colorStarts[i]) + ", " + Integer.toString(colorEnds[i]));
        }*/
        
        
        //Vertex size*size is our sink, and vertex (size*size + 1) is our source
        for(int i = 0; i < numColors; i++)
        {
            g.addEdge(size*size, colorStarts[i]);
            g.addEdge(colorEnds[i], size*size + 1);
            //System.out.println(colorEnds[i]);
        }
        
        //Add edges from colorStarts to adjacent empty squares, and from
        //empty squares to colorEnds
        ArrayList<Integer> empty = new ArrayList<Integer>();
        for(int i = 0; i < numColors; i++)
        {
            empty = adjEmpty(board, colorStarts[i]);
            for(int j = 0; j < empty.size(); j++)
                g.addEdge(colorStarts[i], empty.get(j));
        }
        
        for(int i = 0; i < numColors; i++)
        {
            if(i != currColor - 1)
            {
                empty = adjEmpty(board, colorEnds[i]);
                for(int j = 0; j < empty.size(); j++)
                    g.addEdge(empty.get(j), colorEnds[i]);
            }
        }
        
        //Connect adjacent starts and ends. If they are our current color,
        //this will already be done.
        for(int i = 0; i < numColors; i++)
        {
            if(i != currColor && isAdjacent(colorStarts[i], colorEnds[i]))
                g.addEdge(colorStarts[i], colorEnds[i]);
        }
        
        for(int i = 0; i < size*size; i++)
        {
            if(board[i] == 0)
            {
                empty = adjEmpty(board, i);
                for(int j = 0; j < empty.size(); j++)
                {
                    g.addEdge(i, empty.get(j));
                }
            }
        }
        return g;
    }
    
    public boolean Connected(int[] board, boolean[] searched, int startLoc, int endLoc)
    {
        //System.out.println(startLoc);
        if(startLoc == endLoc)
            return true;
        else
        {
            int[] newLocs = {-1, -1, -1, -1};
            if(startLoc >= size && board[startLoc - size] == 0 && !searched[startLoc - size])
                newLocs[0] = startLoc - size;
            
            if(startLoc < size*(size - 1) && board[startLoc + size] == 0 && !searched[startLoc + size])
                newLocs[1] = startLoc + size;
            
            if(startLoc%size > 0 && board[startLoc - 1] == 0 && !searched[startLoc - 1])
                newLocs[2] = startLoc - 1;
            
            if(startLoc%size < size - 1 && board[startLoc + 1] == 0 && !searched[startLoc + 1])
                newLocs[3] = startLoc + 1;
            
            searched[startLoc] = true;
            for(int i = 0; i < 4; i++)
            {
                if(newLocs[i] >= 0 && Connected(board, searched, newLocs[i], endLoc))
                    return true;
            }
            return false;
        }
    }
    
    public boolean ConnectedZeros(int[] board, int startLoc, boolean[] searched, boolean[] goal)
    {
        //System.out.println(startLoc);
        int[] newLocs = {-1, -1, -1, -1};
        if(startLoc >= size)
        {
            if(goal[startLoc - size])
            {
                goal[startLoc] = true;
                return true;
            }
            else if(board[startLoc - size] == 0 && !searched[startLoc - size])
                newLocs[0] = startLoc - size;
        }

        if(startLoc < size*(size - 1))
        {
            if(goal[startLoc + size])
            {
                goal[startLoc] = true;
                return true;
            }
            else if(board[startLoc + size] == 0 && !searched[startLoc + size])
                newLocs[1] = startLoc + size;
        }

        if(startLoc%size > 0)
        {
            if(goal[startLoc - 1])
            {
                goal[startLoc] = true;
                return true;
            }
            else if(board[startLoc - 1] == 0 && !searched[startLoc - 1])
                newLocs[2] = startLoc - 1;
        }

        if(startLoc%size < size - 1)
        {
            if(goal[startLoc + 1])
            {
                goal[startLoc] = true;
                return true;
            }
            else if(board[startLoc + 1] == 0 && !searched[startLoc + 1])
                newLocs[3] = startLoc + 1;
        }

        searched[startLoc] = true;
        for(int i = 0; i < 4; i++)
        {
            if(newLocs[i] >= 0 && ConnectedZeros(board, newLocs[i], searched, goal))
            {
                goal[newLocs[i]] = true;
                return true;
            }
        }
        return false;
    }
    
    public int numEmpty(int loc, int[] board, boolean[] endpoints)
    {
        int empty = 0;
        int[] newLocs = {-1, -1, -1, -1};
        if(loc >= size)
            newLocs[0] = loc - size;
        if(loc < size*(size - 1))
            newLocs[1] = loc + size;
        if(loc%size > 0)
            newLocs[2] = loc - 1;
        if(loc%size < size - 1)
            newLocs[3] = loc + 1;
        
        for(int i = 0; i < 4; i++)
        {
            if(newLocs[i] >= 0 && (board[newLocs[i]] == 0 || endpoints[newLocs[i]]))
                empty++;
        }
        return empty;
    }
    
    public int[][] ValidPaths(int[][] currPaths, int[][][] allPaths, int currColor)
    {
        boolean[] validPaths = new boolean[currPaths.length];
        int numValidPaths = 0;
        for(int i = 0; i < currPaths.length; i++)
            validPaths[i] = true;
        
        //validPaths[i] = for all colors there exists a path with no conflicts
        for(int i = 0; i < currPaths.length; i++)
        {
            for(int j = 0; j < currColor && validPaths[i]; j++)
            {
                boolean colorConflict = true;
                for(int k = 0; k < allPaths[j].length && colorConflict; k++)
                {
                    boolean pathConflict = false;
                    for(int l = 0; l < size*size && !pathConflict; l++)
                    {
                        if((currPaths[i][l] > 0) && (allPaths[j][k][l] > 0) && (currPaths[i][l] != allPaths[j][k][l]))
                            pathConflict = true;
                    }
                    if(!pathConflict)
                        colorConflict = false;
                }
                if(colorConflict)
                    validPaths[i] = false;
            }
            if(validPaths[i])
                numValidPaths++;
        }
        
        int[][] newPaths = new int[numValidPaths][size*size];
        int currValidPath = 0;
        for(int i = 0; i < currPaths.length; i++)
        {
            if(validPaths[i])
            {
                newPaths[currValidPath] = currPaths[i];
                currValidPath++;
            }
        }
        return newPaths;
    }
    
    //Returns 1 if arr1 is larger, -1 if arr2 is larger,
    //and 0 if the two arrays are equal.
    public int CompareArrays(int[] arr1, int[] arr2)
    {
        assert(arr1.length == arr2.length);
        for(int i = 0; i < arr1.length; i++)
        {
            if(arr1[i] > arr2[i])
                return 1;
            else if(arr1[i] < arr2[i])
                return -1;
        }
        return 0;
    }
    
    // Adds arr1 and arr2 into a new array, remove duplicate elements.
    public int[][] MergeArrays(int[][] arr1, int[][] arr2)
    {
        int newLength = arr1.length + arr2.length;
        int ind1 = 0; //Index for arr1
        int ind2 = 0; //Index for arr2
        int indNew = 0; //Index for arrNew
        int[][] arrNew = new int[newLength][];
        
        while(ind1 < arr1.length && ind2 < arr2.length)
        {
            int compare = CompareArrays(arr1[ind1], arr2[ind2]);
            if(compare == -1) //Add element from first array
            {
                arrNew[indNew] = arr1[ind1];
                ind1++;
            }
            else if(compare == 1) //Add element from second array.
            {
                arrNew[indNew] = arr2[ind2];
                ind2++;
            }
            else // Add only one copy of the identical elements.
            {
                arrNew[indNew] = arr1[ind1];
                ind1++;
                ind2++;
            }
            indNew++;
        }
        
        while(ind1 < arr1.length)
        {
            arrNew[indNew] = arr1[ind1];
            ind1++;
            indNew++;
        }
        
        while(ind2 < arr2.length)
        {
            arrNew[indNew] = arr2[ind2];
            ind2++;
            indNew++;
        }
        
        arrNew = Arrays.copyOf(arrNew, indNew);
        return arrNew;
    }
    
    //paths contains a different array of possible paths for each color.
    //We then create one linear equation for each grid square. This
    //also will enforce the constraint of having one path per color, since
    //all paths of the same color have the same endpoints.
    public int[][] CreateEqs(int[][][] paths)
    {
        int numPaths = 0;
        for(int i = 0; i < numColors; i++)
            numPaths += paths[i].length;
        
        //We have one linear equation for each square in our grid,
        //and there are numPaths variables.
        //TODO: For efficiency, remove the equations for the final points
        //of each path, since they duplicate the equations for the
        //starting points.
        int[][] eqs = new int[size*size][numPaths];
        int currPathInd = 0;
        for(int i = 0; i < numColors; i++)
        {
            for(int j = 0; j < paths[i].length; j++)
            {
                for(int k = 0; k < size*size; k++)
                {
                    if(paths[i][j][k] == i + 1)
                        eqs[k][currPathInd] = 1;
                }
                currPathInd++;
            }
        }
        
        return eqs;
    }
    
    //For now, I'm using my own solver, which just does a recursive search.
    //It returns an array specifying the index of the path that is true for each color.
    public boolean SolveEqs(int[][] eqs, int[] solution, int currColor)
    {
        /*System.out.print("Current solution: ");
        for(int i = 0; i < solution.length; i++)
            System.out.print(solution[i] + " ");
        System.out.println();*/
        
        int numEqs = eqs.length;
        int numPaths = eqs[0].length;
        boolean solutionCheck;
        int[][] newEqs;
        //System.out.println("Equations: " + numEqs + ", Paths: " + numPaths);
        //PrintEqs(eqs);
        
        int currIndShift = 1; //The number of paths we have tried for a given color.
        while(currIndShift <= numPaths)
        {
            solutionCheck = true;
            //Last variable true: We remove all other variables for that equation.
            //If this fails, we decrease numPaths by 1 so that we ignore the last
            //column of our equations, and try again, until there are no paths left.
            
            //System.out.println("Final variable true.");
            int numEqsSatisfied = 0;
            int[] pathsRemoved = new int[numPaths - currIndShift];
            for(int i = 0; i < numPaths - currIndShift; i++)
                pathsRemoved[i] = 0;
            int newNumEqs;
            
            //Record which paths intersect with the one we are choosing, and 
            //remove those columns from our matrix.
            for(int i = 0; i < numEqs; i++)
            {
                if(eqs[i][numPaths - currIndShift] == 1)
                {
                    numEqsSatisfied++;
                    for(int j = 0; j < numPaths - currIndShift; j++)
                        if(eqs[i][j] == 1)
                            pathsRemoved[j] = 1;
                }
            }

            int numPathsIntersect = 0;
            for(int i = 0; i < numPaths - currIndShift; i++)
                if(pathsRemoved[i] == 1)
                    numPathsIntersect++;
            newNumEqs = numEqs - numEqsSatisfied;
            int newNumPaths = (numPaths - currIndShift) - numPathsIntersect;
            //System.out.println("New equations: " + newNumEqs + ", New paths: " + newNumPaths);

            if(newNumEqs == 0)
            {
                //System.out.println("Solution found!");
                //Solution found! Final path set to 1, all other paths set to 0.
                //Note that the final path in our current set of our equations is
                //stored in the columnsUsed array.
                solution[currColor] = numPaths - currIndShift;
                //System.out.println("Unshifted index: " + (numPaths - currIndShift));
                return true;
            }
            else if(newNumPaths == 0)
            {
                //No solution. We try a new path for the current color.
                solutionCheck = false;
            }
            else
            {
                //We must check the paths for the next color.
                newEqs = new int[newNumEqs][newNumPaths];
                int currPath = 0;
                boolean validEqs = true; //A variable to check that all rows have a 1.
                for(int i = 0; i < numEqs; i++)
                {
                    if(eqs[i][numPaths - currIndShift] != 1)
                    {
                        int currColumn = 0;
                        boolean validRow = false; //A variable to check that the current row has a 1.
                        for(int j = 0; j < numPaths - currIndShift; j++)
                        {
                            if(pathsRemoved[j] == 0)
                            {
                                newEqs[currPath][currColumn] = eqs[i][j];
                                if(eqs[i][j] == 1)
                                    validRow = true;
                                currColumn++;
                            }
                        }
                        if(!validRow)
                        {
                            validEqs = false;
                            break;
                        }
                        currPath++;
                    }
                }
                if(validEqs)
                {
                    //System.out.println("currColor: " + (currColor + 1) + ", currIndShift: " + currIndShift);
                    solutionCheck = SolveEqs(newEqs, solution, currColor - 1);
                }
                else
                    solutionCheck = false;
                if(solutionCheck)
                {
                    //Solution found!
                    //We have to shift the indices for each color to account for
                    //the paths that were deleted before those paths were chosen.
                    ShiftSolution(solution, pathsRemoved, currColor);
                    solution[currColor] = numPaths - currIndShift;
                    return solutionCheck;
                }
            }

            //No solution was found. Try again with last variable false.
            //System.out.println("No solution found. Trying again with final variable false.");
            currIndShift++;
        }
        
        //We break from our while loop only after we have removed all of the paths.
        return false;
    }
    
    public void ShiftSolution(int[] solution, int[] removedInds, int maxInd)
    {
        int[] newSolution = new int[numColors];
        int toShift = 0;
        int pathsUsed = -1;
        for(int i = 0; i < removedInds.length && toShift < maxInd; i++)
        {
            if(removedInds[i] == 0)
                pathsUsed++;
            if(solution[toShift] == pathsUsed)
            {
                newSolution[toShift] = i;
                toShift++;
            }
        }
        
        for(int i = 0; i < maxInd; i++)
            solution[i] = newSolution[i];
    }
    
    public void DisplaySolution(int[] pathInds, int[][][] paths)
    {
        System.out.println("Solution found!");
        int[] colorInds = new int[numColors];
        int currPathShift = 0;
        for(int i = 0; i < numColors; i++)
        {
            colorInds[i] = pathInds[i] - currPathShift;
            currPathShift += paths[i].length;
        }
        /*for(int i = 0; i < pathInds.length; i++)
            System.out.print(pathInds[i] + " ");
        System.out.println();*/
        
        int[] filledBoard = new int[size*size];
        for(int color = 0; color < numColors; color++)
        {
            //Copy path into board.
            for(int gridSq = 0; gridSq < size*size; gridSq++)
                if(paths[color][colorInds[color]][gridSq] == color + 1)
                    filledBoard[gridSq] = color + 1;
        }
        
        System.out.println("\nFilled board:");
        for(int i = 0; i < size; i++)
        {
            for(int j = 0; j < size; j++)
                System.out.format("%4d", filledBoard[i*size + j]);
            System.out.println();
        }
    }
}
