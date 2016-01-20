package simpledb;

import org.junit.Test;
import static org.junit.Assert.*;
import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import simpledb.systemtest.SimpleDbTestBase;
/**
 *   This set of tests was created by Kevin Chen.
 */
public class BufferPoolTest extends SimpleDbTestBase {

    @Test public void placeholder() {
        assertEquals(1, 1);
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(BufferPoolTest.class);
    }
}
