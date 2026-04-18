package coganh;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseControl implements MouseListener {

    GamePanel_CoGanh gp;

    public MouseControl(GamePanel_CoGanh gp) {
        this.gp = gp;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        // Trỏ sang gc thay vì dl như ngày xưa
        if (gp.gc != null) {
            gp.gc.xuLyChuot(x, y);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

}