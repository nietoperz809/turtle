/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package turtle;

import com.pdfjet.Cap;
import com.pdfjet.Font;
import com.pdfjet.Letter;
import com.pdfjet.PDF;
import com.pdfjet.Page;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class HackedPage extends Page
{
    int hash1 = "This document was created with the evaluation version of PDFjet".hashCode();
    int hash2 = "To acquire a license please visit http://pdfjet.com".hashCode();

    public HackedPage(PDF pdf, float[] floats) throws Exception
    {
        super(pdf, floats);
    }

    @Override
    public void drawString(Font font, String s, float f, float f1) throws IOException
    {
        if (s.hashCode() == hash1 || s.hashCode() == hash2)
        {
            return;
        }
        super.drawString(font, s, f, f1);
    }
}

/**
 *
 * @author Administrator
 */
public class PdfTurtle extends Turtle
{
    PDF pdf;
    Page page;
    String filename;
    FileOutputStream fos;
    PdfGraphics graph;

    PdfTurtle(int width, int height)
    {
        super(width, height);
        filename = "c:\\Example_12.pdf";
        try
        {
            fos = new FileOutputStream(filename);

            pdf = new PDF(fos);
            page = new HackedPage(pdf, Letter.PORTRAIT);
            graph = new PdfGraphics(page);
        }
        catch (Exception ex)
        {
            System.err.println("PdfTurtle Constructor failed");
        }
    }

//    @Override
//    protected void drawLine (Graphics g, int x1, int y1, int x2, int y2, int thickness)
//    {
//        Line line = new Line (x1, y1, x2, y2);
//        try
//        {
//            line.drawOn(page);
//        }
//        catch (Exception ex)
//        {
//            System.err.println ("drawLine failed");
//        }
//    }
    @Override
    public void paint(Graphics g)
    {

    }

    @Override
    public Graphics getGraphics()
    {
        return graph;
    }
    
    /**
     * Must be called at end of drawing/moving the turtle
     *
     * @throws Exception
     */
    @Override
    public void execute(Graphics g) throws Exception
    {
        if (g instanceof FakeGraphics)
        {
            super.drawTurtleSteps(g);
            return;
        }
        page.setLinePattern("[] 0");
        page.setPenColor(Color.BLACK.getRGB());
        page.setPenWidth(1);
        page.setLineCapStyle(Cap.BUTT);
        super.drawTurtleSteps(g);
        page.strokePath();
        
        pdf.flush();
        fos.close();

        Desktop.getDesktop().open(new File(filename));
    }
}
