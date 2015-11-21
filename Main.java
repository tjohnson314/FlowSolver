/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * A program to solve puzzles from the Flow app, using linear programming.
 *
 * The board is described in a matrix in a text file. For a board with n
 * colors, the colors are assigned labels from 1 to n. Empty squares are
 * marked with 0. The start and end points are duplicated in a separate list.
 *
 * Each of the n^2 squares is assigned a number from 0 to n^2 - 1, by labeling
 * across from left to right, top to bottom. A path is stored as an array of
 * length n^2 - 1, with 0 marking unused squares, 1 marking the start and used
 * squares, and 2 marking the endpoint.
 *
 * For each color, we recursively construct the list of all possible paths
 * from the start to the end point that satisfy some minimum pruning criteria.
 * Each path is then given a weight variable, which must be binary.
 *
 * Our constraints are that the weights of the paths at each point in our grid
 * must sum to 1, and that there must be exactly one path of each color. Such
 * a set of paths constitutes a solution of the Flow problem.
 *
 * The following simple pruning criteria can be used:
 * 1) A path is pruned if it creates an uncovered region of the graph with no
 *    start/end points, or with exactly one of some color's start/end points.
 * 2) A path is pruned if it covers the same squares and has the same start/end
 *    points as another path.
*/

package flowsolver;

import java.util.Scanner;
import java.io.*;

/**
 *
 * @author timothyjohnson
 */
public class Main
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        String fileName = "input12.txt";
        Board board = ReadFile(fileName);
        board.Solve();
    }
    
    public static Board ReadFile(String file)
    {
        System.out.println("Reading file.");
        int size;
        int colors;
        int[] boardArr;
        String nextLine;
        
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(file));
            nextLine = in.readLine();
            Scanner sc = new Scanner(nextLine);
            size = sc.nextInt();
            colors = sc.nextInt();
            boardArr = new int[size*size];
            
            for(int i = 0; i < size; i++)
            {
                nextLine = in.readLine();
                sc = new Scanner(nextLine);
                for(int j = 0; j < size; j++)
                {
                    boardArr[i*size + j] = sc.nextInt();
                }
            }
            
            return new Board(size, colors, boardArr);
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        
        return new Board(0, 0, new int[0]);
    }
    
}
