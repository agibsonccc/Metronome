package tv.floe.metronome.classification.neuralnetworks.core.neurons;

import java.util.ArrayList;
import java.util.Arrays;

import tv.floe.metronome.classification.neuralnetworks.conf.Config;
import tv.floe.metronome.classification.neuralnetworks.core.Connection;
import tv.floe.metronome.classification.neuralnetworks.core.Layer;
import tv.floe.metronome.classification.neuralnetworks.core.Weight;
import tv.floe.metronome.classification.neuralnetworks.input.InputFunction;
import tv.floe.metronome.classification.neuralnetworks.input.WeightedSum;
import tv.floe.metronome.classification.neuralnetworks.transfer.Linear;
import tv.floe.metronome.classification.neuralnetworks.transfer.Sigmoid;
import tv.floe.metronome.classification.neuralnetworks.transfer.Step;
import tv.floe.metronome.classification.neuralnetworks.transfer.TransferFunction;

public class Neuron {
	
	public Layer parentLayer = null;
	public ArrayList<Connection> inConnections = null;
	public ArrayList<Connection> outConnections = null;
	
	protected InputFunction inputFunction;
	protected TransferFunction transferFunction;
    private String label;
    double netInput = 0;
        	
	
    /**
	 * Total net input for this neuron. Represents total input for this neuron
	 * received from input function.
	 */
	protected transient double totalNetInputFunctionInput = 0;

	/**
	 * the value this neuron outputs
	 */
	protected transient double output = 0;

	/**
	 * the amount of error for the neuron
	 */
	protected transient double error = 0;
	
	public Neuron() {
		
		this.inputFunction = new WeightedSum();
		this.transferFunction = new Step();
		
		
		this.inConnections = new ArrayList<Connection>();
		this.outConnections = new ArrayList<Connection>();
		
		//this.inputFunction = new WeightedSum();
		//this.transferFunction = new Step();

		//this.inputConnections = new Connection[0];
        
		//this.outConnections = new Connection[0];		
		
		
	}

	
	public Neuron( InputFunction inputFunc, TransferFunction transFunc) {
		
		this.inConnections = new ArrayList<Connection>();
		this.outConnections = new ArrayList<Connection>();
		
		this.inputFunction = inputFunc;
		this.transferFunction = transFunc;
		
		
	}
	
	
	/**
	 * TODO
	 * - InputFunction
	 * - TransferFunction
	 * 
	 * 
	 * 
	 * @param c
	 * @param layerIndex
	 * @return
	 */
	public static Neuron createNeuron(Config c, int layerIndex) {
		
		Neuron n = null; 
		TransferFunction tf = null;
		
		if (0 == layerIndex) {
			n = new InputNeuron();
			//tf = new Linear();
			
		} else {
			n = new Neuron(new WeightedSum(), new Sigmoid());
			// tf = new Linear();
			//tf = new Sigmoid();
		}
		
		
		
		return n;
		
		
	}
	
	public Weight[] getWeights() {
		
		Weight[] weights = new Weight[inConnections.size()];
		for(int i = 0; i< inConnections.size(); i++) {
			weights[i] = inConnections.get(i).getWeight();
		}
		return weights;

		
	}
	
	public void randomizeWeights() {
		
		for(Connection connection : this.inConnections) {
			
			connection.getWeight().randomize();
			
		}		
		
		
	}
	
	public void randomizeWeights(double min, double max) {
		for(Connection connection : this.inConnections) {
			connection.getWeight().randomize(min, max);
		}
	}	
	
	public void initWeights(double val) {
		
        for(Connection connection : this.inConnections) {
        	
            connection.getWeight().setValue(val);
            
       }
		
	}
	
	public void reset() {
		
		
	}
	
	public void calcOutput() {
		
        if ((this.inConnections.size() > 0)) {
        		this.netInput = this.inputFunction.getOutput(this.inConnections);
        }
        
        //System.out.println( "> net input: " + this.netInput + ", conn size: " + this.inConnections.size() );

        this.output = this.transferFunction.getOutput(this.netInput);
		
		
	}
	
    public void removeAllInputConnections() {
        // run through all input connections
        for(int i = 0; i < inConnections.size(); i++) {
        	inConnections.get(i).getFromNeuron().removeOutputConnection(inConnections.get(i));    
        	inConnections.set(i,  null );                                   
        }
        
        this.inConnections = new ArrayList<Connection>();
    }
    
    public void removeAllOutputConnections() {
        for(int i = 0; i <  outConnections.size(); i++) {
            outConnections.get(i).getToNeuron().removeInputConnection(outConnections.get(i));
            outConnections.set( i, null );
        }            
        this.outConnections = new ArrayList<Connection>();                
    }
    
    public void removeAllConnections() {
        removeAllInputConnections();
        removeAllOutputConnections();
    }	
	
	
    public boolean hasOutputConnectionTo(Neuron neuron) {
        for(Connection connection : outConnections) {
            if (connection.getToNeuron() == neuron) {
                return true;
            }
        }            
        return false;            
    }
    
    public boolean hasInputConnectionFrom(Neuron neuron) {
        for(Connection connection : inConnections) {
            if (connection.getFromNeuron() == neuron) {
                return true;
            }
        }            
        return false;            
    }
    
    
    
    
    
    
    
    
	
	
	public void addInConnection(Connection connection) throws Exception {     
        // check whaeather connection is  null
        if (connection == null) {
            throw new Exception("Attempt to add null connection to neuron!");
        }
              
        // make sure that connection instance is pointing to this neuron
        if (connection.getToNeuron() != this) {
            throw new Exception("Cannot add input connection - bad toNeuron specified!");
        } 
        
        if (this.hasInputConnectionFrom(connection.getFromNeuron())) {
        	System.out.println( "> already connected!" );
            return;
        }            
        
        //this.inConnections =  Arrays.copyOf(inputConnections, inputConnections.length+1);     // grow existing connections  array to make space for new connection
        //this.inConnections[inputConnections.length - 1] = connection;
        
        this.inConnections.add(connection);
        
        //System.out.println( "> len: " + this.inConnections.size() );
        
        Neuron fromNeuron = connection.getFromNeuron();
        fromNeuron.addOutConnection(connection);                    
	}
	
	/**
	 * Adds input connection from specified neuron
	 *
	 * @param fromNeuron
	 *            neuron to connect from
	 * @throws Exception 
	 */
	public void addInConnection(Neuron fromNeuron) throws Exception {
		Connection connection = new Connection(fromNeuron, this);
		this.addInConnection(connection);
	}
	
	/**
	 * Adds input connection with the given weight, from given neuron
	 * 
	 * @param fromNeuron
	 *            neuron to connect from
	 * @param weightVal
	 *	      connection weight value
	 * @throws Exception 
	 * 
	 */	
	public void addInConnection(Neuron fromNeuron, double weightVal) throws Exception {
		Connection connection = new Connection(fromNeuron, this, weightVal);
		this.addInConnection(connection);
	}	
	
	// TODO: check the connection state
	private void addOutConnection(Connection c) {

		this.outConnections.add(c);
		
	}
	
	
	
	public ArrayList<Connection> getInConnections() {
		return this.inConnections;
	}

	public ArrayList<Connection> getOutConnections() {
		return this.outConnections;
	}
	
	
	public double getOutput() {
		
		return this.output;
		
	}
	
	public void setInput(double input) {
		this.netInput = input;
	}

	public double getNetInput() {
		return this.netInput;
	}
	
	public double getError() {
		return error;
	}

	public void setError(double error) {
		this.error = error;
	}
	
	
	public void setParentLayer(Layer p) {
		this.parentLayer = p;
	}

	public Layer getParentLayer() {
		return this.parentLayer;
	}
	
	public TransferFunction getTransferFunction() {
		return this.transferFunction;
	}

	
	
    protected void removeInputConnection(Connection conn) {
    	
    	this.inConnections.remove(conn);

    }
    
    protected void removeOutputConnection(Connection conn) {
    	
    	this.outConnections.remove(conn);

    }        

	public void removeInputConnectionFrom(Neuron fromNeuron) {

        for (int x = 0; x < inConnections.size(); x++) {

        	if (inConnections.get(x).getFromNeuron() == fromNeuron) {
        		
        		fromNeuron.removeOutputConnection(inConnections.get(x));
        		
                this.removeInputConnection(inConnections.get(x));
                
                return;
                
        	} // if
        	
        } // for
	        
	}
	    
	public void removeOutputConnectionTo(Neuron toNeuron) {
		
        for(int i = 0; i < outConnections.size(); i++) {
        	
			if (outConnections.get(i).getToNeuron() == toNeuron) {
				
				toNeuron.removeInputConnection( outConnections.get( i ) );    
				this.removeOutputConnection( outConnections.get( i ) );
				
				return;
				
			}
			
        }   
        
	}        
	
	
	

}