package simugen.core.components.abstracts;

import java.util.ArrayList;
import java.util.List;

import simugen.core.components.interfaces.Queue;
import simugen.core.defaults.DefaultElementQueuedContext;
import simugen.core.defaults.NullEngineTick;
import simugen.core.enums.QueueMethod;
import simugen.core.interfaces.Component;
import simugen.core.interfaces.EngineTick;
import simugen.core.transfer.ElementTransferData;
import simugen.core.transfer.TransferInputPipe;
import simugen.core.transfer.TransferOutputPipe;

public class AbstractQueue extends AbstractComponent implements Queue
{
	protected int totalCapacity = -1;

	protected final QueueMethod method;

	protected final TransferInputPipe inputPipe = new TransferInputPipe(this);

	protected final TransferOutputPipe outputPipe = new TransferOutputPipe();

	protected final List<ElementTransferData> elementsInQueue = new ArrayList<>();

	/**
	 * Default infinite queue with First in First out queuing rules.
	 */
	public AbstractQueue()
	{
		this.method = QueueMethod.FIFO;

		addProcessData();
	}

	/**
	 * Create a FIFO queue with a limited capacity.
	 * 
	 * @param totalCapacity
	 */
	public AbstractQueue(int totalCapacity)
	{
		this(totalCapacity, QueueMethod.FIFO);
	}

	/**
	 * Create an unlimited capacity queue with specified QueueMethod
	 * 
	 * @param method
	 */
	public AbstractQueue(QueueMethod method)
	{
		this.method = method;

		addProcessData();
	}

	/**
	 * Create a limited capacity queue with specified QueueMethod
	 * 
	 * @param totalCapacity
	 * @param method
	 */
	public AbstractQueue(int totalCapacity, QueueMethod method)
	{
		this(method);

		this.totalCapacity = totalCapacity;
	}

	private void addProcessData()
	{
		final DefaultElementQueuedContext context = new DefaultElementQueuedContext(
				this);

		addProcessDataContext(ElementTransferData.class, context);
	}

	/**
	 * Queues don't care what tick it is, if it can move persons out, it will.
	 */
	@Override
	public void getEvents(EngineTick tick)
	{
		current = new NullEngineTick();

		super.getEvents(tick);
	}

	@Override
	protected void generateEvents(EngineTick tick)
	{
		final int index = getNextIndex();

		final ElementTransferData eData = elementsInQueue.get(index);

		final Component to = outputPipe.getUnion().getDownStreamPipe()
				.getOwner();

		long time = tick.getEventTime(0);

		// If the element entered and exited the queue in the same tick.
		if (time < eData.getTime())
		{
			time = eData.getTime();
		}

		final ElementTransferData data = new ElementTransferData(this, to,
				eData.getData(), time);

		if (outputPipe.canSendPipeData(data))
		{
			super.events.add(outputPipe.sendPipeData(data));

			elementsInQueue.remove(eData);
		}
	}

	protected int getNextIndex()
	{
		switch (this.method)
		{
		case FIFO:
			return 0;
		case LIFO:
			return elementsInQueue.size() - 1;
		case PRIORITY:
			getNextIndexPriority();
		default:
			return -1;
		}
	}

	/**
	 * Sub-classes must override if they want to make a priority queue
	 * 
	 * @return
	 */
	protected int getNextIndexPriority()
	{
		throw new IllegalAccessError();
	}

	@Override
	public void queueElement(ElementTransferData data)
	{
		elementsInQueue.add(data);
	}

	@Override
	public int getElementCapacity()
	{
		return totalCapacity == -1 ? -1
				: totalCapacity - elementsInQueue.size();
	}

	@Override
	public TransferInputPipe getTransferInputPipe()
	{
		return this.inputPipe;
	}

	@Override
	public TransferOutputPipe getTransferOutputPipe()
	{
		return this.outputPipe;
	}

	@Override
	protected boolean canGenerate()
	{
		return !elementsInQueue.isEmpty();
	}

}
