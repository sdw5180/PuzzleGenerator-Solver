'''
Satogaeri solver.
More or less brute force, with hooks and some data structures 
to add intelligence.
See main method for puzzle file format.
'''
__author__ = 'zjb'
from copy import deepcopy
from collections import namedtuple
from math import copysign
import sys

class Circle:
    # for each circle in the puzzle, we keep its location, value,
    # a list of possible destinations, and whether it has moved yet.
    __slots__ = ('r','c','val','dests','done')
    def __init__(self,r,c,v):
        self.r = r
        self.c = c
        self.val = v
        self.dests = []
        self.done = False

class Region:
    # this stores the squares that make up a region, as well as
    # which circles can reach it (but the solver doesn't use this)
    __slots__ = ('num','locs','canreach','filled')
    def __init__(self,num):
        self.num = num
        self.locs = []
        self.canreach = set()
        self.filled = False

Puzzle = namedtuple('Puzzle',['ht','wd','rgrid','cgrid'])

SolveState = namedtuple('SolveState',['grid','circs','regs'])


def try_hop(grid,row,col,dist,dr,dc):
    # can a circle go in the indicated direction for the given amount
    if row + dist*dr < 0 or row + dist*dr >= len(grid) or col + dist*dc < 0 or col + dist*dc >= len(grid[0]):
        return False
    r = row
    c = col
    steps = 0
    while steps < dist:
        r += dr
        c += dc
        if grid[r][c] != '.':
            return False
        steps += 1
    return True

def try_line(grid,row,col,dr,dc):
    # what cells in the given direction can the circle reach?
    r = row
    c = col
    stops = []
    while True:
        r += dr
        c += dc
        if r < 0 or r >= len(grid) or c < 0 or c >= len(grid[0]):
            break
        if grid[r][c] == '.':
            stops.append((r,c))
        else:
            break
    return stops

def init_puzzle(wd,ht,rs,grid,cvgrid,rgrid):

    regs = []
    for i in range(rs):
        regs.append(Region(i))

    # cvgrid (incoming) has the values of each circle, or -2 if none
    # cgrid will hold the indices of the circle object, or -2 if none
    cgrid = deepcopy(cvgrid)

    circs = []
    for row in range(ht):
        for col in range(wd):
            cval = cvgrid[row][col]
            if cval >= -1:
                c = Circle(row,col,cvgrid[row][col])
                # separate cases: zero, >0, empty circle
                if cval == 0:
                    # region is satisfied!
                    regs[rgrid[row][col]].filled = True
                    c.done = True
                    grid[row][col] = '*'
                elif cval > 0:
                    if try_hop(grid,row,col,cval,0,1):
                        c.dests.append((row,col+cval))
                    if try_hop(grid,row,col,cval,0,-1):
                        c.dests.append((row,col-cval))
                    if try_hop(grid,row,col,cval,1,0):
                        c.dests.append((row+cval,col))
                    if try_hop(grid,row,col,cval,-1,0):
                        c.dests.append((row-cval,col))
                elif cval == -1:
                    # can go nowhere
                    c.dests.append((row,col))
                    # or can go in any of the four directions
                    c.dests.extend(try_line(grid,row,col,0,1))
                    c.dests.extend(try_line(grid,row,col,1,0))
                    c.dests.extend(try_line(grid,row,col,0,-1))
                    c.dests.extend(try_line(grid,row,col,-1,0))
                cgrid[row][col] = len(circs)
                circs.append(c)
                for d in c.dests:
                    dreg = rgrid[d[0]][d[1]]
                    regs[dreg].canreach.add((cgrid[row][col],d))
    if len(circs) != len(regs):
        print('Abort mission!',len(circs),'circles and',len(regs),'regions!')
        return None

    return Puzzle(ht,wd,rgrid,cgrid), SolveState(grid,circs,regs)

def draw_line(puz,state,r,c,rto,cto):
    '''
    destructively draws a line from (r,c) to (rto,cto)
    Does not check contents, so better test first...
    Does not consider the effects of the line on other circles' 
    potential lines...
    '''
    #print('drawing from ',r,c,'to',rto,cto)
    initr = r
    initc = c
    if r != rto or c != cto:
        dr = 0 if r == rto else int(copysign(1,rto-r))
        dc = 0 if c == cto else int(copysign(1,cto-c))
        if dr == 0: # across
            while c != cto:
                c += dc
                state.grid[r][c] = '-'
        else: #up/down
            while r != rto:
                r += dr
                state.grid[r][c] = '|'
    # now that region is satisfied
    satis = puz.rgrid[rto][cto]
    state.regs[satis].filled = True

    # change the grid at source and destination
    if initr == rto and initc == cto:
        state.grid[rto][cto] = '*'
    else:
        state.grid[rto][cto] = state.grid[initr][initc]
        if rto > initr:
            state.grid[initr][initc] = 'v'
        elif rto < initr:
            state.grid[initr][initc] = '^'
        elif cto < initc:
            state.grid[initr][initc] = '<'
        else:
            state.grid[initr][initc] = '>'

def solve(puz,state):
    '''
    Solve the given puzzle from the given state.
    Just brute force, checking un-done circles from top down
    '''
    # intelligent human-like deductions would go here

    # now we find the next circle to try, here brute-force but
    # that is not a great idea in the long run...
    # selection could be MRV or human-like
    nextcirc = None
    for circ in state.circs:
        if circ.done:
            continue
        nextcirc = circ
        break
    if not nextcirc: # yay!
        print('Not branching any more!')
        return state
    #print("Branching on ",nextcirc.r,nextcirc.c,"# options",len(nextcirc.dests))

    # found a circle, now try all options
    for d in nextcirc.dests:
        # since we didn't pre-emptively remove destinations when drawing
        # previous lines, we have to check it now
        # this is ugly since I am reverse-engineering an intelligent solution
        dr = d[0]-nextcirc.r
        dc = d[1]-nextcirc.c
        dist = abs(dr)+abs(dc)
        dr = int(copysign(1,dr)) if dr != 0 else 0
        dc = int(copysign(1,dc)) if dc != 0 else 0
        # nothing in the way
        if not try_hop(state.grid,nextcirc.r,nextcirc.c,dist,dr,dc):                      
            continue
        # is the region we land in already filled?
        landing = puz.rgrid[d[0]][d[1]]
        if state.regs[landing].filled:
            continue
        # ok, this line is presently OK
        newstate = deepcopy(state)
        cnum = puz.cgrid[nextcirc.r][nextcirc.c]
        #print('Branching on circle',cnum,'at',nextcirc.r,nextcirc.c,'to',d)
        # set it to be done and region to be filled
        newstate.circs[cnum].done = True
        newstate.circs[cnum].dests = [d]
        draw_line(puz,newstate,nextcirc.r,nextcirc.c,d[0],d[1])
        newstate.regs[landing].filled = True
        # now recursively solve
        result = solve(puz,newstate)
        if result:
            return result
    # none of those worked, fail (bad decision higher in the tree)
    return None

def print_grid(grid):
    for row in grid:
        for ch in row:
            print(ch,end=' ')
        print()
    print()

def main():
    with open(sys.argv[1]) as f:
        # first line of file is width height #regions
        sizeln = f.readline().strip().split()
        wd = int(sizeln[0])
        ht = int(sizeln[1])
        rs = int(sizeln[2])
        # circles in grid
        # . for empty, O [letter O, not zero!] for empty circle (w/o number), else int (including maybe zero!)
        # cgrid will hold that info but as ints (-2, -1, given#)
        cgrid = []
        grid = [] # nicer for looking at and keeping track of circle movement, all chars
        for r in range(ht):
            row = f.readline().strip().split()
            grid.append(row)
            crow = [-2 if sp == '.' else -1 if sp == 'O' else int(sp) for sp in row]
            cgrid.append(crow)
        # region grid, starting with region 1(!?)
        rgrid = []
        # for each region, list of (row,col) pairs
        # just create the list here, region objects will get made/filled in init_puzzle
        rlocs = [[] for _ in range(rs)]
        for r in range(ht):
            row = f.readline().strip().split()
            rrow = []
            col = 0
            for sp in row:
                reg = int(sp)-1
                rrow.append(reg)
                rlocs[reg].append((r,col))
                col += 1
            rgrid.append(rrow)
    puz, init_state = init_puzzle(wd,ht,rs,grid,cgrid,rgrid)
    print_grid(init_state.grid)
    solution = solve(puz,init_state)
    if solution:
        print_grid(solution.grid)
    else:
        print("No solution found")
        
if __name__ == '__main__':
    main()
