import java.util.ArrayList;

public class SatogaeriRegion{ // implements Comparable{
    public int region;                                                      // region number
    public boolean filled = false;
    public ArrayList<SatogaeriCell> cells = new ArrayList<>();              // ArrayList containing the regions cells
    public static ArrayList<SatogaeriRegion> regions = new ArrayList<>();   // ArrayList containing all region Objects

    public SatogaeriCell circle;

    /**
     * Constructor
     * @param r     region number
     */
    public SatogaeriRegion(int r){
        this.region = r;
        regions.add(this);
    }


    /**
     * Resets the regions' arraylist for a board reset
     */
    public static void resetRegions(){
        regions = new ArrayList<>();
    }


    /**
     * Marks the region as complete
     */
    public boolean fill(){
        if (!this.filled) { 
            this.filled = true; 
            return this.filled;
        }
        else {
            return false;
        }
    }


    /**
     * Checks if the region is complete
     */
    public boolean isComplete() {
        return this.filled;
    }


    /**
     * Returns the list of cells for this region
     */
    public ArrayList<SatogaeriCell> getCells(){
        return this.cells;
    }


    /**
     * Adds a cell to the region
     * @param cell          cell to be added
     */
    public void addCell(SatogaeriCell cell){
        this.cells.add(cell);
    }


    /**
     * Creates a new cell object and adds it to the region
     * @param r             row of cell to be created
     * @param c             col of cell to be created
     */
    public void addCell(int r, int c){
        SatogaeriCell cell = new SatogaeriCell(r, c, this.region);
        this.addCell(cell);
    }


    /**
     * Saves the region's solution circle cell
     * @param c
     */
    public void setCircle(SatogaeriCell c){
        this.circle = c;
    }

    /**
     * Gets a random cell from the region and returns it
     */
    public SatogaeriCell getRandomCell(){
        int index = (int)(Math.random() * this.cells.size());
        return this.cells.get(index);
    }


    //public static SatogaeriRegion getSmallestRegion(){
    //    Collections.sort(regions);
    //    return regions.get(0);
    //}



    //@Override
    //public int compareTo(Object o) {
    //    SatogaeriRegion r = (SatogaeriRegion) o;
    //    return (this.cells.size() < r.cells.size()) ? -1: (this.cells.size() > r.cells.size()) ? 1:0 ;        
    //}


}