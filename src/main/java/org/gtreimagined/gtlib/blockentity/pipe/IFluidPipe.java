package org.gtreimagined.gtlib.blockentity.pipe;

import tesseract.api.IConnectable;

/**
 * A fluid pipe is the unit of interaction with fluid inventories.
 */
public interface IFluidPipe extends IConnectable {

    /**
     * Returns the maximum amount of packets that this fluid component will permit to pass through or be received in a single tick.
     *
     * @return A positive integer representing the maximum packets, zero or negative indicates that this component accepts no fluid.
     */
    int getCapacity();

    /**
     * Returns the maximum amount of pressure that this fluid component will permit to pass through or be received in a single tick.
     *
     * @return A positive integer representing the maximum amount, zero or negative indicates that this component accepts no fluid.
     */
    long getPressure();

    /**
     * Returns the maximum temperature that this fluid component will permit to pass through or be received in a single packet.
     *
     * @return A positive integer representing the maximum accepted temp, zero or negative indicates that this component accepts no fluid.
     */
    int getTemperature();

    /**
     * @return Checks that the pipe can handle gases.
     */
    boolean isGasProof();
}
