package simpledb;

import java.util.*;

/**
 * The Aggregation operator that computes an aggregate (e.g., sum, avg, max,
 * min). Note that we only support aggregates over a single column, grouped by a
 * single column.
 */
public class Aggregate extends Operator {

    private static final long serialVersionUID = 1L;

    private DbIterator child;
    private int aggregateField;
    private int groupField;
    private Aggregator.Op op;

    private Aggregator aggregator;
    private DbIterator aggregateIterator;

    /**
     * Constructor.
     * 
     * Implementation hint: depending on the type of afield, you will want to
     * construct an {@link IntAggregator} or {@link StringAggregator} to help
     * you with your implementation of readNext().
     * 
     * 
     * @param child
     *            The DbIterator that is feeding us tuples.
     * @param afield
     *            The column over which we are computing an aggregate.
     * @param gfield
     *            The column over which we are grouping the result, or -1 if
     *            there is no grouping
     * @param aop
     *            The aggregation operator to use
     */
    public Aggregate(DbIterator child, int afield, int gfield, Aggregator.Op aop) {
        this.child = child;
        this.aggregateField = afield;
        this.groupField = gfield;
        this.op = aop;

        Type groupByType;
        if (gfield == Aggregator.NO_GROUPING) {
            groupByType = null;
        } else {
            groupByType = child.getTupleDesc().getFieldType(gfield);
        }
        switch (child.getTupleDesc().getFieldType(this.aggregateField)) {
            case INT_TYPE:
                this.aggregator = new IntegerAggregator(gfield, groupByType, afield, aop);
                break;
            case STRING_TYPE:
                this.aggregator = new StringAggregator(gfield, groupByType, afield, aop);
                break;
            default:
                // should never happen
                break;
        }
    }

    /**
     * @return If this aggregate is accompanied by a groupby, return the groupby
     *         field index in the <b>INPUT</b> tuples. If not, return
     *         {@link simpledb.Aggregator#NO_GROUPING}
     * */
    public int groupField() {
    	return this.groupField;
    }

    /**
     * @return If this aggregate is accompanied by a group by, return the name
     *         of the groupby field in the <b>OUTPUT</b> tuples If not, return
     *         null;
     * */
    public String groupFieldName() {
	    return this.child.getTupleDesc().getFieldName(this.groupField);
    }

    /**
     * @return the aggregate field
     * */
    public int aggregateField() {
	    return this.aggregateField;
    }

    /**
     * @return return the name of the aggregate field in the <b>OUTPUT</b>
     *         tuples
     * */
    public String aggregateFieldName() {
	    return this.child.getTupleDesc().getFieldName(this.aggregateField);
    }

    /**
     * @return return the aggregate operator
     * */
    public Aggregator.Op aggregateOp() {
	    return this.op;
    }

    public static String nameOfAggregatorOp(Aggregator.Op aop) {
	return aop.toString();
    }

    public void open() throws NoSuchElementException, DbException,
	    TransactionAbortedException {
        this.child.open();
        while(this.child.hasNext()) {
            // rewind or clear aggregator??
            this.aggregator.mergeTupleIntoGroup(this.child.next());
        }
        this.aggregateIterator = this.aggregator.iterator();
        this.aggregateIterator.open();
        super.open();
    }

    /**
     * Returns the next tuple. If there is a group by field, then the first
     * field is the field by which we are grouping, and the second field is the
     * result of computing the aggregate, If there is no group by field, then
     * the result tuple should contain one field representing the result of the
     * aggregate. Should return null if there are no more tuples.
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        if (this.aggregateIterator.hasNext()) {
            return this.aggregateIterator.next();
        } else {
            return null;
        }
    }

    public void rewind() throws DbException, TransactionAbortedException {
        this.child.rewind(); // ??
        this.aggregateIterator.rewind();
    }

    /**
     * Returns the TupleDesc of this Aggregate. If there is no group by field,
     * this will have one field - the aggregate column. If there is a group by
     * field, the first field will be the group by field, and the second will be
     * the aggregate value column.
     * 
     * The name of an aggregate column should be informative. For example:
     * "aggName(aop) (child_td.getFieldName(afield))" where aop and afield are
     * given in the constructor, and child_td is the TupleDesc of the child
     * iterator.
     */
    public TupleDesc getTupleDesc() {
	    return this.aggregator.getTupleDesc();
    }

    public void close() {
        super.close();
        this.aggregateIterator.close();
        this.child.close();
    }

    @Override
    public DbIterator[] getChildren() {
	// some code goes here
	return null;
    }

    @Override
    public void setChildren(DbIterator[] children) {
	// some code goes here
    }
    
}
