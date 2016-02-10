package simpledb;

import java.io.IOException;

/**
 * Inserts tuples read from the child operator into the tableid specified in the
 * constructor
 */
public class Insert extends Operator {

    private static final long serialVersionUID = 1L;

    private TransactionId transactionId;
    private DbIterator child;
    private int tableId;

    private TupleDesc resultDescriptor;
    private boolean fetched;

    /**
     * Constructor.
     * 
     * @param t
     *            The transaction running the insert.
     * @param child
     *            The child operator from which to read tuples to be inserted.
     * @param tableid
     *            The table in which to insert tuples.
     * @throws DbException
     *             if TupleDesc of child differs from table into which we are to
     *             insert.
     */
    public Insert(TransactionId t, DbIterator child, int tableid)
            throws DbException {
        this.transactionId = t;
        this.child = child;
        this.tableId = tableid;

        HeapFile heapFile = (HeapFile) Database.getCatalog().getDatabaseFile(tableId);
        if (!this.child.getTupleDesc().equals(heapFile.getTupleDesc())) {
            throw new DbException("Tuple descriptor mismatch");
        }

        Type[] types = new Type[] { Type.INT_TYPE };
        String[] fields = new String[] { null };
        this.resultDescriptor = new TupleDesc(types, fields);
        this.fetched = false;
    }

    public TupleDesc getTupleDesc() {
        return this.resultDescriptor;
    }

    public void open() throws DbException, TransactionAbortedException {
        this.child.open();
        super.open();
    }

    public void close() {
        super.close();
        this.child.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        this.child.rewind();
        this.fetched = false; // perchance?
    }

    /**
     * Inserts tuples read from child into the tableid specified by the
     * constructor. It returns a one field tuple containing the number of
     * inserted records. Inserts should be passed through BufferPool. An
     * instances of BufferPool is available via Database.getBufferPool(). Note
     * that insert DOES NOT need check to see if a particular tuple is a
     * duplicate before inserting it.
     * 
     * @return A 1-field tuple containing the number of inserted records, or
     *         null if called more than once.
     * @see Database#getBufferPool
     * @see BufferPool#insertTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        if (!this.fetched) {
            int count = 0;
            while (this.child.hasNext()) {
                Tuple tuple = this.child.next();
                try {
                    Database.getBufferPool().insertTuple(this.transactionId, this.tableId, tuple);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new TransactionAbortedException();
                }
                count++;
            }

            Tuple resultTuple = new Tuple(this.resultDescriptor);
            resultTuple.setField(0, new IntField(count));
            this.fetched = true;
            return resultTuple;
        } else {
            return null;
        }
    }

    @Override
    public DbIterator[] getChildren() {
        return new DbIterator[] { this.child };
    }

    @Override
    public void setChildren(DbIterator[] children) {
        this.child = children[0];
    }
}
