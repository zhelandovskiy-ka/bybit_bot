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

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ConcurrentModificationException;

@Component
public class ChartForm {
    private final InstrumentService instrumentServiceOld;
    private JFreeChart chartMain;
    private XYSeries linePrice;
    private XYPlot plot;
    private NumberAxis yAxis;
    private String currentInstrument;
    private ChartPanel chartMainPanel;

    public ChartForm(InstrumentService instrumentServiceOld) {
        this.instrumentServiceOld = instrumentServiceOld;
    }

    public void display() {
        JFrame frame = new JFrame("ByBit Trading Bot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panelList = createListPanel();
        JPanel panelCharts = createChartsPanel();

        JPanel panelMain = new JPanel();
        panelMain.setLayout(new BorderLayout());
        frame.setContentPane(panelMain);

        panelMain.add(panelList, BorderLayout.WEST);
        panelMain.add(panelCharts, BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setSize(950, 500);
        frame.setVisible(true);
    }

    private JPanel createListPanel() {
        JList<Object> list = new JList<>(instrumentServiceOld.getSymbolList().toArray());
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                currentInstrument = (String) list.getSelectedValue();
                drawChart();
            }
        });

        return new JPanel(new BorderLayout()) {{
            add(new JScrollPane(list), BorderLayout.CENTER);
        }};
    }

    private JPanel createChartsPanel() {
        chartMainPanel = createChart();

        return new JPanel(new BorderLayout()) {{
            add(chartMainPanel, BorderLayout.CENTER);
        }};
    }

    private ChartPanel createChart() {
        //инициализация трех линий для графика
        linePrice = new XYSeries("name");

        //создание датасета и добавление линий в него
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(linePrice);

        //создание графика
        chartMain = ChartFactory.createXYLineChart(
                "Graphic ",
                "time",
                "price",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

       yAxis = new NumberAxis();

        plot = chartMain.getXYPlot();
        plot.getRendererForDataset(dataset).setSeriesPaint(2, Color.GREEN);
        plot.getRendererForDataset(dataset).setSeriesPaint(3, Color.MAGENTA);

        return new ChartPanel(chartMain) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(480, 240);
            }
        };
    }

    public void drawChart() {
        Instrument instrument = instrumentServiceOld.getInstrumentByName(currentInstrument);

        linePrice.clear();

        linePrice.setKey("price close");
        chartMain.setTitle("Graphic " + currentInstrument);

        int size = instrument.getCandlestickList().size();
        int pos = 0;

        System.out.println("getCandlestickList().size() " + size);
        //заполнение линии текущей цены
        for (int i = 59; i >= 0; i--) {
            linePrice.add(++pos, instrument.getCandlestickList().get(i).getPriceClose());
        }

        plot.setRangeAxis(yAxis);

        //вычисление небольшого отступа от верха и от низа графика
        double min = instrument.getMinPrice();
        double max = instrument.getMaxPrice();

        double rangeOut;
        if (min == max)
            rangeOut = max * 0.1;
        else
            rangeOut = (max - min) * 0.1;

        double lower = min - rangeOut;
        double upper = max + rangeOut;

        plot.getRangeAxis().setRange(lower, upper);
    }

    public void drawAndMakeScreen(String instrumentName) {
        currentInstrument = instrumentName;

        try {
            drawChart();
//            drawChartStochastic();
//            saveWindowScreenshot();
            int res = saveChartToJpeg();
            if (res == 0)
                saveChartToJpeg();
        } catch (ConcurrentModificationException e) {
            System.err.println(e.getMessage());
        }
    }

    public int saveChartToJpeg() {
        File file1 = new File("chart1.jpg");
//        File file2 = new File("chart2.jpg");
        try {
            ChartUtils.saveChartAsJPEG(file1, chartMain, chartMainPanel.getWidth(), chartMainPanel.getHeight());
//            ChartUtils.saveChartAsJPEG(file2, chartStochastic, stochasticPanel.getWidth(), stochasticPanel.getHeight());

//            Utilits.mergeImage(file1, file2);
            return 1;
        } catch (IOException | ConcurrentModificationException | IllegalStateException | IndexOutOfBoundsException |
                 NullPointerException e) {
            System.out.println("error 180 | TradingChart | saveChartToJpeg");
            e.printStackTrace();
            return 0;
        }
    }
}
