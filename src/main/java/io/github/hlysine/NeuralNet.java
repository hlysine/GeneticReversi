package io.github.hlysine;

import io.github.hlysine.table.Table;
import io.github.hlysine.table.TableRow;

import static io.github.hlysine.Helper.max;

public class NeuralNet {

    private final int iNodes;//No. of input nodes
    private final int hNodes;//No. of hidden nodes
    private final int oNodes;//No. of output nodes

    private final int hLayers;//No. of hidden layers

    private Matrix whi;//matrix containing weights between the input nodes and the hidden nodes
    private final Matrix[] whh;//matrices containing weights between the hidden nodes and another layer of hidden nodes
    private Matrix woh;//matrix containing weights between hidden nodes and the output nodes


    //constructor
    public NeuralNet(int inputs, int hiddenNo, int outputNo, int hiddenLayers) {

        //set dimensions from parameters
        iNodes = inputs;
        oNodes = outputNo;
        hNodes = hiddenNo;
        hLayers = hiddenLayers;


        //create first layer weights
        //included bias weight
        whi = new Matrix(hNodes, iNodes +1);

        //create second layer weights
        //include bias weight
        whh = new Matrix[hLayers];
        for (int i = 0; i<hLayers; i++) {
            whh[i] = new Matrix(hNodes, hNodes +1);
        }

        //create second layer weights
        //include bias weight
        woh = new Matrix(oNodes, hNodes +1);

        //set the matrices to random values
        whi.randomize();
        for (int i = 0; i<hLayers; i++)
            whh[i].randomize();
        woh.randomize();
    }

    //constructor that does not randomize the weights
    public NeuralNet(int inputs, int hiddenNo, int outputNo, int hiddenLayers, boolean noRandomize) {

        //set dimensions from parameters
        iNodes = inputs;
        oNodes = outputNo;
        hNodes = hiddenNo;
        hLayers = hiddenLayers;


        //create first layer weights
        //included bias weight
        whi = new Matrix(hNodes, iNodes +1);

        //create second layer weights
        //include bias weight
        whh = new Matrix[hLayers];
        for (int i = 0; i<hLayers; i++) {
            whh[i] = new Matrix(hNodes, hNodes +1);
        }

        //create second layer weights
        //include bias weight
        woh = new Matrix(oNodes, hNodes +1);

        if (!noRandomize) {
            //set the matrices to random values
            whi.randomize();
            for (int i = 0; i<hLayers; i++)
                whh[i].randomize();
            woh.randomize();
        }
    }


    //mutation function for genetic algorithm
    public void mutate(float mr) {
        //mutates each weight matrix
        whi.mutate(mr);
        for (int i = 0; i<hLayers; i++)
            whh[i].mutate(mr);
        woh.mutate(mr);
    }


    //calculate the output values by feeding forward through the neural network
    public float[] output(float[] inputsArr) {

        //convert array to matrix
        //Note woh has nothing to do with it its just a function in the Matrix class
        Matrix inputs = woh.singleColumnMatrixFromArray(inputsArr);

        //add bias
        Matrix inputsBias = inputs.addBias();


        //-----------------------calculate the guessed output

        //apply layer one weights to the inputs
        Matrix hiddenInputs = whi.dot(inputsBias);

        //pass through activation function(sigmoid)
        Matrix hiddenOutputs = hiddenInputs.activate();

        Matrix hiddenOutputsBiased = hiddenOutputs;

        for (int i = 0; i<hLayers; i++) {

            //add bias
            hiddenOutputsBiased = hiddenOutputsBiased.addBias();

            //apply weights
            Matrix hiddenInputs2 = whh[i].dot(hiddenOutputsBiased);
            hiddenOutputsBiased = hiddenInputs2.activate();
        }

        hiddenOutputsBiased = hiddenOutputsBiased.addBias();
        //apply level three weights
        Matrix outputInputs = woh.dot(hiddenOutputsBiased);
        //pass through activation function(sigmoid)
        Matrix outputs = outputInputs.activate();

        //convert to an array and return
        return outputs.toArray();
    }

    //crossover function for genetic algorithm
    public NeuralNet crossover(NeuralNet partner) {

        //creates a new child with layer matricies from both parents
        NeuralNet child = new NeuralNet(iNodes, hNodes, oNodes, hLayers, true);
        child.whi = whi.crossover(partner.whi);
        for (int i = 0; i<hLayers; i++)
            child.whh[i] = whh[i].crossover(partner.whh[i]);
        child.woh = woh.crossover(partner.woh);
        return child;
    }

    //return a neural net which is a clone of this Neural net
    public NeuralNet clone() {
        NeuralNet clone  = new NeuralNet(iNodes, hNodes, oNodes, hLayers, true);
        clone.whi = whi.clone();
        for (int i = 0; i<hLayers; i++)
            clone.whh[i] = whh[i].clone();
        clone.woh = woh.clone();

        return clone;
    }

    //converts the weights matrices to a single table
    //used for storing the snakes brain in a file
    public Table NetToTable() {

        //create table
        Table t = new Table();


        //convert the matrices to an array
        float[] whiArr = whi.toArray();

        float[][] whhArr;
        whhArr = new float[hLayers][hNodes];
        for (int i = 0; i<hLayers; i++) {
            whhArr[i] = whh[i].toArray();
        }
        float[] wohArr = woh.toArray();

        //set the amount of columns in the table
        for (int i = 0; i< max(whiArr.length, whhArr[0].length, wohArr.length); i++) {
            t.addColumn();
        }

        //set the first row as whi
        TableRow tr = t.addRow();

        for (int i = 0; i< whiArr.length; i++) {
            tr.setFloat(i, whiArr[i]);
        }


        //set the rest as whh
        for (int j = 0; j<hLayers; j++) {
            tr = t.addRow();

            for (int i = 0; i< whhArr[j].length; i++) {
                tr.setFloat(i, whhArr[j][i]);
            }
        }
        //set the last row as woh
        tr = t.addRow();

        for (int i = 0; i< wohArr.length; i++) {
            tr.setFloat(i, wohArr[i]);
        }

        //return table
        return t;
    }


    //takes in table as parameter and overwrites the matrices data for this neural network
    //used to load snakes from file
    public void TableToNet(Table t) {

        //create arrays to temporarily store the data for each matrix
        float[] whiArr = new float[whi.rows * whi.cols];
        float[][] whhArr = new float[hLayers][whh[0].rows * whh[0].cols];
        float[] wohArr = new float[woh.rows * woh.cols];

        //set the whi array as the first row of the table
        TableRow tr = t.getRow(0);

        for (int i = 0; i< whiArr.length; i++) {
            whiArr[i] = tr.getFloat(i);
        }

        for (int j = 0; j<hLayers; j++) {
            //set the whh array as the remaining rows of the table
            tr = t.getRow(j+1);

            for (int i = 0; i< whhArr[j].length; i++) {
                whhArr[j][i] = tr.getFloat(i);
            }
        }

        //set the woh array as the last row of the table

        tr = t.getRow(hLayers + 1);

        for (int i = 0; i< wohArr.length; i++) {
            wohArr[i] = tr.getFloat(i);
        }


        //convert the arrays to matrices and set them as the layer matrices
        whi.fromArray(whiArr);
        for (int i = 0; i<hLayers; i++)
            whh[i].fromArray(whhArr[i]);
        woh.fromArray(wohArr);
    }
}
