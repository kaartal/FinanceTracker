package financeapp;
import javax.swing.*;
public class  Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Finance Tracker");
        FinanceTrackerForm form = new FinanceTrackerForm();
        frame.setContentPane(form.getMainPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
