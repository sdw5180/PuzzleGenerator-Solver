import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SatogaeriGen {
    /* Globals */
    private static int MIN_REGION_SIZE = 2;
    private static boolean DEBUG_MODE = false;
    private static int MAX_CIRCLE_VAL = 4;
    private static int MIN_CIRCLE_VAL = 2;

    private static SatogaeriCell[][] regionBoard;               // board displaying the regions of the board
    private static char[][] board;                              // board displaying the circles after being moved (solution)
    private static Character[][] startBoard;                    // board displaying the circles before being moved
    private static int numRegions;                              // total number of regions for the board
    public static int rows;                                     // board dimensions
    public static int cols;

    private static Map<Integer, SatogaeriRegion> regionsMap;


    /**
     * Generates the region board for the puzzle - ie the regionBoard (n x m) is divided into numRegions total
     * areas in which every cell of an area is connected horizontally or vertically to others in the same region.
     * 
     * A board is only accepted if all regions have at least 2 cells and no cells are left unasigned to a region; 
     * the function will loop until an acceptable board is created then output it to the concole
     * 
     * for a 3 x 3 board example:
     *      1 1 2
     *      3 1 2
     *      3 3 2
     */
    private static void generateRegions(){
        SatogaeriCell newCell = null;
        SatogaeriCell cell = null;
        int r = 0; int c = 0;

        int totCells = rows * cols;
        numRegions = (int)(totCells * (MIN_CIRCLE_VAL/10));
        int perRegion = (totCells / numRegions);

        boolean regionsComplete = false;

        while (!regionsComplete){
            regionBoard = new SatogaeriCell[rows][cols];
            SatogaeriRegion.resetRegions();
            regionsMap = new HashMap<>();

            // instanciate regions:
            for (int regNum = 1; regNum < numRegions; regNum ++){
                SatogaeriRegion reg = new SatogaeriRegion(regNum);
                regionsMap.put(regNum, reg);
            }

            // for each region, assign it a single empty cell as a starting cell:
            for (SatogaeriRegion region : SatogaeriRegion.regions){
                while (regionBoard[r][c] != null){
                    r = randNum(rows); c = randNum(cols);
                }
                newCell = new SatogaeriCell(r, c, region.region);   // region.region lol
                regionBoard[r][c] = newCell;
                region.addCell(newCell);
            }

            // for each region, randomly expand it to fill board cells
            for (SatogaeriRegion region : SatogaeriRegion.regions){
                for (int i = 0; i < perRegion;){
                    cell = region.getRandomCell();
                    if (cell != null){
                        newCell = cell.createNeighborCell(rows, cols, regionBoard);
                        if (newCell != null) { 
                            region.addCell(newCell); 
                            regionBoard[newCell.row][newCell.col] = newCell;
                        }
                        i += 1;
                    }
                }
            }
            if (DEBUG_MODE){ printRegionBoard(); System.out.println("\n"); }
            
            // fill remaining cells by adding them to neighboring regions
            for (int rw = 0; rw < rows; rw++){
                for (int cl = 0; cl < cols; cl++){
                    if (regionBoard[rw][cl] == null){
                        newCell = SatogaeriCell.findMatchNeighbor(regionBoard, rw, cl);
                        if (newCell != null){
                            regionBoard[rw][cl] = newCell;
                            regionsMap.get(newCell.region).addCell(newCell);   // god, this line of code looks ugly
                        }
                    }
                }
            }

            if (DEBUG_MODE){ printRegionBoard(); System.out.println("\n"); }
            regionsComplete = true; // assumed true until proven false

            // If any regions are too small, recreate the board:
            for (SatogaeriRegion reg : SatogaeriRegion.regions){ if (reg.cells.size() < MIN_REGION_SIZE){ 
                if (DEBUG_MODE){ System.out.println("Region with size 1: " + reg.region); }
                regionsComplete = false;
            } }
            // If any cells are unasigned, recreate the board:
            if (!regionsComplete()){
                if (DEBUG_MODE){ System.out.println("Unfilled space in region board"); }
                regionsComplete = false;
            }
        }
        if (DEBUG_MODE){
            System.out.println("\n--------------\nCompleted region board:");
            printRegionBoard();
        }
    }


    /**
     * Given that a regionBoard has been successfully created, generateCircles() created a 'circle' cell for
     * every region, assigns it a movement amount, and moves the circle
     */
    private static void generateCircles(){
        ArrayList<SatogaeriCell> circles = new ArrayList<>();
        boolean successfulMove;
        boolean successfulCircles = false;

        // reset circles:
        for (SatogaeriCell circle : circles){
            circle.circle = false; circle.circleNum = -1;
        }
        circles = new ArrayList<>();
        board = new char[rows][cols];

        while (!successfulCircles){
            // reset circles:
            for (SatogaeriCell circle : circles){ circle.circle = false; circle.circleNum = -1; }
            circles = new ArrayList<>();
            board = new char[rows][cols];

            // generate a random circle for each region:
            for (SatogaeriRegion region : SatogaeriRegion.regions){
                SatogaeriCell circleCell = region.getRandomCell();
                // Create a circle that is to be moved some distance between MIN_CIRCLE_VAL and MAX_CIRCLE_VAL (inclusive)
                circleCell.makeCircle(getRandomNumber(MIN_CIRCLE_VAL, MAX_CIRCLE_VAL));
                region.setCircle(circleCell);
                circles.add(circleCell);
            }
            if (DEBUG_MODE) { printCircleBoard(); }
            
            // fill char board with circle cells and periods (to indicate open space)
            for (SatogaeriCell[] row : regionBoard){
                for (SatogaeriCell cell : row){
                    if (cell.circle){ board[cell.row][cell.col] = Character.forDigit(cell.circleNum, 10); }
                    else{ board[cell.row][cell.col] = '.'; }
                }
            }

            successfulCircles = true;
            // for each circleCell, move it to a start position circleNum spaces from its location:
            for (SatogaeriCell circleCell : circles){
                successfulMove = circleCell.validateMovement(board);

                // if no movement is legal, loop to recreate circle assignments:
                if (!successfulMove) {
                    successfulCircles = false;
                    if (DEBUG_MODE) { System.out.println("Unsuccessful move for circle of region: " + circleCell.region); } 
                    break;
                }
            }
        }

        // fill in a board to hold the start locations of each circle:
        startBoard = new Character[rows][cols];
        for (SatogaeriCell[] row : regionBoard){
            for (SatogaeriCell cell : row){
                if (cell.circle){ startBoard[cell.cRow][cell.cCol] = Character.forDigit(cell.circleNum, 10); }
            }
        }for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){ 
                if (startBoard[i][j] == null) { startBoard[i][j] = '_'; }
            }
        }
    }


    public static void main(String[] args) {
        Scanner consoleIn = new Scanner(System.in);
        System.out.println("Run program in debug mode? y/n\n"+
                            "(preliminary boards will be printed as they are generated, this may increase runtime))");
        String debugMode = consoleIn.nextLine();
        if (debugMode.equals("y")) {DEBUG_MODE = true;}
        System.out.println("Input board dimensions, seperated by a comma and space:\n"+
                            "- Eg: 7, 9");
        String dimensions = consoleIn.nextLine();
        //String dimensions = "7, 9";
        String[] dimensionsArr = dimensions.split(", ");
        rows = Integer.valueOf(dimensionsArr[0]); 
        cols = Integer.valueOf(dimensionsArr[1]);

        System.out.println("Select difficulty  (1-3):\n");
        String difficulty = consoleIn.nextLine();
        switch (difficulty) {
            case "2":
                MAX_CIRCLE_VAL = 5; MIN_CIRCLE_VAL = 2;
                break;
            case "3":
                MAX_CIRCLE_VAL = 6; MIN_CIRCLE_VAL = 3;
                break;
            default:            // default as diffuiculty 1
                MAX_CIRCLE_VAL = 4; MIN_CIRCLE_VAL = 2;
                break;
        }
        System.out.println("board of: " + rows + " by " + cols + "\nGenerating ... \n");

        // Generate and save the new puzzle:
        generateRegions();              
        generateCircles();
        printFinalPuzzle();
        savePuzzle();   

        consoleIn.close();
    }


    /**
     * Stolen: https://www.baeldung.com/java-generating-random-numbers-in-range lol
     * Min assumed to be zero
     * @param max               maximum int value allowed
     * @return
     */
    public static int randNum(int max) {
        return (int) (Math.random() * max);
    }

    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }


    /**
     * outputs the regionBoard to the console
     */
    private static void printRegionBoard(){
        for (SatogaeriCell[] row : regionBoard){
            for (SatogaeriCell cell : row){
                if (cell != null){ System.out.print(cell.region); }
                else{ System.out.print("_"); }
                System.out.print("\t");
            }
            System.out.print("\n");
        }
    }


    /**
     * outputs the regionBoard to the console
     */
    private static void printCircleBoard(){
        System.out.print("\n");
        for (SatogaeriCell[] row : regionBoard){
            for (SatogaeriCell cell : row){
                if (cell != null){ 
                    if (cell.circle){ System.out.print(cell.circleNum); }
                    else{ System.out.print("_"); }
                }
                System.out.print("\t");
            }
            System.out.print("\n");
        }
    }


    /**
     * Verifies that all cells on the board have been assigned to a region
     * @return              true if the board is complete, false otherwise
     */
    private static boolean regionsComplete(){
        for (SatogaeriCell[] row : regionBoard){
            if (Arrays.asList(row).contains(null)){ return false; }
        }
        return true;
    }


    /**
     * outputs the final puzzle to the console
     */
    private static void printFinalPuzzle(){
        String regionRow = "";
        String circleRow = "";
        String solutionRow = "";
        String startRow = "";
        System.out.println("\nRegion map: " + "\t\t\t" +"Solution map: " + "\t\t\t" + "Circles (initial location):\t\tCircles (final loc) map:\n");

        for (int i = 0; i < rows; i ++){
            regionRow = "";
            circleRow = "";
            solutionRow = "";
            startRow = "";

            for (int j = 0; j < cols; j++){
                regionRow += regionBoard[i][j].region + " ";
                if (regionBoard[i][j].region < 10) { regionRow += " ";}
                circleRow += board[i][j] + " "; 
                if (regionBoard[i][j].circle){ solutionRow += "o "; }
                else { solutionRow += "_ "; }
                startRow += startBoard[i][j] + " ";
            }
            System.out.print(regionRow + "\t\t" + circleRow + "\t\t" + startRow + "\t\t" + solutionRow + "\n");
        }
        System.out.println();
    }


    /**
     * Saves the current (completed) puzzle to a txt file following the format used by satogaeri-solver.py:
     * example (for a 3x4 puzzle with 2 regions):
     * 
     *      4 3 5               // num_col, num_row, num_regions -- in that order
     *      . . . .             // board presenting the starting circle locations
     *      . 1 . .
     *      . . 2 .
     *      1 1 1 1             // board displaying the regions (each cell having a 
     *      1 1 2 2             // number to indicate its region)
     *      2 2 2 2
     */
    private static void savePuzzle(){
        // Output the puzzle to a new file:
        String fileName = "newPuzzle.txt";
        File file= new File(fileName);

        // delete file, fileName, if it exists already:
        if (file.delete()) { System.out.println("Previous file deleted: " + file.getName()); }
        // create a new file, fileName:
        try{ if (file.createNewFile()) { System.out.println("File created: " + file.getName()); }
        } catch (IOException e) {  System.out.println("Something went wrong in file creation"); System.exit(-1); }

        String puzzleOut = parsePuzzleText();

        // write the completed puzzle to the file:
        try{
            FileWriter fileWriter = new FileWriter(fileName);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print(puzzleOut);
            printWriter.close();
            System.out.println("Saved puzzle to " + fileName + "\n");
        }
        catch (Exception e) { System.exit(-1);} // oops
    }

    private static String parsePuzzleText(){
        String puzzle = "";
        puzzle += cols + " " + rows + " " + (numRegions - 1) + "\n";

        // starting cells:
        for (int i = 0; i < rows; i ++){
            for (int j = 0; j < cols; j++){
                if (startBoard[i][j] == '_') { puzzle += ". "; }
                else { puzzle += startBoard[i][j] + " "; }
            }
            puzzle += "\n";
        }

        // regions:
        for (int i = 0; i < rows; i ++){
            for (int j = 0; j < cols; j++){
                puzzle += regionBoard[i][j].region + " ";
            }
            puzzle += "\n";
        }

        return puzzle;
    }
}