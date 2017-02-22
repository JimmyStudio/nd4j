package org.nd4j.linalg.profiling;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.executioner.GridExecutioner;
import org.nd4j.linalg.api.ops.executioner.OpExecutioner;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.profiler.OpProfiler;
import org.nd4j.linalg.profiler.data.StackAggregator;
import org.nd4j.linalg.profiler.data.primitives.StackDescriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author raver119@gmail.com
 */
@Slf4j
public class StackAggregatorTests {

    @Before
    public void setUp() {
        Nd4j.getExecutioner().setProfilingMode(OpExecutioner.ProfilingMode.ALL);
        OpProfiler.getInstance().reset();
    }

    @After
    public void tearDown() {
        Nd4j.getExecutioner().setProfilingMode(OpExecutioner.ProfilingMode.DISABLED);
    }


    @Test
    public void testBasicBranching1() {
        StackAggregator aggregator = new StackAggregator();

        aggregator.incrementCount();

        aggregator.incrementCount();

        assertEquals(2, aggregator.getTotalEventsNumber());
        assertEquals(2, aggregator.getUniqueBranchesNumber());
    }

    @Test
    public void testBasicBranching2() {
        StackAggregator aggregator = new StackAggregator();

        for (int i = 0; i < 10; i++) {
            aggregator.incrementCount();
        }

        assertEquals(10, aggregator.getTotalEventsNumber());

        // simnce method is called in loop, there should be only 1 unique code branch
        assertEquals(1, aggregator.getUniqueBranchesNumber());
    }


    @Test
    public void testTrailingFrames1() {
        StackAggregator aggregator = new StackAggregator();
        aggregator.incrementCount();


        StackDescriptor descriptor = aggregator.getLastDescriptor();

        log.info("Trace: {}", descriptor.toString());

        // we just want to make sure that OpProfiler methods are NOT included in trace
        assertTrue(descriptor.getStackTrace()[descriptor.size() - 1].getClassName().contains("StackAggregatorTests"));
    }

    @Test
    public void testTrailingFrames2() {
        INDArray x = Nd4j.create(new int[] {10, 10}, 'f');
        INDArray y = Nd4j.create(new int[] {10, 10}, 'c');

        x.assign(y);


        x.assign(y);

        if (Nd4j.getExecutioner() instanceof GridExecutioner)
            ((GridExecutioner) Nd4j.getExecutioner()).flushQueueBlocking();

        StackAggregator aggregator = OpProfiler.getInstance().getMixedOrderAggregator();

        StackDescriptor descriptor = aggregator.getLastDescriptor();

        log.info("Trace: {}", descriptor.toString());

        assertEquals(2, aggregator.getTotalEventsNumber());
        assertEquals(2, aggregator.getUniqueBranchesNumber());

        aggregator.renderTree();
    }

    @Test
    public void testScalarAggregator() {
        INDArray x = Nd4j.create(10);

        x.putScalar(0, 1.0);

        double x_0 = x.getDouble(0);

        assertEquals(1.0, x_0, 1e-5);

        StackAggregator aggregator = OpProfiler.getInstance().getScalarAggregator();

        assertEquals(2, aggregator.getTotalEventsNumber());
        assertEquals(2, aggregator.getUniqueBranchesNumber());

        aggregator.renderTree(false);
    }
}
