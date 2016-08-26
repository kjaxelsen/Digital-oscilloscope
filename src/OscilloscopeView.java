/**
 * Created by Kevin on 29/03/16.
 * View window
 */
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import org.jfree.ui.RectangleEdge;


@SuppressWarnings("All")
public class OscilloscopeView extends JFrame implements ChartMouseListener {
    Container mainWindow;
    JLabel channels, trigger, functionGenerator, measurements;
    JButton reArm, force;
    GridBagConstraints gbc = new GridBagConstraints();
    Border blackline = BorderFactory.createLineBorder(Color.black, 2);
    SpinnerModel smnumSamples = new SpinnerNumberModel(25000, 25000, 50000, 100);
    SpinnerModel smChOff = new SpinnerNumberModel(0, -4500, 4500, 1);
    SpinnerModel smThresh = new SpinnerNumberModel(10, 0, 1000, 1);
    SpinnerModel smFreq = new SpinnerNumberModel(10, 0, 1000, 1);
    SpinnerModel smP2p = new SpinnerNumberModel(10, 0, 1000, 1);
    SpinnerModel smFGOff = new SpinnerNumberModel(0, -2500, 2500, 1);
    XYSeries xyseries = new XYSeries("XYGraph");
    XYSeries altera = new XYSeries("Channel A");
    XYSeries alterb = new XYSeries("Channel B");
    XYSeries alterm = new XYSeries("Math Channel");
    XYSeries alterf = new XYSeries("Filter Channel");
    JFreeChart chart;
    ChartPanel graphPan;
    XYSeriesCollection dataset = new XYSeriesCollection();
    JButton xyGo;
    //JSpinner yRangeSetter, xRangeSetter;
    NumberAxis timeAxis, voltAxis;
    XYPlot xyPlot;
    JCheckBox chA, chB, chM, chF;
    JLabel maxVoltAM, minVoltAM, avgVoltAM, stdVoltAM, freqVoltAM, maxP2PAM, maxVoltBM, minVoltBM,
            avgVoltBM, stdVoltBM, freqVoltBM, maxP2PBM, maxVoltMM, minVoltMM, avgVoltMM, stdVoltMM, freqVoltMM,
            maxP2PMM, maxVoltFM, minVoltFM, avgVoltFM, stdVoltFM, freqVoltFM, maxP2PFM, netElement,
            trigStatElement, currSampleElement;
    JTextField formulaMth;
    JButton submitFormula, sams;
    JRadioButton mthEn, filtLP, filtBp, bit8, bit12, trigAuto, trigNorm, trigSing,
            trigRising, trigFalling, trigLevel, fgOOn, fgOOff, sineW, squareW,
            triangleW, rampW, randomW;
    JLabel csvFilt;
    JButton browseFilt;
    JButton submitFilt;
    JRadioButton filtEn, coupACA, coupDCA, coupACB, coupDCB;
    JComboBox chanList, yRangeSetter, xRangeSetter;
    JTabbedPane chanMeasurements;
    JPanel chanAMeas, chanBMeas, chanMMeas, chanFMeas;
    private Crosshair xCrosshair;
    private Crosshair yCrosshair;
    Crosshair xTrig, yTrig, xTrig2, yTrig2;
    JButton connecter;
    JSpinner chOffsetNumA, chOffsetNumB, samNo;

    public OscilloscopeView() {
        mainWindow = getContentPane();
        setTitle("Oscilloscope");
        setLayout(new BorderLayout());
        setSize(1800,920);

        JPanel left = new JPanel();
        left.setLayout(new BorderLayout());
        left.setBackground(Color.darkGray);
        mainWindow.add(left, BorderLayout.CENTER);

        JPanel nw = new JPanel();
        nw.setLayout(new BorderLayout());
        nw.setBackground(Color.darkGray);
        left.add(nw, BorderLayout.CENTER);

        //graph
        // Add the series to your data set
        dataset.addSeries(altera);
        dataset.addSeries(alterb);
        dataset.addSeries(alterm);
        dataset.addSeries(alterf);


        // Generate the graph
        chart = ChartFactory.createXYLineChart(
                "", // Title
                "Time (ms)", // x-axis Label
                "Voltage (mV)", // y-axis Label
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Plot Orientation
                true, // Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
        );
        graphPan = new ChartPanel(chart);

        xyPlot = (XYPlot) chart.getPlot();
        timeAxis = (NumberAxis) xyPlot.getDomainAxis();
        timeAxis.setTickUnit(new NumberTickUnit(1));
        timeAxis.setRange(0.0, 12);
        voltAxis = (NumberAxis) xyPlot.getRangeAxis();
        voltAxis.setTickUnit(new NumberTickUnit(20));
        voltAxis.setRange(-100, 100);
        xyPlot.setBackgroundPaint(Color.BLACK);
        graphPan.setMouseWheelEnabled(true);
        graphPan.setMouseZoomable(false);
        CrosshairOverlay crosshairOverlayt = new CrosshairOverlay();
        CrosshairOverlay crosshairOverlayt2 = new CrosshairOverlay();
        this.xTrig = new Crosshair(Double.NaN, Color.RED, new BasicStroke(0f));
        this.yTrig = new Crosshair(Double.NaN, Color.RED, new BasicStroke(0f));
        this.xTrig2 = new Crosshair(Double.NaN, Color.BLUE, new BasicStroke(0f));
        this.yTrig2 = new Crosshair(Double.NaN, Color.BLUE, new BasicStroke(0f));
        crosshairOverlayt.addDomainCrosshair(xTrig);
        crosshairOverlayt.addRangeCrosshair(yTrig);
        crosshairOverlayt2.addDomainCrosshair(xTrig2);
        crosshairOverlayt2.addRangeCrosshair(yTrig2);
        graphPan.addOverlay(crosshairOverlayt);
        graphPan.addOverlay(crosshairOverlayt2);
        this.graphPan.addChartMouseListener(this);
        CrosshairOverlay crosshairOverlay = new CrosshairOverlay();
        this.xCrosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
        this.xCrosshair.setLabelVisible(true);
        this.xCrosshair.setLabelBackgroundPaint(Color.YELLOW);
        this.yCrosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
        this.yCrosshair.setLabelVisible(true);
        this.yCrosshair.setLabelBackgroundPaint(Color.YELLOW);
        crosshairOverlay.addDomainCrosshair(xCrosshair);
        crosshairOverlay.addRangeCrosshair(yCrosshair);
        graphPan.addOverlay(crosshairOverlay);
        graphPan.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                graphPan.setMaximumDrawHeight(e.getComponent().getHeight());
                graphPan.setMaximumDrawWidth(e.getComponent().getWidth());
                graphPan.setMinimumDrawWidth(e.getComponent().getWidth());
                graphPan.setMinimumDrawHeight(e.getComponent().getHeight());
            }
        });
        nw.add(graphPan, BorderLayout.CENTER);
        graphPan.validate();

        JPanel sw = new JPanel();
        sw.setBackground(Color.lightGray);
        sw.setLayout(new BorderLayout());
        left.add(sw, BorderLayout.SOUTH);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(Color.yellow);
        mainWindow.add(right, BorderLayout.EAST);

        //Channels
        JPanel channelPanel = new JPanel();
        channelPanel.setLayout(new BorderLayout());
        channelPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
        channelPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, channelPanel.getMinimumSize().height));
        channelPanel.setBackground(Color.lightGray);
        right.add(channelPanel);

        channels = new JLabel("<HTML><U>Channel</U></HTML>");
        channels.setHorizontalAlignment(SwingConstants.CENTER);
        channels.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        channelPanel.add(channels, BorderLayout.NORTH);

        JPanel visibility, couplingOffsetA, couplingOffsetB, couplingA, couplingB,
                chOffsetA, chOffsetB, filteringA;


        //Visibility
        visibility = new JPanel();
        visibility.setLayout(new BoxLayout(visibility, BoxLayout.Y_AXIS));
        TitledBorder visib = BorderFactory.createTitledBorder(blackline, "Visibility");
        visib.setTitleJustification(TitledBorder.CENTER);
        visibility.setBorder(visib);
        visibility.setBorder(new CompoundBorder(visibility.getBorder(), new EmptyBorder(10, 10, 10, 10)));
        channelPanel.add(visibility, BorderLayout.WEST);
        chA = new JCheckBox("A");
        chB = new JCheckBox("B");
        chM = new JCheckBox("Math");
        chF = new JCheckBox("Filter                                     ");
        chA.setSelected(true);
        chB.setSelected(true);
        chM.setSelected(true);
        chF.setSelected(true);
        visibility.add(chA);
        visibility.add(chB);
        visibility.add(chM);
        visibility.add(chF);


        //Coupling and offset
        JTabbedPane coupoff = new JTabbedPane();
        channelPanel.add(coupoff, BorderLayout.EAST);
        //coupoff A
        couplingOffsetA = new JPanel();
        coupoff.addTab("A", null, couplingOffsetA, "Channel A Coupling and Offset");
        couplingOffsetA.setLayout(new BoxLayout(couplingOffsetA, BoxLayout.Y_AXIS));
        couplingOffsetA.setBackground(Color.lightGray);

        //coupling
        couplingA = new JPanel();
        couplingA.setLayout(new BoxLayout(couplingA, BoxLayout.X_AXIS));
        TitledBorder coupA = BorderFactory.createTitledBorder(blackline, "Coupling");
        coupA.setTitleJustification(TitledBorder.CENTER);
        couplingA.setBorder(coupA);
        couplingA.setBorder(new CompoundBorder(couplingA.getBorder(), new EmptyBorder(3, 10, 3, 10)));
        couplingOffsetA.add(couplingA);

        coupACA = new JRadioButton(" AC              ");
        coupDCA = new JRadioButton(" DC             ");
        coupDCA.setSelected(true);
        ButtonGroup coupGroupA = new ButtonGroup();
        coupGroupA.add(coupACA);
        coupGroupA.add(coupDCA);
        couplingA.add(coupACA);
        couplingA.add(coupDCA);



        //offset
        chOffsetA = new JPanel();
        chOffsetA.setLayout(new BoxLayout(chOffsetA, BoxLayout.X_AXIS));
        TitledBorder offA = BorderFactory.createTitledBorder(blackline, "Offset");
        offA.setTitleJustification(TitledBorder.CENTER);
        chOffsetA.setBorder(offA);
        chOffsetA.setBorder(new CompoundBorder(chOffsetA.getBorder(), new EmptyBorder(3, 10, 3, 10)));
        couplingOffsetA.add(chOffsetA);


        chOffsetNumA = new JSpinner(smChOff);
        chOffsetA.add(chOffsetNumA);
        JLabel fgcVoltsA = new JLabel("  mV");
        JButton fgcoGoA = new JButton("Set");
        chOffsetA.add(fgcVoltsA);
        chOffsetA.add(fgcoGoA);

        //filtering band/low
        filteringA = new JPanel();
        filteringA.setLayout(new BoxLayout(filteringA, BoxLayout.X_AXIS));
        TitledBorder filtA = BorderFactory.createTitledBorder(blackline, "Filter");
        filtA.setTitleJustification(TitledBorder.CENTER);
        filteringA.setBorder(filtA);
        filteringA.setBorder(new CompoundBorder(filteringA.getBorder(), new EmptyBorder(3, 11, 3, 11)));
        couplingOffsetA.add(filteringA);

        filtLP = new JRadioButton("Low-Pass");
        filtBp = new JRadioButton("Bandpass");
        filtLP.setSelected(true);
        ButtonGroup filtGroupA = new ButtonGroup();
        filtGroupA.add(filtLP);
        filtGroupA.add(filtBp);
        filteringA.add(filtLP);
        filteringA.add(filtBp);

        //coupoff B
        couplingOffsetB = new JPanel();
        coupoff.addTab("B", null, couplingOffsetB, "Channel B Coupling and Offset");
        couplingOffsetB.setLayout(new BoxLayout(couplingOffsetB, BoxLayout.Y_AXIS));
        couplingOffsetB.setBackground(Color.lightGray);

        //coupling
        couplingB = new JPanel();
        couplingB.setLayout(new BoxLayout(couplingB, BoxLayout.X_AXIS));
        TitledBorder coupB = BorderFactory.createTitledBorder(blackline, "Coupling");
        coupB.setTitleJustification(TitledBorder.CENTER);
        couplingB.setBorder(coupB);
        couplingB.setBorder(new CompoundBorder(couplingB.getBorder(), new EmptyBorder(3, 10, 3, 10)));
        couplingOffsetB.add(couplingB);

        coupACB = new JRadioButton(" AC              ");
        coupDCB = new JRadioButton(" DC             ");
        coupDCB.setSelected(true);
        ButtonGroup coupGroupB = new ButtonGroup();
        coupGroupB.add(coupACB);
        coupGroupB.add(coupDCB);
        couplingB.add(coupACB);
        couplingB.add(coupDCB);

        //offset
        chOffsetB = new JPanel();
        chOffsetB.setLayout(new BoxLayout(chOffsetB, BoxLayout.X_AXIS));
        TitledBorder offB = BorderFactory.createTitledBorder(blackline, "Offset");
        offB.setTitleJustification(TitledBorder.CENTER);
        chOffsetB.setBorder(offB);
        chOffsetB.setBorder(new CompoundBorder(chOffsetB.getBorder(), new EmptyBorder(3, 10, 3, 10)));
        couplingOffsetB.add(chOffsetB);

        chOffsetNumB = new JSpinner(smChOff);
        chOffsetB.add(chOffsetNumB);
        JLabel fgcVoltsB = new JLabel("  mV");
        JButton fgcoGoB = new JButton("Set");
        chOffsetB.add(fgcVoltsB);
        chOffsetB.add(fgcoGoB);

        //Math and Filter Channels
        JPanel mathFilter = new JPanel();
        mathFilter.setLayout(new BorderLayout());
        mathFilter.setBackground(Color.lightGray);
        channelPanel.add(mathFilter, BorderLayout.SOUTH);

        //Math
        JPanel mathChan = new JPanel();
        mathChan.setLayout(new GridLayout(0, 2));
        TitledBorder mthCh = BorderFactory.createTitledBorder(blackline, "Math Channel");
        mthCh.setTitleJustification(TitledBorder.CENTER);
        mathChan.setBorder(mthCh);
        mathChan.setBorder(new CompoundBorder(mathChan.getBorder(), new EmptyBorder(10, 10, 10, 10)));
        mathFilter.add(mathChan, BorderLayout.NORTH);


        formulaMth = new JTextField(15);
        submitFormula = new JButton("Submit");
        mthEn = new JRadioButton("Enabled");
        JRadioButton mthDis = new JRadioButton("Disabled");
        mthDis.setSelected(true);
        ButtonGroup mthStatusGroup = new ButtonGroup();
        mthStatusGroup.add(mthEn);
        mthStatusGroup.add(mthDis);
        mathChan.add(mthEn);
        mathChan.add(mthDis);
        mathChan.add(formulaMth);
        mathChan.add(submitFormula);

        //Filter
        String[] channelStrings = {"Channel A", "Channel B", "Math Channel"};
        JPanel filtChan = new JPanel();
        filtChan.setLayout(new GridBagLayout());
        TitledBorder filCh = BorderFactory.createTitledBorder(blackline, "Filter Channel");
        filCh.setTitleJustification(TitledBorder.CENTER);
        filtChan.setBorder(filCh);
        filtChan.setBorder(new CompoundBorder(filtChan.getBorder(), new EmptyBorder(10, 10, 10, 10)));
        mathFilter.add(filtChan, BorderLayout.SOUTH);

        csvFilt = new JLabel("Select File");
        browseFilt = new JButton("Browse");
        submitFilt = new JButton("Submit");
        filtEn = new JRadioButton("Enabled                                ");
        JRadioButton filtDis = new JRadioButton("Disabled                            ");
        filtDis.setSelected(true);
        ButtonGroup filtStatusGroup = new ButtonGroup();
        filtStatusGroup.add(filtEn);
        filtStatusGroup.add(filtDis);
        chanList = new JComboBox(channelStrings);
        chanList.setSelectedIndex(0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        filtChan.add(filtEn, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        filtChan.add(filtDis, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        filtChan.add(csvFilt, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        filtChan.add(browseFilt, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        filtChan.add(chanList, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        filtChan.add(submitFilt, gbc);

        //Triggers
        JPanel triggerPanel, trigMode, trigType, trigButtons;
        triggerPanel = new JPanel();
        triggerPanel.setLayout(new BorderLayout());
        triggerPanel.setBackground(Color.GRAY);
        triggerPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
        triggerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, triggerPanel.getMinimumSize().height));
        right.add(triggerPanel);

        trigger = new JLabel("<HTML><U>Trigger</U></HTML>");
        trigger.setHorizontalAlignment(SwingConstants.CENTER);
        trigger.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        triggerPanel.add(trigger, BorderLayout.NORTH);

        //Mode
        trigMode = new JPanel();
        trigMode.setLayout(new BoxLayout(trigMode, BoxLayout.Y_AXIS));
        TitledBorder triM = BorderFactory.createTitledBorder(blackline, "Mode");
        triM.setTitleJustification(TitledBorder.CENTER);
        trigMode.setBorder(triM);
        trigMode.setBorder(new CompoundBorder(trigMode.getBorder(), new EmptyBorder(10, 10, 10, 10)));
        triggerPanel.add(trigMode, BorderLayout.WEST);

        trigAuto = new JRadioButton("Auto");
        trigNorm = new JRadioButton("Normal                                  ");
        trigSing = new JRadioButton("Single");
        trigAuto.setSelected(true);
        ButtonGroup trigMGroup = new ButtonGroup();
        trigMGroup.add(trigAuto);
        trigMGroup.add(trigNorm);
        trigMGroup.add(trigSing);
        trigMode.add(trigAuto);
        trigMode.add(trigNorm);
        trigMode.add(trigSing);

        //Type
        trigType = new JPanel();
        trigType.setLayout(new BoxLayout(trigType, BoxLayout.Y_AXIS));
        TitledBorder triT = BorderFactory.createTitledBorder(blackline, "Type");
        triT.setTitleJustification(TitledBorder.CENTER);
        trigType.setBorder(triT);
        trigType.setBorder(new CompoundBorder(trigType.getBorder(), new EmptyBorder(10, 10, 10, 10)));
        triggerPanel.add(trigType, BorderLayout.EAST);

        trigRising = new JRadioButton("Rising");
        trigFalling = new JRadioButton("Falling                                   ");
        trigLevel = new JRadioButton("Level");
        trigLevel.setSelected(true);
        ButtonGroup trigTGroup = new ButtonGroup();
        trigTGroup.add(trigRising);
        trigTGroup.add(trigFalling);
        trigTGroup.add(trigLevel);
        trigType.add(trigRising);
        trigType.add(trigFalling);
        trigType.add(trigLevel);

        //southpanel
        JPanel sthPan = new JPanel();
        sthPan.setLayout(new BorderLayout());
        triggerPanel.add(sthPan, BorderLayout.SOUTH);

        //Trigger Threshold
        JPanel fgThresh = new JPanel();
        fgThresh.setLayout(new BoxLayout(fgThresh, BoxLayout.X_AXIS));
        TitledBorder fgTh = BorderFactory.createTitledBorder(blackline, "Threshold");
        fgTh.setTitleJustification(TitledBorder.CENTER);
        fgThresh.setBorder(fgTh);
        fgThresh.setBorder(new CompoundBorder(fgThresh.getBorder(), new EmptyBorder(10, 10, 10, 10)));
        sthPan.add(fgThresh, BorderLayout.NORTH);

        JSpinner fgThreshTxt = new JSpinner(smThresh);
        JLabel fgThr = new JLabel("mV  ");
        JButton thrGO = new JButton("Set");
        fgThresh.add(fgThreshTxt);
        fgThresh.add(fgThr);
        fgThresh.add(thrGO);

        //Buttons
        trigButtons = new JPanel();
        trigButtons.setLayout(new FlowLayout());
        trigButtons.setBackground(Color.gray);
        sthPan.add(trigButtons, BorderLayout.SOUTH);
        reArm = new JButton("Re-Arm");
        trigButtons.add(reArm);
        force = new JButton("Force");
        trigButtons.add(force);

        //FuncGens
        JPanel funcGenPanel, fgOut, fgFreq, fgWType, fgP2P, fgOffset, funcGenExtend, fgE2;
        funcGenPanel= new JPanel();
        funcGenPanel.setLayout(new BorderLayout());
        funcGenPanel.setBackground(Color.lightGray);
        funcGenPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
        funcGenPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, funcGenPanel.getMinimumSize().height));
        right.add(funcGenPanel);

        functionGenerator = new JLabel("<HTML><U>Function Generator</U></HTML>");
        functionGenerator.setHorizontalAlignment(SwingConstants.CENTER);
        functionGenerator.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        funcGenPanel.add(functionGenerator, BorderLayout.NORTH);

        //output
        fgOut = new JPanel();
        fgOut.setLayout(new BoxLayout(fgOut, BoxLayout.X_AXIS));
        TitledBorder oput = BorderFactory.createTitledBorder(blackline, "Output");
        oput.setTitleJustification(TitledBorder.CENTER);
        fgOut.setBorder(oput);
        fgOut.setBorder(new CompoundBorder(fgOut.getBorder(), new EmptyBorder(10, 10, 10, 10)));
        fgOut.setMaximumSize(new Dimension(Integer.MAX_VALUE, fgOut.getMinimumSize().height));
        funcGenPanel.add(fgOut, BorderLayout.CENTER);

        fgOOn = new JRadioButton("On");
        fgOOff = new JRadioButton("Off");
        fgOOff.setSelected(true);
        ButtonGroup fgOutGroup = new ButtonGroup();
        fgOutGroup.add(fgOOn);
        fgOutGroup.add(fgOOff);
        fgOut.add(fgOOn);
        fgOut.add(fgOOff);

        //Frequency
        fgFreq = new JPanel();
        fgFreq.setLayout(new BoxLayout(fgFreq, BoxLayout.X_AXIS));
        TitledBorder fgF = BorderFactory.createTitledBorder(blackline, "Frequency");
        fgF.setTitleJustification(TitledBorder.CENTER);
        fgFreq.setBorder(fgF);
        fgFreq.setBorder(new CompoundBorder(fgFreq.getBorder(), new EmptyBorder(10, 10, 10, 10)));
        funcGenPanel.add(fgFreq, BorderLayout.EAST);

        JSpinner fgFreqTxt = new JSpinner(smFreq);
        fgFreq.add(fgFreqTxt);
        JLabel fgFrq = new JLabel("Hertz  ");
        JButton frqGo = new JButton("Set");
        fgFreq.add(fgFrq);
        fgFreq.add(frqGo);

        //Extend Funcgen
        funcGenExtend = new JPanel();
        funcGenExtend.setLayout(new BorderLayout());
        funcGenPanel.add(funcGenExtend, BorderLayout.SOUTH);

        //WaveType
        fgWType = new JPanel();
        fgWType.setLayout(new BoxLayout(fgWType, BoxLayout.Y_AXIS));
        TitledBorder fgT = BorderFactory.createTitledBorder(blackline, "Wave Type");
        fgT.setTitleJustification(TitledBorder.CENTER);
        fgWType.setBorder(fgT);
        fgWType.setBorder(new CompoundBorder(fgWType.getBorder(), new EmptyBorder(10, 10, 10, 10)));
        funcGenExtend.add(fgWType, BorderLayout.WEST);

        sineW = new JRadioButton("Sine");
        squareW = new JRadioButton("Square");
        triangleW = new JRadioButton("Triangle                                 ");
        rampW = new JRadioButton("Ramp");
        randomW = new JRadioButton("Random");
        sineW.setSelected(true);
        ButtonGroup fgWGroup = new ButtonGroup();
        fgWGroup.add(sineW);
        fgWGroup.add(squareW);
        fgWGroup.add(triangleW);
        fgWGroup.add(rampW);
        fgWGroup.add(randomW);
        fgWType.add(sineW);
        fgWType.add(squareW);
        fgWType.add(triangleW);
        fgWType.add(rampW);
        fgWType.add(randomW);

        //function generatior extension 2
        fgE2 = new JPanel();
        fgE2.setLayout(new BoxLayout(fgE2, BoxLayout.Y_AXIS));
        funcGenExtend.add(fgE2, BorderLayout.EAST);

        //PeaktoPeak
        fgP2P = new JPanel();
        fgP2P.setLayout(new BoxLayout(fgP2P, BoxLayout.X_AXIS));
        TitledBorder fgP = BorderFactory.createTitledBorder(blackline, "Peak-to-Peak Voltage");
        fgP.setTitleJustification(TitledBorder.CENTER);
        fgP2P.setBorder(fgP);
        fgP2P.setBorder(new CompoundBorder(fgP2P.getBorder(), new EmptyBorder(10, 10, 10, 10)));
        fgE2.add(fgP2P);

        JSpinner fgP2PTxt = new JSpinner(smP2p);
        JLabel p2pVolts = new JLabel("mV  ");
        JButton p2pGO = new JButton("Set");
        fgP2P.add(fgP2PTxt);
        fgP2P.add(p2pVolts);
        fgP2P.add(p2pGO);

        //offset
        fgOffset = new JPanel();
        fgOffset.setLayout(new BoxLayout(fgOffset, BoxLayout.X_AXIS));
        TitledBorder fgOff = BorderFactory.createTitledBorder(blackline, "Offset");
        fgOff.setTitleJustification(TitledBorder.CENTER);
        fgOffset.setBorder(fgOff);
        fgOffset.setBorder(new CompoundBorder(fgOffset.getBorder(), new EmptyBorder(10, 10, 10, 10)));
        fgE2.add(fgOffset);

        JSpinner fgOffsetTxt = new JSpinner(smFGOff);
        JLabel fgoVolts = new JLabel("mV  ");
        JButton offGO = new JButton("Set");
        fgOffset.add(fgOffsetTxt);
        fgOffset.add(fgoVolts);
        fgOffset.add(offGO);

        //formatter
        JPanel formatter = new JPanel();
        formatter.setLayout(new BorderLayout());
        formatter.setBorder(new EmptyBorder(10, 10, 10, 10));
        formatter.setBackground(Color.GRAY);
        right.add(formatter);
        connecter = new JButton("Connect");
        formatter.add(connecter, BorderLayout.CENTER);

        //Ranges
        String[] voltRangeStrings = {"20 mV", "50 mV", "100 mV", "200 mV", "500 mV", "1 V", "2 V"};
        String[] timeRangeStrings = {"1 us", "2 us", "5 us", "10 us", "20 us", "50 us",
                "100 us", "200 us", "500 us", "1 ms", "2 ms", "5 ms", "10 ms", "20 ms", "50 ms",
                "100 ms", "200 ms", "500 ms", "1 s"};
        JPanel rangeSetter = new JPanel();
        rangeSetter.setLayout(new BoxLayout(rangeSetter, BoxLayout.Y_AXIS));
        rangeSetter.setBackground(Color.lightGray);
        TitledBorder rngS = BorderFactory.createTitledBorder(blackline, "Ranges");
        rngS.setTitleJustification(TitledBorder.CENTER);
        rangeSetter.setBorder(rngS);
        rangeSetter.setBorder(new CompoundBorder(rangeSetter.getBorder(), new EmptyBorder(10, 10, 10, 10)));
        rangeSetter.setBorder(new CompoundBorder(new EmptyBorder(10, 80, 10, 80), rangeSetter.getBorder()));
        //rangeSetter.setBorder(new EmptyBorder(10, 80, 10, 80));
        JLabel yRange = new JLabel("Vertical Range");
        yRangeSetter = new JComboBox(voltRangeStrings);
        yRangeSetter.setSelectedItem("20 mV");
        xyGo = new JButton("Set");
        yRangeSetter.setBackground(Color.LIGHT_GRAY);
        JLabel xRange = new JLabel("Horizontal Range");
        xRangeSetter = new JComboBox(timeRangeStrings);
        xRangeSetter.setSelectedItem("1 ms");
        xRangeSetter.setBackground(Color.LIGHT_GRAY);
        rangeSetter.add(yRange);
        rangeSetter.add(yRangeSetter);
        rangeSetter.add(xRange);
        rangeSetter.add(xRangeSetter);
        rangeSetter.add(xyGo);
        sw.add(rangeSetter, BorderLayout.WEST);

        //Measurements
        JPanel measurementPanel = new JPanel();
        measurementPanel.setLayout(new BorderLayout());
        measurementPanel.setBackground(Color.lightGray);
        measurements= new JLabel("<HTML><U>Measurements</U></HTML>");
        measurements.setHorizontalAlignment(SwingConstants.CENTER);
        measurements.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        measurementPanel.add(measurements, BorderLayout.NORTH);


        chanAMeas = new JPanel();
        chanAMeas.setLayout(new GridLayout(0, 2));
        JLabel maxVoltA = new JLabel("Max Voltage ");
        JLabel minVoltA = new JLabel("Min Voltage ");
        JLabel maxP2PA = new JLabel("Max Peak-to-Peak Voltage ");
        JLabel avgVoltA = new JLabel("Average Voltage ");
        JLabel stdVoltA = new JLabel("Standard Deviation of Voltage ");
        JLabel freqVoltA = new JLabel("Frequency ");
        maxVoltAM = new JLabel("- mV");
        minVoltAM = new JLabel("- mV");
        maxP2PAM = new JLabel("- mV");
        avgVoltAM = new JLabel("- mV");
        stdVoltAM = new JLabel("-");
        freqVoltAM = new JLabel("- Hertz");
        chanAMeas.add(maxVoltA);
        chanAMeas.add(maxVoltAM);
        chanAMeas.add(minVoltA);
        chanAMeas.add(minVoltAM);
        chanAMeas.add(maxP2PA);
        chanAMeas.add(maxP2PAM);
        chanAMeas.add(avgVoltA);
        chanAMeas.add(avgVoltAM);
        chanAMeas.add(stdVoltA);
        chanAMeas.add(stdVoltAM);
        chanAMeas.add(freqVoltA);
        chanAMeas.add(freqVoltAM);
        chanMeasurements = new JTabbedPane();
        chanMeasurements.addTab("Channel A", null, chanAMeas, "Channel A Measurements");

        chanBMeas = new JPanel();
        chanBMeas.setLayout(new GridLayout(0, 2));
        JLabel maxVoltB = new JLabel("Max Voltage ");
        JLabel minVoltB = new JLabel("Min Voltage ");
        JLabel maxP2PB = new JLabel("Max Peak-to-Peak Voltage ");
        JLabel avgVoltB = new JLabel("Average Voltage ");
        JLabel stdVoltB = new JLabel("Standard Deviation of Voltage ");
        JLabel freqVoltB = new JLabel("Frequency ");
        maxVoltBM = new JLabel("- mV");
        minVoltBM = new JLabel("- mv");
        maxP2PBM = new JLabel("- mv");
        avgVoltBM = new JLabel("- mv");
        stdVoltBM = new JLabel("-");
        freqVoltBM = new JLabel("- Hertz");
        chanBMeas.add(maxVoltB);
        chanBMeas.add(maxVoltBM);
        chanBMeas.add(minVoltB);
        chanBMeas.add(minVoltBM);
        chanBMeas.add(maxP2PB);
        chanBMeas.add(maxP2PBM);
        chanBMeas.add(avgVoltB);
        chanBMeas.add(avgVoltBM);
        chanBMeas.add(stdVoltB);
        chanBMeas.add(stdVoltBM);
        chanBMeas.add(freqVoltB);
        chanBMeas.add(freqVoltBM);
        chanMeasurements.addTab("Channel B", null, chanBMeas, "Channel B Measurements");

        chanMMeas = new JPanel();
        chanMMeas.setLayout(new GridLayout(0, 2));
        JLabel maxVoltM = new JLabel("Max Voltage ");
        JLabel minVoltM = new JLabel("Min Voltage ");
        JLabel maxP2PM = new JLabel("Max Peak-to-Peak Voltage ");
        JLabel avgVoltM = new JLabel("Average Voltage ");
        JLabel stdVoltM = new JLabel("Standard Deviation of Voltage ");
        JLabel freqVoltM = new JLabel("Frequency ");
        maxVoltMM = new JLabel("- mv");
        minVoltMM = new JLabel("- mv");
        maxP2PMM = new JLabel("- mv");
        avgVoltMM = new JLabel("- mv");
        stdVoltMM = new JLabel("-");
        freqVoltMM = new JLabel("- Hertz");
        chanMMeas.add(maxVoltM);
        chanMMeas.add(maxVoltMM);
        chanMMeas.add(minVoltM);
        chanMMeas.add(minVoltMM);
        chanMMeas.add(maxP2PM);
        chanMMeas.add(maxP2PMM);
        chanMMeas.add(avgVoltM);
        chanMMeas.add(avgVoltMM);
        chanMMeas.add(stdVoltM);
        chanMMeas.add(stdVoltMM);
        chanMMeas.add(freqVoltM);
        chanMMeas.add(freqVoltMM);
        chanMeasurements.addTab("Math Channel", null, chanMMeas, "Math Channel Measurements");

        chanFMeas = new JPanel();
        chanFMeas.setLayout(new GridLayout(0, 2));
        JLabel maxVoltF = new JLabel("Max Voltage ");
        JLabel minVoltF = new JLabel("Min Voltage ");
        JLabel maxP2PF = new JLabel("Max Peak-to-Peak Voltage ");
        JLabel avgVoltF = new JLabel("Average Voltage ");
        JLabel stdVoltF = new JLabel("Standard Deviation of Voltage ");
        JLabel freqVoltF = new JLabel("Frequency ");
        maxVoltFM = new JLabel("- mv");
        minVoltFM = new JLabel("- mv");
        maxP2PFM = new JLabel("- mv");
        avgVoltFM = new JLabel("- mv");
        stdVoltFM = new JLabel("-");
        freqVoltFM = new JLabel("- Hertz");
        chanFMeas.add(maxVoltF);
        chanFMeas.add(maxVoltFM);
        chanFMeas.add(minVoltF);
        chanFMeas.add(minVoltFM);
        chanFMeas.add(maxP2PF);
        chanFMeas.add(maxP2PFM);
        chanFMeas.add(avgVoltF);
        chanFMeas.add(avgVoltFM);
        chanFMeas.add(stdVoltF);
        chanFMeas.add(stdVoltFM);
        chanFMeas.add(freqVoltF);
        chanFMeas.add(freqVoltFM);
        chanMeasurements.addTab("Filter Channel", null, chanFMeas, "Filter Channel Measurements");
        measurementPanel.add(chanMeasurements, BorderLayout.CENTER);
        sw.add(measurementPanel, BorderLayout.CENTER);

        //Samples and Bits
        JPanel samBit = new JPanel();
        samBit.setLayout(new BorderLayout());
        sw.add(samBit, BorderLayout.EAST);

        //Samples
        JPanel samPanel = new JPanel();
        samPanel.setBackground(Color.lightGray);
        TitledBorder samP = BorderFactory.createTitledBorder(blackline, "Number of Samples");
        samP.setTitleJustification(TitledBorder.CENTER);
        samPanel.setBorder(samP);
        samPanel.setBorder(new CompoundBorder(samPanel.getBorder(), new EmptyBorder(10, 10, 10, 10)));
        samPanel.setBorder(new CompoundBorder(new EmptyBorder(10, 80, 10, 80), samPanel.getBorder()));
        samNo = new JSpinner(smnumSamples);
        sams = new JButton("Set");
        samPanel.add(samNo);
        samPanel.add(sams);
        samBit.add(samPanel, BorderLayout.NORTH);

        //Bits
        JPanel bitPanel = new JPanel();
        bitPanel.setLayout(new BoxLayout(bitPanel, BoxLayout.X_AXIS));
        bitPanel.setBackground(Color.lightGray);
        TitledBorder bitP = BorderFactory.createTitledBorder(blackline, "Bits");
        bitP.setTitleJustification(TitledBorder.CENTER);
        bitPanel.setBorder(bitP);
        bitPanel.setBorder(new CompoundBorder(bitPanel.getBorder(), new EmptyBorder(10, 10, 10, 10)));
        bitPanel.setBorder(new CompoundBorder(new EmptyBorder(10, 80, 10, 80), bitPanel.getBorder()));
        samBit.add(bitPanel);

        bit8 = new JRadioButton("   8 Bit   ");
        bit12 = new JRadioButton("   12 Bit   ");
        bit8.setSelected(true);
        ButtonGroup bitGroup = new ButtonGroup();
        bitGroup.add(bit8);
        bitGroup.add(bit12);
        bitPanel.add(bit8);
        bitPanel.add(bit12);

        //Statuses
        JPanel statuses = new JPanel();
        JLabel netTitle = new JLabel("Network: ");
        netElement = new JLabel("Disconnected");
        JLabel trigStatTitle = new JLabel("| Trigger Status: ");
        trigStatElement = new JLabel("Stopped");
        JLabel currSampleTitle = new JLabel("| Current Sample Rate: ");
        currSampleElement = new JLabel("0");
        JLabel herz = new JLabel("Hz");
        statuses.add(netTitle);
        statuses.add(netElement);
        statuses.add(trigStatTitle);
        statuses.add(trigStatElement);
        statuses.add(currSampleTitle);
        statuses.add(currSampleElement);
        statuses.add(herz);
        sw.add(statuses, BorderLayout.SOUTH);


        setVisible(true);
    }
    @Override
    public void chartMouseClicked(ChartMouseEvent event) {
        Rectangle2D dataArea = graphPan.getScreenDataArea();
        JFreeChart chart = event.getChart();
        XYPlot plot = (XYPlot) chart.getPlot();
        ValueAxis xAxis = plot.getDomainAxis();
        double x = xAxis.java2DToValue(event.getTrigger().getX(), dataArea,
                RectangleEdge.BOTTOM);
        ValueAxis yAxis = plot.getRangeAxis();
        double y = yAxis.java2DToValue(event.getTrigger().getY(), dataArea,
                RectangleEdge.RIGHT);
        this.xCrosshair.setValue(x);
        this.yCrosshair.setValue(y);
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent event) {
        //do nothing
    }



}
