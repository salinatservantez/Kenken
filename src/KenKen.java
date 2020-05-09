/**
 * Description: prompts the user for a file that contains the specifications for a puzzle.
 * The first line of the puzzle file should specify the size of the puzzle and assumes a maximum size of 9x9.
 * Each subsequent line should identify a cage, with the constraint first followed by the coordinates in the cage
 *
 * @author Salina Servantez
 * @edu.uwp.cs.340.section001
 *  * @edu.uwp.cs.340.assignment5-KenKen
 *  * @bugs none
 */


import java.io.*;
import java.util.*;

/**
 * KenKen Class: Takes in a file with a KenKen board and solves. Afterwards it outputs to a new file the solved board
 */
public class KenKen {


    // The KenKen board
    private int[][] KenKen;
    private ArrayList<Group> groups;

    /**
     * Constructor for KenKen objects
     * @param board Takes a KenKen board as 2D array
     */
    private KenKen(int [][] board, ArrayList<Group> groups)
    {
        KenKen = board;
        this.groups = groups;
    }

    /**
     * Checks if a number placement is valid on the current board
     * @param row Row of the number placement
     * @param col Column of the number placement
     * @param number The number being put in the spot
     * @return A boolean on whether or not it is a valid move
     */
    private boolean validMove(int row, int col,int number, int size){
        //Checks if the number comes up in the row
        for(int i=0;i<size;i++)
        {
            if(KenKen[row][i]==number && col != i)
            {
                return false;
            }
        }

        //Checks if the number comes up in the column
        for(int i=0;i<size;i++)
        {
            if(KenKen[i][col]==number && row != i)
            {
                return false;
            }
        }

        Point p = new Point(col, row);

        //Figure out which group the point is in and check if it is valid in that group
        for (Group group: groups) {
            if(group.containsPoint(p)){
                if(!group.validGrouping(KenKen)){
                    return false;
                }
                break;
            }
        }

        //Return true if all checks pass
        return true;
    }

    /**
     * Solves the KenKen puzzle
     * @return True if the puzzle was solved succesfully otherwise false
     */

    public boolean solve(int size)
    {
        //Starts from the top left square and goes until it fails
        for(int row=0;row<size;row++)
        {
            for(int col=0;col<size;col++)
            {
                //If the space doesn't have a number yet
                if(KenKen[row][col]== 0)
                {
                    for(int number=1;number <= size;number++)
                    {
                        KenKen[row][col] = number;
                        //Make sure the move is valid
                        if(validMove(row, col, number, size))
                        {
                            //Solve with the new board variation
                            //If the number worked with the current board return true
                            if(solve(size))
                            {
                                return true;
                            }
                        }
                        //Otherwise change the number back to 0
                        KenKen[row][col] = 0;
                    }
                    //Return false if all 9 numbers were tested and failed
                    return false;
                }
            }
        }
        //Return true if the end of the loop is reached, this means the solution has been found
        return true;
    }

    public static void main(String[] args) throws Exception
    {
        Scanner input = new Scanner(System.in);
        System.out.print("Enter the filename for the KenKen board: ");
        String filename = input.next();
        //The board to be passed to the constructor
        Scanner file = new Scanner(new File(filename));
        //Find the size of the board
        int size = Integer.parseInt(file.nextLine());
        int [][] board = new int[size][size];
        String [][] groupBoard = new String[size][size];
        ArrayList<Group> groups = new ArrayList<>();

        //Read in the file to the board array
        while(file.hasNextLine()){
            String line = file.nextLine();
            //Split into individual numbers
            String[] numbers = line.split(" ");
            //Grab the expected result and the operator of the group
            int result = Integer.parseInt(numbers[0]);
            char op = numbers[1].charAt(0);
            ArrayList<Point> points = new ArrayList<>();
            int j = 2;
            // If it isn't group fill the slot on the board
            if(op == '#'){
                int y = Integer.parseInt(numbers[j++]);
                int x = Integer.parseInt(numbers[j]);
                board[y][x] = result;
            } else {
                //Otherwise get all of the points in the group
                while(j < numbers.length){
                    int y = Integer.parseInt(numbers[j++]);
                    int x = Integer.parseInt(numbers[j++]);
                    Point p = new Point(x, y);
                    board[y][x] = 0;
                    points.add(p);
                    groupBoard[y][x] = "" + result + op;
                }

                //Create the group and add it to the list of groups
                Group group = new Group(op, points, result);
                groups.add(group);

            }
        }

        //Make and solve the KenKen board
        KenKen kenken = new KenKen(board, groups);
        if(kenken.solve(size)){
            //If it is solved print out the board
            System.out.println("Solved");
            for (int i = 0; i < size; i++){
                for (int j = 0; j < size; j++) {
                    System.out.print(kenken.KenKen[i][j] + " ");
                }
                System.out.println();
            }
        } else {
            //If there is no solution print a message
            System.out.println("A solution could not be found");
        }
    }


}

//Class to hold the group of math operations for the board
class Group {
    //The operator for the group
    private char op;
    //The points contained in the group
    public ArrayList<Point> points;
    //The expected result
    private int result;

    /**
     * Constructor for groups
     * @param op - The operator of the group
     * @param points - The points in the group
     * @param result - The result in the group
     */
    public Group(char op, ArrayList<Point> points, int result){
        this.op = op;
        this.points = points;
        this.result = result;
    }

    /**
     * Checks if the points on the board follow the rules of the group
     * @param board
     * @return
     */
    public boolean validGrouping(int[][] board){

        //Do the appropriate operation based on the group
        //Return true if the result is correct or points in the group are empty
        if(op == '/'){
            int math = opDiv(board);
            return result == math || math == 0;
        }
        else if ( op == '-' ){
            int math = opSub(board);
            return result == math || math == 0;
        }
        else if ( op == '+') {
            int math = opAdd(board);
            return result == math || math == 0;
        }
        else {
            int math = opMult(board);
            return result == math || math == 0;
        }
    }


    /**
     * Add operation for groups
     * @param board - The board being checked
     * @return  The result of the summation
     */
    private int opAdd(int [][] board){

        //Sum up all of the points
        int result = 0;

        for(Point point: points){
            int num = board[point.y][point.x];
            //Return 0 if any points are empty
            if(num == 0)
                return 0;
            result += num;
        }

        return result;
    }

    /**
     * Multiplication operation for groups
     * @param board - The board being checked
     * @return The result of the multiplication
     */
    private int opMult(int [][] board){

        int result = 0;

        //Multiply all the points
        for(Point point: points){
            //Set the value of the first point to result
            if (result == 0)
                result = board[point.y][point.x];
            else {
                //If the point is zero return 0
                if (board[point.y][point.x] != 0)
                    result *= board[point.y][point.x];
                else
                    return 0;

            }
            //Don't keep going if the result is already too big
            if (result > this.result)
                return -1;
        }

        return result;
    }

    /**
     * Division operation for groups
     * @param board - The board being checked
     * @return The result of the division
     */
    private int opDiv(int [][] board){
        double result;

        //Grab both points and take value inside of them
        Point p1 = points.get(0);
        Point p2 = points.get(1);
        double num1 = board[p1.y][p1.x];
        double num2 = board[p2.y][p2.x];

        //If either are zero return 0 otherwise divide the smaller value from the larger
        if(num1 == 0 || num2 == 0){
            return 0;
        } else if (num1 > num2) {
            result = num1 / num2;
        } else {
            result = num2 / num1;
        }


        //If there is a remainder return a failing number
        if(result - (int) result == 0){
            return (int) result;
        } else {
            return -1;
        }
    }

    /**
     * Subtraction operation for groups
     * @param board - The board being checked
     * @return The result of the division
     */
    private int opSub(int [][] board){
        int result = 0;

        //Grab both points and take value inside of them
        Point p1 = points.get(0);
        Point p2 = points.get(1);
        int num1 = board[p1.y][p1.x];
        int num2 = board[p2.y][p2.x];

        //If either are zero return 0 otherwise subtract the smaller value from the larger
        if(num1 == 0 || num2 == 0){
            return result;
        } else if (num1 > num2) {
            result = num1 - num2;
        } else {
            result = num2 - num1;
        }

        return result;
    }

    /**
     * Check if the point is inside of the group
     * @param p the point being checked
     * @return A boolean on whether or not it is in the group
     */
    public boolean containsPoint(Point p){
        for(Point p2: points){
            if(p2.x == p.x && p2.y == p.y)
                return true;
        }
        return false;
    }

}

// Point class keep track of groups
class Point {
    public int x;
    public int y;

    public Point (int x, int y){
        this.x = x;
        this.y = y;
    }


}