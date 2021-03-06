package simugen.core.transfer;

import simugen.core.interfaces.Component;
import simugen.core.transfer.interfaces.InputPipe;

public final class TransferInputPipe implements InputPipe<ElementTransferData>
{
	private final Component owner;

	public TransferInputPipe(Component owner)
	{
		this.owner = owner;
	}

	@Override
	public void getPipeData(ElementTransferData pipeData)
	{
		this.owner.receiveData(pipeData);
	}

	@Override
	public boolean isReady(ElementTransferData pipeData)
	{
		return owner.canReceiveElement(pipeData);
	}

	public Component getOwner()
	{
		return owner;
	}
}
