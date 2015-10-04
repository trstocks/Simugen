package simugen.core.defaults;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.random.MersenneTwister;

import simugen.core.interfaces.LoggingStyle;
import simugen.core.interfaces.SimEngine;
import simugen.core.interfaces.SimEvent;
import simugen.core.interfaces.SimEventListener;
import simugen.core.interfaces.SimModel;

public class DefaultSimEngine implements SimEngine
{
	private SimModel internalModel;

	private PrintStream streamOut;

	private LoggingStyle logging;

	private boolean forceStop;

	private MersenneTwister doubleGenerator;

	private long seed;

	private List<SimEventListener> listListeners = new ArrayList<>();

	public void setModel(SimModel model)
	{
		internalModel = model;
	}

	public void start()
	{
		start(new MersenneTwister().nextLong());
	}

	public void start(long seed)
	{
		this.seed = seed;

		if (streamOut == null)
		{
			streamOut = System.out;
		}
		if (internalModel == null)
		{
			throw new IllegalStateException("Model has not been set");
		}
		else if (!internalModel.isReady())
		{
			throw new IllegalStateException("Model has not readied up");
		}

		doubleGenerator = new MersenneTwister(seed);

		forceStop = false;

		internalModel.startUp();

		internalModel.getListeners();

		SimEvent e = getNextEvent();

		while (e != null && !forceStop)
		{
			print(e.printEvent(logging), logging);

			e = getNextEvent();

			listListeners.forEach(new SimEventConsumer(e));

			if (e instanceof ModelFinishedEvent)
			{
				print(e.printEvent(logging), logging);

				e = null;
			}
		}

		if (forceStop)
		{
			print("Engine was forcibly stopped.", LoggingStyle.ERR);
		}
	}

	private SimEvent getNextEvent()
	{
		return internalModel.getNextEvent(this);
	}

	public void stop()
	{
		forceStop = true;
	}

	public double getNext()
	{
		return doubleGenerator.nextDouble();
	}

	public void print(String message, LoggingStyle style)
	{
		if (message == null)
		{
			return;
		}
		switch (style)
		{
		case DATA:
			streamOut.println(message);
			break;
		case DEBUG:
			streamOut.println("[DEBUG] " + message);
			break;
		case ERR:
		{
			if (streamOut.equals(System.out))
			{
				System.err.println("[ERROR] " + message);
			}
			else
			{
				streamOut.println("[ERROR]" + message);
			}
		}
		}
	}

	@Override
	public void setStreamOut(PrintStream out)
	{
		this.streamOut = out;
	}

	@Override
	public void setLoggingStyle(LoggingStyle log)
	{
		this.logging = log;
	}

	@Override
	public long getSeed()
	{
		return seed;
	}

	@Override
	public void addEventListener(SimEventListener e)
	{
		listListeners.add(e);
	}
}