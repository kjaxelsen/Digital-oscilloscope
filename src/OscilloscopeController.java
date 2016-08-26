
import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.fathzer.soft.javaluator.StaticVariableSet;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Kevin on 29/03/16.
 * Controller for the oscilloscope
 */
@SuppressWarnings("All")
public class OscilloscopeController {
    private OscilloscopeView view;
    private OscilloscopeModel model;
    double xMultiplier = 1.0, yMultiplier = 1.0;
    String multix = "ms", multiy = "mV";

    /**
     *
     * @param modelI the model
     * @param viewI the view
     * provides interaction between model and view
     */
    public OscilloscopeController(OscilloscopeModel modelI, OscilloscopeView viewI) {
        model = modelI;
        view = viewI;
        //set default ranges
        model.setHorizRange("1 ms");
        model.setVertRange("50 mV");
        //action listeners
        view.xyGo.addActionListener(new setVerticalRangeListener());
        view.chA.addActionListener(new setChannelVisibility("A"));
        view.chB.addActionListener(new setChannelVisibility("B"));
        view.chM.addActionListener(new setChannelVisibility("M"));
        view.chF.addActionListener(new setChannelVisibility("F"));
        view.submitFormula.addActionListener(new setExpression());
        view.browseFilt.addActionListener(new getFile());
        view.submitFilt.addActionListener(new filtering());
        view.filtBp.addActionListener(new passFiltering());
        view.filtLP.addActionListener(new passFiltering());
        view.bit8.addActionListener(new bitListener());
        view.bit12.addActionListener(new bitListener());
        view.sams.addActionListener(new sampleListener());
        view.coupACA.addActionListener(new coupListener());
        view.coupDCA.addActionListener(new coupListener());
        view.coupACB.addActionListener(new coupListener());
        view.coupDCB.addActionListener(new coupListener());
        view.force.addActionListener(new trigForceListener());
        view.reArm.addActionListener(new trigRearmListener());
        view.trigAuto.addActionListener(new trigModeListener());
        view.trigNorm.addActionListener(new trigModeListener());
        view.trigSing.addActionListener(new trigModeListener());
        view.trigRising.addActionListener(new trigTypeListener());
        view.trigFalling.addActionListener(new trigTypeListener());
        view.trigLevel.addActionListener(new trigTypeListener());
        view.fgOOn.addActionListener(new fGenOutListener());
        view.fgOOff.addActionListener(new fGenOutListener());
        view.sineW.addActionListener(new fGenWTListener());
        view.squareW.addActionListener(new fGenWTListener());
        view.triangleW.addActionListener(new fGenWTListener());
        view.rampW.addActionListener(new fGenWTListener());
        view.randomW.addActionListener(new fGenWTListener());
        view.connecter.addActionListener(new java.awt.event.ActionListener() {
            //enter ip for connection
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if(view.connecter.getText().equals("Connect")) {
                    String IPAdd = JOptionPane.showInputDialog(view,
                            "Enter IP Address", null);
                    model.setIP(IPAdd);
                } else {
                    System.out.println("damn");
                    view.connecter.setText("Connect");
                    model.setBreaker(true);
                }
            }
        });
    }
    //get view used for thread
    public synchronized OscilloscopeView getView() {
        return view;
    }
    //get model used for thread
    public synchronized OscilloscopeModel getModel() {
        return model;
    }

    /**
     * handles recieved messages
     * @param part1 first byte
     * @param part2 second byte
     */
    public synchronized void messageHandler(Byte part1, Byte part2) {
        int byt1 = part1 & 0xFF;
        int byt2 = part2 & 0xFF;
        int bytTotal = combineBytes(byt1, byt2);
        //switch for different message types
        switch(getIdBytes(byt1)) {
            case 30:
                //Vertical Range
                System.out.println("Vertical Range");
                int voltnum = bytTotal & 0b0000011111111111;
                if(voltnum >= 1000) {
                    System.out.println("volts " + voltnum/1000);
                    setVertRange((voltnum/1000) + " V");
                } else {
                    System.out.println("mV " + voltnum);
                    setVertRange(voltnum + " mV");
                }
                redrawPlot();
                break;
            case 29:
                //Horizontal Range (us)
                System.out.println("Horizontal Range (us)");
                int timeNumUs = bytTotal & 0b0000000111111111;
                System.out.println("us " + timeNumUs);
                setHorizRange(timeNumUs + " us");
                redrawPlot();
                break;
            case 28:
                //Horizontal Range (ms)
                System.out.println("Horizontal Range (ms)");
                int timeNumMs = bytTotal & 0b0000000111111111;
                System.out.println("ms " + timeNumMs);
                setHorizRange(timeNumMs + " ms");
                redrawPlot();
                break;
            case 27:
                //Horizontal Range (s)
                System.out.println("Horizontal Range (s)");
                setHorizRange("1 s");
                redrawPlot();
                break;
            case 26:
                //Number of Samples
                System.out.println("Number of Samples");
                break;
            case 25:
                //Bit
                System.out.println("Bit");
                //if 8bit
                if((byt2 & 0b00000001) == 0) {
                    view.bit8.setSelected(true);
                //if 12 bit
                } else {
                    view.bit12.setSelected(true);
                }
                break;
            case 24:
                //Coupling
                System.out.println("Coupling");
                //if chA Ac
                if((byt2 & 0b00000011) == 0) {
                    view.coupACA.setSelected(true);
                //if chA DC
                } else if((byt2 & 0b00000011) == 1) {
                    view.coupDCA.setSelected(true);
                //if chB Ac
                } else if((byt2 & 0b00000011) == 2) {
                    view.coupACB.setSelected(true);
                //if chB DC
                } else if((byt2 & 0b00000011) == 3) {
                    view.coupDCB.setSelected(true);
                }
                break;
            case 23:
                //Channel Offset
                System.out.println("Channel Offset");
                int offset = bytTotal & 0b0000001111111111;
                //if chA
                if((byt1 & 0b00000100) == 0) {
                    //convert offset********************************************
                    view.chOffsetNumA.setValue(offset);
                //else chB
                } else {
                    //convert offset***********************************************
                    view.chOffsetNumB.setValue(offset);
                }
                break;
            case 22:
                //Trigger Options
                System.out.println("Trigger Options");
                //if Trigger Mode
                if((byt2 & 0b00001100) == 0) {
                    switch(byt2 & 0b00000011) {
                        case 0:
                            //auto
                            view.trigAuto.setSelected(true);
                            break;
                        case 1:
                            //Normal
                            view.trigNorm.setSelected(true);
                            break;
                        case 2:
                            //Single
                            view.trigSing.setSelected(true);
                            break;
                    }
                //if trigger type
                } else if((byt2 & 0b00001100) == 4) {
                    switch(byt2 & 0b00000011) {
                        case 0:
                            //Rising
                            view.trigRising.setSelected(true);
                            break;
                        case 1:
                            //Falling
                            view.trigFalling.setSelected(true);
                            break;
                        case 2:
                            //Level
                            view.trigLevel.setSelected(true);
                            break;
                    }
                //Trigger State
                } else {
                    switch(byt2 & 0b00000011) {
                        case 0:
                            //Armed
                            view.trigStatElement.setText("Armed");
                            break;
                        case 1:
                            //Triggered
                            view.trigStatElement.setText("Triggered");
                            break;
                        case 2:
                            //Stopped
                            view.trigStatElement.setText("Stopped");
                            break;
                    }
                }
                break;
            case 21:
                //Trigger Threshold
                System.out.println("Trigger Threshold");
                break;
            case 20:
                //Function Generator
                System.out.println("Function Generator");
                //if output on/off
                if((byt2 & 0b00001000) == 0) {
                    //if on
                    if((byt2 & 0b00000001) == 0) {
                        view.fgOOn.setSelected(true);
                    //if off
                    } else {
                        view.fgOOff.setSelected(true);
                    }
                //if wavetype
                } else {
                    switch(byt2 & 0b00000111) {
                        case 0:
                            //Sine
                            view.sineW.setSelected(true);
                            break;
                        case 1:
                            //Square
                            view.squareW.setSelected(true);
                            break;
                        case 2:
                            //Triangle
                            view.triangleW.setSelected(true);
                            break;
                        case 3:
                            //Ramp
                            view.rampW.setSelected(true);
                            break;
                        case 4:
                            //Random
                            view.randomW.setSelected(true);
                            break;
                    }
                }
                break;
            case 19:
                //Function Generator Frequency
                System.out.println("Function Generator Frequency");
                break;
            case 18:
                //Function Generator Peak-to-peak
                System.out.println("Function Generator Peak-to-peak");
                break;
            case 17:
                //Function Generator offset
                System.out.println("Function Generator offset");
                break;
            case 16:
                //Sample Rate
                System.out.println("Sample Rate");
                int samplerate = (bytTotal & 0b0000011111111111) * 500;
                view.currSampleElement.setText("" + samplerate);
                break;

        }
    }
    //gets bits to identify msg type
    public synchronized int getIdBytes(int byt) {
        return (byt >> 3);
    }
    //combines bytes into int
    public synchronized int combineBytes(int b1, int b2) {
        return ((b1 << 8) | (b2 & 0xFF)) & 0xFFFF;
    }
    //resets multipliers for plots
    public void resetMultipliers() {
        xMultiplier = 1;
        yMultiplier = 1;
    }

    /**
     * plots the channels and calculates measurements
     * @param dataSets the set of points
     * @param channel the channel to plot
     */
    public synchronized void setSeries(ArrayList<ArrayList<Double>> dataSets, String channel) {
        XYSeries series = null;
        Boolean initRead, runit = true;
        Double max = 0.0, min = 0.0, avg = 0.0, p2p = 0.0, total = 0.0, stdTotal = 0.0;
        JLabel setMax = null, setMin = null, setAvg = null, setP2p = null, setStd = null, setFrq = null;
        DecimalFormat df = new DecimalFormat("#.####");
        if(channel.equals("A") || channel.equals("B")) {
            for (int i = 0; i < dataSets.size(); i++) {
                dataSets.get(i).set(1, dataSets.get(i).get(1) * yMultiplier);
                dataSets.get(i).set(0, dataSets.get(i).get(0) * xMultiplier);
            }
        }
        //sets general case variables to alter view
        switch (channel) {
            //if A
            case "A":
                model.setChanA(dataSets);
                series = view.altera;
                setMax = view.maxVoltAM;
                setMin = view.minVoltAM;
                setAvg = view.avgVoltAM;
                setP2p = view.maxP2PAM;
                setStd =  view.stdVoltAM;
                setFrq = view.freqVoltAM;
                int half = (int)Math.floor(dataSets.size()/2.0);
                view.xTrig.setValue(dataSets.get(half).get(0));
                view.yTrig.setValue(dataSets.get(half).get(1));
                break;
            //if b
            case "B":
                model.setChanB(dataSets);
                series = view.alterb;
                setMax = view.maxVoltBM;
                setMin = view.minVoltBM;
                setAvg = view.avgVoltBM;
                setP2p = view.maxP2PBM;
                setStd =  view.stdVoltBM;
                setFrq = view.freqVoltBM;
                int half2 = (int)Math.floor(dataSets.size()/2.0);
                view.xTrig2.setValue(dataSets.get(half2).get(0));
                view.yTrig2.setValue(dataSets.get(half2).get(1));
                break;
            //if math channel
            case "M":
                if(view.mthEn.isSelected()) {
                    dataSets = mathChanCalc();
                    if(!dataSets.isEmpty()) {
                        model.setChanM(dataSets);
                    }
                    series = view.alterm;
                    setMax = view.maxVoltMM;
                    setMin = view.minVoltMM;
                    setAvg = view.avgVoltMM;
                    setP2p = view.maxP2PMM;
                    setStd =  view.stdVoltMM;
                    setFrq = view.freqVoltMM;
                } else {
                    runit = false;
                }
                break;
            //if filter channel
            case "F":
                if(view.filtEn.isSelected()) {
                    dataSets = filtChanGet();
                    if(!dataSets.isEmpty()) {
                        model.setChanF(dataSets);
                    }
                    series = view.alterf;
                    setMax = view.maxVoltFM;
                    setMin = view.minVoltFM;
                    setAvg = view.avgVoltFM;
                    setP2p = view.maxP2PFM;
                    setStd =  view.stdVoltFM;
                    setFrq = view.freqVoltFM;
                } else {
                    runit = false;
                }
                break;
        }
        if(runit) {
            series.clear();
            initRead = true;
            //change for onscreen
            for (ArrayList<Double> data : dataSets) {
                //check all within plot bounds
                if (initRead) {
                    if(data.get(0) < view.timeAxis.getUpperBound() && view.voltAxis.getLowerBound() < data.get(1)
                            && view.voltAxis.getUpperBound() > data.get(1)) {
                        max = data.get(1);
                        min = data.get(1);
                        total = data.get(1);
                        initRead = false;
                    }
                } else if (max < data.get(1)) {
                    if(data.get(0) < view.timeAxis.getUpperBound() && view.voltAxis.getLowerBound() < data.get(1)
                            && view.voltAxis.getUpperBound() > data.get(1)) {
                        max = data.get(1);
                        total += data.get(1);
                    }
                } else if (min > data.get(1)) {
                    if(data.get(0) < view.timeAxis.getUpperBound() && view.voltAxis.getLowerBound() < data.get(1)
                            && view.voltAxis.getUpperBound() > data.get(1)) {
                        min = data.get(1);
                        total += data.get(1);
                    }
                } else {
                    if(data.get(0) < view.timeAxis.getUpperBound() && view.voltAxis.getLowerBound() < data.get(1)
                            && view.voltAxis.getUpperBound() > data.get(1)) {
                        total += data.get(1);
                    }
                }
                //add data to plot
                series.add(data.get(0), data.get(1));
            }
            //calculate measurements
            avg = total / dataSets.size();
            p2p = max - min;
            int k = 0, m = 0;
            double one = 0, two = 0, fTotal= 0;
            for(int j = 0; j<dataSets.size(); j++) {
                stdTotal += ((dataSets.get(j).get(1) - avg) * (dataSets.get(j).get(1) - avg));
                if(k<2 && j > 0 && (j+1) < dataSets.size()) {
                    if(dataSets.get(j-1).get(1) < dataSets.get(j).get(1) && dataSets.get(j).get(1) >= dataSets.get(j+1).get(1)) {
                        if(k == 0) {
                            one = dataSets.get(j).get(0);
                            k++;
                        } else if(k == 1) {
                            two = dataSets.get(j).get(0);
                            k = 1;
                            fTotal += (two - one);
                            one = two;
                            m++;
                        }
                    }
                }
            }
            if(m == 0) {
                m = 1;
            }
            fTotal = fTotal/m;
            stdTotal = stdTotal / dataSets.size();
            stdTotal = Math.sqrt(stdTotal);
            setAvg.setText(df.format(avg) + multiy);
            setMin.setText(df.format(min) + multiy);
            setP2p.setText(df.format(p2p) + multiy);
            setMax.setText(df.format(max) + multiy);
            setStd.setText(df.format(stdTotal));
            setFrq.setText(df.format(fTotal) + " Hz");
        }
    }

    /**
     * calculates data set for filter based on inputs
     * @return the dataset for filter
     */
    private ArrayList<ArrayList<Double>> filtChanGet() {
        ArrayList<Double> column1 = new ArrayList<>();
        ArrayList<Double> column2 = new ArrayList<>();
        ArrayList<Double> temppts = new ArrayList<>();
        ArrayList<ArrayList<Double>> channel = new ArrayList<>();
        ArrayList<ArrayList<Double>> channelFinal = new ArrayList<>();
        Double results = 0.0;
        if(view.filtEn.isSelected()) {
            //find which channel to filter
            model.setFiltChan(view.chanList.getSelectedItem().toString());
            if(view.chanList.getSelectedItem().toString().contentEquals("Channel A")
                    && model.getChanA()!= null) {
                channel = model.getChanA();
            } else if(view.chanList.getSelectedItem().toString().contentEquals("Channel B")) {
                channel = model.getChanB();
            }else if(view.chanList.getSelectedItem().toString().contentEquals("Math Channel")) {
                //cant filter math while in math function
                if(model.getMathChan().contains("F")) {
                    model.setFiltChan("");
                    JOptionPane.showMessageDialog(view, "Math contains filter already");
                } else {
                    channel = model.getChanM();
                }
            } else {
                JOptionPane.showMessageDialog(view, "Channel Not Available");
            }
            ArrayList<ArrayList<Double>> dataSetsRev = model.getFiltData();
            if(dataSetsRev != null) {
                if(dataSetsRev.get(0).size() == 1) {
                    //FIR
                    for(ArrayList<Double> data : dataSetsRev) {
                        column1.add(data.get(0));
                    }
                    for(int n = 0; n < channel.size(); n++) {
                        temppts.add(channel.get(n).get(0));
                        for (int i = 0; i < column1.size(); i++) {
                            if((n - i) < 0) {
                                results += column1.get(i) * 0;
                            } else {
                                results += column1.get(i) * channel.get(n - i).get(1);
                            }
                        }
                        temppts.add(results);
                        channelFinal.add(temppts);
                        temppts = new ArrayList<>();
                        results = 0.0;
                    }
                } else if(dataSetsRev.get(0).size() == 2) {
                    //IIR
                    for(ArrayList<Double> data : dataSetsRev) {
                        column1.add(data.get(0));
                        column2.add(data.get(1));
                    }
                    if(column1.size() == column2.size()) {
                        for (int n = 0; n < channel.size(); n++) {
                            temppts.add(channel.get(n).get(0));
                            for (int i = 0; i < column1.size(); i++) {
                                //channelFinal.get(i).add(column1.get(i) * channel.get(channel.size() - i).get(1));
                                if ((n - i) < 0) {
                                    results += column1.get(i) * 0;
                                } else {
                                    results += column1.get(i) * channel.get(n - i).get(1);
                                }
                            }
                            for (int j = 1; j < column2.size(); j++) {
                                if ((n - j) < 0) {
                                    results -= column2.get(j) * 0;
                                } else {
                                    results -= column2.get(j) * channelFinal.get(n - j).get(1);
                                }
                            }
                            results *= (1 / column2.get(0));
                            temppts.add(results);
                            channelFinal.add(temppts);
                            temppts = new ArrayList<>();
                            results = 0.0;
                        }
                    } else {
                        JOptionPane.showMessageDialog(view, "Rejected due to differing number of rows in each column");
                    }
                } else {
                    //Err
                    JOptionPane.showMessageDialog(view, "Rejected due to number of columns");
                }
            } else {
                //do nothing
            }
        }
        return channelFinal;
    }

    /**
     * gets the dataset forthe math channel
     * @return math channel datasets
     */
    private ArrayList<ArrayList<Double>> mathChanCalc() {
        ArrayList<ArrayList<Double>> mathData = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> mathPoint = new ArrayList<Double>();
        String expression = model.getExpression();
        model.setMathChan(expression);
        DoubleEvaluator eval = new DoubleEvaluator();
        int lengthCheck = 0;
        boolean bA = false, bB = false, bF = false, err = false;
        StaticVariableSet<Double> variables = new StaticVariableSet<Double>();
        try {
            //ensure it contains a channel then see which
            if (expression.contains("A") || expression.contains("B") || expression.contains("F")) {
                if (expression.contains("A")) {
                    bA = true;
                    if (model.getChanA() != null) {
                        lengthCheck = model.getChanA().size();
                    } else {
                        //err
                        err = true;
                    }
                }
                if (expression.contains("B")) {
                    bB = true;
                    if (model.getChanB() != null) {
                        if (lengthCheck < model.getChanB().size()) {
                            lengthCheck = model.getChanB().size();
                        } else {
                            //err
                            err = true;
                        }
                    }
                }
                if (expression.contains("F")) {
                    if(!model.getFiltChan().contentEquals("Math Channel")) {
                        bF = true;
                        if (model.getChanF() != null) {
                            if (lengthCheck < model.getChanF().size()) {
                                lengthCheck = model.getChanF().size();
                            }
                        } else {
                            //err
                            err = true;
                        }
                    } else {
                        JOptionPane.showMessageDialog(view, "Filter channel already contains math");
                        model.setMathChan("");
                    }
                }
                if (err) {
                    //err
                }
                //set variables of channels
                for (int i = 0; i < lengthCheck; i++) {
                    if (bA) {
                        variables.set("A", model.getChanA().get(i).get(1));
                    }
                    if (bB) {
                        variables.set("B", model.getChanB().get(i).get(1));
                    }
                    if (bF) {
                        variables.set("F", model.getChanF().get(i).get(1));
                    }
                    if (bA) {
                        mathPoint.add(model.getChanA().get(i).get(0));
                    } else if (bB) {
                        mathPoint.add(model.getChanB().get(i).get(0));
                    } else if (bF) {
                        mathPoint.add(model.getChanF().get(i).get(0));
                    }
                    mathPoint.add(eval.evaluate(expression, variables));
                    mathData.add(mathPoint);
                    mathPoint = new ArrayList<Double>();
                }

            } else {
            }
        } catch(Exception e) {
            JOptionPane.showMessageDialog(view, "Invalid Formula");
            model.setExpression("");
        }
        return mathData;
    }

    /**
     * listens for vertical range change
     */
    private class setVerticalRangeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String[] yrng = null, xrng = null;
            if(model.isConnected()) {
                model.setToSend(true);
            }
            setHorizRange(view.xRangeSetter.getSelectedItem().toString());
            setVertRange(view.yRangeSetter.getSelectedItem().toString());
            model.setToSend(false);
            redrawPlot();
        }
    }

    /**
     * redraws the plots when needed
     */
    public synchronized void redrawPlot() {
        if(model.getChanA() != null && !model.getChanA().isEmpty()) {
            model.getChanA().get(0).get(1);
            setSeries(model.getChanA(), "A");
        }
        if(model.getChanB() != null && !model.getChanB().isEmpty()) {
            setSeries(model.getChanB(), "B");
        }
        if(view.mthEn.isSelected() && model.getChanM() != null && !model.getChanM().isEmpty()) {
            setSeries(null, "M");
        }
        if(view.filtEn.isSelected() && model.getChanF() != null && !model.getChanF().isEmpty()) {
            setSeries(null, "F");
        }
    }

    /**
     * sets range and messages server to let know
     * @param vPerDiv volts per division
     */
    private void setVertRange(String vPerDiv) {
        String[] yrng = null;
        int value = 0;
        model.setVertRange(vPerDiv);
        yrng = vPerDiv.split(" ");
        if(yrng[1].equals("mV")) {
            view.voltAxis.setLabel("Voltage (mV)");
            if(multiy.equals("mV")) {
                yMultiplier = 1.0;
            } else {
                yMultiplier = 1000;
            }
            multiy = "mV";
            value = Integer.parseInt(yrng[0]);
        } else if(yrng[1].equals("V")){
            view.voltAxis.setLabel("Voltage (V)");
            if(multiy.equals("V")) {
                yMultiplier = 1.0;
            } else {
                yMultiplier = 0.001;
            }
            multiy = "V";
            value = Integer.parseInt(yrng[0]) * 1000;
        }
        double vertRange = Double.parseDouble(yrng[0]);
        view.voltAxis.setTickUnit(new NumberTickUnit(vertRange));
        view.voltAxis.setRange(-5 * vertRange, 5 * vertRange);
        view.yRangeSetter.setSelectedItem(vPerDiv);
        //if not set by server (dont want inf loop)
        if(model.toSend()) {
            int testit = (value | 0b1111000000000000);
            ArrayList<Byte> bytes = new ArrayList<>();
            bytes.add((byte)((testit >> 8) & 0xFF));
            bytes.add((byte)(testit & 0xFF));
            model.addOutBox(bytes);
        }
    }

    /**
     * sets range and messages server to let know
     * @param tPerDiv time per division
     */
    private void setHorizRange(String tPerDiv) {
        String[] xrng = null;
        int value = 0;
        model.setHorizRange(tPerDiv);
        xrng = tPerDiv.split(" ");
        if(xrng[1].equals("us")) {
            view.timeAxis.setLabel("Time (us)");
            if(multix.equals("us")) {
                xMultiplier = 1;
            } else if(multix.equals("ms")) {
                xMultiplier = 1000;
            } else if(multix.equals("s")) {
                xMultiplier = 1000000;
            }
            multix = "us";
            value = 0b1110100000000000;
        } else if(xrng[1].equals("ms")) {
            view.timeAxis.setLabel("Time (ms)");
            if(multix.equals("us")) {
                xMultiplier = 0.001;
            } else if(multix.equals("ms")) {
                xMultiplier = 1;
            } else if(multix.equals("s")) {
                xMultiplier = 1000;
            }
            multix = "ms";
            value = 0b1110000000000000;
        } else if(xrng[1].equals("s")) {
            view.timeAxis.setLabel("Time (s)");
            if(multix.equals("us")) {
                xMultiplier = 0.000001;
            } else if(multix.equals("ms")) {
                xMultiplier = 0.001;
            } else if(multix.equals("s")) {
                xMultiplier = 1;
            }
            multix = "s";
            value = 0b1101100000000000;
        }
        double horizRange = Double.parseDouble(xrng[0]);
        view.timeAxis.setTickUnit(new NumberTickUnit(horizRange));
        view.timeAxis.setRange(0.0, 12 * horizRange);
        view.xRangeSetter.setSelectedItem(tPerDiv);
        if(model.toSend()) {
            int testit = (Integer.parseInt(xrng[0]) | value);
            ArrayList<Byte> bytes = new ArrayList<>();
            bytes.add((byte)((testit >> 8) & 0xFF));
            bytes.add((byte)(testit & 0xFF));
            model.addOutBox(bytes);
        }
    }

    /**
     * toggles visibility from checkboxes
     */
    private class setChannelVisibility implements ActionListener {
        private String channel;
        @Override
        public void actionPerformed(ActionEvent e) {
            XYItemRenderer renderer = ((XYPlot)view.chart.getPlot()).getRenderer();
            boolean selected = false;
            switch (channel) {
                case "A":
                    //toggle visibility
                    renderer.setSeriesVisible(0, !renderer.isSeriesVisible(0));
                    if(!view.chA.isSelected()) {
                        view.chanMeasurements.remove(view.chanAMeas);
                    } else {
                        selected = true;
                    }
                    break;
                case "B":
                    renderer.setSeriesVisible(1, !renderer.isSeriesVisible(1));
                    if(!view.chB.isSelected()) {
                        view.chanMeasurements.remove(view.chanBMeas);
                    } else {
                        selected = true;
                    }
                    break;
                case "M":
                    renderer.setSeriesVisible(2, !renderer.isSeriesVisible(2));
                    if(!view.chM.isSelected()) {
                        view.chanMeasurements.remove(view.chanMMeas);
                    } else {
                        selected = true;
                    }
                    break;
                case "F":
                    renderer.setSeriesVisible(3, !renderer.isSeriesVisible(3));
                    if(!view.chF.isSelected()) {
                        view.chanMeasurements.remove(view.chanFMeas);
                    } else {
                        selected = true;
                    }
                    break;
            }
            if(selected) {
                view.chanMeasurements.removeAll();
                if(view.chA.isSelected()) {
                    view.chanMeasurements.addTab("Channel A", null, view.chanAMeas, "Channel A Measurements");
                }
                if(view.chB.isSelected()) {
                    view.chanMeasurements.addTab("Channel B", null, view.chanBMeas, "Channel B Measurements");
                }
                if(view.chM.isSelected()) {
                    view.chanMeasurements.addTab("Math Channel", null, view.chanMMeas, "Math Channel Measurements");
                }
                if(view.chF.isSelected()) {
                    view.chanMeasurements.addTab("Filter Channel", null, view.chanFMeas, "Filter Channel Measurements");
                }
            }
        }

        public setChannelVisibility(String channel) {
            this.channel = channel;
        }
    }

    public void downsample(int sampleRate, ArrayList<ArrayList<Double>> samples) {
        double downVal = sampleRate/(100000 + 100);
        boolean remove = false;
        for(int i = 0; i < samples.size(); i++) {
            if(i%downVal == 0) {
                //do nothing
            } else {
                samples.get(i).set(1, 0.0);
            }
        }
        //System.out.println("lll " + (10000000/downVal)/(20000000/2));
        //IirFilterCoefficients fc = IirFilterDesignFisher.design(lowpass, butterworth, 2, 0.1, (10000000/downVal)/(20000000/2), 0);

        //IirFilter filta = new IirFilter(fc);
        //for(int j = 0; j<samples.size(); j++) {
         //   double set = filta.step(samples.get(j).get(1));
         //   samples.get(j).set(1, set);
        //}


    }


    /**
     * sets math expression
     */
    private class setExpression implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String expression = view.formulaMth.getText();
            if(expression.contains("A") || expression.contains("B") || expression.contains("F")) {
                model.setExpression(view.formulaMth.getText());
                setSeries(null, "M");
                redrawPlot();
                ArrayList<Byte> bytes = new ArrayList<>();
                bytes.add((byte)0b00000001);
                bytes.add((byte)0b00000001);
                model.addOutBox(bytes);
            } else {
                JOptionPane.showMessageDialog(view, "Invalid Formula");
            }
        }
    }

    /**
     * gets file for filter
     */
    private class getFile implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser c = new JFileChooser();
            c.setFileFilter(new FileNameExtensionFilter("Commas Separated Files", "csv"));
            int rVal = c.showOpenDialog(view);
            if (rVal == JFileChooser.APPROVE_OPTION) {
                //view.csvFilt.setText(c.getSelectedFile().getName());
                model.setFiltFile(c.getCurrentDirectory().toString() + "\\" + c.getSelectedFile().getName());
                view.csvFilt.setText(c.getSelectedFile().getName());
            }
            if (rVal == JFileChooser.CANCEL_OPTION) {
                view.csvFilt.setText("Select File");
            }
        }
    }

    /**
     * runs filter channel
     */
    private class filtering implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(view.filtEn.isSelected()) {
                try {
                    //System.out.println(view.csvFilt.getText());
                    ArrayList<ArrayList<Double>> filtData = SampleDataReader.parseSampleFile(model.getFiltFile());
                    model.setFiltData(filtData);
                } catch (FileNotFoundException e1) {
                    JOptionPane.showMessageDialog(view, "File Not Found");
                }
            }
            setSeries(null, "F");
            redrawPlot();
        }
    }

    /**
     * for unimplemented bandpass
     */
    private class passFiltering implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            //if low pass
            if(view.filtLP.isSelected()) {
                System.out.println("lp");
            //if bandpass
            } else {
                System.out.println("bp");

            }
        }
    }

    /**
     * listens to bit changes -sends msg to server
     */
    private class bitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ArrayList<Byte> bytes = new ArrayList<>();
            bytes.add((byte)0b11001000);
            if(view.bit8.isSelected()) {
                view.samNo.setValue(50000);
                bytes.add((byte)0b00000000);
            } else {
                bytes.add((byte)0b00000001);
            }

            model.addOutBox(bytes);
        }
    }

    /**
     * listens for sample changes -sends msg to server
     */
    private class sampleListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ArrayList<Byte> bytes = new ArrayList<>();
            bytes.add((byte)0b11010000);
            if(view.bit8.isSelected()) {
                view.samNo.setValue(50000);
            }
            bytes.add((byte)Integer.parseInt(view.samNo.getValue().toString()));
            model.addOutBox(bytes);
        }
    }

    /**
     * listens for coupling changes -sends msg to server
     */
    private class coupListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ArrayList<Byte> bytes = new ArrayList<>();
            bytes.add((byte)0b11000000);
            if(view.coupACA.isSelected()) {
                bytes.add((byte)0);
            } else if(view.coupACB.isSelected()) {
                bytes.add((byte)1);
            } else if(view.coupDCA.isSelected()) {
                bytes.add((byte)2);
            } else {
                bytes.add((byte)3);
            }
            model.addOutBox(bytes);
        }
    }

    /**
     * listens for trigger mode changes -sends msg to server
     */
    private class trigModeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ArrayList<Byte> bytes = new ArrayList<>();
            bytes.add((byte)0b10110000);
            if(view.trigAuto.isSelected()) {
                bytes.add((byte)0b00000000);
            } else if(view.trigNorm.isSelected()) {
                bytes.add((byte)0b00000001);
            } else {
                bytes.add((byte)0b00000010);
            }
            model.addOutBox(bytes);
        }
    }

    /**
     * listens for trigger type changes -sends msg to server
     */
    private class trigTypeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ArrayList<Byte> bytes = new ArrayList<>();
            bytes.add((byte)0b10110000);
            if(view.trigRising.isSelected()) {
                bytes.add((byte)0b00000100);
            } else if(view.trigFalling.isSelected()) {
                bytes.add((byte)0b00000101);
            } else {
                bytes.add((byte)0b00000110);
            }
            model.addOutBox(bytes);
        }
    }

    /**
     * listens for force push -sends msg to server
     */
    private class trigForceListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ArrayList<Byte> bytes = new ArrayList<>();
            bytes.add((byte)0b10110000);
            bytes.add((byte)0b00001100);
            model.addOutBox(bytes);
        }
    }

    /**
     * listens for rearm push -sends msg to server
     */
    private class trigRearmListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(view.trigSing.isSelected()) {
                ArrayList<Byte> bytes = new ArrayList<>();
                bytes.add((byte) 0b10110000);
                bytes.add((byte) 0b00010000);
                model.addOutBox(bytes);
            }
        }
    }

    /**
     * listens for funtiongen on/off toggle
     */
    private class fGenOutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ArrayList<Byte> bytes = new ArrayList<>();
            bytes.add((byte) 0b10100000);
            if(view.fgOOn.isSelected()) {
                bytes.add((byte) 0b00000000);
            } else {
                bytes.add((byte) 0b00000001);
            }
            model.addOutBox(bytes);
        }
    }

    /**
     * listens for fungen wavetype change
     */
    private class fGenWTListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ArrayList<Byte> bytes = new ArrayList<>();
            if(view.fgOOn.isSelected()) {
                bytes.add((byte) 0b10100000);
                if (view.sineW.isSelected()) {
                    bytes.add((byte) 0b00001000);
                } else if (view.squareW.isSelected()) {
                    bytes.add((byte) 0b00001001);
                } else if (view.triangleW.isSelected()) {
                    bytes.add((byte) 0b00001010);
                } else if (view.rampW.isSelected()) {
                    bytes.add((byte) 0b00001011);
                } else {
                    bytes.add((byte) 0b00001100);
                }
                model.addOutBox(bytes);
            }
        }
    }



}
