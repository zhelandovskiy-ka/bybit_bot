package ru.ka_zhelandovskiy.bybit_bot.view;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.stereotype.Component;
import ru.ka_zhelandovskiy.bybit_bot.dto.Instrument;
import ru.ka_zhelandovskiy.bybit_bot.services.InstrumentService;
import ru.ka_zhelandovskiy.bybit_bot.utils.Utilities;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ConcurrentModificationException;

@Component
public class NotForm {
    private JLabel label;
    private JLabel labelProfit;

    public void display() {
        JFrame frame = new JFrame("ByBit Trading Bot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(Color.BLACK);

        JPanel panelMain = new JPanel();
        panelMain.setLayout(new BorderLayout());
        panelMain.setBackground(Color.BLACK);
        frame.setContentPane(panelMain);

        label = new JLabel();
        label.setFont(new Font("Arial", Font.BOLD, 300));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        labelProfit = new JLabel();
        labelProfit.setFont(new Font("Arial", Font.BOLD, 50));
        labelProfit.setHorizontalAlignment(SwingConstants.CENTER);

        panelMain.add(label, BorderLayout.CENTER);
        panelMain.add(labelProfit, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setSize(950, 500);
        frame.setVisible(true);
    }

    public void setLabelText(double sum) {
        double oldPrice = 0;

        if (!label.getText().isEmpty())
            oldPrice = Double.parseDouble(label.getText().substring(0, label.getText().length() - 1));

        if (sum < oldPrice) {
            label.setForeground(Color.RED);
        }
        if (sum > oldPrice) {
            label.setForeground(Color.GREEN);
        }
        if (sum == oldPrice) {
            label.setForeground(Color.GRAY);
        }

        double firstSum = 73;
        double profit = Utilities.roundDouble((sum - firstSum) / firstSum * 100);

        if (profit < firstSum) {
            labelProfit.setForeground(Color.RED);
        }
        if (profit > firstSum) {
            labelProfit.setForeground(Color.GREEN);
        }
        if (profit == firstSum) {
            labelProfit.setForeground(Color.GRAY);
        }

        this.label.setText(sum + "$");
        this.labelProfit.setText(profit + "%");
    }
}
