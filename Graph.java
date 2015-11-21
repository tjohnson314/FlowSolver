package flowsolver;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * @author timothyjohnson
 */
public class Graph
{
    private int size;
    private ArrayList<ArrayList<Edge>> edges;
    
    //We store our graph using an ArrayList for the adjacent vertices.
    //The number of vertices is fixed for this problem.
    public Graph(int numVertices)
    {
        size = numVertices;
        edges = new ArrayList<ArrayList<Edge>>();
        for(int i = 0; i < numVertices; i++)
            edges.add(new ArrayList<Edge>());
    }
    
    public void Print()
    {
        System.out.println("Printing graph...");
        for(int i = 0; i < size; i++)
        {
            System.out.print(Integer.toString(i) + ": ");
            for(int j = 0; j < edges.get(i).size(); j++)
                System.out.print(Integer.toString(edges.get(i).get(j).dest) + " ");
            System.out.println();
        }
        System.out.println();

    }
    
    //For this graph, all edges have weight 1.
    //But if we use an edge, then the opposite direction will have capacity 2.
    //So we need to stor the capacity.
    public void addEdge(int v1, int v2)
    {
        if(v1 < 0 || v1 >= size)
        {
            System.out.println("Error! Vertex 1 id in addEdge is invalid.");
            System.exit(-1);
        }
        else if(v2 < 0 || v2 >= size)
        {
            System.out.println("Error! Vertex 2 id in addEdge is invalid.");
            System.exit(-1);
        }
        else
            edges.get(v1).add(new Edge(v2, 1));
    }
    
    //Prints a warning if the edge does not exist.
    public void deleteEdge(int v1, int v2)
    {
        if(v1 < 0 || v1 >= size)
        {
            System.out.println("Error! Vertex 1 id in deleteEdge is invalid.");
            System.exit(-1);
        }
        else if(v2 < 0 || v2 >= size)
        {
            System.out.println("Error! Vertex 2 id in deleteEdge is invalid.");
            System.exit(-1);
        }
        else
        {
            int index = getIndex(v1, v2);

            if(index >= 0)
                edges.get(v1).remove(index);
            else
                System.out.println("Warning: Attempted to delete an edge that does not exist.");
        }
    }
    
    //We increment the capacity of the given edge by 1. This includes creating
    //the edge if it does not yet exist.
    public void incrEdge(int v1, int v2)
    {
        if(v1 < 0 || v1 >= size)
        {
            System.out.println("Error! Vertex 1 id in deleteEdge is invalid.");
            System.exit(-1);
        }
        else if(v2 < 0 || v2 >= size)
        {
            System.out.println("Error! Vertex 2 id in deleteEdge is invalid.");
            System.exit(-1);
        }
        else
        {
            int index = getIndex(v1, v2);
            
            if(index >= 0)
            {
                edges.get(v1).get(index).capacity++;
            }
            else
            {
                edges.get(v1).add(new Edge(v2, 1));
            }
        }
    }
    
    //We decrement the capacity of the given edge by 1. This includes removing
    //the edge if it does not yet exist.
    public void decrEdge(int v1, int v2)
    {
        if(v1 < 0 || v1 >= size)
        {
            System.out.println("Error! Vertex 1 id in deleteEdge is invalid.");
            System.exit(-1);
        }
        else if(v2 < 0 || v2 >= size)
        {
            System.out.println("Error! Vertex 2 id in deleteEdge is invalid.");
            System.exit(-1);
        }
        else
        {
            int index = getIndex(v1, v2);
            
            if(index >= 0)
            {
                if(edges.get(v1).get(index).capacity == 1)
                    edges.get(v1).remove(index);
                else
                    edges.get(v1).get(index).capacity--;
            }
            else
            {
                System.out.println("Error! Tried to decrement capacity of nonexistent edge.");
                System.exit(-1);
            }
        }
    }
    
    private int getIndex(int v1, int v2)
    {
        for(int i = 0; i < edges.get(v1).size(); i++)
        {
            if(edges.get(v1).get(i).dest == v2)
            return i;
        }
        
        return -1;
    }
    
    //Since my maximum flow is at most the number of colors, I use Ford-Fulkerson.
    //We do a depth-first search from the source to the sink, possibly going backwards
    //along edges we have already used and returning that flow.
    public int calcFlow(int src, int sink)
    {   
        int maxFlow = 0;
        while(removePath(src, sink))
            maxFlow++;
        
        //this.Print();
        
        return maxFlow;
    }
    
    public boolean removePath(int src, int sink)
    {
        //this.Print();
        int curr, next;
        Stack<Integer> toSearch = new Stack<Integer>();
        boolean[] found = new boolean[size];
        int[] prevNode = new int[size];
        for(int i = 0; i < size; i++)
        {
            found[i] = false;
            prevNode[i] = -1;
        }
        
        toSearch.add(src);
        while(toSearch.size() > 0)
        {
            curr = toSearch.pop();
            if(curr == sink)
            {
                //System.out.println("Path found!");
                //Remove edges from path, until we reach the source.
                //ArrayList<Integer> pathRev = new ArrayList<Integer>();
                //pathRev.add(sink);
                while(curr != src)
                {
                    //System.out.print(Integer.toString(curr) + " ");
                    int prev = prevNode[curr];
                    //pathRev.add(prev);
                    this.decrEdge(prev, curr);
                    
                    //Add edge for possible backflow
                    this.incrEdge(curr, prev);
                    
                    curr = prev;
                }
                
                //for(int i = pathRev.size() - 1; i >= 0; i--)
                //    System.out.print(pathRev.get(i) + " ");
                //System.out.println("\n");
                
                return true;
            }
            
            if(!found[curr])
            {
                found[curr] = true;
                
                for(int i = 0; i < edges.get(curr).size(); i++)
                {
                    next = edges.get(curr).get(i).dest;
                    toSearch.add(next);
                    if(prevNode[next] == -1)
                        prevNode[next] = curr;
                }
            }
        }
        
        return false;
    }
}

class Edge
{
    int dest;
    int capacity;
    
    public Edge(int dest, int capacity)
    {
        this.dest = dest;
        this.capacity = capacity;
    }
}