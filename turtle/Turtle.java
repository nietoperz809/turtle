/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package turtle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.swing.JPanel;

/**
 *
 * @author Administrator
 */
public class Turtle extends JPanel
{
    /**
     * Factor to make radians from degrees
     */
    static final double DEGFACTOR = (2 * Math.PI) / 360.0;
    /**
     * Handles and applies Rules
     */
    RuleManager rules = new RuleManager();
    /**
     * Current turtle position x/y
     */
    private Point currentTurtlePosition;
    /**
     * Current angle
     */
    private double currentAngle;
    /**
     * Current command that is executed
     */
    private int linenumber;
    /**
     * Holds all commands
     */
    private ArrayList<Command> commands = new ArrayList<>();
    /**
     * Stack for positions and angles
     */
    private final Stack<StackElement> stack = new Stack<>();
    /**
     * Background Color
     */
    Color penColor = Color.BLACK;

    /**
     * LSystem drawing variables
     */
    double fixangle;
    double fixstep;
    Point multiplicator;
    int pensize;
    public DoublePoint minmax = new DoublePoint();
    
    public class DoublePoint
    {
        final Point Min;
        final Point Max;

        public DoublePoint()
        {
            Min = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
            Max = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
        }
        
        public DoublePoint(DoublePoint src)
        {
            Min = new Point(src.Min);
            Max = new Point(src.Max);
        }
        
        @Override
        public String toString()
        {
            return "Min: " + Min + "/Max: " + Max;
        }

        void addPoint(Point pt)
        {
            if (pt.x < Min.x)
            {
                Min.x = pt.x;
            }
            if (pt.y < Min.y)
            {
                Min.y = pt.y;
            }
            if (pt.x > Max.x)
            {
                Max.x = pt.x;
            }
            if (pt.y > Max.y)
            {
                Max.y = pt.y;
            }
        }
    }

    class LindeResult
    {
        public DoublePoint minmax;
        public String result;

        LindeResult(DoublePoint p, String s)
        {
            minmax = p;
            result = s;
        }
    }

//    static public Turtle fromString(String s)
//    {
//        Turtle t = new Turtle();
//        t.commands.append(s);
//        return t;
//    }
    /**
     * Constructor that sets Frame size
     *
     * @param width Frame width
     * @param height Frame height
     */
    public Turtle(int width, int height)
    {
        this.setBackground(Color.WHITE);
        this.setPreferredSize(new Dimension(width, height));
    }

    /**
     * Constructor that sets frame size to default values
     */
    public Turtle()
    {
        this(800, 600);
    }

    /**
     * Draws by using x/y as offsets to the current position
     *
     * @param x X
     * @param y Y
     */
    public void CmdDrawToRelative(int x, int y)
    {
        commands.add(Command.create(Command.CMD.draw, x,y));
    }

    /**
     * Draws by using x/y as next position
     *
     * @param x X
     * @param y Y
     */
    public void CmdDrawToAbsolute(int x, int y)
    {
        commands.add(Command.create(Command.CMD.drawabs, x,y));
    }

    /**
     * Calls CmdDrawToRelative but using a point
     *
     * @param pt the Point
     */
    public void CmdDrawToRelative(Point pt)
    {
        Turtle.this.CmdDrawToRelative(pt.x, pt.y);
    }

    /**
     * Set pen to new positions using x/y as offset
     *
     * @param x X
     * @param y Y
     */
    public void CmdSetPenPositionRelative(int x, int y)
    {
        commands.add(Command.create(Command.CMD.move, x,y));
    }

    /**
     * Set pen to new absolute position
     *
     * @param x X
     * @param y Y
     */
    public void CmdSetPenPositionAbsolute(int x, int y)
    {
        commands.add (Command.create(Command.CMD.moveabs, x, y));
    }

    /**
     * Calls CmdSetPenPositionRelative using a Point
     *
     * @param pt the Point
     */
    public void CmdSetPenPositionRelative(Point pt)
    {
        Turtle.this.CmdSetPenPositionRelative(pt.x, pt.y);
    }

    /**
     * Set multiplier e.g. step width
     *
     * @param x Factor to multiply x
     * @param y Factor to multiply y
     */
    public void CmdSetStepFactor(int x, int y)
    {
        commands.add (Command.create(Command.CMD.mult, x, y));
    }

    /**
     * Same as above but using Point
     *
     * @param pt Point that contains both multipliers for x/y
     */
    public void CmdSetStepFactor(Point pt)
    {
        Turtle.this.CmdSetStepFactor(pt.x, pt.y);
    }

    /**
     * Set new line color
     *
     * @param r Red value
     * @param g Green value
     * @param b Blue value
     */
    public void CmdSetPenColor(int r, int g, int b)
    {
        commands.add (Command.create(Command.CMD.color, r, g, b));
    }

    /**
     * Same as above but uses color object
     *
     * @param c The new color
     */
    public void CmdSetColor(Color c)
    {
        CmdSetPenColor(c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Resets the turtle e.g. moves pen to default position
     */
    public void CmdReset()
    {
        commands.add (Command.create(Command.CMD.reset));
    }

    /**
     * Draws forward by using distance d and stored angle
     *
     * @param d Distance to draw to
     */
    public void CmdDrawForward(int d)
    {
        commands.add (Command.create(Command.CMD.F, d));
    }

    /**
     * Moves pen forward using distance d and stored angle
     *
     * @param d Distance to move to
     */
    public void CmdMoveForward(int d)
    {
        commands.add (Command.create(Command.CMD.f, d));
    }

    /**
     * Draws backwards by using distance d and stored angle
     *
     * @param d Distance to draw to
     */
    public void CmdDrawBackward(int d)
    {
        commands.add (Command.create(Command.CMD.R, d));
    }

    /**
     * Moves pen backwards using distance d and stored angle
     *
     * @param d Distance to move to
     */
    public void CmdMoveBackward(int d)
    {
        commands.add (Command.create(Command.CMD.r, d));
    }

    /**
     * Turns write direction left
     *
     * @param d Angle to turn
     */
    public void CmdRotateLeft(double d)
    {
        commands.add (Command.create(Command.CMD.plus, d));
    }

    /**
     * Turns write direction right
     *
     * @param d Angle to turn
     */
    public void CmdRotateRight(double d)
    {
        commands.add (Command.create(Command.CMD.minus, d));
    }

    /**
     * Sets new pen size
     *
     * @param p new pen size
     */
    public void CmdSetPenSize(int p)
    {
        commands.add (Command.create(Command.CMD.pensize, p));
    }

    /**
     * Stores current position and angle on Stack
     */
    public void CmdPush()
    {
        commands.add (Command.create(Command.CMD.push));
    }

    /**
     * Restore current position and angle from stack.
     */
    public void CmdPop()
    {
        commands.add (Command.create(Command.CMD.pop));
    }

    /**
     * Draws a Lindenmayer structure that was previously generated using other
     * functions
     */
    public void CmdLindeDraw()
    {
        commands.add (Command.create(Command.CMD.lindedraw));
    }

    /**
     * Set Lindenmayer start axiom using {F,-,+} and constants
     *
     * @param ax Axiom as String eg FF+F-F-F
     */
    public void CmdSetLindeAxiom(String ax)
    {
        commands.add (Command.create(Command.CMD.lindeaxiom, ax));
    }

    /**
     * Sets Lindenmayer replacement rule of the form a->b This means that a is
     * replaced by b -> serves as separator More the one rules can be set
     * simultaneously
     *
     * @param r Lindenmayer Rule as sting
     * @param probability 1.0 -> Rule applies always, 0.0 -> never
     */
    public void CmdSetLindeRule(String r, double probability)
    {
        commands.add (Command.create(Command.CMD.linderule, r, probability));
    }

    public void CmdSetLindeRule(String r)
    {
        Turtle.this.CmdSetLindeRule(r, 1.0);
    }

    /**
     * Sets final replacement rule of the form a->b Same as above but this rule
     * is only applied as final step -> serves as separator More the one rules
     * can be set simultaneously
     *
     * @param r Final Rule as sting
     * @param probability 1.0 -> Rule applies always, 0.0 -> never
     */
    public void CmdSetLindeFinalRule(String r, double probability)
    {
        commands.add (Command.create(Command.CMD.finalrule, r, probability));
    }

    public void CmdSetLindeFinalRule(String r)
    {
        Turtle.this.CmdSetLindeFinalRule(r, 1.0);
    }

    /**
     * Set number of recursions (that is, how many times the rules are applied)
     * The default number is 1 if this function is never called
     *
     * @param rec
     */
    public void CmdSetLindeRecursionNumber(int rec)
    {
        commands.add (Command.create(Command.CMD.linderec, rec));
    }

    /**
     * Sets the angle that is used when + or - is executed The default value is
     * 0.0 if this function is never called
     *
     * @param d the Angle in Degrees
     */
    public void CmdSetLindeAngle(double d)
    {
        commands.add (Command.create(Command.CMD.lindeangle, d));
    }

    /**
     * Set the length of as step when F or f are executed The default value is 1
     *
     * @param n The step width
     */
    public void CmdSetLindeLineLength(double n)
    {
        commands.add (Command.create(Command.CMD.lindestep, n));
    }

    public LindeResult getLindeResult() throws Exception
    {
        return new LindeResult(minmax, rules.getResult());
    }

    public List<Command> getCommandList()
    {
        return new ArrayList<>(commands);
    }

    public void setCommandList(List<Command> list)
    {
        commands = new ArrayList<>(list);
    }

    @Override
    public Graphics getGraphics()
    {
        return super.getGraphics();
    }

    /**
     * Pushes current values (position and angle) onto stack
     */
    private void push()
    {
        stack.push(new StackElement(currentAngle, currentTurtlePosition));
    }

    /**
     * Pops new Values (position and angle) from stack
     *
     * @throws Exception
     */
    private void pop() throws Exception
    {
        StackElement e = stack.pop();
        currentAngle = e.getAngle();
        currentTurtlePosition = e.getPoint();
    }

    /**
     * Returns in * sc if sc is positive, else in/-sc
     *
     * @param in Value
     * @param sc Multiplier
     * @return Result (See above)
     */
    private int applyScaling(int in, int sc)
    {
        if (sc < 0)
        {
            return in / -sc;
        }
        else
        {
            return in * sc;
        }
    }

    /**
     * Calculates new point from Data using polar coordinates
     *
     * @param in Input point
     * @param distance Distance
     * @param angle Angle
     * @param mult Distance Multiplier
     * @param reverse 1 means forward, -1 means backwards
     * @return The new point
     */
    private Point newPoint(Point in, double distance, double angle, int reverse)
    {
        Point n = new Point();
        angle *= DEGFACTOR;
        n.x = in.x + reverse * applyScaling((int) (distance * Math.cos(angle)), multiplicator.x);
        n.y = in.y + reverse * applyScaling((int) (distance * Math.sin(angle)), multiplicator.y);
        return n;
    }

    /**
     * Resembles Graphics.drawline with thickness
     *
     * @param g Graphics context
     * @param x1 X from
     * @param y1 Y from
     * @param x2 X to
     * @param y2 Y to
     */
    protected void drawLine(Graphics g, Point from, Point to) //int x1, int y1, int x2, int y2)
    {
        if (pensize == 0)
        {
            g.drawLine(from.x, from.y, to.x, to.y);
            return;
        }
        int dX = to.x - from.x;
        int dY = to.y - from.y;
        double linelength = Math.sqrt(dX * dX + dY * dY);
        double scale = (pensize) / (2 * linelength);
        double ddx = -scale * dY;
        double ddy = scale * dX;
        ddx += (ddx > 0) ? 0.5 : -0.5;
        ddy += (ddy > 0) ? 0.5 : -0.5;
        int dx = (int) ddx;
        int dy = (int) ddy;
        int xPoints[] = new int[4];
        int yPoints[] = new int[4];
        xPoints[0] = from.x + dx;
        yPoints[0] = from.y + dy;
        xPoints[1] = from.x - dx;
        yPoints[1] = from.y - dy;
        xPoints[2] = to.x - dx;
        yPoints[2] = to.y - dy;
        xPoints[3] = to.x + dx;
        yPoints[3] = to.y + dy;
        g.fillPolygon(xPoints, yPoints, 4);
    }

    /**
     * Executes one draw operation and updates Point to the next position
     *
     * @param g Graphics context
     * @param pos Current position
     * @param angle Angle
     * @param mult Distance Multiplier
     * @param pensize Pen Size
     * @param distance Distance
     * @param cmd F or R that means Forward or Reverse
     */
    private void doDraw(Graphics g, Point pos, double angle, double distance, int cmd)
    {
        Point p = newPoint(pos, distance, angle, cmd);
        if (g instanceof FakeGraphics)
        {
            minmax.addPoint(p);
        }
        drawLine(g, pos, p);
        pos.x = p.x;
        pos.y = p.y;
    }

    /**
     *
     * @param g
     * @throws Exception
     */
    public void execute(Graphics g) throws Exception
    {
    }

    /**
     * Draws Lindenmayer System that is in a String
     *
     * @param g1 Graphics context to draw to
     * @param lsystem
     * @throws Exception When stack underflow occurs
     */
    public void drawLindenmayerSystem(Graphics g1, String lsystem) throws Exception
    {
        for (int s = 0; s < lsystem.length(); s++)
        {
            char c = lsystem.charAt(s);
            switch (c)
            {
                case '/':
                    fixstep /= 2;
                    if (fixstep < 1)
                    {
                        fixstep = 1;
                    }
                    break;

                case '*':
                    fixstep *= 2;
                    break;

                case '[':
                    push();
                    break;

                case ']':
                    pop();
                    break;

                case 'F':
                    doDraw(g1, currentTurtlePosition, currentAngle, fixstep, 1);
                    break;

                case 'R':
                    doDraw(g1, currentTurtlePosition, currentAngle, fixstep, -1);
                    break;

                case 'f':
                    currentTurtlePosition = newPoint(currentTurtlePosition, fixstep, currentAngle, 1);
                    break;

                case 'r':
                    currentTurtlePosition = newPoint(currentTurtlePosition, fixstep, currentAngle, -1);
                    break;

                case '+':
                    currentAngle -= fixangle % 360;
                    break;

                case '-':
                    currentAngle += fixangle % 360;
                    break;
            }
        }
    }

    /**
     * Does all the drawing based on ArrayList with commands
     *
     * @throws Exception Ex. that can occurs
     * @param g Graphics Context
     */
    void drawTurtleSteps(Graphics g1) throws Exception
    {
        //Dimension dim = this.getSize();
        currentTurtlePosition = new Point(0, 0);
        g1.setColor(penColor);
        Point temp = new Point();
        linenumber = 0;
        currentAngle = -90.0;
        fixangle = 90.0;
        fixstep = 10.0;
        multiplicator = new Point(1, 1);
        pensize = 0;

        for (Command cmd : commands)
        {
            linenumber++;
            switch (cmd.command)
            {
                case lindeangle:
                    fixangle = (Double) cmd.n1;
                    break;

                case lindestep:
                    fixstep = (Double) cmd.n1;
                    break;

                case lindeaxiom:
                    rules.setAxiom((String) cmd.n1);
                    break;

                case linderule:
                    rules.setRule((String) cmd.n1, (double) cmd.n2);
                    break;

                case finalrule:
                    rules.setFinalRule((String) cmd.n1, (double) cmd.n2);
                    break;

                case linderec:
                    rules.setRecursions((Integer) cmd.n1);
                    break;

                case lindedraw:
                    drawLindenmayerSystem(g1, rules.getResult());
                    break;

                case pensize:
                    pensize = (Integer) cmd.n1;
                    break;

                case push:
                    push();
                    break;

                case pop:
                    pop();
                    break;

                case reset:
                    multiplicator = new Point(1, 1);
                    //abs = false;
                    currentTurtlePosition = new Point(0,0);
                    currentAngle = 0.0;
                    g1.setColor(penColor);
                    break;

                case R:  // Draw Backwards
                    doDraw(g1, currentTurtlePosition, currentAngle, (double) cmd.n1, -1);
                    break;

                case F:  // Draw Forward
                    doDraw(g1, currentTurtlePosition, currentAngle, (double) cmd.n1, 1);
                    break;

                case r:  // Go Back
                    currentTurtlePosition = newPoint(currentTurtlePosition, (double) cmd.n1,
                            currentAngle, -1);
                    break;

                case f:  // Go Forward
                    currentTurtlePosition = newPoint(currentTurtlePosition, (double) cmd.n1,
                            currentAngle, 1);
                    break;

                case plus:
                    currentAngle -= (double) cmd.n1 % 360;
                    break;

                case minus:
                    currentAngle += (double) cmd.n1 % 360;
                    break;

                case color:
                    penColor = new Color((Integer) cmd.n1, (Integer) cmd.n2, (Integer) cmd.n3);
                    g1.setColor(penColor);
                    break;

                case mult:
                    multiplicator.x = (Integer) cmd.n1;
                    multiplicator.y = (Integer) cmd.n2;
                    break;

                case moveabs:
                case drawabs:
                    temp.x = applyScaling((Integer) cmd.n1, multiplicator.x);
                    temp.y = applyScaling((Integer) cmd.n2, multiplicator.y);
                    if (cmd.command == Command.CMD.drawabs)
                    {
                        drawLine(g1, currentTurtlePosition, temp);
                    }
                    currentTurtlePosition.x = temp.x;
                    currentTurtlePosition.y = temp.y;
                    break;

                case move:
                case draw:
                    temp.x = currentTurtlePosition.x + applyScaling((Integer) cmd.n1, multiplicator.x);
                    temp.y = currentTurtlePosition.y + applyScaling((Integer) cmd.n2, multiplicator.y);
                    if (cmd.command == Command.CMD.draw)
                    {
                        drawLine(g1, currentTurtlePosition, temp);
                    }
                    currentTurtlePosition.x = temp.x;
                    currentTurtlePosition.y = temp.y;
                    break;
            }
        }
    }

    /**
     * Overridden paint function Draws turtle graphics
     *
     * @param g Graphics context
     */
    @Override
    public void paint(Graphics g)
    {
        try
        {
            drawTurtleSteps(g);
        }
        catch (Exception ex)
        {
            System.exit(-1);
        }
    }

    /**
     * Empty update function to prevent senseless canvas repaints
     *
     * @param g
     */
    @Override
    public void update(Graphics g)
    {
        //paint (g);
    }

    /**
     * Gets command list as human readable strings TODO add useful content
     *
     * @return
     */
    @Override
    public String toString()
    {
        return commands.toString();
    }
}
